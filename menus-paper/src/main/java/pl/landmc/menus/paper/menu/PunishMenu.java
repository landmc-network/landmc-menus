package pl.landmc.menus.paper.menu;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.MenuChannel;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuAction;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.platform.paper.menu.Items;
import pl.landmc.platform.paper.menu.Menu;

/**
 * The screen a staff member picks a punishment from.
 *
 * <p>One tile per offence and three clicks on each: a left click warns, a right click bans and a
 * shift-right click kicks. That is how the previous version of the network worked, and the
 * reason it worked that way is that the three are the same decision at three strengths - a
 * separate tile for each would be twelve tiles saying four things.
 *
 * <p>Nothing here knows how long a ban is or what its reason says. The tile carries a name for
 * each click and an identifier to send back; the side that records punishments decides what the
 * identifier means. A menu that named its own durations would be a client telling the proxy how
 * long to ban somebody for.
 *
 * <p>The window closes on any click that does something. A staff member who has just banned
 * somebody is finished with the screen, and leaving it open invites a second click on a menu
 * whose tiles no longer describe the situation.
 */
public final class PunishMenu extends Menu {

    private final MenuPayload.Punish payload;
    private final MenusMessages.PunishSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public PunishMenu(
            MenuPayload.Punish payload,
            MenusMessages.PunishSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(
                style.text().of(
                        messages.title,
                        Map.of("{PLAYER}", style.text().escaped(payload.subject()))),
                Math.clamp(messages.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        for (MenuPayload.Punish.Option option : this.payload.options()) {
            // A slot outside this menu would throw while drawing and lose the whole screen. The
            // layout is the proxy's to decide and a configuration can be wrong about how big
            // this is.
            if (option.slot() < 0 || option.slot() >= this.size()) {
                continue;
            }

            this.button(option.slot(), this.render(option), (player, type) -> {
                String click = click(type);
                if (click == null || labelOf(option, click).isEmpty()) {
                    // A click this tile does nothing on. Silent: the lore already says so.
                    return;
                }

                player.closeInventory();
                this.channel.send(
                        player, MenuAction.of(MenuKind.PUNISH, click, option.id()));
            });
        }

        this.fill(this.style.filler());
    }

    private ItemStack render(MenuPayload.Punish.Option option) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{NAME}", option.name());
        placeholders.put("{PLAYER}", this.style.text().escaped(this.payload.subject()));
        placeholders.put("{LEFT}", this.describe(option.left()));
        placeholders.put("{RIGHT}", this.describe(option.right()));
        placeholders.put("{SHIFT_RIGHT}", this.describe(option.shiftRight()));

        return Items.of(material(option.icon()))
                .name(this.style.text().of(this.messages.optionName, placeholders))
                .lore(this.style.text().ofAll(this.messages.optionLore, placeholders))
                .plain()
                .build();
    }

    /** What a click is called, or the phrase for one that does nothing. */
    private String describe(String label) {
        return label.isEmpty() ? this.messages.optionNothing : label;
    }

    private static String labelOf(MenuPayload.Punish.Option option, String click) {
        return switch (click) {
            case "left" -> option.left();
            case "right" -> option.right();
            case "shift" -> option.shiftRight();
            default -> "";
        };
    }

    /**
     * Which of the three this click counts as.
     *
     * <p>Shift and a left click is deliberately not one of them: it is the gesture for moving a
     * stack, so people press it by accident, and banning somebody by accident is the thing this
     * screen most has to avoid.
     */
    private static String click(ClickType type) {
        return switch (type) {
            case LEFT -> "left";
            case RIGHT -> "right";
            case SHIFT_RIGHT -> "shift";
            default -> null;
        };
    }

    private static Material material(String name) {
        Material material = Material.matchMaterial(name.toUpperCase(Locale.ROOT));
        return material == null || material.isAir() ? Material.PAPER : material;
    }
}
