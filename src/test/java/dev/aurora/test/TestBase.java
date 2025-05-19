package dev.aurora.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.aurora.Manager.GuiManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class providing MockBukkit setup for all tests
 * Provides pre-configured server, plugin, GuiManager, and player mocks
 */
public abstract class TestBase {
    protected ServerMock server;
    protected JavaPlugin plugin;
    protected GuiManager guiManager;
    protected PlayerMock player;

    @BeforeEach
    public void setUp() {
        // Initialize MockBukkit server
        server = MockBukkit.mock();

        // Create mock plugin
        plugin = MockBukkit.createMockPlugin();

        // Create GuiManager instance
        guiManager = new GuiManager(plugin);

        // Create test player
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    public void tearDown() {
        // Cleanup GuiManager
        if (guiManager != null) {
            guiManager.shutdown();
        }

        // Unmock Bukkit
        MockBukkit.unmock();
    }

    /**
     * Creates additional player for multi-player tests
     */
    protected PlayerMock createPlayer(String name) {
        return server.addPlayer(name);
    }

    /**
     * Waits for scheduler tasks to complete
     */
    protected void waitForScheduler(int ticks) {
        server.getScheduler().performTicks(ticks);
    }
}
