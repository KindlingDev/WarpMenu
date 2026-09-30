package dev.kindling.warpmenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * One page of the warp menu. The top five rows hold warps; the bottom row holds navigation.
 * Implementing InventoryHolder lets the click listener recognise this menu reliably.
 */
public final class WarpMenu implements InventoryHolder {

    static final int PAGE_SIZE = 45;
    static final int SLOT_PREVIOUS = 45;
    static final int SLOT_CLOSE = 49;
    static final int SLOT_NEXT = 53;

    private final List<Warp> warps;
    private final int page;
    private final int pages;
    private final Inventory inventory;

    private WarpMenu(WarpMenuPlugin plugin, Player viewer, List<Warp> warps, int page) {
        this.warps = warps;
        this.pages = Math.max(1, (warps.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        this.page = Math.clamp(page, 0, pages - 1);

        Messages msg = plugin.messages();
        this.inventory = Bukkit.createInventory(this, 54, msg.title(
                Messages.value("page", this.page + 1), Messages.value("pages", pages)));

        boolean admin = viewer.hasPermission("warpmenu.admin");
        int start = this.page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && start + i < warps.size(); i++) {
            inventory.setItem(i, warpItem(plugin, warps.get(start + i), admin));
        }

        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of());
        for (int slot = PAGE_SIZE; slot < 54; slot++) inventory.setItem(slot, filler);
        if (this.page > 0) {
            inventory.setItem(SLOT_PREVIOUS, item(Material.ARROW, msg.item("menu.previous-page", "Previous page"), List.of()));
        }
        if (this.page < pages - 1) {
            inventory.setItem(SLOT_NEXT, item(Material.ARROW, msg.item("menu.next-page", "Next page"), List.of()));
        }
        inventory.setItem(SLOT_CLOSE, item(Material.BARRIER, msg.item("menu.close", "Close"), List.of()));
    }

    /** Opens the menu showing only the warps the player may use. */
    public static void open(WarpMenuPlugin plugin, Player player, int page) {
        open(plugin, player, page, true);
    }

    /** @param explainIfEmpty send "no warps yet" when there is nothing to show (off when refreshing after a delete) */
    public static void open(WarpMenuPlugin plugin, Player player, int page, boolean explainIfEmpty) {
        List<Warp> visible = plugin.store().all().stream().filter(w -> plugin.canUse(player, w)).toList();
        if (visible.isEmpty()) {
            if (explainIfEmpty) plugin.messages().send(player, "no-warps");
            player.closeInventory();
            return;
        }
        player.openInventory(new WarpMenu(plugin, player, visible, page).getInventory());
    }

    private static ItemStack warpItem(WarpMenuPlugin plugin, Warp warp, boolean admin) {
        Messages msg = plugin.messages();
        TagResolver placeholders = TagResolver.resolver(
                Messages.warp(warp.name()),
                Messages.value("world", warp.world()),
                Messages.value("x", Math.round(warp.x())),
                Messages.value("y", Math.round(warp.y())),
                Messages.value("z", Math.round(warp.z())));

        List<Component> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("menu.warp-lore")) {
            lore.add(msg.item(line, placeholders));
        }
        if (admin) {
            lore.add(msg.item("menu.admin-lore", "Shift + right-click to delete"));
            if (msg.has("menu.admin-icon-lore")) lore.add(msg.item("menu.admin-icon-lore", ""));
        }
        return item(warp.icon(), msg.item("menu.warp-name", "<warp>", placeholders), lore);
    }

    private static ItemStack item(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material.isItem() ? material : Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    /** @return the warp in this slot, or null for navigation and empty slots */
    public Warp warpAt(int slot) {
        int index = page * PAGE_SIZE + slot;
        return slot < PAGE_SIZE && index < warps.size() ? warps.get(index) : null;
    }

    public int page() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
