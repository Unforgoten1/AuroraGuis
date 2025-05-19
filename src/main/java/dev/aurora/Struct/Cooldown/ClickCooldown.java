package dev.aurora.Struct.Cooldown;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages click cooldowns to prevent spam clicking
 * Supports per-slot and global GUI cooldowns
 */
public class ClickCooldown {
    private final Map<UUID, Long> globalCooldowns;
    private final Map<UUID, Map<Integer, Long>> slotCooldowns;
    private long defaultCooldown;
    private boolean enabled;

    /**
     * Creates a new cooldown manager
     */
    public ClickCooldown() {
        this.globalCooldowns = new ConcurrentHashMap<>();
        this.slotCooldowns = new ConcurrentHashMap<>();
        this.defaultCooldown = 0;
        this.enabled = true;
    }

    /**
     * Sets the default cooldown duration
     *
     * @param milliseconds Cooldown duration in milliseconds
     */
    public void setDefaultCooldown(long milliseconds) {
        this.defaultCooldown = milliseconds;
    }

    /**
     * Gets the default cooldown duration
     *
     * @return Cooldown duration in milliseconds
     */
    public long getDefaultCooldown() {
        return defaultCooldown;
    }

    /**
     * Sets whether cooldowns are enabled
     *
     * @param enabled true to enable cooldowns
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if cooldowns are enabled
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if a player can click (global cooldown)
     *
     * @param player The player
     * @return true if the player can click
     */
    public boolean canClick(Player player) {
        if (!enabled || defaultCooldown <= 0) return true;

        Long lastClick = globalCooldowns.get(player.getUniqueId());
        if (lastClick == null) return true;

        return System.currentTimeMillis() - lastClick >= defaultCooldown;
    }

    /**
     * Checks if a player can click a specific slot
     *
     * @param player The player
     * @param slot The slot number
     * @param slotCooldown The slot-specific cooldown (0 for default)
     * @return true if the player can click the slot
     */
    public boolean canClickSlot(Player player, int slot, long slotCooldown) {
        if (!enabled) return true;

        // Check global cooldown first
        if (!canClick(player)) return false;

        // If no slot-specific cooldown, allow
        if (slotCooldown <= 0) return true;

        Map<Integer, Long> playerSlots = slotCooldowns.get(player.getUniqueId());
        if (playerSlots == null) return true;

        Long lastClick = playerSlots.get(slot);
        if (lastClick == null) return true;

        return System.currentTimeMillis() - lastClick >= slotCooldown;
    }

    /**
     * Records a click for global cooldown
     *
     * @param player The player
     */
    public void recordClick(Player player) {
        if (!enabled) return;
        globalCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Records a click for a specific slot
     *
     * @param player The player
     * @param slot The slot number
     */
    public void recordSlotClick(Player player, int slot) {
        if (!enabled) return;

        recordClick(player); // Also record global

        slotCooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(slot, System.currentTimeMillis());
    }

    /**
     * Gets the remaining cooldown time for a player (global)
     *
     * @param player The player
     * @return Remaining milliseconds, or 0 if no cooldown
     */
    public long getRemainingCooldown(Player player) {
        if (!enabled || defaultCooldown <= 0) return 0;

        Long lastClick = globalCooldowns.get(player.getUniqueId());
        if (lastClick == null) return 0;

        long elapsed = System.currentTimeMillis() - lastClick;
        long remaining = defaultCooldown - elapsed;

        return Math.max(0, remaining);
    }

    /**
     * Gets the remaining cooldown time for a specific slot
     *
     * @param player The player
     * @param slot The slot number
     * @param slotCooldown The slot-specific cooldown
     * @return Remaining milliseconds, or 0 if no cooldown
     */
    public long getRemainingSlotCooldown(Player player, int slot, long slotCooldown) {
        if (!enabled || slotCooldown <= 0) return 0;

        Map<Integer, Long> playerSlots = slotCooldowns.get(player.getUniqueId());
        if (playerSlots == null) return 0;

        Long lastClick = playerSlots.get(slot);
        if (lastClick == null) return 0;

        long elapsed = System.currentTimeMillis() - lastClick;
        long remaining = slotCooldown - elapsed;

        return Math.max(0, remaining);
    }

    /**
     * Clears all cooldowns for a player
     *
     * @param player The player
     */
    public void clearCooldowns(Player player) {
        globalCooldowns.remove(player.getUniqueId());
        slotCooldowns.remove(player.getUniqueId());
    }

    /**
     * Clears a specific slot cooldown for a player
     *
     * @param player The player
     * @param slot The slot number
     */
    public void clearSlotCooldown(Player player, int slot) {
        Map<Integer, Long> playerSlots = slotCooldowns.get(player.getUniqueId());
        if (playerSlots != null) {
            playerSlots.remove(slot);
        }
    }

    /**
     * Clears all cooldowns
     */
    public void clearAll() {
        globalCooldowns.clear();
        slotCooldowns.clear();
    }

    /**
     * Cleans up expired cooldowns
     * Should be called periodically to prevent memory leaks
     */
    public void cleanup() {
        long now = System.currentTimeMillis();

        // Clean global cooldowns
        globalCooldowns.entrySet().removeIf(entry ->
            now - entry.getValue() > defaultCooldown * 2
        );

        // Clean slot cooldowns
        slotCooldowns.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(slotEntry ->
                now - slotEntry.getValue() > 60000 // 1 minute max
            );
            return entry.getValue().isEmpty();
        });
    }

    /**
     * Gets the number of players with active cooldowns
     *
     * @return Number of players
     */
    public int getActiveCooldownCount() {
        return globalCooldowns.size() + slotCooldowns.size();
    }
}
