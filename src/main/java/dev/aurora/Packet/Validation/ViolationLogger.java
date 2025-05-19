package dev.aurora.Packet.Validation;

import dev.aurora.Packet.API.IPacketGui;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logs exploit attempts for auditing and analysis
 * Tracks violation counts per player and provides detailed logs
 */
public class ViolationLogger {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final File logDirectory;
    private final Map<UUID, PlayerViolationData> violationData;
    private final boolean enabled;

    /**
     * Creates a new violation logger
     * @param logDirectory The directory to write logs to
     * @param enabled Whether logging is enabled
     */
    public ViolationLogger(File logDirectory, boolean enabled) {
        this.logDirectory = logDirectory;
        this.violationData = new ConcurrentHashMap<>();
        this.enabled = enabled;

        if (enabled && !logDirectory.exists()) {
            logDirectory.mkdirs();
        }
    }

    /**
     * Logs a violation
     * @param player The player who violated
     * @param gui The GUI where violation occurred
     * @param exploitType The type of exploit
     * @param details Additional details
     */
    public void logViolation(Player player, IPacketGui gui, IPacketGui.ExploitType exploitType, String details) {
        if (!enabled) {
            return;
        }

        // Update player violation data
        UUID uuid = player.getUniqueId();
        PlayerViolationData data = violationData.computeIfAbsent(uuid, k -> new PlayerViolationData(player.getName()));
        data.recordViolation(exploitType);

        // Write to log file
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format(
                "[%s] %s (%s) - GUI: %s - Exploit: %s (Severity: %d) - Details: %s",
                timestamp,
                player.getName(),
                uuid,
                gui.getName(),
                exploitType.name(),
                exploitType.getSeverity(),
                details != null ? details : "None"
        );

        writeToFile(logMessage);

        // Console log for high-severity violations
        if (exploitType.getSeverity() >= 4) {
            System.err.println("[AuroraGuis Anti-Dupe] CRITICAL: " + logMessage);
        }
    }

    /**
     * Gets violation data for a player
     * @param player The player
     * @return Violation data, or null if none
     */
    public PlayerViolationData getViolationData(Player player) {
        return violationData.get(player.getUniqueId());
    }

    /**
     * Gets the total violation count for a player
     * @param player The player
     * @return Total violations
     */
    public int getTotalViolations(Player player) {
        PlayerViolationData data = violationData.get(player.getUniqueId());
        return data != null ? data.getTotalViolations() : 0;
    }

    /**
     * Clears violation data for a player
     * @param player The player
     */
    public void clearPlayer(Player player) {
        violationData.remove(player.getUniqueId());
    }

    /**
     * Clears all violation data
     */
    public void clearAll() {
        violationData.clear();
    }

    /**
     * Writes a message to the log file
     * @param message The message to write
     */
    private void writeToFile(String message) {
        try {
            String filename = "violations-" + FILE_DATE_FORMAT.format(new Date()) + ".log";
            File logFile = new File(logDirectory, filename);

            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(message);
            }
        } catch (IOException e) {
            System.err.println("[AuroraGuis] Failed to write violation log: " + e.getMessage());
        }
    }

    /**
     * Gets a summary report of all violations
     * @return Summary string
     */
    public String getSummaryReport() {
        if (violationData.isEmpty()) {
            return "No violations recorded.";
        }

        StringBuilder report = new StringBuilder();
        report.append("=== Violation Summary ===\n");
        report.append(String.format("Total players with violations: %d\n\n", violationData.size()));

        violationData.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getTotalViolations(), e1.getValue().getTotalViolations()))
                .limit(10)
                .forEach(entry -> {
                    PlayerViolationData data = entry.getValue();
                    report.append(String.format("%s: %d violations\n", data.getPlayerName(), data.getTotalViolations()));
                    data.getViolationCounts().forEach((type, count) ->
                            report.append(String.format("  - %s: %d\n", type.name(), count.get()))
                    );
                });

        return report.toString();
    }

    /**
     * Data class for tracking violations per player
     */
    public static class PlayerViolationData {
        private final String playerName;
        private final Map<IPacketGui.ExploitType, AtomicInteger> violationCounts;
        private final AtomicInteger totalViolations;
        private long firstViolationTime;
        private long lastViolationTime;

        public PlayerViolationData(String playerName) {
            this.playerName = playerName;
            this.violationCounts = new ConcurrentHashMap<>();
            this.totalViolations = new AtomicInteger(0);
            this.firstViolationTime = System.currentTimeMillis();
            this.lastViolationTime = System.currentTimeMillis();
        }

        public void recordViolation(IPacketGui.ExploitType exploitType) {
            violationCounts.computeIfAbsent(exploitType, k -> new AtomicInteger(0)).incrementAndGet();
            totalViolations.incrementAndGet();
            lastViolationTime = System.currentTimeMillis();
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getTotalViolations() {
            return totalViolations.get();
        }

        public int getViolationCount(IPacketGui.ExploitType exploitType) {
            AtomicInteger count = violationCounts.get(exploitType);
            return count != null ? count.get() : 0;
        }

        public Map<IPacketGui.ExploitType, AtomicInteger> getViolationCounts() {
            return violationCounts;
        }

        public long getFirstViolationTime() {
            return firstViolationTime;
        }

        public long getLastViolationTime() {
            return lastViolationTime;
        }
    }
}
