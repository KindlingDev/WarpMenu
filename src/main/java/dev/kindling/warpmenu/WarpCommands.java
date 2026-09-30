package dev.kindling.warpmenu;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/** /warp, /setwarp, /delwarp and /warpmenu. */
public final class WarpCommands implements TabExecutor {

    private final WarpMenuPlugin plugin;

    public WarpCommands(WarpMenuPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "warp" -> warp(sender, args);
            case "setwarp" -> setWarp(sender, args);
            case "delwarp" -> delWarp(sender, args);
            case "warpmenu" -> admin(sender, args);
        }
        return true;
    }

    private void warp(CommandSender sender, String[] args) {
        Messages msg = plugin.messages();
        if (!(sender instanceof Player player)) {
            msg.send(sender, "players-only");
            return;
        }
        if (!player.hasPermission("warpmenu.use")) {
            msg.send(player, "no-permission");
            return;
        }
        if (args.length == 0) {
            WarpMenu.open(plugin, player, 0);
            return;
        }
        Warp warp = plugin.store().get(args[0]);
        // Hidden warps look the same as missing ones, so names aren't leaked.
        if (warp == null || !plugin.canUse(player, warp)) {
            msg.send(player, "unknown-warp", Messages.warp(args[0]));
            return;
        }
        plugin.teleports().request(player, warp);
    }

    private void setWarp(CommandSender sender, String[] args) {
        Messages msg = plugin.messages();
        if (!(sender instanceof Player player)) {
            msg.send(sender, "players-only");
            return;
        }
        if (!player.hasPermission("warpmenu.admin")) {
            msg.send(player, "no-permission");
            return;
        }
        if (args.length < 1 || args.length > 2) {
            msg.send(player, "usage-setwarp");
            return;
        }
        if (!WarpStore.isValidName(args[0])) {
            msg.send(player, "invalid-name");
            return;
        }
        Material typed = null;
        if (args.length == 2) {
            typed = Material.matchMaterial(args[1]);
            if (typed == null || !typed.isItem() || typed.isAir()) {
                msg.send(player, "unknown-icon", Messages.value("icon", args[1]));
                return;
            }
        }
        // Icon priority: typed icon > held item > existing icon (when moving) > default.
        Warp existing = plugin.store().get(args[0]);
        String name = existing != null ? existing.name() : args[0];
        Material held = player.getInventory().getItemInMainHand().getType();
        Material icon = typed != null ? typed
                : !held.isAir() ? held
                : existing != null ? existing.icon()
                : plugin.defaultIcon();
        boolean moved = plugin.store().put(Warp.at(name, player.getLocation(), icon));
        msg.send(player, moved ? "warp-moved" : "warp-set", Messages.warp(name));
    }

    private void delWarp(CommandSender sender, String[] args) {
        Messages msg = plugin.messages();
        if (!sender.hasPermission("warpmenu.admin")) {
            msg.send(sender, "no-permission");
            return;
        }
        if (args.length != 1) {
            msg.send(sender, "usage-delwarp");
            return;
        }
        Warp warp = plugin.store().get(args[0]);
        if (warp == null) {
            msg.send(sender, "unknown-warp", Messages.warp(args[0]));
            return;
        }
        plugin.store().remove(warp.name());
        msg.send(sender, "warp-deleted", Messages.warp(warp.name()));
    }

    private void admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("warpmenu.admin")) {
            plugin.messages().send(sender, "no-permission");
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            plugin.messages().send(sender, "reloaded");
        } else {
            plugin.messages().send(sender, "usage-admin");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2 && command.getName().equals("setwarp") && sender.hasPermission("warpmenu.admin")) {
            String prefix = args[1].toLowerCase();
            return Arrays.stream(Material.values())
                    .filter(m -> m.isItem() && !m.isAir() && !m.isLegacy())
                    .map(m -> m.getKey().getKey())
                    .filter(n -> n.startsWith(prefix))
                    .toList();
        }
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase();
        return switch (command.getName()) {
            case "warp" -> plugin.store().all().stream()
                    .filter(w -> !(sender instanceof Player p) || plugin.canUse(p, w))
                    .map(Warp::name)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .toList();
            case "setwarp", "delwarp" -> sender.hasPermission("warpmenu.admin")
                    ? plugin.store().all().stream().map(Warp::name)
                        .filter(n -> n.toLowerCase().startsWith(prefix)).toList()
                    : List.of();
            case "warpmenu" -> "reload".startsWith(prefix) && sender.hasPermission("warpmenu.admin")
                    ? List.of("reload") : List.of();
            default -> List.of();
        };
    }
}
