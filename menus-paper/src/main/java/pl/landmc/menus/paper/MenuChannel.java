package pl.landmc.menus.paper;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.landmc.menus.protocol.MenuAction;
import pl.landmc.menus.protocol.MenuProtocol;

/**
 * Sends a click back to the plugin that can act on it.
 *
 * <p>The message travels up the player's own connection and the proxy takes it off there, so it
 * arrives addressed to that player without anything here having to say who they are. That is
 * also why it carries no authority: the receiver decides what a player may do, and this only
 * says which button was pressed.
 */
public final class MenuChannel {

    private final Plugin plugin;

    public MenuChannel(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void send(Player player, MenuAction action) {
        player.sendPluginMessage(this.plugin, MenuProtocol.CHANNEL, MenuProtocol.encode(action));
    }
}
