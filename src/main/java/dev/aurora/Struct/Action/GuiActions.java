package dev.aurora.Struct.Action;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Utilities.Sound.SoundEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * Pre-built action builders for common GUI interactions
 * Provides fluent API for composing complex actions
 */
public class GuiActions {

    /**
     * Close the GUI
     */
    public static Consumer<InventoryClickEvent> close() {
        return event -> event.getWhoClicked().closeInventory();
    }

    /**
     * Close with sound effect
     */
    public static Consumer<InventoryClickEvent> close(SoundEffect sound) {
        return event -> {
            sound.play((Player) event.getWhoClicked());
            event.getWhoClicked().closeInventory();
        };
    }

    /**
     * Go to next page
     */
    public static Consumer<InventoryClickEvent> nextPage(AuroraGui gui) {
        return event -> {
            if (gui.hasNextPage()) {
                gui.nextPage();
            }
        };
    }

    /**
     * Go to next page with sound
     */
    public static Consumer<InventoryClickEvent> nextPage(AuroraGui gui, SoundEffect sound) {
        return event -> {
            if (gui.hasNextPage()) {
                sound.play((Player) event.getWhoClicked());
                gui.nextPage();
            } else {
                SoundEffect.ERROR.play((Player) event.getWhoClicked());
            }
        };
    }

    /**
     * Go to previous page
     */
    public static Consumer<InventoryClickEvent> prevPage(AuroraGui gui) {
        return event -> {
            if (gui.hasPrevPage()) {
                gui.prevPage();
            }
        };
    }

    /**
     * Go to previous page with sound
     */
    public static Consumer<InventoryClickEvent> prevPage(AuroraGui gui, SoundEffect sound) {
        return event -> {
            if (gui.hasPrevPage()) {
                sound.play((Player) event.getWhoClicked());
                gui.prevPage();
            } else {
                SoundEffect.ERROR.play((Player) event.getWhoClicked());
            }
        };
    }

    /**
     * Open another GUI
     */
    public static Consumer<InventoryClickEvent> openGui(AuroraGui targetGui) {
        return event -> {
            Player player = (Player) event.getWhoClicked();
            event.getWhoClicked().closeInventory();
            targetGui.open(player);
        };
    }

    /**
     * Open another GUI with sound
     */
    public static Consumer<InventoryClickEvent> openGui(AuroraGui targetGui, SoundEffect sound) {
        return event -> {
            Player player = (Player) event.getWhoClicked();
            sound.play(player);
            event.getWhoClicked().closeInventory();
            targetGui.open(player);
        };
    }

    /**
     * Refresh/update the GUI
     */
    public static Consumer<InventoryClickEvent> refresh(AuroraGui gui) {
        return event -> gui.update();
    }

    /**
     * Refresh with sound
     */
    public static Consumer<InventoryClickEvent> refresh(AuroraGui gui, SoundEffect sound) {
        return event -> {
            sound.play((Player) event.getWhoClicked());
            gui.update();
        };
    }

    /**
     * Send a message to the player
     */
    public static Consumer<InventoryClickEvent> message(String message) {
        return event -> event.getWhoClicked().sendMessage(message);
    }

    /**
     * Execute a command as the player
     */
    public static Consumer<InventoryClickEvent> command(String command) {
        return event -> {
            Player player = (Player) event.getWhoClicked();
            player.performCommand(command);
        };
    }

    /**
     * Execute a command as console
     */
    public static Consumer<InventoryClickEvent> consoleCommand(String command) {
        return event -> {
            Player player = (Player) event.getWhoClicked();
            String cmd = command.replace("%player%", player.getName());
            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), cmd);
        };
    }

    /**
     * Play a sound
     */
    public static Consumer<InventoryClickEvent> sound(SoundEffect sound) {
        return event -> sound.play((Player) event.getWhoClicked());
    }

    /**
     * Combine multiple actions
     */
    public static Consumer<InventoryClickEvent> combine(Consumer<InventoryClickEvent>... actions) {
        return event -> {
            for (Consumer<InventoryClickEvent> action : actions) {
                if (action != null) {
                    action.accept(event);
                }
            }
        };
    }

    /**
     * Do nothing (placeholder)
     */
    public static Consumer<InventoryClickEvent> none() {
        return event -> {};
    }

    /**
     * Builder for complex action chains
     */
    public static class ActionBuilder {
        private Consumer<InventoryClickEvent> action = none();

        public ActionBuilder then(Consumer<InventoryClickEvent> nextAction) {
            Consumer<InventoryClickEvent> current = this.action;
            this.action = event -> {
                current.accept(event);
                nextAction.accept(event);
            };
            return this;
        }

        public ActionBuilder playSound(SoundEffect sound) {
            return then(sound(sound));
        }

        public ActionBuilder sendMessage(String message) {
            return then(message(message));
        }

        public ActionBuilder close() {
            return then(GuiActions.close());
        }

        public ActionBuilder openGui(AuroraGui gui) {
            return then(GuiActions.openGui(gui));
        }

        public ActionBuilder refresh(AuroraGui gui) {
            return then(GuiActions.refresh(gui));
        }

        public ActionBuilder command(String command) {
            return then(GuiActions.command(command));
        }

        public ActionBuilder consoleCommand(String command) {
            return then(GuiActions.consoleCommand(command));
        }

        public Consumer<InventoryClickEvent> build() {
            return action;
        }
    }

    public static ActionBuilder builder() {
        return new ActionBuilder();
    }
}
