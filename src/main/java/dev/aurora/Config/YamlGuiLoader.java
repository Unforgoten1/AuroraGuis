package dev.aurora.Config;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Utilities.Items.ItemBuilder;
import dev.aurora.Utilities.Strings.ColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Loads GUI configurations from YAML files
 * Supports hot-reloading and action registry for dynamic behavior
 */
public class YamlGuiLoader {
    private final GuiManager manager;
    private final Map<String, Consumer<InventoryClickEvent>> actionRegistry;
    private final Map<String, AuroraGui> loadedGuis;
    private final ConfigCache configCache;

    /**
     * Creates a new YAML GUI loader
     *
     * @param manager The GuiManager to register GUIs with
     */
    public YamlGuiLoader(GuiManager manager) {
        this.manager = manager;
        this.actionRegistry = new ConcurrentHashMap<>();
        this.loadedGuis = new ConcurrentHashMap<>();
        this.configCache = new ConfigCache();

        registerDefaultActions();
    }

    /**
     * Registers default actions (close, command, etc.)
     */
    private void registerDefaultActions() {
        registerAction("close", event -> {
            if (event.getWhoClicked() instanceof Player) {
                ((Player) event.getWhoClicked()).closeInventory();
            }
        });

        registerAction("none", event -> {
            // Do nothing
        });
    }

    /**
     * Registers a custom action by ID
     *
     * @param actionId The action identifier
     * @param action The action to execute
     */
    public void registerAction(String actionId, Consumer<InventoryClickEvent> action) {
        actionRegistry.put(actionId.toLowerCase(), action);
    }

