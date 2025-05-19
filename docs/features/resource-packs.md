# Resource Pack Support

AuroraGuis provides comprehensive support for custom resource packs, enabling you to create unique, branded GUIs with custom models and pixel-perfect titles.

## Overview

The resource pack system includes:

- **Model Registry** - Manage custom model definitions
- **Title Builder** - Create pixel-perfect centered titles
- **Font Characters** - Unicode symbols and spacing characters
- **Texture Validator** - Detect missing textures before deployment

## Model Registry

### What is the Model Registry?

The Model Registry is a central system for managing custom model definitions. Instead of remembering CustomModelData values, you reference models by simple IDs like "ruby" or "gold_coin".

### Setup

#### 1. Create models.yml

Create a `models.yml` file in your plugin's data folder:

```yaml
models:
  ruby:
    base-material: EMERALD
    custom-model-data: 1001
    name: "&cRuby"
    lore:
      - "&7A rare gemstone"
      - "&7Value: $1000"

  gold_coin:
    base-material: GOLD_NUGGET
    custom-model-data: 2001
    name: "&6Gold Coin"

  diamond_sword_custom:
    base-material: DIAMOND_SWORD
    custom-model-data: 3001
    name: "&b&lLegendary Sword"
    lore:
      - "&7Damage: +50"
      - "&7Durability: Infinite"
```

#### 2. Load Models on Startup

In your plugin's `onEnable()` method:

```java
import dev.aurora.ResourcePack.ModelRegistry;

@Override
public void onEnable() {
    // Load models from configuration
    File modelsFile = new File(getDataFolder(), "models.yml");
    int loaded = ModelRegistry.loadFromConfig(modelsFile);

    getLogger().info("Loaded " + loaded + " custom models");
}
```

### Using Custom Models

#### Method 1: ItemBuilder.customModel()

The easiest way - reference by model ID:

```java
import dev.aurora.Utilities.Items.ItemBuilder;

ItemStack ruby = new ItemBuilder(Material.EMERALD)
    .customModel("ruby")  // Looks up from ModelRegistry
    .lore("&7Click to sell")
    .build();
```

#### Method 2: Direct CustomModelData

If you prefer to set the value directly:

```java
ItemStack item = new ItemBuilder(Material.EMERALD)
    .customModelData(1001)
    .name("&cRuby")
    .build();
```

#### Method 3: ModelRegistry.createItem()

Quick creation from model ID:

```java
import dev.aurora.ResourcePack.ModelRegistry;

ItemStack ruby = ModelRegistry.createItem("ruby");
```

### Programmatic Registration

Register models in code without YAML:

```java
import dev.aurora.ResourcePack.ModelRegistry;
import dev.aurora.ResourcePack.ModelData;
import org.bukkit.Material;

// Register a model
ModelRegistry.register("ruby", Material.EMERALD, 1001, "&cRuby", Arrays.asList("&7Rare gem"));

// Or create ModelData first
ModelData model = new ModelData("ruby", Material.EMERALD, 1001, "&cRuby", Arrays.asList("&7Rare"));
ModelRegistry.register(model);
```

## Title Builder

### Pixel-Perfect Titles

The TitleBuilder allows you to create perfectly centered GUI titles using pixel width calculations.

**Default GUI Title Width:** 176 pixels

### Basic Usage

```java
import dev.aurora.ResourcePack.TitleBuilder;

String title = new TitleBuilder()
    .text("&6&lShop")
    .center()
    .build();

AuroraGui gui = new AuroraGui("shop")
    .title(title)
    .rows(6)
    .register(manager);
```

### Using Icons

```java
import dev.aurora.ResourcePack.FontCharacters;

String title = new TitleBuilder()
    .icon(FontCharacters.STAR)
    .space(4)
    .text("&6&lPremium Shop")
    .space(4)
    .icon(FontCharacters.STAR)
    .center()
    .build();
```

### Custom Spacing

```java
String title = new TitleBuilder()
    .text("&cLeft")
    .space(50)  // 50 pixels of space
    .text("&aRight")
    .build();
```

