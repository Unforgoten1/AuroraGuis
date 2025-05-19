package dev.aurora.GUI;

import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.Listener.GuiListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * Common interface for all GUI types in AuroraGuis
 * Implemented by both AuroraGui (Bukkit event-based) and PacketGui (packet-based)
 *
 * This interface defines the core contract that all GUI implementations must follow,
 * enabling polymorphic handling of different GUI types while maintaining a consistent API.
 */
public interface IGui {

    /**
     * Gets the unique name of this GUI
     * @return The GUI name
     */
    String getName();

    /**
     * Gets the display title of this GUI
     * @return The GUI title
     */
    String getTitle();

    /**
     * Gets the number of rows in this GUI
     * @return Number of rows (1-6)
     */
    int getRows();

    /**
     * Gets the total number of slots in this GUI
     * @return Total slots (rows * 9)
     */
    int getSize();

    /**
     * Gets the underlying Bukkit inventory
     * @return The inventory object
     */
    Inventory getInventory();

    /**
     * Gets the GUI manager this GUI is registered with
     * @return The GUI manager
     */
    GuiManager getManager();

    /**
     * Sets the GUI manager
     * @param manager The manager
     */
    void setManager(GuiManager manager);

    /**
     * Gets all players currently viewing this GUI
     * @return List of viewing players
     */
    List<Player> getViewers();

    /**
     * Opens this GUI for a player
     * @param player The player to open for
     */
    void open(Player player);

    /**
     * Handles a click event in this GUI
     * @param event The click event
     */
    void handleClick(InventoryClickEvent event);

    /**
     * Registers this GUI with a manager
     * @param manager The manager to register with
     * @return This GUI for chaining
     */
    IGui register(GuiManager manager);

    /**
     * Adds a listener to this GUI
     * @param listener The listener to add
     * @return This GUI for chaining
     */
    IGui addListener(GuiListener listener);

    /**
     * Removes a listener from this GUI
     * @param listener The listener to remove
     * @return This GUI for chaining
     */
    IGui removeListener(GuiListener listener);

    /**
     * Gets all registered listeners
     * @return List of listeners
     */
    List<GuiListener> getListeners();

    /**
     * Cleans up resources when GUI is closed
     * Should cancel tasks, clear caches, etc.
     */
    void cleanup();

    /**
     * Updates the GUI display for all viewers
     * @return This GUI for chaining
     */
    IGui update();
}
