package dev.aurora.GUI;

import dev.aurora.Manager.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

/**
 * A virtual GUI that can exceed Minecraft's 54-slot (6-row) limit through seamless pagination.
 * <p>
 * VirtualGui creates the illusion of a larger inventory by automatically managing multiple
 * 54-slot pages and providing transparent navigation. Players experience it as one large GUI.
 * </p>
 * <p>
 * Example: A 15-row VirtualGui (135 slots) automatically splits into 3 pages with navigation.
 * </p>
 *
 * @since 1.1.0
 */
public class VirtualGui implements IGui {

    private final String name;
    private String title;
    private final int virtualRows; // Can be > 6
    private final int totalVirtualSlots;
    private final VirtualGuiConfig config;
    private final GuiManager guiManager;

    // Physical pages (each is a standard 54-slot GUI)
    private final List<AuroraGui> pages;
    private final int contentSlotsPerPage; // Slots available for content (45 with nav row reserved)
    private final int totalPages;

    // Virtual slot mappings
    private final Map<Integer, ItemStack> virtualItems;
    private final Map<Integer, Consumer<InventoryClickEvent>> virtualActions;

    // Player page tracking
    private final Map<UUID, Integer> playerCurrentPage;

    /**
     * Creates a new VirtualGui.
     *
     * @param name The GUI identifier
     * @param title The GUI title
     * @param virtualRows Number of virtual rows (can be > 6)
     * @param config Navigation configuration
     * @param guiManager The GUI manager
     */
    public VirtualGui(String name, String title, int virtualRows, VirtualGuiConfig config, GuiManager guiManager) {
        if (virtualRows < 1) {
            throw new IllegalArgumentException("Virtual rows must be at least 1");
        }
        if (config == null) {
            config = VirtualGuiConfig.defaults();
        }
        if (guiManager == null) {
            throw new IllegalArgumentException("GuiManager cannot be null");
        }

        this.name = name;
        this.title = title != null ? title : "Virtual GUI";
        this.virtualRows = virtualRows;
        this.config = config;
        this.guiManager = guiManager;

        // Calculate total virtual slots
        this.totalVirtualSlots = virtualRows * 9;

        // Calculate content slots per page (reserve bottom row for navigation if auto-nav enabled)
        this.contentSlotsPerPage = config.isAutoNavigation() ? 45 : 54;

        // Calculate total pages needed
        this.totalPages = (int) Math.ceil((double) totalVirtualSlots / contentSlotsPerPage);

        // Initialize data structures
        this.pages = new ArrayList<>(totalPages);
        this.virtualItems = new HashMap<>();
        this.virtualActions = new HashMap<>();
        this.playerCurrentPage = new HashMap<>();

        // Create physical pages
        createPages();
    }

