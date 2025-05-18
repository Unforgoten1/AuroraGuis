package dev.aurora.Struct.Animation;

import dev.aurora.Struct.Animation.API.IAnimation;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Consumer;

public interface MultiSlotAnimation extends IAnimation {
    // Returns a map of slot -> (item, click action) for the current frame
    Map<Integer, AnimationSlot> getNextFrame();

    static class AnimationSlot {
        private final ItemStack item;
        private final Consumer<InventoryClickEvent> clickAction;

        public AnimationSlot(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
            this.item = item;
            this.clickAction = clickAction;
        }

        public ItemStack getItem() {
            return item;
        }

        public Consumer<InventoryClickEvent> getClickAction() {
            return clickAction;
        }
    }

    // Default implementations for single-slot compatibility
    @Override
    default ItemStack getNextItem() {
        return null; // Not used for multi-slot
    }

    @Override
    default Consumer<InventoryClickEvent> getClickAction() {
        return null; // Not used for multi-slot
    }
}