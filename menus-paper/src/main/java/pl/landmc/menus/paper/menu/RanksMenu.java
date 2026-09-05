package pl.landmc.menus.paper.menu;

import java.util.HashMap;
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
 * {@code /rangi} - the rank shop.
 *
 * <p>Each rank sits where the shop said to put it rather than in the next free space. The
 * original's layout is what players who used that shop remember, and a rank that moves between
 * builds is one somebody buys by accident.
 *
 * <p>Nothing is decided here. A tile knows whether it is owned because the shop said so, and a
 * click only asks to buy - the plugin that holds the money checks all of it again.
 */
public final class RanksMenu extends Menu {

    /** Where the original kept the way back. */
    private static final int BACK_SLOT = 53;

    private final MenuPayload.Ranks payload;
    private final MenusMessages.RanksSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public RanksMenu(
            MenuPayload.Ranks payload,
            MenusMessages.RanksSection messages,
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
        for (MenuPayload.Ranks.Offer offer : this.payload.offers()) {
            // A slot outside this inventory would throw while drawing and lose the whole menu,
            // so an offer configured into one is skipped instead. The shop's own log says which.
            if (offer.slot() < 0 || offer.slot() >= this.size()) {
                continue;
            }

            if (offer.owned()) {
                this.item(offer.slot(), this.render(offer));
                continue;
            }

            this.button(offer.slot(), this.render(offer), (player, type) ->
                    this.send(player, MenuAction.of(MenuKind.RANKS, "buy", offer.id())));
        }

        this.button(BACK_SLOT, this.back(), (player, type) ->
                this.send(player, MenuAction.of(MenuKind.RANKS, "back")));

        this.fill(this.style.filler());
    }

    private ItemStack render(MenuPayload.Ranks.Offer offer) {
        Map<String, String> placeholders = new HashMap<>();
        // The rank's name is the network's own configuration and arrives already coloured, so
        // it goes in as it is; everything else here is a number this menu wrote itself.
        placeholders.put("{RANK}", offer.displayName());
        placeholders.put("{PRICE}", Long.toString(offer.price()));
        placeholders.put("{COMMAND}", this.style.text().escaped(offer.infoCommand()));
        placeholders.put("{MISSING}", Long.toString(offer.missing(this.payload.balance())));
        placeholders.put("{STATE}", this.state(offer));

        Items.Builder item = offer.texture().isEmpty()
                ? Items.of(material(offer))
                : Items.head(offer.texture());

        item.name(this.style.text().of(this.messages.rankName, placeholders))
                .lore(this.style.text().ofAll(this.messages.rankLore, placeholders))
                .plain();

        if (offer.glowing()) {
            item.glowing();
        }

        return item.build();
    }

    /**
     * The last line of the lore: owned, affordable, or how much is missing.
     *
     * <p>Substituted into the lore as a placeholder rather than appended, so the shop's wording
     * stays one editable block instead of a template plus a line bolted on the end.
     */
    private String state(MenuPayload.Ranks.Offer offer) {
        if (offer.owned()) {
            return this.messages.stateOwned;
        }
        return offer.missing(this.payload.balance()) > 0L
                ? this.messages.stateTooPoor
                : this.messages.stateBuy;
    }

    private ItemStack back() {
        return Items.of(Material.ARROW)
                .name(this.style.text().of(this.messages.backName))
                .lore(this.style.text().ofAll(this.messages.backLore, Map.of()))
                .plain()
                .build();
    }

    /** The configured material, or paper when it names something this server does not have. */
    private static Material material(MenuPayload.Ranks.Offer offer) {
        Material material = Material.matchMaterial(offer.icon());
        return material == null || material.isAir() ? Material.PAPER : material;
    }

    private void send(Player player, MenuAction action) {
        this.channel.send(player, action);
        player.closeInventory();
    }
}
