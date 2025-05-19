package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.GUI.IGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Packet.API.PacketGuiConfig;
import dev.aurora.Packet.API.ValidationLevel;
import dev.aurora.Packet.Core.PacketGui;
import dev.aurora.Struct.Animation.Animation;
import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Struct.BorderType;
import dev.aurora.Struct.SlotGroup;
import dev.aurora.Struct.Theme.GuiTheme;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

/**
 * Fluent builder API for constructing GUIs programmatically
 * Provides a chainable interface for GUI creation
 */
public class GuiBuilder {
    private final GuiManager manager;
    private String name;
    private String title;
    private int rows = 3;
    private GuiTheme theme;
    private boolean autoRegister = true;
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();
    private final Map<Integer, IAnimation> animations = new HashMap<>();
    private final List<ItemStack> paginatedItems = new ArrayList<>();
    private Consumer<InventoryClickEvent> paginatedAction;
    private BorderType borderType;
    private ItemStack borderItem;
    private final List<SlotGroupBuilder> slotGroups = new ArrayList<>();
    private dev.aurora.Struct.Cooldown.ClickCooldown cooldown;
    private Long globalCooldown;
    private dev.aurora.Struct.Animation.TitleAnimation titleAnimation;
    private dev.aurora.Struct.Sort.GuiSorter sorter;
    private boolean autoSortEnabled;

    // Packet mode support
    private boolean usePacketGui = false;
    private ValidationLevel validationLevel = ValidationLevel.PACKET;
    private PacketGuiConfig packetConfig = null;

    // Resource pack support
    private dev.aurora.ResourcePack.TitleBuilder customTitleBuilder;
    private dev.aurora.ResourcePack.ResourcePackConfig resourcePackConfig;

    /**
     * Slot group builder for nested configuration
     */
    public static class SlotGroupBuilder {
        private final String name;
        private final List<Integer> slots = new ArrayList<>();
        private ItemStack fillItem;
        private Consumer<InventoryClickEvent> action;
        private long cooldown;
        private String permission;

        public SlotGroupBuilder(String name) {
            this.name = name;
        }

        public SlotGroupBuilder slots(int... slots) {
            for (int slot : slots) {
                this.slots.add(slot);
            }
            return this;
        }

        public SlotGroupBuilder slotRange(int start, int end) {
            for (int i = start; i <= end; i++) {
                this.slots.add(i);
            }
            return this;
        }

        public SlotGroupBuilder fill(ItemStack item) {
            this.fillItem = item;
            return this;
        }

        public SlotGroupBuilder action(Consumer<InventoryClickEvent> action) {
            this.action = action;
            return this;
        }

        public SlotGroupBuilder cooldown(long milliseconds) {
            this.cooldown = milliseconds;
            return this;
        }

        public SlotGroupBuilder permission(String permission) {
            this.permission = permission;
            return this;
        }

        SlotGroup build(AuroraGui gui) {
            int[] slotArray = slots.stream().mapToInt(i -> i).toArray();
            SlotGroup group = new SlotGroup(name, gui, slotArray);

            if (fillItem != null) {
                group.fill(fillItem, action);
            }
            if (cooldown > 0) {
                group.setCooldown(cooldown);
            }
            if (permission != null) {
                group.setPermission(permission);
            }

            return group;
        }
    }

    /**
     * Creates a new GUI builder
     *
     * @param manager The GUI manager
     */
    public GuiBuilder(GuiManager manager) {
        this.manager = manager;
        this.name = "gui-" + System.currentTimeMillis();
    }

    /**
     * Sets the GUI name
     *
     * @param name The name
     * @return This builder
     */
    public GuiBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the GUI title
     *
     * @param title The title
     * @return This builder
     */
    public GuiBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets a custom title using TitleBuilder for pixel-perfect alignment
     * <p>
     * This is a convenience method equivalent to calling titleBuilder.build() and
     * passing the result to title(). The TitleBuilder will be stored and can be
     * used later for dynamic title updates.
     * </p>
     *
     * @param titleBuilder The TitleBuilder instance
     * @return This builder
     * @since 1.1.0
     */
    public GuiBuilder customTitle(dev.aurora.ResourcePack.TitleBuilder titleBuilder) {
        if (titleBuilder != null) {
            this.customTitleBuilder = titleBuilder;
            this.title = titleBuilder.build();
        }
        return this;
    }

