package pl.landmc.menus.paper.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
 * The daily reward: seven days in a row, and what each one is worth.
 *
 * <p>All seven are drawn, not only the one that can be taken. A player deciding whether to come
 * back tomorrow is deciding about tomorrow's tile, and a menu showing today alone would be a
 * button rather than a reason.
 *
 * <p>Three states and three different sentences, the same shape the cosmetics menu uses: a day
 * already taken says so, today says to click, and a day still ahead says which day it is. Only
 * today does anything when clicked - the rest are there to be read.
 *
 * <p>Nothing here decides what anybody gets. The amounts arrive with the payload and the click
 * sends back nothing but "the reward, please": what a day is worth and whether it is really
 * today are the proxy's to answer, because the proxy is where the wallets are.
 */
public final class DailyMenu extends Menu {

    private static final String CLAIMED = "CLAIMED";
    private static final String TODAY = "TODAY";
    private static final String NEXT = "NEXT";

    private final MenuPayload.Daily payload;
    private final MenusMessages.DailySection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public DailyMenu(
            MenuPayload.Daily payload,
            MenusMessages.DailySection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(
                style.text().of(
                        messages.title,
                        Map.of("{STREAK}", Integer.toString(payload.streak()))),
                Math.clamp(messages.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        List<Integer> slots = this.messages.slots;
        List<MenuPayload.Daily.Day> days = this.payload.days();

        for (int index = 0; index < days.size() && index < slots.size(); index++) {
            MenuPayload.Daily.Day day = days.get(index);
            int slot = slots.get(index);

            // A slot outside this menu would throw while drawing and lose the whole screen.
            if (slot < 0 || slot >= this.size()) {
                continue;
            }

            this.button(slot, this.render(day), (player, type) -> {
                if (!TODAY.equals(day.state())) {
                    // A day already taken or still ahead. Silent: the tile says which it is.
                    return;
                }

                // Left open. The proxy answers with the menu again and it is redrawn in place,
                // so the tile turns over under the cursor.
                this.channel.send(player, MenuAction.of(MenuKind.DAILY, "claim"));
            });
        }

        this.fill(this.style.filler());
    }

    private ItemStack render(MenuPayload.Daily.Day day) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{DAY}", Integer.toString(day.day()));
        placeholders.put("{COINS}", Long.toString(day.coins()));
        placeholders.put("{DIAMONDS}", Long.toString(day.diamonds()));
        placeholders.put("{STREAK}", Integer.toString(this.payload.streak()));

        String state = day.state().toUpperCase(Locale.ROOT);
        Items.Builder tile = Items.of(this.material(state))
                .name(this.style.text().of(this.messages.dayName, placeholders))
                .lore(this.style.text().ofAll(this.lore(state), placeholders))
                .amount(day.day())
                .plain();

        if (TODAY.equals(state)) {
            // The one tile worth looking at is the one that glows.
            tile.glowing();
        }
        return tile.build();
    }

    private List<String> lore(String state) {
        return switch (state) {
            case CLAIMED -> this.messages.dayClaimedLore;
            case TODAY -> this.messages.dayTodayLore;
            case NEXT -> this.messages.dayNextLore;
            default -> this.messages.dayLockedLore;
        };
    }

    private Material material(String state) {
        String name = switch (state) {
            case CLAIMED -> this.messages.claimedMaterial;
            case TODAY -> this.messages.todayMaterial;
            case NEXT -> this.messages.nextMaterial;
            default -> this.messages.lockedMaterial;
        };

        Material material = Material.matchMaterial(name.toUpperCase(Locale.ROOT));
        return material == null || material.isAir() ? Material.CHEST : material;
    }
}
