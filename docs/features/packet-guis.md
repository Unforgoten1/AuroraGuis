# Packet GUIs

Enterprise-grade anti-duplication protection through packet-level validation.

## Overview

**PacketGui** extends AuroraGui with packet interception and validation, providing:

- **Packet-Level Security** - Intercepts inventory packets before Bukkit processes them
- **11 Exploit Detection Types** - From click spam to NBT injection
- **Configurable Validation Levels** - BASIC, PACKET, and ADVANCED modes
- **Server-Side Truth Tracking** - Maintains authoritative inventory state
- **Violation Monitoring** - Comprehensive logging and alerting

## When to Use Packet GUIs

### ✅ Use PacketGui For:

- **Economy Systems** - Shops, banks, currency exchange
- **Reward Systems** - Daily rewards, crates, lootboxes
- **Storage Systems** - Personal vaults, shared storage
- **Trading Systems** - Player-to-player trading
- **Any GUI where item duplication would cause problems**

### ❌ Use AuroraGui For:

- **Navigation Menus** - Server selector, teleport menus
- **Information Displays** - Help menus, rules, tutorials
- **Cosmetic Selectors** - Kit selection, cosmetic items
- **Display-Only GUIs** - No item transactions

## Quick Start

### Step 1: Enable Packet Support

Enable packet support once during plugin initialization:

```java
public class YourPlugin extends JavaPlugin {
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        guiManager = new GuiManager(this);

        // Enable packet support for PacketGuis
        guiManager.enablePacketSupport();
    }
}
```

### Step 2: Create a PacketGui

```java
PacketGui shop = new PacketGui("secure-shop")
    .title("&6Secure Shop")
    .rows(6)
    .validationLevel(ValidationLevel.PACKET)
    .addItem(13, createDiamondItem(), event -> {
        Player player = (Player) event.getWhoClicked();

        // This transaction is protected from duplication
        economy.withdraw(player, 1000);
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
        player.sendMessage("§aPurchased 64 diamonds!");
    })
    .register(guiManager);

shop.open(player);
```

## Validation Levels

### BASIC - No Validation

**Overhead:** 0ms
**Protection:** Bukkit events only

```java
PacketGui menu = new PacketGui("menu")
    .validationLevel(ValidationLevel.BASIC)
    .register(manager);
```

**Use When:**
- Testing the library
- Performance is absolutely critical
- You don't need anti-dupe protection

### PACKET - Standard Protection (Recommended)

**Overhead:** ~1-2ms per click
**Protection:**
- ✅ Click timing validation (min 50ms delay)
- ✅ Click rate limiting (max 20/second)
- ✅ Cursor duplication detection
- ✅ Cursor swap detection
- ✅ Invalid slot access prevention
- ✅ Close desync detection

```java
PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.PACKET)
    .register(manager);
```

**Use When:**
- Most shops and trading GUIs
- Good balance of security and performance
- Standard economy protection

### ADVANCED - Maximum Security

**Overhead:** ~3-5ms per click
**Protection:**
- ✅ All PACKET protections
- ✅ NBT injection detection (SHA-256 fingerprinting)
- ✅ Shift-click loop prevention
- ✅ Transaction state verification

```java
PacketGui bank = new PacketGui("bank")
    .validationLevel(ValidationLevel.ADVANCED)
    .config(PacketGuiConfig.strict())
    .register(manager);
```

**Use When:**
- Bank vaults with valuable items
- Premium shops
- High-value currency systems
- Maximum security required

## Anti-Dupe Protection

### The 11 Exploit Types

| Exploit | Layer | Severity | Detection Method |
|---------|-------|----------|------------------|
| CLICK_DELAY | 1 | 2 | Timing < 50ms |
| CLICK_SPAM | 1 | 3 | Rate > 20/second |
| INVALID_SLOT | 1 | 3 | Slot out of bounds |
| CURSOR_DUPLICATION | 2 | 4 | Cursor amount increased |
| CURSOR_SWAP | 2 | 3 | Cursor type changed |
| CLOSE_DESYNC | 2 | 3 | State mismatch on close |
| NBT_INJECTION | 3 | 5 | SHA-256 hash mismatch |
| SHIFT_CLICK_LOOP | 3 | 4 | Duplicate fingerprint |
| TRANSACTION_MISMATCH | 3 | 4 | Expected ≠ actual state |
| NO_CLOSE_PACKET | 4 | 5 | Missing close packet |
| STALE_SESSION | 4 | 4 | Session timeout |

