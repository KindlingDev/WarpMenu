package dev.kindling.warpmenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

/** Reads MiniMessage strings from config.yml. */
public final class Messages {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final WarpMenuPlugin plugin;

    public Messages(WarpMenuPlugin plugin) {
        this.plugin = plugin;
    }

    public static TagResolver warp(String name) {
        return Placeholder.unparsed("warp", name);
    }

    public static TagResolver value(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }

    /** Sends messages.&lt;key&gt; with the prefix. An empty message in the config is not sent. */
    public void send(CommandSender to, String key, TagResolver... placeholders) {
        String raw = text("messages." + key, "");
        if (raw.isEmpty()) return;
        to.sendMessage(MM.deserialize(text("messages.prefix", "") + raw, placeholders));
    }

    /**
     * Reads a string, falling back to the jar's built-in config.yml. getString(path, fallback)
     * skips those defaults, which would hide messages added in updates from older config files.
     */
    private String text(String path, String fallback) {
        String value = plugin.getConfig().getString(path);
        return value != null ? value : fallback;
    }

    /** Parses a config string for use in item names and lore (no default italics). */
    public Component item(String raw, TagResolver... placeholders) {
        return MM.deserialize(raw, placeholders).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public Component item(String path, String fallback, TagResolver... placeholders) {
        return item(text(path, fallback), placeholders);
    }

    public boolean has(String path) {
        return !text(path, "").isEmpty();
    }

    public Component title(TagResolver... placeholders) {
        return MM.deserialize(text("menu.title", "Warps"), placeholders);
    }
}
