package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Struct.ViewerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * GUI that supports multiple simultaneous viewers
 * Updates are synchronized and broadcast to all viewers
 */
public class SharedGui extends AuroraGui {
    private final ViewerManager viewerManager;
    private final ReadWriteLock lock;
    private ViewUpdateMode updateMode;
    private final Map<Integer, ItemStack> pendingBroadcasts;
    private Consumer<Player> onViewerJoin;
    private Consumer<Player> onViewerLeave;

    /**
     * Update mode for shared GUIs
     */
    public enum ViewUpdateMode {
        /** Updates are immediately sent to all viewers */
        IMMEDIATE,
        /** Updates are batched and sent at end of tick */
        BATCHED,
        /** Updates must be manually triggered */
        MANUAL
    }

    /**
     * Creates a new SharedGui
     *
     * @param name The GUI name
     */
    public SharedGui(String name) {
        super(name);
        this.viewerManager = new ViewerManager();
        this.lock = new ReentrantReadWriteLock();
        this.updateMode = ViewUpdateMode.IMMEDIATE;
        this.pendingBroadcasts = new HashMap<>();
    }

    /**
     * Adds a viewer with specific permissions
     *
     * @param player The player to add
     * @param permissions The viewer permissions
     * @return This GUI for chaining
     */
    public SharedGui addViewer(Player player, ViewerManager.ViewerPermissions permissions) {
        lock.writeLock().lock();
        try {
            viewerManager.addViewer(player.getUniqueId(), permissions);

            // Fire join event
            if (onViewerJoin != null) {
                onViewerJoin.accept(player);
            }

            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Adds a viewer with full access
     *
     * @param player The player to add
     * @return This GUI for chaining
     */
    public SharedGui addViewer(Player player) {
        return addViewer(player, ViewerManager.ViewerPermissions.fullAccess());
    }

    /**
     * Removes a viewer
     *
     * @param player The player to remove
     * @return This GUI for chaining
     */
    public SharedGui removeViewer(Player player) {
        lock.writeLock().lock();
        try {
            viewerManager.removeViewer(player.getUniqueId());

            // Fire leave event
            if (onViewerLeave != null) {
                onViewerLeave.accept(player);
            }

            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Sets the update mode
     *
     * @param mode The update mode
     * @return This GUI for chaining
     */
    public SharedGui setUpdateMode(ViewUpdateMode mode) {
        this.updateMode = mode;
        return this;
    }

    /**
     * Sets callback when viewer joins
     *
     * @param callback The callback
     * @return This GUI for chaining
     */
    public SharedGui onViewerJoin(Consumer<Player> callback) {
        this.onViewerJoin = callback;
        return this;
    }

    /**
     * Sets callback when viewer leaves
     *
     * @param callback The callback
     * @return This GUI for chaining
     */
    public SharedGui onViewerLeave(Consumer<Player> callback) {
        this.onViewerLeave = callback;
        return this;
    }

    /**
     * Broadcasts an update to all viewers
     *
     * @param slot The slot to update
     * @param item The item to set
     * @return This GUI for chaining
     */
    public SharedGui broadcastUpdate(int slot, ItemStack item) {
        lock.writeLock().lock();
        try {
            getInventory().setItem(slot, item);

            switch (updateMode) {
                case IMMEDIATE:
                    sendUpdateToViewers(slot, item);
                    break;
                case BATCHED:
                    pendingBroadcasts.put(slot, item);
                    scheduleFlush();
                    break;
                case MANUAL:
                    pendingBroadcasts.put(slot, item);
                    break;
            }

            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Manually triggers broadcast of pending updates (for MANUAL mode)
     *
     * @return This GUI for chaining
     */
    public SharedGui flushUpdates() {
        lock.writeLock().lock();
        try {
            for (Map.Entry<Integer, ItemStack> entry : pendingBroadcasts.entrySet()) {
                sendUpdateToViewers(entry.getKey(), entry.getValue());
            }
            pendingBroadcasts.clear();
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets all active viewers
     *
     * @return List of viewer players
     */
    public List<Player> getActiveViewers() {
        lock.readLock().lock();
        try {
            return viewerManager.getActivePlayers();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets viewer count
     *
     * @return Number of viewers
     */
    public int getViewerCount() {
        lock.readLock().lock();
        try {
            return viewerManager.getViewerCount();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        lock.readLock().lock();
        try {
            ViewerManager.ViewerPermissions perms = viewerManager.getPermissions(player.getUniqueId());

            if (perms == null) {
                event.setCancelled(true);
                return;
            }

            // Check permissions
            if (!perms.canClick()) {
                event.setCancelled(true);
                return;
            }

            if (!perms.canAccessSlot(event.getSlot())) {
                event.setCancelled(true);
                return;
            }
        } finally {
            lock.readLock().unlock();
        }

        // Call parent handler
        super.handleClick(event);
    }

    @Override
    public AuroraGui updateItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (viewerManager.getViewerCount() > 0) {
            // Use broadcast update for shared GUIs
            broadcastUpdate(slot, item);
            if (clickAction != null) {
                getClickActions().put(slot, clickAction);
            }
            return this;
        } else {
            // No viewers, use normal update
            return super.updateItem(slot, item, clickAction);
        }
    }

    /**
     * Opens this GUI for a player and adds them as a viewer
     *
     * @param player The player to open for
     */
    @Override
    public void open(Player player) {
        addViewer(player);
        super.open(player);
    }

    @Override
    public void cleanup() {
        lock.writeLock().lock();
        try {
            viewerManager.clear();
            pendingBroadcasts.clear();
        } finally {
            lock.writeLock().unlock();
        }
        super.cleanup();
    }

    /**
     * Sends update to all viewers
     */
    private void sendUpdateToViewers(int slot, ItemStack item) {
        for (Player viewer : viewerManager.getActivePlayers()) {
            viewer.getOpenInventory().getTopInventory().setItem(slot, item);
        }
    }

    /**
     * Schedules flush of batched updates
     */
    private void scheduleFlush() {
        if (getManager() != null) {
            org.bukkit.Bukkit.getScheduler().runTask(getManager().getPlugin(), this::flushUpdates);
        }
    }

    /**
     * Gets click actions map (for internal use)
     */
    private Map<Integer, Consumer<InventoryClickEvent>> getClickActions() {
        try {
            java.lang.reflect.Field field = AuroraGui.class.getDeclaredField("clickActions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, Consumer<InventoryClickEvent>> actions =
                (Map<Integer, Consumer<InventoryClickEvent>>) field.get(this);
            return actions;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

}
