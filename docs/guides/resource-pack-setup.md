# Resource Pack Setup

Learn how to use custom models and textures in your GUIs with the ModelRegistry system.

## Overview

AuroraGuis includes a ModelRegistry system for managing custom item models from resource packs. This allows you to:

- Use custom item textures in GUIs
- Register and retrieve models by ID
- Apply CustomModelData values automatically
- Cache models for performance

## Quick Start

### Step 1: Register Custom Models

```java
public class YourPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Register custom models
        ModelRegistry.register("ruby", new ModelData(
            "ruby",                    // Model ID
            Material.DIAMOND,         // Base material
            1000001,                  // CustomModelData value
            "&cRuby",                 // Display name (optional)
            Arrays.asList("&7A precious red gem") // Lore (optional)
        ));

        ModelRegistry.register("gold_coin", new ModelData(
            "gold_coin",
            Material.GOLD_NUGGET,
            1000002,
            "&6Gold Coin"
        ));

        ModelRegistry.register("magic_wand", new ModelData(
            "magic_wand",
            Material.STICK,
            1000003,
            "&5Magic Wand",
            Arrays.asList("&7A powerful magical artifact")
        ));
    }
}
```

### Step 2: Use in ItemBuilder

```java
// Simple usage
ItemStack ruby = new ItemBuilder(Material.DIAMOND)
    .customModel("ruby")
    .build();

// The ItemBuilder automatically applies:
// - Base material (DIAMOND)
// - CustomModelData (1000001)
// - Display name (&cRuby)
// - Lore (&7A precious red gem)
```

### Step 3: Use in GUIs

```java
AuroraGui shop = new AuroraGui("custom-shop")
    .title("&6Custom Item Shop")
    .rows(3);

// Use custom model by ID
shop.addItem(11, new ItemBuilder(Material.DIAMOND)
    .customModel("ruby")
    .lore("&7Price: &6$500")
    .build(), event -> {
        // Handle purchase
    });

shop.addItem(13, new ItemBuilder(Material.GOLD_NUGGET)
    .customModel("gold_coin")
    .amount(64)
    .lore("&7Price: &6$100")
    .build(), event -> {
        // Handle purchase
    });

shop.register(manager);
shop.open(player);
```

## ModelData API

### Constructor

```java
ModelData model = new ModelData(
    String modelId,           // Unique identifier
    Material baseMaterial,    // Base item material
    int customModelData       // CustomModelData value
);

// With display name
ModelData model = new ModelData(
    String modelId,
    Material baseMaterial,
    int customModelData,
    String displayName
);

// With display name and lore
ModelData model = new ModelData(
    String modelId,
    Material baseMaterial,
    int customModelData,
    String displayName,
    List<String> lore
);
```

### Getters

```java
ModelData model = ModelRegistry.get("ruby");

String id = model.getModelId();
Material material = model.getBaseMaterial();
int cmd = model.getCustomModelData();
String name = model.getDisplayName();
List<String> lore = model.getLore();
```

## ModelRegistry API

### Register Models

```java
// Register single model
ModelRegistry.register("ruby", modelData);

// Register multiple models
Map<String, ModelData> models = new HashMap<>();
models.put("ruby", rubyModel);
models.put("sapphire", sapphireModel);
ModelRegistry.registerAll(models);
```

### Retrieve Models

```java
// Get by ID
ModelData model = ModelRegistry.get("ruby");

// Get all models
Map<String, ModelData> allModels = ModelRegistry.getAll();

// Check if exists
boolean exists = ModelRegistry.has("ruby");
```

### Unregister Models

```java
// Unregister single model
ModelRegistry.unregister("ruby");

// Clear all models
ModelRegistry.clear();
```

## Resource Pack Structure

### Directory Layout

```
resourcepack/
├── pack.mcmeta
├── pack.png
└── assets/
    └── minecraft/
        ├── models/
        │   └── item/
        │       ├── diamond.json      (override)
        │       ├── gold_nugget.json  (override)
        │       ├── stick.json        (override)
        │       ├── ruby.json         (custom model)
        │       ├── gold_coin.json    (custom model)
        │       └── magic_wand.json   (custom model)
        └── textures/
            └── item/
                ├── ruby.png
                ├── gold_coin.png
                └── magic_wand.png
```

### pack.mcmeta

```json
{
  "pack": {
    "pack_format": 8,
    "description": "Custom GUI Items"
  }
}
```

### Item Model Override (diamond.json)

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/diamond"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1000001
      },
      "model": "item/ruby"
    }
  ]
}
```

### Custom Model (ruby.json)

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/ruby"
  }
}
```

## Complete Example

### Plugin Setup

