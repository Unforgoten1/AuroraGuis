package dev.aurora.Metrics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central metrics tracking system for all GUIs
 * Tracks opens, closes, clicks, and generates analytics reports
 */
public class GuiMetrics {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, GuiStats> stats;
    private final Map<UUID, OpenSession> activeSessions; // player -> current session
    private final JavaPlugin plugin;
    private final boolean enabled;
    private BukkitTask autoExportTask;

    /**
     * Represents an active GUI session
     */
    private static class OpenSession {
        final String guiName;
        final long openTime;

        OpenSession(String guiName) {
            this.guiName = guiName;
            this.openTime = System.currentTimeMillis();
        }

        long getDuration() {
            return System.currentTimeMillis() - openTime;
        }
    }

    /**
     * Creates a new metrics tracker
     *
     * @param plugin The plugin instance
     * @param enabled Whether metrics are enabled
     */
    public GuiMetrics(JavaPlugin plugin, boolean enabled) {
        this.plugin = plugin;
        this.enabled = enabled;
        this.stats = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    /**
     * Creates metrics with default enabled state (true)
     *
     * @param plugin The plugin instance
     */
    public GuiMetrics(JavaPlugin plugin) {
        this(plugin, true);
    }

    /**
     * Records a GUI open event
     *
     * @param guiName The name of the GUI
     * @param player The player who opened it
     */
    public void recordOpen(String guiName, Player player) {
        if (!enabled) return;

        GuiStats guiStats = stats.computeIfAbsent(guiName, GuiStats::new);
        guiStats.recordOpen(player.getUniqueId());

        // Start session tracking
        activeSessions.put(player.getUniqueId(), new OpenSession(guiName));
    }

    /**
     * Records a GUI close event
     *
     * @param player The player who closed the GUI
     */
    public void recordClose(Player player) {
        if (!enabled) return;

        OpenSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            GuiStats guiStats = stats.get(session.guiName);
            if (guiStats != null) {
                guiStats.recordClose(session.getDuration());
            }
        }
    }

    /**
     * Records a click event
     *
     * @param guiName The name of the GUI
     * @param slot The slot that was clicked
     */
    public void recordClick(String guiName, int slot) {
        if (!enabled) return;

        GuiStats guiStats = stats.computeIfAbsent(guiName, GuiStats::new);
        guiStats.recordClick(slot);
    }

    /**
     * Gets statistics for a specific GUI
     *
     * @param guiName The GUI name
     * @return The stats, or null if not found
     */
    public GuiStats getStats(String guiName) {
        return stats.get(guiName);
    }

    /**
     * Gets all tracked GUI statistics
     *
     * @return Map of GUI name -> stats
     */
    public Map<String, GuiStats> getAllStats() {
        return new HashMap<>(stats);
    }

    /**
     * Gets names of all tracked GUIs
     *
     * @return Set of GUI names
     */
    public Set<String> getTrackedGuiNames() {
        return new HashSet<>(stats.keySet());
    }

    /**
     * Resets statistics for a specific GUI
     *
     * @param guiName The GUI name
     */
    public void resetStats(String guiName) {
        GuiStats guiStats = stats.get(guiName);
        if (guiStats != null) {
            guiStats.reset();
        }
    }

    /**
     * Resets all statistics
     */
    public void resetAllStats() {
        stats.values().forEach(GuiStats::reset);
    }

    /**
     * Clears all statistics (removes entries)
     */
    public void clearAllStats() {
        stats.clear();
        activeSessions.clear();
    }

