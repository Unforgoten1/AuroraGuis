# Basic GUIs

Master event-based GUI creation with AuroraGui - the foundation of the library.

## Overview

**AuroraGui** is the standard event-based GUI class that handles most use cases:

- ✅ Simple navigation menus
- ✅ Information displays
- ✅ Item selectors
- ✅ Cosmetic menus
- ✅ Configuration interfaces

For economy systems and shops, see [Packet GUIs](packet-guis.md).

## Quick Start

###Your First GUI

```java
public class SimpleMenu {
    private final GuiManager manager;

    public SimpleMenu(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
    }

    public void openMenu(Player player) {
        AuroraGui menu = new AuroraGui("main-menu")
            .title("&6&lMain Menu")
            .rows(3)
            .addItem(13, new ItemBuilder(Material.COMPASS)
                .name("&bNavigate")
                .lore("&7Click to open navigation")
                .build(), event -> {
                    Player p = (Player) event.getWhoClicked();
                    p.sendMessage("§aOpening navigation...");
                    // Open another GUI
                })
            .register(manager);

        menu.open(player);
    }
}
```

## AuroraGui API

### Constructor

```java
// Create with unique identifier
AuroraGui gui = new AuroraGui(String name);
```

The `name` parameter:
- Must be unique across your plugin
- Used for internal tracking
- Not displayed to players

### Configuration Methods

#### Title

```java
gui.title(String title)
```

- Supports color codes (`&a`, `&6`, etc.)
- Maximum 32 characters
- Updates dynamically

**Example:**
```java
gui.title("&6&lItem Shop")
   .title("&a&lVIP Shop"); // Can change anytime
```

#### Rows

```java
gui.rows(int rows)
```

- Valid range: 1-6
- Each row = 9 slots
- Total slots = rows × 9

**Examples:**
```java
gui.rows(1); // 9 slots
gui.rows(3); // 27 slots
gui.rows(6); // 54 slots (maximum)
```

### Adding Items

#### Single Item

```java
gui.addItem(int slot, ItemStack item)
gui.addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler)
```

**Example:**
```java
ItemStack diamond = new ItemBuilder(Material.DIAMOND)
    .name("&bDiamond")
    .lore("&7A precious gem")
    .build();

// Non-clickable item
gui.addItem(10, diamond);

// Clickable item
gui.addItem(10, diamond, event -> {
    Player player = (Player) event.getWhoClicked();
    player.sendMessage("§aYou clicked the diamond!");
});
```

#### Multiple Items

```java
gui.setItems(Map<Integer, ItemStack> items)
```

**Example:**
```java
Map<Integer, ItemStack> items = new HashMap<>();
items.put(10, item1);
items.put(11, item2);
items.put(12, item3);

gui.setItems(items);
```

#### Fill Slots

```java
gui.fill(ItemStack item)
gui.fill(int startSlot, int endSlot, ItemStack item)
```

**Example:**
```java
ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
    .name("&7")
    .build();

// Fill all empty slots
gui.fill(filler);

// Fill range (slots 0-8, top row)
gui.fill(0, 8, filler);
```

### Borders

#### Set Border

```java
gui.setBorder(BorderType type, ItemStack item)
```

**Border Types:**
- `BorderType.FULL` - All edges
- `BorderType.TOP` - Top row
- `BorderType.BOTTOM` - Bottom row
- `BorderType.SIDES` - Left and right columns
- `BorderType.CORNERS` - Four corners

**Example:**
```java
ItemStack border = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE)
    .name("&9")
    .build();

gui.setBorder(BorderType.FULL, border);
```

**Border Slot Positions:**
```
FULL Border:
┌─────────────────────────────────┐
│  X   X   X   X   X   X   X   X   X  │  Top
│  X   ·   ·   ·   ·   ·   ·   ·   X  │  Sides
│  X   ·   ·   ·   ·   ·   ·   ·   X  │  Sides
│  X   X   X   X   X   X   X   X   X  │  Bottom
└─────────────────────────────────┘

CORNERS Border:
┌─────────────────────────────────┐
│  X   ·   ·   ·   ·   ·   ·   ·   X  │
│  ·   ·   ·   ·   ·   ·   ·   ·   ·  │
│  ·   ·   ·   ·   ·   ·   ·   ·   ·  │
│  X   ·   ·   ·   ·   ·   ·   ·   X  │
└─────────────────────────────────┘
```

### Pagination

#### Enable Pagination

