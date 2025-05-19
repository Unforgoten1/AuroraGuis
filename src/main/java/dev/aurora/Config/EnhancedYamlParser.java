package dev.aurora.Config;

import dev.aurora.Struct.Animation.Animation;
import dev.aurora.Struct.Animation.Animations.*;
import dev.aurora.Struct.Animation.Frame;
import dev.aurora.Struct.Condition.ClickCondition;
import dev.aurora.Struct.Cooldown.ClickCooldown;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Extended YAML parsing utilities for advanced AuroraGuis features.
 * <p>
 * Provides static methods to parse animations, conditions, cooldowns, and actions
 * from YAML configuration sections.
 * </p>
 *
 * @since 1.1.0
 */
public class EnhancedYamlParser {

    private static Logger logger = Logger.getLogger("AuroraGuis");

    /**
     * Sets the logger for parser operations.
     *
     * @param newLogger The logger to use
     */
    public static void setLogger(Logger newLogger) {
        logger = newLogger;
    }

    /**
     * Parses an Animation from a configuration section.
     * <p>
     * Expected format:
     * <pre>
     * animation:
     *   type: "PULSING_GLOW"
     *   speed: 10
     * </pre>
     * </p>
     *
     * @param section The configuration section
     * @return The parsed Animation, or null if parsing failed
     */
    public static dev.aurora.Struct.Animation.API.IAnimation parseAnimation(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        try {
            String type = section.getString("type", "").toUpperCase();
            int speed = section.getInt("speed", 10);

            switch (type) {
                case "PULSING_GLOW":
                    // PulsingGlow requires an ItemStack - cannot be created without item context
                    // This will be handled at item level, not in general parsing
                    logger.warning("PulsingGlow animation requires item context - use at item level");
                    return null;

                case "PULSING_BORDER":
                    // PulsingBorder has no-arg constructor
                    return new PulsingBorder();

                case "ITEM_ROLLING":
                    // Requires item list - not fully supported in YAML yet
                    return null;

                case "ROTATING_COMPASS":
                    // RotatingCompass has no-arg constructor
                    return new RotatingCompass();

                case "LOADING_BAR":
                    // LoadingBar requires specific parameters
                    int slots = section.getInt("slots", 9);
                    return new LoadingBar(slots, speed);

                case "WAVE":
                case "WAVE_ANIMATION":
                    // WaveAnimation requires slots array and materials
                    logger.warning("WaveAnimation requires complex setup - not supported in simple YAML");
                    return null;

                case "MARQUEE":
                case "MARQUEE_ANIMATION":
                    // MarqueeAnimation requires text and material
                    String marqueeText = section.getString("text", "");
                    org.bukkit.Material marqueeMat = org.bukkit.Material.PAPER;
                    return new MarqueeAnimation(marqueeText, speed, marqueeMat);

                case "SPIRAL":
                case "SPIRAL_ANIMATION":
                    // SpiralAnimation requires complex parameters
                    logger.warning("SpiralAnimation requires complex setup - not supported in simple YAML");
                    return null;

                case "TYPEWRITER":
                case "TYPEWRITER_ANIMATION":
                    String text = section.getString("text", "");
                    org.bukkit.Material typeMat = org.bukkit.Material.PAPER;
                    return new TypewriterAnimation(text, typeMat);

                default:
                    logger.warning("Unknown animation type: " + type);
                    return null;
            }

        } catch (Exception e) {
            logger.warning("Failed to parse animation: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses a ClickCondition from a configuration section.
     * <p>
     * Expected format:
     * <pre>
     * requirements:
     *   permission: "shop.weapons"
     *   level: 10
     *   money: 100
     *   deny-message: "&cYou don't meet the requirements!"
     * </pre>
     * </p>
     *
     * @param section The configuration section
     * @return The parsed ClickCondition, or null if parsing failed
     */
    public static ClickCondition parseCondition(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        try {
            ClickCondition condition = null;

            // Parse permission requirement
            String permission = section.getString("permission");
            if (permission != null && !permission.isEmpty()) {
                condition = ClickCondition.requirePermission(permission);
            }

            // Parse level requirement
            if (section.contains("level")) {
                int level = section.getInt("level");
                // Level requirement will need to be handled differently
                // ClickCondition doesn't have a .custom() method currently
                // This feature requires extending ClickCondition class
                logger.warning("Level requirements not yet fully supported in conditions");
            }

            // Parse click type requirement
            String clickType = section.getString("click-type");
            if (clickType != null) {
                switch (clickType.toUpperCase()) {
                    case "LEFT":
                        ClickCondition leftClick = ClickCondition.requireLeftClick();
                        condition = condition != null ? condition.and(leftClick) : leftClick;
                        break;
                    case "RIGHT":
                        ClickCondition rightClick = ClickCondition.requireRightClick();
                        condition = condition != null ? condition.and(rightClick) : rightClick;
                        break;
                    case "SHIFT":
                    case "SHIFT_CLICK":
                        ClickCondition shiftClick = ClickCondition.requireShiftClick();
                        condition = condition != null ? condition.and(shiftClick) : shiftClick;
                        break;
                }
            }

            // Note: Money requirements would require Vault integration
            // This can be added later if needed

            return condition;

        } catch (Exception e) {
            logger.warning("Failed to parse condition: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses a ClickCooldown from a cooldown value.
     * <p>
     * Can be an integer (milliseconds) or a section with per-slot cooldowns.
     * </p>
     *
     * @param cooldownValue The cooldown value (Integer or ConfigurationSection)
     * @return The parsed ClickCooldown, or null if parsing failed
     */
    public static ClickCooldown parseCooldown(Object cooldownValue) {
        if (cooldownValue == null) {
            return null;
        }

        try {
            // ClickCooldown has a no-arg constructor
            // Cooldown values are set via setCooldown() method
            // For now, return a basic cooldown instance
            // The actual cooldown time would need to be set separately
            logger.warning("ClickCooldown parsing needs enhancement - using default cooldown");
            return new ClickCooldown();

            // TODO: Extend ClickCooldown to accept milliseconds in constructor
            // or add a factory method for creating cooldowns with specific durations

            // return null;

        } catch (Exception e) {
            logger.warning("Failed to parse cooldown: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses actions from a YAML value (single action or list of actions).
     * <p>
     * Expected formats:
     * <pre>
     * # Single action
     * action: "close"
     *
     * # Multiple actions
     * actions:
     *   - type: "sound"
     *     sound: "ENTITY_PLAYER_LEVELUP"
     *   - type: "message"
     *     message: "&aSuccess!"
     *   - type: "close"
     * </pre>
     * </p>
     *
     * @param actionsValue The actions value (String, ConfigurationSection, or List)
     * @param itemConfig The item configuration section (for context)
     * @return List of action consumers (never null, may be empty)
     */
    public static List<Consumer<InventoryClickEvent>> parseActions(Object actionsValue, ConfigurationSection itemConfig) {
        List<Consumer<InventoryClickEvent>> actions = new ArrayList<>();

        if (actionsValue == null) {
            return actions;
        }

        try {
            // Single action (String)
            if (actionsValue instanceof String) {
                String action = (String) actionsValue;
                Consumer<InventoryClickEvent> consumer = createActionConsumer(action, itemConfig);
                if (consumer != null) {
                    actions.add(consumer);
                }
                return actions;
            }

            // List of actions
            if (actionsValue instanceof List) {
                List<?> actionList = (List<?>) actionsValue;
                for (Object actionObj : actionList) {
                    if (actionObj instanceof ConfigurationSection) {
                        ConfigurationSection actionSection = (ConfigurationSection) actionObj;
                        Consumer<InventoryClickEvent> consumer = createActionFromSection(actionSection);
                        if (consumer != null) {
                            actions.add(consumer);
                        }
                    } else if (actionObj instanceof String) {
                        Consumer<InventoryClickEvent> consumer = createActionConsumer((String) actionObj, itemConfig);
                        if (consumer != null) {
                            actions.add(consumer);
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.warning("Failed to parse actions: " + e.getMessage());
        }

        return actions;
    }

    /**
     * Creates an action consumer from an action string.
     *
     * @param action The action string (e.g., "close", "message: Hello")
     * @param itemConfig The item configuration
     * @return The action consumer, or null if invalid
     */
    private static Consumer<InventoryClickEvent> createActionConsumer(String action, ConfigurationSection itemConfig) {
        if (action == null || action.isEmpty()) {
            return null;
        }

        String actionLower = action.toLowerCase();

        if (actionLower.equals("close")) {
            return event -> event.getWhoClicked().closeInventory();
        }

        if (actionLower.startsWith("message:")) {
            String message = action.substring(8).trim();
            return event -> event.getWhoClicked().sendMessage(message);
        }

        if (actionLower.startsWith("command:")) {
            String command = action.substring(8).trim();
            return event -> {
                Player player = (Player) event.getWhoClicked();
                player.performCommand(command);
            };
        }

        if (actionLower.startsWith("console_command:")) {
            String command = action.substring(16).trim();
            return event -> {
                Player player = (Player) event.getWhoClicked();
                String finalCommand = command.replace("%player%", player.getName());
                org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), finalCommand);
            };
        }

        return null;
    }

    /**
     * Creates an action consumer from a configuration section.
     *
     * @param section The action configuration section
     * @return The action consumer, or null if invalid
     */
    private static Consumer<InventoryClickEvent> createActionFromSection(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String type = section.getString("type", "").toLowerCase();

        switch (type) {
            case "close":
                return event -> event.getWhoClicked().closeInventory();

            case "message":
                String message = section.getString("message", "");
                return event -> event.getWhoClicked().sendMessage(message);

            case "command":
                String command = section.getString("command", "");
                return event -> {
                    Player player = (Player) event.getWhoClicked();
                    player.performCommand(command);
                };

            case "console_command":
                String consoleCommand = section.getString("command", "");
                return event -> {
                    Player player = (Player) event.getWhoClicked();
                    String finalCommand = consoleCommand.replace("%player%", player.getName());
                    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), finalCommand);
                };

            case "sound":
                String soundName = section.getString("sound", "");
                float volume = (float) section.getDouble("volume", 1.0);
                float pitch = (float) section.getDouble("pitch", 1.0);
                return event -> {
                    try {
                        Player player = (Player) event.getWhoClicked();
                        org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName.toUpperCase());
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid sound: " + soundName);
                    }
                };

            default:
                logger.warning("Unknown action type: " + type);
                return null;
        }
    }

    /**
     * Parses placeholders in a string using PlaceholderAPI if available.
     * <p>
     * If PlaceholderAPI is not available, returns the string unchanged.
     * </p>
     *
     * @param text The text with placeholders
     * @param player Optional player for placeholder parsing (can be null)
     * @return The parsed text
     */
    public static String parsePlaceholders(String text, Player player) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Check if PlaceholderAPI is available via reflection
        try {
            Class<?> placeholderAPIClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method setPlaceholders = placeholderAPIClass.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class);
            return (String) setPlaceholders.invoke(null, player, text);
        } catch (ClassNotFoundException e) {
            // PlaceholderAPI not available - return text unchanged
            return text;
        } catch (Exception e) {
            // Error invoking PlaceholderAPI - return text unchanged
            logger.warning("Error parsing placeholders: " + e.getMessage());
            return text;
        }
    }

    /**
     * Parses placeholders in a list of strings.
     *
     * @param lines The list of strings
     * @param player Optional player for placeholder parsing
     * @return The parsed list
     */
    public static List<String> parsePlaceholders(List<String> lines, Player player) {
        if (lines == null || lines.isEmpty()) {
            return lines;
        }

        List<String> parsed = new ArrayList<>();
        for (String line : lines) {
            parsed.add(parsePlaceholders(line, player));
        }
        return parsed;
    }
}
