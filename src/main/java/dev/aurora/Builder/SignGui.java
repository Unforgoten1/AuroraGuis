package dev.aurora.Builder;

import dev.aurora.Compatibility.SignCompat;
import dev.aurora.Manager.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Sign-based input GUI
 * Uses sign editing for multi-line text input
 * Alternative to chat and anvil input methods
 */
public class SignGui {
    private static final Map<UUID, SignSession> activeSessions = new ConcurrentHashMap<>();
    private static SignListener listener;

    private final GuiManager guiManager;
    private final JavaPlugin plugin;
    private String[] defaultLines = new String[]{"", "", "", ""};
    private BiConsumer<Player, String[]> onComplete;
    private Runnable onCancel;
    private Predicate<String[]> validator;
    private String invalidMessage = "&cInvalid input!";
    private int timeout = 300; // 15 seconds

    private static class SignSession {
        final BiConsumer<Player, String[]> onComplete;
        final Predicate<String[]> validator;
        final String invalidMessage;
        final Runnable onCancel;
        final long startTime;
        final int timeout;

        SignSession(BiConsumer<Player, String[]> onComplete, Predicate<String[]> validator,
                   String invalidMessage, Runnable onCancel, int timeout) {
            this.onComplete = onComplete;
            this.validator = validator;
            this.invalidMessage = invalidMessage;
            this.onCancel = onCancel;
            this.startTime = System.currentTimeMillis();
            this.timeout = timeout;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - startTime > timeout * 50L;
        }
    }

    private static class SignListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onSignChange(SignChangeEvent event) {
            Player player = event.getPlayer();
            SignSession session = activeSessions.get(player.getUniqueId());

            if (session == null) return;

            event.setCancelled(true);

            String[] lines = SignCompat.extractSignText(event.getLines());

            // Validate
            if (session.validator != null && !session.validator.test(lines)) {
                player.sendMessage(session.invalidMessage);
                return;
            }

            // Remove session
            activeSessions.remove(player.getUniqueId());

            // Execute callback on main thread
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugins()[0], () -> {
                if (session.onComplete != null) {
                    session.onComplete.accept(player, lines);
                }
            });
        }
    }

    /**
     * Creates a new SignGui
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     */
    public SignGui(GuiManager guiManager, JavaPlugin plugin) {
        this.guiManager = guiManager;
        this.plugin = plugin;

        // Register listener if not already registered
        if (listener == null) {
            listener = new SignListener();
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    /**
     * Sets a specific line's default text
     *
     * @param index Line index (0-3)
     * @param text Default text for this line
     * @return This instance for chaining
     */
    public SignGui line(int index, String text) {
        if (index >= 0 && index < 4) {
            defaultLines[index] = text != null ? text : "";
        }
        return this;
    }

    /**
     * Sets all default lines at once
     *
     * @param lines Array of default lines (max 4)
     * @return This instance for chaining
     */
    public SignGui lines(String... lines) {
        for (int i = 0; i < Math.min(lines.length, 4); i++) {
            defaultLines[i] = lines[i] != null ? lines[i] : "";
        }
        return this;
    }

    /**
     * Sets the callback when sign is completed
     *
     * @param callback The callback accepting (player, lines array)
     * @return This instance for chaining
     */
    public SignGui onComplete(BiConsumer<Player, String[]> callback) {
        this.onComplete = callback;
        return this;
    }

    /**
     * Sets the callback when sign input is cancelled
     *
     * @param callback The cancel callback
     * @return This instance for chaining
     */
    public SignGui onCancel(Runnable callback) {
        this.onCancel = callback;
        return this;
    }

    /**
     * Sets the validator for sign input
     *
     * @param validator Predicate that returns true if lines are valid
     * @return This instance for chaining
     */
    public SignGui validator(Predicate<String[]> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Sets the message shown when input is invalid
     *
     * @param message The invalid message
     * @return This instance for chaining
     */
    public SignGui invalidMessage(String message) {
        this.invalidMessage = message;
        return this;
    }

    /**
     * Sets the timeout in seconds
     *
     * @param seconds Timeout in seconds (default: 15)
     * @return This instance for chaining
     */
    public SignGui timeout(int seconds) {
        this.timeout = seconds;
        return this;
    }

    /**
     * Opens the sign editor for the player
     * Falls back to InputGui if sign manipulation fails
     *
     * @param player The player to open for
     */
    public void open(Player player) {
        if (!SignCompat.isSupported()) {
            fallbackToInputGui(player);
            return;
        }

        boolean success = SignCompat.openSignEditor(player, defaultLines);

        if (!success) {
            fallbackToInputGui(player);
            return;
        }

        // Create and store session
        SignSession session = new SignSession(
            onComplete, validator, invalidMessage, onCancel, timeout
        );
        activeSessions.put(player.getUniqueId(), session);

        // Schedule timeout check
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            SignSession timeoutSession = activeSessions.remove(player.getUniqueId());
            if (timeoutSession != null && timeoutSession.isExpired()) {
                player.sendMessage("&cInput timeout!");
                if (timeoutSession.onCancel != null) {
                    timeoutSession.onCancel.run();
                }
            }
        }, timeout * 20L);
    }

    /**
     * Fallback to chat-based InputGui (for first line only)
     */
    private void fallbackToInputGui(Player player) {
        new InputGui(guiManager, plugin)
            .prompt("Enter text:")
            .validator(validator != null ? input -> validator.test(new String[]{input, "", "", ""}) : null)
            .invalidMessage(invalidMessage)
            .timeout(timeout / 20)
            .onInput((p, input) -> {
                if (onComplete != null) {
                    onComplete.accept(p, new String[]{input, "", "", ""});
                }
            })
            .onCancel(onCancel)
            .open(player);
    }

    /**
     * Cancels an active sign input session
     *
     * @param player The player whose session to cancel
     */
    public static void cancel(Player player) {
        SignSession session = activeSessions.remove(player.getUniqueId());
        if (session != null && session.onCancel != null) {
            session.onCancel.run();
        }
    }

    /**
     * Checks if player has an active sign input session
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

    // Common validators
    public static Predicate<String[]> notEmpty() {
        return lines -> lines != null && lines.length > 0 &&
            lines[0] != null && !lines[0].trim().isEmpty();
    }

    public static Predicate<String[]> allLinesNotEmpty() {
        return lines -> {
            if (lines == null || lines.length < 4) return false;
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) return false;
            }
            return true;
        };
    }

    public static Predicate<String[]> lineNotEmpty(int index) {
        return lines -> lines != null && lines.length > index &&
            lines[index] != null && !lines[index].trim().isEmpty();
    }
}
