package dev.aurora.GUI;

import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Configuration for Virtual GUIs that exceed the normal 6-row (54-slot) limit.
 * <p>
 * VirtualGuiConfig controls navigation button placement, appearance, and behavior
 * for seamless multi-page navigation in extended GUIs.
 * </p>
 *
 * @since 1.1.0
 */
public class VirtualGuiConfig {

    private ItemStack nextButton;
    private ItemStack prevButton;
    private ItemStack pageIndicator;
    private boolean autoNavigation;
    private int navigationRow;
    private int[] navSlots; // [prev_slot, indicator_slot, next_slot]

    /**
     * Creates a new VirtualGuiConfig with default settings.
     */
    public VirtualGuiConfig() {
        this.autoNavigation = true;
        this.navigationRow = 5; // Bottom row (0-indexed)
        this.navSlots = new int[]{45, 49, 53}; // Previous, Indicator, Next

        // Default navigation buttons
        this.prevButton = new ItemBuilder(Material.ARROW)
                .name("&e← Previous Page")
                .build();

        this.nextButton = new ItemBuilder(Material.ARROW)
                .name("&eNext Page →")
                .build();

        this.pageIndicator = new ItemBuilder(Material.PAPER)
                .name("&6Page {current} / {total}")
                .build();
    }

    /**
     * Gets the "next page" button item.
     *
     * @return The next button ItemStack
     */
    public ItemStack getNextButton() {
        return nextButton;
    }

    /**
     * Sets the "next page" button item.
     *
     * @param nextButton The button ItemStack
     * @return This config for chaining
     */
    public VirtualGuiConfig setNextButton(ItemStack nextButton) {
        this.nextButton = nextButton;
        return this;
    }

    /**
     * Gets the "previous page" button item.
     *
     * @return The previous button ItemStack
     */
    public ItemStack getPrevButton() {
        return prevButton;
    }

    /**
     * Sets the "previous page" button item.
     *
     * @param prevButton The button ItemStack
     * @return This config for chaining
     */
    public VirtualGuiConfig setPrevButton(ItemStack prevButton) {
        this.prevButton = prevButton;
        return this;
    }

    /**
     * Gets the page indicator item.
     * <p>
     * The item's display name can use placeholders:
     * - {current} = current page number
     * - {total} = total pages
     * </p>
     *
     * @return The page indicator ItemStack
     */
    public ItemStack getPageIndicator() {
        return pageIndicator;
    }

    /**
     * Sets the page indicator item.
     *
     * @param pageIndicator The indicator ItemStack
     * @return This config for chaining
     */
    public VirtualGuiConfig setPageIndicator(ItemStack pageIndicator) {
        this.pageIndicator = pageIndicator;
        return this;
    }

    /**
     * Checks if automatic navigation controls are enabled.
     *
     * @return true if auto-navigation is enabled, false otherwise
     */
    public boolean isAutoNavigation() {
        return autoNavigation;
    }

    /**
     * Sets whether navigation controls should be automatically added.
     *
     * @param autoNavigation true to enable, false to disable
     * @return This config for chaining
     */
    public VirtualGuiConfig setAutoNavigation(boolean autoNavigation) {
        this.autoNavigation = autoNavigation;
        return this;
    }

    /**
     * Gets the row index where navigation controls are placed.
     * <p>
     * Row 0 = top row, Row 5 = bottom row (in a 6-row GUI).
     * </p>
     *
     * @return The navigation row (0-5)
     */
    public int getNavigationRow() {
        return navigationRow;
    }

    /**
     * Sets the row where navigation controls are placed.
     *
     * @param navigationRow The row index (0-5)
     * @return This config for chaining
     */
    public VirtualGuiConfig setNavigationRow(int navigationRow) {
        this.navigationRow = Math.max(0, Math.min(5, navigationRow));
        return this;
    }

    /**
     * Gets the slot positions for navigation controls.
     * <p>
     * Returns an array of 3 integers:
     * - Index 0: Previous button slot
     * - Index 1: Page indicator slot
     * - Index 2: Next button slot
     * </p>
     *
     * @return Array of 3 slot indices
     */
    public int[] getNavSlots() {
        return navSlots.clone();
    }

    /**
     * Sets the slot positions for navigation controls.
     *
     * @param prevSlot Previous button slot
     * @param indicatorSlot Page indicator slot
     * @param nextSlot Next button slot
     * @return This config for chaining
     */
    public VirtualGuiConfig setNavSlots(int prevSlot, int indicatorSlot, int nextSlot) {
        this.navSlots = new int[]{prevSlot, indicatorSlot, nextSlot};
        return this;
    }

    /**
     * Gets the previous button slot.
     *
     * @return The slot index
     */
    public int getPrevSlot() {
        return navSlots[0];
    }

    /**
     * Gets the page indicator slot.
     *
     * @return The slot index
     */
    public int getIndicatorSlot() {
        return navSlots[1];
    }

    /**
     * Gets the next button slot.
     *
     * @return The slot index
     */
    public int getNextSlot() {
        return navSlots[2];
    }

    /**
     * Creates a default VirtualGuiConfig.
     *
     * @return A new config with default settings
     */
    public static VirtualGuiConfig defaults() {
        return new VirtualGuiConfig();
    }

    /**
     * Creates a seamless VirtualGuiConfig (no page indicator).
     *
     * @return A new config without page numbers shown
     */
    public static VirtualGuiConfig seamless() {
        VirtualGuiConfig config = new VirtualGuiConfig();
        config.setPageIndicator(new ItemBuilder(Material.BARRIER)
                .name("&7")
                .build()); // Invisible/empty indicator
        return config;
    }

    @Override
    public String toString() {
        return "VirtualGuiConfig{" +
                "autoNavigation=" + autoNavigation +
                ", navigationRow=" + navigationRow +
                ", navSlots=[" + navSlots[0] + "," + navSlots[1] + "," + navSlots[2] + "]" +
                '}';
    }
}
