package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Typewriter effect animation that reveals text one character at a time
 */
public class TypewriterAnimation implements IAnimation {
    private final String fullText;
    private final Material material;
    private final int ticksPerChar;
    private final boolean loop;
    private int currentIndex;

    public TypewriterAnimation(String fullText, Material material, int ticksPerChar, boolean loop) {
        this.fullText = fullText;
        this.material = material;
        this.ticksPerChar = ticksPerChar;
        this.loop = loop;
        this.currentIndex = 0;
    }

    public TypewriterAnimation(String fullText, Material material) {
        this(fullText, material, 2, false);
    }

    @Override
    public ItemStack getNextItem() {
        String currentText = fullText.substring(0, Math.min(currentIndex + 1, fullText.length()));
        String cursor = currentIndex < fullText.length() ? "&f_" : "";

        return new ItemBuilder(material)
            .name("&e" + currentText + cursor)
            .build();
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        return null;
    }

    @Override
    public int getDuration() {
        return ticksPerChar;
    }

    @Override
    public boolean shouldContinue() {
        currentIndex++;
        if (currentIndex >= fullText.length()) {
            if (loop) {
                currentIndex = 0;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        currentIndex = 0;
    }
}
