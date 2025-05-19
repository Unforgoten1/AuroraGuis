package dev.aurora.Packet.Validation;

import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.API.PacketGuiConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Validates post-click transactions to detect shift-click loops and item mismatches
 * Tracks recently processed items to prevent rapid duplication
 *
 * Detects:
 * - Shift-Click-Loop exploits (same item shift-clicked rapidly)
 * - Transaction-Mismatch (post-click state doesn't match expected)
 */
public class TransactionValidator {

    private final Map<UUID, Set<ItemFingerprint>> processedItems;
    private final PacketGuiConfig config;
    private final JavaPlugin plugin;

    /**
     * Creates a new transaction validator
     * @param config The configuration
     * @param plugin The plugin instance
     */
    public TransactionValidator(PacketGuiConfig config, JavaPlugin plugin) {
        this.processedItems = new ConcurrentHashMap<>();
        this.config = config;
        this.plugin = plugin;
    }

    /**
     * Validates a shift-click to prevent loop exploits
     * @param player The player
     * @param fingerprint The item fingerprint
     * @return Validation result
     */
    public ClickValidator.ValidationResult validateShiftClick(Player player, ItemFingerprint fingerprint) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            return ClickValidator.ValidationResult.pass();
        }

        UUID uuid = player.getUniqueId();
        Set<ItemFingerprint> processed = processedItems.computeIfAbsent(uuid, k -> new CopyOnWriteArraySet<>());

        // Check if this exact item was just processed
        if (processed.contains(fingerprint)) {
            return ClickValidator.ValidationResult.fail(
                    IPacketGui.ExploitType.SHIFT_CLICK_LOOP,
                    "Same item shift-clicked rapidly: " + fingerprint.getMaterialId()
            );
        }

        // Add to processed set
        processed.add(fingerprint);

        // Schedule cleanup after 100ms (2 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Set<ItemFingerprint> current = processedItems.get(uuid);
            if (current != null) {
                current.remove(fingerprint);
                if (current.isEmpty()) {
                    processedItems.remove(uuid);
                }
            }
        }, 2L);

        return ClickValidator.ValidationResult.pass();
    }

    /**
     * Validates a transaction result against expected state
     * Used for ADVANCED validation level
     *
     * @param player The player
     * @param expectedFingerprints Expected item fingerprints after transaction
     * @param actualFingerprints Actual item fingerprints after transaction
     * @return Validation result
     */
    public ClickValidator.ValidationResult validateTransaction(
            Player player,
            Map<Integer, ItemFingerprint> expectedFingerprints,
            Map<Integer, ItemFingerprint> actualFingerprints) {

        // Check if all expected fingerprints match actual
        for (Map.Entry<Integer, ItemFingerprint> entry : expectedFingerprints.entrySet()) {
            int slot = entry.getKey();
            ItemFingerprint expected = entry.getValue();
            ItemFingerprint actual = actualFingerprints.get(slot);

            if (!fingerprintsMatch(expected, actual)) {
                return ClickValidator.ValidationResult.fail(
                        IPacketGui.ExploitType.TRANSACTION_MISMATCH,
                        "Transaction mismatch at slot " + slot + ": expected " + expected + ", got " + actual
                );
            }
        }

        return ClickValidator.ValidationResult.pass();
    }

    /**
     * Marks the start of a transaction for tracking
     * @param player The player
     */
    public void beginTransaction(Player player) {
        // Clear any pending processed items for clean state
        Set<ItemFingerprint> processed = processedItems.get(player.getUniqueId());
        if (processed != null && processed.size() > 10) {
            // Prevent memory leak from rapid clicks
            processed.clear();
        }
    }

    /**
     * Marks the end of a transaction
     * @param player The player
     */
    public void endTransaction(Player player) {
        // Cleanup is automatic via scheduled tasks
    }

    /**
     * Clears processed items for a player
     * @param player The player
     */
    public void clearPlayer(Player player) {
        processedItems.remove(player.getUniqueId());
    }

    /**
     * Gets the number of recently processed items for a player
     * @param player The player
     * @return Count
     */
    public int getProcessedItemCount(Player player) {
        Set<ItemFingerprint> processed = processedItems.get(player.getUniqueId());
        return processed != null ? processed.size() : 0;
    }

    /**
     * Cleanup method to remove expired data
     * Should be called periodically
     */
    public void cleanup() {
        processedItems.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Checks if two fingerprints match
     * @param fp1 First fingerprint
     * @param fp2 Second fingerprint
     * @return true if match
     */
    private boolean fingerprintsMatch(ItemFingerprint fp1, ItemFingerprint fp2) {
        if (fp1 == null && fp2 == null) {
            return true;
        }
        if (fp1 == null || fp2 == null) {
            return fp1 != null ? fp1.isEmpty() : fp2.isEmpty();
        }
        return fp1.equals(fp2);
    }
}
