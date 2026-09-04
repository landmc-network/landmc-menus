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
import pl.landmc.platform.paper.menu.PaginatedMenu;

/**
 * The friends list.
 *
 * <p>Whether a friend shows as online, and which server they are named as being on, is decided
 * entirely by the proxy before the payload is sent. That is not a detail: vanish lives there,
 * and a menu that worked it out for itself would be a way to find a hidden administrator.
 */
public final class FriendsMenu extends PaginatedMenu<MenuPayload.Friends.Friend> {

    private final MenusMessages.FriendsSection messages;
    private final MenuStyle style;
    private final MenuChannel channel;
    private final int pendingRequests;

    public FriendsMenu(
            MenuPayload.Friends payload,
            MenusMessages.FriendsSection messages,
            MenuStyle style,
            MenuChannel channel) {

        super(style.text().of(messages.title), Math.clamp(messages.rows, 1, 6), payload.friends());

        this.messages = Objects.requireNonNull(messages, "messages");
        this.style = Objects.requireNonNull(style, "style");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.pendingRequests = payload.pendingRequests();
    }

    @Override
    protected ItemStack render(MenuPayload.Friends.Friend friend) {
        Map<String, String> placeholders = Map.of(
                "{PLAYER}", this.style.text().escaped(friend.name()),
                "{SERVER}", this.style.text().escaped(friend.server()));

        // The head is drawn from the name rather than a texture URL, so it costs no lookup and
        // is right even for a player this server has never seen.
        return Items.head(Bukkit.getOfflinePlayer(friend.name()))
                .name(this.style.text().of(
                        friend.online() ? this.messages.friendOnline : this.messages.friendOffline,
                        placeholders))
                .lore(this.style.text().ofAll(
                        friend.online()
                                ? this.messages.friendOnlineLore
                                : this.messages.friendOfflineLore,
                        placeholders))
                .plain()
                .build();
    }

    @Override
    protected void onSelect(Player player, MenuPayload.Friends.Friend friend, ClickType type) {
        // Shift-right removes, which is deliberately awkward: a friend removed by a stray click
        // is a friend request the player has to send again and explain.
        if (type == ClickType.SHIFT_RIGHT) {
            this.channel.send(player, MenuAction.of(MenuKind.FRIENDS, "remove", friend.name()));
            player.closeInventory();
            return;
        }

        if (friend.online()) {
            this.channel.send(player, MenuAction.of(MenuKind.FRIENDS, "join", friend.name()));
            player.closeInventory();
        }
    }

    @Override
    protected void decorate() {
        int bottom = this.size() - 9;

        if (this.isEmpty()) {
            this.item(
                    this.size() / 2,
                    Items.of(Material.BOOK)
                            .name(this.style.text().of(this.messages.noFriends))
                            .lore(this.style.text().ofAll(this.messages.noFriendsLore, Map.of()))
                            .plain()
                            .build());
        }

        if (this.pendingRequests > 0) {
            Map<String, String> placeholders =
                    Map.of("{COUNT}", Integer.toString(this.pendingRequests));

            this.button(
                    bottom + 8,
                    Items.of(Material.WRITABLE_BOOK)
                            .name(this.style.text().of(this.messages.pendingRequests, placeholders))
                            .lore(this.style.text().ofAll(
                                    this.messages.pendingRequestsLore, placeholders))
                            .amount(this.pendingRequests)
                            .glowing()
                            .plain()
                            .build(),
                    (player, type) -> {
                        this.channel.send(player, MenuAction.of(MenuKind.FRIENDS, "requests"));
                        player.closeInventory();
                    });
        }

        this.fill(this.style.filler());
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