### Available Font Characters

```java
// Unicode Symbols
FontCharacters.HEART      // ❤
FontCharacters.STAR       // ★
FontCharacters.CHECKMARK  // ✓
FontCharacters.CROSS      // ✗
FontCharacters.ARROW_LEFT // ←
FontCharacters.ARROW_RIGHT // →
FontCharacters.ARROW_UP   // ↑
FontCharacters.ARROW_DOWN // ↓
FontCharacters.BULLET     // •
FontCharacters.CIRCLE     // ●
FontCharacters.DIAMOND    // ◆
FontCharacters.SQUARE     // ■
FontCharacters.MUSIC      // ♫
FontCharacters.SKULL      // ☠

// Spacing characters
FontCharacters.SPACE_NEG_1   // -1 pixel
FontCharacters.SPACE_NEG_8   // -8 pixels
FontCharacters.SPACE_8       // +8 pixels
FontCharacters.SPACE_16      // +16 pixels
// ... and more
```

### Builder Methods

```java
TitleBuilder builder = new TitleBuilder();

builder.text(String text);          // Add text segment
builder.icon(FontCharacters icon);  // Add special character
builder.character(char c);          // Add custom Unicode character
builder.space(int pixels);          // Add spacing (positive or negative)
builder.center();                   // Center the title (176px width)
builder.center(int width);          // Center within custom width
builder.build();                    // Build final string
builder.getWidth();                 // Get current pixel width
builder.clear();                    // Clear all segments
```

### Integration with GuiBuilder

```java
import dev.aurora.Builder.GuiBuilder;

String title = new TitleBuilder()
    .icon(FontCharacters.DIAMOND)
    .text(" &b&lShop ")
    .icon(FontCharacters.DIAMOND)
    .center()
    .build();

IGui shop = GuiBuilder.shop(manager, title)
    .item(10, item1, handler1)
    .item(13, item2, handler2)
    .build();

// Or use the customTitle() method
GuiBuilder builder = new GuiBuilder(manager)
    .name("shop")
    .customTitle(new TitleBuilder()
        .text("&6&lShop")
        .center())
    .rows(6);
```

## Resource Pack Configuration

### Create resource-pack.yml

```yaml
resource-pack:
  enabled: true
  url: "https://example.com/custompack.zip"
  hash: "abc123..."  # SHA-1 hash
  required: false

custom-fonts:
  coin: '\uE001'
  heart: '\u2764'
  star: '\u2605'
  custom_icon: '\uE100'
```

### Load Configuration

```java
import dev.aurora.ResourcePack.ResourcePackConfig;

@Override
public void onEnable() {
    File configFile = new File(getDataFolder(), "resource-pack.yml");

    ResourcePackConfig config = new ResourcePackConfig()
        .loadFromFile(configFile);

    if (!configFile.exists()) {
        config.saveDefaults(configFile);
    }

    // Use custom fonts
    Character coin = config.getCustomFont("coin");
    if (coin != null) {
        String title = coin + " &6Shop " + coin;
    }
}
```

## Texture Validator

### Validate Before Deployment

The TextureValidator helps you catch missing textures before your players do:

```java
import dev.aurora.ResourcePack.TextureValidator;

AuroraGui shop = createShopGui();

// Find missing textures
List<String> warnings = TextureValidator.findMissingTextures(shop);

if (!warnings.isEmpty()) {
    getLogger().warning("Found " + warnings.size() + " potential texture issues:");
    for (String warning : warnings) {
        getLogger().warning("  - " + warning);
    }
}
```

### Validate Specific Items

```java
import org.bukkit.Material;

boolean valid = TextureValidator.isValid(Material.EMERALD, 1001);
if (!valid) {
    getLogger().warning("CustomModelData 1001 for EMERALD is not registered!");
}
```

### Generate Integration Guide

```java
File guideFile = new File(getDataFolder(), "RESOURCE_PACK_GUIDE.md");
TextureValidator.generateGuide(guideFile);
```

This creates a markdown file with:
- All registered models
- Required CustomModelData values
- Resource pack structure
- Integration instructions

