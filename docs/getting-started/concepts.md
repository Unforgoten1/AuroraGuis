# Core Concepts

Understanding AuroraGuis' architecture and terminology will help you build powerful, maintainable GUI systems.

## Overview

AuroraGuis is built on three core principles:

1. **Fluent API Design** - Method chaining for readable, intuitive code
2. **Event-Driven Architecture** - Respond to user interactions with handlers
3. **Composable Features** - Mix and match animations, conditions, cooldowns, etc.

## Architecture

### GUI Hierarchy

```
IGui (Interface)
├── AuroraGui (Event-based)
├── PacketGui (Packet validation)
└── VirtualGui (Extended sizes)
```

All GUI types implement the `IGui` interface, allowing polymorphic handling throughout the system.

### Core Components

#### 1. GuiManager

The **GuiManager** is the central coordinator for all GUIs in your plugin.

**Responsibilities:**
- Register and track active GUIs
- Handle packet support initialization
- Manage animation scheduling
- Coordinate GUI lifecycle events

**Usage:**
```java
public class YourPlugin extends JavaPlugin {
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        // Create manager ONCE during plugin startup
        guiManager = new GuiManager(this);

        // Optional: Enable packet support for PacketGuis
        guiManager.enablePacketSupport();
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
```

#### 2. AuroraGui

The **AuroraGui** is the standard event-based GUI class.

**Key Features:**
- Bukkit event handling (InventoryClickEvent)
- Animations support
- Pagination
- Borders and decorations
- Click conditions and cooldowns

**Lifecycle:**
1. **Create** - Instantiate with `new AuroraGui(name)`
2. **Configure** - Chain methods to set title, rows, items
3. **Register** - Call `.register(manager)` to activate
4. **Open** - Call `.open(player)` to display
5. **Close** - Automatically handled or manual `.close(player)`

#### 3. PacketGui

The **PacketGui** extends AuroraGui with packet-level validation.

**Why PacketGui?**
- Intercepts packets **before** Bukkit processes them
- Detects 11 different exploit types
- Server-side truth tracking
- Configurable validation levels

**When to Use:**
- Economy systems (shops, banks, trading)
- Reward systems
- Any GUI where item duplication would be problematic

#### 4. VirtualGui

The **VirtualGui** breaks the 54-slot (6-row) limit using pagination.

**How It Works:**
- Creates multiple physical 54-slot pages
- Maps virtual slots (0-∞) to physical pages
- Automatic navigation buttons
- Seamless transitions

## Key Concepts

### 1. Slots

**Slot Numbering:**
```
┌─────────────────────────────────┐
│  0   1   2   3   4   5   6   7   8  │  Row 0
│  9  10  11  12  13  14  15  16  17  │  Row 1
│ 18  19  20  21  22  23  24  25  26  │  Row 2
│ 27  28  29  30  31  32  33  34  35  │  Row 3
│ 36  37  38  39  40  41  42  43  44  │  Row 4
│ 45  46  47  48  49  50  51  52  53  │  Row 5
└─────────────────────────────────┘
```

**Calculating Slots:**
```java
// Get slot from row/column (0-indexed)
int slot = (row * 9) + column;

// Get row from slot
int row = slot / 9;

// Get column from slot
int column = slot % 9;

// Center slot in GUI
int centerSlot = (rows / 2) * 9 + 4;
```

### 2. Items

Items are the visual elements displayed in GUI slots.

**Creating Items:**
```java
// Using ItemBuilder (recommended)
ItemStack item = new ItemBuilder(Material.DIAMOND)
    .name("&bDiamond Item")
    .lore("&7Line 1", "&7Line 2")
    .amount(16)
    .glow()
    .build();

// Adding to GUI
gui.addItem(13, item, event -> {
    Player player = (Player) event.getWhoClicked();
    player.sendMessage("Clicked!");
});
```

**Item Properties:**
- Material type
- Display name (supports color codes)
- Lore (supports color codes)
- Amount
- Custom model data (for resource packs)
- Enchantments (including glow effect)
- NBT data

### 3. Click Handlers

Click handlers are `Consumer<InventoryClickEvent>` functions that execute when items are clicked.

**Basic Handler:**
```java
gui.addItem(10, item, event -> {
    Player player = (Player) event.getWhoClicked();
    player.sendMessage("You clicked slot 10!");
    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
});
```

**Handler with Conditions:**
```java
gui.addItem(10, item,
    event -> handlePurchase(event),
    ClickCondition.requirePermission("shop.vip")
        .and(ClickCondition.requireLeftClick())
);
```

