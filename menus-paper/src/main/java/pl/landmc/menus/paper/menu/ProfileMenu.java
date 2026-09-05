package pl.landmc.menus.paper.menu;

import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
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
 * A player's own profile, laid out the way the old server laid it out.
 *
 * <p>The tab strip along the top, the head in the middle, the way into the rank shop and the
 * premium login setting, in the slots it used. The strip is the part that matters most: it is
 * what makes the profile and the friends list one place rather than two menus that happen to
 * know about each other.
 *
 * <p>A summary, not a place things happen. Every tile either states a fact or opens the thing
 * that owns it, so this menu holds no logic of its own and cannot disagree with what those show.
 *
 * <p>What the original had here and this does not: a statistics sub-view with coins, diamonds
 * and a visual rank, and a whole tab of minigame statistics. This network has no second
 * currency, no visual ranks and no minigames, and a tile that says "coming soon" is a tile that
 * says the menu was not finished.
 */
public final class ProfileMenu extends Menu {

    /**
     * Six rows, and not configurable.
     *
     * <p>The tiles sit in the slots the old server used, and those only exist in a menu this
     * size. A smaller one folded them onto each other and the shop tile disappeared underneath
     * the premium book - which is exactly what happened the first time this was drawn.
     */
    private static final int ROWS = 6;

    /** Where the old server put each tile. */
    private static final int HEAD_SLOT = 22;
    private static final int SHOP_SLOT = 38;
    private static final int PREMIUM_SLOT = 40;
    private static final int VISUAL_SLOT = 42;

    /** Which tab this menu is, so the strip knows which one to light. */
    private static final MenuKind SELF = MenuKind.PROFILE;

    private final MenuPayload.Profile payload;
    private final MenusMessages messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public ProfileMenu(
            MenuPayload.Profile payload,
            MenusMessages messages,
            MenuStyle style,
            MenuChannel channel) {

        super(
                style.text().of(
                        messages.profile.title,
                        Map.of("{PLAYER}", style.text().escaped(payload.playerName()))),
                ROWS);

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        this.tabs();

        this.item(HEAD_SLOT, this.head());

        this.button(SHOP_SLOT, this.shop(), (player, type) ->
                this.send(player, MenuAction.of(MenuKind.PROFILE, "shop")));

        this.button(PREMIUM_SLOT, this.premium(), (player, type) ->
                this.send(player, MenuAction.of(MenuKind.PROFILE, "premium")));

        // Left open: this one is answered with another menu.
        this.button(VISUAL_SLOT, this.visual(), (player, type) ->
                this.channel.send(player, MenuAction.of(MenuKind.PROFILE, "visual")));

        this.fill(this.style.filler());
    }

    /** The strip along the top: this menu, and the way to the other two. */
    private void tabs() {
        for (int slot = 0; slot < MenuTabs.WIDTH && slot < this.size(); slot++) {
            this.item(slot, MenuTabs.filler());
        }

        for (MenuTabs.Tab tab : MenuTabs.strip(this.messages.common, this.style, SELF)) {
            if (tab.action() == null) {
                this.item(tab.slot(), tab.item());
                continue;
            }
            // Left open: every one of these is answered with another menu, and closing first
            // would show the world for the length of one round trip to the proxy.
            this.button(tab.slot(), tab.item(), (player, type) ->
                    this.channel.send(player, tab.action()));
        }
    }

    private ItemStack head() {
        MenusMessages.ProfileSection profile = this.messages.profile;

        String rank = this.payload.rank().isBlank() ? profile.noRank : this.payload.rank();

        Map<String, String> placeholders = Map.of(
                "{PLAYER}", this.style.text().escaped(this.payload.playerName()),
                // Not escaped. A rank prefix is set by an administrator in LuckPerms and is
                // written to be read as colour; the player's name is the one value on this tile
                // that somebody else chose.
                "{RANK}", rank,
                "{SERVER}", this.style.text().escaped(this.payload.currentServer()),
                "{FRIENDS}", Integer.toString(this.payload.friends()),
                "{REQUESTS}", Integer.toString(this.payload.pendingRequests()));

        return Items.head(Bukkit.getOfflinePlayer(this.payload.playerName()))
                .name(this.style.text().of(profile.playerName, placeholders))
                .lore(this.style.text().ofAll(profile.playerLore, placeholders))
                .plain()
                .build();
    }

    private ItemStack visual() {
        return Items.of(Material.NAME_TAG)
                .name(this.style.text().of(this.messages.profile.visualName))
                .lore(this.style.text().ofAll(this.messages.profile.visualLore, Map.of()))
                .plain()
                .build();
    }

    private ItemStack shop() {
        return Items.of(Material.PAPER)
                .name(this.style.text().of(this.messages.profile.shopName))
                .lore(this.style.text().ofAll(this.messages.profile.shopLore, Map.of()))
                .plain()
                .build();
    }

    /**
     * The way into the premium login setting.
     *
     * <p>Without its current state: that lives in the login plugin's own table, and the proxy
     * reaching into it to draw a tile would be one plugin reading another's storage for a
     * cosmetic. The command behind this button says which way it is set, and switches it.
     */
    private ItemStack premium() {
        return Items.of(Material.WRITABLE_BOOK)
                .name(this.style.text().of(this.messages.profile.premiumName))
                .lore(this.style.text().ofAll(this.messages.profile.premiumLore, Map.of()))
                .plain()
                .build();
    }

    private void send(Player player, MenuAction action) {
        this.channel.send(player, action);
        // Closed: what answers these is a message in chat or another plugin's menu, and a chat
        // line arrives behind an inventory that is no longer true.
        player.closeInventory();
    }
}
