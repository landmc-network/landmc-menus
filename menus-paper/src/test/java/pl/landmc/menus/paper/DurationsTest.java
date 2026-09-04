package pl.landmc.menus.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** How long a punishment has left, as a player reads it. */
class DurationsTest {

    @Test
    @DisplayName("two units at most, largest first")
    void showsTwoUnits() {
        assertEquals("2d 3h", Durations.remaining(
                Duration.ofDays(2).plusHours(3).plusMinutes(14).toMillis()));
        assertEquals("3h 14min", Durations.remaining(
                Duration.ofHours(3).plusMinutes(14).plusSeconds(9).toMillis()));
        assertEquals("14min", Durations.remaining(Duration.ofMinutes(14).toMillis()));
    }

    @Test
    @DisplayName("a unit that is zero is left out rather than printed as zero")
    void skipsAnEmptyUnit() {
        assertEquals("2d", Durations.remaining(Duration.ofDays(2).toMillis()));
        assertEquals("3h", Durations.remaining(Duration.ofHours(3).toMillis()));
    }

    @Test
    @DisplayName("something about to expire reads as less than a minute, never as zero")
    void handlesTheLastMinute() {
        assertEquals("<1min", Durations.remaining(Duration.ofSeconds(30).toMillis()));
        assertEquals("<1min", Durations.remaining(0));
    }

    @Test
    @DisplayName("a punishment that expired while the menu was being built does not show a minus")
    void handlesAnAlreadyExpiredPunishment() {
        // The payload is built on the proxy and drawn a moment later on the backend; a ban that
        // ran out in between is a normal race and must not render as "-1min".
        assertEquals("<1min", Durations.remaining(Duration.ofMinutes(-5).toMillis()));
    }

    @Test
    @DisplayName("a date is written the way staff say it")
    void formatsADate() {
        // 2026-09-05 14:30 UTC, fixed rather than derived, so the pattern itself is checked.
        long epochMillis = 1_788_618_600_000L;

        assertEquals("05.09.2026 14:30", Durations.date(epochMillis, ZoneId.of("UTC")));
    }

    @Test
    @DisplayName("the date follows the zone it is given")
    void respectsTheZone() {
        long epochMillis = 1_788_618_600_000L;

        assertEquals("05.09.2026 16:30", Durations.date(epochMillis, ZoneId.of("Europe/Warsaw")));
    }
}