**Event Information:**
- `event.getWhoClicked()` - Player who clicked
- `event.getSlot()` - Slot number clicked
- `event.getCurrentItem()` - Item in clicked slot
- `event.getCursor()` - Item on cursor
- `event.isLeftClick()` / `event.isRightClick()`
- `event.isShiftClick()`

### 4. Animations

Animations update items in slots over time.

**Types:**
- **Frame-based** - Sequence of items with durations
- **Single-slot** - Animates one slot (e.g., pulsing item)
- **Multi-slot** - Animates multiple slots (e.g., border wave)

**How Animations Work:**
1. Animation added to GUI with `.addAnimation(slot, animation)`
2. Registered with centralized `AnimationScheduler`
3. Scheduler ticks animation every Minecraft tick (50ms)
4. Animation returns next frame item and duration
5. GUI updates slot with new item

**Performance:**
- All animations share one BukkitTask (O(1) overhead)
- No per-animation tasks
- Highly scalable

### 5. Conditions

Conditions filter clicks before handlers execute.

**Functional Interface:**
```java
@FunctionalInterface
public interface ClickCondition {
    boolean test(InventoryClickEvent event);
}
```

**Built-in Conditions:**
- `requirePermission(String)` - Check permission
- `requireLeftClick()` - Must be left-click
- `requireRightClick()` - Must be right-click
- `requireShiftClick()` - Must be shift-click
- `requireItem()` - Slot must have item

**Combinators:**
- `.and(other)` - Both conditions must pass
- `.or(other)` - Either condition must pass
- `.negate()` - Inverts condition

### 6. Cooldowns

Cooldowns prevent spam-clicking.

**Types:**
- **Global** - Applies to all clicks in GUI
- **Per-slot** - Applies to specific slot

**Usage:**
```java
ClickCooldown cooldown = new ClickCooldown();
cooldown.setDefaultCooldown(1000); // 1 second

if (cooldown.canClick(player)) {
    // Process click
    cooldown.recordClick(player);
} else {
    long remaining = cooldown.getRemainingCooldown(player);
    player.sendMessage("Wait " + (remaining / 1000) + " seconds");
}
```

### 7. Pagination

Pagination allows displaying more items than fit in one GUI.

**How It Works:**
1. Provide list of items with `.addPaginatedItems(items, handler)`
2. System splits items across pages automatically
3. Add navigation buttons for prev/next
4. Use `.nextPage()` / `.prevPage()` to navigate

**Example:**
```java
List<ItemStack> items = getAllShopItems(); // 100 items

gui.addPaginatedItems(items, event -> handlePurchase(event))
   .addItem(45, prevButton, e -> gui.prevPage())
   .addItem(53, nextButton, e -> gui.nextPage());
```

### 8. Borders

Borders decorate GUI edges with items.

**Border Types:**
```java
enum BorderType {
    FULL,      // All edges
    TOP,       // Top row only
    BOTTOM,    // Bottom row only
    SIDES,     // Left and right columns
    CORNERS    // Four corners only
}
```

**Usage:**
```java
ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
    .name("&7")
    .build();

gui.setBorder(BorderType.FULL, borderItem);
```

### 9. Listeners

Listeners respond to GUI lifecycle events.

**GuiListener Interface:**
```java
public interface GuiListener {
    void onOpen(Player player, AuroraGui gui);
    void onClose(Player player, AuroraGui gui);
    void onClick(InventoryClickEvent event);
    void onAnimationStart(AuroraGui gui, int slot);
    void onAnimationComplete(AuroraGui gui, int slot);
}
```

**Usage:**
```java
gui.addListener(new GuiListener() {
    @Override
    public void onOpen(Player player, AuroraGui gui) {
        player.sendMessage("§aWelcome to " + gui.getName());
    }

    @Override
    public void onClose(Player player, AuroraGui gui) {
        savePlayerData(player);
    }
});
```

## Design Patterns

### 1. Fluent API Pattern

AuroraGuis uses method chaining for readable configuration:

```java
AuroraGui gui = new AuroraGui("shop")
    .title("&6Item Shop")           // Returns this
    .rows(6)                         // Returns this
    .setBorder(BorderType.FULL, item) // Returns this
    .addItem(13, item, handler)      // Returns this
    .register(manager);              // Returns IGui
```

**Benefits:**
- Self-documenting code
- IDE auto-completion friendly
- Reduces boilerplate

### 2. Builder Pattern

Complex GUIs can use `GuiBuilder`:

