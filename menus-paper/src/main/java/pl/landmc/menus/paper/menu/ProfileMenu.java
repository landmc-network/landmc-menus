package pl.landmc.menus.paper.menu;

import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
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
import pl.landmc.platform.paper.menu.Menu;

/**
 * A player's own profile: who the network thinks they are, and the way into the menus that hold
 * the detail.
 *
 * <p>A summary, not a place things happen. Every tile either states a fact or opens the menu
 * that owns it - the friends list draws itself, the premium switch belongs to the login plugin -
 * so this menu holds no logic of its own and cannot disagree with what those show.
 *
 * <p>The original had tiles for minigame statistics and two currencies. Neither exists on this
 * network, and a tile that says "coming soon" is a tile that says the menu was not finished.
 */
public final class ProfileMenu extends Menu {

    private final MenuPayload.Profile payload;
    private final MenusMessages.ProfileSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;

    public ProfileMenu(
            MenuPayload.Profile payload,
            MenusMessages.ProfileSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(
                style.text().of(
                        messages.title,
                        Map.of("{PLAYER}", style.text().escaped(payload.playerName()))),
                Math.clamp(messages.rows, 1, 6));

        this.payload = Objects.requireNonNull(payload, "payload");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    protected void redraw() {
        int middle = this.size() / 2;

        this.item(middle - 2, this.head());
        // Left open: the friends menu replaces this one, and closing first would show the
        // world for as long as the message takes to reach the proxy and come back.
        this.button(middle, this.friends(), (player, type) ->
                this.channel.send(player, MenuAction.of(MenuKind.PROFILE, "friends")));
        this.button(middle + 2, this.premium(), (player, type) ->
                this.send(player, MenuAction.of(MenuKind.PROFILE, "premium")));

        this.fill(this.style.filler());
    }

    private ItemStack head() {
        String rank = this.payload.rank().isBlank()
                ? this.messages.noRank
                : this.payload.rank();

        Map<String, String> placeholders = Map.of(
                "{PLAYER}", this.style.text().escaped(this.payload.playerName()),
                // A rank name comes from LuckPerms, which is to say from something an
                // administrator typed, so it is escaped like any other value.
                "{RANK}", this.style.text().escaped(rank),
                "{SERVER}", this.style.text().escaped(this.payload.currentServer()));

        return Items.head(Bukkit.getOfflinePlayer(this.payload.playerName()))
                .name(this.style.text().of(this.messages.playerName, placeholders))
                .lore(this.style.text().ofAll(this.messages.playerLore, placeholders))
                .plain()
                .build();
    }

    private ItemStack friends() {
        Map<String, String> placeholders = Map.of(
                "{FRIENDS}", Integer.toString(this.payload.friends()),
                "{REQUESTS}", Integer.toString(this.payload.pendingRequests()));

        Items.Builder item = Items.of(Material.PLAYER_HEAD)
                .name(this.style.text().of(this.messages.friendsName, placeholders))
                .lore(this.style.text().ofAll(this.messages.friendsLore, placeholders))
                .plain();

        if (this.payload.pendingRequests() > 0) {
            // Something is waiting for an answer, and a number on a tile is easy to walk past.
            item.glowing();
        }

        return item.build();
    }

    /**
     * The way into the premium login setting.
     *
     * <p>Without its current state: that lives in the login plugin's own table, and the proxy
     * reaching into it to draw a tile would be one plugin reading another's storage for a
     * cosmetic. The command behind this button says which way it is set, and switches it.
     */
    private ItemStack premium() {
        return Items.of(Material.LIME_DYE)
                .name(this.style.text().of(this.messages.premiumName))
                .lore(this.style.text().ofAll(this.messages.premiumLore, Map.of()))
                .plain()
                .build();
    }

    private void send(Player player, MenuAction action) {
        this.channel.send(player, action);
        // Closed rather than left open: what happens next is another menu or a chat message
        // from the proxy, and both arrive behind an inventory that is no longer true.
        player.closeInventory();
    }
}
