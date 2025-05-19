package dev.aurora.Struct.Persistence;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles GUI state persistence for players
 * Allows saving and loading GUI states across sessions
 */
public class PersistentGui {
    private final AuroraGui gui;
    private final Map<UUID, GuiState> playerStates;
    private boolean enabled;
    private File dataFolder;
    private PersistenceMode mode;
    private final Set<PersistenceEvent> saveEvents;

    public enum PersistenceMode {
        /** Save to memory only (lost on restart) */
        MEMORY,
        /** Save to disk (persists across restarts) */
        DISK,
        /** Save to both memory and disk */
        BOTH
    }

    public enum PersistenceEvent {
        /** Save when GUI is closed */
        CLOSE,
        /** Save when item is clicked */
        CLICK,
        /** Save when page is changed */
        PAGE_CHANGE,
        /** Save manually only */
        MANUAL
    }

    /**
     * Represents a saved GUI state
     */
    public static class GuiState implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Map<Integer, ItemStack> items;
        private final int currentPage;
        private final Map<String, Object> customData;
        private final long timestamp;

        public GuiState() {
            this.items = new HashMap<>();
            this.currentPage = 0;
            this.customData = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }

        public GuiState(Map<Integer, ItemStack> items, int currentPage, Map<String, Object> customData) {
            this.items = new HashMap<>(items);
            this.currentPage = currentPage;
            this.customData = new HashMap<>(customData);
            this.timestamp = System.currentTimeMillis();
        }

        public Map<Integer, ItemStack> getItems() { return items; }
        public int getCurrentPage() { return currentPage; }
        public Map<String, Object> getCustomData() { return customData; }
        public long getTimestamp() { return timestamp; }

        public void setItem(int slot, ItemStack item) {
            if (item == null) {
                items.remove(slot);
            } else {
                items.put(slot, item);
            }
        }

        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        public void setCustomData(String key, Object value) {
            if (value == null) {
                customData.remove(key);
            } else if (value instanceof Serializable) {
                customData.put(key, value);
            }
        }

