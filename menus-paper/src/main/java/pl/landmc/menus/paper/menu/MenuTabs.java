package pl.landmc.menus.paper.menu;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.platform.paper.menu.Items;

/**
 * The strip along the top of the profile and the friends list.
 *
 * <p>The old server drew the same bar in both, which is what made them one place rather than two
 * menus that happen to be related: whichever you were looking at, the other was one click away.
 * Ours had no way back at all - {@code /profil} opened the friends list and that was the end of
 * it.
 *
 * <p>The colours are the ones it used: magenta for the profile, lime for the friends, and a
 * plain grey row behind them.
 */
public final class MenuTabs {

    /** Where the two tabs sit, and where the row they sit in ends. */
    public static final int PROFILE_SLOT = 0;
    public static final int FRIENDS_SLOT = 1;
    public static final int WIDTH = 9;

    private MenuTabs() {
    }

    /** The background the tabs are drawn on. */
    public static ItemStack filler() {
        return Items.filler(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static ItemStack profile(MenusMessages.CommonSection messages, MenuStyle style, boolean active) {
        return tab(Material.MAGENTA_STAINED_GLASS_PANE, messages.tabProfile, style, active);
    }

    public static ItemStack friends(MenusMessages.CommonSection messages, MenuStyle style, boolean active) {
        return tab(Material.LIME_STAINED_GLASS_PANE, messages.tabFriends, style, active);
    }

    /**
     * One tab.
     *
     * <p>The one you are looking at glows, which is how the old server showed it - it enchanted
     * the pane and hid the enchantment.
     */
    private static ItemStack tab(
            Material material, String name, MenuStyle style, boolean active) {

        Items.Builder item = Items.of(material)
                .name(style.text().of(name, Map.of()))
                .plain();

        if (active) {
            item.glowing();
        }

        return item.build();
    }
}
