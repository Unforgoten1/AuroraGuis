package dev.aurora.Struct.Animation;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class Frame {
    private final ItemStack item;
    private final int duration;
    private final Consumer<InventoryClickEvent> clickAction;

    public Frame(ItemStack item, int duration, Consumer<InventoryClickEvent> clickAction) {
        this.item = item != null ? item.clone() : null;
        this.duration = duration;
        this.clickAction = clickAction;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getDuration() {
        return duration;
    }

    public Consumer<InventoryClickEvent> getClickAction() {
        return clickAction;
    }
}