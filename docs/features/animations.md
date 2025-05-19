# Animations

Bring your GUIs to life with smooth, performant animations powered by the centralized AnimationScheduler.

## Overview

AuroraGuis includes a powerful animation system that provides:

- **9 Built-in Animation Types** - From pulsing borders to loading bars
- **Custom Frame-Based Animations** - Create your own sequences
- **Centralized Scheduling** - O(1) overhead regardless of animation count
- **Multi-Slot Support** - Animate multiple slots simultaneously
- **Title Animations** - Animated GUI titles

## How Animations Work

```
┌─────────────────────────────────────┐
│ AnimationScheduler (Master Task)   │
│ Runs every Minecraft tick (50ms)   │
└─────────────────────────────────────┘
          ↓
┌─────────────────────────────────────┐
│ Tick all registered animations      │
│ - GUI A: Slots 4, 13, 22            │
│ - GUI B: Slots 0-8, 45-53           │
│ - GUI C: Title animation            │
└─────────────────────────────────────┘
          ↓
┌─────────────────────────────────────┐
│ Animation returns next frame        │
│ - ItemStack to display              │
│ - Duration in ticks                 │
└─────────────────────────────────────┘
          ↓
┌─────────────────────────────────────┐
│ GUI updates slot with new item      │
│ Schedule next frame update          │
└─────────────────────────────────────┘
```

### Performance Benefits

**Traditional Approach (O(n) overhead):**
```
Animation 1 → BukkitTask 1 (20 ticks/sec)
Animation 2 → BukkitTask 2 (20 ticks/sec)
Animation 3 → BukkitTask 3 (20 ticks/sec)
...
Animation 100 → 100 tasks running!
```

**AuroraGuis Approach (O(1) overhead):**
```
All Animations → Single Master Task (20 ticks/sec)
- Scales to thousands of animations
- Constant scheduler overhead
- Efficient tick distribution
```

## Quick Start

### Basic Animation

```java
AuroraGui menu = new AuroraGui("animated-menu")
    .title("&6Animated Menu")
    .rows(3);

// Create pulsing animation
Animation pulsing = new PulsingBorder(3, 1);

// Add to slot 13 (center)
menu.addAnimation(13, pulsing);

menu.register(manager);
menu.open(player);
```

### Multiple Animations

```java
AuroraGui menu = new AuroraGui("multi-animated")
    .title("&6Multi-Animated Menu")
    .rows(5);

// Border pulse
menu.addAnimation(4, new PulsingBorder(3, 1));

// Rotating compass
menu.addAnimation(13, new RotatingCompass());

// Loading bar
menu.addAnimation(22, new LoadingBar(Direction.HORIZONTAL, 10));

menu.register(manager);
menu.open(player);
```

## Built-in Animation Types

### 1. PulsingBorder

Creates a pulsing effect around GUI borders.

```java
Animation pulse = new PulsingBorder(
    int rows,          // GUI rows
    int pulseSpeed     // Speed (1-5, higher = faster)
);

// Example
menu.addAnimation(4, new PulsingBorder(3, 1));
```

**Visual Effect:**
```
┌─────────────────────────────────┐
│  ○   ○   ○   ○   ○   ○   ○   ○   ○  │  Frame 1 (dim)
│  ○   ·   ·   ·   ·   ·   ·   ·   ○  │
│  ○   ○   ○   ○   ○   ○   ○   ○   ○  │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│  ●   ●   ●   ●   ●   ●   ●   ●   ●  │  Frame 2 (bright)
│  ●   ·   ·   ·   ·   ·   ·   ·   ●  │
│  ●   ●   ●   ●   ●   ●   ●   ●   ●  │
└─────────────────────────────────┘
```

### 2. RotatingCompass

Animates a compass rotating through all directions.

```java
Animation compass = new RotatingCompass();

menu.addAnimation(13, compass);
```

**Frames:** N → NE → E → SE → S → SW → W → NW → N (repeats)

### 3. ItemRolling

Cycles through a list of different items.

```java
List<Material> materials = Arrays.asList(
    Material.DIAMOND,
    Material.EMERALD,
    Material.GOLD_INGOT,
    Material.IRON_INGOT
);

Animation rolling = new ItemRolling(materials, 10); // 10 ticks per item

menu.addAnimation(13, rolling);
```

### 4. LoadingBar

Creates a progress bar animation.

