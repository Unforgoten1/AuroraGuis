package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.Action.GuiActions;
import dev.aurora.Struct.BorderType;
import dev.aurora.Utilities.Items.ItemBuilder;
import dev.aurora.Utilities.Sound.SoundEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Builder for creating confirmation dialogs
 * Provides easy yes/no prompts with customizable actions
 */
public class ConfirmationGui {
    private String title = "&cConfirm Action";
    private ItemStack confirmItem;
    private ItemStack cancelItem;
    private ItemStack displayItem;
    private Consumer<InventoryClickEvent> onConfirm;
    private Consumer<InventoryClickEvent> onCancel;
    private boolean closeOnConfirm = true;
    private boolean closeOnCancel = true;
    private SoundEffect confirmSound = SoundEffect.SUCCESS;
    private SoundEffect cancelSound = SoundEffect.CLICK;
    private GuiManager manager;

    public ConfirmationGui(GuiManager manager) {
        this.manager = manager;

        // Default confirm button (green)
        this.confirmItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&a&lCONFIRM")
            .lore("&7Click to confirm")
            .durability(5)
            .build();

        // Default cancel button (red)
        this.cancelItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&c&lCANCEL")
            .lore("&7Click to cancel")
            .durability(14)
            .build();
    }

    /**
     * Set the title of the confirmation GUI
     */
    public ConfirmationGui title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Set the item to display in the center (what's being confirmed)
     */
    public ConfirmationGui displayItem(ItemStack item) {
        this.displayItem = item;
        return this;
    }

    /**
     * Set the confirm button item
     */
    public ConfirmationGui confirmItem(ItemStack item) {
        this.confirmItem = item;
        return this;
    }

    /**
     * Set the cancel button item
     */
    public ConfirmationGui cancelItem(ItemStack item) {
        this.cancelItem = item;
        return this;
    }

    /**
     * Set the action to run when confirmed
     */
    public ConfirmationGui onConfirm(Consumer<InventoryClickEvent> action) {
        this.onConfirm = action;
        return this;
    }

    /**
     * Set the action to run when cancelled
     */
    public ConfirmationGui onCancel(Consumer<InventoryClickEvent> action) {
        this.onCancel = action;
        return this;
    }

    /**
     * Set whether to close GUI on confirm (default: true)
     */
    public ConfirmationGui closeOnConfirm(boolean close) {
        this.closeOnConfirm = close;
        return this;
    }

    /**
     * Set whether to close GUI on cancel (default: true)
     */
    public ConfirmationGui closeOnCancel(boolean close) {
        this.closeOnCancel = close;
        return this;
    }

    /**
     * Set the sound to play on confirm
     */
    public ConfirmationGui confirmSound(SoundEffect sound) {
        this.confirmSound = sound;
        return this;
    }

    /**
     * Set the sound to play on cancel
     */
    public ConfirmationGui cancelSound(SoundEffect sound) {
        this.cancelSound = sound;
        return this;
    }

    /**
     * Build and register the confirmation GUI
     */
    public AuroraGui build() {
        String guiName = "confirm-" + System.currentTimeMillis();
        AuroraGui gui = new AuroraGui(guiName)
            .title(title)
            .rows(3)
            .setBorder(BorderType.FULL, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name("&7")
                .durability(7)
                .build());

        // Add display item in center if provided
        if (displayItem != null) {
            gui.addItem(13, displayItem, null);
        }

        // Add confirm buttons (left side)
        for (int i = 10; i <= 12; i++) {
            gui.addItem(i, confirmItem, createConfirmAction(gui));
        }

        // Add cancel buttons (right side)
        for (int i = 14; i <= 16; i++) {
            gui.addItem(i, cancelItem, createCancelAction(gui));
        }

        gui.register(manager);
        return gui;
    }

    /**
     * Build and open for a specific player
     */
    public void open(Player player) {
        build().open(player);
    }

    private Consumer<InventoryClickEvent> createConfirmAction(AuroraGui gui) {
        return event -> {
            Player player = (Player) event.getWhoClicked();

            // Play sound
            if (confirmSound != null) {
                confirmSound.play(player);
            }

            // Execute custom action
            if (onConfirm != null) {
                onConfirm.accept(event);
            }

            // Close if configured
            if (closeOnConfirm) {
                player.closeInventory();
            }
        };
    }

    private Consumer<InventoryClickEvent> createCancelAction(AuroraGui gui) {
        return event -> {
            Player player = (Player) event.getWhoClicked();

            // Play sound
            if (cancelSound != null) {
                cancelSound.play(player);
            }

            // Execute custom action
            if (onCancel != null) {
                onCancel.accept(event);
            }

            // Close if configured
            if (closeOnCancel) {
                player.closeInventory();
            }
        };
    }

    /**
     * Quick static method to create a simple yes/no confirmation
     */
    public static void confirm(GuiManager manager, Player player, String title, Runnable onConfirm) {
        new ConfirmationGui(manager)
            .title(title)
            .onConfirm(event -> onConfirm.run())
            .open(player);
    }

    /**
     * Create a confirmation with custom display item
     */
    public static void confirm(GuiManager manager, Player player, String title, ItemStack displayItem, Runnable onConfirm) {
        new ConfirmationGui(manager)
            .title(title)
            .displayItem(displayItem)
            .onConfirm(event -> onConfirm.run())
            .open(player);
    }
}
