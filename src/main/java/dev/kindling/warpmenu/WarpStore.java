package dev.kindling.warpmenu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

/** Loads and saves warps to warps.yml. Names are case-insensitive and kept sorted. */
public final class WarpStore {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    private final WarpMenuPlugin plugin;
    private final File file;
    private final Map<String, Warp> warps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public WarpStore(WarpMenuPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
    }

    public static boolean isValidName(String name) {
        return VALID_NAME.matcher(name).matches();
    }

    public void load() {
        warps.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String name : yaml.getKeys(false)) {
            ConfigurationSection s = yaml.getConfigurationSection(name);
            if (s == null) continue;
            Material icon = Material.matchMaterial(s.getString("icon", ""));
            warps.put(name, new Warp(name, s.getString("world", "world"),
                    s.getDouble("x"), s.getDouble("y"), s.getDouble("z"),
                    (float) s.getDouble("yaw"), (float) s.getDouble("pitch"),
                    icon != null ? icon : plugin.defaultIcon()));
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Warp w : warps.values()) {
            ConfigurationSection s = yaml.createSection(w.name());
            s.set("world", w.world());
            s.set("x", w.x());
            s.set("y", w.y());
            s.set("z", w.z());
            s.set("yaw", w.yaw());
            s.set("pitch", w.pitch());
            s.set("icon", w.icon().name());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save warps.yml", e);
        }
    }

    public Warp get(String name) {
        return warps.get(name);
    }

    /** @return true if a warp with this name already existed */
    public boolean put(Warp warp) {
        Warp old = warps.remove(warp.name());
        warps.put(warp.name(), warp);
        save();
        return old != null;
    }

    public boolean remove(String name) {
        boolean removed = warps.remove(name) != null;
        if (removed) save();
        return removed;
    }

    public List<Warp> all() {
        return new ArrayList<>(warps.values());
    }
}
