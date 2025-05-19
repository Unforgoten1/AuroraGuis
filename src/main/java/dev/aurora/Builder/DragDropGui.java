package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.DragZone;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * GUI with drag-and-drop support
 * Allows items to be dragged between defined zones with validation
 */
public class DragDropGui extends AuroraGui {
    private static final Map<UUID, DragState> dragStates = new ConcurrentHashMap<>();
    private static DragDropListener listener;

    private final Map<String, DragZone> zones;
    private BiConsumer<Player, DragEvent> onDragStart;
    private BiConsumer<Player, DragEvent> onDragComplete;
    private BiConsumer<Player, DragEvent> onDrop;
    private boolean allowQuickMove = false;

    /**
     * Represents the state of a drag operation
     */
    private static class DragState {
        final int sourceSlot;
        final String sourceZone;
        final ItemStack draggedItem;
        final long startTime;

        DragState(int sourceSlot, String sourceZone, ItemStack draggedItem) {
            this.sourceSlot = sourceSlot;
            this.sourceZone = sourceZone;
            this.draggedItem = draggedItem;
            this.startTime = System.currentTimeMillis();
        }
    }

    /**
     * Information about a drag/drop event
     */
    public static class DragEvent {
        private final Player player;
        private final int fromSlot;
        private final int toSlot;
        private final String fromZone;
        private final String toZone;
        private final ItemStack item;
        private boolean cancelled = false;

        public DragEvent(Player player, int fromSlot, int toSlot, String fromZone,
                        String toZone, ItemStack item) {
            this.player = player;
            this.fromSlot = fromSlot;
            this.toSlot = toSlot;
            this.fromZone = fromZone;
            this.toZone = toZone;
            this.item = item;
        }

        public Player getPlayer() { return player; }
        public int getFromSlot() { return fromSlot; }
        public int getToSlot() { return toSlot; }
        public String getFromZone() { return fromZone; }
        public String getToZone() { return toZone; }
        public ItemStack getItem() { return item; }
        public boolean isCancelled() { return cancelled; }
        public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    }

