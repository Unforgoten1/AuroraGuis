package examples;

import dev.aurora.Builder.GuiBuilder;
import dev.aurora.GUI.IGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.API.PacketGuiConfig;
import dev.aurora.Packet.API.ValidationLevel;
import dev.aurora.Packet.Core.PacketGui;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example demonstrating packet-based GUIs with anti-dupe protection
 *
 * Packet GUIs provide three levels of validation:
 * - BASIC: No packet validation (same as standard AuroraGui)
 * - PACKET: Packet interception + truth tracking (~1-2ms overhead)
 * - ADVANCED: Full anti-dupe with fingerprinting (~3-5ms overhead)
 */
public class PacketGuiExample {

    private final JavaPlugin plugin;
    private final GuiManager guiManager;

    public PacketGuiExample(JavaPlugin plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        // IMPORTANT: Enable packet support before creating any PacketGuis
        guiManager.enablePacketSupport();
    }

    /**
     * Example 1: Basic PacketGui with default settings
     * Uses PACKET validation level (balanced security/performance)
     */
    public void basicPacketGui(Player player) {
        // Create a packet GUI - API is identical to AuroraGui
        PacketGui gui = new PacketGui("basic-shop")
                .title("§6Protected Shop")
                .rows(3)
                .addItem(13, createShopItem(), event -> {
                    // This click is protected from exploits!
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.sendMessage("§aYou purchased an item!");
                    // Economy withdrawal would go here
                })
                .register(guiManager);

        gui.open(player);
    }

    /**
     * Example 2: Using GuiBuilder with packet mode
     * Cleanest way to create packet GUIs
     */
    public void builderPacketGui(Player player) {
        IGui gui = GuiBuilder.shop(guiManager, "§6§lPremium Shop")
                .packetMode(ValidationLevel.ADVANCED) // Enable packet mode with ADVANCED validation
                .item(10, createDiamondItem(), this::handleExpensivePurchase)
                .item(13, createGoldItem(), this::handlePurchase)
                .item(16, createEmeraldItem(), this::handleRarePurchase)
                .cooldown(1000) // Additional cooldown on top of packet validation
                .build();

        gui.open(player);
    }

    /**
     * Example 3: Custom configuration for strict anti-dupe
     * Perfect for high-value economy items
     */
    public void strictPacketGui(Player player) {
        // Create strict configuration
        PacketGuiConfig config = PacketGuiConfig.strict()
                .minClickDelayMs(100)       // Slower clicks required
                .maxClicksPerSecond(10)      // Lower rate limit
                .kickOnViolation(true)       // Kick on exploit
                .violationKickThreshold(3);  // Kick after 3 violations

        PacketGui gui = new PacketGui("bank", config)
                .title("§6§lBank Vault §c[Protected]")
                .rows(6)
                .addItem(22, createVaultItem(), event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    // High-value transaction protected by ADVANCED validation
                    clicker.sendMessage("§aAccessing vault...");
                })
                .register(guiManager);

