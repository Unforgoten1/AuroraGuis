package dev.aurora.Metrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistics for a specific GUI
 * Thread-safe tracking of opens, clicks, and timing data
 */
public class GuiStats {
    private final String guiName;
    private final AtomicInteger openCount;
    private final AtomicInteger closeCount;
    private final AtomicInteger totalClicks;
    private final Map<Integer, AtomicInteger> slotClicks; // slot -> click count
    private final Map<UUID, Integer> playerOpens; // player -> open count
    private final AtomicLong totalOpenDuration; // milliseconds
    private final long firstOpenTime;
    private long lastOpenTime;

    public GuiStats(String guiName) {
        this.guiName = guiName;
        this.openCount = new AtomicInteger(0);
        this.closeCount = new AtomicInteger(0);
        this.totalClicks = new AtomicInteger(0);
        this.slotClicks = new ConcurrentHashMap<>();
        this.playerOpens = new ConcurrentHashMap<>();
        this.totalOpenDuration = new AtomicLong(0);
        this.firstOpenTime = System.currentTimeMillis();
        this.lastOpenTime = this.firstOpenTime;
    }

    /**
     * Records a GUI open event
     *
     * @param playerId The player who opened the GUI
     */
    public void recordOpen(UUID playerId) {
        openCount.incrementAndGet();
        lastOpenTime = System.currentTimeMillis();
        playerOpens.merge(playerId, 1, Integer::sum);
    }

    /**
     * Records a GUI close event with duration
     *
     * @param durationMillis How long the GUI was open (milliseconds)
     */
    public void recordClose(long durationMillis) {
        closeCount.incrementAndGet();
        totalOpenDuration.addAndGet(durationMillis);
    }

    /**
     * Records a click event
     *
     * @param slot The slot that was clicked
     */
    public void recordClick(int slot) {
        totalClicks.incrementAndGet();
        slotClicks.computeIfAbsent(slot, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Gets the GUI name
     */
    public String getGuiName() {
        return guiName;
    }

    /**
     * Gets total number of opens
     */
    public int getOpenCount() {
        return openCount.get();
    }

    /**
     * Gets total number of closes
     */
    public int getCloseCount() {
        return closeCount.get();
    }

    /**
     * Gets total clicks across all slots
     */
    public int getTotalClicks() {
        return totalClicks.get();
    }

    /**
     * Gets click count for a specific slot
     *
     * @param slot The slot number
     * @return Number of clicks on that slot
     */
    public int getSlotClicks(int slot) {
        AtomicInteger clicks = slotClicks.get(slot);
        return clicks != null ? clicks.get() : 0;
    }

    /**
     * Gets all slot click data
     *
     * @return Map of slot -> click count
     */
    public Map<Integer, Integer> getAllSlotClicks() {
        Map<Integer, Integer> result = new HashMap<>();
        for (Map.Entry<Integer, AtomicInteger> entry : slotClicks.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    /**
     * Gets the top N most clicked slots
     *
     * @param n Number of top slots to return
     * @return List of slots sorted by click count (descending)
     */
    public List<Map.Entry<Integer, Integer>> getTopClickedSlots(int n) {
        return getAllSlotClicks().entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(n)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Gets number of unique players who opened this GUI
     */
    public int getUniquePlayerCount() {
        return playerOpens.size();
    }

    /**
     * Gets all player open counts
     *
     * @return Map of player UUID -> open count
     */
    public Map<UUID, Integer> getPlayerOpens() {
        return new HashMap<>(playerOpens);
    }

    /**
     * Gets average duration the GUI was kept open (milliseconds)
     *
     * @return Average duration, or 0 if no closes recorded
     */
    public double getAverageDuration() {
        int closes = closeCount.get();
        if (closes == 0) return 0;
        return (double) totalOpenDuration.get() / closes;
    }

    /**
     * Gets total duration this GUI has been open (milliseconds)
     */
    public long getTotalDuration() {
        return totalOpenDuration.get();
    }

    /**
     * Gets time of first open (Unix timestamp)
     */
    public long getFirstOpenTime() {
        return firstOpenTime;
    }

    /**
     * Gets time of last open (Unix timestamp)
     */
    public long getLastOpenTime() {
        return lastOpenTime;
    }

    /**
     * Gets average clicks per open
     *
     * @return Average number of clicks, or 0 if no opens
     */
    public double getAverageClicksPerOpen() {
        int opens = openCount.get();
        if (opens == 0) return 0;
        return (double) totalClicks.get() / opens;
    }

    /**
     * Resets all statistics
     */
    public void reset() {
        openCount.set(0);
        closeCount.set(0);
        totalClicks.set(0);
        slotClicks.clear();
        playerOpens.clear();
        totalOpenDuration.set(0);
    }

    /**
     * Generates a summary report
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== GUI Statistics: ").append(guiName).append(" ===\n");
        report.append("Opens: ").append(getOpenCount()).append("\n");
        report.append("Closes: ").append(getCloseCount()).append("\n");
        report.append("Unique Players: ").append(getUniquePlayerCount()).append("\n");
        report.append("Total Clicks: ").append(getTotalClicks()).append("\n");
        report.append("Avg Clicks/Open: ").append(String.format("%.2f", getAverageClicksPerOpen())).append("\n");
        report.append("Avg Duration: ").append(String.format("%.2f", getAverageDuration() / 1000.0)).append("s\n");

        List<Map.Entry<Integer, Integer>> topSlots = getTopClickedSlots(5);
        if (!topSlots.isEmpty()) {
            report.append("\nTop Clicked Slots:\n");
            for (int i = 0; i < topSlots.size(); i++) {
                Map.Entry<Integer, Integer> entry = topSlots.get(i);
                report.append("  ").append(i + 1).append(". Slot ").append(entry.getKey())
                      .append(": ").append(entry.getValue()).append(" clicks\n");
            }
        }

        return report.toString();
    }

    @Override
    public String toString() {
        return "GuiStats{" +
                "name='" + guiName + '\'' +
                ", opens=" + getOpenCount() +
                ", closes=" + getCloseCount() +
                ", clicks=" + getTotalClicks() +
                ", uniquePlayers=" + getUniquePlayerCount() +
                '}';
    }
}
