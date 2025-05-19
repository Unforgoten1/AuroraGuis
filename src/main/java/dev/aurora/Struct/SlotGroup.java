package dev.aurora.Struct;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

/**
 * Manages a group of slots that share common properties
 * Allows bulk operations on multiple slots at once
 */
public class SlotGroup {
    private final String name;
    private final Set<Integer> slots;
    private final AuroraGui gui;
    private String permission;
    private long cooldown;
    private boolean locked;

    /**
     * Creates a new slot group
     *
     * @param name The group name
     * @param gui The parent GUI
     * @param slots The slot numbers in this group
     */
    public SlotGroup(String name, AuroraGui gui, int... slots) {
        this.name = name;
        this.gui = gui;
        this.slots = new HashSet<>();
        this.permission = null;
        this.cooldown = 0;
        this.locked = false;

        for (int slot : slots) {
            this.slots.add(slot);
        }
    }

    /**
     * Creates a new slot group with a range
     *
     * @param name The group name
     * @param gui The parent GUI
     * @param startSlot The first slot (inclusive)
     * @param endSlot The last slot (inclusive)
     */
    public static SlotGroup range(String name, AuroraGui gui, int startSlot, int endSlot) {
        int[] slots = new int[endSlot - startSlot + 1];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = startSlot + i;
        }
        return new SlotGroup(name, gui, slots);
    }

    /**
     * Creates a horizontal row group
     *
     * @param name The group name
     * @param gui The parent GUI
     * @param row The row number (0-5)
     * @return SlotGroup for the row
     */
    public static SlotGroup row(String name, AuroraGui gui, int row) {
        int startSlot = row * 9;
        return range(name, gui, startSlot, startSlot + 8);
    }

    /**
     * Creates a vertical column group
     *
     * @param name The group name
     * @param gui The parent GUI
     * @param column The column number (0-8)
     * @param rows The number of rows in the GUI
     * @return SlotGroup for the column
     */
    public static SlotGroup column(String name, AuroraGui gui, int column, int rows) {
        int[] slots = new int[rows];
        for (int i = 0; i < rows; i++) {
            slots[i] = i * 9 + column;
        }
        return new SlotGroup(name, gui, slots);
    }

    /**
     * Creates a border group (outer edge slots)
     *
     * @param name The group name
     * @param gui The parent GUI
     * @param rows The number of rows
     * @return SlotGroup for the border
     */
    public static SlotGroup border(String name, AuroraGui gui, int rows) {
        Set<Integer> borderSlots = new HashSet<>();

        // Top row
        for (int i = 0; i < 9; i++) {
            borderSlots.add(i);
        }

        // Bottom row
        for (int i = (rows - 1) * 9; i < rows * 9; i++) {
            borderSlots.add(i);
        }

        // Left and right columns
        for (int row = 1; row < rows - 1; row++) {
            borderSlots.add(row * 9); // Left
            borderSlots.add(row * 9 + 8); // Right
        }

        int[] slotsArray = borderSlots.stream().mapToInt(Integer::intValue).toArray();
        return new SlotGroup(name, gui, slotsArray);
    }

    /**
     * Gets the group name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all slots in this group
     *
     * @return Unmodifiable set of slots
     */
    public Set<Integer> getSlots() {
        return Collections.unmodifiableSet(slots);
    }

    /**
     * Adds a slot to this group
     *
     * @param slot The slot number
     * @return This group for chaining
     */
    public SlotGroup addSlot(int slot) {
        slots.add(slot);
        return this;
    }

    /**
     * Removes a slot from this group
     *
     * @param slot The slot number
     * @return This group for chaining
     */
    public SlotGroup removeSlot(int slot) {
        slots.remove(slot);
        return this;
    }

    /**
     * Checks if this group contains a slot
     *
     * @param slot The slot number
     * @return true if contained
     */
    public boolean contains(int slot) {
        return slots.contains(slot);
    }

    /**
     * Fills all slots in this group with an item
     *
     * @param item The item to fill with
     * @return This group for chaining
     */
    public SlotGroup fill(ItemStack item) {
        for (int slot : slots) {
            gui.setItem(slot, item);
        }
        return this;
    }

    /**
     * Fills all slots with an item and action
     *
     * @param item The item
     * @param action The click action
     * @return This group for chaining
     */
    public SlotGroup fill(ItemStack item, Consumer<InventoryClickEvent> action) {
        for (int slot : slots) {
            gui.setItem(slot, item, action);
        }
        return this;
    }

    /**
     * Clears all slots in this group
     *
     * @return This group for chaining
     */
    public SlotGroup clear() {
        for (int slot : slots) {
            gui.setItem(slot, null);
        }
        return this;
    }

    /**
     * Sets a permission required to interact with this group
     *
     * @param permission The permission node
     * @return This group for chaining
     */
    public SlotGroup setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Gets the required permission
     *
     * @return The permission, or null if none
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Checks if a player has permission for this group
     *
     * @param player The player
     * @return true if permitted
     */
    public boolean hasPermission(Player player) {
        return permission == null || player.hasPermission(permission);
    }

    /**
     * Sets a cooldown for all slots in this group
     *
     * @param milliseconds Cooldown duration
     * @return This group for chaining
     */
    public SlotGroup setCooldown(long milliseconds) {
        this.cooldown = milliseconds;
        for (int slot : slots) {
            gui.setSlotCooldown(slot, milliseconds);
        }
        return this;
    }

    /**
     * Gets the cooldown duration
     *
     * @return Cooldown in milliseconds
     */
    public long getCooldown() {
        return cooldown;
    }

    /**
     * Locks this group (prevents interaction)
     *
     * @param locked true to lock
     * @return This group for chaining
     */
    public SlotGroup setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    /**
     * Checks if this group is locked
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Applies an action to all slots
     *
     * @param action The action to apply
     * @return This group for chaining
     */
    public SlotGroup forEach(Consumer<Integer> action) {
        for (int slot : slots) {
            action.accept(slot);
        }
        return this;
    }

    /**
     * Gets the number of slots in this group
     *
     * @return Slot count
     */
    public int size() {
        return slots.size();
    }

    /**
     * Checks if a player can interact with this group
     *
     * @param player The player
     * @return true if allowed
     */
    public boolean canInteract(Player player) {
        return !locked && hasPermission(player);
    }

    @Override
    public String toString() {
        return "SlotGroup{" +
                "name='" + name + '\'' +
                ", slots=" + slots.size() +
                ", locked=" + locked +
                ", permission='" + permission + '\'' +
                '}';
    }
}
