package pl.landmc.menus.paper;

import java.time.ZoneId;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.slf4j.Logger;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.paper.menu.FriendsMenu;
import pl.landmc.menus.paper.menu.MenuStyle;
import pl.landmc.menus.paper.menu.ProfileMenu;
import pl.landmc.menus.paper.menu.DailyMenu;
import pl.landmc.menus.paper.menu.PunishMenu;
import pl.landmc.menus.paper.menu.PunishmentsMenu;
import pl.landmc.menus.paper.menu.RanksMenu;
import pl.landmc.menus.paper.menu.CosmeticsMenu;
import pl.landmc.menus.paper.menu.ReportMenu;
import pl.landmc.menus.paper.menu.ServersMenu;
import pl.landmc.menus.paper.menu.StatisticsMenu;
import pl.landmc.menus.paper.menu.VisualRanksMenu;
import pl.landmc.menus.paper.menu.ShopMenu;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.menus.protocol.MenuProtocol;
import pl.landmc.menus.protocol.MenuProtocolException;
import pl.landmc.platform.paper.menu.Menu;

/**
 * Opens the menu a payload describes.
 *
 * <p>Everything arriving here is treated as text to display and nothing else. The channel runs
 * over the player's own connection, so a modified client can write to it, and the only thing
 * that buys them is a menu full of words they chose - shown to themselves. Nothing in a payload
 * decides what anybody is allowed to do; that stays with the plugin the click is sent back to.
 */
public final class MenuMessageListener implements PluginMessageListener {

    private final Plugin plugin;
    private final MenusMessages messages;
    private final MenuStyle style;
    private final MenuChannel channel;
    private final Text text;
    private final ZoneId zone;
    private final Logger logger;

    public MenuMessageListener(
            Plugin plugin,
            MenusMessages messages,
            MenuStyle style,
            MenuChannel channel,
            Text text,
            ZoneId zone,
            Logger logger) {

        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.text = Objects.requireNonNull(text, "text");
        this.zone = Objects.requireNonNull(zone, "zone");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!MenuProtocol.CHANNEL.equals(channel)) {
            return;
        }

        // An action is what this server sends, not what it receives. One arriving here is
        // either our own message coming back or somebody experimenting with the channel.
        if (MenuProtocol.isAction(message)) {
            return;
        }

        MenuPayload payload;
        try {
            payload = MenuProtocol.decodePayload(message);
        }
        catch (MenuProtocolException exception) {
            // Debug, not warn: a malformed message on a channel a client can write to is not a
            // fault, and a log line per attempt is a way to fill a disk.
            this.logger.debug(
                    "Unreadable menu payload for {}: {}", player.getName(), exception.getMessage());

            // A message written by a build that speaks a different version is a rolling restart
            // half done, not a broken client - and the player deserves to know why the menu they
            // asked for never appeared.
            if (!MenuProtocol.isKnownVersion(message)) {
                this.reportUnsupported(player);
            }
            return;
        }

        this.open(player, payload);
    }

    private void open(Player player, MenuPayload payload) {
        Menu menu = switch (payload) {
            case MenuPayload.Friends friends -> new FriendsMenu(
                    friends, this.messages, this.style, this.channel);
            case MenuPayload.Punishments punishments -> new PunishmentsMenu(
                    punishments, this.messages.punishments, this.style, this.zone);
            case MenuPayload.Punish punish -> new PunishMenu(
                    punish, this.messages.punish, this.style, this.channel);
            case MenuPayload.Daily daily -> new DailyMenu(
                    daily, this.messages.daily, this.style, this.channel);
            case MenuPayload.Cosmetics cosmetics -> new CosmeticsMenu(
                    cosmetics, this.messages.cosmetics, this.style, this.channel);
            case MenuPayload.Report report -> new ReportMenu(
                    report, this.messages.report, this.style, this.channel);
            case MenuPayload.Servers servers -> new ServersMenu(
                    servers, this.messages.servers, this.style, this.channel, MenuKind.SERVERS);
            // Drawn by the same menu with different words: a lobby list and a server list look
            // alike and read differently. The kind travels with it so a click on a hub is
            // answered by whoever owns the hubs, not by whoever owns the modes.
            case MenuPayload.Lobbies lobbies -> new ServersMenu(
                    lobbies.asServers(), this.messages.lobbies, this.style, this.channel,
                    MenuKind.LOBBIES);
            case MenuPayload.Profile profile -> new ProfileMenu(
                    profile, this.messages, this.style, this.channel);
            case MenuPayload.Shop shop -> new ShopMenu(
                    shop, this.messages.shop, this.style, this.channel);
            case MenuPayload.Ranks ranks -> new RanksMenu(
                    ranks, this.messages.ranks, this.style, this.channel);
            case MenuPayload.VisualRanks visual -> new VisualRanksMenu(
                    visual, this.messages.visualRanks, this.style, this.channel);
            case MenuPayload.Statistics statistics -> new StatisticsMenu(
                    statistics, this.messages, this.style, this.channel);
        };

        // Paper hands plugin messages to listeners on the main thread, which is where a menu
        // has to be opened. The check is here because "usually the main thread" is exactly the
        // kind of assumption that holds until a future Paper changes it, and the failure then
        // would be a corrupted inventory rather than an exception.
        if (Bukkit.isPrimaryThread()) {
            this.show(player, menu);
            return;
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (player.isOnline()) {
                this.show(player, menu);
            }
        });
    }

    private void show(Player player, Menu menu) {
        menu.open(player);

        // The note this network's menus have opened with since its first version.
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    /** Tells a player the other side of the network is speaking a version this build does not. */
    private void reportUnsupported(Player player) {
        player.sendMessage(this.text.of(this.messages.common.unsupportedMenu));
    }
}