    /**
     * Listener for drag events across all DragDropGuis
     */
    private static class DragDropListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryDrag(InventoryDragEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
            DragState state = dragStates.get(player.getUniqueId());

            if (state != null) {
                // We're tracking this drag - validate drop
                event.setCancelled(true);
            }
        }
    }

    /**
     * Creates a new DragDropGui
     *
     * @param name The GUI name
     */
    public DragDropGui(String name) {
        super(name);
        this.zones = new ConcurrentHashMap<>();

        // Register listener if needed
        if (listener == null) {
            listener = new DragDropListener();
            try {
                JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugins()[0];
                Bukkit.getPluginManager().registerEvents(listener, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds a drag zone to this GUI
     *
     * @param zone The zone to add
     * @return This GUI for chaining
     */
    public DragDropGui addZone(DragZone zone) {
        zones.put(zone.getName(), zone);
        return this;
    }

    /**
     * Sets the callback when drag starts
     *
     * @param callback The callback
     * @return This GUI for chaining
     */
    public DragDropGui onDragStart(BiConsumer<Player, DragEvent> callback) {
        this.onDragStart = callback;
        return this;
    }

    /**
     * Sets the callback when drag completes (successful drop)
     *
     * @param callback The callback
     * @return This GUI for chaining
     */
    public DragDropGui onDragComplete(BiConsumer<Player, DragEvent> callback) {
        this.onDragComplete = callback;
        return this;
    }

    /**
     * Sets the callback when item is dropped
     *
     * @param callback The callback
     * @return This GUI for chaining
     */
    public DragDropGui onDrop(BiConsumer<Player, DragEvent> callback) {
        this.onDrop = callback;
        return this;
    }

    /**
     * Enables shift-click quick move between zones
     *
     * @param allow true to allow quick move
     * @return This GUI for chaining
     */
    public DragDropGui allowQuickMove(boolean allow) {
        this.allowQuickMove = allow;
        return this;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();

        // Find zone for this slot
        String zoneName = getZoneForSlot(slot);

        // Handle shift-click quick move
        if (allowQuickMove && event.getClick() == ClickType.SHIFT_LEFT) {
            if (zoneName != null && clickedItem != null) {
                handleQuickMove(event, player, slot, zoneName, clickedItem);
                return;
            }
        }

        // Handle drag start (picking up item)
        if (clickedItem != null && zoneName != null) {
            DragZone zone = zones.get(zoneName);
            if (zone != null && !zone.isReadOnly()) {
                // Start drag
                DragState state = new DragState(slot, zoneName, clickedItem.clone());
                dragStates.put(player.getUniqueId(), state);

                // Fire drag start event
                if (onDragStart != null) {
                    DragEvent dragEvent = new DragEvent(player, slot, -1, zoneName, null, clickedItem);
                    onDragStart.accept(player, dragEvent);
                }
            }
        }

        // Handle drop (placing item)
        DragState dragState = dragStates.get(player.getUniqueId());
        if (dragState != null && event.getCursor() != null && !event.getCursor().getType().name().equals("AIR")) {
            String targetZone = getZoneForSlot(slot);

            if (targetZone != null) {
                handleDrop(event, player, dragState, slot, targetZone);
                return;
            }
        }

        // Clear drag state if clicking empty slot
        if (event.getCursor() == null || event.getCursor().getType().name().equals("AIR")) {
            dragStates.remove(player.getUniqueId());
        }

        // Call parent handler
        super.handleClick(event);
    }

    /**
     * Handles dropping an item into a zone
     */
    private void handleDrop(InventoryClickEvent event, Player player, DragState dragState,
                           int targetSlot, String targetZone) {
        event.setCancelled(true);

        DragZone sourceZone = zones.get(dragState.sourceZone);
        DragZone destZone = zones.get(targetZone);

        if (sourceZone == null || destZone == null) {
            dragStates.remove(player.getUniqueId());
            return;
        }

        // Check if drag is allowed between zones
        if (!sourceZone.canDragTo(targetZone)) {
            player.sendMessage("&cCannot drag items to this zone!");
            dragStates.remove(player.getUniqueId());
            return;
        }

        // Validate item
        if (!destZone.validateItem(player, dragState.draggedItem)) {
            player.sendMessage(destZone.getErrorMessage());
            dragStates.remove(player.getUniqueId());
            return;
        }

        // Create drag event
        DragEvent dragEvent = new DragEvent(
            player,
            dragState.sourceSlot,
            targetSlot,
            dragState.sourceZone,
            targetZone,
            dragState.draggedItem
        );

        // Fire drop event
        if (onDrop != null) {
            onDrop.accept(player, dragEvent);
            if (dragEvent.isCancelled()) {
                dragStates.remove(player.getUniqueId());
                return;
            }
        }

        // Perform the drop (handled by Bukkit)
        event.setCancelled(false);

        // Fire drag complete event
        if (onDragComplete != null) {
            Bukkit.getScheduler().runTask(
                (JavaPlugin) Bukkit.getPluginManager().getPlugins()[0],
                () -> onDragComplete.accept(player, dragEvent)
            );
        }

        dragStates.remove(player.getUniqueId());
    }

    /**
     * Handles shift-click quick move
     */
    private void handleQuickMove(InventoryClickEvent event, Player player, int sourceSlot,
                                 String sourceZone, ItemStack item) {
        event.setCancelled(true);

        DragZone source = zones.get(sourceZone);
        if (source == null || source.isReadOnly()) return;

        // Find first valid target zone
        for (DragZone targetZone : zones.values()) {
            if (targetZone.getName().equals(sourceZone)) continue;
            if (!source.canDragTo(targetZone.getName())) continue;
            if (!targetZone.validateItem(player, item)) continue;

            // Find empty slot in target zone
            for (int targetSlot : targetZone.getSlots()) {
                ItemStack slotItem = getInventory().getItem(targetSlot);
                if (slotItem == null || slotItem.getType().name().equals("AIR")) {
                    // Move item
                    getInventory().setItem(targetSlot, item);
                    getInventory().setItem(sourceSlot, null);

                    // Fire events
                    DragEvent dragEvent = new DragEvent(
                        player, sourceSlot, targetSlot,
                        sourceZone, targetZone.getName(), item
                    );

                    if (onDrop != null) onDrop.accept(player, dragEvent);
                    if (onDragComplete != null) onDragComplete.accept(player, dragEvent);

                    return;
                }
            }
        }

        player.sendMessage("&cNo valid destination for this item!");
    }

    /**
     * Gets the zone name for a specific slot
     */
    private String getZoneForSlot(int slot) {
        for (DragZone zone : zones.values()) {
            if (zone.containsSlot(slot)) {
                return zone.getName();
            }
        }
        return null;
    }

    /**
     * Cleans up drag states
     */
    @Override
    public void cleanup() {
        super.cleanup();
        dragStates.clear();
    }

    /**
     * Global cleanup (call on plugin disable)
     */
    public static void cleanupAll() {
        dragStates.clear();
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
    }
}
