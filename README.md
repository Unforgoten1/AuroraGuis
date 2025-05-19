# AuroraGuis - Modern Minecraft GUI API

A powerful, performant, and developer-friendly GUI library for Spigot/Paper plugins featuring animations, packet-level validation, anti-duplication protection, and much more.

## Features

### Core GUI System
- 🎨 **Fluent Builder API** - Intuitive method chaining for GUI creation
- 🎬 **Advanced Animation System** - Smooth animations with centralized scheduling
- 📄 **Pagination Support** - Built-in multi-page navigation
- 📋 **Template System** - Reusable GUI layouts
- ⚡ **Async Item Loading** - Non-blocking item loading for expensive operations
- 🎯 **Click Conditions** - Flexible permission and click-type filtering
- 🎧 **Event Listeners** - Comprehensive lifecycle hooks
- 🔧 **Item Builder** - Powerful ItemStack builder with NBT support
- 🎨 **Border System** - Easy border decoration

### Packet-Based GUIs (NEW!)
- 🛡️ **Anti-Dupe Protection** - 11 different exploit detection mechanisms
- 📦 **Packet-Level Validation** - Intercepts packets before Bukkit processing
- ⚡ **Low Overhead** - Configurable validation levels (0-5ms per click)
- 🔒 **Withhold-Close-Packet Protection** - Prevents GUI close packet withholding exploits
- 📊 **Violation Logging** - Detailed logging of exploit attempts
- 🔄 **Automatic Rollback** - Server-side truth tracking with auto-correction
- ⏱️ **Session Monitoring** - Detects stale sessions and timeout exploits
- 🎯 **Configurable Strictness** - Three preset levels (lenient, normal, strict)

## Installation

### Maven

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/Unforgoten1/AuroraGuis</url>
</repository>

<dependency>
    <groupId>dev.aurora</groupId>
    <artifactId>AuroraGuis</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Manual
Download the latest JAR from releases and add to your project's build path.

## Quick Start

### Basic GUI (Event-Based)
```java
GuiManager manager = new GuiManager(plugin);

AuroraGui gui = new AuroraGui("my-gui")
    .title("&6My Awesome GUI")
    .rows(3)
    .setBorder(BorderType.FULL, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
        .name("&7")
        .build())
    .addItem(13, new ItemBuilder(Material.DIAMOND)
        .name("&bClick me!")
        .lore("&7This is a clickable item")
        .build(), event -> {
            Player player = (Player) event.getWhoClicked();
            player.sendMessage("You clicked the diamond!");
        })
    .register(manager);

gui.open(player);
```

### Packet-Based GUI with Anti-Dupe
```java
// Enable packet support ONCE during plugin startup
manager.enablePacketSupport();

// Create secure GUI
PacketGui secureShop = new PacketGui("shop")
    .title("&6&lSecure Shop")
    .rows(6)
    .validationLevel(ValidationLevel.ADVANCED)
    .addItem(13, diamondItem, event -> {
        Player player = (Player) event.getWhoClicked();
        // This is protected from all 11 exploit types!
        economy.withdraw(player, 1000);
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
    })
    .register(manager);

secureShop.open(player);
```

### Using Builder Pattern
```java
IGui shop = GuiBuilder.shop(manager, "&6Item Shop")
    .packetMode(ValidationLevel.PACKET)  // Enable packet validation
    .item(10, swordItem, this::handleSwordPurchase)
    .item(13, armorItem, this::handleArmorPurchase)
    .item(16, toolItem, this::handleToolPurchase)
    .build();
```

## Validation Levels

Choose the right protection level for your use case:

| Level | Overhead | Protection | Use Case |
|-------|----------|------------|----------|
| **BASIC** | 0ms | Bukkit events only | Regular GUIs, menus, navigation |
| **PACKET** | ~1-2ms | Timing + cursor validation | Shops, trading, player interactions |
| **ADVANCED** | ~3-5ms | Full protection + fingerprinting | Economy, banks, high-value items |

## Anti-Dupe Protection

PacketGuis protect against 11 different exploit types:

### Layer 1: Packet Interception
1. **CLICK_DELAY** - Prevents rapid clicking (min 50ms between clicks)
2. **CLICK_SPAM** - Rate limiting (max 20 clicks/second)
3. **INVALID_SLOT** - Blocks out-of-bounds slot access

### Layer 2: Server-Side Truth Tracking
4. **CURSOR_DUPLICATION** - Detects cursor amount increases
5. **CURSOR_SWAP** - Detects cursor type changes
6. **CLOSE_DESYNC** - Validates inventory state on close

