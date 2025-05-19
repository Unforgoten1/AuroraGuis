package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.BorderType;
import dev.aurora.Struct.MenuItem;
import dev.aurora.Utilities.Items.ItemBuilder;
import dev.aurora.Utilities.Sound.SoundEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for creating right-click context menus
 * Provides quick action menus that overlay the current GUI
 */
public class ContextMenu {
    private final GuiManager manager;
    private String title = "&8Actions";
    private final List<MenuItem> items;
    private AuroraGui parentGui;
    private SoundEffect clickSound = SoundEffect.CLICK;
    private boolean closeOnAction = true;

    /**
     * Creates a new ContextMenu
     *
     * @param manager The GUI manager
     */
    public ContextMenu(GuiManager manager) {
        this.manager = manager;
        this.items = new ArrayList<>();
    }

    /**
     * Sets the menu title
     *
     * @param title The title to display
     * @return This instance for chaining
     */
    public ContextMenu title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Adds a menu item
     *
     * @param icon The item icon
     * @param name The display name
     * @param action The action to execute
     * @return This instance for chaining
     */
    public ContextMenu addItem(ItemStack icon, String name, Consumer<Player> action) {
        items.add(MenuItem.create(icon, name, action));
        return this;
    }

    /**
     * Adds a conditional menu item (only shown if condition passes)
     *
     * @param icon The item icon
     * @param name The display name
     * @param action The action to execute
     * @param condition Condition that must be true
     * @return This instance for chaining
     */
    public ContextMenu addConditionalItem(ItemStack icon, String name, Consumer<Player> action,
                                         java.util.function.Predicate<Player> condition) {
        items.add(MenuItem.conditional(icon, name, action, condition));
        return this;
    }

    /**
     * Adds a separator (visual divider)
     *
     * @return This instance for chaining
     */
    public ContextMenu addSeparator() {
        ItemStack separator = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .durability(7)
            .build();
        items.add(MenuItem.separator(separator));
        return this;
    }

    /**
     * Sets the parent GUI to return to after closing
     *
     * @param parentGui The parent GUI
     * @return This instance for chaining
     */
    public ContextMenu setParent(AuroraGui parentGui) {
        this.parentGui = parentGui;
        return this;
    }

    /**
     * Sets the click sound
     *
     * @param sound The sound to play
     * @return This instance for chaining
     */
    public ContextMenu clickSound(SoundEffect sound) {
        this.clickSound = sound;
        return this;
    }

    /**
     * Sets whether to close menu on action
     *
     * @param close true to close on action
     * @return This instance for chaining
     */
    public ContextMenu closeOnAction(boolean close) {
        this.closeOnAction = close;
        return this;
    }

    /**
     * Builds and opens the context menu for a player
     *
     * @param player The player to open for
     */
    public void open(Player player) {
        open(player, -1);
    }

    /**
     * Opens the context menu for a player, tracking source slot
     *
     * @param player The player to open for
     * @param sourceSlot The slot that triggered this menu
     */
    public void open(Player player, int sourceSlot) {
        // Filter items based on conditions
        List<MenuItem> visibleItems = new ArrayList<>();
        for (MenuItem item : items) {
            if (item.shouldShow(player)) {
                visibleItems.add(item);
            }
        }

        // Calculate rows needed
        int rows = Math.min(6, Math.max(1, (int) Math.ceil(visibleItems.size() / 7.0)));

        // Build GUI
        String guiName = "context-" + System.currentTimeMillis();
        AuroraGui gui = new AuroraGui(guiName)
            .title(title)
            .rows(rows);

        // Add border
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .durability(15)
            .build();
        gui.setBorder(BorderType.FULL, borderItem);

        // Add items
        int slot = 10; // Start from slot 10 (skip border)
        for (MenuItem menuItem : visibleItems) {
            if (slot >= rows * 9 - 9) break; // Don't overflow

            // Skip border slots
            if (slot % 9 == 0) slot++; // Left border
            if (slot % 9 == 8) slot += 2; // Right border

            ItemStack icon = menuItem.getIcon();
            if (icon != null && menuItem.getName() != null && !menuItem.isSeparator()) {
                // Apply name to icon
                icon = new ItemBuilder(icon.clone())
                    .name(menuItem.getName())
                    .build();
            }

            gui.addItem(slot, icon, event -> {
                Player p = (Player) event.getWhoClicked();

                // Play sound
                if (clickSound != null) {
                    clickSound.play(p);
                }

                // Execute action
                menuItem.execute(p);

                // Close if configured
                if (closeOnAction) {
                    p.closeInventory();

                    // Return to parent GUI if set
                    if (parentGui != null) {
                        org.bukkit.Bukkit.getScheduler().runTaskLater(
                            manager.getPlugin(),
                            () -> parentGui.open(p),
                            1L
                        );
                    }
                }
            });

            slot++;
        }

        // Add back button if parent exists
        if (parentGui != null && rows > 1) {
            ItemStack backButton = new ItemBuilder(Material.ARROW)
                .name("&7← Back")
                .lore("&7Return to previous menu")
                .build();

            gui.addItem((rows - 1) * 9 + 4, backButton, event -> {
                Player p = (Player) event.getWhoClicked();
                if (clickSound != null) {
                    clickSound.play(p);
                }
                p.closeInventory();
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    manager.getPlugin(),
                    () -> parentGui.open(p),
                    1L
                );
            });
        }

        gui.register(manager);
        gui.open(player);
    }
}
