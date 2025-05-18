package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class PulsingBorder implements IAnimation {
    private final ItemBuilder glass;
    private int brightness;
    private final int[] brightnessLevels = {0, 4, 8, 12, 8, 4}; // Data values for brightness

    public PulsingBorder() {
        this.glass = new ItemBuilder(Material.STAINED_GLASS_PANE);
        this.brightness = 0;
    }

    @Override
    public ItemStack getNextItem() {
        return glass.durability(brightnessLevels[brightness])
                .name("Pulse " + brightnessLevels[brightness]);
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        return event -> event.getWhoClicked().sendMessage("Clicked pulsing border!");
    }

    @Override
    public int getDuration() {
        return 15; // 0.75 seconds per brightness level
    }

    @Override
    public boolean shouldContinue() {
        brightness = (brightness + 1) % brightnessLevels.length;
        return true; // Loop indefinitely
    }

    @Override
    public void init() {
        brightness = 0;
    }
}