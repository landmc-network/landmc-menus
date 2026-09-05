package pl.landmc.menus.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The one number the proxy pushes to a backend's sidebar. */
class SidebarProtocolTest {

    @Test
    @DisplayName("a balance comes back as it went out")
    void roundTrips() {
        assertEquals(0L, SidebarProtocol.decodeBalance(SidebarProtocol.encode(0L)));
        assertEquals(1_234L, SidebarProtocol.decodeBalance(SidebarProtocol.encode(1_234L)));
        assertEquals(
                Long.MAX_VALUE, SidebarProtocol.decodeBalance(SidebarProtocol.encode(Long.MAX_VALUE)));
    }

    @Test
    @DisplayName("a balance that could not be real is not shown to the player")
    void refusesANegativeBalance() {
        assertEquals(0L, SidebarProtocol.decodeBalance(SidebarProtocol.encode(-1L)));
    }

    @Test
    @DisplayName("a message from a version this build does not speak is refused, not guessed at")
    void refusesAnotherVersion() {
        byte[] message = SidebarProtocol.encode(10L);
        message[0] = 99;

        MenuProtocolException failure = assertThrows(
                MenuProtocolException.class, () -> SidebarProtocol.decodeBalance(message));

        assertEquals(true, failure.getMessage().contains("99"), failure.getMessage());
    }

    @Test
    @DisplayName("a message that stops in the middle is refused rather than half-read")
    void refusesATruncatedMessage() {
        byte[] full = SidebarProtocol.encode(42L);

        for (int length = 0; length < full.length; length++) {
            byte[] truncated = new byte[length];
            System.arraycopy(full, 0, truncated, 0, length);

            assertThrows(
                    MenuProtocolException.class,
                    () -> SidebarProtocol.decodeBalance(truncated),
                    "a message cut to " + length + " bytes was accepted");
        }
    }
}
