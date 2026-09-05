package pl.landmc.menus.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The wire format, including what happens when the bytes are not what they claim to be.
 *
 * <p>These messages arrive over a player's own connection, so "not what they claim to be" is not
 * a hypothetical: anybody with a modified client can write whatever they like onto this channel.
 * Half of what follows is therefore about refusing input rather than about reading it.
 */
class MenuProtocolTest {

    @Test
    @DisplayName("a friend list comes back as it went out")
    void roundTripsFriends() {
        MenuPayload.Friends original = new MenuPayload.Friends(
                List.of(
                        new MenuPayload.Friends.Friend("Crispi", true, "lobby"),
                        new MenuPayload.Friends.Friend("Anna", false, ""),
                        new MenuPayload.Friends.Friend("Żółć_123", true, "skyblock-1")),
                4);

        MenuPayload decoded = MenuProtocol.decodePayload(MenuProtocol.encode(original));

        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("a punishment history comes back as it went out")
    void roundTripsPunishments() {
        MenuPayload.Punishments original = new MenuPayload.Punishments(
                "Crispi",
                List.of(
                        new MenuPayload.Punishments.Punishment(
                                42L, "BAN", "oszukiwanie", "Admin", 1_700_000_000_000L, 0L, true),
                        new MenuPayload.Punishments.Punishment(
                                41L, "WARN", "spam", "Moderator", 1_600_000_000_000L,
                                1_600_003_600_000L, false)));

        assertEquals(original, MenuProtocol.decodePayload(MenuProtocol.encode(original)));
    }

    @Test
    @DisplayName("a server list comes back as it went out")
    void roundTripsServers() {
        MenuPayload.Servers original = new MenuPayload.Servers(
                "lobby",
                List.of(
                        new MenuPayload.Servers.Server("lobby", "Lobby", 42, true),
                        new MenuPayload.Servers.Server("skyblock-1", "SkyBlock", 0, false)));

        assertEquals(original, MenuProtocol.decodePayload(MenuProtocol.encode(original)));
    }

    @Test
    @DisplayName("a profile comes back as it went out")
    void roundTripsAProfile() {
        MenuPayload.Profile original = new MenuPayload.Profile(
                "Crispi", "Administrator", 12, 3, "lobby");

        assertEquals(original, MenuProtocol.decodePayload(MenuProtocol.encode(original)));

        // A network with no rank system sends an empty rank rather than leaving the field out.
        MenuPayload.Profile plain = new MenuPayload.Profile("Anna", "", 0, 0, "");
        assertEquals(plain, MenuProtocol.decodePayload(MenuProtocol.encode(plain)));
    }

    @Test
    @DisplayName("an empty list is a valid menu, not an error")
    void roundTripsAnEmptyList() {
        // Somebody with no friends still opens the menu; it just says so.
        MenuPayload.Friends empty = new MenuPayload.Friends(List.of(), 0);

        assertEquals(empty, MenuProtocol.decodePayload(MenuProtocol.encode(empty)));
    }

    @Test
    @DisplayName("an action comes back as it went out")
    void roundTripsAnAction() {
        MenuAction original = MenuAction.of(MenuKind.SERVERS, "connect", "skyblock-1");

        assertEquals(original, MenuProtocol.decodeAction(MenuProtocol.encode(original)));

        MenuAction withoutArgument = MenuAction.of(MenuKind.FRIENDS, "requests");
        assertEquals(withoutArgument, MenuProtocol.decodeAction(MenuProtocol.encode(withoutArgument)));
    }

    @Test
    @DisplayName("an action is recognised without being decoded")
    void tellsAnActionFromAPayload() {
        assertTrue(MenuProtocol.isAction(MenuProtocol.encode(MenuAction.of(MenuKind.FRIENDS, "x"))));
        assertFalse(MenuProtocol.isAction(
                MenuProtocol.encode(new MenuPayload.Friends(List.of(), 0))));
        assertFalse(MenuProtocol.isAction(new byte[0]));
        assertFalse(MenuProtocol.isAction(new byte[] {1}));
    }

    @Test
    @DisplayName("a payload read as an action is refused, and the other way round")
    void refusesTheWrongKindOfMessage() {
        byte[] payload = MenuProtocol.encode(new MenuPayload.Friends(List.of(), 0));
        byte[] action = MenuProtocol.encode(MenuAction.of(MenuKind.FRIENDS, "remove", "Anna"));

        assertThrows(MenuProtocolException.class, () -> MenuProtocol.decodeAction(payload));
        assertThrows(MenuProtocolException.class, () -> MenuProtocol.decodePayload(action));
    }

    @Test
    @DisplayName("a message from a version this build does not speak is refused, not guessed at")
    void refusesAnotherVersion() {
        byte[] message = MenuProtocol.encode(new MenuPayload.Friends(List.of(), 0));
        message[0] = 99;

        MenuProtocolException failure = assertThrows(
                MenuProtocolException.class, () -> MenuProtocol.decodePayload(message));

        // The message has to say which version it saw: during a rolling restart this is the
        // line that explains why one backend draws menus and another does not.
        assertTrue(failure.getMessage().contains("99"), failure.getMessage());
    }

    @Test
    @DisplayName("a count that could not be real is refused before anything is allocated")
    void refusesAnImpossibleCount() throws IOException {
        // What four bytes of 0xFF ask for: a two-billion-element list, before a single entry
        // has been read. Trusting this field is how a plugin message becomes an outage.
        assertThrows(MenuProtocolException.class, () -> MenuProtocol.decodePayload(
                friendsHeaderWithCount(Integer.MAX_VALUE)));

        assertThrows(MenuProtocolException.class, () -> MenuProtocol.decodePayload(
                friendsHeaderWithCount(-1)));

        assertThrows(MenuProtocolException.class, () -> MenuProtocol.decodePayload(
                friendsHeaderWithCount(1_001)));
    }

    @Test
    @DisplayName("a message that stops in the middle is refused rather than half-read")
    void refusesATruncatedMessage() {
        byte[] full = MenuProtocol.encode(new MenuPayload.Friends(
                List.of(new MenuPayload.Friends.Friend("Crispi", true, "lobby")), 0));

        for (int length = 0; length < full.length; length++) {
            byte[] truncated = new byte[length];
            System.arraycopy(full, 0, truncated, 0, length);

            assertThrows(
                    MenuProtocolException.class,
                    () -> MenuProtocol.decodePayload(truncated),
                    "a message cut to " + length + " bytes was accepted");
        }
    }

    @Test
    @DisplayName("random bytes never decode into a menu")
    void refusesNoise() {
        java.util.Random random = new java.util.Random(20260905L);

        for (int attempt = 0; attempt < 500; attempt++) {
            byte[] noise = new byte[random.nextInt(64)];
            random.nextBytes(noise);

            try {
                MenuProtocol.decodePayload(noise);
            }
            catch (MenuProtocolException expected) {
                continue;
            }
            catch (RuntimeException unexpected) {
                throw new AssertionError(
                        "noise threw something other than MenuProtocolException: " + unexpected,
                        unexpected);
            }
            // Decoding cleanly is acceptable only if the noise really was a valid message,
            // which for these lengths it cannot be.
            throw new AssertionError("random bytes decoded as a menu payload");
        }
    }

    @Test
    @DisplayName("an action longer than a name is refused at both ends of the wire")
    void refusesAnOversizedAction() {
        String tooLong = "a".repeat(MenuAction.MAXIMUM_LENGTH + 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> MenuAction.of(MenuKind.FRIENDS, tooLong));
        assertThrows(
                IllegalArgumentException.class,
                () -> MenuAction.of(MenuKind.FRIENDS, "remove", tooLong));
        assertThrows(
                IllegalArgumentException.class,
                () -> MenuAction.of(MenuKind.FRIENDS, ""));
    }

    @Test
    @DisplayName("a menu id keeps its number, because a rolling restart mixes builds")
    void pinsTheMenuIds() {
        // These numbers are on the wire. Changing one makes an older backend open a different
        // menu than the proxy asked for, and nothing anywhere reports it.
        assertEquals(1, MenuKind.FRIENDS.id());
        assertEquals(2, MenuKind.PUNISHMENTS.id());
        assertEquals(3, MenuKind.SERVERS.id());
        assertEquals(4, MenuKind.PROFILE.id());
        assertEquals(5, MenuKind.SHOP.id());
        assertEquals(6, MenuKind.RANKS.id());
        assertEquals(7, MenuKind.VISUAL_RANKS.id());

        for (MenuKind kind : MenuKind.values()) {
            assertEquals(kind, MenuKind.byId(kind.id()).orElseThrow());
        }
        assertTrue(MenuKind.byId(99).isEmpty());
    }

    @Test
    @DisplayName("a full-sized friend list still fits in one plugin message")
    void staysWithinAReasonableSize() {
        List<MenuPayload.Friends.Friend> friends = new ArrayList<>();
        for (int index = 0; index < 100; index++) {
            friends.add(new MenuPayload.Friends.Friend("GraczODlugimNicku" + index, true, "skyblock-1"));
        }

        byte[] encoded = MenuProtocol.encode(new MenuPayload.Friends(friends, 20));

        // The friend limit is 100. A payload that outgrew a plugin message would fail on a
        // full list only - which is to say, on exactly the players who use the feature most.
        assertTrue(encoded.length < 32_768, "a full friend list encodes to " + encoded.length + " bytes");
    }

    @Test
    @DisplayName("the premium shop comes back as it went out")
    void roundTripsTheShop() {
        MenuPayload.Shop original = new MenuPayload.Shop(List.of("VIP", "SVIP", "SZEFUNCIO"), 200L, 10);

        assertEquals(original, MenuProtocol.decodePayload(MenuProtocol.encode(original)));
        assertEquals(
                new MenuPayload.Shop(List.of(), 0L, 10),
                MenuProtocol.decodePayload(MenuProtocol.encode(new MenuPayload.Shop(List.of(), 0L, 10))));
    }

    @Test
    @DisplayName("the rank shop comes back as it went out")
    void roundTripsTheRankShop() {
        MenuPayload.Ranks original = new MenuPayload.Ranks(340L, List.of(
                new MenuPayload.Ranks.Offer(
                        "vip", "<green>VIP", 11, "PLAYER_HEAD",
                        "http://textures.minecraft.net/texture/1b67", "/vip", 200L, true, false),
                new MenuPayload.Ranks.Offer(
                        "szefuncio", "<gold>SZEFUNCIO", 15, "PLAYER_HEAD", "", "/szefuncio",
                        1000L, false, true)));

        assertEquals(original, MenuProtocol.decodePayload(MenuProtocol.encode(original)));
    }

    @Test
    @DisplayName("an offer says how much more the player needs, and never a negative amount")
    void reportsWhatIsMissing() {
        MenuPayload.Ranks.Offer offer = new MenuPayload.Ranks.Offer(
                "vip", "VIP", 11, "PLAYER_HEAD", "", "/vip", 200L, false, false);

        assertEquals(200L, offer.missing(0L));
        assertEquals(60L, offer.missing(140L));
        // Somebody who can afford it is not short by a negative number.
        assertEquals(0L, offer.missing(200L));
        assertEquals(0L, offer.missing(5_000L));
    }

    @Test
    @DisplayName("a balance that could not be real is not carried into the menu")
    void refusesANegativeBalance() {
        // Nothing writes this, which is the point: the shop draws "brakuje ci" from it, and a
        // negative number there would be a lie shown to a player about their own money.
        byte[] encoded = MenuProtocol.encode(new MenuPayload.Ranks(0L, List.of()));
        encoded[3] = (byte) 0xFF;

        MenuPayload.Ranks decoded = (MenuPayload.Ranks) MenuProtocol.decodePayload(encoded);
        assertEquals(0L, decoded.balance());
    }

    @Test
    @DisplayName("the visual ranks come back as they went out")
    void roundTripsVisualRanks() {
        MenuPayload.VisualRanks original = new MenuPayload.VisualRanks(340L, "dzban", List.of(
                new MenuPayload.VisualRanks.Offer("dzban", "Dzban", 20L, true),
                new MenuPayload.VisualRanks.Offer("shrek", "Shrek", 100L, false)));

        assertEquals(original, MenuProtocol.decodePayload(MenuProtocol.encode(original)));

        // Nobody is wearing one, which is an empty id rather than a missing field.
        MenuPayload.VisualRanks none = new MenuPayload.VisualRanks(0L, "", List.of());
        assertEquals(none, MenuProtocol.decodePayload(MenuProtocol.encode(none)));
    }

    /** A friends payload whose header is valid and whose entry count is not. */
    private static byte[] friendsHeaderWithCount(int count) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeByte(1);
            out.writeByte(1);
            out.writeByte(MenuKind.FRIENDS.id());
            out.writeInt(0);
            out.writeInt(count);
        }
        return bytes.toByteArray();
    }
}
