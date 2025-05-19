package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.BorderType;
import dev.aurora.Utilities.Items.ItemBuilder;
import dev.aurora.Utilities.Sound.SoundEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating selection/choice GUIs
 * Allows players to select from multiple options
 */
public class SelectorGui<T> {
    private String title = "&6Select an Option";
    private final List<Option<T>> options;
    private BiConsumer<Player, T> onSelect;
    private boolean closeOnSelect = true;
    private SoundEffect selectSound = SoundEffect.CLICK;
    private ItemStack borderItem;
    private int rows = 3;
    private final GuiManager manager;

    public static class Option<T> {
        private final ItemStack displayItem;
        private final T value;

        public Option(ItemStack displayItem, T value) {
            this.displayItem = displayItem;
            this.value = value;
        }

        public ItemStack getDisplayItem() {
            return displayItem;
        }

        public T getValue() {
            return value;
        }
    }

    public SelectorGui(GuiManager manager) {
        this.manager = manager;
        this.options = new ArrayList<>();
        this.borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .durability(7)
            .build();
    }

    /**
     * Set the title
     */
    public SelectorGui<T> title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Set number of rows
     */
    public SelectorGui<T> rows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be 1-6");
        }
        this.rows = rows;
        return this;
    }

    /**
     * Add an option to select
     */
    public SelectorGui<T> addOption(ItemStack displayItem, T value) {
        options.add(new Option<>(displayItem, value));
        return this;
    }

    /**
     * Add multiple options
     */
    public SelectorGui<T> addOptions(List<Option<T>> options) {
        this.options.addAll(options);
        return this;
    }

    /**
     * Set the action when an option is selected
     */
    public SelectorGui<T> onSelect(BiConsumer<Player, T> action) {
        this.onSelect = action;
        return this;
    }

    /**
     * Set whether to close on selection
     */
    public SelectorGui<T> closeOnSelect(boolean close) {
        this.closeOnSelect = close;
        return this;
    }

    /**
     * Set the selection sound
     */
    public SelectorGui<T> selectSound(SoundEffect sound) {
        this.selectSound = sound;
        return this;
    }

    /**
     * Set the border item
     */
    public SelectorGui<T> borderItem(ItemStack item) {
        this.borderItem = item;
        return this;
    }

    /**
     * Build the selector GUI
     */
    public AuroraGui build() {
        String guiName = "selector-" + System.currentTimeMillis();
        AuroraGui gui = new AuroraGui(guiName)
            .title(title)
            .rows(rows);

        if (borderItem != null) {
            gui.setBorder(BorderType.FULL, borderItem);
        }

        // Add options to GUI
        int slot = 10; // Start from slot 10 (skip border)
        for (Option<T> option : options) {
            if (slot >= rows * 9 - 9) break; // Don't overflow last row

            // Skip border slots
            if (slot % 9 == 0) slot++; // Left border
            if (slot % 9 == 8) slot += 2; // Right border

            gui.addItem(slot, option.getDisplayItem(), createSelectAction(option));
            slot++;
        }

        gui.register(manager);
        return gui;
    }

    /**
     * Build and open for a player
     */
    public void open(Player player) {
        build().open(player);
    }

    private Consumer<InventoryClickEvent> createSelectAction(Option<T> option) {
        return event -> {
            Player player = (Player) event.getWhoClicked();

            // Play sound
            if (selectSound != null) {
                selectSound.play(player);
            }

            // Execute callback
            if (onSelect != null) {
                onSelect.accept(player, option.getValue());
            }

            // Close if configured
            if (closeOnSelect) {
                player.closeInventory();
            }
        };
    }

    /**
     * Create a simple selector with string options
     */
    public static SelectorGui<String> simple(GuiManager manager, String title, List<String> options) {
        SelectorGui<String> selector = new SelectorGui<String>(manager).title(title);

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            ItemStack item = new ItemBuilder(Material.PAPER)
                .name("&e" + option)
                .lore("&7Click to select")
                .build();
            selector.addOption(item, option);
        }

        return selector;
    }
}
