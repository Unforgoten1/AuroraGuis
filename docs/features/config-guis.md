# Config-Based GUI System

Create entire GUIs from YAML configuration files with automatic command registration. Perfect for server administrators who want to customize GUIs without coding.

## Overview

The config-based GUI system provides:

- **YAML Configuration** - Define GUIs in `.yml` files
- **Auto Command Registration** - Commands like `/shop` automatically created
- **Hot-Reload Support** - Reload GUIs without restarting
- **Full Feature Support** - Animations, conditions, custom models, and more
- **PlaceholderAPI Integration** - Use placeholders in titles and lore

## Quick Start

### Step 1: Enable Config System

In your plugin's `onEnable()`:

```java
import dev.aurora.Config.GuiConfigManager;
import dev.aurora.Manager.GuiManager;

@Override
public void onEnable() {
    GuiManager guiManager = new GuiManager(this);

    // Initialize config-based GUI system
    GuiConfigManager configManager = new GuiConfigManager(this, guiManager);

    // Load all GUIs from guis/ folder
    int loaded = configManager.loadAllGuis();
    getLogger().info("Loaded " + loaded + " GUI(s) from configuration");

    // Auto-register commands
    int registered = configManager.registerCommands();
    getLogger().info("Registered " + registered + " command(s)");

    // Store reference for later use
    guiManager.setConfigManager(configManager);
}
```

### Step 2: Create a GUI Configuration

Create a file: `plugins/YourPlugin/guis/shop.yml`

```yaml
gui:
  name: "shop"
  title: "&6&lItem Shop"
  rows: 6

  command: "shop"
  aliases: ["store", "market"]
  permission: "myplugin.shop"
  auto-register: true

  border:
    material: "GRAY_STAINED_GLASS_PANE"
    name: " "

  items:
    - slot: 13
      material: "DIAMOND"
      name: "&b&lDiamond"
      lore:
        - "&7Price: $100"
        - "&7Click to purchase"
      action: "close"
```

### Step 3: Test

Players can now use `/shop`, `/store`, or `/market` to open the GUI!

## YAML Schema

### Basic Structure

```yaml
gui:
  # Required fields
  name: "gui-identifier"
  title: "&6Title"
  rows: 6

  # Optional command configuration
  command: "command-name"
  aliases: ["alias1", "alias2"]
  permission: "plugin.permission"
  auto-register: true

  # Optional features
  virtual-rows: 12  # For VirtualGui (>6 rows)
  packet-validation: "PACKET"  # BASIC, PACKET, or ADVANCED
  update-interval: 20  # Auto-update every N ticks

  # Border decoration
  border:
    material: "MATERIAL_NAME"
    name: "Display name"
    lore:
      - "Line 1"

  # Items
  items:
    - slot: 0
      material: "MATERIAL"
      # ... item configuration
```

### Item Configuration

```yaml
items:
  - slot: 13  # Required
    material: "DIAMOND"  # Required

    # Display properties
    name: "&b&lDisplay Name"
    lore:
      - "&7Line 1"
      - "&7Line 2"
    amount: 1
    glow: false

    # Custom model support
    custom-model: "ruby"  # From ModelRegistry
    # OR
    custom-model-data: 1001  # Direct CMD value

    # Enchantments
    enchantments:
      DURABILITY: 3
      DAMAGE_ALL: 5

    # Animation (optional)
    animation:
      type: "PULSING_BORDER"
      speed: 10

    # Requirements (optional)
    requirements:
      permission: "shop.weapons"
      level: 10
      deny-message: "&cYou need level 10!"
      click-type: "LEFT"  # LEFT, RIGHT, SHIFT

    # Cooldown (optional)
    cooldown: 1000  # Milliseconds

    # Single action
    action: "close"

    # OR multiple actions
    actions:
      - type: "sound"
        sound: "ENTITY_PLAYER_LEVELUP"
        volume: 1.0
        pitch: 1.5
      - type: "message"
        message: "&aPurchase successful!"
      - type: "close"
```

## Available Actions

### Close GUI

