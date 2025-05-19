package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Struct.Theme.GuiTheme;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * GUI that applies a consistent visual theme
 * Provides themed item creation and consistent styling
 */
public class ThemedGui extends AuroraGui {
    private final GuiTheme theme;

    /**
     * Creates a new themed GUI
     *
     * @param name The GUI name
     * @param theme The theme to apply
     */
    public ThemedGui(String name, GuiTheme theme) {
        super(name);
        this.theme = theme;
    }

    /**
     * Creates a themed GUI with DARK theme
     *
     * @param name The GUI name
     */
    public ThemedGui(String name) {
        this(name, GuiTheme.DARK);
    }

    /**
     * Gets the theme
     *
     * @return The GUI theme
     */
    public GuiTheme getTheme() {
        return theme;
    }

    /**
     * Sets title with primary theme color
     *
     * @param title The title
     * @return This GUI for chaining
     */
    @Override
    public ThemedGui title(String title) {
        super.title(theme.colorPrimary(title));
        return this;
    }

    /**
     * Fills the GUI with themed background
     *
     * @return This GUI for chaining
     */
    public ThemedGui fillBackground() {
        fillBorder(theme.createBackground());
        return this;
    }

    /**
     * Sets a themed background border
     *
     * @return This GUI for chaining
     */
    public ThemedGui themedBorder() {
        border(theme.createBackground());
        return this;
    }

    /**
     * Adds a success button (green)
     *
     * @param slot The slot number
     * @param name The button name
     * @param action The click action
     * @return This GUI for chaining
     */
    public ThemedGui addSuccessButton(int slot, String name, Consumer<InventoryClickEvent> action) {
        setItem(slot, theme.createSuccess(name), action);
        return this;
    }

    /**
     * Adds an error/cancel button (red)
     *
     * @param slot The slot number
     * @param name The button name
     * @param action The click action
     * @return This GUI for chaining
     */
    public ThemedGui addErrorButton(int slot, String name, Consumer<InventoryClickEvent> action) {
        setItem(slot, theme.createError(name), action);
        return this;
    }

    /**
     * Adds a warning button (orange/yellow)
     *
     * @param slot The slot number
     * @param name The button name
     * @param action The click action
     * @return This GUI for chaining
     */
    public ThemedGui addWarningButton(int slot, String name, Consumer<InventoryClickEvent> action) {
        setItem(slot, theme.createWarning(name), action);
        return this;
    }

    /**
     * Adds an info button (blue)
     *
     * @param slot The slot number
     * @param name The button name
     * @param action The click action
     * @return This GUI for chaining
     */
    public ThemedGui addInfoButton(int slot, String name, Consumer<InventoryClickEvent> action) {
        setItem(slot, theme.createInfo(name), action);
        return this;
    }

    /**
     * Adds an accent button (theme accent color)
     *
     * @param slot The slot number
     * @param name The button name
     * @param action The click action
     * @return This GUI for chaining
     */
    public ThemedGui addAccentButton(int slot, String name, Consumer<InventoryClickEvent> action) {
        setItem(slot, theme.createAccent(name), action);
        return this;
    }

    /**
     * Adds a themed item
     *
     * @param slot The slot number
     * @param item The item (name will be themed)
     * @param action The click action
     * @return This GUI for chaining
     */
    public ThemedGui addThemedItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        // Apply theme color to item if it has a display name
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            ItemStack themed = item.clone();
            themed.getItemMeta().setDisplayName(
                theme.colorText(themed.getItemMeta().getDisplayName())
            );
            setItem(slot, themed, action);
        } else {
            setItem(slot, item, action);
        }
        return this;
    }

    /**
     * Creates a confirmation dialog with themed buttons
     *
     * @param title The dialog title
     * @param confirmAction Action on confirm
     * @param cancelAction Action on cancel
     * @return ThemedGui configured as confirmation dialog
     */
    public static ThemedGui confirmation(GuiTheme theme, String title,
                                         Consumer<Player> confirmAction,
                                         Consumer<Player> cancelAction) {
        ThemedGui gui = new ThemedGui("confirmation", theme)
                .title(title)
                .rows(3);

        gui.fillBackground();

        // Confirm button (green)
        gui.addSuccessButton(11, "✓ Confirm", event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            if (confirmAction != null) {
                confirmAction.accept(player);
            }
        });

        // Cancel button (red)
        gui.addErrorButton(15, "✗ Cancel", event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            if (cancelAction != null) {
                cancelAction.accept(player);
            }
        });

        return gui;
    }

    /**
     * Creates a confirmation dialog with DARK theme
     *
     * @param title The dialog title
     * @param confirmAction Action on confirm
     * @param cancelAction Action on cancel
     * @return ThemedGui configured as confirmation dialog
     */
    public static ThemedGui confirmation(String title,
                                         Consumer<Player> confirmAction,
                                         Consumer<Player> cancelAction) {
        return confirmation(GuiTheme.DARK, title, confirmAction, cancelAction);
    }

    // Override methods to return ThemedGui instead of AuroraGui for chaining

    @Override
    public ThemedGui rows(int rows) {
        super.rows(rows);
        return this;
    }

    @Override
    public ThemedGui setItem(int slot, ItemStack item) {
        super.setItem(slot, item);
        return this;
    }

    @Override
    public ThemedGui setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        super.setItem(slot, item, action);
        return this;
    }

    @Override
    public ThemedGui border(ItemStack item) {
        super.border(item);
        return this;
    }

    @Override
    public ThemedGui fillBorder(ItemStack item) {
        super.fillBorder(item);
        return this;
    }

    @Override
    public ThemedGui setGlobalCooldown(long milliseconds) {
        super.setGlobalCooldown(milliseconds);
        return this;
    }

    @Override
    public ThemedGui setSlotCooldown(int slot, long milliseconds) {
        super.setSlotCooldown(slot, milliseconds);
        return this;
    }

    @Override
    public ThemedGui setCooldownsEnabled(boolean enabled) {
        super.setCooldownsEnabled(enabled);
        return this;
    }
}
