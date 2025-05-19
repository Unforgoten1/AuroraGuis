package dev.aurora.Manager;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized animation scheduler that runs all animations in a single task
 * Reduces scheduler overhead from O(n) tasks to O(1) where n = number of animations
 */
public class AnimationScheduler {
    private final JavaPlugin plugin;
    private final Map<AuroraGui, Set<Integer>> guiAnimations;
    private BukkitTask masterTask;
    private long tickCounter;

    public AnimationScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.guiAnimations = new ConcurrentHashMap<>();
        this.tickCounter = 0;
        startMasterTask();
    }

    /**
     * Start the master animation task that runs every tick
     */
    private void startMasterTask() {
        masterTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            tickCounter++;

            for (Map.Entry<AuroraGui, Set<Integer>> entry : guiAnimations.entrySet()) {
                AuroraGui gui = entry.getKey();
                Set<Integer> slots = entry.getValue();

                for (Integer slot : slots) {
                    gui.tickAnimation(slot, tickCounter);
                }
            }
        }, 0L, 1L); // Run every tick
    }

    /**
     * Register an animation slot for a GUI
     * @param gui The GUI containing the animation
     * @param slot The slot with the animation
     */
    public void registerAnimation(AuroraGui gui, int slot) {
        guiAnimations.computeIfAbsent(gui, k -> ConcurrentHashMap.newKeySet()).add(slot);
    }

    /**
     * Unregister a specific animation slot
     * @param gui The GUI containing the animation
     * @param slot The slot to unregister
     */
    public void unregisterAnimation(AuroraGui gui, int slot) {
        Set<Integer> slots = guiAnimations.get(gui);
        if (slots != null) {
            slots.remove(slot);
            if (slots.isEmpty()) {
                guiAnimations.remove(gui);
            }
        }
    }

    /**
     * Unregister all animations for a GUI
     * @param gui The GUI to unregister
     */
    public void unregisterAll(AuroraGui gui) {
        guiAnimations.remove(gui);
    }

    /**
     * Shutdown the animation scheduler
     * Should be called when plugin is disabled
     */
    public void shutdown() {
        if (masterTask != null && !masterTask.isCancelled()) {
            masterTask.cancel();
        }
        guiAnimations.clear();
    }

    /**
     * Get current tick counter
     * @return The current tick count
     */
    public long getTickCounter() {
        return tickCounter;
    }
}
