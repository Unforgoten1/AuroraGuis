# Conditions & Cooldowns

Control when and how players can interact with GUI items using conditions and cooldowns.

## Overview

AuroraGuis provides two complementary systems for controlling click interactions:

- **ClickCondition** - Requirements that must be met before clicks are processed
- **ClickCooldown** - Time-based restrictions to prevent spam clicking

## Click Conditions

### What are Click Conditions?

Click conditions are predicates that filter clicks before handlers execute. They enable permission checks, click-type validation, and custom logic.

### Functional Interface

```java
@FunctionalInterface
public interface ClickCondition {
    boolean test(InventoryClickEvent event);
}
```

### Built-in Conditions

#### Permission Check

```java
ClickCondition.requirePermission("shop.vip")
```

Checks if player has the specified permission.

**Example:**
```java
gui.addItem(13, vipItem, event -> {
    giveVIPReward((Player) event.getWhoClicked());
}, ClickCondition.requirePermission("shop.vip"));
```

#### Left Click

```java
ClickCondition.requireLeftClick()
```

Requires the click to be a left click.

**Example:**
```java
gui.addItem(10, purchaseButton, event -> {
    handlePurchase(event);
}, ClickCondition.requireLeftClick());
```

#### Right Click

```java
ClickCondition.requireRightClick()
```

Requires the click to be a right click.

**Example:**
```java
gui.addItem(10, infoButton, event -> {
    showInfo((Player) event.getWhoClicked());
}, ClickCondition.requireRightClick());
```

#### Shift Click

```java
ClickCondition.requireShiftClick()
```

Requires the click to be a shift click.

**Example:**
```java
gui.addItem(10, quickBuyButton, event -> {
    handleQuickBuy(event);
}, ClickCondition.requireShiftClick());
```

#### Item Presence

```java
ClickCondition.requireItem()
```

Requires the clicked slot to have an item.

**Example:**
```java
gui.addItem(10, emptySlot, event -> {
    // Only processes if slot has item
}, ClickCondition.requireItem());
```

### Custom Conditions

Create custom conditions using lambda expressions:

```java
// Check player level
ClickCondition levelCheck = event -> {
    Player player = (Player) event.getWhoClicked();
    return player.getLevel() >= 10;
};

// Check economy balance
ClickCondition balanceCheck = event -> {
    Player player = (Player) event.getWhoClicked();
    return economy.getBalance(player) >= 1000;
};

// Check world
ClickCondition worldCheck = event -> {
    Player player = (Player) event.getWhoClicked();
    return player.getWorld().getName().equals("world");
};

// Use in GUI
gui.addItem(13, item, handler, levelCheck);
```

### Condition Combinators

#### AND - Both conditions must pass

```java
ClickCondition combined = ClickCondition.requirePermission("shop.vip")
    .and(ClickCondition.requireLeftClick());

gui.addItem(13, vipItem, handler, combined);
```

#### OR - Either condition must pass

```java
ClickCondition either = ClickCondition.requirePermission("shop.vip")
    .or(ClickCondition.requirePermission("shop.premium"));

gui.addItem(13, premiumItem, handler, either);
```

#### NEGATE - Invert condition

```java
ClickCondition notShift = ClickCondition.requireShiftClick().negate();

gui.addItem(13, normalItem, handler, notShift);
```

### Complex Condition Examples

#### VIP with Left Click Only

```java
gui.addItem(13, vipItem, event -> {
    giveVIPReward((Player) event.getWhoClicked());
},
ClickCondition.requirePermission("shop.vip")
    .and(ClickCondition.requireLeftClick())
);
```

#### Premium User with Level Requirement

```java
gui.addItem(13, premiumItem, event -> {
    givePremiumReward((Player) event.getWhoClicked());
},
ClickCondition.requirePermission("shop.premium")
    .and(event -> ((Player) event.getWhoClicked()).getLevel() >= 20)
);
```

#### Admin or VIP Access

```java
gui.addItem(13, specialItem, event -> {
    giveSpecialReward((Player) event.getWhoClicked());
},
ClickCondition.requirePermission("shop.admin")
    .or(ClickCondition.requirePermission("shop.vip"))
);
```

