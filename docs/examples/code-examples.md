# Code Examples

Practical code examples demonstrating AuroraGuis features.

## Basic GUI Examples

### Simple Navigation Menu

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
            .addItem(11, createButton(Material.GRASS_BLOCK, "&aSpawn"),
                e -> teleportSpawn((Player) e.getWhoClicked()))
            .addItem(13, createButton(Material.ENDER_PEARL, "&5Warps"),
                e -> openWarps((Player) e.getWhoClicked()))
            .addItem(15, createButton(Material.DIAMOND_SWORD, "&cPvP"),
                e -> joinPvP((Player) e.getWhoClicked()))
            .register(manager);

        menu.open(player);
    }

    private ItemStack createButton(Material material, String name) {
        return new ItemBuilder(material).name(name).build();
    }
}
```

### Bordered GUI

```java
AuroraGui gui = new AuroraGui("bordered")
    .title("&6Menu")
    .rows(5)
    .setBorder(BorderType.FULL, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
        .name("&7")
        .build())
    .addItem(13, centerItem, handler)
    .register(manager);
```

### Paginated Shop

```java
public void openShop(Player player) {
    List<ItemStack> items = getShopItems();  // Get your items

    AuroraGui shop = new AuroraGui("shop")
        .title("&6Shop")
        .rows(6)
        .addPaginatedItems(items, event -> handlePurchase(event))
        .addItem(45, prevButton, e -> shop.prevPage())
        .addItem(53, nextButton, e -> shop.nextPage())
        .register(manager);

    shop.open(player);
}
```

## Secure Shop Examples

### Basic Secure Shop

```java
PacketGui shop = new PacketGui("secure-shop")
    .title("&6Secure Shop")
    .rows(3)
    .validationLevel(ValidationLevel.PACKET)
    .addItem(13, diamondItem, event -> {
        Player p = (Player) event.getWhoClicked();
        economy.withdraw(p, 1000);
        p.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
    })
    .register(manager);
```

### Shop with Violation Handling

```java
PacketGui shop = new PacketGui("monitored-shop")
    .title("&6Shop")
    .rows(6)
    .validationLevel(ValidationLevel.ADVANCED)
    .onViolation((player, exploitType) -> {
        plugin.getLogger().warning(player.getName() + " attempted " + exploitType);

        if (exploitType.getSeverity() >= 5) {
            player.kickPlayer("§cExploit detected");
        }
    })
    .addItem(13, item, handler)
    .register(manager);
```

## Animation Examples

### Pulsing Border

```java
AuroraGui menu = new AuroraGui("animated")
    .title("&6Animated Menu")
    .rows(3)
    .addAnimation(4, new PulsingBorder(3, 1))
    .register(manager);
```

### Rotating Center Item

```java
List<Material> materials = Arrays.asList(
    Material.DIAMOND,
    Material.EMERALD,
    Material.GOLD_INGOT
);

menu.addAnimation(13, new ItemRolling(materials, 20));
```

### Loading Bar

```java
for (int i = 0; i < 9; i++) {
    menu.addAnimation(i, new LoadingBar(Direction.HORIZONTAL, 100));
}
```

## Condition Examples

### Permission-Based Item

```java
gui.addItem(13, vipItem, handler,
    ClickCondition.requirePermission("shop.vip")
);
```

### Multiple Conditions

```java
gui.addItem(13, item, handler,
    ClickCondition.requirePermission("shop.premium")
        .and(ClickCondition.requireLeftClick())
        .and(event -> ((Player) event.getWhoClicked()).getLevel() >= 20)
);
```

## Cooldown Examples

### Basic Cooldown

```java
ClickCooldown cooldown = new ClickCooldown();
cooldown.setDefaultCooldown(1000);  // 1 second

gui.addItem(13, rewardItem, event -> {
    Player p = (Player) event.getWhoClicked();

    if (cooldown.canClick(p)) {
        giveReward(p);
        cooldown.recordClick(p);
    } else {
        p.sendMessage("§cCooldown: " + cooldown.getRemainingCooldown(p) / 1000 + "s");
    }
});
```

### Per-Slot Cooldowns

```java
ClickCooldown cooldown = new ClickCooldown();

// Hourly reward (1 hour)
gui.addItem(11, hourlyReward, event -> {
    Player p = (Player) event.getWhoClicked();

    if (cooldown.canClickSlot(p, 11, 3600000)) {
        giveHourlyReward(p);
        cooldown.recordSlotClick(p, 11);
    }
});

