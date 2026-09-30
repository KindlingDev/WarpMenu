package dev.kindling.warpmenu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/** A named location. The world is stored by name so warps survive worlds being unloaded. */
public record Warp(String name, String world, double x, double y, double z, float yaw, float pitch, Material icon) {

    public static Warp at(String name, Location loc, Material icon) {
        return new Warp(name, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(), icon);
    }

    public Warp withIcon(Material newIcon) {
        return new Warp(name, world, x, y, z, yaw, pitch, newIcon);
    }

    /** @return the location, or null if the world is not loaded */
    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        return w == null ? null : new Location(w, x, y, z, yaw, pitch);
    }

    public String permission() {
        return "warpmenu.warp." + name.toLowerCase();
    }
}
