package dev.aurora.Packet.Validation;

import dev.aurora.Packet.API.IPacketGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks cursor state to prevent cursor-splitting exploits
 * Detects attempts to duplicate or manipulate cursor items
 *
 * Detects:
 * - Cursor-Duplication (amount illegally increased)
 * - Cursor-Swap (item type changed unexpectedly)
 */
public class CursorTracker {

    private final Map<UUID, ItemStack> trackedCursors;

    /**
     * Creates a new cursor tracker
     */
    public CursorTracker() {
        this.trackedCursors = new ConcurrentHashMap<>();
    }

    /**
     * Validates a cursor action
     * @param player The player
     * @param claimed The item the client claims is on cursor
     * @return Validation result
     */
    public ClickValidator.ValidationResult validateCursorAction(Player player, ItemStack claimed) {
        UUID uuid = player.getUniqueId();
        ItemStack tracked = trackedCursors.get(uuid);

        // First pickup or cursor is empty
        if (tracked == null || tracked.getType() == Material.AIR) {
            // Update tracking
            if (claimed != null && claimed.getType() != Material.AIR) {
                trackedCursors.put(uuid, claimed.clone());
            }
            return ClickValidator.ValidationResult.pass();
        }

        // Cursor is empty now (item was placed)
        if (claimed == null || claimed.getType() == Material.AIR) {
            trackedCursors.remove(uuid);
            return ClickValidator.ValidationResult.pass();
        }

        // Validate amount can't increase (duplication attempt)
        if (claimed.getAmount() > tracked.getAmount()) {
            return ClickValidator.ValidationResult.fail(
                    IPacketGui.ExploitType.CURSOR_DUPLICATION,
                    "Cursor amount increased from " + tracked.getAmount() + " to " + claimed.getAmount()
            );
        }

        // Validate item type unchanged (swap exploit)
        if (!claimed.getType().equals(tracked.getType())) {
            return ClickValidator.ValidationResult.fail(
                    IPacketGui.ExploitType.CURSOR_SWAP,
                    "Cursor type changed from " + tracked.getType() + " to " + claimed.getType()
            );
        }

        // Valid - update tracking
        trackedCursors.put(uuid, claimed.clone());
        return ClickValidator.ValidationResult.pass();
    }

    /**
     * Initializes cursor tracking for a player
     * @param player The player
     * @param cursor The initial cursor item
     */
    public void initializeCursor(Player player, ItemStack cursor) {
        if (cursor != null && cursor.getType() != Material.AIR) {
            trackedCursors.put(player.getUniqueId(), cursor.clone());
        } else {
            trackedCursors.remove(player.getUniqueId());
        }
    }

    /**
     * Gets the tracked cursor for a player
     * @param player The player
     * @return The tracked cursor, or null
     */
    public ItemStack getTrackedCursor(Player player) {
        ItemStack tracked = trackedCursors.get(player.getUniqueId());
        return tracked != null ? tracked.clone() : null;
    }

    /**
     * Updates the tracked cursor
     * @param player The player
     * @param cursor The new cursor item
     */
    public void updateCursor(Player player, ItemStack cursor) {
        if (cursor == null || cursor.getType() == Material.AIR) {
            trackedCursors.remove(player.getUniqueId());
        } else {
            trackedCursors.put(player.getUniqueId(), cursor.clone());
        }
    }

    /**
     * Checks if cursor tracking matches actual cursor
     * @param player The player
     * @return true if synced
     */
    public boolean isCursorSynced(Player player) {
        ItemStack tracked = getTrackedCursor(player);
        ItemStack actual = player.getItemOnCursor();

        // Both null/air = synced
        if ((tracked == null || tracked.getType() == Material.AIR) &&
            (actual == null || actual.getType() == Material.AIR)) {
            return true;
        }

        // One null, one not = desync
        if (tracked == null || actual == null) {
            return false;
        }

        // Compare items
        return itemsMatch(tracked, actual);
    }

    /**
     * Forces cursor resync
     * @param player The player
     */
    public void forceResyncCursor(Player player) {
        player.setItemOnCursor(null);
        trackedCursors.remove(player.getUniqueId());
    }

    /**
     * Clears tracking for a player
     * @param player The player
     */
    public void clearPlayer(Player player) {
        trackedCursors.remove(player.getUniqueId());
    }

    /**
     * Clears all tracking
     */
    public void clearAll() {
        trackedCursors.clear();
    }

    /**
     * Compares two ItemStacks for equality
     * @param item1 First item
     * @param item2 Second item
     * @return true if items match
     */
    private boolean itemsMatch(ItemStack item1, ItemStack item2) {
        if (item1 == null && item2 == null) {
            return true;
        }
        if (item1 == null || item2 == null) {
            return false;
        }

        // Check type and amount
        if (item1.getType() != item2.getType() || item1.getAmount() != item2.getAmount()) {
            return false;
        }

        // Check durability
        if (item1.getDurability() != item2.getDurability()) {
            return false;
        }

        // Check metadata
        if (item1.hasItemMeta() != item2.hasItemMeta()) {
            return false;
        }

        if (item1.hasItemMeta()) {
            return item1.getItemMeta().equals(item2.getItemMeta());
        }

        return true;
    }
}
