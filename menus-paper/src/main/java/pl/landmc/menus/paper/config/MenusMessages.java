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
    public DailySection daily = new DailySection();

    @Comment("")
    public PunishSection punish = new PunishSection();

    @Comment("")
    public PunishmentsSection punishments = new PunishmentsSection();

    @Comment("")
    public ServersSection servers = new ServersSection();

    @Comment("")
    @Comment("Podserwery - lista instancji lobby. Te same pola co przy serwerach.")
    public ServersSection lobbies = lobbiesDefaults();

    @Comment("")
    public CosmeticsSection cosmetics = new CosmeticsSection();

    @Comment("")
    public ReportSection report = new ReportSection();

    @Comment("")
    public ProfileSection profile = new ProfileSection();

    @Comment("")
    public StatisticsSection statistics = new StatisticsSection();

    @Comment("")
    public ShopSection shop = new ShopSection();

    @Comment("")
    public RanksSection ranks = new RanksSection();

    @Comment("")
    @CustomKey("visual-ranks")
    public VisualRanksSection visualRanks = new VisualRanksSection();

    /** The lobby list starts from the server list's wording and changes what differs. */
    private static ServersSection lobbiesDefaults() {
        ServersSection section = new ServersSection();
        section.title = "<dark_gray>Podserwery";
        section.rows = 1;
        // The old server drew the hubs as dyes and turned the one you were standing on grey.
        section.serverCurrentIcon = "GRAY_DYE";
        // One row of hubs and nothing else; the sign belonged to the list of modes.
        section.infoEnabled = false;
        return section;
    }

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

        @CustomKey("tab-statistics")
        public String tabStatistics = "<green>Statystyki";

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
    /**
     * The screen a staff member picks a punishment from.
     *
     * <p>Nothing here says what a click does - the words for that come from the side that
     * records punishments, so the menu and the punishment cannot describe the same thing
     * differently.
     */
    /**
     * Nagroda dzienna: siedem dni pod rzad.
     *
     * <p>Rysowane sa wszystkie siedem, nie tylko dzisiejszy. Gracz decydujacy, czy wroci jutro,
     * decyduje o jutrzejszym kafelku - menu z jednym byloby przyciskiem, a nie powodem.
     */
    public static class DailySection extends OkaeriConfig {

        @Comment("Placeholder: {STREAK} - ile dni z rzedu.")
        public String title = "<dark_gray>Nagroda dzienna";

        @Comment("")
        public int rows = 3;

        @Comment("")
        @Comment("Gdzie stoja kafelki kolejnych dni. Siedem w jednym rzedzie, po jednym na")
        @Comment("kolumne, z ramka po bokach - tydzien czyta sie w linii, a nie w dwoch kupkach.")
        public List<Integer> slots = List.of(10, 11, 12, 13, 14, 15, 16);

        @Comment("")
        @Comment("Materialy kafelka wedlug stanu. Nie szyby: szyba jest materialem ramki,")
        @Comment("wiec kafelek z niej zrobiony wyglada jak puste miejsce.")
        @CustomKey("claimed-material")
        public String claimedMaterial = "LIME_DYE";

        @CustomKey("today-material")
        public String todayMaterial = "CHEST";

        @Comment("Dzien, ktory bedzie do wziecia jutro.")
        @CustomKey("next-material")
        public String nextMaterial = "CLOCK";

        @CustomKey("locked-material")
        public String lockedMaterial = "GRAY_DYE";

        @Comment("")
        @Comment("Nazwa kafelka. Placeholdery: {DAY}, {COINS}, {DIAMONDS}, {STREAK}")
        @CustomKey("day-name")
        public String dayName = "<green>Dzień <white>{DAY}";

        @Comment("")
        @Comment("Trzy stany, trzy rozne zdania.")
        @CustomKey("day-claimed-lore")
        public List<String> dayClaimedLore = List.of(
                "<gray>Nagroda: <gold>{COINS} monet <dark_gray>+ <aqua>{DIAMONDS}❖",
                "",
                "<green>Odebrane.");

        @CustomKey("day-today-lore")
        public List<String> dayTodayLore = List.of(
                "<gray>Nagroda: <gold>{COINS} monet <dark_gray>+ <aqua>{DIAMONDS}❖",
                "",
                "<yellow>Kliknij, aby odebrać.");

        @CustomKey("day-next-lore")
        public List<String> dayNextLore = List.of(
                "<gray>Nagroda: <gold>{COINS} monet <dark_gray>+ <aqua>{DIAMONDS}❖",
                "",
                "<aqua>To jutro. <gray>Wróć, żeby nie stracić serii.");

        @CustomKey("day-locked-lore")
        public List<String> dayLockedLore = List.of(
                "<gray>Nagroda: <gold>{COINS} monet <dark_gray>+ <aqua>{DIAMONDS}❖",
                "",
                "<dark_gray>Dzień <white>{DAY}<dark_gray> serii.");
    }

    public static class PunishSection extends OkaeriConfig {

        @Comment("Placeholder: {PLAYER}")
        public String title = "<dark_gray>Wybór kary <dark_gray>- {PLAYER}";

        @Comment("")
        public int rows = 6;

        @Comment("")
        @Comment("Placeholdery: {NAME}, {PLAYER}")
        @CustomKey("option-name")
        public String optionName = "{NAME}";

        @Comment("")
        @Comment("Placeholdery: {NAME}, {PLAYER}, {LEFT}, {RIGHT}, {SHIFT_RIGHT}")
        @CustomKey("option-lore")
        public List<String> optionLore = List.of(
                "<gray>Kara dla gracza <white>{PLAYER}</white>:",
                "",
                "<yellow>LPM <dark_gray>» <gray>{LEFT}",
                "<yellow>PPM <dark_gray>» <gray>{RIGHT}",
                "<yellow>Shift + PPM <dark_gray>» <gray>{SHIFT_RIGHT}");

        @Comment("")
        @Comment("Co pisze przy kliknieciu, ktore nic nie robi.")
        @CustomKey("option-nothing")
        public String optionNothing = "<dark_gray>nic";
    }

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
        @Comment("Kafelek prowadzacy do rang wizualnych. Slot 42 - tam, gdzie stary serwer")
        @Comment("mial 'Mnozniki (wkrotce)', czyli jedyny kafelek, ktory i tak nic nie robil.")
        @CustomKey("visual-name")
        public String visualName = "<green>Rangi wizualne";

        @CustomKey("visual-lore")
        public List<String> visualLore = List.of(
                "<gray>Nazwa, którą nosisz na czacie",
                "<gray>zamiast nazwy swojej rangi.",
                "",
                "<yellow>Kliknij, aby wybrać.");

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
        @Comment("Opis rangi. Placeholdery: {RANK}, {PRICE}, {COMMAND}, {STATE}, {DURATION}")
        @CustomKey("rank-lore")
        public List<String> rankLore = List.of(
                "<gray>Ranga, dzięki której wyróżnisz się na czacie!",
                "",
                "<green>Wszystkie rzeczy, które posiada ta ranga ...",
                "<green>... można znaleźć pod komendą <white>{COMMAND}<green>.",
                "",
                "<white>Cena: <aqua>{PRICE}❖",
                "",
                "{DURATION}",
                "",
                "{STATE}");

        @Comment("")
        @Comment("Za {DURATION} wstawiane jest jedno z tych dwoch. Zdanie o wieczystej randze")
        @Comment("bylo na starym LandMC wpisane na sztywno w opis - i przestaje byc prawda")
        @Comment("w chwili, w ktorej sklep zaczyna sprzedawac range na miesiac.")
        @CustomKey("duration-permanent")
        public String durationPermanent =
                "<red><bold>PAMIĘTAJ! <red>Ranga jest na <red><underlined>ZAWSZE!";

        @Comment("Placeholder: {DAYS}")
        @CustomKey("duration-days")
        public String durationDays =
                "<red><bold>PAMIĘTAJ! <red>Ranga wygasa po <red><underlined>{DAYS} dniach!";

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

    /** The third tab of the profile. */
    public static class StatisticsSection extends OkaeriConfig {

        @Comment("Placeholder: {PLAYER}")
        public String title = "<dark_gray>Twój profil - Statystyki";

        @Comment("")
        public int rows = 6;

        @Comment("")
        @Comment("Kafelek jednej statystyki. Etykiete, wartosc, przedmiot i slot podaje")
        @Comment("wtyczka, ktora te liczbe posiada - to menu nie wie, co pokazuje.")
        @Comment("Placeholdery: {LABEL}, {VALUE}")
        @CustomKey("entry-name")
        public String entryName = "<green>{LABEL}";

        @CustomKey("entry-lore")
        public List<String> entryLore = List.of("", "<white>{VALUE}", "");

        @Comment("")
        @Comment("Gdy siec nie ma jeszcze nic do policzenia.")
        @CustomKey("empty-name")
        public String emptyName = "<green>Brak statystyk";

        @CustomKey("empty-lore")
        public List<String> emptyLore = List.of(
                "<gray>Nie ma jeszcze czego liczyć.",
                "",
                "<gray>Pojawią się tutaj, kiedy",
                "<gray>ruszy rozgrywka na SkyBlocku.");
    }

    /** {@code /dodatki} */
    public static class CosmeticsSection extends OkaeriConfig {

        @Comment("Bez {BALANCE} celowo: tytul, ktory zmienia sie razem z trescia, wymusza")
        @Comment("prawdziwe otwarcie okna zamiast przerysowania go w miejscu - czyli mrugniecie")
        @Comment("i kursor wyrzucony na srodek po kazdym zakupie. Stan portfela jest na kafelkach.")
        public String title = "<dark_gray>Dodatki";

        @Comment("")
        public int rows = 6;

        @Comment("")
        @Comment("Kafelek kategorii na pierwszym ekranie.")
        @Comment("Placeholdery: {NAME}, {OWNED}, {TOTAL}, {BALANCE}")
        @CustomKey("category-name")
        public String categoryName = "{NAME}";

        @CustomKey("category-lore")
        public List<String> categoryLore = List.of(
                "<gray>Posiadasz: <white>{OWNED}</white><gray>/<white>{TOTAL}",
                "",
                "<yellow>Kliknij, aby otworzyć.");

        @Comment("")
        @Comment("Powrot do wyboru kategorii.")
        @CustomKey("back-slot")
        public int backSlot = 49;

        @CustomKey("back-icon")
        public String backIcon = "ARROW";

        @CustomKey("back-name")
        public String backName = "<yellow>Powrót";

        @CustomKey("back-lore")
        public List<String> backLore = List.of("<gray>Wróć do wyboru kategorii.");

        @Comment("")
        @Comment("Nazwa kafelka. Sama nazwa dodatku, material i miejsce przychodza ze sklepu.")
        @Comment("Placeholdery: {NAME}, {PRICE}, {BALANCE}, {MISSING}")
        @CustomKey("offer-name")
        public String offerName = "{NAME}";

        @Comment("")
        @Comment("Trzy stany, trzy rozne zdania: jeszcze nie kupione mowi ile kosztuje,")
        @Comment("kupione mowi zeby zalozyc, zalozone mowi zeby zdjac.")
        @CustomKey("offer-for-sale-lore")
        public List<String> offerForSaleLore = List.of(
                "<gray>Cena: <aqua>{PRICE}❖",
                "",
                "<yellow>Kliknij, aby kupić.");

        @CustomKey("offer-too-poor-lore")
        public List<String> offerTooPoorLore = List.of(
                "<gray>Cena: <aqua>{PRICE}❖",
                "",
                "<red>Brakuje Ci <aqua>{MISSING}❖<red>.");

        @CustomKey("offer-owned-lore")
        public List<String> offerOwnedLore = List.of(
                "<green>Posiadasz ten dodatek.",
                "",
                "<yellow>Kliknij, aby założyć.");

        @CustomKey("offer-worn-lore")
        public List<String> offerWornLore = List.of(
                "<green>Założone.",
                "",
                "<yellow>Kliknij, aby zdjąć.");
    }

    /** {@code /zglos} */
    public static class ReportSection extends OkaeriConfig {

        @Comment("Placeholder: {PLAYER}")
        public String title = "<gray>Menu zgłoszenia";

        @Comment("")
        public int rows = 6;

        @Comment("")
        @Comment("Tabliczka z nazwa zglaszanego gracza. Wazniejsza niz wyglada:")
        @Comment("komenda bierze nick, menu otwiera sie chwile pozniej, a bez tego")
        @Comment("gracz, ktory pomylil nick, nie ma jak tego zauwazyc przed kliknieciem.")
        @CustomKey("subject-slot")
        public int subjectSlot = 4;

        @CustomKey("subject-icon")
        public String subjectIcon = "OAK_SIGN";

        @CustomKey("subject-name")
        public String subjectName = "<white>Zgłaszany gracz: <green>{PLAYER}";

        @CustomKey("subject-lore")
        public List<String> subjectLore = List.of(
                "<gray>Wybierz powód zgłoszenia poniżej.");

        @Comment("")
        @Comment("Kafelek powodu. Sam powod - nazwa, material i slot - przychodzi z proxy.")
        @Comment("Placeholdery: {REASON}, {PLAYER}")
        @CustomKey("reason-name")
        public String reasonName = "<green>Zgłoś za: {REASON}";

        @CustomKey("reason-lore")
        public List<String> reasonLore = List.of(
                "<gray>Kliknij, aby zgłosić gracza <white>{PLAYER}</white>.");
    }

    /** {@code /serwery} */
    public static class ServersSection extends OkaeriConfig {

        public String title = "<dark_gray>Lista serwerów";

        @Comment("")
        public int rows = 6;

        @Comment("")
        @Comment("Nazwa kafelka. Nazwa i lore serwera przychodza z proxy - tam sa ustawiane,")
        @Comment("bo tam jest lista serwerow. Tutaj jest tylko to, co wyglada tak samo")
        @Comment("dla kazdego z nich.")
        @Comment("Placeholdery: {SERVER}, {ONLINE}")
        @CustomKey("server-name")
        public String serverName = "{SERVER}";

        @Comment("")
        @Comment("Serwer, na ktorym gracz juz jest. Nazwa idzie tu bez kolorow serwera,")
        @Comment("zeby czerwien byla widoczna - tak samo robil stary LandMC.")
        @CustomKey("server-current-name")
        public String serverCurrentName = "<red>{SERVER}";

        @Comment("")
        @Comment("Ta linia zastepuje ostatnia linie lore serwera, na ktorym gracz stoi.")
        @CustomKey("server-current-line")
        public String serverCurrentLine = "<red>Znajdujesz się już na tym serwerze!";

        @Comment("")
        @Comment("Material kafelka aktualnego serwera. Puste = ten sam, co zwykle.")
        @CustomKey("server-current-icon")
        public String serverCurrentIcon = "";

        @Comment("")
        @Comment("Serwer, ktorego proxy nie moglo dosiegnac.")
        @CustomKey("server-offline")
        public String serverOffline = "<red>{SERVER}";

        @CustomKey("server-offline-lore")
        public List<String> serverOfflineLore = List.of("<gray>Chwilowo niedostępny.");

        @Comment("")
        @Comment("Tabliczka nad lista, ta sama co na starym LandMC.")
        @CustomKey("info-enabled")
        public boolean infoEnabled = true;

        @CustomKey("info-slot")
        public int infoSlot = 4;

        @CustomKey("info-icon")
        public String infoIcon = "OAK_SIGN";

        @CustomKey("info-name")
        public String infoName = "<green>Wybór trybu";

        @CustomKey("info-lore")
        public List<String> infoLore = List.of(
                "<gray>Wybierz serwer i zacznij swoją przygodę!");
    }
}