### How Protection Works

```
Player Click
    ↓
┌─────────────────────────────────────┐
│ Layer 1: Packet Interception       │
│ - Check click timing (50ms min)    │
│ - Rate limit (20 clicks/sec max)   │
│ - Validate slot bounds              │
└─────────────────────────────────────┘
    ↓ [Cancel if invalid]
┌─────────────────────────────────────┐
│ Layer 2: Server-Side Truth         │
│ - Track cursor state                │
│ - Detect amount changes             │
│ - Detect type swaps                 │
└─────────────────────────────────────┘
    ↓ [Rollback if violation]
┌─────────────────────────────────────┐
│ Layer 3: Transaction Verification  │
│ - SHA-256 item fingerprinting       │
│ - Shift-click duplicate detection   │
│ - State verification                │
└─────────────────────────────────────┘
    ↓ [Cancel transaction if invalid]
┌─────────────────────────────────────┐
│ Layer 4: Session Monitoring        │
│ - Track open/close packets          │
│ - Detect session timeout            │
│ - Force cleanup                     │
└─────────────────────────────────────┘
    ↓
Bukkit InventoryClickEvent
    ↓
Your Click Handler
```

## Configuration

### Preset Configurations

```java
// Lenient - For trusted players
PacketGuiConfig.lenient()
    .minClickDelayMs(30)
    .maxClicksPerSecond(25)
    .sessionTimeoutMs(600000); // 10 minutes

// Normal - Recommended default
PacketGuiConfig.normal()
    .minClickDelayMs(50)
    .maxClicksPerSecond(20)
    .sessionTimeoutMs(300000); // 5 minutes

// Strict - Maximum security
PacketGuiConfig.strict()
    .minClickDelayMs(100)
    .maxClicksPerSecond(15)
    .sessionTimeoutMs(180000) // 3 minutes
    .kickOnViolation(true);
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

PacketGui gui = new PacketGui("shop")
    .config(config)
    .register(manager);
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `validationLevel` | ValidationLevel | PACKET | Security level |
| `minClickDelayMs` | long | 50 | Minimum ms between clicks |
| `maxClicksPerSecond` | int | 20 | Maximum click rate |
| `sessionTimeoutMs` | long | 300000 | Session timeout (5 min) |
| `inactivityCheckIntervalMs` | long | 30000 | Check interval (30 sec) |
| `forceCloseOnTimeout` | boolean | true | Auto-close on timeout |
| `detectStaleSession` | boolean | true | Enable monitoring |
| `autoRollbackOnViolation` | boolean | true | Auto-fix desyncs |
| `logViolations` | boolean | true | Log to file |
| `kickOnViolation` | boolean | false | Auto-kick players |
| `violationKickThreshold` | int | 10 | Kicks after N violations |

## Violation Handling

### Adding a Violation Handler

```java
PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.ADVANCED)
    .onViolation((player, exploitType) -> {
        // Log violation
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

### Progressive Punishment System

```java
Map<UUID, Integer> violations = new HashMap<>();

PacketGui bank = new PacketGui("bank")
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

### Violation Logs

Violations are automatically logged to:
```
plugins/YourPlugin/logs/violations.log
```

Example log entry:
```
[2026-02-09 14:23:45] CURSOR_DUPLICATION by Player123 in GUI 'shop' (Severity: 4)
Details: Cursor amount increased from 1 to 64
```

## Complete Example

### Secure Economy Shop

```java
public class SecureShop {
    private final GuiManager manager;
    private final Economy economy;

    public SecureShop(JavaPlugin plugin, Economy economy) {
        this.manager = new GuiManager(plugin);
        this.economy = economy;

        // Enable packet support
        manager.enablePacketSupport();
    }

    public void openShop(Player player) {
        PacketGui shop = new PacketGui("secure-shop")
            .title("&6&lSecure Shop")
            .rows(6)
            .validationLevel(ValidationLevel.PACKET)
            .config(PacketGuiConfig.normal())
            .setBorder(BorderType.FULL, createBorder())

            // Diamond section
            .addItem(10, createShopItem(Material.DIAMOND, 64, 1000),
                event -> handlePurchase(player, Material.DIAMOND, 64, 1000))

            // Gold section
            .addItem(19, createShopItem(Material.GOLD_INGOT, 64, 500),
                event -> handlePurchase(player, Material.GOLD_INGOT, 64, 500))

            // Iron section
            .addItem(28, createShopItem(Material.IRON_INGOT, 64, 200),
                event -> handlePurchase(player, Material.IRON_INGOT, 64, 200))

            // Violation handler
            .onViolation((p, exploitType) -> {
                plugin.getLogger().severe(
                    p.getName() + " attempted " + exploitType + " in shop!"
                );

                // Alert admins
                Bukkit.getOnlinePlayers().stream()
                    .filter(admin -> admin.hasPermission("shop.admin"))
                    .forEach(admin -> admin.sendMessage(
                        "§c§l[ALERT] " + p.getName() + " attempted " + exploitType
                    ));
            })

            .register(manager);

        shop.open(player);
    }

    private void handlePurchase(Player player, Material material, int amount, int price) {
        if (economy.getBalance(player) < price) {
            player.sendMessage("§cInsufficient funds! Need $" + price);
            return;
        }

        // Protected transaction
        economy.withdrawPlayer(player, price);
        player.getInventory().addItem(new ItemStack(material, amount));
        player.sendMessage("§aPurchased " + amount + "x " + material.name() + " for $" + price);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    private ItemStack createShopItem(Material material, int amount, int price) {
        return new ItemBuilder(material)
            .name("&e" + material.name())
            .lore(
                "&7Amount: &f" + amount,
                "&7Price: &6$" + price,
                "",
                "&aClick to purchase!"
            )
            .build();
    }

    private ItemStack createBorder() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build();
    }
}
```

## Performance Considerations

### Benchmark Results

Testing: Paper 1.16.5, 50 concurrent GUIs, 25 players

| Level | Avg Overhead | Memory | CPU Usage |
|-------|--------------|--------|-----------|
| BASIC | 0ms | 5 KB | <0.1% |
| PACKET | 1.5ms | 8 KB | 0.3% |
| ADVANCED | 4ms | 10 KB | 0.8% |

### Optimization Tips

**1. Choose appropriate validation level**
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
    .inactivityCheckIntervalMs(60000); // Every minute
```

**3. Disable unnecessary features**
```java
// Disable session monitoring if not needed
PacketGuiConfig config = new PacketGuiConfig()
    .detectStaleSession(false);
```

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

3. **Monitor violation logs regularly**
   ```
   tail -f plugins/YourPlugin/logs/violations.log
   ```

4. **Test with trusted players first**
5. **Adjust config based on server performance**
6. **Use ADVANCED only for critical GUIs**

### ❌ DON'T:

1. **Don't use ADVANCED for everything** - Unnecessary overhead
2. **Don't ignore violation logs** - You'll miss real exploits
3. **Don't auto-kick without testing** - Possible false positives
4. **Don't use packet GUIs for display menus** - Waste of resources
5. **Don't forget to enable packet support** - PacketGui won't work

## Migration from AuroraGui

### Step 1: Enable Packet Support

```java
// In your onEnable()
guiManager.enablePacketSupport();
```

### Step 2: Change Class

```java
// Before
AuroraGui shop = new AuroraGui("shop")
    .title("Shop")
    .addItem(13, item, handler)
    .register(manager);

// After
PacketGui shop = new PacketGui("shop")
    .title("Shop")
    .validationLevel(ValidationLevel.PACKET) // Only change!
    .addItem(13, item, handler)
    .register(manager);
```

### Step 3: Test and Monitor

- Monitor violation logs
- Adjust configuration as needed
- Gradually increase strictness

## Troubleshooting

### "PacketEvents not initialized"

```java
// Solution: Enable packet support in onEnable()
guiManager.enablePacketSupport();
```

### Too many false positives

```java
// Solution: Use lenient config
.config(PacketGuiConfig.lenient())
```

### Violations not logged

```java
// Solution: Ensure logging is enabled
.config(new PacketGuiConfig().logViolations(true))
```

### Performance issues

```java
// Solution: Reduce strictness or monitoring
.validationLevel(ValidationLevel.PACKET)
.config(new PacketGuiConfig()
    .inactivityCheckIntervalMs(60000)
)
```

## Next Steps

- **[Anti-Dupe Guide](../guides/anti-dupe.md)** - Deep dive into security
- **[Performance Guide](../guides/performance.md)** - Optimization tips
- **[API Reference](../api/packet-gui.md)** - Complete API documentation
- **[Examples](../examples/code-examples.md)** - More code examples

---

**Need help?** Check the [API Reference](../api/packet-gui.md) or open an issue on GitHub.
