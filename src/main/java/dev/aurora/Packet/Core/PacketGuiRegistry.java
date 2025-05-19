package dev.aurora.Packet.Core;

import dev.aurora.Packet.API.IPacketGui;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for tracking active packet-based GUIs per player
 * Thread-safe registry using ConcurrentHashMap
 */
public class PacketGuiRegistry {

    private static final PacketGuiRegistry INSTANCE = new PacketGuiRegistry();

    private final Map<UUID, IPacketGui> activeGuis = new ConcurrentHashMap<>();
    private final Map<String, IPacketGui> registeredGuis = new ConcurrentHashMap<>();

    private PacketGuiRegistry() {
        // Singleton
    }

    /**
     * Gets the singleton instance
     * @return The registry instance
     */
    public static PacketGuiRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a packet GUI by name
     * @param name The GUI name
     * @param gui The GUI instance
     */
    public void registerGui(String name, IPacketGui gui) {
        registeredGuis.put(name, gui);
    }

    /**
     * Unregisters a packet GUI by name
     * @param name The GUI name
     */
    public void unregisterGui(String name) {
        registeredGuis.remove(name);
    }

    /**
     * Gets a registered GUI by name
     * @param name The GUI name
     * @return The GUI, or null if not found
     */
    public IPacketGui getRegisteredGui(String name) {
        return registeredGuis.get(name);
    }

    /**
     * Sets the active GUI for a player
     * @param player The player
     * @param gui The GUI they're viewing
     */
    public void setActiveGui(Player player, IPacketGui gui) {
        activeGuis.put(player.getUniqueId(), gui);
    }

    /**
     * Gets the active GUI for a player
     * @param player The player
     * @return The active GUI, or null if none
     */
    public IPacketGui getActiveGui(Player player) {
        return activeGuis.get(player.getUniqueId());
    }

    /**
     * Gets the active GUI for a player by UUID
     * @param uuid The player UUID
     * @return The active GUI, or null if none
     */
    public IPacketGui getActiveGui(UUID uuid) {
        return activeGuis.get(uuid);
    }

    /**
     * Removes the active GUI for a player
     * @param player The player
     * @return The GUI that was removed, or null
     */
    public IPacketGui removeActiveGui(Player player) {
        return activeGuis.remove(player.getUniqueId());
    }

    /**
     * Removes the active GUI for a player by UUID
     * @param uuid The player UUID
     * @return The GUI that was removed, or null
     */
    public IPacketGui removeActiveGui(UUID uuid) {
        return activeGuis.remove(uuid);
    }

    /**
     * Checks if a player has an active packet GUI open
     * @param player The player
     * @return true if player has active GUI
     */
    public boolean hasActiveGui(Player player) {
        return activeGuis.containsKey(player.getUniqueId());
    }

    /**
     * Checks if a player has an active packet GUI open by UUID
     * @param uuid The player UUID
     * @return true if player has active GUI
     */
    public boolean hasActiveGui(UUID uuid) {
        return activeGuis.containsKey(uuid);
    }

    /**
     * Gets the number of active GUIs
     * @return Active GUI count
     */
    public int getActiveGuiCount() {
        return activeGuis.size();
    }

    /**
     * Gets the number of registered GUIs
     * @return Registered GUI count
     */
    public int getRegisteredGuiCount() {
        return registeredGuis.size();
    }

    /**
     * Clears all active GUIs
     * Should be called during shutdown
     */
    public void clearAll() {
        activeGuis.clear();
        registeredGuis.clear();
    }
}
