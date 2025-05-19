package dev.aurora.Struct.Listener;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Listener interface for GUI lifecycle events
 * All methods have default implementations for optional usage
 */
public interface GuiListener {

    /**
     * Called when a GUI is opened by a player
     * @param player The player opening the GUI
     * @param gui The GUI being opened
     */
    default void onOpen(Player player, AuroraGui gui) {}

    /**
     * Called when a GUI is closed by a player
     * @param player The player closing the GUI
     * @param gui The GUI being closed
     */
    default void onClose(Player player, AuroraGui gui) {}

    /**
     * Called before an item click is processed
     * Return false to cancel the click
     * @param event The click event
     * @param gui The GUI being clicked
     * @return true to allow click, false to cancel
     */
    default boolean onBeforeClick(InventoryClickEvent event, AuroraGui gui) {
        return true;
    }

    /**
     * Called after an item click is processed
     * @param event The click event
     * @param gui The GUI that was clicked
     */
    default void onAfterClick(InventoryClickEvent event, AuroraGui gui) {}

    /**
     * Called when a page changes in pagination
     * @param player The player viewing the GUI
     * @param gui The GUI whose page changed
     * @param oldPage The previous page number
     * @param newPage The new page number
     */
    default void onPageChange(Player player, AuroraGui gui, int oldPage, int newPage) {}

    /**
     * Called when an animation starts
     * @param gui The GUI containing the animation
     * @param slot The slot where animation started
     */
    default void onAnimationStart(AuroraGui gui, int slot) {}

    /**
     * Called when an animation completes
     * @param gui The GUI containing the animation
     * @param slot The slot where animation completed
     */
    default void onAnimationComplete(AuroraGui gui, int slot) {}
}