    /**
     * Exports all statistics to JSON
     *
     * @return JSON string
     */
    public String exportToJson() {
        JsonObject root = new JsonObject();
        root.addProperty("exportTime", System.currentTimeMillis());
        root.addProperty("exportDate", new Date().toString());
        root.addProperty("totalGuis", stats.size());

        JsonObject guisObject = new JsonObject();
        for (Map.Entry<String, GuiStats> entry : stats.entrySet()) {
            GuiStats guiStats = entry.getValue();
            JsonObject guiObject = new JsonObject();

            guiObject.addProperty("openCount", guiStats.getOpenCount());
            guiObject.addProperty("closeCount", guiStats.getCloseCount());
            guiObject.addProperty("totalClicks", guiStats.getTotalClicks());
            guiObject.addProperty("uniquePlayers", guiStats.getUniquePlayerCount());
            guiObject.addProperty("averageDuration", guiStats.getAverageDuration());
            guiObject.addProperty("averageClicksPerOpen", guiStats.getAverageClicksPerOpen());
            guiObject.addProperty("firstOpenTime", guiStats.getFirstOpenTime());
            guiObject.addProperty("lastOpenTime", guiStats.getLastOpenTime());

            // Add slot click data
            JsonObject slotsObject = new JsonObject();
            for (Map.Entry<Integer, Integer> slotEntry : guiStats.getAllSlotClicks().entrySet()) {
                slotsObject.addProperty(String.valueOf(slotEntry.getKey()), slotEntry.getValue());
            }
            guiObject.add("slotClicks", slotsObject);

            guisObject.add(entry.getKey(), guiObject);
        }
        root.add("guis", guisObject);

        return GSON.toJson(root);
    }

    /**
     * Exports statistics to a file
     *
     * @param file The target file
     * @throws IOException if file cannot be written
     */
    public void exportToFile(File file) throws IOException {
        String json = exportToJson();

        // Create parent directories if needed
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }

    /**
     * Imports statistics from a JSON file
     *
     * @param file The file to import from
     * @throws IOException if file cannot be read
     */
    public void importFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getPath());
        }

        String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        // Note: Full import implementation would require deserializing the JSON
        // For now, this is a placeholder for the file reading part
    }

    /**
     * Enables automatic periodic export to file
     *
     * @param file The file to export to
     * @param intervalTicks Interval in ticks (20 ticks = 1 second)
     */
    public void enableAutoExport(File file, long intervalTicks) {
        if (autoExportTask != null) {
            autoExportTask.cancel();
        }

        autoExportTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin,
            () -> {
                try {
                    exportToFile(file);
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to auto-export metrics: " + e.getMessage());
                }
            },
            intervalTicks,
            intervalTicks
        );
    }

    /**
     * Disables automatic export
     */
    public void disableAutoExport() {
        if (autoExportTask != null) {
            autoExportTask.cancel();
            autoExportTask = null;
        }
    }

    /**
     * Generates a text report for all GUIs
     *
     * @return Multi-line report string
     */
    public String generateFullReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== AuroraGuis Metrics Report ===\n");
        report.append("Generated: ").append(new Date()).append("\n");
        report.append("Total GUIs Tracked: ").append(stats.size()).append("\n");
        report.append("Currently Open Sessions: ").append(activeSessions.size()).append("\n\n");

        // Sort by open count
        List<GuiStats> sortedStats = new ArrayList<>(stats.values());
        sortedStats.sort((a, b) -> Integer.compare(b.getOpenCount(), a.getOpenCount()));

        for (GuiStats guiStats : sortedStats) {
            report.append(guiStats.generateReport()).append("\n");
        }

        return report.toString();
    }

    /**
     * Gets the top N most popular GUIs by open count
     *
     * @param n Number of GUIs to return
     * @return List of GUI stats sorted by popularity
     */
    public List<GuiStats> getTopGuisByOpens(int n) {
        return stats.values().stream()
                .sorted((a, b) -> Integer.compare(b.getOpenCount(), a.getOpenCount()))
                .limit(n)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Gets the top N most clicked GUIs
     *
     * @param n Number of GUIs to return
     * @return List of GUI stats sorted by total clicks
     */
    public List<GuiStats> getTopGuisByClicks(int n) {
        return stats.values().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalClicks(), a.getTotalClicks()))
                .limit(n)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Checks if metrics tracking is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets number of active open sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Cleanup on plugin disable
     */
    public void shutdown() {
        disableAutoExport();
        activeSessions.clear();
    }
}
