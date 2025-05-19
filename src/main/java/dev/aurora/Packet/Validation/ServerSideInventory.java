package dev.aurora.Packet.Validation;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains authoritative server-side state for GUI inventories
 * This is the "source of truth" for validating client-side claims
 *
 * Key anti-dupe principle: Never trust the client, always verify against server truth
 */
public class ServerSideInventory {

    private final Map<Integer, ItemStack> serverSlots;
    private final Map<UUID, ItemStack> cursorState;
    private final Map<UUID, Long> lastSync;
    private final int size;

    /**
     * Creates a new server-side inventory tracker
     * @param size The inventory size (rows * 9)
     */
    public ServerSideInventory(int size) {
        this.size = size;
        this.serverSlots = new ConcurrentHashMap<>();
        this.cursorState = new ConcurrentHashMap<>();
        this.lastSync = new ConcurrentHashMap<>();
    }

    /**
     * Initializes the server truth from a Bukkit inventory
     * Should be called when GUI is first opened
     *
     * @param inventory The Bukkit inventory to copy from
     */
    public void initializeFromInventory(Inventory inventory) {
        serverSlots.clear();
        for (int i = 0; i < Math.min(size, inventory.getSize()); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                // Deep clone to prevent reference manipulation
                serverSlots.put(i, item.clone());
            }
        }
    }

    /**
     * Gets the authoritative item at a slot
     * @param slot The slot index
     * @return The server-side item, or null if empty
     */
    public ItemStack getAuthoritativeItem(int slot) {
        if (slot < 0 || slot >= size) {
            return null;
        }
        ItemStack item = serverSlots.get(slot);
        return item != null ? item.clone() : null;
    }

    /**
     * Sets the authoritative item at a slot
     * @param slot The slot index
     * @param item The item to set (will be cloned)
     */
    public void setAuthoritativeItem(int slot, ItemStack item) {
        if (slot < 0 || slot >= size) {
            return;
        }
        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            serverSlots.remove(slot);
        } else {
            serverSlots.put(slot, item.clone());
        }
    }

    /**
     * Validates that a slot's claimed state matches server truth
     *
     * @param slot The slot index
     * @param claimed The item the client claims is there
     * @return true if matches, false if mismatch (potential exploit)
     */
    public boolean validateSlotState(int slot, ItemStack claimed) {
        ItemStack authoritative = getAuthoritativeItem(slot);

        // Both null/air = valid
        if ((authoritative == null || authoritative.getType() == org.bukkit.Material.AIR) &&
            (claimed == null || claimed.getType() == org.bukkit.Material.AIR)) {
            return true;
        }

        // One null, one not = mismatch
        if (authoritative == null || claimed == null) {
            return false;
        }

        // Compare items
        return itemsMatch(authoritative, claimed);
    }

    /**
     * Gets the cursor item for a player
     * @param player The player
     * @return The cursor item, or null
     */
    public ItemStack getCursorItem(Player player) {
        ItemStack item = cursorState.get(player.getUniqueId());
        return item != null ? item.clone() : null;
    }

    /**
     * Sets the cursor item for a player
     * @param player The player
     * @param item The cursor item (will be cloned)
     */
    public void setCursorItem(Player player, ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            cursorState.remove(player.getUniqueId());
        } else {
            cursorState.put(player.getUniqueId(), item.clone());
        }
    }

    /**
     * Validates that a player's cursor state matches server truth
     *
     * @param player The player
     * @param claimed The item the client claims is on cursor
     * @return true if matches, false if mismatch (potential exploit)
     */
    public boolean validateCursorState(Player player, ItemStack claimed) {
        ItemStack authoritative = getCursorItem(player);

        // Both null/air = valid
        if ((authoritative == null || authoritative.getType() == org.bukkit.Material.AIR) &&
            (claimed == null || claimed.getType() == org.bukkit.Material.AIR)) {
            return true;
        }

        // One null, one not = mismatch
        if (authoritative == null || claimed == null) {
            return false;
        }

        // Compare items
        return itemsMatch(authoritative, claimed);
    }

    /**
     * Forces a resync of inventory for a player
     * Sends the authoritative server state to correct desyncs
     *
     * @param player The player to resync
     * @param inventory The inventory to update
     */
    public void forceResync(Player player, Inventory inventory) {
        // Update all slots with server truth
        for (int i = 0; i < size; i++) {
            ItemStack authoritative = getAuthoritativeItem(i);
            inventory.setItem(i, authoritative);
        }

        // Clear cursor
        player.setItemOnCursor(null);
        cursorState.remove(player.getUniqueId());

        // Update player inventory
        player.updateInventory();

        lastSync.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Checks if inventory is synced for a player
     * @param player The player
     * @param inventory The inventory to check
     * @return true if synced
     */
    public boolean isPlayerSynced(Player player, Inventory inventory) {
        // Check all slots
        for (int i = 0; i < Math.min(size, inventory.getSize()); i++) {
            ItemStack claimed = inventory.getItem(i);
            if (!validateSlotState(i, claimed)) {
                return false;
            }
        }

        // Check cursor
        ItemStack claimedCursor = player.getItemOnCursor();
        return validateCursorState(player, claimedCursor);
    }

    /**
     * Gets the last sync time for a player
     * @param player The player
     * @return Milliseconds since epoch, or 0 if never synced
     */
    public long getLastSyncTime(Player player) {
        return lastSync.getOrDefault(player.getUniqueId(), 0L);
    }

    /**
     * Clears all tracked state for a player
     * @param player The player
     */
    public void clearPlayer(Player player) {
        cursorState.remove(player.getUniqueId());
        lastSync.remove(player.getUniqueId());
    }

    /**
     * Clears all tracked state
     */
    public void clearAll() {
        serverSlots.clear();
        cursorState.clear();
        lastSync.clear();
    }

    /**
     * Compares two ItemStacks for equality
     * Considers type, amount, and metadata
     *
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

        // Check type
        if (item1.getType() != item2.getType()) {
            return false;
        }

        // Check amount
        if (item1.getAmount() != item2.getAmount()) {
            return false;
        }

        // Check durability
        if (item1.getDurability() != item2.getDurability()) {
            return false;
        }

        // Check metadata (enchantments, lore, etc.)
        if (item1.hasItemMeta() != item2.hasItemMeta()) {
            return false;
        }

        if (item1.hasItemMeta()) {
            // Use Bukkit's equals for metadata comparison
            return item1.getItemMeta().equals(item2.getItemMeta());
        }

        return true;
    }

    /**
     * Gets the inventory size
     * @return The size
     */
    public int getSize() {
        return size;
    }
}
