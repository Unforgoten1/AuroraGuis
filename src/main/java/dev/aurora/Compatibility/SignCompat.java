package dev.aurora.Compatibility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Cross-version sign GUI compatibility layer
 * Handles sign editor packet manipulation across versions
 */
public class SignCompat {
    private static final ServerVersion VERSION = ServerVersion.getInstance();

    /**
     * Opens a sign editor for the player with pre-filled lines
     *
     * @param player The player to open sign editor for
     * @param lines The initial lines (max 4)
     * @return true if successful
     */
    public static boolean openSignEditor(Player player, String[] lines) {
        if (lines == null) lines = new String[]{"", "", "", ""};
        if (lines.length < 4) {
            String[] fullLines = new String[4];
            System.arraycopy(lines, 0, fullLines, 0, lines.length);
            for (int i = lines.length; i < 4; i++) {
                fullLines[i] = "";
            }
            lines = fullLines;
        }

        try {
            // Place temporary sign at player location
            Location signLoc = player.getLocation().clone();
            signLoc.setY(1); // Place at Y=1 to avoid visibility

            Block block = signLoc.getBlock();
            Material originalType = block.getType();

            // Set block to sign
            if (VERSION.isAtLeast(ServerVersion.V1_14)) {
                block.setType(Material.valueOf("OAK_SIGN"));
            } else {
                block.setType(Material.valueOf("SIGN"));
            }

            // Get sign state
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();

            // Set lines
            for (int i = 0; i < 4; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update(true, false);

            // Open sign editor
            if (VERSION.isAtLeast(ServerVersion.V1_14)) {
                openSignEditorModern(player, signLoc);
            } else {
                openSignEditorLegacy(player, signLoc);
            }

            // Restore original block after a delay
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugins()[0],
                () -> block.setType(originalType),
                2L
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens sign editor for 1.14+ versions
     */
    private static void openSignEditorModern(Player player, Location signLoc) throws Exception {
        // Use reflection since openSign was added in 1.19.3+
        try {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signLoc.getBlock().getState();
            java.lang.reflect.Method openSignMethod = Player.class.getMethod("openSign", org.bukkit.block.Sign.class);
            openSignMethod.invoke(player, sign);
        } catch (NoSuchMethodException e) {
            // Fall back to legacy NMS approach for versions before 1.19.3
            openSignEditorLegacy(player, signLoc);
        }
    }

    /**
     * Opens sign editor for legacy versions using NMS
     */
    private static void openSignEditorLegacy(Player player, Location signLoc) throws Exception {
        String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName()
            .split("\\.")[3];

        // Get classes
        Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
        Class<?> entityPlayerClass = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
        Class<?> blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
        Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutOpenSignEditor");
        Class<?> playerConnectionClass = Class.forName("net.minecraft.server." + version + ".PlayerConnection");

        // Get player handle
        Object craftPlayer = craftPlayerClass.cast(player);
        Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
        Object entityPlayer = getHandleMethod.invoke(craftPlayer);

        // Create BlockPosition
        Constructor<?> blockPosConstructor = blockPositionClass.getConstructor(
            int.class, int.class, int.class);
        Object blockPosition = blockPosConstructor.newInstance(
            signLoc.getBlockX(),
            signLoc.getBlockY(),
            signLoc.getBlockZ()
        );

        // Create packet
        Constructor<?> packetConstructor = packetClass.getConstructor(blockPositionClass);
        Object packet = packetConstructor.newInstance(blockPosition);

        // Send packet
        Object playerConnection = entityPlayerClass.getField("playerConnection").get(entityPlayer);
        Method sendPacketMethod = playerConnectionClass.getMethod("sendPacket",
            Class.forName("net.minecraft.server." + version + ".Packet"));
        sendPacketMethod.invoke(playerConnection, packet);
    }

    /**
     * Extracts text from a sign update event
     *
     * @param lines The sign lines from event
     * @return Array of 4 lines
     */
    public static String[] extractSignText(String[] lines) {
        if (lines == null) return new String[]{"", "", "", ""};
        if (lines.length < 4) {
            String[] fullLines = new String[4];
            System.arraycopy(lines, 0, fullLines, 0, lines.length);
            for (int i = lines.length; i < 4; i++) {
                fullLines[i] = "";
            }
            return fullLines;
        }
        return lines;
    }

    /**
     * Checks if sign GUI is supported on this version
     *
     * @return true if supported
     */
    public static boolean isSupported() {
        return true; // All versions 1.8+ support sign editing
    }
}
