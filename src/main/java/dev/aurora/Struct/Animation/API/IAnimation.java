package dev.aurora.Struct.Animation.API;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface IAnimation {
    // Get the next item and click action (or null to stop)
    ItemStack getNextItem();

    Consumer<InventoryClickEvent> getClickAction();

    // Duration in ticks for the current frame
    int getDuration();

    // Whether the animation should continue
    boolean shouldContinue();

    // Optional: Initialize or reset the animation
    default void init() {
    }
}