    /**
     * Associates a resource pack configuration with this GUI
     * <p>
     * This stores the resource pack config for reference but does not automatically
     * apply it. Use this when you want to track which resource pack should be used
     * with a particular GUI.
     * </p>
     *
     * @param config The ResourcePackConfig
     * @return This builder
     * @since 1.1.0
     */
    public GuiBuilder withResourcePack(dev.aurora.ResourcePack.ResourcePackConfig config) {
        this.resourcePackConfig = config;
        return this;
    }

    /**
     * Sets the number of rows
     *
     * @param rows The rows (1-6)
     * @return This builder
     */
    public GuiBuilder rows(int rows) {
        this.rows = Math.max(1, Math.min(6, rows));
        return this;
    }

    /**
     * Sets the GUI theme
     *
     * @param theme The theme
     * @return This builder
     */
    public GuiBuilder theme(GuiTheme theme) {
        this.theme = theme;
        return this;
    }

    /**
     * Sets whether to auto-register the GUI
     *
     * @param autoRegister true to auto-register
     * @return This builder
     */
    public GuiBuilder autoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
        return this;
    }

    /**
     * Adds an item at a slot
     *
     * @param slot The slot
     * @param item The item
     * @return This builder
     */
    public GuiBuilder item(int slot, ItemStack item) {
        items.put(slot, item);
        return this;
    }

    /**
     * Adds an item with click action
     *
     * @param slot The slot
     * @param item The item
     * @param action The click action
     * @return This builder
     */
    public GuiBuilder item(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        items.put(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
        return this;
    }

    /**
     * Adds multiple items
     *
     * @param items Map of slot to item
     * @return This builder
     */
    public GuiBuilder items(Map<Integer, ItemStack> items) {
        this.items.putAll(items);
        return this;
    }

    /**
     * Adds an animation at a slot
     *
     * @param slot The slot
     * @param animation The animation
     * @return This builder
     */
    public GuiBuilder animation(int slot, IAnimation animation) {
        animations.put(slot, animation);
        return this;
    }

    /**
     * Adds paginated items
     *
     * @param items The items
     * @return This builder
     */
    public GuiBuilder paginatedItems(List<ItemStack> items) {
        this.paginatedItems.addAll(items);
        return this;
    }

    /**
     * Adds paginated items
     *
     * @param items The items
     * @return This builder
     */
    public GuiBuilder paginatedItems(ItemStack... items) {
        this.paginatedItems.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * Sets the paginated item click action
     *
     * @param action The action
     * @return This builder
     */
    public GuiBuilder paginatedAction(Consumer<InventoryClickEvent> action) {
        this.paginatedAction = action;
        return this;
    }

    /**
     * Sets the border type
     *
     * @param type The border type
     * @return This builder
     */
    public GuiBuilder border(BorderType type) {
        this.borderType = type;
        return this;
    }

    /**
     * Sets a custom border item
     *
     * @param item The border item
     * @return This builder
     */
    public GuiBuilder border(ItemStack item) {
        this.borderItem = item;
        return this;
    }

    /**
     * Creates a slot group builder
     *
     * @param name The group name
     * @return Slot group builder
     */
    public SlotGroupBuilder slotGroup(String name) {
        SlotGroupBuilder builder = new SlotGroupBuilder(name);
        slotGroups.add(builder);
        return builder;
    }

    /**
     * Sets global click cooldown
     *
     * @param milliseconds The cooldown in milliseconds
     * @return This builder
     */
    public GuiBuilder cooldown(long milliseconds) {
        this.globalCooldown = milliseconds;
        return this;
    }

    /**
     * Sets animated title
     *
     * @param animation The title animation
     * @return This builder
     */
    public GuiBuilder animatedTitle(dev.aurora.Struct.Animation.TitleAnimation animation) {
        this.titleAnimation = animation;
        return this;
    }

    /**
     * Enables auto-sorting
     *
     * @param sorter The sorter
     * @return This builder
     */
    public GuiBuilder autoSort(dev.aurora.Struct.Sort.GuiSorter sorter) {
        this.sorter = sorter;
        this.autoSortEnabled = true;
        return this;
    }

    /**
     * Enables alphabetical sorting
     *
     * @return This builder
     */
    public GuiBuilder sortAlphabetically() {
        return autoSort(dev.aurora.Struct.Sort.GuiSorter.alphabetical());
    }

    /**
     * Enables quantity sorting
     *
     * @return This builder
     */
    public GuiBuilder sortByQuantity() {
        return autoSort(dev.aurora.Struct.Sort.GuiSorter.byQuantity());
    }

    /**
     * Enables rarity sorting
     *
     * @return This builder
     */
    public GuiBuilder sortByRarity() {
        return autoSort(dev.aurora.Struct.Sort.GuiSorter.byRaritySort());
    }

    /**
     * Fills empty slots with an item
     *
     * @param item The fill item
     * @return This builder
     */
    public GuiBuilder fillEmpty(ItemStack item) {
        int totalSlots = rows * 9;
        for (int i = 0; i < totalSlots; i++) {
            if (!items.containsKey(i)) {
                items.put(i, item);
            }
        }
        return this;
    }

    /**
     * Adds a close button at the specified slot
     *
     * @param slot The slot
     * @return This builder
     */
    public GuiBuilder closeButton(int slot) {
        ItemStack closeItem = new dev.aurora.Utilities.Items.ItemBuilder(Material.BARRIER)
                .name("&cClose")
                .build();
        return item(slot, closeItem, event -> event.getWhoClicked().closeInventory());
    }

    /**
     * Adds previous/next page buttons for pagination
     *
     * @param previousSlot The previous button slot
     * @param nextSlot The next button slot
     * @return This builder
     */
    public GuiBuilder paginationButtons(int previousSlot, int nextSlot) {
        ItemStack prevItem = new dev.aurora.Utilities.Items.ItemBuilder(Material.ARROW)
                .name("&7← Previous Page")
                .build();

        ItemStack nextItem = new dev.aurora.Utilities.Items.ItemBuilder(Material.ARROW)
                .name("&7Next Page →")
                .build();

        items.put(previousSlot, prevItem);
        items.put(nextSlot, nextItem);

        return this;
    }

    /**
     * Enables packet-based GUI mode with specified validation level
     * Packet GUIs provide advanced anti-dupe protection
     *
     * @param level The validation level (BASIC, PACKET, ADVANCED)
     * @return This builder
     */
    public GuiBuilder packetMode(ValidationLevel level) {
        this.usePacketGui = true;
        this.validationLevel = level;
        return this;
    }

    /**
     * Enables packet-based GUI mode with custom configuration
     *
     * @param config The packet GUI configuration
     * @return This builder
     */
    public GuiBuilder packetMode(PacketGuiConfig config) {
        this.usePacketGui = true;
        this.packetConfig = config;
        return this;
    }

    /**
     * Builds the GUI
     * Returns IGui which can be either AuroraGui or PacketGui based on configuration
     *
     * @return The constructed GUI
     */
    public IGui build() {
        return usePacketGui ? buildPacketGui() : buildAuroraGui();
    }

    /**
     * Builds a traditional AuroraGui (Bukkit event-based)
     *
     * @return The constructed AuroraGui
     */
    public AuroraGui buildAuroraGui() {
        // Create base GUI or themed GUI
        AuroraGui gui;

        if (theme != null) {
            gui = new ThemedGui(name, theme)
                    .title(title != null ? title : name)
                    .rows(rows);
        } else {
            gui = new AuroraGui(name)
                    .title(title != null ? title : name)
                    .rows(rows);
        }

        // Register if auto-register is enabled
        if (autoRegister) {
            gui.register(manager);
        }

        // Add items
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            Consumer<InventoryClickEvent> action = actions.get(entry.getKey());
            if (action != null) {
                gui.setItem(entry.getKey(), entry.getValue(), action);
            } else {
                gui.setItem(entry.getKey(), entry.getValue());
            }
        }

        // Add animations
        for (Map.Entry<Integer, IAnimation> entry : animations.entrySet()) {
            gui.playAnimation(entry.getKey(), entry.getValue());
        }

        // Add paginated items
        if (!paginatedItems.isEmpty()) {
            gui.addPaginatedItems(paginatedItems, paginatedAction);
        }

        // Apply border
        if (borderType != null) {
            gui.addBorder(borderType);
        } else if (borderItem != null) {
            gui.addCustomBorder(borderItem);
        }

        // Create slot groups
        for (SlotGroupBuilder builder : slotGroups) {
            builder.build(gui);
        }

        // Apply cooldown
        if (globalCooldown != null) {
            gui.setGlobalCooldown(globalCooldown);
        }

        // Apply animated title
        if (titleAnimation != null) {
            gui.setAnimatedTitle(titleAnimation);
        }

        // Apply auto-sort
        if (autoSortEnabled && sorter != null) {
            gui.enableAutoSort(sorter);
        }

        return gui;
    }

    /**
     * Builds a packet-based PacketGui with anti-dupe protection
     *
     * @return The constructed PacketGui
     */
    public PacketGui buildPacketGui() {
        // Create PacketGui with appropriate config
        PacketGui gui;
        if (packetConfig != null) {
            gui = new PacketGui(name, packetConfig);
        } else {
            gui = new PacketGui(name);
            gui.setValidationLevel(validationLevel);
        }

        // Set basic properties
        gui.title(title != null ? title : name)
                .rows(rows);

        // Register if auto-register is enabled
        if (autoRegister && manager.isPacketSupportEnabled()) {
            gui.register(manager);
        } else if (autoRegister && !manager.isPacketSupportEnabled()) {
            throw new IllegalStateException("Packet support must be enabled in GuiManager before creating PacketGuis. Call manager.enablePacketSupport() first.");
        }

        // Add items
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            Consumer<InventoryClickEvent> action = actions.get(entry.getKey());
            if (action != null) {
                gui.addItem(entry.getKey(), entry.getValue(), action);
            } else {
                gui.setItem(entry.getKey(), entry.getValue());
            }
        }

        // Add animations
        for (Map.Entry<Integer, IAnimation> entry : animations.entrySet()) {
            gui.addCustomAnimation(entry.getKey(), entry.getValue());
        }

        // Apply border
        if (borderType != null) {
            gui.setBorder(borderType, null);
        } else if (borderItem != null) {
            gui.setBorder(BorderType.FULL, borderItem);
        }

        // Apply cooldown
        if (globalCooldown != null) {
            gui.setGlobalCooldown(globalCooldown);
        }

        return gui;
    }

    /**
     * Builds and opens the GUI for a player
     *
     * @param player The player
     * @return The constructed GUI
     */
    public IGui buildAndOpen(Player player) {
        IGui gui = build();
        gui.open(player);
        return gui;
    }

    // ============= Static Factory Methods =============

    /**
     * Creates a shop GUI builder
     *
     * @param manager The GUI manager
     * @param title The shop title
     * @return GUI builder configured for a shop
     */
    public static GuiBuilder shop(GuiManager manager, String title) {
        return new GuiBuilder(manager)
                .name("shop-" + System.currentTimeMillis())
                .title(title)
                .rows(6)
                .theme(GuiTheme.GOLD)
                .border(BorderType.FULL)
                .closeButton(49);
    }

    /**
     * Creates a confirmation dialog builder
     *
     * @param manager The GUI manager
     * @param title The dialog title
     * @return GUI builder configured for confirmation
     */
    public static GuiBuilder confirmation(GuiManager manager, String title) {
        return new GuiBuilder(manager)
                .name("confirm-" + System.currentTimeMillis())
                .title(title)
                .rows(3)
                .theme(GuiTheme.DARK);
    }

    /**
     * Creates a selector GUI builder
     *
     * @param manager The GUI manager
     * @param title The selector title
     * @return GUI builder configured for selection
     */
    public static GuiBuilder selector(GuiManager manager, String title) {
        return new GuiBuilder(manager)
                .name("selector-" + System.currentTimeMillis())
                .title(title)
                .rows(5)
                .theme(GuiTheme.OCEAN)
                .border(BorderType.CORNERS);
    }

    /**
     * Creates an inventory GUI builder
     *
     * @param manager The GUI manager
     * @param title The inventory title
     * @return GUI builder configured for inventory
     */
    public static GuiBuilder inventory(GuiManager manager, String title) {
        return new GuiBuilder(manager)
                .name("inventory-" + System.currentTimeMillis())
                .title(title)
                .rows(6)
                .sortByRarity()
                .paginationButtons(45, 53);
    }
}
