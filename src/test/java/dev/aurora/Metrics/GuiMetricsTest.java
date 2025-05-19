package dev.aurora.Metrics;

import dev.aurora.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GUI metrics and statistics tracking
 */
public class GuiMetricsTest extends TestBase {
    private GuiMetrics metrics;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        metrics = new GuiMetrics(plugin, true);
    }

    @Test
    public void testRecordOpen() {
        metrics.recordOpen("test-gui", player);

        GuiStats stats = metrics.getStats("test-gui");
        assertNotNull(stats);
        assertEquals(1, stats.getOpenCount());
        assertEquals(1, stats.getUniquePlayerCount());
    }

    @Test
    public void testRecordMultipleOpens() {
        metrics.recordOpen("test-gui", player);
        metrics.recordOpen("test-gui", player);
        metrics.recordOpen("test-gui", player);

        GuiStats stats = metrics.getStats("test-gui");
        assertEquals(3, stats.getOpenCount());
        assertEquals(1, stats.getUniquePlayerCount());
    }

    @Test
    public void testRecordClose() throws InterruptedException {
        metrics.recordOpen("test-gui", player);
        Thread.sleep(10); // Small delay
        metrics.recordClose(player);

        GuiStats stats = metrics.getStats("test-gui");
        assertEquals(1, stats.getCloseCount());
        assertTrue(stats.getAverageDuration() > 0);
    }

    @Test
    public void testRecordClick() {
        metrics.recordClick("test-gui", 5);
        metrics.recordClick("test-gui", 5);
        metrics.recordClick("test-gui", 10);

        GuiStats stats = metrics.getStats("test-gui");
        assertEquals(3, stats.getTotalClicks());
        assertEquals(2, stats.getSlotClicks(5));
        assertEquals(1, stats.getSlotClicks(10));
    }

    @Test
    public void testMultipleGuis() {
        metrics.recordOpen("gui-1", player);
        metrics.recordOpen("gui-2", player);
        metrics.recordClick("gui-1", 1);
        metrics.recordClick("gui-2", 2);

        assertEquals(2, metrics.getAllStats().size());
        assertNotNull(metrics.getStats("gui-1"));
        assertNotNull(metrics.getStats("gui-2"));
    }

    @Test
    public void testResetStats() {
        metrics.recordOpen("test-gui", player);
        metrics.recordClick("test-gui", 0);

        GuiStats stats = metrics.getStats("test-gui");
        assertEquals(1, stats.getOpenCount());

        metrics.resetStats("test-gui");
        assertEquals(0, stats.getOpenCount());
        assertEquals(0, stats.getTotalClicks());
    }

    @Test
    public void testClearAllStats() {
        metrics.recordOpen("gui-1", player);
        metrics.recordOpen("gui-2", player);

        assertEquals(2, metrics.getAllStats().size());

        metrics.clearAllStats();
        assertEquals(0, metrics.getAllStats().size());
    }

    @Test
    public void testExportToJson() {
        metrics.recordOpen("test-gui", player);
        metrics.recordClick("test-gui", 5);

        String json = metrics.exportToJson();

        assertNotNull(json);
        assertTrue(json.contains("test-gui"));
        assertTrue(json.contains("openCount"));
        assertTrue(json.contains("totalClicks"));
    }

    @Test
    public void testExportToFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("metrics.json").toFile();

        metrics.recordOpen("test-gui", player);
        metrics.recordClick("test-gui", 10);

        metrics.exportToFile(file);

        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    public void testGenerateFullReport() {
        metrics.recordOpen("gui-1", player);
        metrics.recordOpen("gui-2", player);

        String report = metrics.generateFullReport();

        assertNotNull(report);
        assertTrue(report.contains("AuroraGuis Metrics Report"));
        assertTrue(report.contains("gui-1"));
        assertTrue(report.contains("gui-2"));
    }

    @Test
    public void testTopGuisByOpens() {
        metrics.recordOpen("gui-1", player);
        metrics.recordOpen("gui-1", player);
        metrics.recordOpen("gui-2", player);

        List<GuiStats> topGuis = metrics.getTopGuisByOpens(2);

        assertEquals(2, topGuis.size());
        assertEquals("gui-1", topGuis.get(0).getGuiName());
        assertEquals(2, topGuis.get(0).getOpenCount());
    }

    @Test
    public void testTopGuisByClicks() {
        metrics.recordClick("gui-1", 1);
        metrics.recordClick("gui-1", 2);
        metrics.recordClick("gui-1", 3);
        metrics.recordClick("gui-2", 1);

        List<GuiStats> topGuis = metrics.getTopGuisByClicks(2);

        assertEquals(2, topGuis.size());
        assertEquals("gui-1", topGuis.get(0).getGuiName());
        assertEquals(3, topGuis.get(0).getTotalClicks());
    }

    @Test
    public void testDisabledMetrics() {
        GuiMetrics disabledMetrics = new GuiMetrics(plugin, false);

        disabledMetrics.recordOpen("test-gui", player);

        assertNull(disabledMetrics.getStats("test-gui"));
        assertFalse(disabledMetrics.isEnabled());
    }

    @Test
    public void testGuiStatsReport() {
        GuiStats stats = new GuiStats("test-gui");
        stats.recordOpen(player.getUniqueId());
        stats.recordClick(5);
        stats.recordClick(10);
        stats.recordClick(5);

        String report = stats.generateReport();

        assertNotNull(report);
        assertTrue(report.contains("test-gui"));
        assertTrue(report.contains("Opens: 1"));
        assertTrue(report.contains("Total Clicks: 3"));
        assertTrue(report.contains("Top Clicked Slots"));
    }

    @Test
    public void testTopClickedSlots() {
        GuiStats stats = new GuiStats("test-gui");
        stats.recordClick(1);
        stats.recordClick(1);
        stats.recordClick(1);
        stats.recordClick(2);
        stats.recordClick(2);
        stats.recordClick(3);

        List<java.util.Map.Entry<Integer, Integer>> topSlots = stats.getTopClickedSlots(3);

        assertEquals(3, topSlots.size());
        assertEquals(1, topSlots.get(0).getKey().intValue());
        assertEquals(3, topSlots.get(0).getValue().intValue());
    }
}
