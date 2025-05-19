# Packet GUI Complete Guide

This guide covers everything you need to know about AuroraGuis' packet-based GUI system with anti-duplication protection.

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Validation Levels](#validation-levels)
4. [Anti-Dupe Protection](#anti-dupe-protection)
5. [Configuration](#configuration)
6. [Violation Handling](#violation-handling)
7. [Advanced Usage](#advanced-usage)
8. [Performance](#performance)
9. [Best Practices](#best-practices)

---

## Introduction

### What are Packet GUIs?

Packet GUIs intercept and validate inventory packets **before** Bukkit processes them, enabling:
- **Lower-level validation** to detect exploits Bukkit events can't catch
- **Server-side truth tracking** to prevent item duplication
- **Configurable security** levels based on your needs

### Why Use Packet GUIs?

**Use Packet GUIs for:**
- 💰 Economy systems (shops, banks, trading)
- 🎁 Reward systems
- 📦 Storage systems
- 🔄 Player-to-player trading
- Any GUI where item duplication would be problematic

**Use Regular AuroraGuis for:**
- 📋 Navigation menus
- ℹ️ Information displays
- 🎨 Cosmetic selectors
- Any display-only GUI

### How It Works

```
Player Click
    ↓
PacketEvents: Intercept CLICK_WINDOW packet (Layer 1)
    ↓
Validate timing, cursor state, slot bounds
    ↓
[Cancel packet if validation fails]
    ↓
If valid → Bukkit InventoryClickEvent (Layer 2)
    ↓
Your click handler executes
    ↓
Transaction validation (ADVANCED mode)
    ↓
Update server-side truth
```

---

## Getting Started

### Step 1: Enable Packet Support

Enable packet support **once** during plugin startup:

```java
public class YourPlugin extends JavaPlugin {
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        guiManager = new GuiManager(this);

        // Enable packet support
        guiManager.enablePacketSupport();
    }
}
```

### Step 2: Create Your First Packet GUI

```java
PacketGui shop = new PacketGui("shop")
    .title("&6Shop")
    .rows(3)
    .validationLevel(ValidationLevel.PACKET)
    .addItem(13, createDiamondItem(), event -> {
        Player player = (Player) event.getWhoClicked();
        // Handle purchase - protected from duplication!
        economy.withdraw(player, 1000);
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
        player.sendMessage("§aPurchased 64 diamonds for $1000!");
    })
    .register(guiManager);

shop.open(player);
```

### Step 3: Choose Validation Level

```java
// BASIC - No packet validation (same as AuroraGui)
.validationLevel(ValidationLevel.BASIC)

// PACKET - Timing + cursor validation (recommended)
.validationLevel(ValidationLevel.PACKET)

// ADVANCED - Full protection + fingerprinting
.validationLevel(ValidationLevel.ADVANCED)
```

---

## Validation Levels

### BASIC (0ms overhead)

**Protection:**
- Bukkit events only
- No packet interception

**Use When:**
- You don't need anti-dupe protection
- Performance is absolutely critical
- You're just trying the library

**Example:**
```java
PacketGui menu = new PacketGui("menu")
    .validationLevel(ValidationLevel.BASIC)
    .addItem(10, homeButton, this::goHome)
    .register(manager);
```

### PACKET (~1-2ms overhead) **[Recommended]**

**Protection:**
- ✅ Click timing validation (min 50ms delay)
- ✅ Click rate limiting (max 20/second)
- ✅ Cursor duplication detection
- ✅ Cursor swap detection
- ✅ Invalid slot access prevention
- ✅ Close desync detection

**Use When:**
- You need good protection
- You want low overhead
- Most shops and trading GUIs

**Example:**
```java
PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.PACKET)
    .addItem(13, swordItem, this::handlePurchase)
    .register(manager);
```

### ADVANCED (~3-5ms overhead)

**Protection:**
- ✅ All PACKET protections
- ✅ NBT injection detection (SHA-256 fingerprinting)
- ✅ Shift-click loop prevention
- ✅ Transaction state verification

**Use When:**
- You need maximum security
- Economy items are valuable
- Bank vaults, premium shops

**Example:**
```java
PacketGui bank = new PacketGui("bank")
    .validationLevel(ValidationLevel.ADVANCED)
    .config(PacketGuiConfig.strict())
    .addItem(13, valuableItem, this::handleWithdraw)
    .register(manager);
```

---

## Anti-Dupe Protection

### The 11 Exploit Types

| Exploit Type | Layer | Severity | Description |
|--------------|-------|----------|-------------|
| CLICK_DELAY | 1 | 2 | Rapid clicking < 50ms |
| CLICK_SPAM | 1 | 3 | > 20 clicks/second |
| INVALID_SLOT | 1 | 3 | Out-of-bounds access |
| CURSOR_DUPLICATION | 2 | 4 | Cursor amount increase |
| CURSOR_SWAP | 2 | 3 | Cursor type change |
| CLOSE_DESYNC | 2 | 3 | Inventory state mismatch |
| NBT_INJECTION | 3 | 5 | NBT data tampering |
| SHIFT_CLICK_LOOP | 3 | 4 | Shift-click duplication |
| TRANSACTION_MISMATCH | 3 | 4 | State verification fail |
| NO_CLOSE_PACKET | 4 | 5 | Withhold close packet |
| STALE_SESSION | 4 | 4 | Session timeout |

### How Each Protection Works

#### Layer 1: Packet Interception

**CLICK_DELAY** - Prevents rapid clicking
```
EXPLOIT: 100 clicks in 1 second
DETECTION: Time between clicks < 50ms
RESULT: Packet cancelled
```

**CLICK_SPAM** - Rate limiting
```
EXPLOIT: 50 clicks per second
DETECTION: Rate > 20/second
RESULT: Excess clicks blocked
```

**INVALID_SLOT** - Bounds checking
```
EXPLOIT: Access slot 999
DETECTION: Slot >= GUI size
RESULT: Packet cancelled
```

#### Layer 2: Server-Side Truth

**CURSOR_DUPLICATION** - Amount validation
```
EXPLOIT: Cursor 1 → 64 diamonds
DETECTION: Amount increased
RESULT: Rollback + violation
```

**CURSOR_SWAP** - Type validation
```
EXPLOIT: Cursor dirt → diamond
DETECTION: Type changed
RESULT: Rollback + violation
```

**CLOSE_DESYNC** - State validation
```
EXPLOIT: Modified inventory on close
DETECTION: State != server truth
RESULT: Force resync
```

#### Layer 3: Transaction Verification

**NBT_INJECTION** - Fingerprinting
```
EXPLOIT: Tampered item NBT
DETECTION: SHA-256 hash mismatch
RESULT: Transaction cancelled
```

**SHIFT_CLICK_LOOP** - Duplicate detection
```
EXPLOIT: Rapid shift-click same item
DETECTION: Duplicate fingerprint
RESULT: Subsequent clicks blocked
```

**TRANSACTION_MISMATCH** - State verification
```
EXPLOIT: Client claims different result
DETECTION: Expected != actual state
RESULT: Rollback transaction
```

#### Layer 4: Session Monitoring

**NO_CLOSE_PACKET** - Withhold detection
```
EXPLOIT: Open new GUI without closing
DETECTION: New inventory without close
RESULT: Force close + violation
```

**STALE_SESSION** - Timeout detection
```
EXPLOIT: GUI open for hours
DETECTION: Inactivity > timeout
RESULT: Force close + optional kick
```

---

## Configuration

### Preset Configurations

```java
// Lenient - For trusted players
PacketGuiConfig.lenient()
    .minClickDelayMs(30)
    .maxClicksPerSecond(25)
    .sessionTimeoutMs(600000)  // 10 minutes
    .kickOnViolation(false)

// Normal - Recommended default
PacketGuiConfig.normal()
    .minClickDelayMs(50)
    .maxClicksPerSecond(20)
    .sessionTimeoutMs(300000)  // 5 minutes
    .kickOnViolation(false)

// Strict - Maximum security
PacketGuiConfig.strict()
    .minClickDelayMs(100)
    .maxClicksPerSecond(15)
    .sessionTimeoutMs(180000)  // 3 minutes
    .kickOnViolation(true)
```

### Custom Configuration

```java
PacketGuiConfig config = new PacketGuiConfig()
    .validationLevel(ValidationLevel.PACKET)
    .minClickDelayMs(50)
    .maxClicksPerSecond(20)
    .sessionTimeoutMs(300000)
    .autoRollbackOnViolation(true)
    .logViolations(true)
    .kickOnViolation(false)
    .violationKickThreshold(10);
```

### All Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `validationLevel` | Enum | PACKET | Validation strictness |
| `minClickDelayMs` | long | 50 | Min ms between clicks |
| `maxClicksPerSecond` | int | 20 | Max clicks per second |
| `sessionTimeoutMs` | long | 300000 | Session timeout (5 min) |
| `inactivityCheckIntervalMs` | long | 30000 | Check interval (30 sec) |
| `forceCloseOnTimeout` | boolean | true | Auto-close on timeout |
| `detectStaleSession` | boolean | true | Enable monitoring |
| `autoRollbackOnViolation` | boolean | true | Auto-fix desyncs |
| `logViolations` | boolean | true | Log to file |
| `kickOnViolation` | boolean | false | Auto-kick players |
| `violationKickThreshold` | int | 10 | Kicks after N violations |

---

## Violation Handling

### Adding a Violation Handler

```java
PacketGui gui = new PacketGui("shop")
    .validationLevel(ValidationLevel.ADVANCED)
    .onViolation((player, exploitType) -> {
        // Log the violation
        plugin.getLogger().warning(
            player.getName() + " attempted " + exploitType
        );

        // Alert admins
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.hasPermission("aurora.admin"))
            .forEach(admin -> admin.sendMessage(
                "§c" + player.getName() + " attempted " + exploitType
            ));

        // Take action based on severity
        int severity = exploitType.getSeverity();
        if (severity >= 5) {
            player.kickPlayer("§cExploit attempt detected.");
        } else if (severity >= 4) {
            player.sendMessage("§cWarning: Suspicious activity detected.");
        }
    })
    .register(manager);
```

### Accessing Violation Logs

```java
// Get the violation logger
ViolationLogger logger = gui.getValidator().getViolationLogger();

// Get total violations for a player
int total = logger.getTotalViolations(player);

// Get violations by type
int cursorDupes = logger.getViolationCount(
    player,
    IPacketGui.ExploitType.CURSOR_DUPLICATION
);

// Clear violations
logger.clearViolations(player);
```

### Log Files

Violations are automatically logged to:
```
plugins/YourPlugin/logs/violations.log
```

Example log entry:
```
[2026-02-09 14:23:45] CURSOR_DUPLICATION by Player123 in GUI 'shop' (Severity: 4)
Details: Cursor amount increased from 1 to 64
```

---

## Advanced Usage

### Dynamic Validation Levels

```java
PacketGui shop = new PacketGui("shop")
    .title("&6Shop")
    .rows(6);

// Adjust based on player
if (player.hasPermission("shop.trusted")) {
    shop.validationLevel(ValidationLevel.PACKET);
} else {
    shop.validationLevel(ValidationLevel.ADVANCED);
}

shop.register(manager);
```

### Combining with Other Features

```java
PacketGui shop = new PacketGui("premium-shop")
    .title("&6&lPremium Shop")
    .rows(6)
    .validationLevel(ValidationLevel.ADVANCED)
    .config(PacketGuiConfig.strict())

    // Add borders
    .setBorder(BorderType.FULL, borderItem)

    // Add animations
    .addAnimation(4, new PulsingBorder(3, 1))

    // Add items with conditions and cooldowns
    .addItem(13, diamondItem,
        event -> handlePurchase(event),
        ClickCondition.requirePermission("shop.premium")
            .and(ClickCondition.requireLeftClick())
    )

    // Add violation handler
    .onViolation(this::handleViolation)

    .register(manager);
```

### Progressive Punishment System

```java
Map<UUID, Integer> violations = new HashMap<>();

PacketGui gui = new PacketGui("bank")
    .validationLevel(ValidationLevel.ADVANCED)
    .onViolation((player, exploitType) -> {
        UUID uuid = player.getUniqueId();
        int count = violations.merge(uuid, 1, Integer::sum);

        if (count == 1) {
            player.sendMessage("§eWarning: Suspicious activity detected.");
        } else if (count == 3) {
            player.sendMessage("§6Final warning! Next violation = kick.");
        } else if (count == 5) {
            player.kickPlayer("§cMultiple exploit attempts.");
        } else if (count >= 7) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                "Repeated exploit attempts",
                null,
                "AuroraGuis"
            );
        }
    })
    .register(manager);
```

---

## Performance

### Benchmark Results

Testing conditions:
- Server: Paper 1.16.5
- Concurrent GUIs: 50
- Players: 25

| Level | Avg Overhead | Memory | CPU Usage |
|-------|--------------|--------|-----------|
| BASIC | 0ms | 5 KB | <0.1% |
| PACKET | 1.5ms | 8 KB | 0.3% |
| ADVANCED | 4ms | 10 KB | 0.8% |

### Optimization Tips

**1. Use appropriate validation level**
```java
// Display menu - use BASIC
PacketGui menu = new PacketGui("menu")
    .validationLevel(ValidationLevel.BASIC);

// Shop - use PACKET
PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.PACKET);

// Bank - use ADVANCED
PacketGui bank = new PacketGui("bank")
    .validationLevel(ValidationLevel.ADVANCED);
```

**2. Adjust monitoring frequency**
```java
// Check less frequently on busy servers
PacketGuiConfig config = new PacketGuiConfig()
    .inactivityCheckIntervalMs(60000);  // Every minute
```

**3. Disable unnecessary features**
```java
// Disable session monitoring if not needed
PacketGuiConfig config = new PacketGuiConfig()
    .detectStaleSession(false);
```

---

## Best Practices

### ✅ DO:

1. **Enable packet support once on startup**
   ```java
   guiManager.enablePacketSupport();
   ```

2. **Use PACKET level for most GUIs**
   ```java
   .validationLevel(ValidationLevel.PACKET)
   ```

3. **Monitor violation logs**
   ```
   tail -f plugins/YourPlugin/logs/violations.log
   ```

4. **Test with trusted players first**
5. **Adjust config based on server performance**
6. **Use ADVANCED only for critical GUIs**

### ❌ DON'T:

1. **Don't use ADVANCED for everything** (unnecessary overhead)
2. **Don't ignore violation logs** (miss real exploits)
3. **Don't auto-kick without testing** (false positives)
4. **Don't use packet GUIs for display menus** (waste of resources)
5. **Don't forget to enable packet support** (won't work)

---

## Migration Guide

### From AuroraGui to PacketGui

**Step 1:** Add packet support to plugin
```java
// In your onEnable()
guiManager.enablePacketSupport();
```

**Step 2:** Change class and add validation
```java
// Before
AuroraGui shop = new AuroraGui("shop")
    .title("Shop")
    .addItem(13, item, handler);

// After
PacketGui shop = new PacketGui("shop")
    .title("Shop")
    .validationLevel(ValidationLevel.PACKET)  // Only change!
    .addItem(13, item, handler);
```

**Step 3:** Test thoroughly
- Monitor violation logs
- Adjust configuration as needed
- Gradually increase strictness

---

## Troubleshooting

### Common Issues

**Problem:** "PacketEvents not initialized"
```java
// Solution: Enable packet support
guiManager.enablePacketSupport();
```

**Problem:** Too many false positives
```java
// Solution: Use lenient config
.config(PacketGuiConfig.lenient())
```

**Problem:** Violations not logged
```java
// Solution: Ensure logging is enabled
.config(new PacketGuiConfig().logViolations(true))
```

**Problem:** Performance issues
```java
// Solution: Reduce strictness or monitoring
.validationLevel(ValidationLevel.PACKET)
.config(new PacketGuiConfig()
    .inactivityCheckIntervalMs(60000)
)
```

---

## Further Reading

- [Examples](EXAMPLES.md) - Practical code examples
- [API Reference](API_REFERENCE.md) - Complete API documentation
- [README](README.md) - Project overview
- [Changelog](CHANGELOG.md) - Version history

---

Need help? [Open an issue](https://github.com/Unforgoten1/AuroraGuis/issues)