```java
gui.addPaginatedItems(List<ItemStack> items, Consumer<InventoryClickEvent> handler)
```

**Example:**
```java
List<ItemStack> allItems = new ArrayList<>();
// Add 100 items...

// System automatically splits across pages
gui.addPaginatedItems(allItems, event -> {
    ItemStack clicked = event.getCurrentItem();
    Player player = (Player) event.getWhoClicked();
    player.sendMessage("§aClicked: " + clicked.getItemMeta().getDisplayName());
});

// Add navigation buttons
ItemStack prev = new ItemBuilder(Material.ARROW)
    .name("&ePrevious Page")
    .build();

ItemStack next = new ItemBuilder(Material.ARROW)
    .name("&eNext Page")
    .build();

gui.addItem(45, prev, e -> gui.prevPage());
gui.addItem(53, next, e -> gui.nextPage());
```

#### Page Navigation

```java
gui.nextPage()        // Go to next page
gui.prevPage()        // Go to previous page
gui.setPage(int page) // Go to specific page (0-indexed)
gui.getCurrentPage()  // Get current page number
gui.getTotalPages()   // Get total page count
```

#### Page Change Callback

```java
gui.onPageChange(Consumer<Integer> callback)
```

**Example:**
```java
gui.onPageChange(newPage -> {
    gui.title("&6Shop - Page " + (newPage + 1));
});
```

### Opening and Closing

#### Open GUI

```java
gui.open(Player player)
```

- Opens GUI for specified player
- Cancels if inventory is already open
- Triggers `onOpen` listener

#### Close GUI

```java
gui.close(Player player)
```

- Closes GUI for specified player
- Triggers `onClose` listener
- Automatic cleanup

### Registration

```java
gui.register(GuiManager manager)
```

- **Required** before opening
- Registers with GuiManager for tracking
- Enables animations and other features

**Example:**
```java
AuroraGui gui = new AuroraGui("shop")
    .title("&6Shop")
    .rows(3)
    .addItem(13, item, handler)
    .register(manager); // Must call before open()

gui.open(player);
```

## Advanced Features

### Click Conditions

Add requirements that must be met before click handlers execute.

```java
gui.addItem(slot, item, handler, ClickCondition condition)
```

**Built-in Conditions:**
```java
// Permission check
ClickCondition.requirePermission("shop.vip")

// Click type
ClickCondition.requireLeftClick()
ClickCondition.requireRightClick()
ClickCondition.requireShiftClick()

// Item presence
ClickCondition.requireItem()

// Custom logic
ClickCondition.custom(player -> player.getLevel() >= 10)
```

**Combining Conditions:**
```java
gui.addItem(10, premiumItem, handler,
    ClickCondition.requirePermission("shop.premium")
        .and(ClickCondition.requireLeftClick())
        .and(ClickCondition.custom(p -> p.getLevel() >= 20))
);
```

**Example:**
```java
gui.addItem(13, vipItem, event -> {
    Player player = (Player) event.getWhoClicked();
    giveVIPReward(player);
},
ClickCondition.requirePermission("server.vip")
    .and(ClickCondition.requireLeftClick())
);
```

See [Conditions & Cooldowns](conditions-cooldowns.md) for more details.

### Cooldowns

Prevent spam-clicking with cooldowns.

```java
ClickCooldown cooldown = new ClickCooldown();
cooldown.setDefaultCooldown(1000); // 1 second

gui.addItem(13, rewardItem, event -> {
    Player player = (Player) event.getWhoClicked();

    if (cooldown.canClick(player)) {
        giveReward(player);
        cooldown.recordClick(player);
    } else {
        long remaining = cooldown.getRemainingCooldown(player);
        player.sendMessage("§cCooldown: " + (remaining / 1000) + "s");
    }
});
```

See [Conditions & Cooldowns](conditions-cooldowns.md) for more details.

### Event Listeners

Respond to GUI lifecycle events.

```java
gui.addListener(new GuiListener() {
    @Override
    public void onOpen(Player player, AuroraGui gui) {
        player.sendMessage("§aWelcome to " + gui.getName());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
    }

    @Override
    public void onClose(Player player, AuroraGui gui) {
        savePlayerProgress(player);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        // Called for every click
        logClick(event);
    }

    @Override
    public void onAnimationStart(AuroraGui gui, int slot) {
        plugin.getLogger().info("Animation started at slot " + slot);
    }

    @Override
    public void onAnimationComplete(AuroraGui gui, int slot) {
        plugin.getLogger().info("Animation completed at slot " + slot);
    }
});
```

