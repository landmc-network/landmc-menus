package pl.landmc.menus.protocol;

import java.util.Objects;

/**
 * What a player clicked, travelling back from the server drawing the menu to the plugin that can
 * act on it.
 *
 * <p>An action is a request, never an instruction. It says which button was pressed and for
 * what; whether that is allowed is decided entirely by the receiver, from the player it arrived
 * for. This matters because the message comes up the player's own connection and a modified
 * client can send one: every action here therefore has to be something that player could already
 * do with a command, and the receiver has to check it as though they had typed it.
 *
 * @param menu which menu it came from, so an action name can mean different things in different
 *     menus without a single flat namespace
 * @param action a short verb - {@code connect}, {@code remove}, {@code accept}
 * @param argument what it applies to; empty when the verb needs nothing
 */
public record MenuAction(MenuKind menu, String action, String argument) {

    /** Long enough for a server id or a player name, short enough not to be a payload. */
    public static final int MAXIMUM_LENGTH = 64;

    public MenuAction {
        Objects.requireNonNull(menu, "menu");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(argument, "argument");

        if (action.isEmpty() || action.length() > MAXIMUM_LENGTH) {
            throw new IllegalArgumentException("Action name out of range: " + action.length());
        }
        if (argument.length() > MAXIMUM_LENGTH) {
            throw new IllegalArgumentException("Action argument out of range: " + argument.length());
        }
    }

    public static MenuAction of(MenuKind menu, String action) {
        return new MenuAction(menu, action, "");
    }

    public static MenuAction of(MenuKind menu, String action, String argument) {
        return new MenuAction(menu, action, argument);
    }
}
