package dev.aurora.Manager;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages GUI navigation history for back/forward functionality
 * Tracks GUI navigation per player with a stack-based system
 */
public class GuiNavigator {
    private final Map<UUID, Deque<AuroraGui>> navigationHistory;
    private final GuiManager guiManager;
    private final int maxHistorySize;

    public GuiNavigator(GuiManager guiManager) {
        this(guiManager, 10);
    }

    public GuiNavigator(GuiManager guiManager, int maxHistorySize) {
        this.guiManager = guiManager;
        this.maxHistorySize = maxHistorySize;
        this.navigationHistory = new ConcurrentHashMap<>();
    }

    /**
     * Open a GUI and add current GUI to navigation history
     * @param player The player
     * @param from The current GUI (will be added to history)
     * @param to The GUI to open
     */
    public void navigate(Player player, AuroraGui from, AuroraGui to) {
        UUID uuid = player.getUniqueId();
        Deque<AuroraGui> history = navigationHistory.computeIfAbsent(uuid, k -> new ArrayDeque<>());

        // Add current GUI to history
        if (from != null) {
            history.push(from);

            // Limit history size
            if (history.size() > maxHistorySize) {
                history.removeLast();
            }
        }

        // Open new GUI
        to.open(player);
    }

    /**
     * Open a GUI and add to history
     * @param player The player
     * @param gui The GUI to open
     */
    public void navigate(Player player, AuroraGui gui) {
        AuroraGui current = guiManager.getActiveGui(player);
        navigate(player, current, gui);
    }

    /**
     * Go back to the previous GUI
     * @param player The player
     * @return true if went back, false if no history
     */
    public boolean goBack(Player player) {
        UUID uuid = player.getUniqueId();
        Deque<AuroraGui> history = navigationHistory.get(uuid);

        if (history == null || history.isEmpty()) {
            return false;
        }

        AuroraGui previous = history.pop();
        if (previous != null) {
            previous.open(player);
            return true;
        }

        return false;
    }

    /**
     * Check if player can go back
     * @param player The player
     * @return true if history exists
     */
    public boolean canGoBack(Player player) {
        UUID uuid = player.getUniqueId();
        Deque<AuroraGui> history = navigationHistory.get(uuid);
        return history != null && !history.isEmpty();
    }

    /**
     * Get the previous GUI without navigating
     * @param player The player
     * @return The previous GUI or null
     */
    public AuroraGui getPrevious(Player player) {
        UUID uuid = player.getUniqueId();
        Deque<AuroraGui> history = navigationHistory.get(uuid);
        return (history != null && !history.isEmpty()) ? history.peek() : null;
    }

    /**
     * Clear navigation history for a player
     * @param player The player
     */
    public void clearHistory(Player player) {
        navigationHistory.remove(player.getUniqueId());
    }

    /**
     * Get navigation history size for a player
     * @param player The player
     * @return History size
     */
    public int getHistorySize(Player player) {
        UUID uuid = player.getUniqueId();
        Deque<AuroraGui> history = navigationHistory.get(uuid);
        return history != null ? history.size() : 0;
    }

    /**
     * Clear all navigation history
     */
    public void clearAll() {
        navigationHistory.clear();
    }
}
