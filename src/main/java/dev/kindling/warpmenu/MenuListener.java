package dev.kindling.warpmenu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/** Makes the warp menu read-only and turns clicks into actions. */
public final class MenuListener implements Listener {

    private final WarpMenuPlugin plugin;

    public MenuListener(WarpMenuPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof WarpMenu menu)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            // Players may use their own inventory (e.g. to pick up an icon item),
            // but nothing may be shift-clicked or collected into or out of the menu.
            InventoryAction action = event.getAction();
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        switch (slot) {
            case WarpMenu.SLOT_PREVIOUS -> WarpMenu.open(plugin, player, menu.page() - 1);
            case WarpMenu.SLOT_NEXT -> WarpMenu.open(plugin, player, menu.page() + 1);
            case WarpMenu.SLOT_CLOSE -> player.closeInventory();
            default -> {
                Warp warp = menu.warpAt(slot);
                if (warp == null) return;
                boolean admin = player.hasPermission("warpmenu.admin");
                ItemStack cursor = event.getCursor();
                if (admin && !cursor.getType().isAir()) {
                    // Dropping an item onto a warp changes its icon; the item stays on the cursor.
                    plugin.store().put(warp.withIcon(cursor.getType()));
                    plugin.messages().send(player, "icon-changed", Messages.warp(warp.name()));
                    WarpMenu.open(plugin, player, menu.page());
                    return;
                }
                if (event.getClick() == ClickType.SHIFT_RIGHT && admin) {
                    plugin.store().remove(warp.name());
                    plugin.messages().send(player, "warp-deleted", Messages.warp(warp.name()));
                    WarpMenu.open(plugin, player, menu.page(), false);
                    return;
                }
                player.closeInventory();
                plugin.teleports().request(player, warp);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof WarpMenu) event.setCancelled(true);
    }
}
