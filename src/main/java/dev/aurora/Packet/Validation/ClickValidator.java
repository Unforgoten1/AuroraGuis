package dev.aurora.Packet.Validation;

import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.API.PacketGuiConfig;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates click timing to prevent auto-clicker and spam exploits
 * Tracks click history per player using sliding window algorithm
 *
 * Detects:
 * - Click-Delay exploits (clicks too fast)
 * - Click-Spam exploits (too many clicks per second)
 */
public class ClickValidator {

    private final Map<UUID, Deque<Long>> clickHistory;
    private final PacketGuiConfig config;

    /**
     * Creates a new click validator
     * @param config The configuration
     */
    public ClickValidator(PacketGuiConfig config) {
        this.clickHistory = new ConcurrentHashMap<>();
        this.config = config;
    }

    /**
     * Validates the timing of a click
     * @param player The player
     * @return Result containing validation status and exploit type if failed
     */
    public ValidationResult validateClickTiming(Player player) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        Deque<Long> history = clickHistory.computeIfAbsent(uuid, k -> new ArrayDeque<>());

        // Check minimum delay between clicks
        if (!history.isEmpty()) {
            long lastClick = history.peekLast();
            long timeSinceLastClick = now - lastClick;

            if (timeSinceLastClick < config.getMinClickDelayMs()) {
                return ValidationResult.fail(IPacketGui.ExploitType.CLICK_DELAY,
                        "Click too fast: " + timeSinceLastClick + "ms (min: " + config.getMinClickDelayMs() + "ms)");
            }
        }

        // Check rate limit (sliding window)
        removeOldClicks(history, now - 1000); // Remove clicks older than 1 second

        if (history.size() >= config.getMaxClicksPerSecond()) {
            return ValidationResult.fail(IPacketGui.ExploitType.CLICK_SPAM,
                    "Too many clicks: " + history.size() + " in last second (max: " + config.getMaxClicksPerSecond() + ")");
        }

        // Valid - record this click
        history.addLast(now);

        // Cleanup old data to prevent memory leak
        if (history.size() > config.getMaxClicksPerSecond() * 2) {
            history.removeFirst();
        }

        return ValidationResult.pass();
    }

    /**
     * Validates a click on a specific slot
     * Includes slot bounds checking
     *
     * @param player The player
     * @param slot The slot index
     * @param maxSlots The maximum slot index
     * @return Validation result
     */
    public ValidationResult validateSlotClick(Player player, int slot, int maxSlots) {
        // Check slot bounds
        if (slot < 0 || slot >= maxSlots) {
            return ValidationResult.fail(IPacketGui.ExploitType.INVALID_SLOT,
                    "Invalid slot: " + slot + " (max: " + (maxSlots - 1) + ")");
        }

        // Check timing
        return validateClickTiming(player);
    }

    /**
     * Removes old clicks from history (older than timestamp)
     * @param history The click history
     * @param oldestAllowed The oldest timestamp to keep
     */
    private void removeOldClicks(Deque<Long> history, long oldestAllowed) {
        while (!history.isEmpty() && history.peekFirst() < oldestAllowed) {
            history.removeFirst();
        }
    }

    /**
     * Clears click history for a player
     * @param player The player
     */
    public void clearPlayer(Player player) {
        clickHistory.remove(player.getUniqueId());
    }

    /**
     * Gets the number of clicks in the last second for a player
     * @param player The player
     * @return Click count
     */
    public int getRecentClickCount(Player player) {
        Deque<Long> history = clickHistory.get(player.getUniqueId());
        if (history == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        removeOldClicks(history, now - 1000);
        return history.size();
    }

    /**
     * Cleanup method to remove expired data
     * Should be called periodically
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        clickHistory.entrySet().removeIf(entry -> {
            removeOldClicks(entry.getValue(), now - 5000); // Keep 5 seconds
            return entry.getValue().isEmpty();
        });
    }

    /**
     * Result of validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final IPacketGui.ExploitType exploitType;
        private final String reason;

        private ValidationResult(boolean valid, IPacketGui.ExploitType exploitType, String reason) {
            this.valid = valid;
            this.exploitType = exploitType;
            this.reason = reason;
        }

        public static ValidationResult pass() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult fail(IPacketGui.ExploitType exploitType, String reason) {
            return new ValidationResult(false, exploitType, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public IPacketGui.ExploitType getExploitType() {
            return exploitType;
        }

        public String getReason() {
            return reason;
        }
    }
}
