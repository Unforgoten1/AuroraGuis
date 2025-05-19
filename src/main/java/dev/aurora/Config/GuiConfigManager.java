package dev.aurora.Config;

import dev.aurora.GUI.IGui;
import dev.aurora.Manager.GuiManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central manager for config-based GUIs.
 * <p>
 * Scans the guis/ folder, loads YAML configurations, builds GUIs,
 * and automatically registers commands.
 * </p>
 *
 * @since 1.1.0
 */
public class GuiConfigManager {

    private final JavaPlugin plugin;
    private final GuiManager guiManager;
    private final YamlGuiLoader loader;
    private final File guisFolder;
    private final Map<String, LoadedGuiConfig> loadedConfigs;
    private final CommandRegistry commandRegistry;
    private Logger logger;

    /**
     * Creates a new GuiConfigManager.
     *
     * @param plugin The plugin instance
     * @param guiManager The GUI manager
     */
    public GuiConfigManager(JavaPlugin plugin, GuiManager guiManager) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (guiManager == null) {
            throw new IllegalArgumentException("GuiManager cannot be null");
        }

        this.plugin = plugin;
        this.guiManager = guiManager;
        this.loader = new YamlGuiLoader(guiManager);
        this.loadedConfigs = new HashMap<>();
        this.commandRegistry = new CommandRegistry(plugin, guiManager);
        this.logger = plugin.getLogger();

