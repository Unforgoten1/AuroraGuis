# Shop Tutorial

Learn how to create a complete, secure shop system from scratch using AuroraGuis.

## What We'll Build

A fully-functional multi-category shop with:
- ✅ Category navigation system
- ✅ Paginated item listings
- ✅ Secure transactions (anti-dupe protection)
- ✅ Economy integration
- ✅ Animated elements
- ✅ Responsive feedback

## Prerequisites

- AuroraGuis library installed
- Vault economy plugin (or similar)
- Basic Java and Bukkit API knowledge

## Step 1: Project Setup

### Add Dependencies

```xml
<!-- pom.xml -->
<dependencies>
    <!-- AuroraGuis -->
    <dependency>
        <groupId>dev.aurora</groupId>
        <artifactId>AuroraGuis</artifactId>
        <version>1.1.0</version>
    </dependency>

    <!-- Vault API -->
    <dependency>
        <groupId>net.milkbowl.vault</groupId>
        <artifactId>VaultAPI</artifactId>
        <version>1.7</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Plugin Main Class

```java
public class ShopPlugin extends JavaPlugin {
    private GuiManager guiManager;
    private Economy economy;
    private ShopSystem shopSystem;

    @Override
    public void onEnable() {
        // Initialize GuiManager
        guiManager = new GuiManager(this);
        guiManager.enablePacketSupport();

        // Setup Vault Economy
        if (!setupEconomy()) {
            getLogger().severe("Vault economy not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Create shop system
        shopSystem = new ShopSystem(this, guiManager, economy);

        // Register command
        getCommand("shop").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                shopSystem.openMainShop((Player) sender);
            }
            return true;
        });

