package dev.aurora.GUI;

import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.BorderType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AuroraGui {
    private final String name;
    private String title;
    private int rows;
    private Inventory inventory;
    private Map<Integer, Consumer<InventoryClickEvent>> clickActions;
    private GuiManager manager;
    private int currentPage;
    private List<ItemStack> paginatedItems;
    private Map<Integer, Consumer<InventoryClickEvent>> paginatedActions;
    private int itemsPerPage;

    public AuroraGui(String name) {
        this.name = name;
        this.title = name;
        this.rows = 3;
        this.clickActions = new HashMap<>();
        this.paginatedItems = new ArrayList<>();
        this.paginatedActions = new HashMap<>();
        this.currentPage = 0;
        this.itemsPerPage = 7; // Default for 3-row GUI with FULL border
    }

    public AuroraGui title(String title) {
        this.title = title;
        return this;
    }

    public AuroraGui rows(int rows) {
        if (rows < 1 || rows > 6) throw new IllegalArgumentException("Rows must be 1-6");
        this.rows = rows;
        this.itemsPerPage = (rows - 2) * 7; // Adjust based on usable slots
        return this;
    }

    public AuroraGui setBorder(BorderType type, ItemStack borderItem) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (borderItem == null) return this;
        switch (type) {
            case TOP:
                for (int i = 0; i < 9; i++) inventory.setItem(i, borderItem);
                break;
            case BOTTOM:
                for (int i = (rows - 1) * 9; i < rows * 9; i++) inventory.setItem(i, borderItem);
                break;
            case LEFT:
                for (int i = 0; i < rows * 9; i += 9) inventory.setItem(i, borderItem);
                break;
            case RIGHT:
                for (int i = 8; i < rows * 9; i += 9) inventory.setItem(i, borderItem);
                break;
            case FULL:
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, borderItem);
                    inventory.setItem((rows - 1) * 9 + i, borderItem);
                }
                for (int i = 0; i < rows * 9; i += 9) {
                    inventory.setItem(i, borderItem);
                    inventory.setItem(i + 8, borderItem);
                }
                break;
        }
        return this;
    }

    public AuroraGui addItem(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        int slot = findNextEmptySlot();
        if (slot >= 0) {
            inventory.setItem(slot, item);
            if (clickAction != null) clickActions.put(slot, clickAction);
        }
        return this;
    }

    public AuroraGui addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (slot >= 0 && slot < rows * 9) {
            inventory.setItem(slot, item);
            if (clickAction != null) clickActions.put(slot, clickAction);
        }
        return this;
    }

    public AuroraGui setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        int targetSlot = slot == -1 ? findNextEmptySlot() : slot;
        if (targetSlot >= 0 && targetSlot < rows * 9) {
            inventory.setItem(targetSlot, item);
            clickActions.remove(targetSlot);
            if (clickAction != null) clickActions.put(targetSlot, clickAction);
        }
        return this;
    }

    public AuroraGui clearGui(boolean preserveBorder) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        ItemStack[] borderItems = preserveBorder ? inventory.getContents().clone() : null;
        inventory.clear();
        clickActions.clear();
        paginatedItems.clear();
        paginatedActions.clear();
        if (preserveBorder && borderItems != null) {
            for (int i = 0; i < borderItems.length; i++) {
                if (borderItems[i] != null) inventory.setItem(i, borderItems[i]);
            }
        }
        return this;
    }

    public AuroraGui updateItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (slot >= 0 && slot < rows * 9) {
            inventory.setItem(slot, item);
            clickActions.remove(slot);
            if (clickAction != null) clickActions.put(slot, clickAction);
        }
        return this;
    }

    public AuroraGui addPaginatedItems(List<ItemStack> items, Consumer<InventoryClickEvent> clickAction) {
        for (ItemStack item : items) {
            paginatedItems.add(item);
            int slot = paginatedItems.size() - 1;
            if (clickAction != null) paginatedActions.put(slot, clickAction);
        }
        updatePage();
        return this;
    }

    public AuroraGui nextPage() {
        if ((currentPage + 1) * itemsPerPage < paginatedItems.size()) {
            currentPage++;
            updatePage();
        }
        return this;
    }

    public AuroraGui prevPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
        return this;
    }

    private void updatePage() {
        int[] contentSlots = new int[itemsPerPage];
        int startSlot = 10;
        for (int i = 0; i < itemsPerPage; i++) {
            contentSlots[i] = startSlot + (i % 7) + (i / 7) * 9;
        }
        for (int slot : contentSlots) {
            inventory.setItem(slot, null);
            clickActions.remove(slot);
        }

        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage && (startIndex + i) < paginatedItems.size(); i++) {
            ItemStack item = paginatedItems.get(startIndex + i);
            Consumer<InventoryClickEvent> action = paginatedActions.get(startIndex + i);
            inventory.setItem(contentSlots[i], item);
            if (action != null) clickActions.put(contentSlots[i], action);
        }

        title = title.replaceAll("Page \\d+", "Page " + (currentPage + 1));
        if (inventory != null) {
            Inventory newInventory = Bukkit.createInventory(null, rows * 9, title);
            newInventory.setContents(inventory.getContents());
            inventory = newInventory;
        }
    }

    public AuroraGui register(GuiManager manager) {
        if (manager == null) throw new IllegalStateException("GuiManager not set");
        manager.registerGui(this);
        return this;
    }

    private int findNextEmptySlot() {
        for (int i = 0; i < rows * 9; i++) {
            if (inventory.getItem(i) == null) return i;
        }
        return -1;
    }

    public void setManager(GuiManager manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public void open(Player player) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        manager.openGui(player, this);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Consumer<InventoryClickEvent> action = clickActions.get(event.getSlot());
        if (action != null) action.accept(event);
    }

    public Inventory getInventory() {
        return inventory;
    }
}