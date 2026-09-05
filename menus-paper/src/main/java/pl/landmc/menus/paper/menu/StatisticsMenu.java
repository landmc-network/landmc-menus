package pl.landmc.menus.paper.menu;

import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.MenuChannel;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.platform.paper.menu.Items;
import pl.landmc.platform.paper.menu.Menu;

/**
 * The third tab of the profile: whatever the network can currently say about a player.
 *
 * <p>This menu knows nothing about what it is showing. Every tile is a label, a value and a
 * material chosen by whoever owns the number, and the slot too - so a statistic that does not
 * exist yet costs a line in that plugin and nothing here.
 *
 * <p>That is deliberate rather than lazy. The things this network will eventually count -
 * islands, jobs, minigames - do not exist yet, and a menu with a field per statistic would have
 * to be changed on three servers at once every time one of them arrived.
 */
public final class StatisticsMenu extends Menu {

    private final MenuPayload.Statistics payload;
    private final MenusMessages messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public StatisticsMenu(
            MenuPayload.Statistics payload,
            MenusMessages messages,
            MenuStyle style,
            MenuChannel channel) {

        super(
                style.text().of(
                        messages.statistics.title,
                        Map.of("{PLAYER}", style.text().escaped(payload.subject()))),
                Math.clamp(messages.statistics.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        for (int slot = 0; slot < MenuTabs.WIDTH && slot < this.size(); slot++) {
            this.item(slot, MenuTabs.filler());
        }

        for (MenuTabs.Tab tab : MenuTabs.strip(
                this.messages.common, this.style, MenuKind.STATISTICS)) {

            if (tab.action() == null) {
                this.item(tab.slot(), tab.item());
                continue;
            }
            this.button(tab.slot(), tab.item(), (player, type) ->
                    this.channel.send(player, tab.action()));
        }

        if (this.payload.entries().isEmpty()) {
            this.item(this.middle(), this.nothing());
        }

        for (MenuPayload.Statistics.Entry entry : this.payload.entries()) {
            // A slot outside this menu would throw while drawing and lose the whole thing, so
            // the entry is skipped instead. The plugin that sent it decides the layout, and a
            // plugin can be wrong.
            if (entry.slot() < MenuTabs.WIDTH || entry.slot() >= this.size()) {
                continue;
            }
            this.item(entry.slot(), this.render(entry));
        }

        this.fill(this.style.filler());
    }

    private ItemStack render(MenuPayload.Statistics.Entry entry) {
        Map<String, String> placeholders = Map.of(
                // Both come from a plugin's configuration rather than from a player, and both
                // are meant to be readable as colour.
                "{LABEL}", entry.label(),
                "{VALUE}", entry.value());

        return Items.of(material(entry.icon()))
                .name(this.style.text().of(this.messages.statistics.entryName, placeholders))
                .lore(this.style.text().ofAll(this.messages.statistics.entryLore, placeholders))
                .plain()
                .build();
    }

    private ItemStack nothing() {
        return Items.of(Material.BOOK)
                .name(this.style.text().of(this.messages.statistics.emptyName))
                .lore(this.style.text().ofAll(this.messages.statistics.emptyLore, Map.of()))
                .plain()
                .build();
    }

    /** The middle square, by row and column - half the size is the left edge of the middle row. */
    private int middle() {
        return (this.size() / MenuTabs.WIDTH / 2) * MenuTabs.WIDTH + MenuTabs.WIDTH / 2;
    }

    /** The configured material, or paper when it names something this server does not have. */
    private static Material material(String name) {
        Material material = Material.matchMaterial(name);
        return material == null || material.isAir() ? Material.PAPER : material;
    }
}
