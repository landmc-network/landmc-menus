package pl.landmc.menus.paper.menu;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.Durations;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.platform.paper.menu.Items;
import pl.landmc.platform.paper.menu.PaginatedMenu;

/**
 * Somebody's punishment history, newest first.
 *
 * <p>Read-only. Lifting a punishment is a command with a permission behind it, and putting a
 * button for it in a menu that staff open while looking at somebody is how a ban gets lifted by
 * a misclick.
 */
public final class PunishmentsMenu extends PaginatedMenu<MenuPayload.Punishments.Punishment> {

    /** What each kind of punishment is drawn as. */
    private static final Map<String, Material> ICONS = Map.of(
            "BAN", Material.RED_DYE,
            "TEMPBAN", Material.ORANGE_DYE,
            "BANIP", Material.RED_DYE,
            "KICK", Material.YELLOW_DYE,
            "WARN", Material.PAPER,
            "MUTE", Material.ORANGE_DYE);

    private static final Material UNKNOWN_ICON = Material.PAPER;

    private final MenusMessages.PunishmentsSection messages;
    private final MenuStyle style;
    private final ZoneId zone;

    public PunishmentsMenu(
            MenuPayload.Punishments payload,
            MenusMessages.PunishmentsSection messages,
            MenuStyle style,
            ZoneId zone) {

        super(
                style.text().of(
                        messages.title,
                        Map.of("{PLAYER}", style.text().escaped(payload.subject()))),
                Math.clamp(messages.rows, 1, 6),
                payload.punishments());

        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.zone = Objects.requireNonNull(zone, "zone");
    }

    @Override
    protected ItemStack render(MenuPayload.Punishments.Punishment punishment) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{TYPE}", this.style.text().escaped(punishment.type()));
        placeholders.put("{ID}", Long.toString(punishment.id()));
        // A reason is free text somebody typed, so it is escaped before it reaches the parser.
        placeholders.put("{REASON}", this.style.text().escaped(punishment.reason()));
        placeholders.put("{STAFF}", this.style.text().escaped(punishment.staff()));
        placeholders.put("{DATE}", Durations.date(punishment.issuedAt(), this.zone));
        placeholders.put("{STATE}", this.state(punishment));

        return Items.of(ICONS.getOrDefault(punishment.type(), UNKNOWN_ICON))
                .name(this.style.text().of(this.messages.punishmentName, placeholders))
                .lore(this.style.text().ofAll(this.messages.punishmentLore, placeholders))
                .plain()
                .build();
    }

    /** Whether it still bites, and for how long. */
    private String state(MenuPayload.Punishments.Punishment punishment) {
        if (!punishment.active()) {
            // A punishment that ran its course and one that staff lifted read differently,
            // because the second is a decision somebody made and the first is not.
            return punishment.expiresAt() > 0 && punishment.expiresAt() <= System.currentTimeMillis()
                    ? this.messages.stateExpired
                    : this.messages.stateLifted;
        }

        if (punishment.expiresAt() == 0) {
            return this.messages.statePermanent;
        }

        return this.messages.stateActive.replace(
                "{TIME}", Durations.remaining(punishment.expiresAt() - System.currentTimeMillis()));
    }

    @Override
    protected void onSelect(Player player, MenuPayload.Punishments.Punishment punishment, ClickType type) {
        // Nothing. See the note on the class.
    }

    @Override
    protected void decorate() {
        if (this.isEmpty()) {
            this.item(
                    this.size() / 2,
                    Items.of(Material.LIME_DYE)
                            .name(this.style.text().of(this.messages.noPunishments))
                            .lore(this.style.text().ofAll(this.messages.noPunishmentsLore, Map.of()))
                            .plain()
                            .build());
        }

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
