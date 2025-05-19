package dev.aurora.Compatibility;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Cross-version anvil GUI compatibility layer
 * Handles anvil GUI creation and manipulation across Minecraft versions
 */
public class AnvilCompat {
    private static final ServerVersion VERSION = ServerVersion.getInstance();

    /**
     * Opens an anvil GUI for the player with the specified title and default text
     *
     * @param player The player to open the anvil for
     * @param title The anvil GUI title
     * @param defaultText The default text in the rename field
     * @return The anvil inventory, or null if failed
     */
    public static Inventory openAnvil(Player player, String title, String defaultText) {
        try {
            if (VERSION.isAtLeast(ServerVersion.V1_14)) {
                return openAnvilModern(player, title, defaultText);
            } else {
                return openAnvilLegacy(player, title, defaultText);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Opens anvil for 1.14+ versions
     */
    private static Inventory openAnvilModern(Player player, String title, String defaultText) throws Exception {
        // Use Bukkit's built-in createInventory for anvil
        Inventory anvil = org.bukkit.Bukkit.createInventory(player,
            org.bukkit.event.inventory.InventoryType.ANVIL, title);

        // Set default item in left slot if text provided
        if (defaultText != null && !defaultText.isEmpty()) {
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(
                org.bukkit.Material.PAPER);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(defaultText);
                item.setItemMeta(meta);
            }
            anvil.setItem(0, item);
        }

        player.openInventory(anvil);
        return anvil;
    }

    /**
     * Opens anvil for legacy versions (1.8-1.13)
     * Uses NMS packet manipulation
     */
    private static Inventory openAnvilLegacy(Player player, String title, String defaultText) throws Exception {
        // Get NMS classes
        String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName()
            .split("\\.")[3];
        Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
        Class<?> entityPlayerClass = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
        Class<?> containerAnvilClass = Class.forName("net.minecraft.server." + version + ".ContainerAnvil");
        Class<?> blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
        Class<?> worldClass = Class.forName("net.minecraft.server." + version + ".World");

        // Get player handle
        Object craftPlayer = craftPlayerClass.cast(player);
        Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
        Object entityPlayer = getHandleMethod.invoke(craftPlayer);

        // Get world
        Method getWorldMethod = entityPlayerClass.getMethod("getWorld");
        Object world = getWorldMethod.invoke(entityPlayer);

        // Create BlockPosition
        Constructor<?> blockPosConstructor = blockPositionClass.getConstructor(int.class, int.class, int.class);
        Object blockPosition = blockPosConstructor.newInstance(0, 0, 0);

        // Create anvil container
        Constructor<?> anvilConstructor = containerAnvilClass.getConstructor(
            int.class, worldClass, blockPositionClass, entityPlayerClass);
        Object anvilContainer = anvilConstructor.newInstance(1, world, blockPosition, entityPlayer);

        // Set custom title if on 1.9+
        if (VERSION.isAtLeast(ServerVersion.V1_9)) {
            try {
                Class<?> chatMessageClass = Class.forName("net.minecraft.server." + version + ".ChatMessage");
                Constructor<?> chatConstructor = chatMessageClass.getConstructor(String.class, Object[].class);
                Object chatMessage = chatConstructor.newInstance(title, new Object[0]);

                Method setTitleMethod = containerAnvilClass.getMethod("a", chatMessageClass);
                setTitleMethod.invoke(anvilContainer, chatMessage);
            } catch (Exception e) {
                // Ignore, use default title
            }
        }

        // Open container
        Method openContainerMethod = entityPlayerClass.getMethod("openContainer",
            Class.forName("net.minecraft.server." + version + ".IInventory"));
        openContainerMethod.invoke(entityPlayer, anvilContainer);

        // Get Bukkit inventory
        Method getBukkitViewMethod = anvilContainer.getClass().getMethod("getBukkitView");
        Object bukkitView = getBukkitViewMethod.invoke(anvilContainer);
        Method getTopInventoryMethod = bukkitView.getClass().getMethod("getTopInventory");
        Inventory anvil = (Inventory) getTopInventoryMethod.invoke(bukkitView);

        // Set default item
        if (defaultText != null && !defaultText.isEmpty()) {
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(
                org.bukkit.Material.PAPER);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(defaultText);
                item.setItemMeta(meta);
            }
            anvil.setItem(0, item);
        }

        return anvil;
    }

    /**
     * Gets the result text from an anvil inventory
     *
     * @param anvil The anvil inventory
     * @return The renamed text, or null if no result
     */
    public static String getAnvilText(Inventory anvil) {
        if (anvil == null) return null;

        org.bukkit.inventory.ItemStack result = anvil.getItem(2); // Result slot
        if (result == null || !result.hasItemMeta()) {
            // Try input slot
            result = anvil.getItem(0);
        }

        if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
            return result.getItemMeta().getDisplayName();
        }

        return null;
    }

    /**
     * Checks if anvil GUI is supported on this version
     *
     * @return true if supported
     */
    public static boolean isSupported() {
        return true; // All versions 1.8+ support anvil GUIs
    }
}
