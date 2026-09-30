package dev.kindling.warpmenu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class WarpMenuPlugin extends JavaPlugin {

    private Messages messages;
    private WarpStore store;
    private TeleportManager teleports;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new Messages(this);
        store = new WarpStore(this);
        store.load();
        teleports = new TeleportManager(this);

        getServer().getPluginManager().registerEvents(teleports, this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        WarpCommands commands = new WarpCommands(this);
        for (String name : List.of("warp", "setwarp", "delwarp", "warpmenu")) {
            Objects.requireNonNull(getCommand(name), name).setExecutor(commands);
        }
        getLogger().info("Loaded " + store.all().size() + " warps.");
    }

    @Override
    public void onDisable() {
        if (teleports != null) teleports.cancelAll();
    }

    public void reload() {
        reloadConfig();
        store.load();
    }

    public boolean canUse(Player player, Warp warp) {
        return !getConfig().getBoolean("per-warp-permissions")
                || player.hasPermission(warp.permission())
                || player.hasPermission("warpmenu.warp.*");
    }

    public Material defaultIcon() {
        Material icon = Material.matchMaterial(getConfig().getString("default-icon", ""));
        return icon != null && icon.isItem() ? icon : Material.ENDER_PEARL;
    }

    public Messages messages() {
        return messages;
    }

    public WarpStore store() {
        return store;
    }

    public TeleportManager teleports() {
        return teleports;
    }
}