```java
Animation loadingBar = new LoadingBar(
    Direction.HORIZONTAL,  // or VERTICAL
    int duration          // Total duration in ticks
);

// Horizontal loading bar (top row)
for (int i = 0; i < 9; i++) {
    menu.addAnimation(i, new LoadingBar(Direction.HORIZONTAL, 100));
}
```

**Visual Effect:**
```
Frame 1: [■ · · · · · · · ·]
Frame 2: [■ ■ · · · · · · ·]
Frame 3: [■ ■ ■ · · · · · ·]
...
Frame 9: [■ ■ ■ ■ ■ ■ ■ ■ ■]
```

### 5. MarqueeAnimation

Scrolling text or items across slots.

```java
Animation marquee = new MarqueeAnimation(
    String text,           // Text to display
    Direction direction,   // LEFT, RIGHT, UP, DOWN
    int speed             // Ticks between moves
);

// Scrolling text across top row
menu.addAnimation(0, new MarqueeAnimation("&6Welcome to the shop!", Direction.RIGHT, 5));
```

### 6. PulsingGlow

Makes an item pulse with glow effect.

```java
Animation glow = new PulsingGlow(
    ItemStack item,    // Item to pulse
    int speed         // Pulse speed (ticks)
);

ItemStack star = new ItemBuilder(Material.NETHER_STAR)
    .name("&6&lPremium Item")
    .build();

menu.addAnimation(13, new PulsingGlow(star, 20));
```

### 7. SpiralAnimation

Creates a spiral pattern from center outward.

```java
Animation spiral = new SpiralAnimation(
    int rows,          // GUI rows
    Material material, // Material for spiral
    int speed         // Animation speed
);

menu.addAnimation(13, new SpiralAnimation(3, Material.DIAMOND, 5));
```

### 8. WaveAnimation

Creates a wave effect across slots.

```java
Animation wave = new WaveAnimation(
    Direction direction,   // Wave direction
    int amplitude,        // Wave height
    int frequency        // Wave frequency
);

// Horizontal wave across middle row
menu.addAnimation(13, new WaveAnimation(Direction.HORIZONTAL, 3, 10));
```

### 9. TypewriterAnimation

Types out text character by character.

```java
Animation typewriter = new TypewriterAnimation(
    String text,       // Text to type
    int ticksPerChar  // Ticks between characters
);

menu.addAnimation(13, new TypewriterAnimation("&aWelcome!", 5));
```

## Custom Frame-Based Animations

### Creating Custom Animations

```java
// Create animation
Animation custom = new Animation(true); // true = loop

// Add frames
custom.addFrame(new Frame(
    new ItemBuilder(Material.DIAMOND).name("&bFrame 1").build(),
    20  // Display for 20 ticks (1 second)
));

custom.addFrame(new Frame(
    new ItemBuilder(Material.EMERALD).name("&aFrame 2").build(),
    20
));

custom.addFrame(new Frame(
    new ItemBuilder(Material.GOLD_INGOT).name("&6Frame 3").build(),
    20
));

// Add to GUI
menu.addAnimation(13, custom);
```

### Frame API

```java
Frame frame = new Frame(ItemStack item, int durationTicks);

// Getters
frame.getItem();         // Get ItemStack
frame.getDuration();     // Get duration in ticks
```

### Animation API

```java
Animation anim = new Animation(boolean loop);

// Add frames
anim.addFrame(Frame frame);
anim.addFrame(ItemStack item, int duration);

// Control
anim.start();            // Start animation
anim.stop();             // Stop animation
anim.reset();            // Reset to first frame
anim.setLoop(boolean);   // Enable/disable looping

// State
anim.isRunning();        // Check if running
anim.getCurrentFrame();  // Get current frame index
anim.getTotalFrames();   // Get total frame count
```

## Multi-Slot Animations

### Animating Multiple Slots

```java
MultiSlotAnimation multiAnim = new MultiSlotAnimation();

// Define slots and their animations
Map<Integer, Animation> slotAnimations = new HashMap<>();
slotAnimations.put(10, new PulsingGlow(item1, 20));
slotAnimations.put(13, new RotatingCompass());
slotAnimations.put(16, new PulsingGlow(item2, 20));

multiAnim.setSlotAnimations(slotAnimations);

// Add to GUI
menu.addMultiSlotAnimation(multiAnim);
```

### Synchronized Multi-Slot Animation

