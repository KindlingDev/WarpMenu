package dev.kindling.warpmenu;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Handles the teleport delay (cancelled by moving) and the cooldown between warps. */
public final class TeleportManager implements Listener {

    private final WarpMenuPlugin plugin;
    private record Pending(BukkitTask task, String warp) {}

    private final Map<UUID, Pending> pending = new HashMap<>();
    private final Map<UUID, Long> lastWarp = new HashMap<>();

    public TeleportManager(WarpMenuPlugin plugin) {
        this.plugin = plugin;
    }

    public void request(Player player, Warp warp) {
        Messages msg = plugin.messages();
        if (warp.toLocation() == null) {
            msg.send(player, "world-missing", Messages.warp(warp.name()));
            return;
        }

        long cooldownMs = plugin.getConfig().getLong("cooldown") * 1000L;
        Long last = lastWarp.get(player.getUniqueId());
        if (cooldownMs > 0 && last != null && !player.hasPermission("warpmenu.bypass.cooldown")) {
            long left = last + cooldownMs - System.currentTimeMillis();
            if (left > 0) {
                msg.send(player, "cooldown", Messages.value("seconds", (left + 999) / 1000));
                return;
            }
        }

        // Repeating the same warp keeps the running countdown; a different warp replaces it.
        Pending current = pending.get(player.getUniqueId());
        if (current != null && current.warp().equalsIgnoreCase(warp.name())) {
            msg.send(player, "already-teleporting", Messages.warp(warp.name()));
            return;
        }
        cancel(player);
        int delay = plugin.getConfig().getInt("teleport-delay");
        if (delay <= 0 || player.hasPermission("warpmenu.bypass.delay")) {
            teleport(player, warp);
            return;
        }

        msg.send(player, "teleport-pending", Messages.warp(warp.name()), Messages.value("seconds", delay));
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pending.remove(player.getUniqueId());
            if (player.isOnline()) teleport(player, warp);
        }, delay * 20L);
        pending.put(player.getUniqueId(), new Pending(task, warp.name()));
    }

    private void teleport(Player player, Warp warp) {
        // Look the location up again: the world may have unloaded during the delay.
        Location loc = warp.toLocation();
        if (loc == null) {
            plugin.messages().send(player, "world-missing", Messages.warp(warp.name()));
            return;
        }
        player.teleportAsync(loc).thenAccept(success -> {
            if (!success) return;
            lastWarp.put(player.getUniqueId(), System.currentTimeMillis());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
            plugin.messages().send(player, "teleported", Messages.warp(warp.name()));
        });
    }

    /** @return true if a pending teleport was cancelled */
    public boolean cancel(Player player) {
        Pending p = pending.remove(player.getUniqueId());
        if (p == null) return false;
        p.task().cancel();
        return true;
    }

    public void cancelAll() {
        pending.values().forEach(p -> p.task().cancel());
        pending.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock() && cancel(event.getPlayer())) {
            plugin.messages().send(event.getPlayer(), "teleport-cancelled");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer());
    }
}