        public Object getCustomData(String key) {
            return customData.get(key);
        }
    }

    /**
     * Creates a persistent GUI wrapper
     *
     * @param gui The GUI to make persistent
     */
    public PersistentGui(AuroraGui gui) {
        this.gui = gui;
        this.playerStates = new ConcurrentHashMap<>();
        this.enabled = false;
        this.mode = PersistenceMode.BOTH;
        this.saveEvents = EnumSet.of(PersistenceEvent.CLOSE);
    }

    /**
     * Sets the data folder for disk persistence
     *
     * @param folder The data folder
     * @return This persistent GUI for chaining
     */
    public PersistentGui setDataFolder(File folder) {
        this.dataFolder = folder;
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return this;
    }

    /**
     * Enables state persistence
     *
     * @param enabled true to enable
     * @return This persistent GUI for chaining
     */
    public PersistentGui setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the persistence mode
     *
     * @param mode The persistence mode
     * @return This persistent GUI for chaining
     */
    public PersistentGui setMode(PersistenceMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Configures when to auto-save
     *
     * @param events The events that trigger saving
     * @return This persistent GUI for chaining
     */
    public PersistentGui saveOn(PersistenceEvent... events) {
        saveEvents.clear();
        saveEvents.addAll(Arrays.asList(events));
        return this;
    }

    /**
     * Saves the current state for a player
     *
     * @param player The player
     * @return true if saved successfully
     */
    public boolean saveState(Player player) {
        if (!enabled) return false;

        GuiState state = captureState(player);

        // Save to memory
        if (mode == PersistenceMode.MEMORY || mode == PersistenceMode.BOTH) {
            playerStates.put(player.getUniqueId(), state);
        }

        // Save to disk
        if ((mode == PersistenceMode.DISK || mode == PersistenceMode.BOTH) && dataFolder != null) {
            return saveToDisk(player.getUniqueId(), state);
        }

        return true;
    }

    /**
     * Loads the saved state for a player
     *
     * @param player The player
     * @return true if state was loaded successfully
     */
    public boolean loadState(Player player) {
        if (!enabled) return false;

        GuiState state = null;

        // Try memory first
        if (mode == PersistenceMode.MEMORY || mode == PersistenceMode.BOTH) {
            state = playerStates.get(player.getUniqueId());
        }

        // Try disk if not in memory
        if (state == null && (mode == PersistenceMode.DISK || mode == PersistenceMode.BOTH) && dataFolder != null) {
            state = loadFromDisk(player.getUniqueId());
        }

        if (state != null) {
            applyState(player, state);
            return true;
        }

        return false;
    }

    /**
     * Clears the saved state for a player
     *
     * @param player The player
     */
    public void clearState(Player player) {
        playerStates.remove(player.getUniqueId());

        if (dataFolder != null) {
            File file = getPlayerFile(player.getUniqueId());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Checks if a player has a saved state
     *
     * @param player The player
     * @return true if state exists
     */
    public boolean hasState(Player player) {
        if (playerStates.containsKey(player.getUniqueId())) {
            return true;
        }

        if (dataFolder != null) {
            return getPlayerFile(player.getUniqueId()).exists();
        }

        return false;
    }

    /**
     * Gets the saved state for a player
     *
     * @param player The player
     * @return The state, or null if none exists
     */
    public GuiState getState(Player player) {
        GuiState state = playerStates.get(player.getUniqueId());

        if (state == null && dataFolder != null) {
            state = loadFromDisk(player.getUniqueId());
        }

        return state;
    }

    /**
     * Sets custom data in the player's state
     *
     * @param player The player
     * @param key The data key
     * @param value The value (must be Serializable)
     */
    public void setCustomData(Player player, String key, Object value) {
        GuiState state = playerStates.computeIfAbsent(player.getUniqueId(), k -> new GuiState());
        state.setCustomData(key, value);
    }

    /**
     * Gets custom data from the player's state
     *
     * @param player The player
     * @param key The data key
     * @return The value, or null if not found
     */
    public Object getCustomData(Player player, String key) {
        GuiState state = playerStates.get(player.getUniqueId());
        return state != null ? state.getCustomData(key) : null;
    }

    /**
     * Handles auto-save on event
     *
     * @param player The player
     * @param event The event type
     */
    public void handleEvent(Player player, PersistenceEvent event) {
        if (enabled && saveEvents.contains(event)) {
            saveState(player);
        }
    }

    /**
     * Captures the current GUI state
     */
    private GuiState captureState(Player player) {
        Map<Integer, ItemStack> items = new HashMap<>();

        // Capture inventory items
        if (gui.getInventory() != null) {
            for (int i = 0; i < gui.getSize(); i++) {
                ItemStack item = gui.getInventory().getItem(i);
                if (item != null) {
                    items.put(i, item.clone());
                }
            }
        }

        // Get existing custom data
        Map<String, Object> customData = new HashMap<>();
        GuiState existingState = playerStates.get(player.getUniqueId());
        if (existingState != null) {
            customData.putAll(existingState.getCustomData());
        }

        return new GuiState(items, gui.getCurrentPage(), customData);
    }

    /**
     * Applies a saved state to the GUI
     */
    private void applyState(Player player, GuiState state) {
        // Restore items
        for (Map.Entry<Integer, ItemStack> entry : state.getItems().entrySet()) {
            gui.setItem(entry.getKey(), entry.getValue().clone());
        }

        // Restore page
        if (state.getCurrentPage() != gui.getCurrentPage()) {
            gui.setPage(state.getCurrentPage());
        }

        // Store state in memory for custom data access
        playerStates.put(player.getUniqueId(), state);
    }

    /**
     * Saves state to disk
     */
    private boolean saveToDisk(UUID playerId, GuiState state) {
        File file = getPlayerFile(playerId);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(state);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads state from disk
     */
    private GuiState loadFromDisk(UUID playerId) {
        File file = getPlayerFile(playerId);

        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GuiState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the file for a player's state
     */
    private File getPlayerFile(UUID playerId) {
        return new File(dataFolder, gui.getName() + "_" + playerId.toString() + ".dat");
    }

    /**
     * Gets the wrapped GUI
     *
     * @return The GUI
     */
    public AuroraGui getGui() {
        return gui;
    }

    /**
     * Checks if persistence is enabled
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the persistence mode
     *
     * @return The mode
     */
    public PersistenceMode getMode() {
        return mode;
    }
}
