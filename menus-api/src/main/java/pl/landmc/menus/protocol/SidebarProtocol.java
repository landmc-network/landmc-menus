package pl.landmc.menus.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * What a backend needs to draw a sidebar that mentions money.
 *
 * <p>Pushed rather than asked for: the sidebar redraws on a timer and a request per player per
 * second would be a conversation about a number that changes when somebody buys something.
 *
 * <p>Both currencies travel together because they change together - a purchase moves one and a
 * reward moves the other, and either way the whole line is redrawn. Two messages would be two
 * chances for a sidebar to show one number from before and one from after.
 */
public final class SidebarProtocol {

    public static final String CHANNEL = "landmc:sidebar";

    /**
     * Two, because the message gained coins.
     *
     * <p>A backend that has not been updated refuses a message it cannot read rather than
     * reading the first eight bytes as a balance and the rest as nothing - which is what an
     * unversioned format would have done, and it would have shown the right diamonds beside a
     * silently missing second number.
     */
    private static final byte VERSION = 2;

    private SidebarProtocol() {
    }

    public static byte[] encode(long diamonds, long coins) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeByte(VERSION);
            out.writeLong(diamonds);
            out.writeLong(coins);
        }
        catch (IOException exception) {
            // A ByteArrayOutputStream does not fail; pretending it might would put a checked
            // exception on every caller for nothing.
            throw new IllegalStateException("Could not encode a sidebar update", exception);
        }
        return bytes.toByteArray();
    }

    public static Balances decode(byte[] message) {
        Objects.requireNonNull(message, "message");

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            int version = in.readUnsignedByte();
            if (version != VERSION) {
                throw new MenuProtocolException(
                        "Sidebar protocol version " + version + "; this build speaks " + VERSION);
            }
            // Neither can be negative, and a negative one would only be there to make a line
            // of the sidebar lie.
            return new Balances(Math.max(0L, in.readLong()), Math.max(0L, in.readLong()));
        }
        catch (IOException exception) {
            throw new MenuProtocolException("Truncated sidebar update", exception);
        }
    }

    /** What a player has, in both of the network's currencies. */
    public record Balances(long diamonds, long coins) {
    }
}
