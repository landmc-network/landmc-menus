package pl.landmc.menus.paper.menu;

import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.Text;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.platform.paper.menu.Items;

/**
 * What every menu shares: how to turn configured text into components, and the parts that look
 * the same in all of them.
 *
 * <p>The page arrows in particular. They are the one thing a player learns once and then expects
 * everywhere, so they are built here rather than three times.
 */
public record MenuStyle(Text text, MenusMessages.CommonSection messages) {

    public MenuStyle {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(messages, "messages");
    }

    /** The grey pane every menu is bordered with, as this network's menus always have been. */
    public ItemStack filler() {
        return Items.filler(Material.GRAY_STAINED_GLASS_PANE);
    }

    public ItemStack previousPage(int page, int pageCount) {
        return this.pageArrow(this.messages.previousPage, page, pageCount);
    }

    public ItemStack nextPage(int page, int pageCount) {
        return this.pageArrow(this.messages.nextPage, page, pageCount);
    }

    private ItemStack pageArrow(String name, int page, int pageCount) {
        Map<String, String> placeholders = Map.of(
                // Page numbers are zero-based in the code and one-based to a person.
                "{PAGE}", Integer.toString(page + 1),
                "{PAGES}", Integer.toString(pageCount));

        return Items.of(Material.ARROW)
                .name(this.text.of(name, placeholders))
                .lore(this.text.of(this.messages.pageLore, placeholders))
                .plain()
                .build();
    }
}
