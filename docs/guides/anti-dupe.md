# Anti-Dupe Security Guide

Comprehensive guide to protecting your server from inventory duplication exploits using PacketGui.

## Understanding Dupe Exploits

### Common Duplication Methods

1. **Rapid Clicking** - Click faster than server can process
2. **Cursor Manipulation** - Modify item on cursor
3. **Packet Injection** - Send fake packets
4. **Transaction Abuse** - Manipulate transaction confirmations
5. **NBT Editing** - Modify item NBT data
6. **Shift-Click Loops** - Rapidly shift-click same item
7. **Close Desync** - Manipulate inventory state on close
8. **Session Hijacking** - Exploit stale sessions

### Why Regular GUIs Fail

```
Player Action → Bukkit Event → Handler

Problems:
- Packets already processed by Bukkit
- No validation of cursor state
- No transaction verification
- No session management
```

### How PacketGui Protects

```
Player Action → Packet Intercept → Validate → Bukkit Event → Handler

Protection:
✅ Pre-Bukkit packet validation
✅ Server-side truth tracking
✅ Transaction verification
✅ Session monitoring
```

## Security Layers

### Layer 1: Packet Interception

**Validates packets BEFORE Bukkit processes them.**

```java
PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.PACKET)
    .config(PacketGuiConfig.normal()
        .minClickDelayMs(50)        // Minimum time between clicks
        .maxClicksPerSecond(20)     // Maximum click rate
    )
    .register(manager);
```

**Detects:**
- Click timing violations (< 50ms)
- Click spam (> 20/second)
- Out-of-bounds slot access

### Layer 2: Server-Side Truth

**Maintains authoritative inventory state.**

```java
PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.PACKET)
    .config(PacketGuiConfig.normal()
        .autoRollbackOnViolation(true)  // Auto-fix desyncs
    )
    .register(manager);
```

**Detects:**
- Cursor amount changes
- Cursor type changes
- Close desync

### Layer 3: Transaction Verification

**Validates every transaction with SHA-256 fingerprinting.**

```java
PacketGui bank = new PacketGui("bank")
    .validationLevel(ValidationLevel.ADVANCED)
    .config(PacketGuiConfig.strict())
    .register(manager);
```

**Detects:**
- NBT injection
- Shift-click loops
- Transaction mismatches

### Layer 4: Session Monitoring

**Tracks GUI sessions and detects anomalies.**

```java
PacketGui vault = new PacketGui("vault")
    .validationLevel(ValidationLevel.ADVANCED)
    .config(PacketGuiConfig.strict()
        .sessionTimeoutMs(180000)      // 3 minute timeout
        .detectStaleSession(true)       // Enable monitoring
        .forceCloseOnTimeout(true)      // Auto-close stale
    )
    .register(manager);
```

**Detects:**
- Missing close packets
- Stale sessions
- Session timeout

## Choosing Validation Levels

### Decision Matrix

| Use Case | Validation Level | Reasoning |
|----------|-----------------|-----------|
| Display menu | BASIC | No transactions |
| Standard shop | PACKET | Good protection, low overhead |
| Bank vault | ADVANCED | Maximum security needed |
| Trading system | ADVANCED | High-value transactions |
| Reward GUI | PACKET | Moderate protection |

### Configuration Matrix

| Server Type | Recommended Config | Reasoning |
|-------------|-------------------|-----------|
| Survival | PACKET + normal() | Balance security/performance |
| Economy-heavy | ADVANCED + strict() | High-value items |
| Creative | BASIC | No dupe risk |
| Minigames | PACKET + lenient() | Fast-paced |

## Implementation Strategies

### Strategy 1: Layered Approach

```java
public class SecureShopSystem {
    private final GuiManager manager;

    public SecureShopSystem(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
        manager.enablePacketSupport();
    }

    // Display menu - BASIC
    public void openMainMenu(Player player) {
        AuroraGui menu = new AuroraGui("main-menu")
            .title("&6Main Menu")
            .register(manager);
        menu.open(player);
    }

    // Standard shop - PACKET
    public void openShop(Player player) {
        PacketGui shop = new PacketGui("shop")
            .title("&6Shop")
            .validationLevel(ValidationLevel.PACKET)
            .config(PacketGuiConfig.normal())
            .register(manager);
        shop.open(player);
    }

    // Bank vault - ADVANCED
    public void openBank(Player player) {
        PacketGui bank = new PacketGui("bank")
            .title("&6Bank")
            .validationLevel(ValidationLevel.ADVANCED)
            .config(PacketGuiConfig.strict())
            .register(manager);
        bank.open(player);
    }
}
```

### Strategy 2: Violation Tracking

```java
public class ViolationTracker {
    private final Map<UUID, List<ViolationRecord>> violations = new HashMap<>();

    public void recordViolation(Player player, IPacketGui.ExploitType type) {
        UUID uuid = player.getUniqueId();
        ViolationRecord record = new ViolationRecord(type, System.currentTimeMillis());

        violations.computeIfAbsent(uuid, k -> new ArrayList<>()).add(record);

        // Take action based on history
        int recentCount = getRecentViolationCount(uuid, 60000); // Last minute
        if (recentCount >= 5) {
            player.kickPlayer("§cToo many violations");
        } else if (recentCount >= 3) {
            player.sendMessage("§cWarning: Suspicious activity detected");
        }
    }

    private int getRecentViolationCount(UUID uuid, long timeWindow) {
        List<ViolationRecord> records = violations.get(uuid);
        if (records == null) return 0;

        long cutoff = System.currentTimeMillis() - timeWindow;
        return (int) records.stream()
            .filter(r -> r.timestamp >= cutoff)
            .count();
    }

    private static class ViolationRecord {
        final IPacketGui.ExploitType type;
        final long timestamp;

        ViolationRecord(IPacketGui.ExploitType type, long timestamp) {
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
```

