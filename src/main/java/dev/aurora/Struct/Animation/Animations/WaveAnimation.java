package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.MultiSlotAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates a wave effect across a row of slots
 * Items pulse up and down in sequence like a wave
 */
public class WaveAnimation implements MultiSlotAnimation {
    private final int[] slots;
    private final ItemStack baseItem;
    private final ItemStack pulseItem;
    private final int waveLength;
    private final int frameDuration;
    private int currentOffset;
    private boolean loop;

    /**
     * Creates a wave animation across the specified slots
     *
     * @param slots The slots to animate (in order)
     * @param baseItem The item to display when not pulsing
     * @param pulseItem The item to display when pulsing
     * @param waveLength Number of slots in the wave peak
     */
    public WaveAnimation(int[] slots, ItemStack baseItem, ItemStack pulseItem, int waveLength) {
        this.slots = slots;
        this.baseItem = baseItem;
        this.pulseItem = pulseItem;
        this.waveLength = waveLength;
        this.frameDuration = 3; // 0.15 seconds per frame
        this.currentOffset = 0;
        this.loop = true;
    }

    /**
     * Creates a wave animation with default glowing effect
     *
     * @param slots The slots to animate
     * @param material The material to use
     * @param waveLength Number of slots in the wave peak
     */
    public WaveAnimation(int[] slots, Material material, int waveLength) {
        this(
            slots,
            new ItemBuilder(material).name("&7~").build(),
            new ItemBuilder(material).name("&b&l~").glow().build(),
            waveLength
        );
    }

    /**
     * Creates a wave animation across a horizontal row
     *
     * @param startSlot The first slot in the row
     * @param count Number of slots in the row
     * @param material The material to use
     */
    public static WaveAnimation horizontal(int startSlot, int count, Material material) {
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            slots[i] = startSlot + i;
        }
        return new WaveAnimation(slots, material, 3);
    }

    /**
     * Creates a wave animation across a vertical column
     *
     * @param startSlot The top slot in the column
     * @param rows Number of rows
     * @param material The material to use
     */
    public static WaveAnimation vertical(int startSlot, int rows, Material material) {
        int[] slots = new int[rows];
        for (int i = 0; i < rows; i++) {
            slots[i] = startSlot + (i * 9);
        }
        return new WaveAnimation(slots, material, 2);
    }

    @Override
    public Map<Integer, AnimationSlot> getNextFrame() {
        Map<Integer, AnimationSlot> frame = new HashMap<>();

        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];

            // Determine if this slot is in the wave peak
            boolean inWave = false;
            for (int w = 0; w < waveLength; w++) {
                int wavePos = (currentOffset - w + slots.length) % slots.length;
                if (i == wavePos) {
                    inWave = true;
                    break;
                }
            }

            ItemStack item = inWave ? pulseItem.clone() : baseItem.clone();
            frame.put(slot, new AnimationSlot(item, null));
        }

        return frame;
    }

    @Override
    public int getDuration() {
        return frameDuration;
    }

    @Override
    public boolean shouldContinue() {
        currentOffset = (currentOffset + 1) % slots.length;
        return loop;
    }

    @Override
    public void init() {
        currentOffset = 0;
    }

    /**
     * Sets whether the animation should loop
     *
     * @param loop true to loop indefinitely
     * @return This animation for chaining
     */
    public WaveAnimation setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }
}
