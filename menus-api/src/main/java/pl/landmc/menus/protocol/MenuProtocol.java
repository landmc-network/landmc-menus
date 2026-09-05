package pl.landmc.menus.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The wire format menus travel in, between the plugin that owns a list and the server that draws
 * it.
 *
 * <p>A plugin message rather than the Redis bus, because this is addressed to one player on one
 * backend and the proxy already has that connection in its hand. The bus is for state that has
 * to reach every node; a menu reaches exactly one.
 *
 * <p>Every read is bounded. The bytes arrive over a player's connection, so they may have been
 * written by anything: a length field that is trusted is a length field that allocates a
 * gigabyte array when somebody sends four bytes of {@code 0xFF}.
 */
public final class MenuProtocol {

    /** The channel both sides register. */
    public static final String CHANNEL = "landmc:menu";

    /**
     * Bumped when the meaning of existing bytes changes, never for an addition at the end.
     *
     * <p>An older backend reading a newer payload has to be able to tell "I do not understand
     * this" from "this is corrupt", and stop, rather than draw half a menu.
     */
    private static final byte VERSION = 1;

    private static final byte OPEN = 1;
    private static final byte ACTION = 2;

    /** Well past any real friend list or history, and far short of anything worth worrying about. */
    private static final int MAXIMUM_ENTRIES = 1_000;

    private MenuProtocol() {
    }

    // --- writing -------------------------------------------------------------------------

    /** Encodes a menu's contents, to be sent to the server the player is on. */
    public static byte[] encode(MenuPayload payload) {
        Objects.requireNonNull(payload, "payload");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeByte(VERSION);
            out.writeByte(OPEN);
            out.writeByte(payload.kind().id());

            switch (payload) {
                case MenuPayload.Friends friends -> writeFriends(out, friends);
                case MenuPayload.Punishments punishments -> writePunishments(out, punishments);
                case MenuPayload.Servers servers -> writeServers(out, servers);
                case MenuPayload.Profile profile -> writeProfile(out, profile);
            }
        }
        catch (IOException exception) {
            // A ByteArrayOutputStream does not fail; this cannot happen, and pretending it
            // might would put a checked exception on every caller for nothing.
            throw new IllegalStateException("Could not encode a menu payload", exception);
        }

