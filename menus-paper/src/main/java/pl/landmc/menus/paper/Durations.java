package pl.landmc.menus.paper;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * How a punishment's dates read to a player.
 *
 * <p>Two units and no more. "2d 3h" is what somebody appealing a ban actually needs; "2 dni,
 * 3 godziny, 14 minut i 8 sekund" is the same information arranged so that nobody reads it.
 */
public final class Durations {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Durations() {
    }

    /** A timestamp, in the server's own time zone - which is the one staff talk in. */
    public static String date(long epochMillis, ZoneId zone) {
        return DATE.format(Instant.ofEpochMilli(epochMillis).atZone(zone));
    }

    /**
     * How much is left, in at most two units.
     *
     * <p>Anything already over reads as less than a minute rather than as a negative: a
     * punishment that expired between the payload being built and the menu being drawn is a
     * normal race, not something to show a minus sign for.
     */
    public static String remaining(long millis) {
        Duration duration = Duration.ofMillis(Math.max(0, millis));

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            return hours > 0 ? days + "d " + hours + "h" : days + "d";
        }
        if (hours > 0) {
            return minutes > 0 ? hours + "h " + minutes + "min" : hours + "h";
        }
        if (minutes > 0) {
            return minutes + "min";
        }
        return "<1min";
    }
}
