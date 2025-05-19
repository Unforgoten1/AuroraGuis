package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Loading bar animation that fills up over time
 * Useful for progress indicators
 */
public class LoadingBar implements IAnimation {
    private final int maxSteps;
    private final int ticksPerStep;
    private final Material emptyMaterial;
    private final Material filledMaterial;
    private final short emptyData;
    private final short filledData;
    private int currentStep;
    private boolean loop;

    public LoadingBar(int maxSteps, int ticksPerStep) {
        this(maxSteps, ticksPerStep, Material.GRAY_STAINED_GLASS_PANE, (short) 7, (short) 5, false);
    }

    public LoadingBar(int maxSteps, int ticksPerStep, Material emptyMaterial, short emptyData, short filledData, boolean loop) {
        this.maxSteps = maxSteps;
        this.ticksPerStep = ticksPerStep;
        this.emptyMaterial = emptyMaterial;
        this.filledMaterial = emptyMaterial;
        this.emptyData = emptyData;
        this.filledData = filledData;
        this.currentStep = 0;
        this.loop = loop;
    }

    @Override
    public ItemStack getNextItem() {
        int percentage = (int) ((currentStep / (double) maxSteps) * 100);
        String bars = generateProgressBar(currentStep, maxSteps);

        return new ItemBuilder(currentStep == 0 ? emptyMaterial : filledMaterial)
            .name("&aLoading... " + percentage + "%")
            .lore(
                "&7" + bars,
                "",
                "&7Step " + currentStep + "/" + maxSteps
            )
            .durability(currentStep == 0 ? emptyData : filledData)
            .build();
    }

    private String generateProgressBar(int current, int max) {
        int filled = (int) ((current / (double) max) * 20);
        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < 20; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                if (i == filled) bar.append("&7");
                bar.append("█");
            }
        }
        return bar.toString();
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        return null;
    }

    @Override
    public int getDuration() {
        return ticksPerStep;
    }

    @Override
    public boolean shouldContinue() {
        currentStep++;
        if (currentStep >= maxSteps) {
            if (loop) {
                currentStep = 0;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        currentStep = 0;
    }
}
