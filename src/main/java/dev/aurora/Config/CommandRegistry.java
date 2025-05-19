package dev.aurora.Config;

import dev.aurora.GUI.IGui;
import dev.aurora.Manager.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles runtime command registration via Bukkit CommandMap reflection.
 * <p>
 * Allows dynamic registration of commands for config-based GUIs without
 * requiring plugin.yml entries.
 * </p>
 *
 * @since 1.1.0
 */
public class CommandRegistry {

    private final JavaPlugin plugin;
    private final GuiManager guiManager;
    private final Map<String, GuiCommand> registeredCommands;
    private CommandMap commandMap;
    private Logger logger;

    /**
     * Creates a new CommandRegistry.
     *
     * @param plugin The plugin instance
     * @param guiManager The GUI manager
     */
    public CommandRegistry(JavaPlugin plugin, GuiManager guiManager) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (guiManager == null) {
            throw new IllegalArgumentException("GuiManager cannot be null");
        }

        this.plugin = plugin;
        this.guiManager = guiManager;
        this.registeredCommands = new HashMap<>();
        this.logger = plugin.getLogger();
        this.commandMap = getCommandMap();
    }

    /**
     * Registers a command for a GUI.
     *
     * @param commandName The command name (without /)
     * @param gui The GUI to open when the command is executed
     * @param permission Optional permission (can be null)
     * @param aliases Optional command aliases
     * @return true if registered successfully, false otherwise
     */
    public boolean registerCommand(String commandName, IGui gui, String permission, List<String> aliases) {
        if (commandName == null || commandName.isEmpty()) {
            logger.warning("Cannot register command: name is null or empty");
            return false;
        }

        if (gui == null) {
            logger.warning("Cannot register command '" + commandName + "': GUI is null");
            return false;
        }

        if (commandMap == null) {
            logger.severe("Cannot register command '" + commandName + "': CommandMap not available");
            return false;
        }

        try {
            // Create the command
            GuiCommand command = new GuiCommand(commandName, gui, permission, guiManager);

            // Set aliases if provided
            if (aliases != null && !aliases.isEmpty()) {
                command.setAliases(aliases);
            }

            // Register with Bukkit
            boolean success = commandMap.register(plugin.getName().toLowerCase(), command);

            if (success) {
                registeredCommands.put(commandName.toLowerCase(), command);
                logger.info("Registered command: /" + commandName +
                          (aliases != null && !aliases.isEmpty() ? " (aliases: " + aliases + ")" : ""));
                return true;
            } else {
                logger.warning("Failed to register command: /" + commandName + " (may already be registered)");
                return false;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registering command: /" + commandName, e);
            return false;
        }
    }

    /**
     * Unregisters a command.
     *
     * @param commandName The command name to unregister
     * @return true if unregistered successfully, false otherwise
     */
    public boolean unregisterCommand(String commandName) {
        if (commandName == null || commandName.isEmpty()) {
            return false;
        }

        String lowerName = commandName.toLowerCase();
        GuiCommand command = registeredCommands.remove(lowerName);

        if (command == null) {
            return false;
        }

        if (commandMap == null) {
            return false;
        }

        try {
            // Unregister from CommandMap
            command.unregister(commandMap);
            logger.info("Unregistered command: /" + commandName);
            return true;

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error unregistering command: /" + commandName, e);
            return false;
        }
    }

    /**
     * Unregisters all commands managed by this registry.
     */
    public void unregisterAll() {
        if (registeredCommands.isEmpty()) {
            return;
        }

        logger.info("Unregistering " + registeredCommands.size() + " command(s)...");

        // Copy keys to avoid concurrent modification
        String[] commandNames = registeredCommands.keySet().toArray(new String[0]);

        for (String commandName : commandNames) {
            unregisterCommand(commandName);
        }

        registeredCommands.clear();
    }

    /**
     * Checks if a command is registered.
     *
     * @param commandName The command name
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(String commandName) {
        if (commandName == null) {
            return false;
        }
        return registeredCommands.containsKey(commandName.toLowerCase());
    }

    /**
     * Gets a registered command.
     *
     * @param commandName The command name
     * @return The GuiCommand, or null if not found
     */
    public GuiCommand getCommand(String commandName) {
        if (commandName == null) {
            return null;
        }
        return registeredCommands.get(commandName.toLowerCase());
    }

    /**
     * Gets all registered command names.
     *
     * @return Array of command names
     */
    public String[] getRegisteredCommands() {
        return registeredCommands.keySet().toArray(new String[0]);
    }

    /**
     * Gets the number of registered commands.
     *
     * @return The count
     */
    public int getCommandCount() {
        return registeredCommands.size();
    }

    /**
     * Gets the Bukkit CommandMap via reflection.
     * <p>
     * This uses reflection to access the private commandMap field in the
     * PluginManager. This is necessary because Bukkit doesn't provide a
     * public API for runtime command registration.
     * </p>
     *
     * @return The CommandMap, or null if unavailable
     */
    private CommandMap getCommandMap() {
        try {
            // Get the server's command map via reflection
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap map = (CommandMap) commandMapField.get(Bukkit.getServer());

            if (map != null) {
                logger.fine("Successfully accessed Bukkit CommandMap");
                return map;
            }

        } catch (NoSuchFieldException e) {
            logger.severe("CommandMap field not found - command registration unavailable");
        } catch (IllegalAccessException e) {
            logger.severe("Cannot access CommandMap - command registration unavailable");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error accessing CommandMap", e);
        }

        return null;
    }

    /**
     * Reloads a command (unregisters then re-registers).
     *
     * @param commandName The command name
     * @param gui The new GUI instance
     * @param permission The permission
     * @param aliases The aliases
     * @return true if reloaded successfully, false otherwise
     */
    public boolean reloadCommand(String commandName, IGui gui, String permission, List<String> aliases) {
        unregisterCommand(commandName);
        return registerCommand(commandName, gui, permission, aliases);
    }
}
