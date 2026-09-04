package pl.landmc.menus.paper.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import java.util.List;

/**
 * {@code messages.yml} - every piece of text in every menu.
 *
 * <p>All of it is configuration rather than code, because a menu is the part of a server that
 * gets reworded most often and the least dangerously. Colours follow the network's existing
 * scheme: green for a heading, grey for the body, white for the value the player is meant to
 * read.
 *
 * <p>Titles are plain strings rather than notices - an inventory title is one component handed
 * to the client when the menu opens, and there is no action bar or title to send it to.
 */
public class MenusMessages extends OkaeriConfig {

    @Comment("Wspolne dla wszystkich menu.")
    public CommonSection common = new CommonSection();

    @Comment("")
    public FriendsSection friends = new FriendsSection();

    @Comment("")
    public PunishmentsSection punishments = new PunishmentsSection();

    @Comment("")
    public ServersSection servers = new ServersSection();

    /** Text that appears in more than one menu. */
    public static class CommonSection extends OkaeriConfig {

        @Comment("Strzalki stronicowania. Placeholdery: {PAGE}, {PAGES}")
        @CustomKey("previous-page")
        public String previousPage = "<green>Poprzednia strona";

        @CustomKey("next-page")
        public String nextPage = "<green>Następna strona";

        @Comment("")
        @CustomKey("page-lore")
        public String pageLore = "<gray>Strona <white>{PAGE}</white> z <white>{PAGES}</white>";

        @Comment("")
        @Comment("Pokazywane, gdy proxy przysle menu, ktorego ta wersja pluginu nie zna.")
        @CustomKey("unsupported-menu")
        public String unsupportedMenu =
                "<red>Błąd> <gray>To menu wymaga nowszej wersji serwera. Zgłoś to administracji.";
    }

    /** {@code /znajomi} */
    public static class FriendsSection extends OkaeriConfig {

        public String title = "<dark_gray>Twój profil - Znajomi";

        @Comment("")
        @Comment("Ile rzedow ma menu, razem z ramka. Od 1 do 6.")
        public int rows = 6;

        @Comment("")
        @Comment("Placeholdery: {PLAYER}, {SERVER}")
        @CustomKey("friend-online")
        public String friendOnline = "<green>{PLAYER}";

        @CustomKey("friend-online-lore")
        public List<String> friendOnlineLore = List.of(
                "<gray>Serwer: <white>{SERVER}",
                "",
                "<gray>Kliknij, aby dołączyć.",
                "<gray>Shift + prawy, aby usunąć z listy.");

        @Comment("")
        @CustomKey("friend-offline")
        public String friendOffline = "<gray>{PLAYER}";

        @CustomKey("friend-offline-lore")
        public List<String> friendOfflineLore = List.of(
                "<gray>Offline",
                "",
                "<gray>Shift + prawy, aby usunąć z listy.");

        @Comment("")
        @Comment("Kafelek na dole, gdy lista jest pusta.")
        @CustomKey("no-friends")
        public String noFriends = "<green>Nie masz jeszcze znajomych";

        @CustomKey("no-friends-lore")
        public List<String> noFriendsLore = List.of(
                "<gray>Dodaj kogoś komendą",
                "<white>/znajomi dodaj [nick]");

        @Comment("")
        @Comment("Placeholder: {COUNT}")
        @CustomKey("pending-requests")
        public String pendingRequests = "<green>Zaproszenia <white>({COUNT})";

        @CustomKey("pending-requests-lore")
        public List<String> pendingRequestsLore = List.of(
                "<gray>Masz <white>{COUNT}</white> oczekujących zaproszeń.",
                "",
                "<gray>Kliknij, aby je zobaczyć.");
    }

    /** {@code /kary} */
    public static class PunishmentsSection extends OkaeriConfig {

        @Comment("Placeholder: {PLAYER}")
        public String title = "<dark_gray>Kary gracza {PLAYER}";

        @Comment("")
        public int rows = 6;

        @Comment("")
        @Comment("Placeholdery: {TYPE}, {ID}")
        @CustomKey("punishment-name")
        public String punishmentName = "<green>{TYPE} <dark_gray>#{ID}";

        @Comment("")
        @Comment("Placeholdery: {REASON}, {STAFF}, {DATE}, {STATE}")
        @CustomKey("punishment-lore")
        public List<String> punishmentLore = List.of(
                "<gray>Powód: <white>{REASON}",
                "<gray>Nadał: <white>{STAFF}",
                "<gray>Data: <white>{DATE}",
                "",
                "{STATE}");

        @Comment("")
        @Comment("Placeholder: {TIME} - ile zostalo.")
        @CustomKey("state-active")
        public String stateActive = "<red>Aktywna <gray>(pozostało {TIME})";

        @CustomKey("state-permanent")
        public String statePermanent = "<red>Aktywna <gray>(na stałe)";

        @CustomKey("state-expired")
        public String stateExpired = "<gray>Wygasła";

        @CustomKey("state-lifted")
        public String stateLifted = "<gray>Zdjęta";

        @Comment("")
        @CustomKey("no-punishments")
        public String noPunishments = "<green>Czysta karta";

        @CustomKey("no-punishments-lore")
        public List<String> noPunishmentsLore = List.of("<gray>Ten gracz nie ma żadnych kar.");
    }

    /** {@code /serwery} */
    public static class ServersSection extends OkaeriConfig {

        public String title = "<dark_gray>Serwery";

        @Comment("")
        public int rows = 3;

        @Comment("")
        @Comment("Placeholdery: {SERVER}, {ONLINE}")
        @CustomKey("server-name")
        public String serverName = "<green>{SERVER}";

        @CustomKey("server-lore")
        public List<String> serverLore = List.of(
                "<gray>Graczy: <white>{ONLINE}",
                "",
                "<gray>Kliknij, aby dołączyć.");

        @Comment("")
        @Comment("Serwer, na ktorym gracz juz jest.")
        @CustomKey("server-current-lore")
        public List<String> serverCurrentLore = List.of(
                "<gray>Graczy: <white>{ONLINE}",
                "",
                "<green>Jesteś tutaj.");

        @Comment("")
        @Comment("Serwer, ktorego proxy nie moglo dosiegnac.")
        @CustomKey("server-offline")
        public String serverOffline = "<red>{SERVER}";

        @CustomKey("server-offline-lore")
        public List<String> serverOfflineLore = List.of("<gray>Chwilowo niedostępny.");
    }
}
