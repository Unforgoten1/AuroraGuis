package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Input GUI that prompts players for chat-based text input
 * Useful for getting names, numbers, or other text values
 */
public class InputGui {
    private static final Map<UUID, InputSession> activeSessions = new ConcurrentHashMap<>();
    private static InputListener listener;

    private final GuiManager guiManager;
    private final JavaPlugin plugin;
    private String title = "&6Enter Input";
    private String prompt = "&7Please enter your input in chat";
    private BiConsumer<Player, String> onInput;
    private Runnable onCancel;
    private Predicate<String> validator;
    private String invalidMessage = "&cInvalid input! Please try again.";
    private AuroraGui returnGui;
    private int timeout = 300; // 15 seconds

    private static class InputSession {
        final BiConsumer<Player, String> onInput;
        final Predicate<String> validator;
        final String invalidMessage;
        final AuroraGui returnGui;
        final Runnable onCancel;
        final long startTime;
        final int timeout;

        InputSession(BiConsumer<Player, String> onInput, Predicate<String> validator,
                    String invalidMessage, AuroraGui returnGui, Runnable onCancel,
                    int timeout) {
            this.onInput = onInput;
            this.validator = validator;
            this.invalidMessage = invalidMessage;
            this.returnGui = returnGui;
            this.onCancel = onCancel;
            this.startTime = System.currentTimeMillis();
            this.timeout = timeout * 20; // Convert to ticks
        }

        boolean isExpired() {
            return System.currentTimeMillis() - startTime > timeout * 50L; // 50ms per tick
        }
    }

    private static class InputListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            InputSession session = activeSessions.get(player.getUniqueId());

            if (session != null) {
                event.setCancelled(true);
                String input = event.getMessage();

                // Validate input
                if (session.validator != null && !session.validator.test(input)) {
                    player.sendMessage(session.invalidMessage);
                    return;
                }

                // Remove session
                activeSessions.remove(player.getUniqueId());

                // Execute callback on main thread
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugins()[0], () -> {
                    if (session.onInput != null) {
                        session.onInput.accept(player, input);
                    }

                    // Reopen return GUI if specified
                    if (session.returnGui != null) {
                        session.returnGui.open(player);
                    }
                });
            }
        }
    }

    public InputGui(GuiManager guiManager, JavaPlugin plugin) {
        this.guiManager = guiManager;
        this.plugin = plugin;

        // Register listener if not already registered
        if (listener == null) {
            listener = new InputListener();
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    /**
     * Set the prompt message shown to player
     */
    public InputGui prompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Set the title of the prompt GUI
     */
    public InputGui title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Set the callback when valid input is received
     */
    public InputGui onInput(BiConsumer<Player, String> callback) {
        this.onInput = callback;
        return this;
    }

    /**
     * Set the callback when input is cancelled
     */
    public InputGui onCancel(Runnable callback) {
        this.onCancel = callback;
        return this;
    }

    /**
     * Set input validator (return true if valid)
     */
    public InputGui validator(Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Set message shown when input is invalid
     */
    public InputGui invalidMessage(String message) {
        this.invalidMessage = message;
        return this;
    }

    /**
     * Set GUI to return to after input
     */
    public InputGui returnTo(AuroraGui gui) {
        this.returnGui = gui;
        return this;
    }

    /**
     * Set timeout in seconds (default: 15)
     */
    public InputGui timeout(int seconds) {
        this.timeout = seconds;
        return this;
    }

    /**
     * Open the input prompt for a player
     */
    public void open(Player player) {
        // Create prompt GUI
        AuroraGui promptGui = new AuroraGui("input-" + System.currentTimeMillis())
            .title(title)
            .rows(3)
            .addItem(13, new ItemBuilder(Material.PAPER)
                .name("&e&lENTER INPUT")
                .lore(
                    "&7" + prompt,
                    "",
                    "&7Type your input in chat",
                    "&7or type &ccancel &7to abort"
                )
                .build(), null)
            .register(guiManager);

        // Close GUI and register input session
        player.closeInventory();
        player.sendMessage(prompt);
        player.sendMessage("&7Type &ccancel &7to abort");

        InputSession session = new InputSession(
            onInput, validator, invalidMessage, returnGui, onCancel, timeout
        );
        activeSessions.put(player.getUniqueId(), session);

        // Schedule timeout check
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeSessions.containsKey(player.getUniqueId())) {
                InputSession timeoutSession = activeSessions.remove(player.getUniqueId());
                player.sendMessage("&cInput timeout!");
                if (timeoutSession.onCancel != null) {
                    timeoutSession.onCancel.run();
                }
                if (timeoutSession.returnGui != null) {
                    timeoutSession.returnGui.open(player);
                }
            }
        }, timeout * 20L);
    }

    /**
     * Cancel an active input session
     */
    public static void cancel(Player player) {
        InputSession session = activeSessions.remove(player.getUniqueId());
        if (session != null && session.onCancel != null) {
            session.onCancel.run();
        }
    }

    /**
     * Check if player has an active input session
     */
    public static boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * Cleanup all sessions (call on plugin disable)
     */
    public static void cleanup() {
        activeSessions.clear();
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
    }

    // Common validators
    public static Predicate<String> notEmpty() {
        return s -> s != null && !s.trim().isEmpty();
    }

    public static Predicate<String> numeric() {
        return s -> s != null && s.matches("\\d+");
    }

    public static Predicate<String> alphanumeric() {
        return s -> s != null && s.matches("[a-zA-Z0-9]+");
    }

    public static Predicate<String> minLength(int min) {
        return s -> s != null && s.length() >= min;
    }

    public static Predicate<String> maxLength(int max) {
        return s -> s != null && s.length() <= max;
    }

    public static Predicate<String> lengthBetween(int min, int max) {
        return s -> s != null && s.length() >= min && s.length() <= max;
    }
}
