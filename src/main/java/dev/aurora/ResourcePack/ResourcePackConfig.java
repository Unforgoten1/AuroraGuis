package dev.aurora.ResourcePack;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for resource pack integration with AuroraGuis.
 * <p>
 * Loads and manages resource pack settings from a resource-pack.yml file,
 * including pack URL, verification hash, and custom font character mappings.
 * </p>
 * <p>
 * Example resource-pack.yml:
 * <pre>
 * resource-pack:
 *   enabled: true
 *   url: "https://example.com/custompack.zip"
 *   hash: "abc123..."
 *   required: false
 *
 * custom-fonts:
 *   coin: '\uE001'
 *   heart: '\u2764'
 *   star: '\u2605'
 * </pre>
 * </p>
 *
 * @since 1.1.0
 */
public class ResourcePackConfig {

    private static Logger logger = Logger.getLogger("AuroraGuis");

    private boolean enabled;
    private String packUrl;
    private String packHash;
    private boolean required;
    private final Map<String, Character> customFonts;

    /**
     * Creates a new ResourcePackConfig with default values.
     */
    public ResourcePackConfig() {
        this.enabled = false;
        this.packUrl = "";
        this.packHash = "";
        this.required = false;
        this.customFonts = new HashMap<>();
    }

    /**
     * Sets the logger for configuration operations.
     *
     * @param newLogger The logger to use
     */
    public static void setLogger(Logger newLogger) {
        logger = newLogger;
    }

    /**
     * Checks if resource pack integration is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether resource pack integration is enabled.
     *
     * @param enabled true to enable, false to disable
     * @return This config for chaining
     */
    public ResourcePackConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the resource pack download URL.
     *
     * @return The pack URL
     */
    public String getPackUrl() {
        return packUrl;
    }

    /**
     * Sets the resource pack download URL.
     *
     * @param packUrl The pack URL
     * @return This config for chaining
     */
    public ResourcePackConfig setPackUrl(String packUrl) {
        this.packUrl = packUrl != null ? packUrl : "";
        return this;
    }

    /**
     * Gets the resource pack SHA-1 hash for verification.
     *
     * @return The pack hash
     */
    public String getPackHash() {
        return packHash;
    }

    /**
     * Sets the resource pack SHA-1 hash for verification.
     *
     * @param packHash The pack hash
     * @return This config for chaining
     */
    public ResourcePackConfig setPackHash(String packHash) {
        this.packHash = packHash != null ? packHash : "";
        return this;
    }

    /**
     * Checks if the resource pack is required for players.
     *
     * @return true if required, false otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether the resource pack is required for players.
     *
     * @param required true if required, false otherwise
     * @return This config for chaining
     */
    public ResourcePackConfig setRequired(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Registers a custom font character mapping.
     *
     * @param name The name/identifier for the character
     * @param character The Unicode character
     * @return This config for chaining
     */
    public ResourcePackConfig registerCustomFont(String name, char character) {
        if (name != null && !name.isEmpty()) {
            customFonts.put(name.toLowerCase(), character);
        }
        return this;
    }

    /**
     * Gets a custom font character by name.
     *
     * @param name The font name/identifier
     * @return The character, or null if not found
     */
    public Character getCustomFont(String name) {
        return name != null ? customFonts.get(name.toLowerCase()) : null;
    }

    /**
     * Gets all registered custom fonts.
     *
     * @return Map of font names to characters
     */
    public Map<String, Character> getCustomFonts() {
        return new HashMap<>(customFonts);
    }

    /**
     * Loads configuration from a YamlConfiguration object.
     *
     * @param config The YAML configuration
     * @return This config for chaining
     */
    public ResourcePackConfig loadFromConfig(YamlConfiguration config) {
        if (config == null) {
            return this;
        }

        // Load resource pack settings
        ConfigurationSection packSection = config.getConfigurationSection("resource-pack");
        if (packSection != null) {
            this.enabled = packSection.getBoolean("enabled", false);
            this.packUrl = packSection.getString("url", "");
            this.packHash = packSection.getString("hash", "");
            this.required = packSection.getBoolean("required", false);
        }

        // Load custom font mappings
        ConfigurationSection fontsSection = config.getConfigurationSection("custom-fonts");
        if (fontsSection != null) {
            for (String fontName : fontsSection.getKeys(false)) {
                String charValue = fontsSection.getString(fontName);
                if (charValue != null && !charValue.isEmpty()) {
                    // Parse Unicode escape sequences
                    char character = parseUnicodeChar(charValue);
                    registerCustomFont(fontName, character);
                }
            }
        }

        logger.info("Loaded resource pack config: enabled=" + enabled +
                   ", custom fonts=" + customFonts.size());

        return this;
    }

    /**
     * Loads configuration from a file.
     *
     * @param configFile The resource-pack.yml file
     * @return This config for chaining
     */
    public ResourcePackConfig loadFromFile(File configFile) {
        if (!configFile.exists()) {
            logger.warning("Resource pack config file not found: " + configFile.getPath());
            return this;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            return loadFromConfig(config);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load resource pack config from " + configFile.getPath(), e);
            return this;
        }
    }

    /**
     * Saves default configuration to a file.
     * <p>
     * Creates an example resource-pack.yml with documentation.
     * </p>
     *
     * @param configFile The file to save to
     * @return true if saved successfully, false otherwise
     */
    public boolean saveDefaults(File configFile) {
        try {
            YamlConfiguration config = new YamlConfiguration();

            // Add header comments (Note: Bukkit doesn't preserve comments perfectly)
            config.set("resource-pack.enabled", false);
            config.set("resource-pack.url", "https://example.com/custompack.zip");
            config.set("resource-pack.hash", "");
            config.set("resource-pack.required", false);

            // Add example custom fonts
            config.set("custom-fonts.coin", "\\uE001");
            config.set("custom-fonts.heart", "\\u2764");
            config.set("custom-fonts.star", "\\u2605");

            config.save(configFile);
            logger.info("Created default resource pack config: " + configFile.getPath());
            return true;

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save default resource pack config", e);
            return false;
        }
    }

    /**
     * Parses a Unicode character from a string.
     * <p>
     * Supports formats: '\\uXXXX', 'X', or raw character.
     * </p>
     *
     * @param value The string value
     * @return The parsed character
     */
    private char parseUnicodeChar(String value) {
        if (value == null || value.isEmpty()) {
            return ' ';
        }

        // Handle Unicode escape sequences (\\uXXXX or \\uXXXX)
        if (value.contains("\\u") || value.contains("\\\\u")) {
            value = value.replace("\\\\u", "\\u");
            if (value.startsWith("\\u") && value.length() >= 6) {
                try {
                    String hex = value.substring(2, 6);
                    return (char) Integer.parseInt(hex, 16);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid Unicode escape: " + value);
                }
            }
        }

        // Handle quoted characters
        if (value.startsWith("'") && value.endsWith("'") && value.length() == 3) {
            return value.charAt(1);
        }

        // Return first character
        return value.charAt(0);
    }

    @Override
    public String toString() {
        return "ResourcePackConfig{" +
                "enabled=" + enabled +
                ", packUrl='" + packUrl + '\'' +
                ", required=" + required +
                ", customFonts=" + customFonts.size() +
                '}';
    }
}
