# Quick Start Guide

Get up and running with AuroraGuis in just 5 minutes! This guide will walk you through creating your first GUI.

## Step 1: Initialize GuiManager

The `GuiManager` is the central coordinator for all your GUIs. Initialize it in your plugin's `onEnable()` method:

```java
import dev.aurora.Manager.GuiManager;

public class YourPlugin extends JavaPlugin {
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        // Initialize the GUI manager
        guiManager = new GuiManager(this);

        getLogger().info("GuiManager initialized!");
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
```

## Step 2: Create Your First GUI

Let's create a simple 3-row menu:

```java
import dev.aurora.GUI.AuroraGui;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public void createMenuGui() {
    AuroraGui menu = new AuroraGui("main-menu")
        .title("&6&lMain Menu")
        .rows(3);

    // Add a clickable item
    menu.addItem(13,
        new ItemBuilder(Material.DIAMOND)
            .name("&b&lClick Me!")
            .lore("&7This is a clickable item")
            .build(),
        event -> {
            Player player = (Player) event.getWhoClicked();
            player.sendMessage("§aYou clicked the diamond!");
        }
    );

    // Register the GUI
    menu.register(guiManager);
}
```

## Step 3: Open the GUI

Create a command to open your GUI:

```java
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {
    private final GuiManager guiManager;

    public MenuCommand(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Get and open the GUI
        AuroraGui menu = guiManager.getGui("main-menu");
        if (menu != null) {
            menu.open(player);
        }

        return true;
    }
}
```

Register the command in your plugin:

```java
@Override
public void onEnable() {
    guiManager = new GuiManager(this);
    createMenuGui();

    // Register command
    getCommand("menu").setExecutor(new MenuCommand(guiManager));
}
```

## Step 4: Add More Features

### Add a Border

```java
AuroraGui menu = new AuroraGui("main-menu")
    .title("&6&lMain Menu")
    .rows(3)
    .setBorder(BorderType.FULL,
        new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build()
    );
```

### Add Multiple Items

```java
// Add items to specific slots
menu.addItem(10, homeButton, this::handleHomeClick);
menu.addItem(12, shopButton, this::handleShopClick);
menu.addItem(14, settingsButton, this::handleSettingsClick);
menu.addItem(16, exitButton, event -> {
    event.getWhoClicked().closeInventory();
});
```

### Add an Animation

```java
import dev.aurora.Struct.Animation.Animations.RotatingCompass;

menu.addAnimation(13, new RotatingCompass());
```

## Complete Example

Here's a complete working example:

```java
package com.example.plugin;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.BorderType;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        // Initialize GUI manager
        guiManager = new GuiManager(this);

        // Create GUIs
        createMainMenu();

        getLogger().info("Plugin enabled with AuroraGuis!");
    }

    private void createMainMenu() {
        AuroraGui menu = new AuroraGui("main-menu")
            .title("&6&lMain Menu")
            .rows(3)
            .setBorder(BorderType.FULL,
                new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("&7")
                    .build()
            );

        // Home button
        menu.addItem(11,
            new ItemBuilder(Material.EMERALD)
                .name("&a&lHome")
                .lore("&7Return to spawn")
                .build(),
            event -> {
                Player player = (Player) event.getWhoClicked();
                player.closeInventory();
                player.sendMessage("§aTeleporting home...");
                // Add teleport logic here
            }
        );

        // Shop button
        menu.addItem(13,
            new ItemBuilder(Material.GOLD_INGOT)
                .name("&6&lShop")
                .lore("&7Browse items")
                .build(),
            event -> {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage("§eOpening shop...");
                // Open shop GUI here
            }
        );

        // Settings button
        menu.addItem(15,
            new ItemBuilder(Material.COMPARATOR)
                .name("&e&lSettings")
                .lore("&7Configure preferences")
                .build(),
            event -> {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage("§7Opening settings...");
                // Open settings GUI here
            }
        );

        // Close button
        menu.addItem(22,
            new ItemBuilder(Material.BARRIER)
                .name("&c&lClose")
                .build(),
            event -> event.getWhoClicked().closeInventory()
        );

        // Register the GUI
        menu.register(guiManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("menu")) {
            AuroraGui menu = guiManager.getGui("main-menu");
            if (menu != null) {
                menu.open(player);
            }
            return true;
        }

        return false;
    }
}
```

Don't forget to add the command to your `plugin.yml`:

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.plugin.MyPlugin
api-version: 1.16

commands:
  menu:
    description: Open the main menu
    usage: /menu
```

## Next Steps

Now that you have a basic GUI working, explore more features:

- **[Packet GUIs](../features/packet-guis.md)** - Add anti-duplication protection for shops
- **[Config-Based GUIs](../features/config-guis.md)** - Create GUIs from YAML files
- **[Virtual GUIs](../features/virtual-guis.md)** - Break the 54-slot limit
- **[Animations](../features/animations.md)** - Add visual effects
- **[Resource Packs](../features/resource-packs.md)** - Use custom models and textures

## Common Patterns

### Using Builder Pattern

```java
import dev.aurora.Builder.GuiBuilder;

IGui shop = GuiBuilder.shop(guiManager, "&6Item Shop")
    .item(10, swordItem, this::handleSwordPurchase)
    .item(13, armorItem, this::handleArmorPurchase)
    .item(16, toolItem, this::handleToolPurchase)
    .build();
```

### Click Conditions

```java
import dev.aurora.Struct.Condition.ClickCondition;

menu.addItem(13, premiumItem,
    event -> openPremiumShop(event),
    ClickCondition.requirePermission("shop.premium")
        .and(ClickCondition.requireLeftClick())
);
```

### Click Cooldowns

```java
import dev.aurora.Struct.Cooldown.ClickCooldown;

ClickCooldown cooldown = new ClickCooldown();

menu.addItem(13, item, event -> {
    Player player = (Player) event.getWhoClicked();

    // Check cooldown logic here
    // Handle click
});
```

---

**Congratulations!** You've created your first AuroraGUI. Continue to the [Basic Concepts](concepts.md) guide to learn more about the library's architecture.