#### Multiple Conditions

```java
gui.addItem(13, ultraRareItem, event -> {
    giveUltraRareReward((Player) event.getWhoClicked());
},
ClickCondition.requirePermission("shop.premium")
    .and(ClickCondition.requireLeftClick())
    .and(event -> {
        Player p = (Player) event.getWhoClicked();
        return p.getLevel() >= 50;
    })
    .and(event -> {
        Player p = (Player) event.getWhoClicked();
        return economy.getBalance(p) >= 10000;
    })
);
```

## Click Cooldowns

### What are Click Cooldowns?

Cooldowns prevent players from spam-clicking items. They support both global GUI cooldowns and per-slot cooldowns.

### Creating a Cooldown Manager

```java
ClickCooldown cooldown = new ClickCooldown();

// Set default cooldown (milliseconds)
cooldown.setDefaultCooldown(1000); // 1 second
```

### Global Cooldowns

Apply a cooldown to all clicks in the GUI:

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

### Per-Slot Cooldowns

Different cooldowns for different slots:

```java
ClickCooldown cooldown = new ClickCooldown();

// Slot 10: 5 second cooldown
gui.addItem(10, dailyReward, event -> {
    Player player = (Player) event.getWhoClicked();

    if (cooldown.canClickSlot(player, 10, 5000)) {
        giveReward(player);
        cooldown.recordSlotClick(player, 10);
    } else {
        long remaining = cooldown.getRemainingSlotCooldown(player, 10, 5000);
        player.sendMessage("§cCooldown: " + (remaining / 1000) + "s");
    }
});

// Slot 13: 10 second cooldown
gui.addItem(13, weeklyReward, event -> {
    Player player = (Player) event.getWhoClicked();

    if (cooldown.canClickSlot(player, 13, 10000)) {
        giveReward(player);
        cooldown.recordSlotClick(player, 13);
    } else {
        long remaining = cooldown.getRemainingSlotCooldown(player, 13, 10000);
        player.sendMessage("§cCooldown: " + (remaining / 1000) + "s");
    }
});
```

### Cooldown API

#### Check if Player Can Click

```java
// Global cooldown
boolean canClick = cooldown.canClick(player);

// Slot-specific cooldown
boolean canClickSlot = cooldown.canClickSlot(player, slot, milliseconds);
```

#### Record Clicks

```java
// Record global click
cooldown.recordClick(player);

// Record slot-specific click
cooldown.recordSlotClick(player, slot);
```

#### Get Remaining Time

```java
// Global cooldown remaining time
long remaining = cooldown.getRemainingCooldown(player);

// Slot-specific cooldown remaining time
long remaining = cooldown.getRemainingSlotCooldown(player, slot, milliseconds);
```

#### Clear Cooldowns

```java
// Clear all cooldowns for a player
cooldown.clearCooldowns(player);

// Clear specific slot cooldown
cooldown.clearSlotCooldown(player, slot);

// Clear all cooldowns (all players)
cooldown.clearAll();
```

#### Enable/Disable

```java
// Disable cooldowns temporarily
cooldown.setEnabled(false);

// Re-enable
cooldown.setEnabled(true);

// Check if enabled
boolean enabled = cooldown.isEnabled();
```

#### Cleanup

```java
// Clean up expired cooldowns (prevents memory leaks)
cooldown.cleanup();

// Get active cooldown count
int count = cooldown.getActiveCooldownCount();
```

### Configuration Options

```java
ClickCooldown cooldown = new ClickCooldown();

// Set default cooldown
cooldown.setDefaultCooldown(1000); // 1 second

// Get default cooldown
long defaultCd = cooldown.getDefaultCooldown();

// Enable/disable
cooldown.setEnabled(true);

// Check if enabled
boolean enabled = cooldown.isEnabled();
```

## Complete Examples

### Example 1: Shop with Cooldown

