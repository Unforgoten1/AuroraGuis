package dev.aurora.Manager;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {
    private final Map<String, AuroraGui> guis;
    private final Map<UUID, AuroraGui> activeGuis;

    public GuiManager(JavaPlugin plugin) {
        this.guis = new HashMap<>();
        this.activeGuis = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void registerGui(AuroraGui gui) {
        gui.setManager(this);
        guis.put(gui.getName(), gui);
    }

    public void openGui(Player player, AuroraGui gui) {
        player.openInventory(gui.getInventory());
        activeGuis.put(player.getUniqueId(), gui);
    }

    public void openGui(Player player, String guiName) {
        AuroraGui gui = guis.get(guiName);
        if (gui != null) openGui(player, gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        AuroraGui gui = activeGuis.get(player.getUniqueId());
        if (gui != null && event.getInventory().equals(gui.getInventory())) {
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        activeGuis.remove(event.getPlayer().getUniqueId());
    }
}