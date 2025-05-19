package dev.aurora.Integration;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for PlaceholderAPI integration
 * Provides methods to parse placeholders in GUI items
 * Works with or without PlaceholderAPI installed
 */
public class PlaceholderHelper {
    private static boolean papiAvailable = false;
    private static Object papiInstance = null;

    static {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            papiAvailable = true;
        } catch (ClassNotFoundException e) {
            papiAvailable = false;
        }
    }

    /**
     * Check if PlaceholderAPI is available
     */
    public static boolean isAvailable() {
        return papiAvailable;
    }

    /**
     * Parse placeholders in a string
     * Falls back to no parsing if PAPI not available
     */
    public static String parse(Player player, String text) {
        if (text == null) return null;

        if (papiAvailable && player != null) {
            try {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                Object result = papiClass.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class)
                    .invoke(null, player, text);
                return (String) result;
            } catch (Exception e) {
                return text;
            }
        }

        return text;
    }

    /**
     * Parse placeholders in a list of strings
     */
    public static List<String> parse(Player player, List<String> texts) {
        if (texts == null) return null;

        List<String> parsed = new ArrayList<>();
        for (String text : texts) {
            parsed.add(parse(player, text));
        }
        return parsed;
    }

    /**
     * Parse placeholders in an ItemStack's display name and lore
     * Returns a new ItemStack with parsed text
     */
    public static ItemStack parse(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;

        ItemStack parsed = item.clone();
        ItemMeta meta = parsed.getItemMeta();

        if (meta.hasDisplayName()) {
            meta.setDisplayName(parse(player, meta.getDisplayName()));
        }

        if (meta.hasLore()) {
            meta.setLore(parse(player, meta.getLore()));
        }

        parsed.setItemMeta(meta);
        return parsed;
    }

    /**
     * Parse placeholders in a list of items
     */
    public static List<ItemStack> parseItems(Player player, List<ItemStack> items) {
        if (items == null) return null;

        List<ItemStack> parsed = new ArrayList<>();
        for (ItemStack item : items) {
            parsed.add(parse(player, item));
        }
        return parsed;
    }

    /**
     * Register a custom placeholder expansion
     * Only works if PAPI is available
     */
    public static boolean registerExpansion(Object expansion) {
        if (!papiAvailable) return false;

        try {
            Class<?> expansionClass = Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
            if (expansionClass.isInstance(expansion)) {
                Object result = expansionClass.getMethod("register").invoke(expansion);
                return (Boolean) result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
