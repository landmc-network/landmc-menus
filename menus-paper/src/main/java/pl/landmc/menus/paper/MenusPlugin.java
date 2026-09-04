package pl.landmc.menus.paper;

import java.time.ZoneId;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.paper.menu.MenuStyle;
import pl.landmc.menus.protocol.MenuProtocol;
import pl.landmc.platform.component.ComponentFormatter;
import pl.landmc.platform.config.ConfigService;
import pl.landmc.platform.paper.menu.Menus;

/**
 * Draws the network's menus.
 *
 * <p>This plugin owns no data and answers no questions. A menu's contents arrive from whichever
 * plugin already owns them - friends from the proxy, a punishment history from the punishments
 * plugin - and a click goes straight back to that plugin to be decided on. Which means this one
 * can be reloaded, rewritten or replaced without touching anything that holds state.
 *
 * <p>It also means there are no commands here. {@code /znajomi}, {@code /kary} and
 * {@code /serwery} live on the proxy, next to the data they show, and reach every backend
 * without being registered on each of them.
 */
public final class MenusPlugin extends JavaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("landmc-menus");

    private MenusMessages messages;

    @Override
    public void onEnable() {
        ComponentFormatter formatter = ComponentFormatter.standard();

        ConfigService configs = new ConfigService();
        this.messages = configs.load(this.getDataFolder().toPath(), "messages.yml", MenusMessages.class);

        Text text = new Text(formatter);
        MenuStyle style = new MenuStyle(text, this.messages.common);
        MenuChannel channel = new MenuChannel(this);

        Menus.enable(this);

        // Outgoing carries a click back to the proxy; incoming brings a menu's contents in.
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, MenuProtocol.CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(
                this,
                MenuProtocol.CHANNEL,
                new MenuMessageListener(
                        this, this.messages, style, channel, text, ZoneId.systemDefault(), LOGGER));

        LOGGER.info("Menus ready on channel {}.", MenuProtocol.CHANNEL);
    }

    @Override
    public void onDisable() {
        // Closes anything still open. A menu left across a reload is an inventory whose buttons
        // belong to classes the old plugin instance loaded, so a click does nothing or throws.
        Menus.disable();

        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }
}
