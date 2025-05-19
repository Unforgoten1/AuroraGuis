package dev.aurora.Struct.Condition;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Functional interface for filtering click events
 * Provides factory methods and combinators for common conditions
 */
@FunctionalInterface
public interface ClickCondition {

    /**
     * Test if the click should be processed
     * @param event The click event
     * @return true to allow click, false to deny
     */
    boolean test(InventoryClickEvent event);

    // Factory methods for common conditions

    /**
     * Require player to have specific permission
     * @param permission The permission to check
     * @return Condition that checks permission
     */
    static ClickCondition requirePermission(String permission) {
        return event -> event.getWhoClicked().hasPermission(permission);
    }

    /**
     * Require left click
     * @return Condition that checks for left click
     */
    static ClickCondition requireLeftClick() {
        return event -> event.isLeftClick();
    }

    /**
     * Require right click
     * @return Condition that checks for right click
     */
    static ClickCondition requireRightClick() {
        return event -> event.isRightClick();
    }

    /**
     * Require shift click
     * @return Condition that checks for shift click
     */
    static ClickCondition requireShiftClick() {
        return event -> event.isShiftClick();
    }

    /**
     * Require current item to be non-null
     * @return Condition that checks for item presence
     */
    static ClickCondition requireItem() {
        return event -> event.getCurrentItem() != null;
    }

    // Combinators

    /**
     * Combine this condition with another using AND logic
     * @param other The other condition
     * @return Combined condition
     */
    default ClickCondition and(ClickCondition other) {
        return event -> this.test(event) && other.test(event);
    }

    /**
     * Combine this condition with another using OR logic
     * @param other The other condition
     * @return Combined condition
     */
    default ClickCondition or(ClickCondition other) {
        return event -> this.test(event) || other.test(event);
    }

    /**
     * Negate this condition
     * @return Negated condition
     */
    default ClickCondition negate() {
        return event -> !this.test(event);
    }
}
