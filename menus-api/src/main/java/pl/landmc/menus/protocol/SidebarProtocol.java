package pl.landmc.menus.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * What a backend needs to draw on its sidebar and cannot know by itself.
 *
 * <p>Today that is one number: how many diamonds the player has. It lives on the proxy, and a
 * scoreboard redraws every second or two per player - reading it from the database at that rate
 * would be a query per player per tick-and-a-bit for a value that changes a few times a day. So
 * the proxy pushes it when it changes and the backend keeps the last thing it was told.
 *
 * <p>This sits beside the menu protocol, and shares its artifact, because it is the same seam:
 * data the proxy owns, sent to the server that displays it. The artifact's name has outgrown its
 * contents - if a third thing joins these two, it is worth splitting out and naming for what it
 * really is.
 *
 * <p>Push, not request-and-answer. A backend that asked would have to wait for a reply before it
 * could draw, and the first frame of a scoreboard is the one a player sees on arrival.
 */
public final class SidebarProtocol {

    /** The channel both sides register. */
    public static final String CHANNEL = "landmc:sidebar";

    /**
     * Bumped when the meaning of existing bytes changes, never for an addition at the end.
     *
     * <p>During a rolling restart an older backend reads a newer proxy's messages. It has to be
     * able to tell "I do not understand this" from "this is corrupt", and leave the sidebar as
     * it was rather than draw a wrong number.
     */
    private static final byte VERSION = 1;

    private SidebarProtocol() {
    }

    /** Encodes what the player has, to be sent to the server they are standing on. */
    public static byte[] encode(long balance) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeByte(VERSION);
            out.writeLong(balance);
        }
        catch (IOException exception) {
            // A ByteArrayOutputStream does not fail; pretending it might would put a checked
            // exception on every caller for nothing.
            throw new IllegalStateException("Could not encode a sidebar update", exception);
        }
        return bytes.toByteArray();
    }

    /**
     * Reads a balance.
     *
     * <p>Never negative, whatever arrives: this number is shown to a player about their own
     * money, and the one thing worse than a stale figure is an impossible one.
     *
     * @throws MenuProtocolException when the bytes are not an update this version understands
     */
    public static long decodeBalance(byte[] message) {
        Objects.requireNonNull(message, "message");

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            int version = in.readUnsignedByte();
            if (version != VERSION) {
                throw new MenuProtocolException(
                        "Sidebar protocol version " + version + "; this build speaks " + VERSION);
            }
            return Math.max(0L, in.readLong());
        }
        catch (IOException exception) {
            throw new MenuProtocolException("Truncated sidebar update", exception);
        }
    }
}