        // Create guis folder
        this.guisFolder = new File(plugin.getDataFolder(), "guis");
        if (!guisFolder.exists()) {
            guisFolder.mkdirs();
            logger.info("Created guis folder: " + guisFolder.getPath());
        }
    }

    /**
     * Gets the YAML GUI loader.
     *
     * @return The loader
     */
    public YamlGuiLoader getLoader() {
        return loader;
    }

    /**
     * Gets the command registry.
     *
     * @return The command registry
     */
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    /**
     * Gets the guis folder.
     *
     * @return The folder
     */
    public File getGuisFolder() {
        return guisFolder;
    }

    /**
     * Loads all GUIs from the guis/ folder.
     *
     * @return Number of GUIs loaded
     */
    public int loadAllGuis() {
        if (!guisFolder.exists() || !guisFolder.isDirectory()) {
            logger.warning("GUIs folder does not exist: " + guisFolder.getPath());
            return 0;
        }

        File[] files = guisFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));

        if (files == null || files.length == 0) {
            logger.info("No GUI configuration files found in " + guisFolder.getName());
            return 0;
        }

        int loaded = 0;
        logger.info("Loading " + files.length + " GUI configuration file(s)...");

        for (File file : files) {
            try {
                if (loadGui(file) != null) {
                    loaded++;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading GUI from " + file.getName(), e);
            }
        }

        logger.info("Loaded " + loaded + " / " + files.length + " GUI(s) from config");
        return loaded;
    }

    /**
     * Loads a single GUI from a YAML file.
     *
     * @param file The YAML file
     * @return The loaded GUI config, or null if loading failed
     */
    public LoadedGuiConfig loadGui(File file) {
        if (file == null || !file.exists()) {
            logger.warning("GUI file does not exist: " + (file != null ? file.getName() : "null"));
            return null;
        }

        try {
            // Load YAML
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection guiSection = yaml.getConfigurationSection("gui");

            if (guiSection == null) {
                logger.warning("No 'gui' section found in " + file.getName());
                return null;
            }

            // Create config wrapper
            LoadedGuiConfig config = new LoadedGuiConfig(file);
            config.setYaml(yaml);

            // Parse basic settings
            String name = guiSection.getString("name");
            if (name == null || name.isEmpty()) {
                name = file.getName().replace(".yml", "");
            }
            config.setName(name);

            // Parse command settings
            String command = guiSection.getString("command");
            config.setCommand(command);

            List<String> aliases = guiSection.getStringList("aliases");
            config.setAliases(aliases);

            String permission = guiSection.getString("permission");
            config.setPermission(permission);

            boolean autoRegister = guiSection.getBoolean("auto-register", true);
            config.setAutoRegister(autoRegister);

            // Build the GUI using YamlGuiLoader
            IGui gui = loader.loadFromConfig(yaml);

            if (gui == null) {
                logger.warning("Failed to build GUI from " + file.getName());
                return null;
            }

            config.setGui(gui);

            // Store in loaded configs
            loadedConfigs.put(name.toLowerCase(), config);

            logger.info("Loaded GUI '" + name + "' from " + file.getName());

            return config;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading GUI from " + file.getName(), e);
            return null;
        }
    }

    /**
     * Registers all commands for loaded GUIs.
     *
     * @return Number of commands registered
     */
    public int registerCommands() {
        int registered = 0;

        for (LoadedGuiConfig config : loadedConfigs.values()) {
            if (!config.isAutoRegister()) {
                continue; // Skip if auto-register is disabled
            }

            String command = config.getCommand();
            if (command == null || command.isEmpty()) {
                continue; // Skip if no command defined
            }

            IGui gui = config.getGui();
            if (gui == null) {
                continue; // Skip if GUI is null
            }

            // Register the command
            boolean success = commandRegistry.registerCommand(
                command,
                gui,
                config.getPermission(),
                config.getAliases()
            );

            if (success) {
                registered++;
            }
        }

        if (registered > 0) {
            logger.info("Registered " + registered + " GUI command(s)");
        }

        return registered;
    }

    /**
     * Unregisters all commands.
     */
    public void unregisterCommands() {
        commandRegistry.unregisterAll();
    }

    /**
     * Reloads a specific GUI by name.
     *
     * @param name The GUI name
     * @return true if reloaded successfully, false otherwise
     */
    public boolean reloadGui(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        LoadedGuiConfig config = loadedConfigs.get(name.toLowerCase());

        if (config == null) {
            logger.warning("GUI not found: " + name);
            return false;
        }

        try {
            // Unregister command
            String command = config.getCommand();
            if (command != null && !command.isEmpty()) {
                commandRegistry.unregisterCommand(command);
            }

            // Remove from loaded configs
            loadedConfigs.remove(name.toLowerCase());

            // Reload from file
            LoadedGuiConfig reloaded = loadGui(config.getSourceFile());

            if (reloaded == null) {
                logger.warning("Failed to reload GUI: " + name);
                return false;
            }

            // Re-register command
            if (reloaded.isAutoRegister() && reloaded.getCommand() != null) {
                commandRegistry.registerCommand(
                    reloaded.getCommand(),
                    reloaded.getGui(),
                    reloaded.getPermission(),
                    reloaded.getAliases()
                );
            }

            logger.info("Reloaded GUI: " + name);
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reloading GUI: " + name, e);
            return false;
        }
    }

    /**
     * Reloads all GUIs.
     *
     * @return Number of GUIs reloaded
     */
    public int reloadAll() {
        logger.info("Reloading all GUIs...");

        // Unregister all commands
        unregisterCommands();

        // Clear loaded configs
        loadedConfigs.clear();

        // Reload all
        int loaded = loadAllGuis();

        // Re-register commands
        registerCommands();

        return loaded;
    }

    /**
     * Gets a loaded GUI config by name.
     *
     * @param name The GUI name
     * @return The config, or null if not found
     */
    public LoadedGuiConfig getConfig(String name) {
        if (name == null) {
            return null;
        }
        return loadedConfigs.get(name.toLowerCase());
    }

    /**
     * Gets all loaded GUI configs.
     *
     * @return Collection of configs (never null)
     */
    public Collection<LoadedGuiConfig> getAllConfigs() {
        return new ArrayList<>(loadedConfigs.values());
    }

    /**
     * Gets the number of loaded GUIs.
     *
     * @return The count
     */
    public int getLoadedCount() {
        return loadedConfigs.size();
    }

    /**
     * Checks if a GUI is loaded.
     *
     * @param name The GUI name
     * @return true if loaded, false otherwise
     */
    public boolean isLoaded(String name) {
        if (name == null) {
            return false;
        }
        return loadedConfigs.containsKey(name.toLowerCase());
    }

    /**
     * Creates an example GUI configuration file.
     *
     * @return true if created successfully, false otherwise
     */
    public boolean createExampleConfig() {
        File exampleFile = new File(guisFolder, "example.yml");

        if (exampleFile.exists()) {
            logger.info("Example GUI config already exists: " + exampleFile.getName());
            return false;
        }

        try {
            YamlConfiguration example = new YamlConfiguration();

            example.set("gui.name", "example-gui");
            example.set("gui.title", "&6&lExample GUI");
            example.set("gui.rows", 3);
            example.set("gui.command", "example");
            example.set("gui.aliases", Arrays.asList("examplegui", "eg"));
            example.set("gui.permission", "aurora.example");
            example.set("gui.auto-register", true);

            // Border
            example.set("gui.border.material", "GRAY_STAINED_GLASS_PANE");
            example.set("gui.border.name", " ");

            // Example item
            example.set("gui.items.0.slot", 13);
            example.set("gui.items.0.material", "DIAMOND");
            example.set("gui.items.0.name", "&b&lClick Me!");
            example.set("gui.items.0.lore", Arrays.asList("&7This is an example item", "&7Click to close the GUI"));
            example.set("gui.items.0.action", "close");

            example.save(exampleFile);
            logger.info("Created example GUI config: " + exampleFile.getName());
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create example config", e);
            return false;
        }
    }
}
