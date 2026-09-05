package pl.landmc.menus.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The two numbers the proxy pushes to a backend's sidebar. */
class SidebarProtocolTest {

    @Test
    @DisplayName("both balances come back as they went out")
    void roundTrips() {
        assertEquals(
                new SidebarProtocol.Balances(0L, 0L),
                SidebarProtocol.decode(SidebarProtocol.encode(0L, 0L)));

        // Different numbers on purpose: one of them being read into the other is exactly the
        // mistake a pair of longs in a row invites.
        assertEquals(
                new SidebarProtocol.Balances(1_234L, 99L),
                SidebarProtocol.decode(SidebarProtocol.encode(1_234L, 99L)));

        assertEquals(
                new SidebarProtocol.Balances(Long.MAX_VALUE, 7L),
                SidebarProtocol.decode(SidebarProtocol.encode(Long.MAX_VALUE, 7L)));
    }

    @Test
    @DisplayName("a balance that could not be real is not shown to the player")
    void refusesANegativeBalance() {
        assertEquals(
                new SidebarProtocol.Balances(0L, 0L),
                SidebarProtocol.decode(SidebarProtocol.encode(-1L, -5L)));
    }

    @Test
    @DisplayName("a message from a version this build does not speak is refused, not guessed at")
    void refusesAnotherVersion() {
        byte[] message = SidebarProtocol.encode(10L, 3L);
        message[0] = 99;

        MenuProtocolException failure = assertThrows(
                MenuProtocolException.class, () -> SidebarProtocol.decode(message));

        assertEquals(true, failure.getMessage().contains("99"), failure.getMessage());
    }

    @Test
    @DisplayName("a message that stops in the middle is refused rather than half-read")
    void refusesATruncatedMessage() {
        byte[] full = SidebarProtocol.encode(42L, 8L);

        for (int length = 0; length < full.length; length++) {
            byte[] truncated = new byte[length];
            System.arraycopy(full, 0, truncated, 0, length);

            assertThrows(
                    MenuProtocolException.class,
                    () -> SidebarProtocol.decode(truncated),
                    "a message cut to " + length + " bytes was accepted");
        }
    }
}
