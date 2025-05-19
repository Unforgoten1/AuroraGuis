# Utilities API Reference

API reference for ItemBuilder, ColorUtils, and other utilities.

## ItemBuilder

Fluent builder for creating ItemStacks.

### Constructor

```java
public ItemBuilder(Material material)
public ItemBuilder(ItemStack item)
```

### Methods

```java
// Basic properties
ItemBuilder name(String name)              // Display name with color codes
ItemBuilder lore(String... lines)          // Add lore lines
ItemBuilder lore(List<String> lines)       // Add lore list
ItemBuilder amount(int amount)             // Stack size

// Enchantments
ItemBuilder enchantment(Enchantment, int level)
ItemBuilder enchant(Enchantment, int level)
ItemBuilder glow()                         // Glow without showing enchants

// Custom model data
ItemBuilder customModelData(int data)
ItemBuilder customModel(String modelId)    // From ModelRegistry

// Item flags
ItemBuilder flag(ItemFlag flag)
ItemBuilder clearFlags()

// Special
ItemBuilder owner(String owner)            // For player heads
ItemBuilder color(Color color)             // For leather armor

// Build
ItemStack build()
```

### Example

```java
ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD)
    .name("&cLegendary Sword")
    .lore(
        "&7Damage: &c+10",
        "&7Speed: &a+5",
        "",
        "&6&lLEGENDARY"
    )
    .enchantment(Enchantment.DAMAGE_ALL, 5)
    .enchantment(Enchantment.DURABILITY, 3)
    .flag(ItemFlag.HIDE_ENCHANTS)
    .glow()
    .build();
```

## ColorUtils

Cross-version color utility with hex color support.

### Methods

```java
// Color translation
public static String color(String text)
public static String color(String text, Object... args)
public static List<String> color(List<String> strings)

// Hex colors (1.16+)
// Supports: &#RRGGBB or #RRGGBB

// String utilities
public static String strip(String text)
public static String toNiceString(String text)
public static String format(String text, Object... args)

// Cache management
public static void clearCache()
public static boolean supportsHexColors()
```

### Examples

```java
// Legacy colors
String colored = ColorUtils.color("&aGreen &6Gold");

// Hex colors (1.16+)
String hex = ColorUtils.color("&#FF5733Custom Red");

// Format with placeholders
String formatted = ColorUtils.color("Hello {0}!", playerName);

// Color list
List<String> lore = ColorUtils.color(Arrays.asList(
    "&7Line 1",
    "&aLine 2"
));
```

## ItemStackPool

Reusable ItemStack cache for performance.

```java
ItemStack border = ItemStackPool.getOrCreate("border", () ->
    new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
        .name("&7")
        .build()
);

// Clear pool when done
ItemStackPool.clear();
```

## ModelRegistry

Manage custom item models from resource packs.

### Methods

```java
// Register models
public static void register(String id, ModelData data)
public static void registerAll(Map<String, ModelData> models)

// Retrieve models
public static ModelData get(String id)
public static Map<String, ModelData> getAll()
public static boolean has(String id)

// Unregister
public static void unregister(String id)
public static void clear()
```

### Example

```java
// Register
ModelRegistry.register("ruby", new ModelData(
    "ruby",
    Material.DIAMOND,
    1000001,
    "&cRuby",
    Arrays.asList("&7A precious gem")
));

// Use in ItemBuilder
ItemStack ruby = new ItemBuilder(Material.DIAMOND)
    .customModel("ruby")
    .build();
```

## ModelData

Data class for custom models.

### Constructor

```java
public ModelData(String modelId, Material baseMaterial, int customModelData)
public ModelData(String modelId, Material baseMaterial, int customModelData, String displayName)
public ModelData(String modelId, Material baseMaterial, int customModelData, String displayName, List<String> lore)
```

### Getters

```java
public String getModelId()
public Material getBaseMaterial()
public int getCustomModelData()
public String getDisplayName()
public List<String> getLore()
```

See guides:
- [Resource Pack Setup](../guides/resource-pack-setup.md)
- [Shop Tutorial](../guides/shop-tutorial.md)
