package dev.aurora.Compatibility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cross-version Material compatibility wrapper
 * Handles material differences between 1.8-1.12 and 1.13+
 * Based on XSeries by CryptoMorin
 */
public enum XMaterial {
    // Common materials with legacy support
    STONE(0, "STONE"),
    GRASS_BLOCK(2, "GRASS", "GRASS_BLOCK"),
    DIRT(3, "DIRT"),
    COBBLESTONE(4, "COBBLESTONE"),
    OAK_PLANKS(5, "WOOD", "OAK_PLANKS"),
    BEDROCK(7, "BEDROCK"),
    SAND(12, "SAND"),
    GRAVEL(13, "GRAVEL"),
    GLASS(20, "GLASS"),

    // Wool colors
    WHITE_WOOL(35, "WOOL", "WHITE_WOOL"),
    ORANGE_WOOL(35, 1, "WOOL:1", "ORANGE_WOOL"),
    MAGENTA_WOOL(35, 2, "WOOL:2", "MAGENTA_WOOL"),
    LIGHT_BLUE_WOOL(35, 3, "WOOL:3", "LIGHT_BLUE_WOOL"),
    YELLOW_WOOL(35, 4, "WOOL:4", "YELLOW_WOOL"),
    LIME_WOOL(35, 5, "WOOL:5", "LIME_WOOL"),
    PINK_WOOL(35, 6, "WOOL:6", "PINK_WOOL"),
    GRAY_WOOL(35, 7, "WOOL:7", "GRAY_WOOL"),
    LIGHT_GRAY_WOOL(35, 8, "WOOL:8", "LIGHT_GRAY_WOOL"),
    CYAN_WOOL(35, 9, "WOOL:9", "CYAN_WOOL"),
    PURPLE_WOOL(35, 10, "WOOL:10", "PURPLE_WOOL"),
    BLUE_WOOL(35, 11, "WOOL:11", "BLUE_WOOL"),
    BROWN_WOOL(35, 12, "WOOL:12", "BROWN_WOOL"),
    GREEN_WOOL(35, 13, "WOOL:13", "GREEN_WOOL"),
    RED_WOOL(35, 14, "WOOL:14", "RED_WOOL"),
    BLACK_WOOL(35, 15, "WOOL:15", "BLACK_WOOL"),

    // Glass panes
    GLASS_PANE(102, "THIN_GLASS", "GLASS_PANE"),
    WHITE_STAINED_GLASS_PANE(160, "STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE"),
    ORANGE_STAINED_GLASS_PANE(160, 1, "STAINED_GLASS_PANE:1", "ORANGE_STAINED_GLASS_PANE"),
    MAGENTA_STAINED_GLASS_PANE(160, 2, "STAINED_GLASS_PANE:2", "MAGENTA_STAINED_GLASS_PANE"),
    LIGHT_BLUE_STAINED_GLASS_PANE(160, 3, "STAINED_GLASS_PANE:3", "LIGHT_BLUE_STAINED_GLASS_PANE"),
    YELLOW_STAINED_GLASS_PANE(160, 4, "STAINED_GLASS_PANE:4", "YELLOW_STAINED_GLASS_PANE"),
    LIME_STAINED_GLASS_PANE(160, 5, "STAINED_GLASS_PANE:5", "LIME_STAINED_GLASS_PANE"),
    PINK_STAINED_GLASS_PANE(160, 6, "STAINED_GLASS_PANE:6", "PINK_STAINED_GLASS_PANE"),
    GRAY_STAINED_GLASS_PANE(160, 7, "STAINED_GLASS_PANE:7", "GRAY_STAINED_GLASS_PANE"),
    LIGHT_GRAY_STAINED_GLASS_PANE(160, 8, "STAINED_GLASS_PANE:8", "LIGHT_GRAY_STAINED_GLASS_PANE"),
    CYAN_STAINED_GLASS_PANE(160, 9, "STAINED_GLASS_PANE:9", "CYAN_STAINED_GLASS_PANE"),
    PURPLE_STAINED_GLASS_PANE(160, 10, "STAINED_GLASS_PANE:10", "PURPLE_STAINED_GLASS_PANE"),
    BLUE_STAINED_GLASS_PANE(160, 11, "STAINED_GLASS_PANE:11", "BLUE_STAINED_GLASS_PANE"),
    BROWN_STAINED_GLASS_PANE(160, 12, "STAINED_GLASS_PANE:12", "BROWN_STAINED_GLASS_PANE"),
    GREEN_STAINED_GLASS_PANE(160, 13, "STAINED_GLASS_PANE:13", "GREEN_STAINED_GLASS_PANE"),
    RED_STAINED_GLASS_PANE(160, 14, "STAINED_GLASS_PANE:14", "RED_STAINED_GLASS_PANE"),
    BLACK_STAINED_GLASS_PANE(160, 15, "STAINED_GLASS_PANE:15", "BLACK_STAINED_GLASS_PANE"),

