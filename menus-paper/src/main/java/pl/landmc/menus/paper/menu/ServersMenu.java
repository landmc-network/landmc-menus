package pl.landmc.menus.paper.menu;

import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.MenuChannel;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuAction;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.platform.paper.menu.Items;
import pl.landmc.platform.paper.menu.PaginatedMenu;

/** The server list: where a player can go, and how busy each one is. */
public final class ServersMenu extends PaginatedMenu<MenuPayload.Servers.Server> {

    private final MenusMessages.ServersSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;
    private final String currentServer;

    public ServersMenu(
            MenuPayload.Servers payload,
            MenusMessages.ServersSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(style.text().of(messages.title), Math.clamp(messages.rows, 1, 6), payload.servers());

        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.currentServer = payload.currentServer();
    }

    @Override
    protected ItemStack render(MenuPayload.Servers.Server server) {
        Map<String, String> placeholders = Map.of(
                "{SERVER}", this.style.text().escaped(server.displayName()),
                "{ONLINE}", Integer.toString(server.online()));

        if (!server.reachable()) {
            return Items.of(Material.GRAY_DYE)
                    .name(this.style.text().of(this.messages.serverOffline, placeholders))
                    .lore(this.style.text().ofAll(this.messages.serverOfflineLore, placeholders))
                    .plain()
                    .build();
        }

        boolean current = server.id().equals(this.currentServer);

        Items.Builder item = Items.of(current ? Material.LIME_DYE : Material.PAPER)
                .name(this.style.text().of(this.messages.serverName, placeholders))
                .lore(this.style.text().ofAll(
                        current ? this.messages.serverCurrentLore : this.messages.serverLore,
                        placeholders))
                // The stack size is the player count, which is how this menu has always read.
                .amount(Math.max(1, server.online()))
                .plain();

        if (current) {
            item.glowing();
        }

        return item.build();
    }

    @Override
    protected void onSelect(Player player, MenuPayload.Servers.Server server, ClickType type) {
        if (!server.reachable() || server.id().equals(this.currentServer)) {
            return;
        }

        // The proxy decides whether this player may go there. This only asks, and asks for no
        // more than /server would.
        this.channel.send(player, MenuAction.of(MenuKind.SERVERS, "connect", server.id()));
        player.closeInventory();
    }

    @Override
    protected void decorate() {
        this.fill(this.style.filler());
    }

    @Override
    protected ItemStack previousPageItem(int page, int pageCount) {
        return this.style.previousPage(page, pageCount);
    }

    @Override
    protected ItemStack nextPageItem(int page, int pageCount) {
        return this.style.nextPage(page, pageCount);
    }
}