## Complete Examples

### Example 1: Navigation Menu

```java
public class NavigationMenu {
    private final GuiManager manager;

    public NavigationMenu(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
    }

    public void open(Player player) {
        AuroraGui menu = new AuroraGui("nav-menu")
            .title("&6&lNavigation")
            .rows(3)
            .setBorder(BorderType.FULL, createBorder())
            .addItem(11, createButton(Material.GRASS_BLOCK, "&aSpawn", "Teleport to spawn"),
                e -> teleportToSpawn((Player) e.getWhoClicked()))
            .addItem(13, createButton(Material.CHEST, "&eWarps", "Open warp menu"),
                e -> openWarps((Player) e.getWhoClicked()))
            .addItem(15, createButton(Material.DIAMOND_SWORD, "&cPvP Arena", "Join PvP"),
                e -> joinPvP((Player) e.getWhoClicked()))
            .register(manager);

        menu.open(player);
    }

    private ItemStack createButton(Material material, String name, String lore) {
        return new ItemBuilder(material)
            .name(name)
            .lore("&7" + lore)
            .build();
    }

    private ItemStack createBorder() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build();
    }

    private void teleportToSpawn(Player player) {
        player.closeInventory();
        player.teleport(player.getWorld().getSpawnLocation());
        player.sendMessage("§aTeleported to spawn!");
    }

    private void openWarps(Player player) {
        // Open warps GUI
    }

    private void joinPvP(Player player) {
        // Join PvP arena
    }
}
```

### Example 2: Confirmation Dialog

```java
public class ConfirmationDialog {
    public static void confirm(GuiManager manager, Player player,
                              String message, Runnable onConfirm, Runnable onCancel) {
        AuroraGui dialog = new AuroraGui("confirm")
            .title("&cConfirm Action")
            .rows(3)

            // Confirm button (green)
            .addItem(11, new ItemBuilder(Material.GREEN_WOOL)
                .name("&a&lCONFIRM")
                .lore("&7Click to confirm")
                .build(), event -> {
                    player.closeInventory();
                    onConfirm.run();
                })

            // Info display (yellow)
            .addItem(13, new ItemBuilder(Material.PAPER)
                .name("&e&lConfirmation")
                .lore("&7" + message, "", "&cThis cannot be undone!")
                .build(), event -> {})

            // Cancel button (red)
            .addItem(15, new ItemBuilder(Material.RED_WOOL)
                .name("&c&lCANCEL")
                .lore("&7Click to cancel")
                .build(), event -> {
                    player.closeInventory();
                    onCancel.run();
                })

            .register(manager);

        dialog.open(player);
    }

    // Usage example
    public void deletePlayerData(Player player, GuiManager manager) {
        confirm(manager, player,
            "Are you sure you want to delete your data?",
            () -> {
                performDeletion(player);
                player.sendMessage("§aData deleted!");
            },
            () -> player.sendMessage("§7Cancelled.")
        );
    }
}
```

### Example 3: Selector GUI

```java
public class KitSelector {
    private final GuiManager manager;

    public KitSelector(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
    }

    public void open(Player player) {
        AuroraGui selector = new AuroraGui("kit-selector")
            .title("&6&lSelect Your Kit")
            .rows(3)
            .setBorder(BorderType.SIDES, createBorder())

            // Warrior kit
            .addItem(11, createKitItem(
                Material.DIAMOND_SWORD,
                "&c&lWarrior Kit",
                "&7Strong melee attacks",
                "&7High health",
                "&7Resistance II"
            ), event -> giveKit(player, "warrior"))

            // Archer kit
            .addItem(13, createKitItem(
                Material.BOW,
                "&a&lArcher Kit",
                "&7Ranged combat",
                "&7Speed II",
                "&7Infinite arrows"
            ), event -> giveKit(player, "archer"))

            // Tank kit
            .addItem(15, createKitItem(
                Material.SHIELD,
                "&9&lTank Kit",
                "&7Heavy armor",
                "&7Absorption III",
                "&7Knockback resistance"
            ), event -> giveKit(player, "tank"))

            .register(manager);

        selector.open(player);
    }

    private ItemStack createKitItem(Material icon, String name, String... lore) {
        return new ItemBuilder(icon)
            .name(name)
            .lore(lore)
            .glow()
            .build();
    }

    private ItemStack createBorder() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build();
    }

    private void giveKit(Player player, String kitName) {
        player.closeInventory();
        // Give kit items and effects
        player.sendMessage("§aYou received the " + kitName + " kit!");
    }
}
```

