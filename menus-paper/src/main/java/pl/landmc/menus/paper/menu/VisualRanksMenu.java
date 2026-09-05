package pl.landmc.menus.paper.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import pl.landmc.menus.paper.MenuChannel;
import pl.landmc.menus.paper.config.MenusMessages;
import pl.landmc.menus.protocol.MenuAction;
import pl.landmc.menus.protocol.MenuKind;
import pl.landmc.menus.protocol.MenuPayload;
import pl.landmc.platform.paper.menu.Items;
import pl.landmc.platform.paper.menu.PaginatedMenu;

/**
 * The visual ranks: a name of your own, worn instead of your rank's.
 *
 * <p>Paginated, which the original was not - it had forty-five of these on one screen and no
 * room for the forty-sixth. The list is configuration now, so it has to survive being longer.
 *
 * <p>Nothing is decided here. A tile knows whether it is owned or worn because the shop said so,
 * and a click only asks; the plugin holding the diamonds checks all of it again.
 */
public final class VisualRanksMenu extends PaginatedMenu<MenuPayload.VisualRanks.Offer> {

    private final MenuPayload.VisualRanks payload;
    private final MenusMessages.VisualRanksSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public VisualRanksMenu(
            MenuPayload.VisualRanks payload,
            MenusMessages.VisualRanksSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(style.text().of(messages.title), Math.clamp(messages.rows, 1, 6), payload.offers());

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected ItemStack render(MenuPayload.VisualRanks.Offer offer) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{RANK}", this.style.text().escaped(offer.name()));
        placeholders.put("{PRICE}", Long.toString(offer.price()));
        placeholders.put("{MISSING}", Long.toString(offer.missing(this.payload.balance())));
        placeholders.put("{STATE}", this.state(offer));

        Items.Builder item = Items.of(Material.OAK_SIGN)
                .name(this.style.text().of(this.messages.rankName, placeholders))
                .lore(this.style.text().ofAll(this.messages.rankLore, placeholders))
                .plain();

        if (this.isActive(offer)) {
            item.glowing();
        }

        return item.build();
    }

    @Override
    protected void onSelect(
            Player player, MenuPayload.VisualRanks.Offer offer, ClickType type) {

        // The one it is already wearing does nothing, so a double click cannot buy anything.
        if (this.isActive(offer)) {
            return;
        }

        String action = offer.owned() ? "use" : "buy";
        this.channel.send(player, MenuAction.of(MenuKind.VISUAL_RANKS, action, offer.id()));
    }

    @Override
    protected void decorate() {
        int bottom = this.size() - 9;

        if (!this.payload.active().isEmpty()) {
            this.button(bottom, this.clear(), (player, type) -> this.channel.send(
                    player, MenuAction.of(MenuKind.VISUAL_RANKS, "clear")));
        }

        this.button(bottom + 8, this.back(), (player, type) -> this.channel.send(
                player, MenuAction.of(MenuKind.VISUAL_RANKS, "back")));

        this.fill(this.style.filler());
    }

    private String state(MenuPayload.VisualRanks.Offer offer) {
        if (this.isActive(offer)) {
            return this.messages.stateActive;
        }
        if (offer.owned()) {
            return this.messages.stateOwned;
        }
        return offer.missing(this.payload.balance()) > 0L
                ? this.messages.stateTooPoor
                : this.messages.stateBuy;
    }

    private boolean isActive(MenuPayload.VisualRanks.Offer offer) {
        return offer.id().equals(this.payload.active());
    }

    private ItemStack clear() {
        return Items.of(Material.BARRIER)
                .name(this.style.text().of(this.messages.clearName))
                .lore(this.style.text().ofAll(this.messages.clearLore, Map.of()))
                .plain()
                .build();
    }

    private ItemStack back() {
        return Items.of(Material.ARROW)
                .name(this.style.text().of(this.messages.backName))
                .lore(this.style.text().ofAll(this.messages.backLore, Map.of()))
                .plain()
                .build();
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
