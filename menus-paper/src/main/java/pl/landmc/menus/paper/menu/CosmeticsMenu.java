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
 * The cosmetics: what is on offer, what has been bought and what is being worn.
 *
 * <p>Every family on one screen rather than a screen each. A player wears one trail and one
 * glow at the same time, and the thing they most want to see is which - splitting that across
 * two menus means clicking twice to answer one question.
 *
 * <p>Three states per tile, and they are three different sentences: not bought yet says what it
 * costs, bought says to put it on, worn says to take it off. Clicking is the same action either
 * way - the shop decides what it means, because it is the side that knows what was paid.
 */
public final class CosmeticsMenu extends Menu {

    private final MenuPayload.Cosmetics payload;
    private final MenusMessages.CosmeticsSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public CosmeticsMenu(
            MenuPayload.Cosmetics payload,
            MenusMessages.CosmeticsSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(
                style.text().of(
                        messages.title,
                        Map.of("{BALANCE}", Long.toString(payload.balance()))),
                Math.clamp(messages.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        for (MenuPayload.Cosmetics.Offer offer : this.payload.offers()) {
            // A slot outside this menu would throw while drawing and lose the whole thing. The
            // shop decides the layout and a configuration can be wrong about how big this is.
            if (offer.slot() < 0 || offer.slot() >= this.size()) {
                continue;
            }

            this.button(offer.slot(), this.render(offer), (player, type) -> {
                this.channel.send(player, MenuAction.of(MenuKind.COSMETICS, "use", offer.id()));
                player.closeInventory();
            });
        }

        this.fill(this.style.filler());
    }

    private ItemStack render(MenuPayload.Cosmetics.Offer offer) {
        boolean worn = offer.id().equals(this.payload.worn().get(offer.family()));

        Map<String, String> placeholders = Map.of(
                // The name is the network's own configuration and arrives written as colour.
                "{NAME}", offer.name(),
                "{PRICE}", Long.toString(offer.price()),
                "{BALANCE}", Long.toString(this.payload.balance()),
                "{MISSING}", Long.toString(offer.missing(this.payload.balance())));

        Items.Builder item = Items.of(material(offer.icon()))
                .name(this.style.text().of(this.messages.offerName, placeholders))
                .lore(this.style.text().ofAll(this.lore(offer, worn), placeholders))
                .plain();

        if (worn) {
            item.glowing();
        }

        return item.build();
    }

    private java.util.List<String> lore(MenuPayload.Cosmetics.Offer offer, boolean worn) {
        if (worn) {
            return this.messages.offerWornLore;
        }
        if (offer.owned()) {
            return this.messages.offerOwnedLore;
        }
        return offer.missing(this.payload.balance()) > 0L
                ? this.messages.offerTooPoorLore
                : this.messages.offerForSaleLore;
    }

    /** The configured material, or paper when it names something this server does not have. */
    private static Material material(String name) {
        Material material = Material.matchMaterial(name);
        return material == null || material.isAir() ? Material.PAPER : material;
    }
}
