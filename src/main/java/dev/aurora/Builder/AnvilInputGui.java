package dev.aurora.Builder;

import dev.aurora.Compatibility.AnvilCompat;
import dev.aurora.Manager.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Anvil-based input GUI for better UX than chat input
 * Uses anvil rename field for text input
 * Falls back to InputGui if anvil manipulation fails
 */
public class AnvilInputGui {
    private static final Map<UUID, AnvilSession> activeSessions = new ConcurrentHashMap<>();
    private static AnvilListener listener;

    private final GuiManager guiManager;
    private final JavaPlugin plugin;
    private String title = "Enter Text";
    private String defaultText = "";
    private BiConsumer<Player, String> onComplete;
    private Runnable onCancel;
    private Predicate<String> validator;
    private String invalidMessage = "&cInvalid input!";
    private int timeout = 300; // 15 seconds in ticks

    private static class AnvilSession {
        final BiConsumer<Player, String> onComplete;
        final Predicate<String> validator;
        final String invalidMessage;
        final Runnable onCancel;
        final long startTime;
        final int timeout;
        final Inventory anvilInventory;

        AnvilSession(BiConsumer<Player, String> onComplete, Predicate<String> validator,
                    String invalidMessage, Runnable onCancel, int timeout, Inventory anvilInventory) {
            this.onComplete = onComplete;
            this.validator = validator;
            this.invalidMessage = invalidMessage;
            this.onCancel = onCancel;
            this.startTime = System.currentTimeMillis();
            this.timeout = timeout;
            this.anvilInventory = anvilInventory;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - startTime > timeout * 50L;
        }
    }

    private static class AnvilListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            if (event.getInventory().getType() != InventoryType.ANVIL) return;

            Player player = (Player) event.getWhoClicked();
            AnvilSession session = activeSessions.get(player.getUniqueId());

            if (session == null) return;
            if (!event.getInventory().equals(session.anvilInventory)) return;

            // Only process clicks on the result slot (slot 2)
            if (event.getRawSlot() != 2) return;

            event.setCancelled(true);

            String text = AnvilCompat.getAnvilText(event.getInventory());
            if (text == null || text.isEmpty()) {
                player.sendMessage(session.invalidMessage);
                return;
            }

            // Validate
            if (session.validator != null && !session.validator.test(text)) {
                player.sendMessage(session.invalidMessage);
                return;
            }

            // Remove session
            activeSessions.remove(player.getUniqueId());

            // Close inventory
            player.closeInventory();

            // Execute callback on main thread
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugins()[0], () -> {
                if (session.onComplete != null) {
                    session.onComplete.accept(player, text);
                }
            });
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!(event.getPlayer() instanceof Player)) return;
            if (event.getInventory().getType() != InventoryType.ANVIL) return;

            Player player = (Player) event.getPlayer();
            AnvilSession session = activeSessions.remove(player.getUniqueId());

            if (session != null && session.onCancel != null) {
                // Delay to avoid conflicts with click event
                Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugins()[0],
                    () -> {
                        if (!activeSessions.containsKey(player.getUniqueId())) {
                            session.onCancel.run();
                        }
                    },
                    1L
                );
            }
        }
    }

    /**
     * Creates a new AnvilInputGui
     *
     * @param guiManager The GUI manager instance
     * @param plugin The plugin instance
     */
    public AnvilInputGui(GuiManager guiManager, JavaPlugin plugin) {
        this.guiManager = guiManager;
        this.plugin = plugin;

        // Register listener if not already registered
        if (listener == null) {
            listener = new AnvilListener();
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    /**
     * Sets the title of the anvil GUI
     *
     * @param title The title to display
     * @return This instance for chaining
     */
    public AnvilInputGui title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the default text in the rename field
     *
     * @param text The default text
     * @return This instance for chaining
     */
    public AnvilInputGui defaultText(String text) {
        this.defaultText = text;
        return this;
    }

    /**
     * Sets the callback when input is completed
     *
     * @param callback The callback accepting (player, input text)
     * @return This instance for chaining
     */
    public AnvilInputGui onComplete(BiConsumer<Player, String> callback) {
        this.onComplete = callback;
        return this;
    }

    /**
     * Sets the callback when input is cancelled
     *
     * @param callback The cancel callback
     * @return This instance for chaining
     */
    public AnvilInputGui onCancel(Runnable callback) {
        this.onCancel = callback;
        return this;
    }

    /**
     * Sets the input validator
     *
     * @param validator Predicate that returns true if input is valid
     * @return This instance for chaining
     */
    public AnvilInputGui validator(Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Sets the message shown when input is invalid
     *
     * @param message The invalid message
     * @return This instance for chaining
     */
    public AnvilInputGui invalidMessage(String message) {
        this.invalidMessage = message;
        return this;
    }

    /**
     * Sets the timeout in seconds
     *
     * @param seconds Timeout in seconds (default: 15)
     * @return This instance for chaining
     */
    public AnvilInputGui timeout(int seconds) {
        this.timeout = seconds;
        return this;
    }

    /**
     * Opens the anvil input GUI for the player
     * Falls back to InputGui if anvil manipulation fails
     *
     * @param player The player to open for
     */
    public void open(Player player) {
        if (!AnvilCompat.isSupported()) {
            // Fallback to chat-based input
            fallbackToChatInput(player);
            return;
        }

        Inventory anvil = AnvilCompat.openAnvil(player, title, defaultText);

        if (anvil == null) {
            // Fallback to chat-based input
            fallbackToChatInput(player);
            return;
        }

        // Create and store session
        AnvilSession session = new AnvilSession(
            onComplete, validator, invalidMessage, onCancel, timeout, anvil
        );
        activeSessions.put(player.getUniqueId(), session);

        // Schedule timeout check
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AnvilSession timeoutSession = activeSessions.remove(player.getUniqueId());
            if (timeoutSession != null && timeoutSession.isExpired()) {
                player.closeInventory();
                player.sendMessage("&cInput timeout!");
                if (timeoutSession.onCancel != null) {
                    timeoutSession.onCancel.run();
                }
            }
        }, timeout * 20L);
    }

    /**
     * Fallback to chat-based InputGui
     */
    private void fallbackToChatInput(Player player) {
        new InputGui(guiManager, plugin)
            .title(title)
            .prompt(title)
            .validator(validator)
            .invalidMessage(invalidMessage)
            .timeout(timeout / 20) // Convert ticks to seconds
            .onInput(onComplete)
            .onCancel(onCancel)
            .open(player);
    }

    /**
     * Cancels an active anvil input session
     *
     * @param player The player whose session to cancel
     */
    public static void cancel(Player player) {
        AnvilSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            player.closeInventory();
            if (session.onCancel != null) {
                session.onCancel.run();
            }
        }
    }

    /**
     * Checks if player has an active anvil input session
     *
     * @param player The player to check
     * @return true if session exists
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

    // Common validators (reuse from InputGui)
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