### Layer 3: Transaction Verification (ADVANCED only)
7. **NBT_INJECTION** - Detects NBT data tampering via SHA-256 hashing
8. **SHIFT_CLICK_LOOP** - Prevents shift-click loop exploits
9. **TRANSACTION_MISMATCH** - Verifies post-click state matches expectations

### Layer 4: Session Monitoring
10. **NO_CLOSE_PACKET** - Detects withhold-close-packet exploits
11. **STALE_SESSION** - Detects session timeout exploits

## Examples

### Secure Economy Shop
```java
PacketGui shop = new PacketGui("premium-shop")
    .title("&6&lPremium Shop")
    .rows(6)
    .validationLevel(ValidationLevel.ADVANCED)
    .config(PacketGuiConfig.strict()
        .sessionTimeoutMs(180000)        // 3 minute timeout
        .autoKickOnTimeout(true)          // Kick on exploit
        .violationKickThreshold(5))       // Kick after 5 violations
    .onViolation((player, exploitType) -> {
        // Log exploit attempts
        plugin.getLogger().warning(player.getName() + " attempted " + exploitType);
        Bukkit.broadcast("§c" + player.getName() + " attempted exploit!", "aurora.admin");
    })
    .addItem(13, createDiamondOffer(), this::handlePurchase)
    .register(manager);
```

### Animated Shop with Pagination
```java
AuroraGui shop = new AuroraGui("animated-shop")
    .title("&6Item Shop - Page 1")
    .rows(6)
    .setBorder(BorderType.FULL, borderItem)
    .addPaginatedItems(getShopItems(), this::handlePurchase)
    .addAnimation(4, new PulsingBorder(3, 1))
    .addItem(45, previousButton, e -> shop.prevPage())
    .addItem(53, nextButton, e -> shop.nextPage())
    .register(manager);
```

### Confirmation Dialog
```java
IGui confirmDialog = GuiBuilder.confirmation(manager, "&cDelete Item?")
    .onConfirm(player -> {
        deleteItem(player);
        player.sendMessage("§aItem deleted!");
    })
    .onCancel(player -> {
        player.sendMessage("§7Cancelled.");
    })
    .build();
```

### Trading System with Anti-Dupe
```java
PacketGui tradeGUI = new PacketGui("trade")
    .title("&eTrade with " + otherPlayer.getName())
    .rows(6)
    .validationLevel(ValidationLevel.ADVANCED)
    .addItem(20, confirmButton, event -> finalizeTrade())
    .register(manager);
```

## Advanced Features

### Click Conditions
```java
gui.addItem(10, premiumItem,
    event -> openPremiumShop(event),
    ClickCondition.requirePermission("shop.premium")
        .and(ClickCondition.requireLeftClick())
        .and(ClickCondition.custom(player -> player.getLevel() >= 10))
);
```

### Click Cooldowns
```java
ClickCooldown cooldown = new ClickCooldown(5000); // 5 seconds

gui.addItem(13, item, event -> {
    Player player = (Player) event.getWhoClicked();

    if (cooldown.hasCooldown(player, 13)) {
        player.sendMessage("§cCooldown: " + cooldown.getRemainingTime(player, 13) + "ms");
        return;
    }

    // Handle click
    cooldown.setCooldown(player, 13);
});
```

### Event Listeners
```java
gui.addListener(new GuiListener() {
    @Override
    public void onOpen(Player player, AuroraGui gui) {
        player.sendMessage("Welcome!");
    }

    @Override
    public void onClose(Player player, AuroraGui gui) {
        savePlayerData(player);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        // Handle click
    }
});
```

### Custom Animations
```java
Animation spinning = new Animation(true) // Loop
    .addFrame(new Frame(item1, 5))
    .addFrame(new Frame(item2, 5))
    .addFrame(new Frame(item3, 5));

gui.addAnimation(13, spinning);
```

### Async Item Loading
```java
AsyncItemLoader loader = new AsyncItemLoader(plugin);

loader.loadAndAddItems(gui,
    () -> fetchItemsFromDatabase(), // Async
    items -> gui.addPaginatedItems(items, null) // Sync
);
```

## Configuration

### Packet GUI Configuration
```java
PacketGuiConfig config = new PacketGuiConfig()
    .validationLevel(ValidationLevel.PACKET)
    .minClickDelayMs(50)              // Minimum time between clicks
    .maxClicksPerSecond(20)            // Maximum click rate
    .sessionTimeoutMs(300000)          // 5 minute timeout
    .autoRollbackOnViolation(true)     // Auto-correct desyncs
    .logViolations(true)               // Log exploit attempts
    .kickOnViolation(false)            // Don't auto-kick
    .violationKickThreshold(10);       // Kick after 10 violations

PacketGui gui = new PacketGui("shop")
    .config(config)
    .register(manager);
```