```java
public class CooldownShop {
    private final GuiManager manager;
    private final ClickCooldown cooldown;

    public CooldownShop(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
        this.cooldown = new ClickCooldown();
        this.cooldown.setDefaultCooldown(1000); // 1 second
    }

    public void openShop(Player player) {
        AuroraGui shop = new AuroraGui("cooldown-shop")
            .title("&6Shop")
            .rows(3)

            .addItem(11, createShopItem(Material.DIAMOND, 1000), event -> {
                Player p = (Player) event.getWhoClicked();

                if (!cooldown.canClick(p)) {
                    long remaining = cooldown.getRemainingCooldown(p);
                    p.sendMessage("§cCooldown: " + (remaining / 1000) + "s");
                    return;
                }

                if (economy.getBalance(p) >= 1000) {
                    economy.withdrawPlayer(p, 1000);
                    p.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
                    cooldown.recordClick(p);
                } else {
                    p.sendMessage("§cInsufficient funds!");
                }
            })

            .register(manager);

        shop.open(player);
    }

    private ItemStack createShopItem(Material material, int price) {
        return new ItemBuilder(material)
            .name("&e" + material.name())
            .lore("&7Price: &6$" + price)
            .build();
    }
}
```

### Example 2: Reward GUI with Multiple Cooldowns

```java
public class RewardGui {
    private final GuiManager manager;
    private final ClickCooldown cooldown;

    public RewardGui(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
        this.cooldown = new ClickCooldown();
    }

    public void openRewards(Player player) {
        AuroraGui rewards = new AuroraGui("rewards")
            .title("&6Daily Rewards")
            .rows(3)

            // Hourly reward (1 hour cooldown)
            .addItem(11, createReward("Hourly", Material.IRON_INGOT), event -> {
                handleReward(player, 11, 3600000, "hourly"); // 1 hour
            })

            // Daily reward (24 hour cooldown)
            .addItem(13, createReward("Daily", Material.GOLD_INGOT), event -> {
                handleReward(player, 13, 86400000, "daily"); // 24 hours
            })

            // Weekly reward (7 day cooldown)
            .addItem(15, createReward("Weekly", Material.DIAMOND), event -> {
                handleReward(player, 15, 604800000, "weekly"); // 7 days
            })

            .register(manager);

        rewards.open(player);
    }

    private void handleReward(Player player, int slot, long cooldownMs, String type) {
        if (!cooldown.canClickSlot(player, slot, cooldownMs)) {
            long remaining = cooldown.getRemainingSlotCooldown(player, slot, cooldownMs);
            long hours = remaining / 3600000;
            long minutes = (remaining % 3600000) / 60000;

            player.sendMessage("§cCooldown: " + hours + "h " + minutes + "m");
            return;
        }

        giveReward(player, type);
        cooldown.recordSlotClick(player, slot);
        player.sendMessage("§aReceived " + type + " reward!");
    }

    private ItemStack createReward(String name, Material icon) {
        return new ItemBuilder(icon)
            .name("&6" + name + " Reward")
            .lore("&7Click to claim!")
            .glow()
            .build();
    }

    private void giveReward(Player player, String type) {
        switch (type) {
            case "hourly":
                player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 16));
                break;
            case "daily":
                player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 32));
                break;
            case "weekly":
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
                break;
        }
    }
}
```

### Example 3: Combining Conditions and Cooldowns

