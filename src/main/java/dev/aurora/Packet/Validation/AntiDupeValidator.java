package dev.aurora.Packet.Validation;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.API.PacketGuiConfig;
import dev.aurora.Packet.API.ValidationLevel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;

/**
 * Master coordinator for anti-dupe validation
 * Orchestrates all validation components (click, cursor, transaction, fingerprint)
 * Decides which validations to apply based on ValidationLevel
 */
public class AntiDupeValidator {

    private final IPacketGui gui;
    private final PacketGuiConfig config;

    // Validation components
    private final ClickValidator clickValidator;
    private final CursorTracker cursorTracker;
    private final TransactionValidator transactionValidator;
    private final ServerSideInventory serverInventory;
    private final ViolationLogger violationLogger;

    /**
     * Creates a new anti-dupe validator
     * @param gui The GUI being validated
     * @param config The validation configuration
     */
    public AntiDupeValidator(IPacketGui gui, PacketGuiConfig config) {
        this.gui = gui;
        this.config = config;

        // Initialize validators
        this.clickValidator = new ClickValidator(config);
        this.cursorTracker = new CursorTracker();
        this.transactionValidator = new TransactionValidator(config, gui.getManager().getPlugin());
        this.serverInventory = new ServerSideInventory(gui.getSize());

        // Initialize violation logger
        File logDir = new File(gui.getManager().getPlugin().getDataFolder(), "logs");
        this.violationLogger = new ViolationLogger(logDir, config.isLogViolations());

        // Initialize server inventory when GUI is created
        if (gui.getInventory() != null) {
            serverInventory.initializeFromInventory(gui.getInventory());
        }
    }

    /**
     * Validates a click packet
     * Applies validation based on configured ValidationLevel
     *
     * @param player The player clicking
     * @param packet The click packet
     * @return true if valid, false if exploit detected
     */
    public boolean validateClick(Player player, WrapperPlayClientClickWindow packet) {
        ValidationLevel level = config.getValidationLevel();

        // BASIC level: No packet validation (just Bukkit events)
        if (level == ValidationLevel.BASIC) {
            return true;
        }

        // PACKET level and above: Validate timing
        ClickValidator.ValidationResult timingResult = clickValidator.validateSlotClick(
                player,
                packet.getSlot(),
                gui.getSize()
        );

        if (!timingResult.isValid()) {
            handleViolation(player, timingResult);
            return false;
        }

        // PACKET level and above: Validate cursor state
        // Use player's actual cursor from Bukkit (accurate at packet interception time)
        ItemStack claimedCursor = player.getItemOnCursor();
        ClickValidator.ValidationResult cursorResult = cursorTracker.validateCursorAction(player, claimedCursor);

        if (!cursorResult.isValid()) {
            handleViolation(player, cursorResult);
            return false;
        }

        // ADVANCED level: Validate item fingerprints
        if (level == ValidationLevel.ADVANCED) {
            ItemStack clickedItem = gui.getInventory().getItem(packet.getSlot());

            if (clickedItem != null) {
                ItemFingerprint fingerprint = ItemFingerprint.create(clickedItem);

                // For shift-clicks, validate against loop exploits
                // Check using packet button (button 0 with shift modifier is shift-click)
                if (packet.getButton() == 0 && player.isSneaking()) {
                    ClickValidator.ValidationResult shiftResult = transactionValidator.validateShiftClick(player, fingerprint);

                    if (!shiftResult.isValid()) {
                        handleViolation(player, shiftResult);
                        return false;
                    }
                }
            }
        }

        // All validations passed
        return true;
    }

