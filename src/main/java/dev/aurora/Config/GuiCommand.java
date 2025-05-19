package dev.aurora.Config;

import dev.aurora.GUI.IGui;
import dev.aurora.Manager.GuiManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dynamically registered command that opens a GUI.
 * <p>
 * Extends Bukkit's Command class to provide runtime command registration
 * for config-based GUIs.
 * </p>
 *
 * @since 1.1.0
 */
public class GuiCommand extends Command {

    private final IGui gui;
    private final String permission;
    private final GuiManager guiManager;

    /**
     * Creates a new GUI command.
     *
     * @param name The command name
     * @param gui The GUI to open
     * @param permission Optional permission (can be null)
     * @param guiManager The GUI manager
     */
    public GuiCommand(String name, IGui gui, String permission, GuiManager guiManager) {
        super(name);

        if (gui == null) {
            throw new IllegalArgumentException("GUI cannot be null");
        }
        if (guiManager == null) {
            throw new IllegalArgumentException("GuiManager cannot be null");
        }

        this.gui = gui;
        this.permission = permission;
        this.guiManager = guiManager;

        // Set description
        this.setDescription("Opens the " + name + " GUI");

        // Set usage
        this.setUsage("/" + name + " [reload]");
    }

    /**
     * Gets the GUI associated with this command.
     *
     * @return The GUI
     */
    public IGui getGui() {
        return gui;
    }

    /**
     * Gets the required permission.
     *
     * @return The permission, or null if no permission required
     */
    public String getRequiredPermission() {
        return permission;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Handle reload subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Check for reload permission
            if (!player.hasPermission("aurora.admin") && !player.isOp()) {
                player.sendMessage("§cYou don't have permission to reload GUIs.");
                return true;
            }

            // Reload is handled by GuiConfigManager
            player.sendMessage("§eReloading GUI...");
            player.sendMessage("§7Use /aurorareload to reload all GUIs.");
            return true;
        }

        // Check permission
        if (permission != null && !permission.isEmpty()) {
            if (!player.hasPermission(permission)) {
                player.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
        }

        // Open the GUI
        try {
            gui.open(player);
            return true;
        } catch (Exception e) {
            player.sendMessage("§cAn error occurred while opening the GUI.");
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();

        // Tab complete reload subcommand for admins
        if (args.length == 1) {
            if (sender.hasPermission("aurora.admin") || sender.isOp()) {
                if ("reload".startsWith(args[0].toLowerCase())) {
                    completions.add("reload");
                }
            }
        }

        return completions;
    }

    @Override
    public String toString() {
        return "GuiCommand{" +
                "name='" + getName() + '\'' +
                ", permission='" + permission + '\'' +
                ", aliases=" + getAliases() +
                '}';
    }
}
