# Configuration System

Load and manage GUIs from YAML configuration files with the GuiConfigManager.

## Overview

AuroraGuis includes a powerful configuration system that allows you to:

- Define GUIs in YAML files
- Load GUIs dynamically at runtime
- Hot-reload configurations without restart
- Cache GUIs for performance
- Register command shortcuts

## Quick Start

### Step 1: Create YAML Configuration

```yaml
# guis.yml
guis:
  main-menu:
    title: "&6&lMain Menu"
    rows: 3
    border:
      type: FULL
      material: GRAY_STAINED_GLASS_PANE
      name: "&7"
    items:
      13:
        material: COMPASS
        name: "&bNavigate"
        lore:
          - "&7Click to open navigation"
        command: "navigate"
```

### Step 2: Initialize GuiConfigManager

```java
public class YourPlugin extends JavaPlugin {
    private GuiManager guiManager;
    private GuiConfigManager configManager;

    @Override
    public void onEnable() {
        guiManager = new GuiManager(this);
        configManager = new GuiConfigManager(this, guiManager);

        // Load GUIs from config
        configManager.loadFromFile("guis.yml");

        // Register command handler
        getCommand("gui").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player && args.length > 0) {
                configManager.openGui((Player) sender, args[0]);
            }
            return true;
        });
    }
}
```

### Step 3: Open GUI

```java
// Open by name
configManager.openGui(player, "main-menu");

// Or with command
player.performCommand("gui main-menu");
```

## Configuration Format

### Basic GUI

```yaml
main-menu:
  title: "&6&lMain Menu"
  rows: 3
  items:
    13:
      material: DIAMOND
      name: "&bCenter Item"
```

### With Border

```yaml
shop:
  title: "&6Shop"
  rows: 6
  border:
    type: FULL  # FULL, TOP, BOTTOM, SIDES, CORNERS
    material: BLUE_STAINED_GLASS_PANE
    name: "&9"
```

### With Multiple Items

```yaml
navigation:
  title: "&6Navigation"
  rows: 3
  items:
    10:
      material: GRASS_BLOCK
      name: "&aSpawn"
      lore:
        - "&7Click to teleport"
      command: "spawn"

    13:
      material: ENDER_PEARL
      name: "&5Warps"
      command: "warps"

    16:
      material: NETHER_STAR
      name: "&6Premium"
      permission: "server.premium"
```

### With Conditions

```yaml
vip-menu:
  title: "&6VIP Menu"
  rows: 3
  items:
    13:
      material: DIAMOND
      name: "&bVIP Item"
      permission: "server.vip"
      click-type: LEFT  # LEFT, RIGHT, SHIFT
```

### With Custom Model Data

```yaml
custom-items:
  title: "&6Custom Items"
  rows: 3
  items:
    13:
      material: DIAMOND
      custom-model-data: 1000001
      name: "&cRuby"
```

## GuiConfigManager API

### Loading Configurations

```java
// Load from file
configManager.loadFromFile("guis.yml");

// Load from multiple files
configManager.loadFromFile("menus.yml");
configManager.loadFromFile("shops.yml");

// Reload all configurations
configManager.reloadAll();
```

### Opening GUIs

```java
// Open GUI by name
configManager.openGui(player, "main-menu");

// Check if GUI exists
boolean exists = configManager.hasGui("main-menu");

// Get GUI instance
AuroraGui gui = configManager.getGui("main-menu");
```

### Caching

```java
// Enable caching (default: true)
configManager.setCacheEnabled(true);

// Clear cache
configManager.clearCache();

// Get cache statistics
int cachedCount = configManager.getCacheSize();
```

### Command Registry

```java
// Register GUI command shortcut
configManager.registerCommand("menu", "main-menu");

// Player can now use: /gui menu
// Which opens the "main-menu" GUI
```

## Advanced Configuration

### Complete Example

