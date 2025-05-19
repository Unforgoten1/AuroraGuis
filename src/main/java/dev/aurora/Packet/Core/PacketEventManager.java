package dev.aurora.Packet.Core;

import com.github.retrooper.packetevents.PacketEvents;
import dev.aurora.Packet.Handler.ClickPacketHandler;
import dev.aurora.Packet.Handler.ClosePacketHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages the lifecycle of PacketEvents integration
 * Handles initialization, registration of packet listeners, and shutdown
 */
public class PacketEventManager {

    private final JavaPlugin plugin;
    private boolean initialized = false;
    private ClickPacketHandler clickHandler;
    private ClosePacketHandler closeHandler;

    public PacketEventManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes PacketEvents and registers packet listeners
     * Should be called once during plugin startup
     */
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("PacketEventManager already initialized");
        }

        try {
            // Initialize PacketEvents if not already initialized
            if (!PacketEvents.getAPI().isInitialized()) {
                PacketEvents.getAPI().getSettings()
                        .checkForUpdates(false)
                        .bStats(false);
                PacketEvents.getAPI().load();
            }

            // Create and register packet handlers
            clickHandler = new ClickPacketHandler(plugin);
            closeHandler = new ClosePacketHandler(plugin);

            PacketEvents.getAPI().getEventManager().registerListener(clickHandler);
            PacketEvents.getAPI().getEventManager().registerListener(closeHandler);

            // Initialize PacketEvents
            if (!PacketEvents.getAPI().isInitialized()) {
                PacketEvents.getAPI().init();
            }

            initialized = true;
            plugin.getLogger().info("PacketEventManager initialized successfully");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize PacketEventManager: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("PacketEventManager initialization failed", e);
        }
    }

    /**
     * Shuts down PacketEvents and unregisters listeners
     * Should be called during plugin shutdown
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }

        try {
            // Unregister listeners
            if (clickHandler != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(clickHandler);
            }
            if (closeHandler != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(closeHandler);
            }

            // Terminate PacketEvents
            PacketEvents.getAPI().terminate();

            initialized = false;
            plugin.getLogger().info("PacketEventManager shutdown successfully");

        } catch (Exception e) {
            plugin.getLogger().warning("Error during PacketEventManager shutdown: " + e.getMessage());
        }
    }

    /**
     * Checks if PacketEvents is initialized
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the click packet handler
     * @return The click handler
     */
    public ClickPacketHandler getClickHandler() {
        return clickHandler;
    }

    /**
     * Gets the close packet handler
     * @return The close handler
     */
    public ClosePacketHandler getCloseHandler() {
        return closeHandler;
    }

    /**
     * Gets the plugin instance
     * @return The plugin
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }
}
