package dev.aurora.Config;

import dev.aurora.GUI.IGui;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for individual GUI configuration loaded from YAML.
 * <p>
 * Each LoadedGuiConfig represents one YAML file in the guis/ folder and tracks
 * its source file, parsed configuration, and built GUI instance.
 * </p>
 *
 * @since 1.1.0
 */
public class LoadedGuiConfig {

    private final File sourceFile;
    private YamlConfiguration yaml;
    private IGui gui;
    private String name;
    private String command;
    private List<String> aliases;
    private String permission;
    private boolean autoRegister;
    private long lastModified;

    /**
     * Creates a new LoadedGuiConfig from a file.
     *
     * @param sourceFile The YAML file containing the GUI configuration
     */
    public LoadedGuiConfig(File sourceFile) {
        if (sourceFile == null) {
            throw new IllegalArgumentException("Source file cannot be null");
        }
        this.sourceFile = sourceFile;
        this.aliases = new ArrayList<>();
        this.lastModified = sourceFile.lastModified();
    }

    /**
     * Gets the source YAML file.
     *
     * @return The file
     */
    public File getSourceFile() {
        return sourceFile;
    }

    /**
     * Gets the parsed YAML configuration.
     *
     * @return The YAML configuration, or null if not loaded
     */
    public YamlConfiguration getYaml() {
        return yaml;
    }

    /**
     * Sets the parsed YAML configuration.
     *
     * @param yaml The YAML configuration
     */
    public void setYaml(YamlConfiguration yaml) {
        this.yaml = yaml;
    }

    /**
     * Gets the built GUI instance.
     *
     * @return The GUI, or null if not built
     */
    public IGui getGui() {
        return gui;
    }

    /**
     * Sets the built GUI instance.
     *
     * @param gui The GUI
     */
    public void setGui(IGui gui) {
        this.gui = gui;
    }

    /**
     * Gets the GUI name/identifier.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the GUI name/identifier.
     *
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the command to open this GUI.
     *
     * @return The command, or null if no command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the command to open this GUI.
     *
     * @param command The command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets the command aliases.
     *
     * @return List of aliases (never null)
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Sets the command aliases.
     *
     * @param aliases List of aliases
     */
    public void setAliases(List<String> aliases) {
        this.aliases = aliases != null ? new ArrayList<>(aliases) : new ArrayList<>();
    }

    /**
     * Gets the required permission.
     *
     * @return The permission, or null if no permission required
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Sets the required permission.
     *
     * @param permission The permission
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Checks if the command should be auto-registered.
     *
     * @return true if auto-register is enabled, false otherwise
     */
    public boolean isAutoRegister() {
        return autoRegister;
    }

    /**
     * Sets whether the command should be auto-registered.
     *
     * @param autoRegister true to auto-register, false otherwise
     */
    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

    /**
     * Reloads this GUI configuration from the file.
     *
     * @return true if reloaded successfully, false otherwise
     */
    public boolean reload() {
        if (!sourceFile.exists()) {
            return false;
        }

        try {
            this.yaml = YamlConfiguration.loadConfiguration(sourceFile);
            this.lastModified = sourceFile.lastModified();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the source file has been modified since last load.
     *
     * @return true if file has changed, false otherwise
     */
    public boolean hasChanged() {
        if (!sourceFile.exists()) {
            return false;
        }
        return sourceFile.lastModified() != lastModified;
    }

    /**
     * Builds the GUI from the YAML configuration.
     * <p>
     * This method must be implemented to parse the YAML and create the GUI instance.
     * It should be called by GuiConfigManager after loading the YAML.
     * </p>
     *
     * @return The built GUI, or null if building failed
     */
    public IGui build() {
        // This will be handled by YamlGuiLoader in GuiConfigManager
        // This method is here for future direct building support
        return null;
    }

    /**
     * Gets the last modified timestamp of the source file.
     *
     * @return The last modified time in milliseconds
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Updates the last modified timestamp.
     */
    public void updateLastModified() {
        if (sourceFile.exists()) {
            this.lastModified = sourceFile.lastModified();
        }
    }

    @Override
    public String toString() {
        return "LoadedGuiConfig{" +
                "name='" + name + '\'' +
                ", command='" + command + '\'' +
                ", file='" + sourceFile.getName() + '\'' +
                ", autoRegister=" + autoRegister +
                '}';
    }
}
