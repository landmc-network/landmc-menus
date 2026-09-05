package pl.landmc.menus.protocol;

import java.util.Optional;

/** Which menu a payload is for. */
public enum MenuKind {

    FRIENDS(1),
    PUNISHMENTS(2),
    SERVERS(3),

    PROFILE(4),

    /** The premium shop, as {@code /sklep} has always opened it. */
    SHOP(5),

    /** The rank shop behind it. */
    RANKS(6);

    private final int id;

    MenuKind(int id) {
        this.id = id;
    }

    /**
     * The number on the wire.
     *
     * <p>Fixed per menu and never reused. A rolling restart has an old backend reading a new
     * proxy's messages, so a number that changes meaning is a menu that opens as the wrong one.
     */
    public int id() {
        return this.id;
    }

    public static Optional<MenuKind> byId(int id) {
        for (MenuKind kind : values()) {
            if (kind.id == id) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }
}
