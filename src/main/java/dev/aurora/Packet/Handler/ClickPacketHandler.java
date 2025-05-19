package dev.aurora.Packet.Handler;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import dev.aurora.Packet.API.IPacketGui;
import dev.aurora.Packet.Core.PacketGuiRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Handles WINDOW_CLICK packets for anti-dupe validation
 * Intercepts clicks before Bukkit processes them
 */
public class ClickPacketHandler extends PacketListenerAbstract {

    private final JavaPlugin plugin;
    private final PacketGuiRegistry registry;

    public ClickPacketHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.registry = PacketGuiRegistry.getInstance();
    }

    /**
     * Called when a packet is received from the client
     * @param event The packet receive event
     */
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Only process WINDOW_CLICK packets
        if (event.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) {
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
        WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);

        // Validate the click through the GUI's validator
        boolean allowed = gui.getValidator().validateClick(player, packet);

        if (!allowed) {
            // Cancel the packet to prevent Bukkit from processing it
            event.setCancelled(true);
            plugin.getLogger().fine("Cancelled click packet for " + player.getName() + " (failed validation)");
        }
    }
}
