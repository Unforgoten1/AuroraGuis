package dev.aurora.GUI;

import dev.aurora.Debug.GuiDebugger;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Struct.Animation.Animation;
import dev.aurora.Struct.Animation.MultiSlotAnimation;
import dev.aurora.Struct.BorderType;
import dev.aurora.Struct.Condition.ClickCondition;
import dev.aurora.Struct.Listener.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AuroraGui implements IGui {
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
    private Consumer<Integer> pageChangeCallback;
    private final Map<Integer, IAnimation> animations;
    private final Map<Integer, BukkitTask> animationTasks;
    private final List<PendingAnimation> pendingAnimations;
    private final Map<Integer, AnimationState> animationStates;
    private final Map<Integer, Long> animationLastTick;
    private final Map<Integer, Long> animationNextTick;
    private final List<GuiListener> listeners;
    private final Map<Integer, ClickCondition> clickConditions;
    private ItemStack[] borderItems;

    // Batch update support
    private boolean batchMode = false;
    private final Map<Integer, ItemStack> pendingUpdates;
    private final Map<Integer, Consumer<InventoryClickEvent>> pendingActions;
    private BukkitTask autoFlushTask;

    // Cooldown support
    private dev.aurora.Struct.Cooldown.ClickCooldown cooldownManager;
    private final Map<Integer, Long> slotCooldowns;

    // Slot group support
    private final Map<String, dev.aurora.Struct.SlotGroup> slotGroups;

    // Animated title support
    private dev.aurora.Struct.Animation.TitleAnimation titleAnimation;
    private BukkitTask titleAnimationTask;

    // GUI locking support
    private dev.aurora.Struct.GuiLock guiLock;

    // Auto-sorting support
    private dev.aurora.Struct.Sort.GuiSorter autoSorter;
    private boolean autoSortEnabled;

    private static class PendingAnimation {
        final int slot;
        final IAnimation animation;

        PendingAnimation(int slot, IAnimation animation) {
            this.slot = slot;
            this.animation = animation;
        }
    }

    private static class AnimationState {
        boolean paused;
        boolean stopped;

        AnimationState() {
            this.paused = false;
            this.stopped = false;
        }
    }

    private static class GuiValidator {
        /**
         * Validate GUI title length
         */
        static void validateTitle(String title) {
            if (title == null) {
                throw new IllegalArgumentException("Title cannot be null");
            }
            // Minecraft title limit is 32 characters
            if (title.length() > 32) {
                GuiDebugger.warn("Title exceeds 32 characters, will be truncated: " + title);
            }
        }

        /**
         * Validate slot is within bounds
         */
        static void validateSlot(int slot, int maxSlots) {
            if (slot < 0 || slot >= maxSlots) {
                throw new IllegalArgumentException("Invalid slot: " + slot + " (max: " + (maxSlots - 1) + ")");
            }
        }

        /**
         * Check for slot conflicts and warn
         */
        static void validateNoSlotConflict(int slot, Map<Integer, ?> animations, ItemStack[] borderItems) {
            if (animations.containsKey(slot)) {
                GuiDebugger.warn("Slot " + slot + " has an active animation, item may be overwritten");
            }
            if (borderItems != null && slot < borderItems.length && borderItems[slot] != null) {
                GuiDebugger.warn("Slot " + slot + " is part of border, will be overwritten");
            }
        }
    }

    public AuroraGui(String name) {
        this.name = name;
        this.title = name;
        this.rows = 3;
        this.clickActions = new ConcurrentHashMap<>();
        this.paginatedItems = new ArrayList<>();
        this.paginatedActions = new ConcurrentHashMap<>();
        this.currentPage = 0;
        this.itemsPerPage = 7;
        this.animations = new ConcurrentHashMap<>();
        this.animationTasks = new ConcurrentHashMap<>();
        this.pendingAnimations = new ArrayList<>();
        this.animationStates = new ConcurrentHashMap<>();
        this.animationLastTick = new ConcurrentHashMap<>();
        this.animationNextTick = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.clickConditions = new ConcurrentHashMap<>();
        this.borderItems = null;
        this.pendingUpdates = new ConcurrentHashMap<>();
        this.pendingActions = new ConcurrentHashMap<>();
        this.cooldownManager = new dev.aurora.Struct.Cooldown.ClickCooldown();
        this.slotCooldowns = new ConcurrentHashMap<>();
        this.slotGroups = new ConcurrentHashMap<>();
        this.guiLock = new dev.aurora.Struct.GuiLock();
        this.autoSorter = null;
        this.autoSortEnabled = false;
    }

    public AuroraGui title(String title) {
        GuiValidator.validateTitle(title);
        if (title.length() > 32) {
            this.title = title.substring(0, 32);
        } else {
            this.title = title;
        }
        return this;
    }

    public AuroraGui rows(int rows) {
        if (rows < 1 || rows > 6) throw new IllegalArgumentException("Rows must be 1-6");
        this.rows = rows;
        this.itemsPerPage = (rows - 2) * 7;
        return this;
    }

    public AuroraGui setBorder(BorderType type, ItemStack borderItem) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (borderItem == null) return this;
        borderItems = new ItemStack[rows * 9];
        switch (type) {
            case TOP:
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case BOTTOM:
                for (int i = (rows - 1) * 9; i < rows * 9; i++) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case LEFT:
                for (int i = 0; i < rows * 9; i += 9) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case RIGHT:
                for (int i = 8; i < rows * 9; i += 9) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case FULL:
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, borderItem);
                    inventory.setItem((rows - 1) * 9 + i, borderItem);
                    borderItems[i] = borderItem;
                    borderItems[(rows - 1) * 9 + i] = borderItem;
                }
                for (int i = 0; i < rows * 9; i += 9) {
                    inventory.setItem(i, borderItem);
                    inventory.setItem(i + 8, borderItem);
                    borderItems[i] = borderItem;
                    borderItems[i + 8] = borderItem;
                }
                break;
        }
        return this;
    }

    public AuroraGui addItem(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        int slot = findNextEmptySlot();
        if (slot == -1) {
            throw new IllegalStateException("GUI is full, cannot add more items");
        }
        if (slot >= 0) {
            inventory.setItem(slot, item);
            if (clickAction != null) clickActions.put(slot, clickAction);
        }
        return this;
    }

    public AuroraGui addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        GuiValidator.validateSlot(slot, rows * 9);
        GuiValidator.validateNoSlotConflict(slot, animations, borderItems);

        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (slot >= 0 && slot < rows * 9) {
            inventory.setItem(slot, item);
            if (clickAction != null) clickActions.put(slot, clickAction);
        }
        return this;
    }

    public AuroraGui setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        int targetSlot = slot == -1 ? findNextEmptySlot() : slot;
        if (targetSlot >= 0 && targetSlot < rows * 9) {
            inventory.setItem(targetSlot, item);
            clickActions.remove(targetSlot);
            if (clickAction != null) clickActions.put(targetSlot, clickAction);
        }
        return this;
    }

    /**
     * Sets an item at a slot without a click action
     * @param slot The slot index
     * @param item The item to set
     * @return This GUI for chaining
     */
    public AuroraGui setItem(int slot, ItemStack item) {
        return setItem(slot, item, null);
    }

    public AuroraGui clearGui(boolean preserveBorder) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        ItemStack[] borderItemsCopy = preserveBorder ? borderItems : null;
        inventory.clear();
        clickActions.clear();
        paginatedItems.clear();
        paginatedActions.clear();
        animations.clear();
        animationTasks.values().forEach(BukkitTask::cancel);
        animationTasks.clear();
        pendingAnimations.clear();
        if (preserveBorder && borderItemsCopy != null) {
            for (int i = 0; i < borderItemsCopy.length; i++) {
                if (borderItemsCopy[i] != null) {
                    inventory.setItem(i, borderItemsCopy[i]);
                    borderItems[i] = borderItemsCopy[i];
                }
            }
        } else {
            borderItems = null;
        }
        return this;
    }

    /**
     * Set the number of items displayed per page
     * @param itemsPerPage Number of items per page
     * @return This GUI for chaining
     */
    public AuroraGui setItemsPerPage(int itemsPerPage) {
        if (itemsPerPage < 1) {
            throw new IllegalArgumentException("Items per page must be at least 1");
        }
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    /**
     * Set callback to be called when page changes
     * @param callback The callback accepting new page number
     * @return This GUI for chaining
     */
    public AuroraGui onPageChange(Consumer<Integer> callback) {
        this.pageChangeCallback = callback;
        return this;
    }

    /**
     * Get current page number (0-indexed)
     * @return The current page
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Get total number of pages
     * @return Total pages
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) paginatedItems.size() / itemsPerPage);
    }

    /**
     * Check if there is a next page
     * @return true if next page exists
     */
    public boolean hasNextPage() {
        return (currentPage + 1) * itemsPerPage < paginatedItems.size();
    }

    /**
     * Check if there is a previous page
     * @return true if previous page exists
     */
    public boolean hasPrevPage() {
        return currentPage > 0;
    }

    /**
     * Set to specific page with validation
     * @param page The page number to navigate to
     * @return This GUI for chaining
     */
    public AuroraGui setPage(int page) {
        if (page < 0 || page >= getTotalPages()) {
            throw new IllegalArgumentException("Invalid page number: " + page);
        }
        int oldPage = currentPage;
        currentPage = page;
        updatePage();

        // Call callback
        if (pageChangeCallback != null) {
            pageChangeCallback.accept(currentPage);
        }

        // Call listeners
        for (Player viewer : getViewers()) {
            for (GuiListener listener : listeners) {
                listener.onPageChange(viewer, this, oldPage, currentPage);
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
            animations.remove(slot);
            if (animationTasks.containsKey(slot)) {
                animationTasks.get(slot).cancel();
                animationTasks.remove(slot);
            }
        }
        return this;
    }

    public AuroraGui addPaginatedItems(List<ItemStack> items, Consumer<InventoryClickEvent> clickAction) {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
        for (ItemStack item : items) {
            if (item == null) continue; // Skip null items
            paginatedItems.add(item);
            int slot = paginatedItems.size() - 1;
            if (clickAction != null) paginatedActions.put(slot, clickAction);
        }
        if (autoSortEnabled && autoSorter != null) {
            sortPaginatedItems();
        }
        updatePage();
        return this;
    }

    public AuroraGui nextPage() {
        if (hasNextPage()) {
            setPage(currentPage + 1);
        }
        return this;
    }

    public AuroraGui prevPage() {
        if (hasPrevPage()) {
            setPage(currentPage - 1);
        }
        return this;
    }

    public AuroraGui addAnimation(int slot, Animation animation) {
        return addCustomAnimation(slot, animation);
    }

    public AuroraGui addCustomAnimation(int slot, IAnimation animation) {
        if (slot < 0 || slot >= rows * 9 || animation == null) return this;

        GuiValidator.validateSlot(slot, rows * 9);

        // Check if slot has existing item
        if (inventory != null && inventory.getItem(slot) != null) {
            GuiDebugger.warn("Slot " + slot + " already has an item, animation will replace it");
        }

        pendingAnimations.add(new PendingAnimation(slot, animation));
        return this;
    }

    private void scheduleAnimation(int slot, IAnimation animation) {
        if (manager == null) {
            throw new IllegalStateException("Cannot schedule animation: GuiManager not set");
        }

        animations.put(slot, animation);
        animationStates.put(slot, new AnimationState());
        animation.init();

        // Initialize tick tracking
        animationLastTick.put(slot, 0L);
        animationNextTick.put(slot, 0L);

        // Register with centralized animation scheduler
        manager.getAnimationScheduler().registerAnimation(this, slot);

        // Call animation start listeners
        for (GuiListener listener : listeners) {
            listener.onAnimationStart(this, slot);
        }
    }

    /**
     * Alias for addCustomAnimation
     * @param slot The slot
     * @param animation The animation
     * @return This GUI for chaining
     */
    public AuroraGui playAnimation(int slot, IAnimation animation) {
        return addCustomAnimation(slot, animation);
    }

    /**
     * Alias for setBorder
     * @param type The border type
     * @return This GUI for chaining
     */
    public AuroraGui addBorder(BorderType type) {
        return setBorder(type, null);
    }

    /**
     * Adds a custom border with specific item
     * @param item The border item
     * @return This GUI for chaining
     */
    public AuroraGui addCustomBorder(ItemStack item) {
        return setBorder(BorderType.FULL, item);
    }

    /**
     * Alias for addCustomBorder
     * @param item The border item
     * @return This GUI for chaining
     */
    public AuroraGui border(ItemStack item) {
        return addCustomBorder(item);
    }

    /**
     * Alias for addCustomBorder
     * @param item The border item
     * @return This GUI for chaining
     */
    public AuroraGui fillBorder(ItemStack item) {
        return addCustomBorder(item);
    }

    /**
     * Updates the pagination display
     * Public wrapper for updatePage()
     */
    public void updatePaginationDisplay() {
        updatePage();
    }

    private void updatePage() {
        int[] contentSlots = new int[itemsPerPage];
        int startSlot = 10;
        for (int i = 0; i < itemsPerPage; i++) {
            contentSlots[i] = startSlot + (i % 7) + (i / 7) * 9;
        }
        for (int slot : contentSlots) {
            if (!animations.containsKey(slot)) {
                inventory.setItem(slot, null);
                clickActions.remove(slot);
            }
        }

        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage && (startIndex + i) < paginatedItems.size(); i++) {
            int slot = contentSlots[i];
            if (!animations.containsKey(slot)) {
                ItemStack item = paginatedItems.get(startIndex + i);
                Consumer<InventoryClickEvent> action = paginatedActions.get(startIndex + i);
                inventory.setItem(slot, item);
                if (action != null) clickActions.put(slot, action);
            }
        }

        title = title.replaceAll("Page \\d+", "Page " + (currentPage + 1));
        if (inventory != null) {
            Inventory newInventory = Bukkit.createInventory(null, rows * 9, title);
            newInventory.setContents(inventory.getContents());
            inventory = newInventory;
            for (Player viewer : getViewers()) {
                viewer.openInventory(newInventory);
            }
        }
    }

    public AuroraGui update() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
            return this;
        }

        for (int i = 0; i < rows * 9; i++) {
            if (!animations.containsKey(i)) {
                inventory.setItem(i, null);
                clickActions.remove(i);
            }
        }

        if (borderItems != null) {
            for (int i = 0; i < borderItems.length; i++) {
                if (borderItems[i] != null && !animations.containsKey(i)) {
                    inventory.setItem(i, borderItems[i]);
                }
            }
        }

        updatePage();

        for (Player viewer : getViewers()) {
            viewer.updateInventory();
        }

        return this;
    }

    public List<Player> getViewers() {
        if (inventory == null) {
            return new ArrayList<>();
        }
        List<Player> viewers = new ArrayList<>();
        for (org.bukkit.entity.HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player) {
                viewers.add((Player) viewer);
            }
        }
        return viewers;
    }

    public AuroraGui register(GuiManager manager) {
        GuiDebugger.log("Registering GUI '" + name + "' with manager");

        if (manager == null) throw new IllegalArgumentException("GuiManager not set");
        manager.registerGui(this);
        for (PendingAnimation pending : new ArrayList<>(pendingAnimations)) {
            scheduleAnimation(pending.slot, pending.animation);
        }
        pendingAnimations.clear();

        GuiDebugger.log("GUI '" + name + "' registered successfully");
        return this;
    }

    private int findNextEmptySlot() {
        for (int i = 0; i < rows * 9; i++) {
            if (inventory.getItem(i) == null && !animations.containsKey(i)) return i;
        }
        return -1;
    }

    public void setManager(GuiManager manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return rows * 9;
    }

    public GuiManager getManager() {
        return manager;
    }

    /**
     * Add a listener to this GUI
     * @param listener The listener to add
     * @return This GUI for chaining
     */
    public AuroraGui addListener(GuiListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    /**
     * Remove a listener from this GUI
     * @param listener The listener to remove
     * @return This GUI for chaining
     */
    public AuroraGui removeListener(GuiListener listener) {
        listeners.remove(listener);
        return this;
    }

    /**
     * Get all registered listeners
     * @return List of listeners
     */
    public List<GuiListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    /**
     * Add item to specific slot with click condition
     * @param slot The slot index
     * @param item The item to add
     * @param clickAction Action to perform on click
     * @param condition Condition that must be met for click
     * @return This GUI for chaining
     */
    public AuroraGui addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction, ClickCondition condition) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        GuiValidator.validateSlot(slot, rows * 9);
        GuiValidator.validateNoSlotConflict(slot, animations, borderItems);

        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (slot >= 0 && slot < rows * 9) {
            inventory.setItem(slot, item);
            if (clickAction != null) clickActions.put(slot, clickAction);
            if (condition != null) clickConditions.put(slot, condition);
        }
        return this;
    }

    public void open(Player player) {
        GuiDebugger.log("Opening GUI '" + name + "' for player " + (player != null ? player.getName() : "null"));

        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (manager == null) {
            throw new IllegalStateException("GUI must be registered before opening");
        }
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);

        manager.openGui(player, this);

        if (GuiDebugger.isEnabled()) {
            GuiDebugger.logGuiState(this);
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        // Call before click listeners
        for (GuiListener listener : listeners) {
            if (!listener.onBeforeClick(event, this)) {
                return; // Click cancelled by listener
            }
        }

        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        // Check cooldown
        if (!canClick(player, slot)) {
            long remaining = getRemainingCooldown(player, slot);
            if (remaining > 0) {
                player.sendMessage("§cPlease wait " + (remaining / 1000.0) + "s before clicking again.");
            }
            return; // Cooldown active
        }

        // Check click condition
        ClickCondition condition = clickConditions.get(slot);
        if (condition != null && !condition.test(event)) {
            return; // Condition not met
        }

        // Record click for cooldown tracking
        recordClick(player, slot);

        Consumer<InventoryClickEvent> action = clickActions.get(slot);
        if (action != null) {
            action.accept(event);
        }

        // Call after click listeners
        for (GuiListener listener : listeners) {
            listener.onAfterClick(event, this);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Pause animation at specific slot
     * @param slot The slot to pause
     * @return This GUI for chaining
     */
    public AuroraGui pauseAnimation(int slot) {
        AnimationState state = animationStates.get(slot);
        if (state != null) {
            state.paused = true;
        }
        return this;
    }

    /**
     * Resume paused animation at specific slot
     * @param slot The slot to resume
     * @return This GUI for chaining
     */
    public AuroraGui resumeAnimation(int slot) {
        AnimationState state = animationStates.get(slot);
        if (state != null) {
            state.paused = false;
        }
        return this;
    }

    /**
     * Stop animation at specific slot permanently
     * @param slot The slot to stop
     * @return This GUI for chaining
     */
    public AuroraGui stopAnimation(int slot) {
        AnimationState state = animationStates.get(slot);
        if (state != null) {
            state.stopped = true;
        }

        // Unregister from animation scheduler
        if (manager != null) {
            manager.getAnimationScheduler().unregisterAnimation(this, slot);
        }

        BukkitTask task = animationTasks.remove(slot);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        animations.remove(slot);
        animationStates.remove(slot);
        animationLastTick.remove(slot);
        animationNextTick.remove(slot);
        return this;
    }

    /**
     * Pause all active animations
     * @return This GUI for chaining
     */
    public AuroraGui pauseAllAnimations() {
        for (Integer slot : animations.keySet()) {
            pauseAnimation(slot);
        }
        return this;
    }

    /**
     * Resume all paused animations
     * @return This GUI for chaining
     */
    public AuroraGui resumeAllAnimations() {
        for (Integer slot : animations.keySet()) {
            resumeAnimation(slot);
        }
        return this;
    }

    /**
     * Called by AnimationScheduler every tick to update animations
     * @param slot The animation slot to tick
     * @param currentTick The current server tick
     */
    public void tickAnimation(int slot, long currentTick) {
        AnimationState state = animationStates.get(slot);
        if (state != null && (state.stopped || state.paused)) {
            return;
        }

        Long nextTick = animationNextTick.get(slot);
        if (nextTick == null || currentTick < nextTick) {
            return; // Not time to update yet
        }

        IAnimation animation = animations.get(slot);
        if (animation == null) return;

        // Update animation
        if (animation instanceof MultiSlotAnimation) {
            updateMultiSlotAnimation(slot, (MultiSlotAnimation) animation);
        } else {
            updateSingleSlotAnimation(slot, animation);
        }

        // Schedule next update
        animationNextTick.put(slot, currentTick + animation.getDuration());

        // Check if animation should continue
        if (!animation.shouldContinue()) {
            stopAnimation(slot);

            // Call animation complete listeners
            for (GuiListener listener : listeners) {
                listener.onAnimationComplete(this, slot);
            }
        }
    }

    /**
     * Update a single-slot animation
     */
    private void updateSingleSlotAnimation(int slot, IAnimation animation) {
        ItemStack item = animation.getNextItem();
        Consumer<InventoryClickEvent> action = animation.getClickAction();

        if (item != null) {
            inventory.setItem(slot, item);
        }
        if (action != null) {
            clickActions.put(slot, action);
        } else {
            clickActions.remove(slot);
        }
    }

    /**
     * Update a multi-slot animation
     */
    private void updateMultiSlotAnimation(int slot, MultiSlotAnimation animation) {
        Map<Integer, MultiSlotAnimation.AnimationSlot> frame = animation.getNextFrame();

        // Clear previous frame slots
        for (int s : frame.keySet()) {
            inventory.setItem(s, null);
            clickActions.remove(s);
        }

        // Set new frame
        for (Map.Entry<Integer, MultiSlotAnimation.AnimationSlot> entry : frame.entrySet()) {
            int s = entry.getKey();
            MultiSlotAnimation.AnimationSlot animSlot = entry.getValue();
            if (animSlot.getItem() != null) {
                inventory.setItem(s, animSlot.getItem());
            }
            if (animSlot.getClickAction() != null) {
                clickActions.put(s, animSlot.getClickAction());
            }
        }
    }

    /**
     * Cleanup method to stop all animations and clear resources
     * Called when GUI is closed or player quits
     */
    public void cleanup() {
        // Cancel auto-flush task
        if (autoFlushTask != null && !autoFlushTask.isCancelled()) {
            autoFlushTask.cancel();
        }

        // Unregister from animation scheduler
        if (manager != null) {
            manager.getAnimationScheduler().unregisterAll(this);
        }

        // Stop all animation tasks
        for (BukkitTask task : animationTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        animationTasks.clear();
        animations.clear();
        animationStates.clear();
        animationLastTick.clear();
        animationNextTick.clear();
        pendingAnimations.clear();
        pendingUpdates.clear();
        pendingActions.clear();
    }

    /**
     * Begins batch update mode
     * Updates are buffered and applied together when endBatch() is called
     * Reduces packet spam for bulk updates
     *
     * @return This GUI for chaining
     */
    public AuroraGui beginBatch() {
        batchMode = true;
        pendingUpdates.clear();
        pendingActions.clear();

        // Cancel any existing auto-flush
        if (autoFlushTask != null && !autoFlushTask.isCancelled()) {
            autoFlushTask.cancel();
        }

        // Schedule auto-flush after 1 tick as safety measure
        if (manager != null) {
            autoFlushTask = Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
                if (batchMode) {
                    GuiDebugger.warn("Auto-flushing batch updates (endBatch not called)");
                    endBatch();
                }
            }, 1L);
        }

        return this;
    }

    /**
     * Ends batch update mode and flushes all pending updates
     *
     * @return This GUI for chaining
     */
    public AuroraGui endBatch() {
        if (!batchMode) return this;

        // Cancel auto-flush
        if (autoFlushTask != null && !autoFlushTask.isCancelled()) {
            autoFlushTask.cancel();
        }

        // Flush updates
        flushBatchUpdates();

        batchMode = false;
        pendingUpdates.clear();
        pendingActions.clear();

        return this;
    }

    /**
     * Internal method to flush all pending batch updates
     */
    private void flushBatchUpdates() {
        if (pendingUpdates.isEmpty()) return;

        // Apply all updates
        for (Map.Entry<Integer, ItemStack> entry : pendingUpdates.entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();

            inventory.setItem(slot, item);

            // Apply action if present
            Consumer<InventoryClickEvent> action = pendingActions.get(slot);
            if (action != null) {
                clickActions.put(slot, action);
            }
        }

        // Update viewers
        for (Player viewer : getViewers()) {
            viewer.updateInventory();
        }
    }

    /**
     * Bulk update multiple slots efficiently
     * Automatically uses batch mode
     *
     * @param updates Map of slot -> ItemStack updates
     * @return This GUI for chaining
     */
    public AuroraGui batchUpdate(Map<Integer, ItemStack> updates) {
        beginBatch();
        for (Map.Entry<Integer, ItemStack> entry : updates.entrySet()) {
            updateItem(entry.getKey(), entry.getValue(), null);
        }
        endBatch();
        return this;
    }

    /**
     * Checks if GUI is in batch mode
     *
     * @return true if batch mode is active
     */
    public boolean isBatchMode() {
        return batchMode;
    }

    // ==================== Cooldown Methods ====================

    /**
     * Sets a global click cooldown for this GUI
     *
     * @param milliseconds Cooldown duration in milliseconds
     * @return This GUI for chaining
     */
    public AuroraGui setGlobalCooldown(long milliseconds) {
        cooldownManager.setDefaultCooldown(milliseconds);
        return this;
    }

    /**
     * Sets a cooldown for a specific slot
     *
     * @param slot The slot number
     * @param milliseconds Cooldown duration in milliseconds
     * @return This GUI for chaining
     */
    public AuroraGui setSlotCooldown(int slot, long milliseconds) {
        slotCooldowns.put(slot, milliseconds);
        return this;
    }

    /**
     * Removes cooldown from a specific slot
     *
     * @param slot The slot number
     * @return This GUI for chaining
     */
    public AuroraGui removeSlotCooldown(int slot) {
        slotCooldowns.remove(slot);
        return this;
    }

    /**
     * Enables or disables cooldowns for this GUI
     *
     * @param enabled true to enable cooldowns
     * @return This GUI for chaining
     */
    public AuroraGui setCooldownsEnabled(boolean enabled) {
        cooldownManager.setEnabled(enabled);
        return this;
    }

    /**
     * Gets the cooldown manager for this GUI
     *
     * @return The cooldown manager
     */
    public dev.aurora.Struct.Cooldown.ClickCooldown getCooldownManager() {
        return cooldownManager;
    }

    /**
     * Checks if a player can click (respecting cooldowns)
     *
     * @param player The player
     * @param slot The slot number
     * @return true if the player can click
     */
    public boolean canClick(Player player, int slot) {
        Long slotCooldown = slotCooldowns.get(slot);
        long cooldownMs = slotCooldown != null ? slotCooldown : 0;
        return cooldownManager.canClickSlot(player, slot, cooldownMs);
    }

    /**
     * Records a click (for cooldown tracking)
     *
     * @param player The player
     * @param slot The slot number
     */
    public void recordClick(Player player, int slot) {
        cooldownManager.recordSlotClick(player, slot);
    }

    /**
     * Gets remaining cooldown time for a player on a slot
     *
     * @param player The player
     * @param slot The slot number
     * @return Remaining milliseconds
     */
    public long getRemainingCooldown(Player player, int slot) {
        Long slotCooldown = slotCooldowns.get(slot);
        if (slotCooldown != null && slotCooldown > 0) {
            return cooldownManager.getRemainingSlotCooldown(player, slot, slotCooldown);
        }
        return cooldownManager.getRemainingCooldown(player);
    }

    /**
     * Clears all cooldowns for a player
     *
     * @param player The player
     */
    public void clearCooldowns(Player player) {
        cooldownManager.clearCooldowns(player);
    }

    // ==================== Slot Group Methods ====================

    /**
     * Creates a new slot group
     *
     * @param name The group name
     * @param slots The slot numbers
     * @return The created slot group
     */
    public dev.aurora.Struct.SlotGroup createSlotGroup(String name, int... slots) {
        dev.aurora.Struct.SlotGroup group = new dev.aurora.Struct.SlotGroup(name, this, slots);
        slotGroups.put(name, group);
        return group;
    }

    /**
     * Gets a slot group by name
     *
     * @param name The group name
     * @return The slot group, or null if not found
     */
    public dev.aurora.Struct.SlotGroup getSlotGroup(String name) {
        return slotGroups.get(name);
    }

    /**
     * Gets all slot groups
     *
     * @return Map of group name to SlotGroup
     */
    public Map<String, dev.aurora.Struct.SlotGroup> getSlotGroups() {
        return new java.util.HashMap<>(slotGroups);
    }

    /**
     * Removes a slot group
     *
     * @param name The group name
     * @return This GUI for chaining
     */
    public AuroraGui removeSlotGroup(String name) {
        slotGroups.remove(name);
        return this;
    }

    /**
     * Finds which group contains a slot
     *
     * @param slot The slot number
     * @return The group name, or null if not in any group
     */
    public String findSlotGroup(int slot) {
        for (Map.Entry<String, dev.aurora.Struct.SlotGroup> entry : slotGroups.entrySet()) {
            if (entry.getValue().contains(slot)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // ==================== Animated Title Methods ====================

    /**
     * Sets an animated title for this GUI
     *
     * @param animation The title animation
     * @return This GUI for chaining
     */
    public AuroraGui setAnimatedTitle(dev.aurora.Struct.Animation.TitleAnimation animation) {
        this.titleAnimation = animation;
        return this;
    }

    /**
     * Starts the title animation for a player
     *
     * @param player The player viewing the GUI
     */
    public void startTitleAnimation(Player player) {
        if (titleAnimation == null || manager == null) return;

        stopTitleAnimation();

        titleAnimation.reset();

        titleAnimationTask = Bukkit.getScheduler().runTaskTimer(
                manager.getPlugin(),
                () -> {
                    if (player.getOpenInventory() == null ||
                        !player.getOpenInventory().getTopInventory().equals(inventory)) {
                        stopTitleAnimation();
                        return;
                    }

                    String currentTitle = titleAnimation.getCurrentText();
                    dev.aurora.Compatibility.TitleCompat.updateTitle(player, currentTitle);

                    if (!titleAnimation.nextFrame()) {
                        stopTitleAnimation();
                    }
                },
                0L,
                titleAnimation.getCurrentDuration()
        );
    }

    /**
     * Stops the title animation
     */
    public void stopTitleAnimation() {
        if (titleAnimationTask != null) {
            titleAnimationTask.cancel();
            titleAnimationTask = null;
        }
    }

    /**
     * Gets the title animation
     *
     * @return The animation, or null if not set
     */
    public dev.aurora.Struct.Animation.TitleAnimation getTitleAnimation() {
        return titleAnimation;
    }

    /**
     * Checks if this GUI has an animated title
     *
     * @return true if animated title is set
     */
    public boolean hasAnimatedTitle() {
        return titleAnimation != null;
    }

    // ==================== GUI Locking Methods ====================

    /**
     * Sets the GUI lock
     *
     * @param lock The GUI lock
     * @return This GUI for chaining
     */
    public AuroraGui setLock(dev.aurora.Struct.GuiLock lock) {
        this.guiLock = lock;
        return this;
    }

    /**
     * Gets the GUI lock
     *
     * @return The GUI lock
     */
    public dev.aurora.Struct.GuiLock getLock() {
        return guiLock;
    }

    /**
     * Sets the lock mode
     *
     * @param mode The lock mode
     * @return This GUI for chaining
     */
    public AuroraGui setLockMode(dev.aurora.Struct.GuiLock.LockMode mode) {
        guiLock.setLockMode(mode);
        return this;
    }

    /**
     * Locks the GUI to prevent closing
     *
     * @return This GUI for chaining
     */
    public AuroraGui lock() {
        guiLock.setLockMode(dev.aurora.Struct.GuiLock.LockMode.PREVENT_CLOSE);
        return this;
    }

    /**
     * Unlocks the GUI
     *
     * @return This GUI for chaining
     */
    public AuroraGui unlock() {
        guiLock.unlock();
        return this;
    }

    /**
     * Checks if the GUI is locked
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return guiLock.isLocked();
    }

    /**
     * Handles close attempt with lock check
     *
     * @param player The player
     * @param event The close event
     * @return true if close was prevented
     */
    public boolean handleLockOnClose(Player player, org.bukkit.event.inventory.InventoryCloseEvent event) {
        return guiLock.handleCloseAttempt(player, event);
    }

    // ============= AUTO-SORTING METHODS =============

    /**
     * Sets the auto sorter for this GUI
     *
     * @param sorter The sorter to use
     * @return This GUI for chaining
     */
    public AuroraGui setAutoSorter(dev.aurora.Struct.Sort.GuiSorter sorter) {
        this.autoSorter = sorter;
        return this;
    }

    /**
     * Gets the auto sorter
     *
     * @return The sorter, or null if not set
     */
    public dev.aurora.Struct.Sort.GuiSorter getAutoSorter() {
        return autoSorter;
    }

    /**
     * Enables automatic sorting
     *
     * @param enabled true to enable
     * @return This GUI for chaining
     */
    public AuroraGui setAutoSortEnabled(boolean enabled) {
        this.autoSortEnabled = enabled;
        if (enabled && autoSorter != null && !paginatedItems.isEmpty()) {
            sortPaginatedItems();
        }
        return this;
    }

    /**
     * Checks if auto-sort is enabled
     *
     * @return true if enabled
     */
    public boolean isAutoSortEnabled() {
        return autoSortEnabled;
    }

    /**
     * Sorts the paginated items using the auto sorter
     * This is called automatically when items are added if auto-sort is enabled
     */
    public void sortPaginatedItems() {
        if (autoSorter != null && !paginatedItems.isEmpty()) {
            List<ItemStack> sorted = autoSorter.sort(paginatedItems);
            paginatedItems.clear();
            paginatedItems.addAll(sorted);

            // Refresh display if GUI is open
            if (manager != null && inventory != null) {
                updatePaginationDisplay();
            }
        }
    }

    /**
     * Sets the auto sorter and enables sorting
     *
     * @param sorter The sorter to use
     * @return This GUI for chaining
     */
    public AuroraGui enableAutoSort(dev.aurora.Struct.Sort.GuiSorter sorter) {
        this.autoSorter = sorter;
        this.autoSortEnabled = true;
        sortPaginatedItems();
        return this;
    }

    /**
     * Disables automatic sorting
     *
     * @return This GUI for chaining
     */
    public AuroraGui disableAutoSort() {
        this.autoSortEnabled = false;
        return this;
    }

    /**
     * Creates and enables alphabetical sorting
     *
     * @return This GUI for chaining
     */
    public AuroraGui sortAlphabetically() {
        return enableAutoSort(dev.aurora.Struct.Sort.GuiSorter.alphabetical());
    }

    /**
     * Creates and enables quantity sorting (descending)
     *
     * @return This GUI for chaining
     */
    public AuroraGui sortByQuantity() {
        return enableAutoSort(dev.aurora.Struct.Sort.GuiSorter.byQuantity());
    }

    /**
     * Creates and enables rarity sorting (descending)
     *
     * @return This GUI for chaining
     */
    public AuroraGui sortByRarity() {
        return enableAutoSort(dev.aurora.Struct.Sort.GuiSorter.byRarityDesc());
    }

    /**
     * Creates and enables multi-criteria sorting
     *
     * @return This GUI for chaining
     */
    public AuroraGui sortMultiCriteria() {
        return enableAutoSort(dev.aurora.Struct.Sort.GuiSorter.multiCriteria());
    }
}