```yaml
advanced-shop:
  title: "&6&lAdvanced Shop"
  rows: 6

  # GUI type (optional)
  type: PACKET  # AURORA, PACKET, VIRTUAL
  validation-level: PACKET  # For PacketGui

  # Border
  border:
    type: FULL
    material: GRAY_STAINED_GLASS_PANE
    name: "&7"

  # Items
  items:
    # Shop items
    10:
      material: DIAMOND_SWORD
      name: "&cDiamond Sword"
      lore:
        - "&7Sharpness V"
        - "&7Unbreaking III"
        - ""
        - "&7Price: &6$500"
        - "&aLeft-click to purchase"
      enchantments:
        DAMAGE_ALL: 5
        DURABILITY: 3
      permission: "shop.weapons"
      click-type: LEFT
      command: "shop purchase diamond_sword"

    # Navigation
    45:
      material: ARROW
      name: "&cBack"
      command: "shop main"

    # Balance display
    49:
      material: GOLD_INGOT
      name: "&6Balance"
      lore:
        - "&7Your balance: &6${balance}"
      refresh-on-click: true

    # Page navigation
    48:
      material: ARROW
      name: "&ePrevious Page"
      action: PREVIOUS_PAGE

    53:
      material: ARROW
      name: "&eNext Page"
      action: NEXT_PAGE
```

### Item Actions

```yaml
items:
  13:
    material: COMPASS
    name: "&bAction Item"

    # Execute command
    command: "spawn"

    # Open another GUI
    action: OPEN_GUI
    target-gui: "other-menu"

    # Previous/Next page
    action: PREVIOUS_PAGE
    # or
    action: NEXT_PAGE

    # Close GUI
    action: CLOSE

    # Execute multiple commands
    commands:
      - "give {player} diamond 1"
      - "eco give {player} 100"
```

### Placeholders

```yaml
items:
  13:
    material: PLAYER_HEAD
    name: "&e{player}"
    lore:
      - "&7Level: &f{level}"
      - "&7Balance: &6${balance}"
      - "&7Health: &c{health}/{max_health}"
      - "&7Hunger: &6{food}/20"
      - "&7World: &a{world}"
```

Built-in placeholders:
- `{player}` - Player name
- `{uuid}` - Player UUID
- `{level}` - Experience level
- `{balance}` - Economy balance (requires Vault)
- `{health}` - Current health
- `{max_health}` - Maximum health
- `{food}` - Food level
- `{world}` - Current world name

## YamlGuiLoader

The underlying loader that parses YAML into GUI objects.

```java
YamlGuiLoader loader = new YamlGuiLoader(plugin, guiManager);

// Load from FileConfiguration
FileConfiguration config = YamlConfiguration.loadConfiguration(file);
AuroraGui gui = loader.loadGui("main-menu", config);

// Load with custom parser
EnhancedYamlParser parser = new EnhancedYamlParser();
AuroraGui gui = loader.loadWithParser("main-menu", config, parser);
```

## Best Practices

### ✅ DO:

1. **Use meaningful GUI names**
   ```yaml
   main-menu:  # Good
   gui1:       # Bad
   ```

2. **Organize by category**
   ```
   guis/
   ├── menus.yml
   ├── shops.yml
   └── admin.yml
   ```

3. **Include comments**
   ```yaml
   # Main navigation menu
   main-menu:
     title: "&6Main Menu"
   ```

4. **Use consistent formatting**

5. **Test after changes**

### ❌ DON'T:

1. **Don't hardcode values** - Use placeholders
2. **Don't duplicate configurations** - Use references
3. **Don't forget validation** - Test YAML syntax
4. **Don't skip permissions** - Add where appropriate
5. **Don't forget backups** - Keep old configs

## Troubleshooting

### GUI Not Loading

**Problem:** GUI doesn't appear in game

**Solution:**
1. Check console for errors
2. Verify YAML syntax
3. Ensure file is in correct location
4. Check GUI name matches exactly

### Items Not Appearing

**Problem:** Slots are empty

**Solution:**
1. Verify material names (use 1.16+ names)
2. Check slot numbers (0-53)
3. Ensure items section exists
4. Check indentation

### Commands Not Working

**Problem:** Click commands don't execute

**Solution:**
1. Verify command exists
2. Check player permissions
3. Ensure command is registered
4. Test command manually first

## Further Reading

- **[GuiBuilder API](../api/builders.md)** - Programmatic GUI building
- **[Shop Tutorial](shop-tutorial.md)** - Complete shop example
- **[Examples](../examples/yaml-examples.md)** - YAML configuration examples

---

**Need help?** Check the examples or open an issue on GitHub!