### Preset Configurations
```java
// Lenient (for trusted players)
config = PacketGuiConfig.lenient();

// Normal (recommended for most GUIs)
config = PacketGuiConfig.normal();

// Strict (for high-value operations)
config = PacketGuiConfig.strict();
```

## Performance

- **Memory**: ~5-10 KB per active GUI
- **CPU**: Minimal overhead with centralized scheduler
- **Validation**:
  - BASIC: 0ms overhead
  - PACKET: ~1-2ms per click
  - ADVANCED: ~3-5ms per click
- **Scalability**: Tested with 50+ concurrent packet GUIs

## Documentation

- 📖 [Packet GUI Guide](PACKET_GUI_GUIDE.md) - Comprehensive packet GUI documentation
- 🔍 [API Reference](API_REFERENCE.md) - Complete API documentation
- 💡 [Examples](EXAMPLES.md) - Practical code examples
- 📝 [Changelog](CHANGELOG.md) - Version history

## Complete Examples

See the [examples/](examples/) directory for working examples:

- `BasicGuiExample.java` - Simple GUI creation
- `PacketShopExample.java` - Secure shop with anti-dupe
- `AnimatedMenuExample.java` - Animated navigation menu
- `PaginationExample.java` - Paginated item listing
- `TradingSystemExample.java` - Player-to-player trading
- `BankVaultExample.java` - Secure bank vault GUI
- `ConfirmationExample.java` - Confirmation dialogs

## Requirements

- **Java**: 8 or higher
- **Spigot/Paper**: 1.8.8 - 1.21+
- **PacketEvents**: 2.6.0+ (automatically shaded for packet GUIs)

## Migration from AuroraGui to PacketGui

Migration is simple - just change the class and add validation level:

```java
// Before (event-based)
AuroraGui gui = new AuroraGui("shop")
    .title("Shop")
    .addItem(13, item, handler);

// After (packet-based with anti-dupe)
PacketGui gui = new PacketGui("shop")
    .title("Shop")
    .validationLevel(ValidationLevel.PACKET)  // Only addition!
    .addItem(13, item, handler);
```

All other features work identically!

## API Structure

```
dev.aurora
├── GUI
│   ├── AuroraGui              # Event-based GUI
│   └── IGui                   # Common GUI interface
├── Packet
│   ├── API
│   │   ├── IPacketGui         # Packet GUI interface
│   │   ├── ValidationLevel    # Validation level enum
│   │   └── PacketGuiConfig    # Configuration
│   ├── Core
│   │   ├── PacketGui          # Packet-based GUI
│   │   ├── PacketGuiRegistry  # Active GUI tracking
│   │   └── PacketEventManager # PacketEvents integration
│   ├── Validation
│   │   ├── AntiDupeValidator  # Master validator
│   │   ├── ServerSideInventory # Truth tracking
│   │   ├── ClickValidator     # Timing validation
│   │   ├── CursorTracker      # Cursor validation
│   │   └── ViolationLogger    # Logging
│   └── Handler
│       ├── ClickPacketHandler # Click interception
│       └── ClosePacketHandler # Close interception
├── Manager
│   ├── GuiManager             # GUI lifecycle
│   └── AnimationScheduler     # Animation system
├── Builder
│   └── GuiBuilder             # Fluent builder
├── Struct
│   ├── Animation              # Animation types
│   ├── Condition              # Click conditions
│   └── Cooldown               # Click cooldowns
└── Utilities
    └── Items/ItemBuilder      # Item creation
```

## Best Practices

1. **Enable packet support** once during plugin startup with `manager.enablePacketSupport()`
2. **Use BASIC** for navigation menus and display-only GUIs
3. **Use PACKET** for shops and player interactions
4. **Use ADVANCED** for economy, banks, and high-value items
5. **Monitor violation logs** to detect exploit attempts
6. **Clean up resources** when your plugin disables (handled automatically)
7. **Test thoroughly** on your target Minecraft version

## Troubleshooting

### PacketEvents Dependency Issues
Ensure PacketEvents 2.6.0+ is properly shaded into your JAR:

```xml
<dependency>
    <groupId>com.github.retrooper</groupId>
    <artifactId>packetevents-spigot</artifactId>
    <version>2.6.0</version>
    <scope>compile</scope>
</dependency>
```

### Compilation Errors
The library requires Java 8+ and Spigot API 1.16.5+. See pom.xml for complete dependencies.

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

- **Issues**: [GitHub Issues](https://github.com/Unforgoten1/AuroraGuis/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Unforgoten1/AuroraGuis/discussions)

## Credits

- **PacketEvents** by Retrooper - Packet manipulation library
- **Spigot/Paper** - Bukkit API implementation

---

**AuroraGuis** - Secure, powerful, and easy-to-use GUI library for Minecraft plugins