```java
// All slots show same frame at same time
MultiSlotAnimation synchronized = new MultiSlotAnimation()
    .setSynchronized(true);

Animation wave = new WaveAnimation(Direction.HORIZONTAL, 3, 10);

// Apply to multiple slots
for (int i = 0; i < 9; i++) {
    synchronized.addSlot(i, wave);
}

menu.addMultiSlotAnimation(synchronized);
```

## Title Animations

### Animated GUI Titles

```java
TitleAnimation titleAnim = new TitleAnimation();

// Add title frames
titleAnim.addFrame("&6&lWelcome", 20);
titleAnim.addFrame("&a&lTo The", 20);
titleAnim.addFrame("&b&lShop!", 20);
titleAnim.setLoop(true);

// Apply to GUI
menu.setTitleAnimation(titleAnim);
```

### Scrolling Title

```java
TitleAnimation scrolling = new TitleAnimation();

String text = "Welcome to our amazing server! ";
for (int i = 0; i < text.length(); i++) {
    String frame = text.substring(i) + text.substring(0, i);
    scrolling.addFrame("&6" + frame.substring(0, Math.min(32, frame.length())), 5);
}

scrolling.setLoop(true);
menu.setTitleAnimation(scrolling);
```

## Animation Lifecycle

### Automatic Management

```java
// Animations start when GUI is opened
menu.open(player);

// Animations stop when GUI is closed
player.closeInventory();

// No manual cleanup needed!
```

### Manual Control

```java
// Stop specific animation
menu.stopAnimation(13);

// Stop all animations
menu.stopAllAnimations();

// Restart animation
menu.restartAnimation(13);
```

### Animation Listeners

```java
menu.addListener(new GuiListener() {
    @Override
    public void onAnimationStart(AuroraGui gui, int slot) {
        plugin.getLogger().info("Animation started at slot " + slot);
    }

    @Override
    public void onAnimationComplete(AuroraGui gui, int slot) {
        plugin.getLogger().info("Animation completed at slot " + slot);

        // Do something when animation finishes
        if (slot == 13) {
            gui.setItem(13, completionItem);
        }
    }
});
```

## Complete Examples

### Example 1: Animated Shop Menu

```java
public class AnimatedShop {
    private final GuiManager manager;

    public AnimatedShop(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
    }

    public void open(Player player) {
        AuroraGui shop = new AuroraGui("animated-shop")
            .title("&6&lShop")
            .rows(5);

        // Pulsing border
        shop.addAnimation(4, new PulsingBorder(5, 2));

        // Rotating compass for navigation
        shop.addAnimation(40, new RotatingCompass());

        // Rolling items for featured section
        List<Material> featured = Arrays.asList(
            Material.DIAMOND_SWORD,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_AXE
        );
        shop.addAnimation(13, new ItemRolling(featured, 30));

        // Pulsing glow on premium item
        ItemStack premium = new ItemBuilder(Material.NETHER_STAR)
            .name("&6&lPremium Package")
            .lore("&7Click to view!")
            .build();
        shop.addAnimation(22, new PulsingGlow(premium, 20));

        // Shop items (non-animated)
        shop.addItem(20, createShopItem(Material.DIAMOND, 100), this::handlePurchase);
        shop.addItem(21, createShopItem(Material.GOLD_INGOT, 50), this::handlePurchase);
        shop.addItem(23, createShopItem(Material.IRON_INGOT, 20), this::handlePurchase);
        shop.addItem(24, createShopItem(Material.EMERALD, 75), this::handlePurchase);

        shop.register(manager);
        shop.open(player);
    }

    private ItemStack createShopItem(Material material, int price) {
        return new ItemBuilder(material)
            .name("&e" + material.name())
            .lore("&7Price: &6$" + price)
            .build();
    }

    private void handlePurchase(InventoryClickEvent event) {
        // Purchase logic
    }
}
```

### Example 2: Loading Screen

```java
public class LoadingScreen {
    private final GuiManager manager;

    public LoadingScreen(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
    }

    public void show(Player player, Runnable onComplete) {
        AuroraGui loading = new AuroraGui("loading")
            .title("&6Loading...")
            .rows(3);

        // Loading bar animation (top row)
        for (int i = 0; i < 9; i++) {
            loading.addAnimation(i, new LoadingBar(Direction.HORIZONTAL, 100));
        }

        // Spinning compass in center
        loading.addAnimation(13, new RotatingCompass());

        // Typewriter message (bottom row)
        loading.addAnimation(22, new TypewriterAnimation("&aPlease wait...", 5));

        // Listen for completion
        loading.addListener(new GuiListener() {
            @Override
            public void onAnimationComplete(AuroraGui gui, int slot) {
                if (slot == 8) { // Last slot of loading bar
                    player.closeInventory();
                    onComplete.run();
                }
            }
        });

        loading.register(manager);
        loading.open(player);
    }
}
```

