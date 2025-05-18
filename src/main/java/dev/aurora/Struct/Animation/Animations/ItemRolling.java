package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.API.Enum.Direction;
import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.ArrayList;

public class ItemRolling implements IAnimation {

    private final Direction direction;
    private final int[] slots;
    private final List<ItemStack> items;
    private final Random random;
    private int currentSlotIndex;
    private int cycleFrame;
    private final int framesPerSlot;
    private final int emptyFrames;
    private ItemBuilder currentItem;
    private final Consumer<InventoryClickEvent> clickAction;

    public ItemRolling(Direction direction, int[] rowsOrColumns, List<ItemStack> items, int ticksPerSlot, Consumer<InventoryClickEvent> clickAction) {
        this.direction = direction;
        this.items = items;
        this.random = new Random();
        this.framesPerSlot = ticksPerSlot;
        this.clickAction = clickAction;

        List<Integer> slotList = new ArrayList<>();
        if (direction == Direction.VERTICAL) {
            for (int col : rowsOrColumns) {
                if (col < 1 || col > 7) continue;
                for (int row = 1; row < 5; row++) {
                    int slot = row * 9 + col;
                    slotList.add(slot);
                }
            }
        } else {
            for (int row : rowsOrColumns) {
                if (row < 1 || row > 4) continue;
                for (int col = 1; col <= 7; col++) {
                    int slot = row * 9 + col;
                    slotList.add(slot);
                }
            }
        }
        this.slots = slotList.stream().mapToInt(i -> i).toArray();

        this.emptyFrames = slots.length * ticksPerSlot;
        this.currentSlotIndex = 0;
        this.cycleFrame = 0;
        this.currentItem = selectRandomItem();
    }

    private ItemBuilder selectRandomItem() {
        if (items.isEmpty()) return null;
        return new ItemBuilder(items.get(random.nextInt(items.size())));
    }

    @Override
    public ItemStack getNextItem() {
        if (slots.length == 0 || items.isEmpty()) return null;

        if (cycleFrame < slots.length * framesPerSlot) {
            int slotIndex = cycleFrame / framesPerSlot;
            if (slotIndex == currentSlotIndex) {
                return currentItem;
            }
        }
        return null;
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        if (cycleFrame < slots.length * framesPerSlot && cycleFrame / framesPerSlot == currentSlotIndex) {
            return clickAction;
        }
        return null;
    }

    @Override
    public int getDuration() {
        return 1;
    }

    @Override
    public boolean shouldContinue() {
        cycleFrame++;
        if (cycleFrame >= (slots.length * framesPerSlot + emptyFrames)) {
            cycleFrame = 0;
            currentSlotIndex = 0;
            currentItem = selectRandomItem();
            return true;
        }
        int newSlotIndex = cycleFrame / framesPerSlot;
        if (newSlotIndex < slots.length) {
            currentSlotIndex = newSlotIndex;
        }
        return true;
    }

    @Override
    public void init() {
        cycleFrame = 0;
        currentSlotIndex = 0;
        currentItem = selectRandomItem();
    }

    public int[] getSlots() {
        return slots;
    }
}
