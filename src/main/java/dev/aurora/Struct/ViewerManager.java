package dev.aurora.Struct;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages viewers and their permissions for shared GUIs
 */
public class ViewerManager {
    private final Map<UUID, ViewerPermissions> viewers;

    public ViewerManager() {
        this.viewers = new ConcurrentHashMap<>();
    }

    /**
     * Adds a viewer with permissions
     */
    public void addViewer(UUID playerId, ViewerPermissions permissions) {
        viewers.put(playerId, permissions);
    }

    /**
     * Removes a viewer
     */
    public void removeViewer(UUID playerId) {
        viewers.remove(playerId);
    }

    /**
     * Gets permissions for a viewer
     */
    public ViewerPermissions getPermissions(UUID playerId) {
        return viewers.get(playerId);
    }

    /**
     * Checks if player is a viewer
     */
    public boolean isViewer(UUID playerId) {
        return viewers.containsKey(playerId);
    }

    /**
     * Gets all active viewers as Player objects
     */
    public List<Player> getActivePlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : viewers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Gets all viewer UUIDs
     */
    public Set<UUID> getViewerIds() {
        return new HashSet<>(viewers.keySet());
    }

    /**
     * Gets viewer count
     */
    public int getViewerCount() {
        return viewers.size();
    }

    /**
     * Clears all viewers
     */
    public void clear() {
        viewers.clear();
    }

    /**
     * Permissions for a GUI viewer
     */
    public static class ViewerPermissions {
        private boolean canClick = true;
        private boolean canDrag = false;
        private Set<Integer> allowedSlots;
        private Set<Integer> deniedSlots;

        public ViewerPermissions() {
            this.allowedSlots = null; // null = all slots
            this.deniedSlots = new HashSet<>();
        }

        public boolean canClick() {
            return canClick;
        }

        public ViewerPermissions setCanClick(boolean canClick) {
            this.canClick = canClick;
            return this;
        }

        public boolean canDrag() {
            return canDrag;
        }

        public ViewerPermissions setCanDrag(boolean canDrag) {
            this.canDrag = canDrag;
            return this;
        }

        public ViewerPermissions setAllowedSlots(int... slots) {
            this.allowedSlots = new HashSet<>();
            for (int slot : slots) {
                this.allowedSlots.add(slot);
            }
            return this;
        }

        public ViewerPermissions setDeniedSlots(int... slots) {
            for (int slot : slots) {
                this.deniedSlots.add(slot);
            }
            return this;
        }

        public boolean canAccessSlot(int slot) {
            if (deniedSlots.contains(slot)) return false;
            if (allowedSlots == null) return true;
            return allowedSlots.contains(slot);
        }

        /**
         * Creates read-only permissions
         */
        public static ViewerPermissions readOnly() {
            return new ViewerPermissions().setCanClick(false).setCanDrag(false);
        }

        /**
         * Creates full access permissions
         */
        public static ViewerPermissions fullAccess() {
            return new ViewerPermissions().setCanClick(true).setCanDrag(true);
        }
    }
}