### Analyze Custom Models

```java
Map<String, Integer> usage = TextureValidator.analyzeCustomModels(gui);

for (Map.Entry<String, Integer> entry : usage.entrySet()) {
    getLogger().info(entry.getKey() + " used " + entry.getValue() + " times");
}
```

## Complete Example

Here's a complete example combining all resource pack features:

```java
package com.example.plugin;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.ResourcePack.*;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        GuiManager manager = new GuiManager(this);

        // Load custom models
        File modelsFile = new File(getDataFolder(), "models.yml");
        int loaded = ModelRegistry.loadFromConfig(modelsFile);
        getLogger().info("Loaded " + loaded + " custom models");

        // Validate all models
        List<String> failures = ModelRegistry.validateAll();
        if (!failures.isEmpty()) {
            getLogger().warning("Failed to validate models: " + failures);
        }

        // Create resource pack config
        ResourcePackConfig rpConfig = new ResourcePackConfig()
            .loadFromFile(new File(getDataFolder(), "resource-pack.yml"));

        // Create shop with custom models and title
        createShop(manager, rpConfig);
    }

    private void createShop(GuiManager manager, ResourcePackConfig rpConfig) {
        // Create pixel-perfect title
        String title = new TitleBuilder()
            .icon(FontCharacters.STAR)
            .space(4)
            .text("&6&lPremium Shop")
            .space(4)
            .icon(FontCharacters.STAR)
            .center()
            .build();

        AuroraGui shop = new AuroraGui("premium-shop")
            .title(title)
            .rows(6);

        // Add items with custom models
        shop.addItem(10,
            new ItemBuilder(Material.EMERALD)
                .customModel("ruby")  // From ModelRegistry
                .build(),
            event -> {
                // Handle purchase
            }
        );

        shop.addItem(13,
            new ItemBuilder(Material.GOLD_NUGGET)
                .customModel("gold_coin")
                .amount(64)
                .build(),
            event -> {
                // Handle purchase
            }
        );

        // Validate textures
        List<String> warnings = TextureValidator.findMissingTextures(shop);
        if (!warnings.isEmpty()) {
            getLogger().warning("Shop has missing textures:");
            warnings.forEach(w -> getLogger().warning("  " + w));
        }

        shop.register(manager);
    }
}
```

## Resource Pack Structure

Your resource pack should follow this structure:

```
custompack.zip
├── pack.mcmeta
└── assets/
    └── minecraft/
        ├── models/
        │   └── item/
        │       ├── emerald.json
        │       ├── gold_nugget.json
        │       └── diamond_sword.json
        └── textures/
            └── item/
                ├── ruby.png
                ├── gold_coin.png
                └── legendary_sword.png
```

### Example Model JSON

**File:** `assets/minecraft/models/item/emerald.json`

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/emerald"
  },
  "overrides": [
    {
      "predicate": {"custom_model_data": 1001},
      "model": "item/ruby"
    }
  ]
}
```

**File:** `assets/minecraft/models/item/ruby.json`

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/ruby"
  }
}
```

## Best Practices

### 1. Use the Model Registry

✅ **DO:**
```java
ItemStack ruby = new ItemBuilder(Material.EMERALD)
    .customModel("ruby")
    .build();
```

❌ **DON'T:**
```java
ItemStack ruby = new ItemBuilder(Material.EMERALD)
    .customModelData(1001)  // Magic number!
    .name("&cRuby")
    .build();
```

### 2. Validate Before Deployment

Always validate your GUIs during development:

```java
if (getConfig().getBoolean("debug", false)) {
    TextureValidator.validateGui(gui, "shop");
}
```

### 3. Version Your Resource Pack

Include version info in pack.mcmeta:

```json
{
  "pack": {
    "pack_format": 15,
    "description": "My Plugin v1.0.0"
  }
}
```

### 4. Provide Fallbacks

Always provide fallback names/lore in models.yml so items work even without the resource pack.

---

**Next:** Learn about [Config-Based GUIs](config-guis.md) →