        getLogger().info("Shop plugin enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
            getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
```

## Step 2: Shop Data Model

### ShopCategory Class

```java
public class ShopCategory {
    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<ShopItem> items;

    public ShopCategory(String id, String displayName, Material icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.items = new ArrayList<>();
    }

    public void addItem(ShopItem item) {
        items.add(item);
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public List<ShopItem> getItems() { return items; }
}
```

### ShopItem Class

```java
public class ShopItem {
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final int amount;
    private final double price;

    public ShopItem(Material material, String displayName, int amount, double price) {
        this.material = material;
        this.displayName = displayName;
        this.lore = new ArrayList<>();
        this.amount = amount;
        this.price = price;
    }

    public ShopItem addLore(String... lines) {
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemStack toItemStack() {
        return new ItemBuilder(material)
            .name(displayName)
            .lore(lore)
            .lore(
                "",
                "&7Amount: &f" + amount,
                "&7Price: &6$" + String.format("%.2f", price),
                "",
                "&aLeft-click to purchase!"
            )
            .amount(amount)
            .build();
    }

    // Getters
    public Material getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public int getAmount() { return amount; }
    public double getPrice() { return price; }
}
```

## Step 3: Main Shop System

### ShopSystem Class

```java
public class ShopSystem {
    private final JavaPlugin plugin;
    private final GuiManager guiManager;
    private final Economy economy;
    private final Map<String, ShopCategory> categories;

    public ShopSystem(JavaPlugin plugin, GuiManager guiManager, Economy economy) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.economy = economy;
        this.categories = new HashMap<>();

        loadCategories();
    }

    private void loadCategories() {
        // Blocks category
        ShopCategory blocks = new ShopCategory("blocks", "&aBlocks", Material.GRASS_BLOCK);
        blocks.addItem(new ShopItem(Material.STONE, "&7Stone", 64, 10.0));
        blocks.addItem(new ShopItem(Material.COBBLESTONE, "&7Cobblestone", 64, 8.0));
        blocks.addItem(new ShopItem(Material.DIRT, "&7Dirt", 64, 5.0));
        blocks.addItem(new ShopItem(Material.OAK_LOG, "&6Oak Log", 64, 15.0));
        blocks.addItem(new ShopItem(Material.GLASS, "&bGlass", 64, 20.0));
        categories.put("blocks", blocks);

        // Tools category
        ShopCategory tools = new ShopCategory("tools", "&eTools", Material.DIAMOND_PICKAXE);
        tools.addItem(new ShopItem(Material.DIAMOND_PICKAXE, "&bDiamond Pickaxe", 1, 500.0)
            .addLore("&7Efficiency V", "&7Unbreaking III"));
        tools.addItem(new ShopItem(Material.DIAMOND_AXE, "&bDiamond Axe", 1, 450.0)
            .addLore("&7Efficiency V", "&7Unbreaking III"));
        tools.addItem(new ShopItem(Material.DIAMOND_SHOVEL, "&bDiamond Shovel", 1, 400.0)
            .addLore("&7Efficiency V", "&7Unbreaking III"));
        categories.put("tools", tools);

        // Weapons category
        ShopCategory weapons = new ShopCategory("weapons", "&cWeapons", Material.DIAMOND_SWORD);
        weapons.addItem(new ShopItem(Material.DIAMOND_SWORD, "&cDiamond Sword", 1, 600.0)
            .addLore("&7Sharpness V", "&7Unbreaking III"));
        weapons.addItem(new ShopItem(Material.BOW, "&6Bow", 1, 200.0)
            .addLore("&7Power V", "&7Infinity I"));
        weapons.addItem(new ShopItem(Material.ARROW, "&fArrows", 64, 50.0));
        categories.put("weapons", weapons);

        // Armor category
        ShopCategory armor = new ShopCategory("armor", "&9Armor", Material.DIAMOND_CHESTPLATE);
        armor.addItem(new ShopItem(Material.DIAMOND_HELMET, "&9Diamond Helmet", 1, 300.0)
            .addLore("&7Protection IV", "&7Unbreaking III"));
        armor.addItem(new ShopItem(Material.DIAMOND_CHESTPLATE, "&9Diamond Chestplate", 1, 500.0)
            .addLore("&7Protection IV", "&7Unbreaking III"));
        armor.addItem(new ShopItem(Material.DIAMOND_LEGGINGS, "&9Diamond Leggings", 1, 400.0)
            .addLore("&7Protection IV", "&7Unbreaking III"));
        armor.addItem(new ShopItem(Material.DIAMOND_BOOTS, "&9Diamond Boots", 1, 300.0)
            .addLore("&7Protection IV", "&7Unbreaking III"));
        categories.put("armor", armor);
    }

    public void openMainShop(Player player) {
        AuroraGui mainShop = new AuroraGui("main-shop")
            .title("&6&lShop Categories")
            .rows(3)
            .setBorder(BorderType.FULL, createBorderItem());

        // Add category buttons
        int slot = 11;
        for (ShopCategory category : categories.values()) {
            mainShop.addItem(slot, createCategoryButton(category), event -> {
                openCategoryShop(player, category);
            });
            slot += 2;
        }

        // Add balance display
        mainShop.addItem(13, createBalanceItem(player), event -> {});

        mainShop.register(guiManager);
        mainShop.open(player);
    }

    public void openCategoryShop(Player player, ShopCategory category) {
        List<ItemStack> shopItems = category.getItems().stream()
            .map(ShopItem::toItemStack)
            .collect(Collectors.toList());

        PacketGui categoryShop = new PacketGui("shop-" + category.getId())
            .title("&6" + category.getDisplayName() + " Shop")
            .rows(6)
            .validationLevel(ValidationLevel.PACKET)
            .setBorder(BorderType.FULL, createBorderItem());

        // Add paginated items
        categoryShop.addPaginatedItems(shopItems, event -> {
            handlePurchase(player, event, category);
        });

        // Navigation buttons
        categoryShop.addItem(45, createBackButton(), e -> {
            openMainShop(player);
        });

        categoryShop.addItem(48, createPreviousButton(), e -> {
            categoryShop.prevPage();
            updatePageInfo(categoryShop, player);
        });

        categoryShop.addItem(49, createBalanceItem(player), e -> {
            // Refresh balance
            categoryShop.setItem(49, createBalanceItem(player));
        });

        categoryShop.addItem(50, createPageInfo(categoryShop), e -> {});

        categoryShop.addItem(53, createNextButton(), e -> {
            categoryShop.nextPage();
            updatePageInfo(categoryShop, player);
        });

        // Add pulsing animation to featured item
        if (!shopItems.isEmpty()) {
            categoryShop.addAnimation(4, new PulsingGlow(shopItems.get(0), 20));
        }

        categoryShop.register(guiManager);
        categoryShop.open(player);
    }

    private void handlePurchase(Player player, InventoryClickEvent event, ShopCategory category) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        // Find matching shop item
        ShopItem shopItem = findShopItem(clicked, category);
        if (shopItem == null) {
            player.sendMessage("§cItem not found!");
            return;
        }

        double price = shopItem.getPrice();
        double balance = economy.getBalance(player);

        if (balance < price) {
            player.sendMessage("§cInsufficient funds! Need $" + String.format("%.2f", price));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Process purchase
        EconomyResponse response = economy.withdrawPlayer(player, price);
        if (response.transactionSuccess()) {
            player.getInventory().addItem(new ItemStack(shopItem.getMaterial(), shopItem.getAmount()));
            player.sendMessage("§aPurchased " + shopItem.getDisplayName() + " for $" +
                String.format("%.2f", price));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

            // Update balance display
            event.getInventory().setItem(49, createBalanceItem(player));
        } else {
            player.sendMessage("§cTransaction failed: " + response.errorMessage);
        }
    }

    private ShopItem findShopItem(ItemStack clicked, ShopCategory category) {
        String clickedName = clicked.getItemMeta().getDisplayName();
        return category.getItems().stream()
            .filter(item -> item.toItemStack().getItemMeta().getDisplayName().equals(clickedName))
            .findFirst()
            .orElse(null);
    }

    // Utility methods for creating items
    private ItemStack createCategoryButton(ShopCategory category) {
        return new ItemBuilder(category.getIcon())
            .name(category.getDisplayName())
            .lore(
                "&7Click to browse",
                "&7" + category.getItems().size() + " items available"
            )
            .glow()
            .build();
    }

    private ItemStack createBalanceItem(Player player) {
        double balance = economy.getBalance(player);
        return new ItemBuilder(Material.GOLD_INGOT)
            .name("&6Your Balance")
            .lore(
                "&7Balance: &6$" + String.format("%.2f", balance),
                "",
                "&7Click to refresh"
            )
            .build();
    }

    private ItemStack createBorderItem() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build();
    }

    private ItemStack createBackButton() {
        return new ItemBuilder(Material.ARROW)
            .name("&cBack")
            .lore("&7Return to categories")
            .build();
    }

    private ItemStack createPreviousButton() {
        return new ItemBuilder(Material.ARROW)
            .name("&ePrevious Page")
            .build();
    }

    private ItemStack createNextButton() {
        return new ItemBuilder(Material.ARROW)
            .name("&eNext Page")
            .build();
    }

    private ItemStack createPageInfo(PacketGui gui) {
        int current = gui.getCurrentPage() + 1;
        int total = gui.getTotalPages();
        return new ItemBuilder(Material.PAPER)
            .name("&6Page " + current + "/" + total)
            .build();
    }

    private void updatePageInfo(PacketGui gui, Player player) {
        gui.setItem(50, createPageInfo(gui));
    }
}
```

## Step 4: Adding Advanced Features

### Add Cooldowns

```java
public class ShopSystem {
    private final ClickCooldown purchaseCooldown;

    public ShopSystem(JavaPlugin plugin, GuiManager guiManager, Economy economy) {
        // ... existing code ...
        this.purchaseCooldown = new ClickCooldown();
        this.purchaseCooldown.setDefaultCooldown(1000); // 1 second between purchases
    }

    private void handlePurchase(Player player, InventoryClickEvent event, ShopCategory category) {
        // Check cooldown
        if (!purchaseCooldown.canClick(player)) {
            long remaining = purchaseCooldown.getRemainingCooldown(player);
            player.sendMessage("§cPlease wait " + (remaining / 1000) + " seconds");
            return;
        }

        // ... existing purchase logic ...

        // Record purchase click
        purchaseCooldown.recordClick(player);
    }
}
```

### Add Confirmation Dialog

```java
private void handlePurchase(Player player, InventoryClickEvent event, ShopCategory category) {
    ItemStack clicked = event.getCurrentItem();
    if (clicked == null) return;

    ShopItem shopItem = findShopItem(clicked, category);
    if (shopItem == null) return;

    double price = shopItem.getPrice();

    // Show confirmation for expensive items
    if (price >= 500.0) {
        showConfirmation(player, shopItem, () -> {
            processPurchase(player, shopItem);
        });
    } else {
        processPurchase(player, shopItem);
    }
}

private void showConfirmation(Player player, ShopItem item, Runnable onConfirm) {
    AuroraGui confirm = new AuroraGui("confirm-purchase")
        .title("§cConfirm Purchase?")
        .rows(3);

    // Confirm button
    confirm.addItem(11, new ItemBuilder(Material.GREEN_WOOL)
        .name("§a§lCONFIRM")
        .lore("§7Purchase " + item.getDisplayName())
        .build(), event -> {
            player.closeInventory();
            onConfirm.run();
        });

    // Item display
    confirm.addItem(13, item.toItemStack(), event -> {});

    // Cancel button
    confirm.addItem(15, new ItemBuilder(Material.RED_WOOL)
        .name("§c§lCANCEL")
        .build(), event -> {
            player.closeInventory();
            player.sendMessage("§7Purchase cancelled.");
        });

    confirm.register(guiManager);
    confirm.open(player);
}

private void processPurchase(Player player, ShopItem shopItem) {
    double price = shopItem.getPrice();
    double balance = economy.getBalance(player);

    if (balance < price) {
        player.sendMessage("§cInsufficient funds!");
        return;
    }

    EconomyResponse response = economy.withdrawPlayer(player, price);
    if (response.transactionSuccess()) {
        player.getInventory().addItem(new ItemStack(shopItem.getMaterial(), shopItem.getAmount()));
        player.sendMessage("§aPurchased " + shopItem.getDisplayName() + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }
}
```

## Step 5: Configuration

### config.yml

```yaml
shop:
  # Enable purchase confirmation for expensive items
  confirm-expensive: true
  expensive-threshold: 500.0

  # Cooldown settings
  purchase-cooldown: 1000 # milliseconds

  # Sound effects
  sounds:
    purchase-success: ENTITY_PLAYER_LEVELUP
    purchase-fail: ENTITY_VILLAGER_NO
    page-turn: UI_BUTTON_CLICK

  # Animation settings
  animations:
    enabled: true
    featured-item-glow: true
```

### Load Configuration

```java
public class ShopSystem {
    private boolean confirmExpensive;
    private double expensiveThreshold;
    private long purchaseCooldownMs;

    public ShopSystem(JavaPlugin plugin, GuiManager guiManager, Economy economy) {
        // ... existing code ...
        loadConfig(plugin);
    }

    private void loadConfig(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.confirmExpensive = config.getBoolean("shop.confirm-expensive", true);
        this.expensiveThreshold = config.getDouble("shop.expensive-threshold", 500.0);
        this.purchaseCooldownMs = config.getLong("shop.purchase-cooldown", 1000);
        this.purchaseCooldown.setDefaultCooldown(purchaseCooldownMs);
    }
}
```

## Step 6: Testing

### Test Categories

```java
@Test
public void testShopCategories() {
    ShopSystem shop = new ShopSystem(plugin, guiManager, economy);

    // Test main shop opens
    shop.openMainShop(player);
    assertTrue(player.getOpenInventory() != null);

    // Test category exists
    assertTrue(shop.hasCategory("blocks"));
    assertTrue(shop.hasCategory("tools"));
}
```

### Test Purchases

```java
@Test
public void testPurchase() {
    // Give player money
    economy.depositPlayer(player, 1000.0);

    // Open shop
    shop.openCategoryShop(player, shop.getCategory("blocks"));

    // Simulate purchase
    // ... test logic ...

    // Verify balance deducted
    assertTrue(economy.getBalance(player) < 1000.0);

    // Verify item received
    assertTrue(player.getInventory().contains(Material.STONE));
}
```

## Next Steps

### Enhancements

1. **Add Search Functionality**
   - Use SearchableGui from builders
   - Filter items by name

2. **Add Sell System**
   - Allow players to sell items back
   - Calculate sell prices (50% of buy price)

3. **Add Transaction History**
   - Log purchases to database
   - Show player purchase history

4. **Add Discounts**
   - VIP discounts with permissions
   - Bulk purchase discounts

5. **Add Stock System**
   - Limited quantity items
   - Restocking mechanism

## Complete Code Repository

The complete, working code for this tutorial is available at:
```
https://github.com/YourUsername/AuroraGuis-ShopExample
```

## Troubleshooting

### Economy Not Working

**Problem:** Vault economy not found

**Solution:** Ensure Vault and an economy plugin (EssentialsX, etc.) are installed

### Items Not Purchasing

**Problem:** Clicks not registering

**Solution:** Ensure PacketGui is registered with `enablePacketSupport()`

### Balance Not Updating

**Problem:** Balance display shows old value

**Solution:** Update the item after purchase:
```java
gui.setItem(49, createBalanceItem(player));
```

## Further Reading

- **[Packet GUIs](../features/packet-guis.md)** - Security features
- **[Animations](../features/animations.md)** - Visual enhancements
- **[Conditions & Cooldowns](../features/conditions-cooldowns.md)** - Access control
- **[Performance Guide](performance.md)** - Optimization tips

---

**Need help?** Join our Discord or open an issue on GitHub!