    /**
     * Validates a close packet
     * Ensures inventory state is correct when GUI is closed
     *
     * @param player The player
     * @return true if valid, false if desync detected
     */
    public boolean validateClose(Player player) {
        ValidationLevel level = config.getValidationLevel();

        // BASIC level: No validation
        if (level == ValidationLevel.BASIC) {
            return true;
        }

        // PACKET level and above: Check for desyncs
        boolean synced = serverInventory.isPlayerSynced(player, gui.getInventory());

        if (!synced) {
            // Log violation
            violationLogger.logViolation(
                    player,
                    gui,
                    IPacketGui.ExploitType.CLOSE_DESYNC,
                    "Inventory desync detected on close"
            );

            // Trigger GUI violation handler
            if (gui instanceof dev.aurora.Packet.Core.PacketGui) {
                ((dev.aurora.Packet.Core.PacketGui) gui).triggerViolation(
                        player,
                        IPacketGui.ExploitType.CLOSE_DESYNC
                );
            }

            // Auto-rollback if configured
            if (config.isAutoRollbackOnViolation()) {
                forceResync(player);
            }

            return false;
        }

        return true;
    }

    /**
     * Forces a resync of inventory state for a player
     * Sends the authoritative server state to correct any desyncs
     *
     * @param player The player to resync
     */
    public void forceResync(Player player) {
        serverInventory.forceResync(player, gui.getInventory());
        cursorTracker.forceResyncCursor(player);
    }

    /**
     * Checks if a player's inventory is synced with server truth
     * @param player The player
     * @return true if synced
     */
    public boolean isPlayerSynced(Player player) {
        return serverInventory.isPlayerSynced(player, gui.getInventory()) &&
                cursorTracker.isCursorSynced(player);
    }

    /**
     * Handles a validation failure
     * Logs the violation and triggers appropriate responses
     *
     * @param player The player
     * @param result The validation result containing exploit info
     */
    private void handleViolation(Player player, ClickValidator.ValidationResult result) {
        IPacketGui.ExploitType exploitType = result.getExploitType();
        String reason = result.getReason();

        // Log violation
        violationLogger.logViolation(player, gui, exploitType, reason);

        // Trigger GUI violation handler
        if (gui instanceof dev.aurora.Packet.Core.PacketGui) {
            ((dev.aurora.Packet.Core.PacketGui) gui).triggerViolation(player, exploitType);
        }

        // Check kick threshold
        if (config.isKickOnViolation()) {
            int totalViolations = violationLogger.getTotalViolations(player);
            if (totalViolations >= config.getViolationKickThreshold()) {
                player.kickPlayer("§cExploit attempt detected\n§7" + exploitType.getDescription());
            }
        }

        // Auto-rollback if configured
        if (config.isAutoRollbackOnViolation()) {
            forceResync(player);
        }
    }

    /**
     * Initializes validation for a player opening the GUI
     * @param player The player
     */
    public void initializePlayer(Player player) {
        // Initialize cursor tracking
        cursorTracker.initializeCursor(player, player.getItemOnCursor());

        // Initialize server inventory truth
        serverInventory.initializeFromInventory(gui.getInventory());
    }

    /**
     * Cleans up validator resources for a player
     * @param player The player
     */
    public void cleanup(Player player) {
        clickValidator.clearPlayer(player);
        cursorTracker.clearPlayer(player);
        transactionValidator.clearPlayer(player);
        serverInventory.clearPlayer(player);
    }

    /**
     * Gets the click validator
     * @return The click validator
     */
    public ClickValidator getClickValidator() {
        return clickValidator;
    }

    /**
     * Gets the cursor tracker
     * @return The cursor tracker
     */
    public CursorTracker getCursorTracker() {
        return cursorTracker;
    }

    /**
     * Gets the transaction validator
     * @return The transaction validator
     */
    public TransactionValidator getTransactionValidator() {
        return transactionValidator;
    }

    /**
     * Gets the server-side inventory tracker
     * @return The server inventory
     */
    public ServerSideInventory getServerInventory() {
        return serverInventory;
    }

    /**
     * Gets the violation logger
     * @return The violation logger
     */
    public ViolationLogger getViolationLogger() {
        return violationLogger;
    }

    /**
     * Periodic cleanup of expired validator data
     * Should be called by a scheduled task
     */
    public void periodicCleanup() {
        clickValidator.cleanup();
        transactionValidator.cleanup();
    }
}
