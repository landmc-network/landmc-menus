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

    @Comment("")
    public ProfileSection profile = new ProfileSection();

    @Comment("")
    public ShopSection shop = new ShopSection();

    @Comment("")
    public RanksSection ranks = new RanksSection();

    @Comment("")
    @CustomKey("visual-ranks")
    public VisualRanksSection visualRanks = new VisualRanksSection();

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
        @Comment("Pasek zakladek u gory profilu i znajomych, tak jak na starym LandMC.")
        @CustomKey("tab-profile")
        public String tabProfile = "<green>Ogólne opcje";

        @CustomKey("tab-friends")
        public String tabFriends = "<green>Znajomi";

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

    /** {@code /profil} */
    public static class ProfileSection extends OkaeriConfig {

        @Comment("Placeholder: {PLAYER}")
        public String title = "<dark_gray>Twój profil";

        @Comment("")
        @Comment("Liczby rzedow tu nie ma celowo. Kafelki stoja w slotach ze starego LandMC")
        @Comment("- 22, 38 i 40 - a te istnieja tylko w menu o szesciu rzedach. Mniejsze menu")
        @Comment("upychalo je jedno na drugim i kafelek sklepu znikal pod ksiazka.")

        @Comment("")
        @Comment("Placeholdery: {PLAYER}, {RANK}, {SERVER}")
        @CustomKey("player-name")
        public String playerName = "<green>{PLAYER}";

        @CustomKey("player-lore")
        public List<String> playerLore = List.of(
                "<gray>Ranga: <white>{RANK}",
                "<gray>Serwer: <white>{SERVER}");

        @Comment("")
        @Comment("Gdy siec nie ma systemu rang albo gracz nie ma zadnej.")
        @CustomKey("no-rank")
        public String noRank = "brak";

        @Comment("")
        @Comment("Placeholdery: {FRIENDS}, {REQUESTS}")
        @CustomKey("friends-name")
        public String friendsName = "<green>Znajomi <white>({FRIENDS})";

        @CustomKey("friends-lore")
        public List<String> friendsLore = List.of(
                "<gray>Oczekujące zaproszenia: <white>{REQUESTS}",
                "",
                "<gray>Kliknij, aby otworzyć listę.");

        @Comment("")
        @Comment("Kafelek prowadzacy do sklepu rang.")
        @CustomKey("shop-name")
        public String shopName = "<green>Wspomóż serwer, zakup rangę!";

        @CustomKey("shop-lore")
        public List<String> shopLore = List.of(
                "",
                "<yellow>Kliknij, aby zobaczyć rangi w sklepie.");

        @Comment("")
        @CustomKey("premium-name")
        public String premiumName = "<green>Logowanie premium";

        @CustomKey("premium-lore")
        public List<String> premiumLore = List.of(
                "<gray>Wejście kontem premium, bez hasła.",
                "",
                "<red>Uwaga: <gray>po włączeniu wejdziesz",
                "<gray>tylko z kontem premium Mojanga.",
                "",
                "<gray>Kliknij, aby sprawdzić i przełączyć.");
    }

    /** {@code /sklep} - the premium shop, as the original opened it. */
    public static class ShopSection extends OkaeriConfig {

        public String title = "<dark_gray>Sklep z usługami premium";

        @Comment("")
        @Comment("Ile rzedow ma menu. W oryginale byly cztery.")
        public int rows = 4;

        @Comment("")
        @Comment("Tabliczka na gorze.")
        @CustomKey("header-name")
        public String headerName = "<green><bold>LANDMC.PL";

        @CustomKey("header-lore")
        public List<String> headerLore = List.of(
                "<gray>Witaj w sklepie! Wybierz odpowiednią kategorię, ...",
                "<gray>... a następnie zakup odpowiednią usługę.");

        @Comment("")
        @Comment("Kafelek rang. Placeholdery: {RANKS} - nazwy rang po przecinku, {FROM} - najnizsza cena")
        @CustomKey("ranks-name")
        public String ranksName = "<green>Rangi <dark_gray>({RANKS}<dark_gray>)";

        @CustomKey("ranks-lore")
        public List<String> ranksLore = List.of(
                "<gray>Zakup rangę z dodatkowymi przywilejami!",
                "",
                "<yellow>Ranga jest na <red><underlined>ZAWSZE</underlined><yellow>!",
                "",
                "<green>Ceny zaczynają się od <aqua>{FROM}❖<green>!",
                "",
                "<yellow>Kliknij, aby wybrać kategorię.");

        @Comment("")
        @Comment("Kafelek doladowania. Placeholder: {RATE} - ile diamentow za zlotowke")
        @CustomKey("top-up-name")
        public String topUpName = "<green>Doładowanie";

        @CustomKey("top-up-lore")
        public List<String> topUpLore = List.of(
                "<gray>Doładuj swoje konto, aby zakupić ...",
                "<gray>... dodatkowe usługi premium.",
                "",
                "<green>Aktualny przelicznik <aqua>❖ <green>to <aqua>{RATE}❖ <green>= <gold>1 PLN<green>.",
                "",
                "<yellow>Kliknij, aby przejść dalej.");
    }

    /** {@code /rangi} - the rank shop. */
    public static class RanksSection extends OkaeriConfig {

        public String title = "<dark_gray>Rangi premium";

        @Comment("")
        @Comment("Ile rzedow ma menu. W oryginale bylo szesc, bo rangi stoja w konkretnych slotach.")
        public int rows = 6;

        @Comment("")
        @Comment("Nazwa kafelka rangi. Placeholder: {RANK}")
        @CustomKey("rank-name")
        public String rankName = "<green>Ranga <dark_gray>({RANK}<dark_gray>)";

        @Comment("")
        @Comment("Opis rangi. Placeholdery: {RANK}, {PRICE}, {COMMAND}, {STATE}")
        @CustomKey("rank-lore")
        public List<String> rankLore = List.of(
                "<gray>Ranga, dzięki której wyróżnisz się na czacie!",
                "",
                "<green>Wszystkie rzeczy, które posiada ta ranga ...",
                "<green>... można znaleźć pod komendą <white>{COMMAND}<green>.",
                "",
                "<white>Cena: <aqua>{PRICE}❖",
                "",
                "<red><bold>PAMIĘTAJ! <red>Ranga jest na <red><underlined>ZAWSZE!",
                "",
                "{STATE}");

        @Comment("")
        @Comment("Ostatnia linia opisu, zaleznie od tego, na co gracza stac.")
        @Comment("Placeholdery: {RANK}, {PRICE}, {MISSING}")
        @CustomKey("state-buy")
        public String stateBuy = "<yellow>Kliknij, aby zakupić rangę {RANK}<yellow>.";

        @CustomKey("state-too-poor")
        public String stateTooPoor =
                "<red>Nie masz tyle <aqua>❖<red>, aby ją zakupić."
                        + " Brakuje Ci <aqua>{MISSING}❖<red>.";

        @CustomKey("state-owned")
        public String stateOwned = "<red>Masz już zakupioną tę rangę!";

        @Comment("")
        @Comment("Kafelek rang wizualnych. Slot 49, tak jak w oryginale.")
        @CustomKey("visual-name")
        public String visualName = "<green>Rangi wizualne";

        @CustomKey("visual-lore")
        public List<String> visualLore = List.of(
                "<gray>Wizualna ranga, która będzie",
                "<gray>wyświetlana na czacie.",
                "",
                "<yellow>Kliknij, aby przejść dalej.");

        @Comment("")
        @Comment("Strzalka powrotu do /sklep. Slot 53, tak jak w oryginale.")
        @CustomKey("back-name")
        public String backName = "<green><bold>POWRÓT";

        @CustomKey("back-lore")
        public List<String> backLore = List.of("<yellow>Kliknij, aby powrócić.");
    }

    /** The visual ranks, behind the rank shop. */
    public static class VisualRanksSection extends OkaeriConfig {

        public String title = "<dark_gray>Rangi wizualne";

        @Comment("")
        public int rows = 6;

        @Comment("")
        @Comment("Nazwa kafelka. Placeholder: {RANK}")
        @CustomKey("rank-name")
        public String rankName = "<green>{RANK}";

        @Comment("")
        @Comment("Opis. Placeholdery: {RANK}, {PRICE}, {MISSING}, {STATE}")
        @CustomKey("rank-lore")
        public List<String> rankLore = List.of(
                "<gray>Wizualna ranga wyświetlana na czacie",
                "<gray>zamiast nazwy Twojej rangi.",
                "",
                "<white>Cena: <aqua>{PRICE}❖",
                "",
                "{STATE}");

        @Comment("")
        @Comment("Ostatnia linia opisu, zaleznie od stanu.")
        @CustomKey("state-buy")
        public String stateBuy = "<yellow>Kliknij, aby zakupić.";

        @CustomKey("state-too-poor")
        public String stateTooPoor =
                "<red>Brakuje Ci <aqua>{MISSING}❖<red>, aby ją zakupić.";

        @CustomKey("state-owned")
        public String stateOwned = "<yellow>Kliknij, aby założyć.";

        @CustomKey("state-active")
        public String stateActive = "<green>Nosisz tę rangę.";

        @Comment("")
        @Comment("Kafelek zdejmujacy range wizualna.")
        @CustomKey("clear-name")
        public String clearName = "<green>Zdejmij rangę wizualną";

        @CustomKey("clear-lore")
        public List<String> clearLore = List.of(
                "<gray>Wrócisz do nazwy swojej zwykłej rangi.",
                "",
                "<yellow>Kliknij, aby zdjąć.");

        @Comment("")
        @Comment("Strzalka powrotu do sklepu rang.")
        @CustomKey("back-name")
        public String backName = "<green><bold>POWRÓT";

        @CustomKey("back-lore")
        public List<String> backLore = List.of("<yellow>Kliknij, aby powrócić.");
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