    // Common items
    DIAMOND(264, "DIAMOND"),
    IRON_INGOT(265, "IRON_INGOT"),
    GOLD_INGOT(266, "GOLD_INGOT"),
    EMERALD(388, "EMERALD"),
    COAL(263, "COAL"),

    // Tools
    DIAMOND_SWORD(276, "DIAMOND_SWORD"),
    DIAMOND_PICKAXE(278, "DIAMOND_PICKAXE"),
    DIAMOND_AXE(279, "DIAMOND_AXE"),
    DIAMOND_SHOVEL(277, "DIAMOND_SHOVEL", "DIAMOND_SPADE"),

    // Special items
    PLAYER_HEAD(397, 3, "SKULL_ITEM:3", "PLAYER_HEAD"),
    COMPASS(345, "COMPASS"),
    CLOCK(347, "WATCH", "CLOCK"),
    ARROW(262, "ARROW"),
    BARRIER(166, "BARRIER"),
    PAPER(339, "PAPER"),
    BOOK(340, "BOOK"),
    CHEST(54, "CHEST"),
    ENDER_CHEST(130, "ENDER_CHEST"),

    // Redstone
    REDSTONE(331, "REDSTONE"),
    REDSTONE_BLOCK(152, "REDSTONE_BLOCK"),
    LEVER(69, "LEVER"),

    // Rails
    RAIL(66, "RAILS", "RAIL"),
    POWERED_RAIL(27, "POWERED_RAIL"),

    // Misc
    TNT(46, "TNT"),
    SPONGE(19, "SPONGE"),
    COMMAND_BLOCK(137, "COMMAND", "COMMAND_BLOCK"),
    ENCHANTING_TABLE(116, "ENCHANTMENT_TABLE", "ENCHANTING_TABLE"),
    ANVIL(145, "ANVIL"),

    // 1.13+ only materials (with fallbacks)
    STRUCTURE_VOID(-1, "BARRIER", "STRUCTURE_VOID"),
    KNOWLEDGE_BOOK(-1, "BOOK", "KNOWLEDGE_BOOK");

    private final int legacyId;
    private final byte legacyData;
    private final String[] names;
    private Material material;

    private static final Map<String, XMaterial> NAME_MAP = new HashMap<>();
    private static final boolean IS_LEGACY = !ServerVersion.getInstance().isFlattening();

    static {
        for (XMaterial mat : values()) {
            for (String name : mat.names) {
                NAME_MAP.put(name.toUpperCase(), mat);
            }
        }
    }

    XMaterial(int legacyId, String... names) {
        this(legacyId, (byte) 0, names);
    }

    XMaterial(int legacyId, int legacyData, String... names) {
        this.legacyId = legacyId;
        this.legacyData = (byte) legacyData;
        this.names = names;
    }

    /**
     * Get the Bukkit Material for this XMaterial
     */
    public Material parseMaterial() {
        if (material != null) return material;

        // Try each name until we find one that works
        for (String name : names) {
            // Remove data value if present
            String materialName = name.split(":")[0];

            try {
                Material mat = Material.getMaterial(materialName);
                if (mat != null) {
                    material = mat;
                    return material;
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback to first name
        try {
            material = Material.getMaterial(names[0].split(":")[0]);
        } catch (Exception e) {
            material = Material.STONE; // Ultimate fallback
        }

        return material;
    }

    /**
     * Create an ItemStack from this XMaterial
     */
    public ItemStack parseItem() {
        return parseItem(1);
    }

    /**
     * Create an ItemStack with specified amount
     */
    public ItemStack parseItem(int amount) {
        Material mat = parseMaterial();

        if (IS_LEGACY && legacyData != 0) {
            try {
                // Use deprecated constructor for legacy versions
                return new ItemStack(mat, amount, legacyData);
            } catch (Exception e) {
                return new ItemStack(mat, amount);
            }
        }

        return new ItemStack(mat, amount);
    }

    /**
     * Get legacy data value (for 1.8-1.12)
     */
    public byte getData() {
        return legacyData;
    }

    /**
     * Check if this material exists on current server version
     */
    public boolean isSupported() {
        return parseMaterial() != null && parseMaterial() != Material.STONE;
    }

    /**
     * Match XMaterial from string name
     */
    public static Optional<XMaterial> matchXMaterial(String name) {
        if (name == null) return Optional.empty();

        XMaterial mat = NAME_MAP.get(name.toUpperCase());
        return Optional.ofNullable(mat);
    }

    /**
     * Match XMaterial from Bukkit Material
     */
    public static Optional<XMaterial> matchXMaterial(Material material) {
        if (material == null) return Optional.empty();

        for (XMaterial xmat : values()) {
            if (xmat.parseMaterial() == material) {
                return Optional.of(xmat);
            }
        }

        return Optional.empty();
    }

    /**
     * Match XMaterial from ItemStack
     */
    public static Optional<XMaterial> matchXMaterial(ItemStack item) {
        if (item == null) return Optional.empty();
        return matchXMaterial(item.getType());
    }
}
