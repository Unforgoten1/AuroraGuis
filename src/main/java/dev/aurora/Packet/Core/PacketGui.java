package dev.aurora.Packet.Core;

import dev.aurora.Debug.GuiDebugger;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.API.PacketGuiConfig;
import dev.aurora.Packet.API.ValidationLevel;
import dev.aurora.Packet.Validation.AntiDupeValidator;
import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Struct.Animation.Animation;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Packet-based GUI implementation with advanced anti-dupe protection
 * Implements the same API as AuroraGui but adds packet-level validation
 *
 * Differences from AuroraGui:
 * - Intercepts packets before Bukkit processes them
 * - Validates clicks at packet level
 * - Tracks server-side inventory truth
 * - Detects and prevents duplication exploits
 * - Configurable validation levels (BASIC, PACKET, ADVANCED)
 */
public class PacketGui implements IPacketGui {

    private final String name;
    private String title;
    private int rows;
    private Inventory inventory;
    private GuiManager manager;
    private PacketGuiConfig config;
    private AntiDupeValidator validator;
    private BiConsumer<Player, ExploitType> violationHandler;

    // Same fields as AuroraGui for API compatibility
    private final Map<Integer, Consumer<InventoryClickEvent>> clickActions;
    private final List<ItemStack> paginatedItems;
    private final Map<Integer, Consumer<InventoryClickEvent>> paginatedActions;
    private int currentPage;
    private int itemsPerPage;
    private Consumer<Integer> pageChangeCallback;
    private final Map<Integer, IAnimation> animations;
    private final List<GuiListener> listeners;
    private final Map<Integer, ClickCondition> clickConditions;
    private ItemStack[] borderItems;

    // Cooldown support
    private dev.aurora.Struct.Cooldown.ClickCooldown cooldownManager;
    private final Map<Integer, Long> slotCooldowns;

    // Session tracking for anti withhold-close-packet protection
    private final Map<UUID, Long> sessionOpenTime;
    private final Map<UUID, Long> lastActivityTime;
    private BukkitTask sessionMonitorTask;

    /**
     * Creates a new packet-based GUI with default configuration
     * @param name The unique GUI name
     */
    public PacketGui(String name) {
        this(name, PacketGuiConfig.normal());
    }

    /**
     * Creates a new packet-based GUI with custom configuration
     * @param name The unique GUI name
     * @param config The validation configuration
     */
    public PacketGui(String name, PacketGuiConfig config) {
        this.name = name;
        this.title = name;
        this.rows = 3;
        this.config = config;
        this.validator = new AntiDupeValidator(this, config);
        this.clickActions = new ConcurrentHashMap<>();
        this.paginatedItems = new ArrayList<>();
        this.paginatedActions = new ConcurrentHashMap<>();
        this.currentPage = 0;
        this.itemsPerPage = 7;
        this.animations = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.clickConditions = new ConcurrentHashMap<>();
        this.borderItems = null;
        this.cooldownManager = new dev.aurora.Struct.Cooldown.ClickCooldown();
        this.slotCooldowns = new ConcurrentHashMap<>();
        this.sessionOpenTime = new ConcurrentHashMap<>();
        this.lastActivityTime = new ConcurrentHashMap<>();
        this.sessionMonitorTask = null;
    }

    // ==================== IPacketGui Implementation ====================

    @Override
    public PacketGuiConfig getConfig() {
        return config;
    }

    @Override
    public ValidationLevel getValidationLevel() {
        return config.getValidationLevel();
    }

    @Override
    public IPacketGui setValidationLevel(ValidationLevel level) {
        config.setValidationLevel(level);
        return this;
    }

    @Override
    public AntiDupeValidator getValidator() {
        return validator;
    }

    @Override
    public IPacketGui onViolation(BiConsumer<Player, ExploitType> handler) {
        this.violationHandler = handler;
        return this;
    }

    @Override
    public void forceResync(Player player) {
        validator.forceResync(player);
    }

    @Override
    public boolean isPlayerSynced(Player player) {
        return validator.isPlayerSynced(player);
    }

