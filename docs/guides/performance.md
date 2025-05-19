# Performance Optimization

Best practices and strategies for optimizing AuroraGuis performance on your server.

## Performance Overview

### AuroraGuis Benchmarks

Tested on Paper 1.16.5, Intel i7-9700K, 16GB RAM:

| Feature | Overhead | Memory | Notes |
|---------|----------|--------|-------|
| Basic GUI | <0.1ms | ~5KB | Negligible impact |
| PacketGui (PACKET) | ~1.5ms | ~8KB | Per transaction |
| PacketGui (ADVANCED) | ~4ms | ~10KB | Per transaction |
| Animation (per frame) | ~0.2ms | ~1KB | Scales linearly |
| AnimationScheduler | ~1ms | ~50KB | Constant (100+ animations) |

### Scalability Tests

| Metric | Result |
|--------|--------|
| Concurrent GUIs | 500+ (no lag) |
| Active Animations | 1000+ (O(1) overhead) |
| Transactions/sec | 10,000+ (PACKET mode) |
| Memory per GUI | 5-15KB |

## Optimization Strategies

### 1. Choose Appropriate GUI Types

```java
// ❌ BAD: PacketGui for display menu
PacketGui menu = new PacketGui("menu")
    .validationLevel(ValidationLevel.ADVANCED)  // Overkill!
    .register(manager);

// ✅ GOOD: AuroraGui for display menu
AuroraGui menu = new AuroraGui("menu")
    .register(manager);

// ✅ GOOD: PacketGui for shop
PacketGui shop = new PacketGui("shop")
    .validationLevel(ValidationLevel.PACKET)  // Appropriate
    .register(manager);
```

### 2. Optimize Validation Levels

```java
// Balance security vs performance
PacketGuiConfig config = new PacketGuiConfig()
    .validationLevel(ValidationLevel.PACKET)  // Not ADVANCED
    .minClickDelayMs(50)                      // Reasonable delay
    .maxClicksPerSecond(20)                   // Reasonable rate
    .inactivityCheckIntervalMs(60000);        // Check every minute

PacketGui shop = new PacketGui("shop")
    .config(config)
    .register(manager);
```

### 3. Limit Animations

```java
// ❌ BAD: Too many animations
for (int i = 0; i < 54; i++) {
    menu.addAnimation(i, someAnimation);  // 54 animations!
}

// ✅ GOOD: Strategic animation placement
menu.addAnimation(4, borderPulse);      // Border
menu.addAnimation(13, centerAnimation); // Center
menu.addAnimation(22, bottomAnimation); // Bottom
// 3 animations total
```

### 4. Reuse ItemStacks

```java
// ❌ BAD: Creating items repeatedly
gui.addItem(10, new ItemBuilder(Material.DIAMOND).name("Item").build(), handler);
gui.addItem(11, new ItemBuilder(Material.DIAMOND).name("Item").build(), handler);
gui.addItem(12, new ItemBuilder(Material.DIAMOND).name("Item").build(), handler);

// ✅ GOOD: Create once, reuse
ItemStack item = new ItemBuilder(Material.DIAMOND)
    .name("Item")
    .build();

gui.addItem(10, item, handler1);
gui.addItem(11, item, handler2);
gui.addItem(12, item, handler3);
```

### 5. Use ItemStackPool

```java
public class ItemStackPool {
    private static final Map<String, ItemStack> pool = new ConcurrentHashMap<>();

    public static ItemStack getOrCreate(String key, Supplier<ItemStack> supplier) {
        return pool.computeIfAbsent(key, k -> supplier.get());
    }

    public static void clear() {
        pool.clear();
    }
}

// Usage
ItemStack border = ItemStackPool.getOrCreate("border", () ->
    new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
        .name("&7")
        .build()
);

gui.setBorder(BorderType.FULL, border);
```

### 6. Optimize Listeners