```yaml
action: "close"
```

### Send Message

```yaml
action: "message:&aHello, player!"

# OR

actions:
  - type: "message"
    message: "&aHello!"
```

### Execute Player Command

```yaml
action: "command:/spawn"

# OR

actions:
  - type: "command"
    command: "spawn"
```

### Execute Console Command

```yaml
action: "console_command:give {player} diamond 1"

# OR

actions:
  - type: "console_command"
    command: "give {player} diamond 1"
```

**Note:** `{player}` is replaced with the player's name.

### Play Sound

```yaml
actions:
  - type: "sound"
    sound: "ENTITY_PLAYER_LEVELUP"
    volume: 1.0
    pitch: 1.5
```

### Open Another GUI

```yaml
action: "open_gui:other-gui-name"

# OR

actions:
  - type: "open_gui"
    gui: "other-gui-name"
```

### Multiple Actions

Combine multiple actions that execute in order:

```yaml
actions:
  - type: "sound"
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
  - type: "message"
    message: "&aItem purchased!"
  - type: "console_command"
    command: "give {player} diamond 1"
  - type: "close"
```

## Animation Types

### Pulsing Border

```yaml
animation:
  type: "PULSING_BORDER"
  speed: 10
```

### Rotating Compass

```yaml
animation:
  type: "ROTATING_COMPASS"
  speed: 5
```

### Loading Bar

```yaml
animation:
  type: "LOADING_BAR"
  slots: 9
  speed: 10
```

### Marquee Text

```yaml
animation:
  type: "MARQUEE"
  text: "Welcome to the shop!"
  speed: 2
```

### Typewriter Effect

```yaml
animation:
  type: "TYPEWRITER"
  text: "Loading..."
```

## Requirements / Conditions

### Permission

```yaml
requirements:
  permission: "shop.premium"
  deny-message: "&cYou need premium rank!"
```

### Click Type

```yaml
requirements:
  click-type: "LEFT"  # LEFT, RIGHT, or SHIFT
```

### Combined Requirements

```yaml
requirements:
  permission: "shop.weapons"
  click-type: "LEFT"
  deny-message: "&cYou need the weapons permission!"
```

## Extended Features

### Virtual GUIs (>6 Rows)

Create GUIs larger than 54 slots:

```yaml
gui:
  name: "mega-inventory"
  title: "&6&lMega Storage"
  virtual-rows: 15  # 135 slots!
  command: "storage"

  items:
    - slot: 0
      material: "CHEST"
      name: "&eSlot 1"

    - slot: 100
      material: "CHEST"
      name: "&eSlot 101"
```

### Packet Validation

Add anti-duplication protection:

```yaml
gui:
  name: "secure-shop"
  title: "&6&lSecure Shop"
  rows: 6
  packet-validation: "ADVANCED"  # BASIC, PACKET, or ADVANCED
  command: "secureshop"
```

### PlaceholderAPI Integration

Use placeholders in titles and lore:

```yaml
gui:
  title: "&6Shop - Balance: &e%vault_eco_balance%"

  items:
    - slot: 13
      material: "DIAMOND"
      name: "&b%player_name%'s Diamond"
      lore:
        - "&7Your level: %player_level%"
        - "&7Your balance: $%vault_eco_balance%"
```

## Hot-Reload

### Reload a Specific GUI

```java
guiManager.reloadGui("shop");
```

Or via command (if enabled):

```
/shop reload
```

### Reload All GUIs

```java
GuiConfigManager configManager = guiManager.getConfigManager();
configManager.reloadAll();
```

## Advanced Example

Complete shop configuration with all features:

