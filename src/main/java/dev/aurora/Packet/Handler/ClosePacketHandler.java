package dev.aurora.Packet.Handler;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.Core.PacketGuiRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Handles CLOSE_WINDOW packets for anti-dupe validation
 * Ensures inventory state is correct when GUI is closed
 */
public class ClosePacketHandler extends PacketListenerAbstract {

    private final JavaPlugin plugin;
    private final PacketGuiRegistry registry;

    public ClosePacketHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.registry = PacketGuiRegistry.getInstance();
    }

    /**
     * Called when a packet is received from the client
     * @param event The packet receive event
     */
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Only process CLOSE_WINDOW packets
        if (event.getPacketType() != PacketType.Play.Client.CLOSE_WINDOW) {
            return;
        }

        // Get player from event
        UUID uuid = event.getUser().getUUID();
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        // Check if player has an active packet GUI
        IPacketGui gui = registry.getActiveGui(player);
        if (gui == null) {
            return; // Not a packet GUI, let Bukkit handle it normally
        }

        // Parse the packet
        WrapperPlayClientCloseWindow packet = new WrapperPlayClientCloseWindow(event);

        // Validate the close through the GUI's validator
        boolean allowed = gui.getValidator().validateClose(player);

        if (!allowed) {
            // Cancel the packet and force resync
            event.setCancelled(true);
            gui.forceResync(player);
            plugin.getLogger().fine("Cancelled close packet for " + player.getName() + " (desync detected)");
        }
    }
}
