package dev.aurora.Packet.API;

import dev.aurora.GUI.IGui;
import dev.aurora.Packet.Validation.AntiDupeValidator;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * Extended interface for packet-based GUIs
 * Provides additional packet-specific functionality on top of the base IGui interface
 */
public interface IPacketGui extends IGui {

    /**
     * Gets the validation configuration for this GUI
     * @return The packet GUI configuration
     */
    PacketGuiConfig getConfig();

    /**
     * Gets the validation level for this GUI
     * @return The validation level
     */
    ValidationLevel getValidationLevel();

    /**
     * Sets the validation level for this GUI
     * @param level The validation level
     * @return This GUI for chaining
     */
    IPacketGui setValidationLevel(ValidationLevel level);

    /**
     * Gets the anti-dupe validator for this GUI
     * @return The validator
     */
    AntiDupeValidator getValidator();

    /**
     * Sets a violation handler to be called when exploit attempts are detected
     * @param handler Handler accepting (Player, ExploitType)
     * @return This GUI for chaining
     */
    IPacketGui onViolation(BiConsumer<Player, ExploitType> handler);

    /**
     * Forces a resync of the inventory for a specific player
     * Sends the authoritative server state to correct any desyncs
     *
     * @param player The player to resync
     */
    void forceResync(Player player);

    /**
     * Checks if a player's inventory state is synced with server
     * @param player The player
     * @return true if synced, false if desync detected
     */
    boolean isPlayerSynced(Player player);

    /**
     * Enum defining types of exploit attempts that can be detected
     */
    enum ExploitType {
        /**
         * Click-Delay: Player clicked too rapidly (< min delay)
         */
        CLICK_DELAY,

        /**
         * Click-Spam: Player exceeded max clicks per second
         */
        CLICK_SPAM,

        /**
         * Close-Desync: Player closed GUI with inventory desync
         */
        CLOSE_DESYNC,

        /**
         * Cursor-Duplication: Cursor item amount illegally increased
         */
        CURSOR_DUPLICATION,

        /**
         * Cursor-Swap: Cursor item type changed unexpectedly
         */
        CURSOR_SWAP,

        /**
         * NBT-Injection: Item NBT was tampered with
         */
        NBT_INJECTION,

        /**
         * Shift-Click-Loop: Same item shift-clicked multiple times rapidly
         */
        SHIFT_CLICK_LOOP,

        /**
         * Invalid-Slot: Click on slot that doesn't exist
         */
        INVALID_SLOT,

        /**
         * Transaction-Mismatch: Post-click state doesn't match expected
         */
        TRANSACTION_MISMATCH,

        /**
         * No-Close-Packet: Player withheld close packet to keep GUI open
         */
        NO_CLOSE_PACKET,

        /**
         * Stale-Session: GUI session timed out (player AFK or packet manipulation)
         */
        STALE_SESSION;

        /**
         * Gets a human-readable description of this exploit type
         * @return Description
         */
        public String getDescription() {
            switch (this) {
                case CLICK_DELAY:
                    return "Clicked too rapidly (possible auto-clicker)";
                case CLICK_SPAM:
                    return "Exceeded maximum clicks per second";
                case CLOSE_DESYNC:
                    return "Closed GUI with mismatched inventory state";
                case CURSOR_DUPLICATION:
                    return "Attempted to duplicate item on cursor";
                case CURSOR_SWAP:
                    return "Attempted to swap cursor item illegally";
                case NBT_INJECTION:
                    return "Attempted to inject or modify NBT data";
                case SHIFT_CLICK_LOOP:
                    return "Rapid shift-click exploit detected";
                case INVALID_SLOT:
                    return "Clicked on invalid slot index";
                case TRANSACTION_MISMATCH:
                    return "Inventory state mismatch after transaction";
                case NO_CLOSE_PACKET:
                    return "Withheld close packet to keep GUI open";
                case STALE_SESSION:
                    return "GUI session expired (possible packet manipulation)";
                default:
                    return "Unknown exploit";
            }
        }

        /**
         * Gets the severity level of this exploit (1-5, 5 being most severe)
         * @return Severity level
         */
        public int getSeverity() {
            switch (this) {
                case CURSOR_DUPLICATION:
                case NBT_INJECTION:
                case TRANSACTION_MISMATCH:
                case NO_CLOSE_PACKET:
                    return 5; // Critical - direct duplication
                case CLOSE_DESYNC:
                case SHIFT_CLICK_LOOP:
                case STALE_SESSION:
                    return 4; // Serious - likely duplication attempt
                case CURSOR_SWAP:
                case INVALID_SLOT:
                    return 3; // Moderate - suspicious activity
                case CLICK_DELAY:
                case CLICK_SPAM:
                    return 2; // Minor - might be auto-clicker
                default:
                    return 1;
            }
        }
    }
}