### Strategy 3: Dynamic Validation

```java
public class DynamicShop {
    private final GuiManager manager;

    public void openShop(Player player) {
        // Adjust validation based on player trust level
        ValidationLevel level = getValidationLevel(player);

        PacketGui shop = new PacketGui("shop")
            .title("&6Shop")
            .validationLevel(level)
            .register(manager);

        shop.open(player);
    }

    private ValidationLevel getValidationLevel(Player player) {
        if (player.hasPermission("shop.trusted")) {
            return ValidationLevel.PACKET;  // Lenient for trusted
        } else if (isNewPlayer(player)) {
            return ValidationLevel.ADVANCED; // Strict for new players
        } else {
            return ValidationLevel.PACKET;   // Normal for regular players
        }
    }

    private boolean isNewPlayer(Player player) {
        // Check if player joined recently
        return player.getFirstPlayed() > System.currentTimeMillis() - 86400000; // 24 hours
    }
}
```

## Monitoring and Logging

### Log Analysis

```java
public class ViolationLogger {
    private final File logFile;
    private final PrintWriter writer;

    public ViolationLogger(JavaPlugin plugin) throws IOException {
        this.logFile = new File(plugin.getDataFolder(), "violations.log");
        this.writer = new PrintWriter(new FileWriter(logFile, true));
    }

    public void log(Player player, IPacketGui.ExploitType type, String details) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String entry = String.format("[%s] %s by %s (UUID: %s) - Severity: %d - %s",
            timestamp,
            type.name(),
            player.getName(),
            player.getUniqueId(),
            type.getSeverity(),
            details
        );

        writer.println(entry);
        writer.flush();
    }

    public void close() {
        writer.close();
    }
}
```

### Real-time Alerts

```java
public class AlertSystem {
    private final JavaPlugin plugin;
    private final String webhookUrl; // Discord webhook

    public void alertAdmins(Player violator, IPacketGui.ExploitType type) {
        String message = String.format(
            "§c§l[SECURITY] §c%s attempted %s (Severity: %d)",
            violator.getName(),
            type.name(),
            type.getSeverity()
        );

        // In-game alerts
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.hasPermission("aurora.alerts"))
            .forEach(admin -> {
                admin.sendMessage(message);
                admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
            });

        // Discord webhook
        if (webhookUrl != null && type.getSeverity() >= 4) {
            sendDiscordAlert(violator, type);
        }
    }

    private void sendDiscordAlert(Player player, IPacketGui.ExploitType type) {
        // Implement Discord webhook
    }
}
```

## Testing Security

### Test Cases

```java
public class SecurityTests {

    @Test
    public void testRapidClicking() {
        // Simulate rapid clicking
        for (int i = 0; i < 100; i++) {
            clickItem(player, 13);
        }

        // Verify violations logged
        assertTrue(violationCount > 0);
    }

    @Test
    public void testCursorManipulation() {
        // Attempt to modify cursor
        modifyCursor(player, Material.DIAMOND, 64);

        // Verify detected and rolled back
        assertTrue(wasRolledBack());
    }

    @Test
    public void testTransactionVerification() {
        // Attempt NBT injection
        injectNBT(player, item);

        // Verify transaction cancelled
        assertTrue(transactionCancelled());
    }
}
```

## Best Practices

### ✅ DO:

1. **Enable packet support**
   ```java
   manager.enablePacketSupport();
   ```

2. **Use appropriate validation levels**
3. **Monitor violation logs regularly**
4. **Implement progressive punishment**
5. **Test security before production**
6. **Keep AuroraGuis updated**

### ❌ DON'T:

1. **Don't use ADVANCED everywhere** - Performance impact
2. **Don't ignore violations** - They indicate real attempts
3. **Don't auto-ban without testing** - False positives possible
4. **Don't skip logging** - Need audit trail
5. **Don't forget backups** - In case of issues

## Incident Response

### When Violations Occur

1. **Review logs** - Determine exploit type
2. **Check player history** - First time or repeat offender?
3. **Analyze pattern** - Targeted or accidental?
4. **Take action** - Warn, kick, or ban
5. **Update config** - Adjust thresholds if needed

### Recovery

```java
public class IncidentResponse {
    public void handleIncident(Player player, IPacketGui.ExploitType type) {
        // 1. Immediate action
        player.closeInventory();

        // 2. Log incident
        logViolation(player, type);

        // 3. Alert admins
        alertAdmins(player, type);

        // 4. Take corrective action
        if (type.getSeverity() >= 5) {
            tempBan(player, 24); // 24 hour ban
        } else if (type.getSeverity() >= 4) {
            kick(player);
        } else {
            warn(player);
        }

        // 5. Roll back if needed
        if (itemsDuplicated()) {
            rollbackInventory(player);
        }
    }
}
```

## Further Reading

- **[Packet GUIs](../features/packet-guis.md)** - Feature overview
- **[Performance Guide](performance.md)** - Optimization tips
- **[API Reference](../api/packet-gui.md)** - Complete API docs

---

**Need help?** Join our Discord for security support!
