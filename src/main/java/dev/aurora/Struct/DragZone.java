package dev.aurora.Struct;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Defines a drag-and-drop zone within a GUI
 * Zones control where items can be dragged from and dropped to
 */
public class DragZone {
    private final String name;
    private final Set<Integer> slots;
    private final BiPredicate<Player, ItemStack> validator;
    private final Set<String> allowedTargets;
    private final boolean readOnly;
    private final String errorMessage;

    private DragZone(String name, Set<Integer> slots, BiPredicate<Player, ItemStack> validator,
                    Set<String> allowedTargets, boolean readOnly, String errorMessage) {
        this.name = name;
        this.slots = slots;
        this.validator = validator;
        this.allowedTargets = allowedTargets;
        this.readOnly = readOnly;
        this.errorMessage = errorMessage;
    }

    /**
     * Builder for creating drag zones
     */
    public static class Builder {
        private final String name;
        private final Set<Integer> slots = new HashSet<>();
        private BiPredicate<Player, ItemStack> validator;
        private final Set<String> allowedTargets = new HashSet<>();
        private boolean readOnly = false;
        private String errorMessage = "&cCannot place item here!";

        public Builder(String name) {
            this.name = name;
        }

        /**
         * Adds a slot to this zone
         */
        public Builder addSlot(int slot) {
            slots.add(slot);
            return this;
        }

        /**
         * Adds multiple slots to this zone
         */
        public Builder addSlots(int... slots) {
            for (int slot : slots) {
                this.slots.add(slot);
            }
            return this;
        }

        /**
         * Adds a range of slots (inclusive)
         */
        public Builder addSlotRange(int start, int end) {
            for (int i = start; i <= end; i++) {
                slots.add(i);
            }
            return this;
        }

        /**
         * Sets the validator for items dropped into this zone
         */
        public Builder validator(BiPredicate<Player, ItemStack> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Allows dragging to a specific target zone
         */
        public Builder allowDragTo(String targetZone) {
            allowedTargets.add(targetZone);
            return this;
        }

        /**
         * Makes this zone read-only (can drag out, cannot drag in)
         */
        public Builder readOnly() {
            this.readOnly = true;
            return this;
        }

        /**
         * Sets the error message shown when validation fails
         */
        public Builder errorMessage(String message) {
            this.errorMessage = message;
            return this;
        }

        public DragZone build() {
            return new DragZone(name, slots, validator, allowedTargets, readOnly, errorMessage);
        }
    }

    public String getName() {
        return name;
    }

    public Set<Integer> getSlots() {
        return slots;
    }

    public boolean containsSlot(int slot) {
        return slots.contains(slot);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Checks if items can be dragged from this zone to the target zone
     */
    public boolean canDragTo(String targetZone) {
        return allowedTargets.isEmpty() || allowedTargets.contains(targetZone);
    }

    /**
     * Validates if an item can be placed in this zone
     */
    public boolean validateItem(Player player, ItemStack item) {
        if (readOnly) return false;
        if (validator == null) return true;
        return validator.test(player, item);
    }
}
