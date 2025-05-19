package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.MultiSlotAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Creates a spiral pattern expanding from the center of the GUI
 * Animates slots in a spiral order from center outward
 */
public class SpiralAnimation implements MultiSlotAnimation {
    private final int rows;
    private final ItemStack baseItem;
    private final ItemStack activeItem;
    private final List<Integer> spiralOrder;
    private final int frameDuration;
    private int currentStep;
    private boolean loop;
    private boolean reverse;

    /**
     * Creates a spiral animation for a GUI
     *
     * @param rows Number of rows in the GUI (1-6)
     * @param baseItem The item to display when inactive
     * @param activeItem The item to display when active
     */
    public SpiralAnimation(int rows, ItemStack baseItem, ItemStack activeItem) {
        this.rows = rows;
        this.baseItem = baseItem;
        this.activeItem = activeItem;
        this.spiralOrder = generateSpiralOrder(rows);
        this.frameDuration = 2; // 0.1 seconds per step
        this.currentStep = 0;
        this.loop = true;
        this.reverse = false;
    }

    /**
     * Creates a spiral animation with default materials
     *
     * @param rows Number of rows in the GUI
     * @param baseMaterial Material for inactive slots
     * @param activeMaterial Material for active slots
     */
    public SpiralAnimation(int rows, Material baseMaterial, Material activeMaterial) {
        this(
            rows,
            new ItemBuilder(baseMaterial).name(" ").build(),
            new ItemBuilder(activeMaterial).name("&b&l*").glow().build()
        );
    }

    /**
     * Creates a spiral animation with glass panes
     *
     * @param rows Number of rows in the GUI
     */
    public static SpiralAnimation withGlass(int rows) {
        return new SpiralAnimation(
            rows,
            Material.GRAY_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE
        );
    }

    /**
     * Generates the spiral order of slots from center outward
     */
    private List<Integer> generateSpiralOrder(int rows) {
        int cols = 9;
        int totalSlots = rows * cols;
        List<Integer> order = new ArrayList<>();
        boolean[][] visited = new boolean[rows][cols];

        // Start from center
        int centerRow = rows / 2;
        int centerCol = cols / 2;

        int row = centerRow;
        int col = centerCol;

        // Direction vectors: right, down, left, up
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        int dirIndex = 0;

        int steps = 1;
        int stepsTaken = 0;
        int turnsBeforeExpanding = 0;

        while (order.size() < totalSlots) {
            if (row >= 0 && row < rows && col >= 0 && col < cols && !visited[row][col]) {
                order.add(row * cols + col);
                visited[row][col] = true;
            }

            stepsTaken++;
            if (stepsTaken == steps) {
                stepsTaken = 0;
                dirIndex = (dirIndex + 1) % 4;
                turnsBeforeExpanding++;

                if (turnsBeforeExpanding == 2) {
                    steps++;
                    turnsBeforeExpanding = 0;
                }
            }

            row += directions[dirIndex][0];
            col += directions[dirIndex][1];
        }

        return order;
    }

    @Override
    public Map<Integer, AnimationSlot> getNextFrame() {
        Map<Integer, AnimationSlot> frame = new HashMap<>();

        // Fill all slots with base item first
        for (int slot = 0; slot < rows * 9; slot++) {
            frame.put(slot, new AnimationSlot(baseItem.clone(), null));
        }

        // Activate slots up to current step
        int steps = reverse ?
            spiralOrder.size() - currentStep :
            Math.min(currentStep, spiralOrder.size());

        for (int i = 0; i < steps; i++) {
            int slot = spiralOrder.get(i);
            frame.put(slot, new AnimationSlot(activeItem.clone(), null));
        }

        return frame;
    }

    @Override
    public int getDuration() {
        return frameDuration;
    }

    @Override
    public boolean shouldContinue() {
        currentStep++;

        if (currentStep > spiralOrder.size()) {
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

    /**
     * Sets whether the animation should loop
     *
     * @param loop true to loop indefinitely
     * @return This animation for chaining
     */
    public SpiralAnimation setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Sets whether the spiral should contract instead of expand
     *
     * @param reverse true to reverse the spiral
     * @return This animation for chaining
     */
    public SpiralAnimation setReverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }
}