```java
public class AdvancedShop {
    private final GuiManager manager;
    private final ClickCooldown cooldown;

    public AdvancedShop(JavaPlugin plugin) {
        this.manager = new GuiManager(plugin);
        this.cooldown = new ClickCooldown();
        this.cooldown.setDefaultCooldown(1000);
    }

    public void openShop(Player player) {
        AuroraGui shop = new AuroraGui("advanced-shop")
            .title("&6Advanced Shop")
            .rows(3)

            // VIP item with cooldown and permission
            .addItem(11, createVIPItem(), event -> {
                Player p = (Player) event.getWhoClicked();

                if (!cooldown.canClick(p)) {
                    long remaining = cooldown.getRemainingCooldown(p);
                    p.sendMessage("§cCooldown: " + (remaining / 1000) + "s");
                    return;
                }

                giveVIPReward(p);
                cooldown.recordClick(p);
            },
            ClickCondition.requirePermission("shop.vip")
                .and(ClickCondition.requireLeftClick())
            )

            // Premium item with level requirement and cooldown
            .addItem(13, createPremiumItem(), event -> {
                Player p = (Player) event.getWhoClicked();

                if (!cooldown.canClickSlot(p, 13, 5000)) {
                    long remaining = cooldown.getRemainingSlotCooldown(p, 13, 5000);
                    p.sendMessage("§cCooldown: " + (remaining / 1000) + "s");
                    return;
                }

                givePremiumReward(p);
                cooldown.recordSlotClick(p, 13);
            },
            ClickCondition.requirePermission("shop.premium")
                .and(event -> ((Player) event.getWhoClicked()).getLevel() >= 20)
                .and(ClickCondition.requireLeftClick())
            )

            .register(manager);

        shop.open(player);
    }

    private ItemStack createVIPItem() {
        return new ItemBuilder(Material.GOLD_BLOCK)
            .name("&6VIP Item")
            .lore("&7Requires VIP permission", "&7Left-click only")
            .build();
    }

    private ItemStack createPremiumItem() {
        return new ItemBuilder(Material.DIAMOND_BLOCK)
            .name("&bPremium Item")
            .lore("&7Requires Premium & Level 20", "&75 second cooldown")
            .build();
    }

    private void giveVIPReward(Player player) {
        player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 32));
        player.sendMessage("§aReceived VIP reward!");
    }

    private void givePremiumReward(Player player) {
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 16));
        player.sendMessage("§aReceived Premium reward!");
    }
}
```

## Best Practices

### ✅ DO:

1. **Use conditions for access control**
   ```java
   ClickCondition.requirePermission("shop.vip")
   ```

2. **Use cooldowns to prevent spam**
   ```java
   cooldown.setDefaultCooldown(1000); // 1 second
   ```

3. **Provide feedback on failed conditions**
   ```java
   if (!condition.test(event)) {
       player.sendMessage("§cYou don't have permission!");
   }
   ```

4. **Show remaining cooldown time**
   ```java
   long remaining = cooldown.getRemainingCooldown(player);
   player.sendMessage("§cCooldown: " + (remaining / 1000) + "s");
   ```

5. **Clean up cooldowns periodically**
   ```java
   cooldown.cleanup(); // Call every few minutes
   ```

### ❌ DON'T:

1. **Don't skip cooldown checks** - Allows spam clicking
2. **Don't forget to record clicks** - Cooldown won't work
3. **Don't use extremely short cooldowns** - Can still spam
4. **Don't forget condition feedback** - Players won't know why it failed
5. **Don't skip cleanup** - Memory leaks over time

## Performance Tips

1. **Reuse ClickCooldown instances** - One per GUI is fine
2. **Run cleanup periodically** - Every 5-10 minutes
3. **Use reasonable cooldowns** - 500ms+ recommended
4. **Limit active cooldowns** - Clear when players leave

## Troubleshooting

### Conditions Not Working

**Problem:** Items clickable without meeting conditions

**Solution:** Ensure condition is passed to `addItem()`
```java
gui.addItem(13, item, handler, condition); // Must include condition!
```

### Cooldown Not Applying

**Problem:** Players can spam click

**Solution:** Ensure you record clicks
```java
if (cooldown.canClick(player)) {
    handleClick();
    cooldown.recordClick(player); // Must call this!
}
```

### Memory Leaks

**Problem:** Memory usage increases over time

**Solution:** Run cleanup periodically
```java
// In a repeating task
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    cooldown.cleanup();
}, 6000L, 6000L); // Every 5 minutes
```

## Next Steps

- **[Basic GUIs](basic-guis.md)** - Learn the foundation
- **[Animations](animations.md)** - Add visual effects
- **[API Reference](../api/aurora-gui.md)** - Complete API docs
- **[Examples](../examples/code-examples.md)** - More code examples

---

**Need help?** Check the [API Reference](../api/aurora-gui.md) or open an issue on GitHub.
