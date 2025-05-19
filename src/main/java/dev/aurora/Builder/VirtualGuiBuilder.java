package dev.aurora.Builder;

import dev.aurora.GUI.VirtualGui;
import dev.aurora.GUI.VirtualGuiConfig;
import dev.aurora.Manager.GuiManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Fluent builder for creating VirtualGuis (extended size GUIs).
 * <p>
 * VirtualGuiBuilder provides a chainable API for constructing GUIs that exceed
 * Minecraft's 54-slot limit through seamless pagination.
 * </p>
 * <p>
 * Example:
 * <pre>
 * VirtualGui gui = new VirtualGuiBuilder(manager)
 *     .name("mega-storage")
 *     .title("&6&lMega Storage")
 *     .virtualRows(15) // 135 total slots
 *     .seamless(true)
 *     .build();
 * </pre>
 * </p>
 *
 * @since 1.1.0
 */
public class VirtualGuiBuilder {

    private final GuiManager manager;
    private String name;
    private String title;
    private int virtualRows = 9; // Default to 9 rows (81 slots)
    private VirtualGuiConfig config;
    private boolean seamless = false;

    /**
     * Creates a new VirtualGuiBuilder.
     *
     * @param manager The GUI manager
     */
    public VirtualGuiBuilder(GuiManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("GuiManager cannot be null");
        }
        this.manager = manager;
        this.config = VirtualGuiConfig.defaults();
    }

    /**
     * Sets the GUI name/identifier.
     *
     * @param name The name
     * @return This builder for chaining
     */
    public VirtualGuiBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the GUI title.
     *
     * @param title The title (supports color codes)
     * @return This builder for chaining
     */
    public VirtualGuiBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the number of virtual rows.
     * <p>
     * Unlike standard GUIs limited to 6 rows, VirtualGuis can have any number of rows.
     * Each additional 5 rows (45 slots) creates a new physical page.
     * </p>
     *
     * @param rows The number of virtual rows (must be >= 1)
     * @return This builder for chaining
     */
    public VirtualGuiBuilder virtualRows(int rows) {
        if (rows < 1) {
            throw new IllegalArgumentException("Virtual rows must be at least 1");
        }
        this.virtualRows = rows;
        return this;
    }

    /**
     * Sets the total number of virtual slots.
     * <p>
     * This is an alternative to virtualRows() for specifying exact slot counts.
     * The row count will be calculated as: ceil(slots / 9)
     * </p>
     *
     * @param slots The total number of slots
     * @return This builder for chaining
     */
    public VirtualGuiBuilder virtualSlots(int slots) {
        if (slots < 9) {
            throw new IllegalArgumentException("Virtual slots must be at least 9");
        }
        this.virtualRows = (int) Math.ceil((double) slots / 9);
        return this;
    }

    /**
     * Sets the navigation configuration.
     *
     * @param config The VirtualGuiConfig
     * @return This builder for chaining
     */
    public VirtualGuiBuilder navigationConfig(VirtualGuiConfig config) {
        this.config = config != null ? config : VirtualGuiConfig.defaults();
        return this;
    }

    /**
     * Sets whether the GUI should use seamless mode (hide page indicators).
     * <p>
     * Seamless mode provides a cleaner navigation experience by hiding the page number indicator.
     * </p>
     *
     * @param seamless true for seamless mode, false for standard
     * @return This builder for chaining
     */
    public VirtualGuiBuilder seamless(boolean seamless) {
        this.seamless = seamless;
        if (seamless) {
            this.config = VirtualGuiConfig.seamless();
        } else {
            this.config = VirtualGuiConfig.defaults();
        }
        return this;
    }

    /**
     * Disables automatic navigation controls.
     * <p>
     * When disabled, you must manually implement page navigation.
     * This gives you full control over how players navigate between pages.
     * </p>
     *
     * @return This builder for chaining
     */
    public VirtualGuiBuilder disableAutoNavigation() {
        this.config.setAutoNavigation(false);
        return this;
    }

    /**
     * Enables automatic navigation controls (default).
     *
     * @return This builder for chaining
     */
    public VirtualGuiBuilder enableAutoNavigation() {
        this.config.setAutoNavigation(true);
        return this;
    }

    /**
     * Sets custom navigation button items.
     *
     * @param prevButton Previous page button
     * @param nextButton Next page button
     * @param pageIndicator Page indicator item
     * @return This builder for chaining
     */
    public VirtualGuiBuilder navigationButtons(ItemStack prevButton, ItemStack nextButton, ItemStack pageIndicator) {
        if (prevButton != null) {
            this.config.setPrevButton(prevButton);
        }
        if (nextButton != null) {
            this.config.setNextButton(nextButton);
        }
        if (pageIndicator != null) {
            this.config.setPageIndicator(pageIndicator);
        }
        return this;
    }

    /**
     * Sets the navigation row (0-5).
     * <p>
     * Row 0 = top, Row 5 = bottom (default).
     * </p>
     *
     * @param row The row index (0-5)
     * @return This builder for chaining
     */
    public VirtualGuiBuilder navigationRow(int row) {
        this.config.setNavigationRow(row);
        return this;
    }

    /**
     * Sets the slots where navigation controls are placed.
     *
     * @param prevSlot Previous button slot
     * @param indicatorSlot Page indicator slot
     * @param nextSlot Next button slot
     * @return This builder for chaining
     */
    public VirtualGuiBuilder navigationSlots(int prevSlot, int indicatorSlot, int nextSlot) {
        this.config.setNavSlots(prevSlot, indicatorSlot, nextSlot);
        return this;
    }

    /**
     * Builds the VirtualGui instance.
     *
     * @return The constructed VirtualGui
     */
    public VirtualGui build() {
        // Generate default name if not set
        if (name == null || name.isEmpty()) {
            name = "virtual-gui-" + System.currentTimeMillis();
        }

        // Generate default title if not set
        if (title == null || title.isEmpty()) {
            title = "Virtual GUI";
        }

        // Create the VirtualGui
        VirtualGui gui = new VirtualGui(name, title, virtualRows, config, manager);

        // Register with manager
        gui.register(manager);

        return gui;
    }

    /**
     * Builds and returns a configured VirtualGui with items pre-added.
     * <p>
     * This is a convenience method for quickly creating a populated VirtualGui.
     * </p>
     *
     * @param itemSetter Consumer that receives the VirtualGui for item setup
     * @return The constructed and populated VirtualGui
     */
    public VirtualGui buildWith(Consumer<VirtualGui> itemSetter) {
        VirtualGui gui = build();
        if (itemSetter != null) {
            itemSetter.accept(gui);
        }
        return gui;
    }
}