    // ==================== IGui Implementation ====================

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getSize() {
        return rows * 9;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public GuiManager getManager() {
        return manager;
    }

    @Override
    public void setManager(GuiManager manager) {
        this.manager = manager;
    }

    @Override
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

    @Override
    public void open(Player player) {
        GuiDebugger.log("Opening PacketGui '" + name + "' for player " + player.getName());

        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (manager == null) {
            throw new IllegalStateException("PacketGui must be registered before opening");
        }
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
        }

        // Register in packet registry
        PacketGuiRegistry.getInstance().setActiveGui(player, this);

        // Track session opening
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        sessionOpenTime.put(uuid, now);
        lastActivityTime.put(uuid, now);

        // Start session monitoring if enabled and not already running
        if (config.isDetectStaleSession() && sessionMonitorTask == null) {
            startSessionMonitoring();
        }

        // Open via manager
        player.openInventory(inventory);

        // Call open listeners
        for (GuiListener listener : listeners) {
            listener.onOpen(player, null); // Pass null for AuroraGui parameter
        }

        if (GuiDebugger.isEnabled()) {
            GuiDebugger.log("PacketGui '" + name + "' opened with validation level: " + config.getValidationLevel());
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        // Call before click listeners
        for (GuiListener listener : listeners) {
            if (!listener.onBeforeClick(event, null)) {
                return; // Click cancelled by listener
            }
        }

        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        // Update last activity time (anti withhold-close-packet)
        lastActivityTime.put(player.getUniqueId(), System.currentTimeMillis());

        // Check cooldown
        if (!canClick(player, slot)) {
            long remaining = getRemainingCooldown(player, slot);
            if (remaining > 0) {
                player.sendMessage("§cPlease wait " + (remaining / 1000.0) + "s before clicking again.");
            }
            return;
        }

        // Check click condition
        ClickCondition condition = clickConditions.get(slot);
        if (condition != null && !condition.test(event)) {
            return;
        }

        // Record click for cooldown tracking
        recordClick(player, slot);

        Consumer<InventoryClickEvent> action = clickActions.get(slot);
        if (action != null) {
            action.accept(event);
        }

        // Call after click listeners
        for (GuiListener listener : listeners) {
            listener.onAfterClick(event, null);
        }
    }

    @Override
    public IPacketGui register(GuiManager manager) {
        GuiDebugger.log("Registering PacketGui '" + name + "' with manager");

        if (manager == null) {
            throw new IllegalArgumentException("GuiManager cannot be null");
        }

        this.manager = manager;
        PacketGuiRegistry.getInstance().registerGui(name, this);

        GuiDebugger.log("PacketGui '" + name + "' registered successfully");
        return this;
    }

    @Override
    public IPacketGui addListener(GuiListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    @Override
    public IPacketGui removeListener(GuiListener listener) {
        listeners.remove(listener);
        return this;
    }

    @Override
    public List<GuiListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    @Override
    public void cleanup() {
        // Stop session monitoring
        if (sessionMonitorTask != null && !sessionMonitorTask.isCancelled()) {
            sessionMonitorTask.cancel();
            sessionMonitorTask = null;
        }

        // Clean up validator resources
        for (Player viewer : getViewers()) {
            validator.cleanup(viewer);
            PacketGuiRegistry.getInstance().removeActiveGui(viewer);

            // Clean up session tracking
            UUID uuid = viewer.getUniqueId();
            sessionOpenTime.remove(uuid);
            lastActivityTime.remove(uuid);
        }

        animations.clear();
    }

    @Override
    public IPacketGui update() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
            return this;
        }

        for (Player viewer : getViewers()) {
            viewer.updateInventory();
        }

        return this;
    }

    // ==================== Fluent API Methods (AuroraGui compatibility) ====================

    public PacketGui title(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        if (title.length() > 32) {
            this.title = title.substring(0, 32);
        } else {
            this.title = title;
        }
        return this;
    }