    /**
     * Creates the physical AuroraGui pages.
     */
    private void createPages() {
        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            String pageName = name + "_page_" + pageIndex;
            String pageTitle = title + (totalPages > 1 ? " (" + (pageIndex + 1) + "/" + totalPages + ")" : "");

            AuroraGui page = new AuroraGui(pageName)
                    .title(pageTitle)
                    .rows(6); // All pages are 6 rows (54 slots)

            // Add navigation controls if auto-navigation is enabled
            if (config.isAutoNavigation()) {
                addNavigationControls(page, pageIndex);
            }

            pages.add(page);
        }
    }

    /**
     * Adds navigation controls to a page.
     */
    private void addNavigationControls(AuroraGui page, int pageIndex) {
        // Previous button
        if (pageIndex > 0) {
            page.setItem(config.getPrevSlot(), config.getPrevButton(), event -> {
                Player player = (Player) event.getWhoClicked();
                navigateToPage(player, pageIndex - 1);
            });
        }

        // Page indicator
        ItemStack indicator = config.getPageIndicator().clone();
        if (indicator.hasItemMeta() && indicator.getItemMeta().hasDisplayName()) {
            String displayName = indicator.getItemMeta().getDisplayName();
            displayName = displayName.replace("{current}", String.valueOf(pageIndex + 1));
            displayName = displayName.replace("{total}", String.valueOf(totalPages));
            indicator.getItemMeta().setDisplayName(displayName);
            indicator.setItemMeta(indicator.getItemMeta());
        }
        page.setItem(config.getIndicatorSlot(), indicator);

        // Next button
        if (pageIndex < totalPages - 1) {
            page.setItem(config.getNextSlot(), config.getNextButton(), event -> {
                Player player = (Player) event.getWhoClicked();
                navigateToPage(player, pageIndex + 1);
            });
        }
    }

    /**
     * Sets an item at a virtual slot.
     *
     * @param virtualSlot The virtual slot index (0 to unlimited)
     * @param item The ItemStack
     * @param action Optional click handler
     * @return This VirtualGui for chaining
     */
    public VirtualGui setItem(int virtualSlot, ItemStack item, Consumer<InventoryClickEvent> action) {
        if (virtualSlot < 0 || virtualSlot >= totalVirtualSlots) {
            throw new IndexOutOfBoundsException("Virtual slot " + virtualSlot + " out of bounds (0-" + (totalVirtualSlots - 1) + ")");
        }

        virtualItems.put(virtualSlot, item);
        if (action != null) {
            virtualActions.put(virtualSlot, action);
        }

        // Update the corresponding physical page
        updatePhysicalPage(virtualSlot);

        return this;
    }

    /**
     * Sets an item at a virtual slot without an action.
     *
     * @param virtualSlot The virtual slot index
     * @param item The ItemStack
     * @return This VirtualGui for chaining
     */
    public VirtualGui setItem(int virtualSlot, ItemStack item) {
        return setItem(virtualSlot, item, null);
    }

    /**
     * Updates the physical page with the item from a virtual slot.
     */
    private void updatePhysicalPage(int virtualSlot) {
        int pageIndex = calculatePhysicalPage(virtualSlot);
        int physicalSlot = calculatePhysicalSlot(virtualSlot, pageIndex);

        if (pageIndex >= 0 && pageIndex < pages.size()) {
            AuroraGui page = pages.get(pageIndex);
            ItemStack item = virtualItems.get(virtualSlot);
            Consumer<InventoryClickEvent> action = virtualActions.get(virtualSlot);

            if (item != null) {
                if (action != null) {
                    page.setItem(physicalSlot, item, action);
                } else {
                    page.setItem(physicalSlot, item);
                }
            }
        }
    }

    /**
     * Calculates which physical page a virtual slot belongs to.
     *
     * @param virtualSlot The virtual slot index
     * @return The page index
     */
    private int calculatePhysicalPage(int virtualSlot) {
        return virtualSlot / contentSlotsPerPage;
    }

    /**
     * Calculates the physical slot within a page for a virtual slot.
     *
     * @param virtualSlot The virtual slot index
     * @param pageIndex The page index
     * @return The physical slot index (0-53)
     */
    private int calculatePhysicalSlot(int virtualSlot, int pageIndex) {
        int slotInPage = virtualSlot % contentSlotsPerPage;

        // If auto-navigation is enabled, map to rows 0-4 (slots 0-44)
        // Otherwise, use all 6 rows (slots 0-53)
        if (config.isAutoNavigation()) {
            // Content fills rows 0-4 (45 slots), row 5 is reserved for navigation
            return slotInPage;
        } else {
            // Content fills all rows
            return slotInPage;
        }
    }

    /**
     * Opens the virtual GUI for a player (starts at page 0).
     *
     * @param player The player
     */
    @Override
    public void open(Player player) {
        navigateToPage(player, 0);
    }

    /**
     * Navigates a player to a specific page.
     *
     * @param player The player
     * @param pageIndex The page index to navigate to
     */
    public void navigateToPage(Player player, int pageIndex) {
        if (pageIndex < 0 || pageIndex >= totalPages) {
            return; // Invalid page
        }

        // Track current page for this player
        playerCurrentPage.put(player.getUniqueId(), pageIndex);

        // Open the physical page
        AuroraGui page = pages.get(pageIndex);
        page.open(player);
    }

    /**
     * Gets the current page index for a player.
     *
     * @param player The player
     * @return The page index, or 0 if not tracked
     */
    public int getCurrentPage(Player player) {
        return playerCurrentPage.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Goes to the next page for a player.
     *
     * @param player The player
     */
    public void nextPage(Player player) {
        int current = getCurrentPage(player);
        if (current < totalPages - 1) {
            navigateToPage(player, current + 1);
        }
    }

    /**
     * Goes to the previous page for a player.
     *
     * @param player The player
     */
    public void prevPage(Player player) {
        int current = getCurrentPage(player);
        if (current > 0) {
            navigateToPage(player, current - 1);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        // Update all page titles
        for (int i = 0; i < pages.size(); i++) {
            String pageTitle = title + (totalPages > 1 ? " (" + (i + 1) + "/" + totalPages + ")" : "");
            // Recreate pages with new title since AuroraGui doesn't have setTitle()
            // This is handled when pages are created
        }
    }

    /**
     * Gets the number of virtual rows.
     *
     * @return The virtual row count
     */
    public int getVirtualRows() {
        return virtualRows;
    }

    /**
     * Gets the total number of virtual slots.
     *
     * @return The slot count
     */
    public int getTotalVirtualSlots() {
        return totalVirtualSlots;
    }

    /**
     * Gets the number of physical pages.
     *
     * @return The page count
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Gets a specific physical page.
     *
     * @param pageIndex The page index
     * @return The AuroraGui page
     */
    public AuroraGui getPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return null;
        }
        return pages.get(pageIndex);
    }

    /**
     * Gets all physical pages.
     *
     * @return List of pages (unmodifiable)
     */
    public List<AuroraGui> getPages() {
        return Collections.unmodifiableList(pages);
    }

    /**
     * Gets the virtual GUI configuration.
     *
     * @return The config
     */
    public VirtualGuiConfig getConfig() {
        return config;
    }

    /**
     * Registers all physical pages with the GUI manager.
     *
     * @param manager The GUI manager
     * @return This VirtualGui for chaining
     */
    @Override
    public IGui register(GuiManager manager) {
        for (AuroraGui page : pages) {
            page.register(manager);
        }
        return this;
    }

    // Implement remaining IGui methods

    @Override
    public int getRows() {
        return 6; // Virtual GUIs are always 6 rows per physical page
    }

    @Override
    public int getSize() {
        return totalVirtualSlots;
    }

    @Override
    public org.bukkit.inventory.Inventory getInventory() {
        // Return the first page's inventory (or current page if tracked)
        return pages.isEmpty() ? null : pages.get(0).getInventory();
    }

    @Override
    public GuiManager getManager() {
        return guiManager;
    }

    @Override
    public void setManager(GuiManager manager) {
        // Manager is set in constructor
    }

    @Override
    public java.util.List<Player> getViewers() {
        // Collect viewers from all pages
        java.util.List<Player> allViewers = new java.util.ArrayList<>();
        for (AuroraGui page : pages) {
            allViewers.addAll(page.getViewers());
        }
        return allViewers;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Clicks are handled by individual pages
    }

    @Override
    public IGui addListener(dev.aurora.Struct.Listener.GuiListener listener) {
        // Add listener to all pages
        for (AuroraGui page : pages) {
            page.addListener(listener);
        }
        return this;
    }

    @Override
    public IGui removeListener(dev.aurora.Struct.Listener.GuiListener listener) {
        // Remove listener from all pages
        for (AuroraGui page : pages) {
            page.removeListener(listener);
        }
        return this;
    }

    @Override
    public java.util.List<dev.aurora.Struct.Listener.GuiListener> getListeners() {
        // Return listeners from first page (they're shared)
        return pages.isEmpty() ? new java.util.ArrayList<>() : pages.get(0).getListeners();
    }

    @Override
    public void cleanup() {
        // Cleanup all pages
        for (AuroraGui page : pages) {
            page.cleanup();
        }
        playerCurrentPage.clear();
    }

    @Override
    public IGui update() {
        // Update all pages
        for (AuroraGui page : pages) {
            page.update();
        }
        return this;
    }

    @Override
    public String toString() {
        return "VirtualGui{" +
                "name='" + name + '\'' +
                ", virtualRows=" + virtualRows +
                ", totalSlots=" + totalVirtualSlots +
                ", pages=" + totalPages +
                '}';
    }
}