```java
// ❌ BAD: Heavy operations in listeners
gui.addListener(new GuiListener() {
    @Override
    public void onClick(InventoryClickEvent event) {
        // Database query on every click!
        database.updatePlayerData(player);
    }
});

// ✅ GOOD: Defer heavy operations
gui.addListener(new GuiListener() {
    @Override
    public void onClick(InventoryClickEvent event) {
        // Schedule async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            database.updatePlayerData(player);
        });
    }
});
```

### 7. Clean Up Resources

```java
public class GuiManagerWrapper {
    private final GuiManager manager;
    private final ClickCooldown cooldown;

    public void shutdown() {
        // Clean up cooldowns
        cooldown.clearAll();

        // Animation scheduler is auto-managed
        // No manual cleanup needed!
    }

    public void onPlayerQuit(Player player) {
        // Clean player-specific data
        cooldown.clearCooldowns(player);
    }
}
```

## Animation Performance

### Frame Duration Optimization

```java
// ❌ BAD: Too fast
Animation tooFast = new Animation(true)
    .addFrame(item, 1);  // Updates every tick!

// ✅ GOOD: Reasonable frame rate
Animation good = new Animation(true)
    .addFrame(item, 20);  // Updates every second
```

### Animation Pooling

```java
public class AnimationPool {
    private static final Map<String, Animation> animations = new HashMap<>();

    static {
        // Pre-create common animations
        animations.put("border-pulse", new PulsingBorder(3, 1));
        animations.put("rotating-compass", new RotatingCompass());
        animations.put("loading-bar", new LoadingBar(Direction.HORIZONTAL, 100));
    }

    public static Animation get(String key) {
        return animations.get(key);
    }
}

// Reuse animations
gui1.addAnimation(4, AnimationPool.get("border-pulse"));
gui2.addAnimation(4, AnimationPool.get("border-pulse"));
gui3.addAnimation(4, AnimationPool.get("border-pulse"));
```

### Conditional Animations

```java
// Only animate for players who want it
if (player.hasPermission("gui.animations") && !player.hasMetadata("animations-off")) {
    gui.addAnimation(13, centerAnimation);
}
```

## Memory Management

### GUI Lifecycle

```java
public class GuiLifecycleManager {
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    public void onGuiOpen(Player player, AuroraGui gui) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void onGuiClose(Player player) {
        lastActivity.remove(player.getUniqueId());
    }

    // Clean up inactive players
    public void cleanup() {
        long timeout = 300000; // 5 minutes
        long now = System.currentTimeMillis();

        lastActivity.entrySet().removeIf(entry ->
            now - entry.getValue() > timeout
        );
    }
}
```

### Periodic Cleanup

```java
@Override
public void onEnable() {
    // Run cleanup every 5 minutes
    Bukkit.getScheduler().runTaskTimer(this, () -> {
        clickCooldown.cleanup();
        guiLifecycleManager.cleanup();

        // Clear ColorUtils cache if too large
        if (ColorUtils.getCacheSize() > 10000) {
            ColorUtils.clearCache();
        }
    }, 6000L, 6000L);
}
```

## Database Optimization

### Async Loading

```java
public void openPlayerVault(Player player) {
    // Show loading screen
    showLoadingScreen(player);

    // Load data async
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        List<ItemStack> items = database.loadVault(player.getUniqueId());

        // Build GUI on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            PacketGui vault = new PacketGui("vault")
                .title("&6Your Vault")
                .rows(6);

            // Add items
            for (int i = 0; i < items.size(); i++) {
                vault.addItem(i, items.get(i), handler);
            }

            vault.register(manager);
            vault.open(player);
        });
    });
}
```

### Batch Operations

```java
// ❌ BAD: Individual saves
for (ItemStack item : items) {
    database.saveItem(item);  // Multiple queries!
}

// ✅ GOOD: Batch save
database.saveItems(items);  // Single query!
```

## Profiling

### Timings

