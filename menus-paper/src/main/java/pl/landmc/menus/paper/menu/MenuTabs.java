package pl.landmc.menus.paper.menu;

import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuAction;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.platform.paper.menu.Items;

/**
 * The strip along the top of the profile, the friends list and the statistics.
 *
 * <p>The old server drew the same bar in all of them, which is what made them one place rather
 * than three menus that happen to know about each other. Ours had no way back at all until this
 * existed.
 *
 * <p>Described here and placed by each menu, rather than drawn here directly: filling slots is
 * a menu's own job and the platform keeps it that way. What this owns is which tabs there are,
 * what they look like and which one you are standing on - the part that was being written three
 * times over and went wrong in one of them.
 *
 * <p>The colours are the old server's: magenta for the profile, lime for the friends, white for
 * the statistics, on a plain grey row.
 */
public final class MenuTabs {

    public static final int WIDTH = 9;

    private MenuTabs() {
    }

    /** The background the tabs are drawn on. */
    public static ItemStack filler() {
        return Items.filler(Material.GRAY_STAINED_GLASS_PANE);
    }

    /**
     * The three tabs, in order.
     *
     * @param active which menu is being drawn. That tab glows and carries no action, because
     *     asking the proxy for the menu you are already looking at is a round trip that ends
     *     where it started.
     */
    public static List<Tab> strip(
            MenusMessages.CommonSection messages, MenuStyle style, MenuKind active) {

        return List.of(
                tab(0, Material.MAGENTA_STAINED_GLASS_PANE, messages.tabProfile, style,
                        active, MenuKind.PROFILE, "profile"),
                tab(1, Material.LIME_STAINED_GLASS_PANE, messages.tabFriends, style,
                        active, MenuKind.FRIENDS, "friends"),
                tab(2, Material.WHITE_STAINED_GLASS_PANE, messages.tabStatistics, style,
                        active, MenuKind.STATISTICS, "statistics"));
    }

    private static Tab tab(
            int slot,
            Material material,
            String name,
            MenuStyle style,
            MenuKind active,
            MenuKind self,
            String action) {

        boolean current = active == self;

        Items.Builder item = Items.of(material)
                .name(style.text().of(name, Map.of()))
                .plain();

        if (current) {
            // The old server enchanted the pane and hid the enchantment to say "you are here".
            item.glowing();
        }

        return new Tab(slot, item.build(), current ? null : MenuAction.of(active, action));
    }

    /**
     * One tab, ready to be placed.
     *
     * @param action what to send when it is clicked, or null for the tab already being shown
     */
    public record Tab(int slot, ItemStack item, @Nullable MenuAction action) {
    }
}