// Daily reward (24 hours)
gui.addItem(13, dailyReward, event -> {
    Player p = (Player) event.getWhoClicked();

    if (cooldown.canClickSlot(p, 13, 86400000)) {
        giveDailyReward(p);
        cooldown.recordSlotClick(p, 13);
    }
});
```

## Listener Examples

### GUI Lifecycle Listener

```java
gui.addListener(new GuiListener() {
    @Override
    public void onOpen(Player player, AuroraGui gui) {
        player.sendMessage("§aWelcome!");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }

    @Override
    public void onClose(Player player, AuroraGui gui) {
        savePlayerProgress(player);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        logClick(event);
    }
});
```

## Confirmation Dialog

```java
public static void confirm(Player player, GuiManager manager, String message,
                          Runnable onConfirm, Runnable onCancel) {
    AuroraGui dialog = new AuroraGui("confirm")
        .title("&cConfirm?")
        .rows(3)
        .addItem(11, new ItemBuilder(Material.GREEN_WOOL)
            .name("&a&lCONFIRM")
            .build(), e -> {
                player.closeInventory();
                onConfirm.run();
            })
        .addItem(13, new ItemBuilder(Material.PAPER)
            .name("&e" + message)
            .build(), e -> {})
        .addItem(15, new ItemBuilder(Material.RED_WOOL)
            .name("&c&lCANCEL")
            .build(), e -> {
                player.closeInventory();
                onCancel.run();
            })
        .register(manager);

    dialog.open(player);
}
```

## Kit Selector

```java
public void openKitSelector(Player player) {
    AuroraGui selector = new AuroraGui("kit-selector")
        .title("&6Select Kit")
        .rows(3)
        .addItem(11, createKitItem("Warrior", Material.DIAMOND_SWORD),
            e -> giveKit(player, "warrior"))
        .addItem(13, createKitItem("Archer", Material.BOW),
            e -> giveKit(player, "archer"))
        .addItem(15, createKitItem("Tank", Material.SHIELD),
            e -> giveKit(player, "tank"))
        .register(manager);

    selector.open(player);
}

private ItemStack createKitItem(String name, Material icon) {
    return new ItemBuilder(icon)
        .name("&e&l" + name)
        .lore("&7Click to select")
        .glow()
        .build();
}
```

## Player Profile GUI

```java
public void openProfile(Player player) {
    AuroraGui profile = new AuroraGui("profile")
        .title("&6Profile: " + player.getName())
        .rows(4)
        .setBorder(BorderType.FULL, borderItem)
        .addItem(13, new ItemBuilder(Material.PLAYER_HEAD)
            .skull(player.getName())
            .name("&e" + player.getName())
            .lore(
                "&7Level: &f" + player.getLevel(),
                "&7Health: &c" + player.getHealth(),
                "&7Hunger: &6" + player.getFoodLevel()
            )
            .build(), e -> {})
        .addItem(20, new ItemBuilder(Material.BOOK)
            .name("&bStats")
            .build(), e -> openStats(player))
        .addItem(24, new ItemBuilder(Material.DIAMOND)
            .name("&aAchievements")
            .build(), e -> openAchievements(player))
        .register(manager);

    profile.open(player);
}
```

## Resource Pack Custom Items

```java
// Register custom models
ModelRegistry.register("ruby", new ModelData(
    "ruby",
    Material.DIAMOND,
    1000001,
    "&cRuby"
));

// Use in GUI
ItemStack ruby = new ItemBuilder(Material.DIAMOND)
    .customModel("ruby")
    .lore("&7A precious gem")
    .build();

gui.addItem(13, ruby, handler);
```

## Virtual GUI (Large Inventory)

```java
VirtualGui largeShop = new VirtualGui("large-shop", 162)  // 3 pages
    .title("&6Large Shop")
    .rows(6);

// Add items across all pages
for (int i = 0; i < 162; i++) {
    largeShop.addItem(i, shopItems.get(i), handler);
}

largeShop.register(manager);
largeShop.open(player);
```

## Complete Shop System

See [Shop Tutorial](../guides/shop-tutorial.md) for a complete, production-ready shop system example.

## Further Reading

- [YAML Examples](yaml-examples.md)
- [Complete Projects](complete-projects.md)
- [Shop Tutorial](../guides/shop-tutorial.md)
