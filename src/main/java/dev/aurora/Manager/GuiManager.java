package dev.aurora.Manager;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.GUI.IGui;
import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.Core.PacketEventManager;
import dev.aurora.Packet.Core.PacketGuiRegistry;
import dev.aurora.Struct.Listener.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<String, WeakReference<AuroraGui>> guis;
    private final Map<UUID, AuroraGui> activeGuis;
    private final AnimationScheduler animationScheduler;

    // Packet support
    private PacketEventManager packetEventManager;
    private boolean packetSupportEnabled = false;

    // Config-based GUI support
    private dev.aurora.Config.GuiConfigManager configManager;

    // Virtual GUI support
    private final Map<UUID, dev.aurora.GUI.VirtualGui> activeVirtualGuis;

    public GuiManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Use WeakHashMap for automatic cleanup of unused GUIs
        // Synchronized wrapper for thread safety
        this.guis = new ConcurrentHashMap<>();
        this.activeGuis = new ConcurrentHashMap<>();
        this.activeVirtualGuis = new ConcurrentHashMap<>();
        this.animationScheduler = new AnimationScheduler(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Enables packet-based GUI support
     * Initializes PacketEvents integration for anti-dupe validation
     * Call this once during plugin startup if you plan to use PacketGui
     */
    public void enablePacketSupport() {
        if (packetSupportEnabled) {
            plugin.getLogger().warning("Packet support already enabled");
            return;
        }

        try {
            packetEventManager = new PacketEventManager(plugin);
            packetEventManager.initialize();
            packetSupportEnabled = true;
            plugin.getLogger().info("Packet-based GUI support enabled successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to enable packet support: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if packet support is enabled
     * @return true if PacketEvents is initialized
     */
    public boolean isPacketSupportEnabled() {
        return packetSupportEnabled;
    }

    /**
     * Gets the packet event manager
     * @return The packet event manager, or null if not enabled
     */
    public PacketEventManager getPacketEventManager() {
        return packetEventManager;
    }

    /**
     * Polymorphic GUI registration - works with both AuroraGui and PacketGui
     * @param gui The GUI to register
     */
    public void registerGui(IGui gui) {
        gui.setManager(this);

        if (gui instanceof IPacketGui) {
            // PacketGui is registered in its own registry
            PacketGuiRegistry.getInstance().registerGui(gui.getName(), (IPacketGui) gui);
        } else if (gui instanceof AuroraGui) {
            // AuroraGui registered in the traditional registry
            guis.put(gui.getName(), new WeakReference<>((AuroraGui) gui));
        }
    }

    /**
     * Legacy method for backward compatibility
     * @param gui The AuroraGui to register
     */
    public void registerGui(AuroraGui gui) {
        gui.setManager(this);
        guis.put(gui.getName(), new WeakReference<>(gui));
    }

    /**
     * Polymorphic GUI opening - automatically detects GUI type
     * @param player The player to open for
     * @param gui The GUI to open (AuroraGui or PacketGui)
     */
    public void openGui(Player player, IGui gui) {
        if (gui instanceof IPacketGui) {
            // PacketGui handles its own opening and registration
            gui.open(player);
        } else if (gui instanceof AuroraGui) {
            openGui(player, (AuroraGui) gui);
        }
    }

    /**
     * Legacy method for opening AuroraGui (backward compatibility)
     * @param player The player to open for
     * @param gui The AuroraGui to open
     */
    public void openGui(Player player, AuroraGui gui) {
        player.openInventory(gui.getInventory());
        activeGuis.put(player.getUniqueId(), gui);

        // Call open listeners
        for (GuiListener listener : gui.getListeners()) {
            listener.onOpen(player, gui);
        }
    }

    public void openGui(Player player, String guiName) {
        WeakReference<AuroraGui> guiRef = guis.get(guiName);
        if (guiRef != null) {
            AuroraGui gui = guiRef.get();
            if (gui != null) {
                openGui(player, gui);
            } else {
                // GUI was garbage collected, remove the dead reference
                guis.remove(guiName);
            }
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public AnimationScheduler getAnimationScheduler() {
        return animationScheduler;
    }

    /**
     * Gets the GUI config manager for loading GUIs from YAML files.
     * <p>
     * The config manager must be initialized manually after GuiManager creation:
     * <pre>
     * GuiConfigManager configMgr = new GuiConfigManager(plugin, guiManager);
     * configMgr.loadAllGuis();
     * configMgr.registerCommands();
     * </pre>
     * </p>
     *
     * @return The config manager, or null if not initialized
     * @since 1.1.0
     */
    public dev.aurora.Config.GuiConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Sets the GUI config manager.
     * <p>
     * This should be called by plugins that want to use config-based GUIs.
     * </p>
     *
     * @param configManager The config manager
     * @since 1.1.0
     */
    public void setConfigManager(dev.aurora.Config.GuiConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Reloads a config-based GUI by name.
     * <p>
     * This is a convenience method that delegates to the config manager.
     * </p>
     *
     * @param name The GUI name
     * @return true if reloaded successfully, false otherwise
     * @since 1.1.0
     */
    public boolean reloadGui(String name) {
        if (configManager == null) {
            plugin.getLogger().warning("Cannot reload GUI: config manager not initialized");
            return false;
        }
        return configManager.reloadGui(name);
    }

    /**
     * Gets the currently active VirtualGui for a player.
     *
     * @param player The player
     * @return The active VirtualGui, or null if none
     * @since 1.1.0
     */
    public dev.aurora.GUI.VirtualGui getActiveVirtualGui(Player player) {
        return activeVirtualGuis.get(player.getUniqueId());
    }

    /**
     * Tracks a VirtualGui as active for a player.
     * <p>
     * This is called automatically by VirtualGui when opened.
     * </p>
     *
     * @param player The player
     * @param virtualGui The VirtualGui
     * @since 1.1.0
     */
    public void setActiveVirtualGui(Player player, dev.aurora.GUI.VirtualGui virtualGui) {
        if (virtualGui == null) {
            activeVirtualGuis.remove(player.getUniqueId());
        } else {
            activeVirtualGuis.put(player.getUniqueId(), virtualGui);
        }
    }

    /**
     * Get the currently active GUI for a player
     * @param player The player
     * @return The active GUI or null
     */
    public AuroraGui getActiveGui(Player player) {
        return activeGuis.get(player.getUniqueId());
    }

    /**
     * Get a registered GUI by name
     * @param name The GUI name
     * @return The GUI or null
     */
    public AuroraGui getGui(String name) {
        WeakReference<AuroraGui> guiRef = guis.get(name);
        if (guiRef != null) {
            AuroraGui gui = guiRef.get();
            if (gui == null) {
                // GUI was garbage collected, remove the dead reference
                guis.remove(name);
            }
            return gui;
        }
        return null;
    }

    /**
     * Cleans up dead weak references from the GUI registry
     * Called automatically during normal operations, can also be called manually
     */
    public void cleanupDeadReferences() {
        guis.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

    /**
     * Gets the number of registered GUIs (including weak references)
     *
     * @return Number of registered GUIs
     */
    public int getRegisteredGuiCount() {
        cleanupDeadReferences();
        return guis.size();
    }

    /**
     * Shutdown the GUI manager and animation scheduler
     * Should be called in plugin onDisable()
     */
    public void shutdown() {
        animationScheduler.shutdown();
        guis.clear();
        activeGuis.clear();

        // Shutdown packet support if enabled
        if (packetSupportEnabled && packetEventManager != null) {
            packetEventManager.shutdown();
            PacketGuiRegistry.getInstance().clearAll();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Check for PacketGui first (packet validation already happened)
        if (packetSupportEnabled) {
            IPacketGui packetGui = PacketGuiRegistry.getInstance().getActiveGui(player);
            if (packetGui != null && event.getInventory().equals(packetGui.getInventory())) {
                packetGui.handleClick(event);
                return;
            }
        }

        // Check for traditional AuroraGui
        AuroraGui gui = activeGuis.get(player.getUniqueId());
        if (gui != null && event.getInventory().equals(gui.getInventory())) {
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Player player = (Player) event.getPlayer();

        // Check for PacketGui first
        if (packetSupportEnabled) {
            IPacketGui packetGui = PacketGuiRegistry.getInstance().removeActiveGui(uuid);
            if (packetGui != null) {
                // Call close listeners
                for (GuiListener listener : packetGui.getListeners()) {
                    listener.onClose(player, null);
                }
                packetGui.cleanup();
                return;
            }
        }

        // Check for traditional AuroraGui
        AuroraGui gui = activeGuis.remove(uuid);
        if (gui != null) {
            // Call close listeners
            for (GuiListener listener : gui.getListeners()) {
                listener.onClose(player, gui);
            }
            gui.cleanup();
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player has an active PacketGui that should have been closed
        if (packetSupportEnabled) {
            IPacketGui activePacketGui = PacketGuiRegistry.getInstance().getActiveGui(uuid);

            if (activePacketGui != null) {
                // Player is opening a new inventory while we think they still have a PacketGui open

                // Check if it's the same inventory (re-opening)
                if (event.getInventory().equals(activePacketGui.getInventory())) {
                    // Same inventory, legitimate re-open
                    return;
                }

                // EXPLOIT DETECTED: Player opened new inventory without closing previous one
                // This means they withheld the close packet!
                plugin.getLogger().warning(
                        "Detected withheld close packet: " + player.getName() +
                        " opened new inventory without closing PacketGui '" + activePacketGui.getName() + "'"
                );

                // Trigger violation if it's a PacketGui
                if (activePacketGui instanceof dev.aurora.Packet.Core.PacketGui) {
                    dev.aurora.Packet.Core.PacketGui packetGui = (dev.aurora.Packet.Core.PacketGui) activePacketGui;
                    packetGui.triggerViolation(player, IPacketGui.ExploitType.NO_CLOSE_PACKET);
                }

                // Force cleanup the old GUI
                activePacketGui.cleanup();
                PacketGuiRegistry.getInstance().removeActiveGui(uuid);
            }
        }

        // Also check for traditional AuroraGui (lower severity)
        AuroraGui activeGui = activeGuis.get(uuid);
        if (activeGui != null && !event.getInventory().equals(activeGui.getInventory())) {
            // Clean up old GUI that wasn't properly closed
            activeGui.cleanup();
            activeGuis.remove(uuid);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // Clean up PacketGui if any
        if (packetSupportEnabled) {
            IPacketGui packetGui = PacketGuiRegistry.getInstance().getActiveGui(uuid);
            if (packetGui != null) {
                packetGui.cleanup();
                PacketGuiRegistry.getInstance().removeActiveGui(uuid);
            }
        }

        // Clean up AuroraGui if any
        AuroraGui gui = activeGuis.get(uuid);
        if (gui != null) {
            gui.cleanup();
            activeGuis.remove(uuid);
        }
    }
}