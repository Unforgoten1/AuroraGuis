package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Pulsing glow effect animation
 * Adds and removes enchantment glow in a pulsing pattern
 */
public class PulsingGlow implements IAnimation {
    private final ItemStack baseItem;
    private final int ticksPerPulse;
    private boolean glowing;

    public PulsingGlow(ItemStack baseItem, int ticksPerPulse) {
        this.baseItem = baseItem.clone();
        this.ticksPerPulse = ticksPerPulse;
        this.glowing = false;
    }

    public PulsingGlow(ItemStack baseItem) {
        this(baseItem, 10);
    }

    @Override
    public ItemStack getNextItem() {
        ItemStack item = baseItem.clone();
        if (glowing) {
            ItemBuilder builder = new ItemBuilder(item);
            builder.glow();
            return builder;
        }
        return item;
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        return null;
    }

    @Override
    public int getDuration() {
        return ticksPerPulse;
    }

    @Override
    public boolean shouldContinue() {
        glowing = !glowing;
        return true; // Always loop
    }

    @Override
    public void init() {
        glowing = false;
    }
}