    public PacketGui rows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be 1-6");
        }
        this.rows = rows;
        this.itemsPerPage = (rows - 2) * 7;
        return this;
    }

    public PacketGui setBorder(BorderType type, ItemStack borderItem) {
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
        }
        if (borderItem == null) {
            return this;
        }

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

    public PacketGui addItem(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
        }
        int slot = findNextEmptySlot();
        if (slot == -1) {
            throw new IllegalStateException("GUI is full, cannot add more items");
        }
        if (slot >= 0) {
            inventory.setItem(slot, item);
            if (clickAction != null) {
                clickActions.put(slot, clickAction);
            }
        }
        return this;
    }

    public PacketGui addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (slot < 0 || slot >= rows * 9) {
            throw new IllegalArgumentException("Invalid slot: " + slot);
        }
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
        }
        inventory.setItem(slot, item);
        if (clickAction != null) {
            clickActions.put(slot, clickAction);
        }
        return this;
    }

    public PacketGui setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
        }
        int targetSlot = slot == -1 ? findNextEmptySlot() : slot;
        if (targetSlot >= 0 && targetSlot < rows * 9) {
            inventory.setItem(targetSlot, item);
            clickActions.remove(targetSlot);
            if (clickAction != null) {
                clickActions.put(targetSlot, clickAction);
            }
        }
        return this;
    }

    public PacketGui setItem(int slot, ItemStack item) {
        return setItem(slot, item, null);
    }

    public PacketGui addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction, ClickCondition condition) {
        addItem(slot, item, clickAction);
        if (condition != null) {
            clickConditions.put(slot, condition);
        }
        return this;
    }

    public PacketGui setGlobalCooldown(long milliseconds) {
        cooldownManager.setDefaultCooldown(milliseconds);
        return this;
    }

    public PacketGui setSlotCooldown(int slot, long milliseconds) {
        slotCooldowns.put(slot, milliseconds);
        return this;
    }

    public PacketGui addAnimation(int slot, Animation animation) {
        if (slot >= 0 && slot < rows * 9 && animation != null) {
            animations.put(slot, animation);
        }
        return this;
    }

    public PacketGui addCustomAnimation(int slot, IAnimation animation) {
        if (slot >= 0 && slot < rows * 9 && animation != null) {
            animations.put(slot, animation);
        }
        return this;
    }

    // ==================== Internal Helper Methods ====================

    private int findNextEmptySlot() {
        for (int i = 0; i < rows * 9; i++) {
            if (inventory.getItem(i) == null && !animations.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean canClick(Player player, int slot) {
        Long slotCooldown = slotCooldowns.get(slot);
        long cooldownMs = slotCooldown != null ? slotCooldown : 0;
        return cooldownManager.canClickSlot(player, slot, cooldownMs);
    }

    private void recordClick(Player player, int slot) {
        cooldownManager.recordSlotClick(player, slot);
    }

    private long getRemainingCooldown(Player player, int slot) {
        Long slotCooldown = slotCooldowns.get(slot);
        if (slotCooldown != null && slotCooldown > 0) {
            return cooldownManager.getRemainingSlotCooldown(player, slot, slotCooldown);
        }
        return cooldownManager.getRemainingCooldown(player);
    }

    /**
     * Internal method to trigger violation handler
     * @param player The player who triggered the violation
     * @param exploitType The type of exploit detected
     */
    public void triggerViolation(Player player, ExploitType exploitType) {
        if (violationHandler != null) {
            violationHandler.accept(player, exploitType);
        }

        // Log to debug
        GuiDebugger.warn("Exploit attempt detected in PacketGui '" + name + "': " +
                         player.getName() + " - " + exploitType.getDescription());
    }

    // ==================== Session Monitoring (Anti Withhold-Close-Packet) ====================

    /**
     * Starts the session monitoring task
     * Periodically checks for stale sessions where players may have withheld the close packet
     */
    private void startSessionMonitoring() {
        if (manager == null) {
            return;
        }

        long intervalTicks = config.getInactivityCheckIntervalMs() / 50; // Convert ms to ticks

        sessionMonitorTask = Bukkit.getScheduler().runTaskTimer(
                manager.getPlugin(),
                this::checkForStaleSessions,
                intervalTicks,
                intervalTicks
        );

        GuiDebugger.log("Session monitoring started for PacketGui '" + name + "' (interval: " + config.getInactivityCheckIntervalMs() + "ms)");
    }

    /**
     * Checks all active sessions for staleness
     * Detects players who may have withheld the close packet
     */
    private void checkForStaleSessions() {
        long now = System.currentTimeMillis();
        long timeoutMs = config.getSessionTimeoutMs();

        // Create a copy of the entries to avoid concurrent modification
        List<Map.Entry<UUID, Long>> entries = new ArrayList<>(sessionOpenTime.entrySet());

        for (Map.Entry<UUID, Long> entry : entries) {
            UUID uuid = entry.getKey();
            long openTime = entry.getValue();
            long lastActivity = lastActivityTime.getOrDefault(uuid, openTime);

            // Calculate session duration and inactivity time
            long sessionDuration = now - openTime;
            long inactivityTime = now - lastActivity;

            // Check if session has timed out
            if (sessionDuration > timeoutMs || inactivityTime > timeoutMs) {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null && player.isOnline()) {
                    // Player is online but session is stale

                    // Check if player actually has this GUI open
                    if (!isPlayerActuallyViewing(player)) {
                        // EXPLOIT DETECTED: Player withheld close packet!
                        handleWithheldClosePacket(player);
                    } else if (inactivityTime > timeoutMs) {
                        // Player is AFK with GUI open too long
                        handleStaleSession(player, inactivityTime);
                    }
                } else {
                    // Player is offline, clean up
                    cleanupSession(uuid);
                }
            }
        }
    }

    /**
     * Checks if a player is actually viewing this GUI
     * @param player The player
     * @return true if the player's open inventory matches this GUI
     */
    private boolean isPlayerActuallyViewing(Player player) {
        if (player.getOpenInventory() == null) {
            return false;
        }
        return player.getOpenInventory().getTopInventory().equals(inventory);
    }

    /**
     * Handles detection of withheld close packet exploit
     * This is a critical exploit where players prevent sending the close packet
     *
     * @param player The player
     */
    private void handleWithheldClosePacket(Player player) {
        GuiDebugger.warn("CRITICAL: Withheld close packet detected for " + player.getName() + " in GUI '" + name + "'");

        // Log violation
        validator.getViolationLogger().logViolation(
                player,
                this,
                ExploitType.NO_CLOSE_PACKET,
                "Player withheld close packet - GUI tracking shows open but player not viewing"
        );

        // Trigger violation handler
        triggerViolation(player, ExploitType.NO_CLOSE_PACKET);

        // Force close and cleanup
        if (config.isForceCloseOnTimeout()) {
            forceCloseGui(player);
        }

        // Kick if configured
        if (config.isKickOnViolation()) {
            int totalViolations = validator.getViolationLogger().getTotalViolations(player);
            if (totalViolations >= config.getViolationKickThreshold()) {
                player.kickPlayer("§cExploit attempt detected\n§7Withheld GUI close packet");
            }
        }
    }

    /**
     * Handles stale session (player AFK too long with GUI open)
     * @param player The player
     * @param inactivityTime How long they've been inactive
     */
    private void handleStaleSession(Player player, long inactivityTime) {
        GuiDebugger.log("Stale session detected for " + player.getName() + " in GUI '" + name + "' (inactive for " + (inactivityTime / 1000) + "s)");

        // Log as lower severity violation
        validator.getViolationLogger().logViolation(
                player,
                this,
                ExploitType.STALE_SESSION,
                "Session inactive for " + (inactivityTime / 1000) + " seconds"
        );

        // Trigger violation handler
        triggerViolation(player, ExploitType.STALE_SESSION);

        // Force close if configured
        if (config.isForceCloseOnTimeout()) {
            player.sendMessage("§cYour GUI session expired due to inactivity.");
            forceCloseGui(player);
        }
    }

    /**
     * Forces a GUI to close for a player and cleans up tracking
     * @param player The player
     */
    private void forceCloseGui(Player player) {
        UUID uuid = player.getUniqueId();

        // Close inventory
        player.closeInventory();

        // Force cleanup
        cleanupSession(uuid);

        // Remove from registry
        PacketGuiRegistry.getInstance().removeActiveGui(uuid);

        // Clean up validator
        validator.cleanup(player);
    }

    /**
     * Cleans up session tracking for a player
     * @param uuid The player UUID
     */
    private void cleanupSession(UUID uuid) {
        sessionOpenTime.remove(uuid);
        lastActivityTime.remove(uuid);
    }

    /**
     * Gets the session open time for a player
     * @param player The player
     * @return Milliseconds since epoch when GUI was opened, or 0 if not tracked
     */
    public long getSessionOpenTime(Player player) {
        return sessionOpenTime.getOrDefault(player.getUniqueId(), 0L);
    }

    /**
     * Gets the last activity time for a player
     * @param player The player
     * @return Milliseconds since epoch of last activity, or 0 if not tracked
     */
    public long getLastActivityTime(Player player) {
        return lastActivityTime.getOrDefault(player.getUniqueId(), 0L);
    }

    /**
     * Gets the session duration for a player
     * @param player The player
     * @return Milliseconds the GUI has been open, or 0 if not open
     */
    public long getSessionDuration(Player player) {
        Long openTime = sessionOpenTime.get(player.getUniqueId());
        if (openTime == null) {
            return 0;
        }
        return System.currentTimeMillis() - openTime;
    }

    /**
     * Gets the inactivity time for a player
     * @param player The player
     * @return Milliseconds since last activity, or 0 if not tracked
     */
    public long getInactivityTime(Player player) {
        Long lastActivity = lastActivityTime.get(player.getUniqueId());
        if (lastActivity == null) {
            return 0;
        }
        return System.currentTimeMillis() - lastActivity;
    }
}