```java
public class GuiProfiler {
    public void profileGuiOpen(Player player, AuroraGui gui) {
        long start = System.nanoTime();

        gui.open(player);

        long duration = System.nanoTime() - start;
        if (duration > 5_000_000) {  // > 5ms
            plugin.getLogger().warning(String.format(
                "Slow GUI open: %s took %.2fms",
                gui.getName(),
                duration / 1_000_000.0
            ));
        }
    }
}
```

### Memory Profiling

```java
public class MemoryProfiler {
    public void profileMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        plugin.getLogger().info(String.format(
            "Memory: %dMB / %dMB (%.1f%%)",
            usedMemory / 1048576,
            totalMemory / 1048576,
            (usedMemory * 100.0) / totalMemory
        ));
    }
}
```

## Server Configuration

### Paper Configuration

```yaml
# paper.yml
settings:
  use-faster-eigencraft-redstone: true
  optimize-explosions: true

# spigot.yml
settings:
  save-user-cache-on-stop-only: true
```

### Java Arguments

```bash
# Recommended JVM flags for Minecraft servers
java -Xms4G -Xmx4G \
  -XX:+UseG1GC \
  -XX:+ParallelRefProcEnabled \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+DisableExplicitGC \
  -XX:+AlwaysPreTouch \
  -XX:G1NewSizePercent=30 \
  -XX:G1MaxNewSizePercent=40 \
  -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 \
  -XX:G1HeapWastePercent=5 \
  -XX:G1MixedGCCountTarget=4 \
  -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:G1MixedGCLiveThresholdPercent=90 \
  -XX:G1RSetUpdatingPauseTimePercent=5 \
  -XX:SurvivorRatio=32 \
  -XX:+PerfDisableSharedMem \
  -XX:MaxTenuringThreshold=1 \
  -jar server.jar nogui
```

## Monitoring

### Metrics Collection

```java
public class GuiMetrics {
    private final AtomicLong totalOpens = new AtomicLong();
    private final AtomicLong totalClicks = new AtomicLong();
    private final AtomicLong totalViolations = new AtomicLong();

    public void recordOpen() {
        totalOpens.incrementAndGet();
    }

    public void recordClick() {
        totalClicks.incrementAndGet();
    }

    public void recordViolation() {
        totalViolations.incrementAndGet();
    }

    public void printStats() {
        plugin.getLogger().info(String.format(
            "GUI Stats - Opens: %d, Clicks: %d, Violations: %d",
            totalOpens.get(),
            totalClicks.get(),
            totalViolations.get()
        ));
    }
}
```

## Best Practices Checklist

### ✅ DO:

- [x] Use appropriate GUI types for each use case
- [x] Choose validation levels based on security needs
- [x] Limit animations to 3-5 per GUI
- [x] Reuse ItemStacks where possible
- [x] Perform heavy operations asynchronously
- [x] Clean up resources periodically
- [x] Monitor performance metrics
- [x] Profile slow operations
- [x] Test under load

### ❌ DON'T:

- [ ] Use PacketGui for display-only menus
- [ ] Use ADVANCED validation everywhere
- [ ] Animate every slot
- [ ] Create ItemStacks repeatedly
- [ ] Run database queries on main thread
- [ ] Ignore memory leaks
- [ ] Skip cleanup
- [ ] Forget to profile
- [ ] Deploy without testing

## Troubleshooting

### High CPU Usage

1. Check animation count - reduce if > 5 per GUI
2. Verify frame durations - increase if < 20 ticks
3. Review validation levels - lower if too strict
4. Profile with timings - find slow operations

### Memory Leaks

1. Check cooldown cleanup - ensure running
2. Verify GUI registration - unregister closed GUIs
3. Clear caches periodically
4. Review custom listeners - ensure no leaks

### Lag Spikes

1. Move database operations async
2. Reduce packet validation strictness
3. Batch operations where possible
4. Profile with Spark or Timings

## Further Reading

- **[Anti-Dupe Guide](anti-dupe.md)** - Security best practices
- **[Packet GUIs](../features/packet-guis.md)** - Validation overhead
- **[Animations](../features/animations.md)** - Animation performance

---

**Need help?** Join our Discord for performance optimization support!
