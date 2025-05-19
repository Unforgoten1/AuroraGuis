package dev.aurora.Template;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Struct.BorderType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reusable GUI template that can be applied to multiple GUI instances
 * Allows creating consistent GUI layouts across different instances
 */
public class GuiTemplate {
    private final String name;
    private int rows;
    private BorderType borderType;
    private ItemStack borderItem;
    private final Map<Integer, TemplateSlot> slots;

    private static class TemplateSlot {
        ItemStack item;
        Consumer<InventoryClickEvent> clickAction;

        TemplateSlot(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
            this.item = item;
            this.clickAction = clickAction;
        }
    }

    public GuiTemplate(String name) {
        this.name = name;
        this.rows = 3;
        this.slots = new HashMap<>();
    }

    /**
     * Set number of rows for this template
     * @param rows Number of rows (1-6)
     * @return This template for chaining
     */
    public GuiTemplate rows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be 1-6");
        }
        this.rows = rows;
        return this;
    }

    /**
     * Set border style for this template
     * @param type Border type
     * @param item Border item
     * @return This template for chaining
     */
    public GuiTemplate border(BorderType type, ItemStack item) {
        this.borderType = type;
        this.borderItem = item;
        return this;
    }

    /**
     * Add a slot with item and click action
     * @param slot Slot index
     * @param item Item to place
     * @param clickAction Action on click
     * @return This template for chaining
     */
    public GuiTemplate slot(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        slots.put(slot, new TemplateSlot(item, clickAction));
        return this;
    }

    /**
     * Add a slot with only an item
     * @param slot Slot index
     * @param item Item to place
     * @return This template for chaining
     */
    public GuiTemplate slot(int slot, ItemStack item) {
        return slot(slot, item, null);
    }

    /**
     * Create a new GUI instance from this template
     * @param guiName Name for the new GUI
     * @return New GUI instance with template applied
     */
    public AuroraGui create(String guiName) {
        AuroraGui gui = new AuroraGui(guiName);
        applyTo(gui);
        return gui;
    }

    /**
     * Apply this template to an existing GUI
     * @param gui The GUI to apply template to
     */
    public void applyTo(AuroraGui gui) {
        gui.rows(rows);

        if (borderType != null && borderItem != null) {
            gui.setBorder(borderType, borderItem);
        }

        for (Map.Entry<Integer, TemplateSlot> entry : slots.entrySet()) {
            TemplateSlot slot = entry.getValue();
            gui.addItem(entry.getKey(), slot.item, slot.clickAction);
        }
    }

    /**
     * Get template name
     * @return Template name
     */
    public String getName() {
        return name;
    }

    /**
     * Get number of rows
     * @return Row count
     */
    public int getRows() {
        return rows;
    }
}
