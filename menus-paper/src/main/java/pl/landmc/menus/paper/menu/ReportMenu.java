package pl.landmc.menus.paper.menu;

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
 * Pick what somebody is being reported for.
 *
 * <p>A menu rather than a typed reason, exactly as the old server had it: the list of things
 * worth reporting is short and fixed, and staff reading a queue of reports want four words they
 * recognise rather than free text somebody typed in anger.
 *
 * <p>The sign at the top names who is being reported. It matters more than it looks: the
 * command takes a name, the menu opens a moment later, and without it a player who typed the
 * wrong nick has no way to notice before clicking.
 *
 * <p>The menu closes on the first click. There is no confirmation step, because the original
 * had none and a report is not a purchase - the cost of one sent by accident is that staff read
 * a line, and the cooldown behind it is what stops that being a nuisance.
 */
public final class ReportMenu extends Menu {

    private final MenuPayload.Report payload;
    private final MenusMessages.ReportSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public ReportMenu(
            MenuPayload.Report payload,
            MenusMessages.ReportSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(
                style.text().of(
                        messages.title, Map.of("{PLAYER}", payload.subject())),
                Math.clamp(messages.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        if (this.fits(this.messages.subjectSlot)) {
            this.item(this.messages.subjectSlot, this.subject());
        }

        for (MenuPayload.Report.Reason reason : this.payload.reasons()) {
            // A slot outside this menu would throw while drawing and lose the whole thing. The
            // proxy decides the layout, and a configuration can be wrong about how big this is.
            if (!this.fits(reason.slot())) {
                continue;
            }

            this.button(reason.slot(), this.render(reason), (player, type) -> {
                this.channel.send(
                        player, MenuAction.of(MenuKind.REPORT, "send", reason.id()));
                player.closeInventory();
            });
        }
    }

    private ItemStack render(MenuPayload.Report.Reason reason) {
        Map<String, String> placeholders = Map.of(
                // The label is the network's own configuration and arrives written as colour.
                "{REASON}", reason.label(),
                "{PLAYER}", this.payload.subject());

        return Items.of(material(reason.icon(), Material.PAPER))
                .name(this.style.text().of(this.messages.reasonName, placeholders))
                .lore(this.style.text().ofAll(this.messages.reasonLore, placeholders))
                .plain()
                .build();
    }

    private ItemStack subject() {
        Map<String, String> placeholders = Map.of("{PLAYER}", this.payload.subject());

        return Items.of(material(this.messages.subjectIcon, Material.OAK_SIGN))
                .name(this.style.text().of(this.messages.subjectName, placeholders))
                .lore(this.style.text().ofAll(this.messages.subjectLore, placeholders))
                .plain()
                .build();
    }

    private boolean fits(int slot) {
        return slot >= 0 && slot < this.size();
    }

    private static Material material(String name, Material fallback) {
        Material material = Material.matchMaterial(name);
        return material == null || material.isAir() ? fallback : material;
    }
}
