package pl.landmc.menus.protocol;

/**
 * A menu message that could not be read.
 *
 * <p>Unchecked, because there is exactly one thing a caller can do about it: ignore the message
 * and log it. These bytes arrive over a player's connection, so a malformed one is as likely to
 * be somebody poking at the channel as it is to be a bug, and neither is a reason to disturb the
 * player who sent it.
 */
public class MenuProtocolException extends RuntimeException {

    public MenuProtocolException(String message) {
        super(message);
    }

    public MenuProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
