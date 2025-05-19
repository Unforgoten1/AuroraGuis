package dev.aurora.Debug;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Debugging utility for GUI operations
 * Provides logging and state inspection capabilities
 */
public class GuiDebugger {
    private static boolean debugEnabled = false;
    private static final Logger logger = Bukkit.getLogger();

    /**
     * Enable debug mode
     */
    public static void enable() {
        debugEnabled = true;
        log("Debug mode enabled");
    }

    /**
     * Disable debug mode
     */
    public static void disable() {
        log("Debug mode disabled");
        debugEnabled = false;
    }

    /**
     * Check if debug mode is enabled
     * @return true if debug enabled
     */
    public static boolean isEnabled() {
        return debugEnabled;
    }

    /**
     * Log a debug message
     * @param message Message to log
     */
    public static void log(String message) {
        if (debugEnabled) {
            logger.log(Level.INFO, "[AuroraGUI Debug] " + message);
        }
    }

    /**
     * Log GUI state information
     * @param gui The GUI to log
     */
    public static void logGuiState(AuroraGui gui) {
        if (!debugEnabled) return;

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== GUI State: ").append(gui.getName()).append(" ===\n");
        sb.append("Current Page: ").append(gui.getCurrentPage()).append("\n");
        sb.append("Total Pages: ").append(gui.getTotalPages()).append("\n");
        sb.append("Viewers: ").append(gui.getViewers().size()).append("\n");
        sb.append("===========================");

        logger.log(Level.INFO, sb.toString());
    }

    /**
     * Log an error with exception
     * @param message Error message
     * @param throwable Exception
     */
    public static void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, "[AuroraGUI Error] " + message, throwable);
    }

    /**
     * Log a warning message
     * @param message Warning message
     */
    public static void warn(String message) {
        logger.log(Level.WARNING, "[AuroraGUI Warning] " + message);
    }

    /**
     * Log an info message (always logged, regardless of debug mode)
     * @param message Info message
     */
    public static void info(String message) {
        logger.log(Level.INFO, "[AuroraGUI] " + message);
    }
}