### Example 3: Welcome Animation

```java
public class WelcomeAnimation {
    private final GuiManager manager;

    public WelcomeAnimation(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
    }

    public void show(Player player) {
        AuroraGui welcome = new AuroraGui("welcome")
            .title("&6&lWelcome!")
            .rows(3);

        // Spiral animation from center
        welcome.addAnimation(13, new SpiralAnimation(3, Material.DIAMOND, 5));

        // Wave animation on top row
        for (int i = 0; i < 9; i++) {
            welcome.addAnimation(i, new WaveAnimation(Direction.HORIZONTAL, 2, 10));
        }

        // Marquee message on bottom row
        welcome.addAnimation(18, new MarqueeAnimation(
            "&6Welcome to our server, " + player.getName() + "!",
            Direction.RIGHT,
            5
        ));

        // Auto-close after 5 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
        }, 100L);

        welcome.register(manager);
        welcome.open(player);
    }
}
```

## Performance Considerations

### Benchmarks

Testing: 100 GUIs with 5 animations each (500 total animations)

| Metric | Value |
|--------|-------|
| Total Overhead | ~2-3ms per tick |
| Memory Usage | ~50KB |
| CPU Impact | <1% |

### Optimization Tips

**1. Limit Animation Count**
```java
// Good - 3-5 animations per GUI
menu.addAnimation(4, borderPulse);
menu.addAnimation(13, centerAnimation);
menu.addAnimation(22, bottomAnimation);

// Bad - Too many animations
for (int i = 0; i < 54; i++) {
    menu.addAnimation(i, someAnimation); // 54 animations!
}
```

**2. Use Appropriate Frame Durations**
```java
// Good - Reasonable frame duration
new Frame(item, 20); // 1 second

// Bad - Too fast
new Frame(item, 1); // Updates every tick!
```

**3. Stop Unused Animations**
```java
// Stop when no longer needed
menu.stopAnimation(13);

// Stop all when GUI is inactive
menu.stopAllAnimations();
```

**4. Reuse Animation Instances**
```java
// Create once
Animation borderPulse = new PulsingBorder(3, 1);

// Reuse for multiple GUIs
gui1.addAnimation(4, borderPulse);
gui2.addAnimation(4, borderPulse);
gui3.addAnimation(4, borderPulse);
```

## Best Practices

### ✅ DO:

1. **Use animations sparingly** - 3-5 per GUI is ideal
2. **Choose appropriate frame rates** - 20+ ticks per frame
3. **Stop animations when not needed**
4. **Reuse animation instances** when possible
5. **Test performance** on your server

### ❌ DON'T:

1. **Don't animate every slot** - Too resource intensive
2. **Don't use 1-tick durations** - Too fast, high overhead
3. **Don't forget to test** - Some servers may struggle
4. **Don't mix too many types** - Keep it simple
5. **Don't animate in display-only GUIs** - Unnecessary

## Troubleshooting

### Animations Not Starting

**Problem:** Animations don't play when GUI opens

**Solution:** Ensure GUI is registered before opening
```java
gui.register(manager); // Must call first!
gui.open(player);
```

### Choppy Animations

**Problem:** Animations appear laggy or skip frames

**Solution:** Increase frame duration
```java
// Instead of
new Frame(item, 5); // Too fast

// Use
new Frame(item, 20); // Smoother
```

### High CPU Usage

**Problem:** Server CPU spikes with animations

**Solution:** Reduce animation count and increase durations
```java
// Limit animations per GUI
if (animationCount > 5) {
    // Remove some animations
}

// Increase frame duration
new Frame(item, 40); // 2 seconds instead of 1
```

## Next Steps

- **[Basic GUIs](basic-guis.md)** - Learn the foundation
- **[API Reference](../api/aurora-gui.md)** - Complete API docs
- **[Examples](../examples/code-examples.md)** - More animation examples
- **[Performance Guide](../guides/performance.md)** - Optimization tips

---

**Need help?** Check the [API Reference](../api/aurora-gui.md) or open an issue on GitHub.