    /**
     * Loads a GUI from a YAML file
     *
     * @param file The YAML file
     * @return The loaded GUI
     * @throws IOException if file cannot be read or is invalid
     */
    public AuroraGui loadFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getPath());
        }

        YamlConfiguration config = configCache.get(file);
        return loadFromConfig(config);
    }

    /**
     * Loads a GUI from a YamlConfiguration
     *
     * @param config The configuration
     * @return The loaded GUI
     * @throws IllegalArgumentException if configuration is invalid
     */
    public AuroraGui loadFromConfig(YamlConfiguration config) {
        ConfigurationSection guiSection = config.getConfigurationSection("gui");
        if (guiSection == null) {
            throw new IllegalArgumentException("Missing 'gui' section in configuration");
        }

        String name = guiSection.getString("name", "yaml-gui-" + System.currentTimeMillis());
        String title = guiSection.getString("title", "GUI");
        int rows = guiSection.getInt("rows", 3);

        // Apply color codes to title
        title = ColorUtils.color(title);

        // Create GUI
        AuroraGui gui = new AuroraGui(name)
                .title(title)
                .rows(rows);

        // Load items
        if (guiSection.contains("items")) {
            List<Map<?, ?>> items = guiSection.getMapList("items");
            for (Map<?, ?> itemMap : items) {
                loadItem(gui, itemMap);
            }
        }

        // Load border
        if (guiSection.contains("border")) {
            ConfigurationSection borderSection = guiSection.getConfigurationSection("border");
            loadBorder(gui, borderSection);
        }

        // Register and cache
        gui.register(manager);
        loadedGuis.put(name, gui);

        return gui;
    }

    /**
     * Loads an item from configuration and adds it to the GUI
     */
    private void loadItem(AuroraGui gui, Map<?, ?> itemMap) {
        int slot = getInt(itemMap, "slot", -1);
        if (slot < 0 || slot >= gui.getSize()) {
            return;
        }

        String materialName = getString(itemMap, "material", "STONE");
        Material material = parseMaterial(materialName);

        if (material == null) {
            return;
        }

        ItemBuilder builder = new ItemBuilder(material);

        // Amount
        int amount = getInt(itemMap, "amount", 1);
        builder.amount(amount);

        // Display name
        if (itemMap.containsKey("name")) {
            String name = getString(itemMap, "name", "");
            builder.name(ColorUtils.color(name));
        }

        // Lore
        if (itemMap.containsKey("lore")) {
            Object loreObj = itemMap.get("lore");
            if (loreObj instanceof List) {
                List<String> lore = new ArrayList<>();
                for (Object line : (List<?>) loreObj) {
                    lore.add(ColorUtils.color(String.valueOf(line)));
                }
                builder.lore(lore);
            }
        }

        // Enchantments
        if (itemMap.containsKey("enchantments")) {
            Object enchObj = itemMap.get("enchantments");
            if (enchObj instanceof Map) {
                Map<?, ?> enchMap = (Map<?, ?>) enchObj;
                for (Map.Entry<?, ?> entry : enchMap.entrySet()) {
                    Enchantment ench = parseEnchantment(String.valueOf(entry.getKey()));
                    if (ench != null) {
                        int level = Integer.parseInt(String.valueOf(entry.getValue()));
                        builder.enchant(ench, level);
                    }
                }
            }
        }

        // Glow effect
        if (getBoolean(itemMap, "glow", false)) {
            builder.glow();
        }

        // Custom model data (enhanced schema support)
        if (itemMap.containsKey("custom-model-data")) {
            int cmd = getInt(itemMap, "custom-model-data", 0);
            builder.customModelData(cmd);
        }

        // Custom model (from ModelRegistry)
        if (itemMap.containsKey("custom-model")) {
            String modelId = getString(itemMap, "custom-model", "");
            builder.customModel(modelId);
        }

        ItemStack item = builder.build();

        // Parse actions (single action or list)
        Consumer<InventoryClickEvent> action = null;
        if (itemMap.containsKey("actions")) {
            // New enhanced schema - list of actions
            Object actionsValue = itemMap.get("actions");
            List<Consumer<InventoryClickEvent>> actions = EnhancedYamlParser.parseActions(actionsValue, null);
            if (!actions.isEmpty()) {
                // Combine multiple actions into one consumer
                action = event -> {
                    for (Consumer<InventoryClickEvent> a : actions) {
                        a.accept(event);
                    }
                };
            }
        } else if (itemMap.containsKey("action")) {
            // Legacy single action support
            String actionStr = getString(itemMap, "action", "none");
            action = parseAction(actionStr);
        }

        // Parse animation (enhanced schema)
        if (itemMap.containsKey("animation")) {
            Object animValue = itemMap.get("animation");
            if (animValue instanceof Map) {
                // Convert Map to ConfigurationSection
                // This requires YamlConfiguration wrapper
                // For now, skip complex animation loading from Map
                // Will be supported when ConfigurationSection is available
            }
        }

        // Parse requirements/conditions (enhanced schema)
        if (itemMap.containsKey("requirements")) {
            Object reqValue = itemMap.get("requirements");
            if (reqValue instanceof Map) {
                // Will be handled when full ConfigurationSection support is added
            }
        }

        // Parse cooldown (enhanced schema)
        if (itemMap.containsKey("cooldown")) {
            Object cooldownValue = itemMap.get("cooldown");
            // Cooldowns will be applied via ClickCondition or separately
            // Implementation depends on how cooldowns are stored per-slot
        }

        // Add to GUI
        if (action != null) {
            gui.setItem(slot, item, action);
        } else {
            gui.setItem(slot, item);
        }
    }

    /**
     * Loads a border from configuration
     */
    private void loadBorder(AuroraGui gui, ConfigurationSection borderSection) {
        if (borderSection == null) return;

        String materialName = borderSection.getString("material", "GRAY_STAINED_GLASS_PANE");
        Material material = parseMaterial(materialName);

        if (material == null) {
            material = Material.STONE;
        }

        ItemBuilder builder = new ItemBuilder(material);

        if (borderSection.contains("name")) {
            builder.name(ColorUtils.color(borderSection.getString("name")));
        }

        ItemStack borderItem = builder.build();
        gui.border(borderItem);
    }

    /**
     * Parses an action string (e.g., "command:/give {player} diamond", "open_gui:shop", "close")
     */
    private Consumer<InventoryClickEvent> parseAction(String actionStr) {
        if (actionStr == null || actionStr.isEmpty()) {
            return null;
        }

        actionStr = actionStr.trim();

        // Check for registered custom action
        if (actionRegistry.containsKey(actionStr.toLowerCase())) {
            return actionRegistry.get(actionStr.toLowerCase());
        }

        // Parse action type:data format
        String[] parts = actionStr.split(":", 2);
        String actionType = parts[0].toLowerCase();
        String actionData = parts.length > 1 ? parts[1] : "";

        switch (actionType) {
            case "close":
                return actionRegistry.get("close");

            case "command":
                return event -> {
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        String command = actionData.replace("{player}", player.getName());
                        player.performCommand(command);
                    }
                };

            case "console_command":
                return event -> {
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        String command = actionData.replace("{player}", player.getName());
                        manager.getPlugin().getServer().dispatchCommand(
                            manager.getPlugin().getServer().getConsoleSender(),
                            command
                        );
                    }
                };

            case "open_gui":
                return event -> {
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        AuroraGui targetGui = loadedGuis.get(actionData);
                        if (targetGui != null) {
                            targetGui.open(player);
                        }
                    }
                };

            case "message":
                return event -> {
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        player.sendMessage(ColorUtils.color(actionData));
                    }
                };

            default:
                // Try custom action by ID
                return actionRegistry.get(actionStr.toLowerCase());
        }
    }

    /**
     * Parses a material name (supports XMaterial-style names)
     */
    private Material parseMaterial(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        try {
            return Material.valueOf(name.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Try legacy conversion (basic)
            String converted = convertLegacyMaterial(name);
            try {
                return Material.valueOf(converted);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    /**
     * Basic legacy material name conversion
     */
    private String convertLegacyMaterial(String legacy) {
        // Common conversions
        Map<String, String> conversions = new HashMap<>();
        conversions.put("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE");
        conversions.put("WOOL", "WHITE_WOOL");
        conversions.put("INK_SACK", "INK_SAC");
        conversions.put("SKULL_ITEM", "PLAYER_HEAD");

        return conversions.getOrDefault(legacy.toUpperCase(), legacy.toUpperCase());
    }

    /**
     * Parses an enchantment name
     */
    private Enchantment parseEnchantment(String name) {
        try {
            return Enchantment.getByName(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Hot-reloads a GUI from file
     *
     * @param file The file to reload from
     * @return The reloaded GUI
     * @throws IOException if reload fails
     */
    public AuroraGui reload(File file) throws IOException {
        // Invalidate cache to force reload
        configCache.invalidate(file);
        AuroraGui gui = loadFromFile(file);
        return gui;
    }

    /**
     * Gets the configuration cache
     *
     * @return The config cache
     */
    public ConfigCache getConfigCache() {
        return configCache;
    }

    /**
     * Gets a loaded GUI by name
     *
     * @param name The GUI name
     * @return The GUI, or null if not found
     */
    public AuroraGui getLoadedGui(String name) {
        return loadedGuis.get(name);
    }

    /**
     * Gets all loaded GUIs
     */
    public Collection<AuroraGui> getLoadedGuis() {
        return new ArrayList<>(loadedGuis.values());
    }

    // Helper methods for map parsing
    private String getString(Map<?, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    private int getInt(Map<?, ?> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean getBoolean(Map<?, ?> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
