package dev.aurora.Struct;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Manages GUI locking to prevent closing or force completion
 * Provides different lock modes for various use cases
 */
public class GuiLock {
    private LockMode lockMode;
    private boolean escapeKeyDisabled;
    private Predicate<Player> unlockCondition;
    private final Set<UUID> bypassPlayers;
    private String lockMessage;

    /**
     * Lock modes for GUI behavior
     */
    public enum LockMode {
        /** No locking - normal behavior */
        NONE,
        /** Prevents closing until completed */
        FORCE_COMPLETION,
        /** Prevents closing entirely (use with caution!) */
        PREVENT_CLOSE,
        /** Requires permission to close */
        REQUIRE_PERMISSION,
        /** Conditional unlock based on custom predicate */
        CONDITIONAL
    }

    /**
     * Creates a new GUI lock with no locking
     */
    public GuiLock() {
        this.lockMode = LockMode.NONE;
        this.escapeKeyDisabled = false;
        this.bypassPlayers = new HashSet<>();
        this.lockMessage = "§cYou must complete this before closing!";
    }

    /**
     * Sets the lock mode
     *
     * @param mode The lock mode
     */
    public void setLockMode(LockMode mode) {
        this.lockMode = mode;
    }

    /**
     * Gets the lock mode
     *
     * @return The current lock mode
     */
    public LockMode getLockMode() {
        return lockMode;
    }

    /**
     * Sets whether the escape key is disabled
     *
     * @param disabled true to disable ESC key
     */
    public void setEscapeKeyDisabled(boolean disabled) {
        this.escapeKeyDisabled = disabled;
    }

    /**
     * Checks if escape key is disabled
     *
     * @return true if ESC is disabled
     */
    public boolean isEscapeKeyDisabled() {
        return escapeKeyDisabled;
    }

    /**
     * Sets the unlock condition (for CONDITIONAL mode)
     *
     * @param condition The condition predicate
     */
    public void setUnlockCondition(Predicate<Player> condition) {
        this.unlockCondition = condition;
    }

    /**
     * Gets the unlock condition
     *
     * @return The unlock condition
     */
    public Predicate<Player> getUnlockCondition() {
        return unlockCondition;
    }

    /**
     * Adds a player who can bypass the lock
     *
     * @param player The player
     */
    public void addBypassPlayer(Player player) {
        bypassPlayers.add(player.getUniqueId());
    }

    /**
     * Removes a player from bypass list
     *
     * @param player The player
     */
    public void removeBypassPlayer(Player player) {
        bypassPlayers.remove(player.getUniqueId());
    }

    /**
     * Checks if a player can bypass the lock
     *
     * @param player The player
     * @return true if can bypass
     */
    public boolean canBypass(Player player) {
        return bypassPlayers.contains(player.getUniqueId());
    }

    /**
     * Sets the message shown when lock prevents closing
     *
     * @param message The message
     */
    public void setLockMessage(String message) {
        this.lockMessage = message;
    }

    /**
     * Gets the lock message
     *
     * @return The lock message
     */
    public String getLockMessage() {
        return lockMessage;
    }

    /**
     * Checks if a player can close the GUI
     *
     * @param player The player
     * @param event The close event
     * @return true if allowed to close
     */
    public boolean canClose(Player player, InventoryCloseEvent event) {
        // Check bypass
        if (canBypass(player)) {
            return true;
        }

        // Check lock mode
        switch (lockMode) {
            case NONE:
                return true;

            case PREVENT_CLOSE:
                return false;

            case FORCE_COMPLETION:
                // Allow closing only if inventory is empty or specific condition met
                return false;

            case REQUIRE_PERMISSION:
                return player.hasPermission("aurora.gui.close");

            case CONDITIONAL:
                if (unlockCondition != null) {
                    return unlockCondition.test(player);
                }
                return true;

            default:
                return true;
        }
    }

    /**
     * Handles a close attempt
     *
     * @param player The player
     * @param event The close event
     * @return true if close was prevented
     */
    public boolean handleCloseAttempt(Player player, InventoryCloseEvent event) {
        if (canClose(player, event)) {
            return false; // Not prevented
        }

        // Prevent close
        player.sendMessage(lockMessage);

        // Reopen the inventory after a tick
        org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugins()[0],
                () -> player.openInventory(event.getInventory()),
                1L
        );

        return true; // Prevented
    }

    /**
     * Unlocks the GUI
     */
    public void unlock() {
        this.lockMode = LockMode.NONE;
    }

    /**
     * Checks if the GUI is locked
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return lockMode != LockMode.NONE;
    }

    /**
     * Clears all bypass players
     */
    public void clearBypassPlayers() {
        bypassPlayers.clear();
    }

    // ==================== Pre-built Locks ====================

    /**
     * Creates a lock that prevents closing until completion
     */
    public static GuiLock forceCompletion() {
        GuiLock lock = new GuiLock();
        lock.setLockMode(LockMode.FORCE_COMPLETION);
        lock.setEscapeKeyDisabled(true);
        lock.setLockMessage("§cPlease complete this form before closing!");
        return lock;
    }

    /**
     * Creates a lock that requires permission to close
     */
    public static GuiLock requirePermission(String permission) {
        GuiLock lock = new GuiLock();
        lock.setLockMode(LockMode.REQUIRE_PERMISSION);
        lock.setLockMessage("§cYou don't have permission to close this GUI!");
        return lock;
    }

    /**
     * Creates a conditional lock
     */
    public static GuiLock conditional(Predicate<Player> condition, String message) {
        GuiLock lock = new GuiLock();
        lock.setLockMode(LockMode.CONDITIONAL);
        lock.setUnlockCondition(condition);
        lock.setLockMessage(message);
        return lock;
    }

    /**
     * Creates a lock that prevents all closing (use with extreme caution!)
     */
    public static GuiLock preventClose() {
        GuiLock lock = new GuiLock();
        lock.setLockMode(LockMode.PREVENT_CLOSE);
        lock.setEscapeKeyDisabled(true);
        lock.setLockMessage("§c§lThis GUI cannot be closed!");
        return lock;
    }
}