### Example 4: Player Profile

```java
public class PlayerProfile {
    private final GuiManager manager;

    public PlayerProfile(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
    }

    public void open(Player player) {
        AuroraGui profile = new AuroraGui("profile")
            .title("&6Profile: " + player.getName())
            .rows(4)
            .setBorder(BorderType.FULL, createBorder())

            // Player head
            .addItem(13, new ItemBuilder(Material.PLAYER_HEAD)
                .skull(player.getName())
                .name("&e" + player.getName())
                .lore(
                    "&7Level: &f" + player.getLevel(),
                    "&7Health: &c" + player.getHealth() + "/" + player.getMaxHealth(),
                    "&7Hunger: &6" + player.getFoodLevel() + "/20"
                )
                .build(), event -> {})

            // Statistics
            .addItem(20, new ItemBuilder(Material.BOOK)
                .name("&bStatistics")
                .lore("&7Click to view stats")
                .build(), e -> openStats(player))

            // Settings
            .addItem(22, new ItemBuilder(Material.COMPARATOR)
                .name("&6Settings")
                .lore("&7Click to configure")
                .build(), e -> openSettings(player))

            // Achievements
            .addItem(24, new ItemBuilder(Material.DIAMOND)
                .name("&aAchievements")
                .lore("&7Click to view")
                .build(), e -> openAchievements(player))

            .register(manager);

        profile.open(player);
    }

    private ItemStack createBorder() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
            .name("&7")
            .build();
    }

    private void openStats(Player player) {
        // Open stats GUI
    }

    private void openSettings(Player player) {
        // Open settings GUI
    }

    private void openAchievements(Player player) {
        // Open achievements GUI
    }
}
```

## Best Practices

### 1. Use Unique Names

✅ **DO:**
```java
new AuroraGui("player-" + player.getUniqueId() + "-profile")
```

❌ **DON'T:**
```java
new AuroraGui("profile") // Conflicts if multiple players
```

### 2. Register Before Opening

✅ **DO:**
```java
gui.register(manager);
gui.open(player);
```

❌ **DON'T:**
```java
gui.open(player); // Forgot to register!
```

### 3. Handle Null Items

✅ **DO:**
```java
gui.addItem(13, item, event -> {
    ItemStack clicked = event.getCurrentItem();
    if (clicked != null && clicked.hasItemMeta()) {
        // Process click
    }
});
```

❌ **DON'T:**
```java
gui.addItem(13, item, event -> {
    String name = event.getCurrentItem().getItemMeta().getDisplayName(); // NullPointerException!
});
```

### 4. Close Inventory Appropriately

✅ **DO:**
```java
gui.addItem(13, item, event -> {
    Player player = (Player) event.getWhoClicked();
    player.closeInventory(); // Before teleport/command
    player.teleport(location);
});
```

❌ **DON'T:**
```java
gui.addItem(13, item, event -> {
    // Forgot to close, player sees GUI during teleport
    player.teleport(location);
});
```

## Performance Tips

1. **Reuse ItemStacks** - Create once, use many times
2. **Limit Listeners** - Only add what you need
3. **Batch Updates** - Use `setItems()` for multiple items
4. **Clean Up** - GuiManager handles this automatically

## Troubleshooting

### Items Not Appearing

**Problem:** Added items don't show in GUI

**Solution:** Ensure GUI is registered before opening
```java
gui.register(manager); // Must call first!
gui.open(player);
```

### Click Handlers Not Working

**Problem:** Clicking items does nothing

**Solution:** Verify handler is passed to `addItem()`
```java
gui.addItem(13, item, event -> {
    // This handler must be provided
});
```

### GUI Not Closing

**Problem:** GUI stays open unexpectedly

**Solution:** Call `player.closeInventory()` in handlers
```java
gui.addItem(13, item, event -> {
    Player player = (Player) event.getWhoClicked();
    player.closeInventory(); // Close explicitly
});
```

## Next Steps

- **[Animations](animations.md)** - Add visual effects to your GUIs
- **[Packet GUIs](packet-guis.md)** - Secure economy systems
- **[Virtual GUIs](virtual-guis.md)** - Create GUIs with more than 54 slots
- **[Examples](../examples/code-examples.md)** - More code examples

---

**Need help?** Check the [API Reference](../api/aurora-gui.md) or open an issue on GitHub.