```yaml
gui:
  name: "premium-shop"
  title: "&6&l⭐ Premium Shop ⭐"
  rows: 6
  command: "premiumshop"
  aliases: ["pshop", "prem"]
  permission: "shop.premium"
  auto-register: true
  packet-validation: "PACKET"

  border:
    material: "BLACK_STAINED_GLASS_PANE"
    name: "&8"

  items:
    # Weapon Section
    - slot: 10
      material: "DIAMOND_SWORD"
      custom-model: "legendary_sword"
      name: "&c&lLegendary Sword"
      lore:
        - "&7Damage: &c+50"
        - "&7"
        - "&ePrice: &6$5000"
        - "&7Click to purchase"
      glow: true
      requirements:
        permission: "shop.weapons"
        click-type: "LEFT"
        deny-message: "&cYou need weapons permission!"
      cooldown: 5000
      actions:
        - type: "sound"
          sound: "ENTITY_PLAYER_LEVELUP"
          volume: 1.0
          pitch: 1.0
        - type: "console_command"
          command: "eco take {player} 5000"
        - type: "console_command"
          command: "give {player} diamond_sword 1"
        - type: "message"
          message: "&aPurchased Legendary Sword for $5000!"

    # Armor Section
    - slot: 12
      material: "DIAMOND_CHESTPLATE"
      name: "&b&lDiamond Armor Set"
      lore:
        - "&7Protection IV"
        - "&7Unbreaking III"
        - "&7"
        - "&ePrice: &6$10000"
      enchantments:
        PROTECTION_ENVIRONMENTAL: 4
        DURABILITY: 3
      glow: true
      actions:
        - type: "sound"
          sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
        - type: "console_command"
          command: "eco take {player} 10000"
        - type: "console_command"
          command: "give {player} diamond_chestplate 1"
        - type: "message"
          message: "&aPurchased Diamond Armor!"

    # Currency Exchange
    - slot: 14
      material: "GOLD_NUGGET"
      custom-model: "gold_coin"
      name: "&6&lGold Coins"
      lore:
        - "&71 Coin = $100"
        - "&7"
        - "&7Your balance: &e%vault_eco_balance%"
        - "&7Click to exchange"
      actions:
        - type: "open_gui"
          gui: "currency-exchange"

    # Info Item
    - slot: 22
      material: "BOOK"
      name: "&e&lShop Information"
      lore:
        - "&7Welcome, &b%player_name%&7!"
        - "&7"
        - "&7Use left-click to purchase"
        - "&7Prices are in dollars"
        - "&7"
        - "&7Your rank: &a%vault_rank%"
      animation:
        type: "PULSING_BORDER"
        speed: 10

    # Navigation
    - slot: 45
      material: "ARROW"
      name: "&e← Back"
      action: "close"

    - slot: 49
      material: "PAPER"
      name: "&6Page 1/1"

    - slot: 53
      material: "BARRIER"
      name: "&c&lClose"
      action: "close"
```

## Custom Actions

Register custom actions in your plugin:

```java
@Override
public void onEnable() {
    GuiConfigManager configManager = new GuiConfigManager(this, guiManager);
    configManager.loadAllGuis();

    // Register custom action
    configManager.getLoader().registerAction("teleport", (event, data) -> {
        Player player = (Player) event.getWhoClicked();
        Location loc = parseLocation(data);
        player.teleport(loc);
        player.sendMessage("§aTeleported!");
    });

    configManager.registerCommands();
}
```

Use in YAML:

```yaml
action: "teleport:world,100,64,200"
```

## Best Practices

### 1. Organize by Category

```
plugins/YourPlugin/guis/
├── shop/
│   ├── main.yml
│   ├── weapons.yml
│   └── armor.yml
├── warps/
│   ├── main.yml
│   └── locations.yml
└── admin/
    └── panel.yml
```

### 2. Use Descriptive Names

✅ **GOOD:** `premium-weapons-shop.yml`
❌ **BAD:** `shop1.yml`

### 3. Comment Your Configs

```yaml
gui:
  name: "shop"
  title: "&6Shop"

  items:
    # Main purchase button
    - slot: 13
      material: "DIAMOND"
      # ... config
```

### 4. Test Before Deployment

Always test configuration changes on a test server first.

### 5. Use Permissions

Always set permissions to control access:

```yaml
permission: "myplugin.gui.shop"
```

---

**Next:** Learn about [Virtual GUIs](virtual-guis.md) for extended sizes →
