package pl.landmc.menus.protocol;

import java.util.List;
import java.util.Objects;

/**
 * The contents of a menu, as they travel from the plugin that owns the data to the server that
 * draws them.
 *
 * <p>Everything here is display material and nothing more. A backend that receives one of these
 * shows what it says and never decides anything from it - the payload arrives over the player's
 * own connection, which means a modified client can send one too. Treating it as data rather
 * than as authority is what makes that a curiosity instead of a hole: the worst a forged payload
 * achieves is showing its sender a menu full of text they wrote themselves.
 */
public sealed interface MenuPayload {

    MenuKind kind();

    /** The friends menu: who they are, and where they are if that may be shown. */
    record Friends(List<Friend> friends, int pendingRequests) implements MenuPayload {

        public Friends {
            friends = List.copyOf(Objects.requireNonNull(friends, "friends"));
        }

        @Override
        public MenuKind kind() {
            return MenuKind.FRIENDS;
        }

        /**
         * @param server the server they are on, or empty when they are offline or hidden - the
         *     proxy decides which, because vanish is its business and not the menu's
         */
        public record Friend(String name, boolean online, String server) {

            public Friend {
                Objects.requireNonNull(name, "name");
                Objects.requireNonNull(server, "server");
            }
        }
    }

    /** Somebody's punishment history, newest first. */
    record Punishments(String subject, List<Punishment> punishments) implements MenuPayload {

        public Punishments {
            Objects.requireNonNull(subject, "subject");
            punishments = List.copyOf(Objects.requireNonNull(punishments, "punishments"));
        }

        @Override
        public MenuKind kind() {
            return MenuKind.PUNISHMENTS;
        }

        /**
         * @param expiresAt epoch milliseconds, or zero for a punishment that does not expire
         */
        public record Punishment(
                long id,
                String type,
                String reason,
                String staff,
                long issuedAt,
                long expiresAt,
                boolean active) {

            public Punishment {
                Objects.requireNonNull(type, "type");
                Objects.requireNonNull(reason, "reason");
                Objects.requireNonNull(staff, "staff");
            }
        }
    }

    /**
     * A player's own profile: who the network thinks they are.
     *
     * <p>Counts rather than lists. The profile is a summary with buttons that open the real
     * menus, so sending the friend list here as well would send it twice - once for the number
     * on the tile and once when the tile is clicked.
     *
     * <p>What is here is what the sender knows. Whether the player logs in with a password or
     * through Mojang is the login plugin's business and lives in its table, so it is deliberately
     * absent rather than guessed at - the profile offers a way into that setting instead, and
     * the command behind it reports its own state.
     *
     * @param rank what their rank is called, empty when the network has no rank system running
     */
    record Profile(
            String playerName,
            String rank,
            int friends,
            int pendingRequests,
            String currentServer) implements MenuPayload {

        public Profile {
            Objects.requireNonNull(playerName, "playerName");
            Objects.requireNonNull(rank, "rank");
            Objects.requireNonNull(currentServer, "currentServer");
        }

        @Override
        public MenuKind kind() {
            return MenuKind.PROFILE;
        }
    }

    /** The servers a player may move to. */
    record Servers(String currentServer, List<Server> servers) implements MenuPayload {

        public Servers {
            Objects.requireNonNull(currentServer, "currentServer");
            servers = List.copyOf(Objects.requireNonNull(servers, "servers"));
        }

        @Override
        public MenuKind kind() {
            return MenuKind.SERVERS;
        }

        /**
         * @param online how many players are on it
         * @param reachable false when the proxy could not reach it just now, so the menu can say
         *     so rather than sending somebody at a server that will refuse them
         */
        public record Server(String id, String displayName, int online, boolean reachable) {

            public Server {
                Objects.requireNonNull(id, "id");
                Objects.requireNonNull(displayName, "displayName");
            }
        }
    }

    /**
     * The premium shop, the menu {@code /sklep} opens.
     *
     * <p>Almost all of it is fixed text that lives in the backend's messages file. What travels
     * is the part the proxy knows and the backend cannot: which ranks are on sale and what they
     * start at, so the tiles read the way the original's did instead of quoting numbers that
     * were written into them by hand.
     *
     * @param cheapestRank the lowest price on offer, for "ceny zaczynają się od"
     * @param diamondsPerPln the top-up rate the deposit tile quotes
     */
    record Shop(List<String> rankNames, long cheapestRank, int diamondsPerPln)
            implements MenuPayload {

        public Shop {
            rankNames = List.copyOf(Objects.requireNonNull(rankNames, "rankNames"));
        }

        @Override
        public MenuKind kind() {
            return MenuKind.SHOP;
        }
    }

    /**
     * The rank shop: what is for sale, for how much, and what the player has to spend with.
     *
     * <p>Whether an offer can be afforded is decided by the sender, not by the menu. The backend
     * has no idea what a diamond is, and a menu doing its own arithmetic could disagree with the
     * plugin that takes the payment - the one disagreement a shop must never have.
     *
     * @param balance what the player holds, so a tile can say how much they are short by
     */
    record Ranks(long balance, List<Offer> offers) implements MenuPayload {

        public Ranks {
            offers = List.copyOf(Objects.requireNonNull(offers, "offers"));
        }

        @Override
        public MenuKind kind() {
            return MenuKind.RANKS;
        }

        /**
         * One rank on sale.
         *
         * @param id what a click sends back; the shop, not the menu, knows what it means
         * @param slot where it sits, because the shop's layout is the proxy's to decide and the
         *     original put each rank in a particular place
         * @param icon the material to draw it as, or PLAYER_HEAD when a texture is given
         * @param texture the skin URL for a head, empty for a plain material
         * @param infoCommand the command that lists what the rank gives, as the lore says
         * @param owned true when the player already has this rank, so it is shown as theirs
         *     rather than offered for sale a second time
         */
        public record Offer(
                String id,
                String displayName,
                int slot,
                String icon,
                String texture,
                String infoCommand,
                long price,
                boolean owned,
                boolean glowing) {

            public Offer {
                Objects.requireNonNull(id, "id");
                Objects.requireNonNull(displayName, "displayName");
                Objects.requireNonNull(icon, "icon");
                Objects.requireNonNull(texture, "texture");
                Objects.requireNonNull(infoCommand, "infoCommand");
            }

            /** How much more the player needs, given what they hold. */
            public long missing(long balance) {
                return Math.max(0L, this.price - balance);
            }
        }
    }
}