```java
IGui shop = GuiBuilder.shop(manager, "&6Shop")
    .item(10, swordItem, this::handleSword)
    .item(13, armorItem, this::handleArmor)
    .border(BorderType.FULL)
    .build();
```

### 3. Strategy Pattern

Validation levels use strategy pattern:

```java
ValidationLevel.BASIC    // No packet validation
ValidationLevel.PACKET   // Standard validation
ValidationLevel.ADVANCED // Full protection
```

Each level implements different validation strategies transparently.

## Configuration Hierarchy

```
Plugin
  └── GuiManager
        ├── AnimationScheduler
        ├── PacketEventManager (if enabled)
        └── GUIs
              ├── AuroraGui
              │     ├── Items
              │     ├── Animations
              │     ├── Conditions
              │     └── Cooldowns
              ├── PacketGui
              │     ├── (All AuroraGui features)
              │     └── Validation
              │           ├── AntiDupeValidator
              │           ├── ServerSideInventory
              │           ├── ClickValidator
              │           └── CursorTracker
              └── VirtualGui
                    ├── Physical Pages (AuroraGuis)
                    └── Navigation
```

## Best Practices

### 1. Manager Lifecycle

✅ **DO:**
```java
// Create once in onEnable()
guiManager = new GuiManager(this);

// Reuse everywhere
public GuiManager getGuiManager() {
    return guiManager;
}
```

❌ **DON'T:**
```java
// Don't create multiple managers
GuiManager manager1 = new GuiManager(this);
GuiManager manager2 = new GuiManager(this); // Bad!
```

### 2. GUI Registration

✅ **DO:**
```java
// Register GUI for lifecycle management
gui.register(manager);
```

❌ **DON'T:**
```java
// Don't open unregistered GUIs
gui.open(player); // Missing .register()!
```

### 3. Resource Cleanup

✅ **DO:**
```java
// Automatic cleanup when player closes
// No manual cleanup needed in most cases
```

❌ **DON'T:**
```java
// Don't manually cancel animation tasks
// Let AnimationScheduler handle it
```

### 4. Validation Level Selection

✅ **DO:**
```java
// Choose appropriate level
PacketGui menu = new PacketGui("menu")
    .validationLevel(ValidationLevel.BASIC); // Just a menu

PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.PACKET); // Shop

PacketGui bank = new PacketGui("bank")
    .validationLevel(ValidationLevel.ADVANCED); // Bank
```

❌ **DON'T:**
```java
// Don't use ADVANCED for everything
PacketGui menu = new PacketGui("menu")
    .validationLevel(ValidationLevel.ADVANCED); // Overkill!
```

## Common Workflows

### Creating a Basic GUI

```java
// 1. Get manager
GuiManager manager = plugin.getGuiManager();

// 2. Create GUI
AuroraGui gui = new AuroraGui("my-gui")
    .title("&6My GUI")
    .rows(3);

// 3. Add items
gui.addItem(13, item, event -> {
    // Handle click
});

// 4. Register
gui.register(manager);

// 5. Open for player
gui.open(player);
```

### Creating a Secure Shop

```java
// 1. Enable packet support (once in onEnable)
manager.enablePacketSupport();

// 2. Create PacketGui
PacketGui shop = new PacketGui("shop")
    .title("&6Secure Shop")
    .rows(6)
    .validationLevel(ValidationLevel.PACKET);

// 3. Add shop items
shop.addItem(10, item, event -> handlePurchase(event));

// 4. Register and open
shop.register(manager);
shop.open(player);
```

### Creating an Animated Menu

```java
// 1. Create GUI
AuroraGui menu = new AuroraGui("menu")
    .title("&6Animated Menu")
    .rows(3);

// 2. Add animations
menu.addAnimation(4, new PulsingBorder());
menu.addAnimation(13, rotatingAnimation);

// 3. Add items
menu.addItem(11, item1, handler1);
menu.addItem(15, item2, handler2);

// 4. Register and open
menu.register(manager);
menu.open(player);
```

## Next Steps

Now that you understand the core concepts:

- **[Basic GUIs](../features/basic-guis.md)** - Learn to create standard GUIs
- **[Animations](../features/animations.md)** - Add visual effects
- **[Packet GUIs](../features/packet-guis.md)** - Implement anti-dupe protection
- **[Virtual GUIs](../features/virtual-guis.md)** - Create large inventories

---

**Questions?** Check the [API Reference](../api/aurora-gui.md) or [Examples](../examples/code-examples.md).
