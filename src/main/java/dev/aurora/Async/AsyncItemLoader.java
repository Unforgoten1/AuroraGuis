package dev.aurora.Async;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility for loading items asynchronously and updating GUI on main thread
 * Useful for expensive operations like database queries or API calls
 */
public class AsyncItemLoader {
    private final JavaPlugin plugin;

    public AsyncItemLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load items asynchronously and add to GUI when ready
     * @param gui The GUI to add items to
     * @param itemSupplier Supplier that provides items (runs async)
     * @param onComplete Callback when items are loaded (runs on main thread)
     * @return CompletableFuture for the operation
     */
    public CompletableFuture<Void> loadAndAddItems(
            AuroraGui gui,
            Supplier<List<ItemStack>> itemSupplier,
            Consumer<List<ItemStack>> onComplete) {

        return CompletableFuture.supplyAsync(itemSupplier)
            .thenAcceptAsync(items -> {
                if (onComplete != null) {
                    onComplete.accept(items);
                }
            }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Load single item asynchronously
     * @param gui The GUI to update
     * @param slot The slot to place item in
     * @param itemSupplier Supplier that provides the item (runs async)
     * @param onComplete Callback when item is loaded (runs on main thread)
     * @return CompletableFuture for the operation
     */
    public CompletableFuture<Void> loadAndSetItem(
            AuroraGui gui,
            int slot,
            Supplier<ItemStack> itemSupplier,
            Consumer<ItemStack> onComplete) {

        return CompletableFuture.supplyAsync(itemSupplier)
            .thenAcceptAsync(item -> {
                if (onComplete != null) {
                    onComplete.accept(item);
                }
            }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Reload GUI content asynchronously
     * Clears GUI and loads new content
     * @param gui The GUI to reload
     * @param player The player viewing the GUI (for update)
     * @param contentSupplier Supplier that provides new content (runs async)
     * @return CompletableFuture for the operation
     */
    public CompletableFuture<Void> reloadGui(
            AuroraGui gui,
            Player player,
            Supplier<List<ItemStack>> contentSupplier) {

        return CompletableFuture.supplyAsync(contentSupplier)
            .thenAcceptAsync(items -> {
                gui.clearGui(true);
                gui.addPaginatedItems(items, null);
                gui.update();
            }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Load items with error handling
     * @param gui The GUI to update
     * @param itemSupplier Supplier that provides items (runs async)
     * @param onComplete Callback on success (runs on main thread)
     * @param onError Callback on error (runs on main thread)
     * @return CompletableFuture for the operation
     */
    public CompletableFuture<Void> loadWithErrorHandling(
            AuroraGui gui,
            Supplier<List<ItemStack>> itemSupplier,
            Consumer<List<ItemStack>> onComplete,
            Consumer<Throwable> onError) {

        return CompletableFuture.supplyAsync(itemSupplier)
            .thenAcceptAsync(items -> {
                if (onComplete != null) {
                    onComplete.accept(items);
                }
            }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable))
            .exceptionally(throwable -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (onError != null) {
                        onError.accept(throwable);
                    }
                });
                return null;
            });
    }
}
