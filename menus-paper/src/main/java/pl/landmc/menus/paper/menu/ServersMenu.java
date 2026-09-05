package pl.landmc.menus.paper.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.MenuChannel;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuAction;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.platform.paper.menu.Items;
import pl.landmc.platform.paper.menu.Menu;

/**
 * The list of servers, and the list of hubs - the same menu, drawn from the same bytes.
 *
 * <p>Every tile sits where the server says it sits, wears the material the server names and
 * carries the lines the server wrote. That is how the old server's list of modes read: SkyBlock
 * in the middle as a block of grass with three lines about building an island, the minigames on
 * the row below it, each with its own block. Laying the tiles out evenly instead - which is what
 * this menu used to do - can only ever produce a row of identical dyes, and loses the one thing
 * the menu was for, which is telling somebody what they would be walking into.
 *
 * <p>So the layout is configuration on the side that owns the list, and this draws it. Nothing
 * here knows that SkyBlock exists.
 *
 * <p>No pagination and no filler, both for the same reason: the original had neither. A slot
 * with nothing in it stays empty.
 */
public final class ServersMenu extends Menu {

    private final MenuPayload.Servers payload;
    private final MenusMessages.ServersSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    /**
     * Which menu a click reports itself as.
     *
     * <p>The two lists are drawn the same and answered by different plugins - the hubs by the
     * proxy's lobby handler, the modes by its server handler - so a click has to say which one
     * it came from.
     */
    private final MenuKind kind;

    public ServersMenu(
            MenuPayload.Servers payload,
            MenusMessages.ServersSection messages,
            MenuStyle style,
            MenuChannel channel,
            MenuKind kind) {

        super(style.text().of(messages.title), Math.clamp(messages.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    @Override
    protected void redraw() {
        if (this.messages.infoEnabled && this.fits(this.messages.infoSlot)) {
            this.item(this.messages.infoSlot, this.info());
        }

        for (MenuPayload.Servers.Server server : this.payload.servers()) {
            // A slot outside this menu would throw while drawing and lose the whole list. The
            // side that sent it decides the layout, and it can be wrong about how big this is.
            if (!this.fits(server.slot())) {
                continue;
            }

            ItemStack tile = this.render(server);

            if (!server.reachable() || server.id().equals(this.payload.currentServer())) {
                this.item(server.slot(), tile);
                continue;
            }

            this.button(server.slot(), tile, (player, type) -> {
                // The proxy decides whether this player may go there. This only asks, and asks
                // for no more than /server would.
                this.channel.send(player, MenuAction.of(this.kind, "connect", server.id()));
                player.closeInventory();
            });
        }
    }

    private ItemStack render(MenuPayload.Servers.Server server) {
        // The name and the lore are the network's own configuration and arrive written as
        // colour, so neither is escaped - unlike a player's name or a punishment reason.
        Map<String, String> placeholders = Map.of(
                "{SERVER}", server.displayName(),
                "{ONLINE}", Integer.toString(server.online()));

        if (!server.reachable()) {
            return Items.of(material(server.icon(), Material.PAPER))
                    .name(this.style.text().of(this.messages.serverOffline, placeholders))
                    .lore(this.style.text().ofAll(this.messages.serverOfflineLore, placeholders))
                    .plain()
                    .build();
        }

        if (!server.id().equals(this.payload.currentServer())) {
            return Items.of(material(server.icon(), Material.PAPER))
                    .name(this.style.text().of(this.messages.serverName, placeholders))
                    .lore(this.style.text().ofAll(server.lore(), placeholders))
                    .plain()
                    .build();
        }

        // The server the player is standing on. The old one stripped the colour off the name
        // before turning it red, replaced the last line of the lore and changed the dye.
        Map<String, String> current = Map.of(
                "{SERVER}", this.style.text().stripped(server.displayName()),
                "{ONLINE}", Integer.toString(server.online()));

        Material icon = this.messages.serverCurrentIcon.isBlank()
                ? material(server.icon(), Material.PAPER)
                : material(this.messages.serverCurrentIcon, material(server.icon(), Material.PAPER));

        return Items.of(icon)
                .name(this.style.text().of(this.messages.serverCurrentName, current))
                .lore(this.style.text().ofAll(this.currentLore(server.lore()), placeholders))
                .plain()
                .glowing()
                .build();
    }

    /** The server's own lore with its last line replaced, which is what the original did. */
    private List<String> currentLore(List<String> lore) {
        if (lore.isEmpty()) {
            return List.of(this.messages.serverCurrentLine);
        }

        List<String> lines = new ArrayList<>(lore);
        lines.set(lines.size() - 1, this.messages.serverCurrentLine);
        return lines;
    }

    private ItemStack info() {
        return Items.of(material(this.messages.infoIcon, Material.OAK_SIGN))
                .name(this.style.text().of(this.messages.infoName))
                .lore(this.style.text().ofAll(this.messages.infoLore, Map.of()))
                .plain()
                .build();
    }

    private boolean fits(int slot) {
        return slot >= 0 && slot < this.size();
    }

    /** The named material, or the fallback when this server does not have it. */
    private static Material material(String name, Material fallback) {
        Material material = Material.matchMaterial(name);
        return material == null || material.isAir() ? fallback : material;
    }
}