        return bytes.toByteArray();
    }

    /** Encodes a click, to be sent back to the plugin that can act on it. */
    public static byte[] encode(MenuAction action) {
        Objects.requireNonNull(action, "action");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeByte(VERSION);
            out.writeByte(ACTION);
            out.writeByte(action.menu().id());
            out.writeUTF(action.action());
            out.writeUTF(action.argument());
        }
        catch (IOException exception) {
            throw new IllegalStateException("Could not encode a menu action", exception);
        }

        return bytes.toByteArray();
    }

    private static void writeFriends(DataOutputStream out, MenuPayload.Friends payload)
            throws IOException {

        out.writeInt(payload.pendingRequests());
        out.writeInt(payload.friends().size());

        for (MenuPayload.Friends.Friend friend : payload.friends()) {
            out.writeUTF(friend.name());
            out.writeBoolean(friend.online());
            out.writeUTF(friend.server());
        }
    }

    private static void writePunishments(DataOutputStream out, MenuPayload.Punishments payload)
            throws IOException {

        out.writeUTF(payload.subject());
        out.writeInt(payload.punishments().size());

        for (MenuPayload.Punishments.Punishment punishment : payload.punishments()) {
            out.writeLong(punishment.id());
            out.writeUTF(punishment.type());
            out.writeUTF(punishment.reason());
            out.writeUTF(punishment.staff());
            out.writeLong(punishment.issuedAt());
            out.writeLong(punishment.expiresAt());
            out.writeBoolean(punishment.active());
        }
    }

    private static void writeProfile(DataOutputStream out, MenuPayload.Profile payload)
            throws IOException {

        out.writeUTF(payload.playerName());
        out.writeUTF(payload.rank());
        out.writeInt(payload.friends());
        out.writeInt(payload.pendingRequests());
        out.writeUTF(payload.currentServer());
    }

    private static void writeServers(DataOutputStream out, MenuPayload.Servers payload)
            throws IOException {

        out.writeUTF(payload.currentServer());
        out.writeInt(payload.servers().size());

        for (MenuPayload.Servers.Server server : payload.servers()) {
            out.writeUTF(server.id());
            out.writeUTF(server.displayName());
            out.writeInt(server.online());
            out.writeBoolean(server.reachable());
        }
    }

    // --- reading -------------------------------------------------------------------------

    /**
     * Reads a menu's contents.
     *
     * @throws MenuProtocolException when the bytes are not a payload this version understands
     */
    public static MenuPayload decodePayload(byte[] message) {
        Objects.requireNonNull(message, "message");

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            requireHeader(in, OPEN);

            MenuKind kind = MenuKind.byId(in.readUnsignedByte())
                    .orElseThrow(() -> new MenuProtocolException("Unknown menu kind"));

            return switch (kind) {
                case FRIENDS -> readFriends(in);
                case PUNISHMENTS -> readPunishments(in);
                case SERVERS -> readServers(in);
                case PROFILE -> readProfile(in);
            };
        }
        catch (IOException exception) {
            throw new MenuProtocolException("Truncated menu payload", exception);
        }
    }

    /** Reads a click. */
    public static MenuAction decodeAction(byte[] message) {
        Objects.requireNonNull(message, "message");

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            requireHeader(in, ACTION);

            MenuKind menu = MenuKind.byId(in.readUnsignedByte())
                    .orElseThrow(() -> new MenuProtocolException("Unknown menu kind"));

            String action = in.readUTF();
            String argument = in.readUTF();

            if (action.isEmpty()
                    || action.length() > MenuAction.MAXIMUM_LENGTH
                    || argument.length() > MenuAction.MAXIMUM_LENGTH) {
                throw new MenuProtocolException("Menu action out of range");
            }

            return new MenuAction(menu, action, argument);
        }
        catch (IOException exception) {
            throw new MenuProtocolException("Truncated menu action", exception);
        }
    }

    /**
     * Whether a message was written by a build that speaks this version.
     *
     * <p>Lets a reader tell "the other side is newer than me" from "these bytes are nonsense",
     * which are worth different answers: the first is a rolling restart in progress and worth
     * telling the player about, the second is somebody poking at the channel and worth nothing.
     */
    public static boolean isKnownVersion(byte[] message) {
        return message.length >= 1 && message[0] == VERSION;
    }

    /** Whether a message is an action rather than a payload, without decoding it. */
    public static boolean isAction(byte[] message) {
        return message.length >= 2 && message[0] == VERSION && message[1] == ACTION;
    }

    private static void requireHeader(DataInputStream in, byte expectedKind) throws IOException {
        int version = in.readUnsignedByte();
        if (version != VERSION) {
            throw new MenuProtocolException(
                    "Menu protocol version " + version + "; this build speaks " + VERSION);
        }

        int kind = in.readUnsignedByte();
        if (kind != expectedKind) {
            throw new MenuProtocolException("Menu message is not the kind that was expected");
        }
    }

    private static MenuPayload readFriends(DataInputStream in) throws IOException {
        int pending = in.readInt();
        int count = readCount(in);

        List<MenuPayload.Friends.Friend> friends = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            friends.add(new MenuPayload.Friends.Friend(
                    in.readUTF(), in.readBoolean(), in.readUTF()));
        }

        return new MenuPayload.Friends(friends, Math.max(0, pending));
    }

    private static MenuPayload readPunishments(DataInputStream in) throws IOException {
        String subject = in.readUTF();
        int count = readCount(in);

        List<MenuPayload.Punishments.Punishment> punishments = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            punishments.add(new MenuPayload.Punishments.Punishment(
                    in.readLong(),
                    in.readUTF(),
                    in.readUTF(),
                    in.readUTF(),
                    in.readLong(),
                    in.readLong(),
                    in.readBoolean()));
        }

        return new MenuPayload.Punishments(subject, punishments);
    }

    private static MenuPayload readProfile(DataInputStream in) throws IOException {
        return new MenuPayload.Profile(
                in.readUTF(),
                in.readUTF(),
                Math.max(0, in.readInt()),
                Math.max(0, in.readInt()),
                in.readUTF());
    }

    private static MenuPayload readServers(DataInputStream in) throws IOException {
        String current = in.readUTF();
        int count = readCount(in);

        List<MenuPayload.Servers.Server> servers = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            servers.add(new MenuPayload.Servers.Server(
                    in.readUTF(), in.readUTF(), in.readInt(), in.readBoolean()));
        }

        return new MenuPayload.Servers(current, servers);
    }

    /**
     * Reads an entry count and refuses an impossible one.
     *
     * <p>The list is sized from this number, so a value that is not checked is an allocation
     * the sender chooses. Four bytes of nonsense would otherwise ask for a two-billion-element
     * list before a single entry has been read.
     */
    private static int readCount(DataInputStream in) throws IOException {
        int count = in.readInt();
        if (count < 0 || count > MAXIMUM_ENTRIES) {
            throw new MenuProtocolException("Menu payload claims " + count + " entries");
        }
        return count;
    }
}