```java
public class CustomItemPlugin extends JavaPlugin {
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        guiManager = new GuiManager(this);

        // Register all custom models
        registerModels();

        // Register commands
        getCommand("customshop").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                openCustomShop((Player) sender);
            }
            return true;
        });
    }

    private void registerModels() {
        // Gems
        ModelRegistry.register("ruby", new ModelData(
            "ruby", Material.DIAMOND, 1000001,
            "&cRuby", Arrays.asList("&7A precious red gem")
        ));

        ModelRegistry.register("sapphire", new ModelData(
            "sapphire", Material.DIAMOND, 1000002,
            "&9Sapphire", Arrays.asList("&7A precious blue gem")
        ));

        ModelRegistry.register("emerald_enhanced", new ModelData(
            "emerald_enhanced", Material.EMERALD, 1000003,
            "&aEnhanced Emerald", Arrays.asList("&7A magical green gem")
        ));

        // Currency
        ModelRegistry.register("gold_coin", new ModelData(
            "gold_coin", Material.GOLD_NUGGET, 1000010,
            "&6Gold Coin"
        ));

        ModelRegistry.register("silver_coin", new ModelData(
            "silver_coin", Material.IRON_NUGGET, 1000011,
            "&fSilver Coin"
        ));

        // Tools
        ModelRegistry.register("magic_wand", new ModelData(
            "magic_wand", Material.STICK, 1000020,
            "&5Magic Wand", Arrays.asList("&7Channel your magic")
        ));

        // Food
        ModelRegistry.register("pizza", new ModelData(
            "pizza", Material.BREAD, 1000030,
            "&6Pizza Slice", Arrays.asList("&7Delicious!")
        ));

        getLogger().info("Registered " + ModelRegistry.getAll().size() + " custom models");
    }

    private void openCustomShop(Player player) {
        AuroraGui shop = new AuroraGui("custom-shop")
            .title("&6&lCustom Item Shop")
            .rows(6)
            .setBorder(BorderType.FULL, createBorder());

        // Gems section
        shop.addItem(10, new ItemBuilder(Material.DIAMOND)
            .customModel("ruby")
            .lore("&7Price: &6$500", "&aClick to purchase!")
            .build(), event -> handlePurchase(player, "ruby", 500));

        shop.addItem(11, new ItemBuilder(Material.DIAMOND)
            .customModel("sapphire")
            .lore("&7Price: &6$450", "&aClick to purchase!")
            .build(), event -> handlePurchase(player, "sapphire", 450));

        shop.addItem(12, new ItemBuilder(Material.EMERALD)
            .customModel("emerald_enhanced")
            .lore("&7Price: &6$600", "&aClick to purchase!")
            .build(), event -> handlePurchase(player, "emerald_enhanced", 600));

        // Currency section
        shop.addItem(19, new ItemBuilder(Material.GOLD_NUGGET)
            .customModel("gold_coin")
            .amount(10)
            .lore("&7Price: &6$50", "&aClick to purchase!")
            .build(), event -> handlePurchase(player, "gold_coin", 50));

        shop.addItem(20, new ItemBuilder(Material.IRON_NUGGET)
            .customModel("silver_coin")
            .amount(25)
            .lore("&7Price: &6$25", "&aClick to purchase!")
            .build(), event -> handlePurchase(player, "silver_coin", 25));

        // Tools section
        shop.addItem(28, new ItemBuilder(Material.STICK)
            .customModel("magic_wand")
            .lore("&7Price: &6$1000", "&aClick to purchase!")
            .build(), event -> handlePurchase(player, "magic_wand", 1000));

        // Food section
        shop.addItem(37, new ItemBuilder(Material.BREAD)
            .customModel("pizza")
            .lore("&7Price: &6$10", "&aClick to purchase!")
            .build(), event -> handlePurchase(player, "pizza", 10));

        shop.register(guiManager);
        shop.open(player);
    }

    private void handlePurchase(Player player, String modelId, int price) {
        // Purchase logic here
        ModelData model = ModelRegistry.get(modelId);
        ItemStack item = new ItemBuilder(model.getBaseMaterial())
            .customModel(modelId)
            .build();

        player.getInventory().addItem(item);
        player.sendMessage("§aPurchased " + model.getDisplayName() + "!");
    }

    private ItemStack createBorder() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build();
    }
}
```

## Best Practices

### ✅ DO:

1. **Use consistent CustomModelData ranges**
   ```java
   // Gems: 1000001-1000099
   // Currency: 1000100-1000199
   // Tools: 1000200-1000299
   ```

2. **Register models on plugin enable**
   ```java
   @Override
   public void onEnable() {
       registerModels();
   }
   ```

3. **Provide meaningful IDs**
   ```java
   ModelRegistry.register("ruby", ...); // Good
   ModelRegistry.register("item1", ...); // Bad
   ```

4. **Include display names and lore**
   ```java
   new ModelData("ruby", Material.DIAMOND, 1000001,
       "&cRuby", Arrays.asList("&7A precious gem"));
   ```

5. **Test in-game thoroughly**

### ❌ DON'T:

1. **Don't use conflicting CustomModelData values**
2. **Don't forget to register before use**
3. **Don't use generic IDs**
4. **Don't hardcode CustomModelData in ItemBuilder**
5. **Don't forget to distribute resource pack to players**

## Troubleshooting

### Model Not Applying

**Problem:** Item shows default texture

**Solution:**
1. Verify model is registered: `ModelRegistry.has("ruby")`
2. Check CustomModelData value matches resource pack
3. Ensure resource pack is installed on client

### Wrong Texture Showing

**Problem:** Different texture appears

**Solution:**
1. Check for CustomModelData conflicts
2. Verify override in base item model
3. Test with F3+T reload

### Model Not Found

**Problem:** `ModelRegistry.get()` returns null

**Solution:**
1. Ensure model is registered in onEnable
2. Check spelling of model ID
3. Verify registration happened before use

## Further Reading

- **[ItemBuilder API](../api/utilities.md#itembuilder)** - Complete ItemBuilder reference
- **[Shop Tutorial](shop-tutorial.md)** - Build a complete shop system
- **[Examples](../examples/code-examples.md)** - More code examples

---

**Need help?** Check the API docs or open an issue on GitHub!
