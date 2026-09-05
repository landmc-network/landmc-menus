package pl.landmc.menus.paper.menu;

import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.MenuChannel;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuAction;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.platform.paper.menu.Items;
import pl.landmc.platform.paper.menu.Menu;

/**
 * {@code /sklep} - the premium shop.
 *
 * <p>The same two doors the original had, in the same two slots: ranks and topping up. It sells
 * nothing itself, which is why nothing here needs to know what a diamond is.
 */
public final class ShopMenu extends Menu {

    /** Where the original put each tile. Players who used that shop reach for these positions. */
    private static final int HEADER_SLOT = 4;
    private static final int RANKS_SLOT = 21;
    private static final int TOP_UP_SLOT = 23;

    private final MenuPayload.Shop payload;
    private final MenusMessages.ShopSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public ShopMenu(
            MenuPayload.Shop payload,
            MenusMessages.ShopSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(style.text().of(messages.title), Math.clamp(messages.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        this.item(HEADER_SLOT, this.header());

        this.button(RANKS_SLOT, this.ranks(), (player, type) ->
                this.send(player, MenuAction.of(MenuKind.SHOP, "ranks")));

        this.button(TOP_UP_SLOT, this.topUp(), (player, type) ->
                this.send(player, MenuAction.of(MenuKind.SHOP, "topup")));

        this.fill(this.style.filler());
    }

    private ItemStack header() {
        return Items.of(Material.OAK_SIGN)
                .name(this.style.text().of(this.messages.headerName))
                .lore(this.style.text().ofAll(this.messages.headerLore, Map.of()))
                .plain()
                .build();
    }

    private ItemStack ranks() {
        Map<String, String> placeholders = Map.of(
                // The rank names arrive already coloured by the shop, so they are not escaped:
                // they are the network's own configuration, not anything a player typed.
                "{RANKS}", String.join("<dark_gray>, ", this.payload.rankNames()),
                "{FROM}", Long.toString(this.payload.cheapestRank()));

        return Items.of(Material.PAPER)
                .name(this.style.text().of(this.messages.ranksName, placeholders))
                .lore(this.style.text().ofAll(this.messages.ranksLore, placeholders))
                .plain()
                .build();
    }

    private ItemStack topUp() {
        Map<String, String> placeholders =
                Map.of("{RATE}", Integer.toString(this.payload.diamondsPerPln()));

        return Items.of(Material.WRITABLE_BOOK)
                .name(this.style.text().of(this.messages.topUpName, placeholders))
                .lore(this.style.text().ofAll(this.messages.topUpLore, placeholders))
                .plain()
                .build();
    }

    private void send(Player player, MenuAction action) {
        this.channel.send(player, action);
        // Closed rather than left open: what happens next is another menu or a message from the
        // proxy, and both arrive behind an inventory that is no longer true.
        player.closeInventory();
    }
}