        gui.open(player);
    }

    /**
     * Example 4: Handling violations with custom logic
     */
    public void guiWithViolationHandler(Player player) {
        PacketGui gui = new PacketGui("trading-post")
                .title("§6Trading Post")
                .rows(5)
                .setValidationLevel(ValidationLevel.ADVANCED)
                .onViolation((violator, exploitType) -> {
                    // Custom violation handling
                    plugin.getLogger().warning(
                            violator.getName() + " attempted " + exploitType.name() +
                            " (Severity: " + exploitType.getSeverity() + ")"
                    );

                    // Alert admins
                    plugin.getServer().broadcast(
                            "§c" + violator.getName() + " attempted exploit: " + exploitType.getDescription(),
                            "aurora.admin"
                    );

                    // Kick for critical violations
                    if (exploitType.getSeverity() >= 5) {
                        violator.kickPlayer("§cExploit attempt detected");
                    }
                });

        gui.addItem(13, createTradeItem(), event -> {
            // Trading logic here
        });

        gui.register(guiManager);
        gui.open(player);
    }

    /**
     * Example 5: Lenient configuration for non-economy GUIs
     * When you want packet support but minimal overhead
     */
    public void lenientPacketGui(Player player) {
        PacketGuiConfig config = PacketGuiConfig.lenient()
                .logViolations(false);  // Don't log for cosmetic GUI

        PacketGui gui = new PacketGui("cosmetics", config)
                .title("§dCosmetics Menu")
                .rows(4)
                .addItem(10, createCosmeticItem(), event -> {
                    // Cosmetic purchase - still has basic protection
                });

        gui.register(guiManager);
        gui.open(player);
    }

    /**
     * Example 6: Mixing AuroraGui and PacketGui in same plugin
     * Use PacketGui for shops, AuroraGui for info menus
     */
    public void mixedGuiTypes(Player player) {
        // Info menu - doesn't need packet validation
        IGui infoGui = new dev.aurora.GUI.AuroraGui("info")
                .title("§bServer Info")
                .rows(3)
                .addItem(13, createInfoItem(), event -> {
                    // Just displaying info, no economy
                })
                .register(guiManager);

        // Shop - needs packet validation
        IGui shopGui = GuiBuilder.shop(guiManager, "§6Shop")
                .packetMode(ValidationLevel.PACKET)  // Protected
                .item(13, createShopItem(), this::handlePurchase)
                .build();

        // Open info GUI first
        infoGui.open(player);
    }

    /**
     * Example 7: Checking if player's inventory is synced
     * Useful for debugging or manual verification
     */
    public void checkSyncStatus(Player player, PacketGui gui) {
        if (gui.isPlayerSynced(player)) {
            player.sendMessage("§aInventory is synced with server");
        } else {
            player.sendMessage("§cDesync detected! Resyncing...");
            gui.forceResync(player);
        }
    }

    /**
     * Example 8: Migration from AuroraGui to PacketGui
     * Nearly identical API!
     */
    public void migrationExample(Player player) {
        // BEFORE (AuroraGui):
        // AuroraGui oldGui = new AuroraGui("shop")
        //     .title("Shop")
        //     .addItem(13, item, this::handleClick)
        //     .register(guiManager);

        // AFTER (PacketGui) - just change class and add validation level:
        PacketGui newGui = new PacketGui("shop")
                .title("Shop")
                .setValidationLevel(ValidationLevel.PACKET)  // Only addition needed!
                .addItem(13, createShopItem(), this::handlePurchase)
                .register(guiManager);

        newGui.open(player);
    }

    // Helper methods for creating items

    private ItemStack createShopItem() {
        return new ItemBuilder(Material.DIAMOND)
                .name("§bDiamond §7(10 coins)")
                .lore("§7Click to purchase")
                .build();
    }

    private ItemStack createDiamondItem() {
        return new ItemBuilder(Material.DIAMOND_BLOCK)
                .name("§bDiamond Block §7(100 coins)")
                .lore("§7High-value item", "§cProtected by anti-dupe")
                .build();
    }

    private ItemStack createGoldItem() {
        return new ItemBuilder(Material.GOLD_INGOT)
                .name("§eGold Ingot §7(5 coins)")
                .build();
    }

    private ItemStack createEmeraldItem() {
        return new ItemBuilder(Material.EMERALD)
                .name("§aEmerald §7(50 coins)")
                .lore("§7Rare item", "§cProtected by anti-dupe")
                .build();
    }

    private ItemStack createVaultItem() {
        return new ItemBuilder(Material.CHEST)
                .name("§6Vault Access")
                .lore("§7Store and retrieve items", "§cMaximum protection")
                .build();
    }

    private ItemStack createTradeItem() {
        return new ItemBuilder(Material.PAPER)
                .name("§eCreate Trade")
                .lore("§7Initiate a trade", "§cAnti-dupe enabled")
                .build();
    }

    private ItemStack createCosmeticItem() {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .name("§dHat")
                .lore("§7Cosmetic item")
                .build();
    }

    private ItemStack createInfoItem() {
        return new ItemBuilder(Material.BOOK)
                .name("§bServer Rules")
                .lore("§7Click to view")
                .build();
    }

    // Click handlers

    private void handlePurchase(org.bukkit.event.inventory.InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.sendMessage("§aPurchase complete!");
        // Economy withdrawal here
    }

    private void handleExpensivePurchase(org.bukkit.event.inventory.InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.sendMessage("§aExpensive purchase complete! Protected from duplication.");
        // Economy withdrawal here
    }

    private void handleRarePurchase(org.bukkit.event.inventory.InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.sendMessage("§aRare item purchased! Maximum anti-dupe protection applied.");
        // Economy withdrawal here
    }
}
