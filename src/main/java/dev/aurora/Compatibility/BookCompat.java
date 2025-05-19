package dev.aurora.Compatibility;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Cross-version compatibility for opening book GUIs
 * Handles NMS differences across Minecraft versions
 */
public class BookCompat {
    private static final String VERSION;
    private static final boolean IS_LEGACY;

    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);

        // Check if version is 1.13+ (no version in package name on some servers)
        IS_LEGACY = VERSION.startsWith("v1_8") || VERSION.startsWith("v1_9") ||
                    VERSION.startsWith("v1_10") || VERSION.startsWith("v1_11") ||
                    VERSION.startsWith("v1_12");
    }

    /**
     * Opens a book for a player
     *
     * @param player The player
     * @param book The book ItemStack
     * @return true if successful
     */
    public static boolean openBook(Player player, ItemStack book) {
        if (book == null || !isBookMaterial(book.getType())) {
            return false;
        }

        try {
            // Try modern API first (1.14.4+)
            if (!IS_LEGACY) {
                return openBookModern(player, book);
            } else {
                return openBookLegacy(player, book);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens book using modern API (1.14+)
     */
    private static boolean openBookModern(Player player, ItemStack book) {
        try {
            // Use openBook method if available
            Method openBookMethod = Player.class.getMethod("openBook", ItemStack.class);
            openBookMethod.invoke(player, book);
            return true;
        } catch (NoSuchMethodException e) {
            // Fallback to NMS
            return openBookNMS(player, book);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens book using legacy methods (1.8-1.13)
     */
    private static boolean openBookLegacy(Player player, ItemStack book) {
        return openBookNMS(player, book);
    }

    /**
     * Opens book using NMS packets
     */
    private static boolean openBookNMS(Player player, ItemStack book) {
        try {
            // Get player's hand slot
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack old = player.getInventory().getItem(slot);

            // Temporarily set book in hand
            player.getInventory().setItem(slot, book);

            // Send packet to open book
            sendOpenBookPacket(player);

            // Restore old item after a tick
            Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugins()[0],
                () -> player.getInventory().setItem(slot, old),
                1L
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends the packet to open a book
     */
    private static void sendOpenBookPacket(Player player) throws Exception {
        Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);

        // Get packet class
        Class<?> packetClass;
        if (IS_LEGACY) {
            packetClass = getNMSClass("PacketPlayOutCustomPayload");
        } else {
            // 1.17+ uses different package structure
            packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenBook");
        }

        // Create and send packet
        if (IS_LEGACY) {
            sendLegacyPacket(player, entityPlayer);
        } else {
            sendModernPacket(player, entityPlayer, packetClass);
        }
    }

    /**
     * Sends book open packet for legacy versions
     */
    private static void sendLegacyPacket(Player player, Object entityPlayer) throws Exception {
        // For older versions, we need to use a different approach
        // This is a simplified version - full implementation would handle all edge cases
        Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

        // Try to open book via different methods based on version
        try {
            Method openBook = playerConnection.getClass().getMethod("a", getNMSClass("EnumHand"));
            Object mainHand = getNMSClass("EnumHand").getEnumConstants()[0];
            openBook.invoke(playerConnection, mainHand);
        } catch (Exception ignored) {
            // Fallback - book will just appear in inventory
        }
    }

    /**
     * Sends book open packet for modern versions
     */
    private static void sendModernPacket(Player player, Object entityPlayer, Class<?> packetClass) throws Exception {
        // Get EnumHand.MAIN_HAND
        Class<?> enumHandClass = Class.forName("net.minecraft.world.EnumHand");
        Object mainHand = enumHandClass.getEnumConstants()[0];

        // Create packet
        Constructor<?> constructor = packetClass.getConstructor(enumHandClass);
        Object packet = constructor.newInstance(mainHand);

        // Send packet
        Object playerConnection = entityPlayer.getClass().getField("b").get(entityPlayer);
        Method sendPacket = playerConnection.getClass().getMethod("a", getNMSClass("Packet"));
        sendPacket.invoke(playerConnection, packet);
    }

    /**
     * Gets NMS class
     */
    private static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + VERSION + "." + className);
    }

    /**
     * Checks if material is a book
     */
    private static boolean isBookMaterial(Material material) {
        String name = material.name();
        return name.equals("WRITTEN_BOOK") ||
               name.equals("BOOK_AND_QUILL") ||
               name.equals("WRITABLE_BOOK");
    }

    /**
     * Creates a written book with pages
     *
     * @param title The book title
     * @param author The book author
     * @param pages The book pages
     * @return Written book ItemStack
     */
    public static ItemStack createBook(String title, String author, List<String> pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            meta.setTitle(title);
            meta.setAuthor(author);
            meta.setPages(pages);
            book.setItemMeta(meta);
        }

        return book;
    }

    /**
     * Creates a written book with pages (array version)
     *
     * @param title The book title
     * @param author The book author
     * @param pages The book pages
     * @return Written book ItemStack
     */
    public static ItemStack createBook(String title, String author, String... pages) {
        return createBook(title, author, java.util.Arrays.asList(pages));
    }
}
