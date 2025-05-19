package dev.aurora.Utilities.Items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Object pool for frequently created ItemStacks
 * Reduces garbage collection pressure by reusing ItemStack objects
 */
public class ItemStackPool {
    private static final int DEFAULT_POOL_SIZE = 50;
    private static final Map<PoolKey, BlockingQueue<ItemStack>> pools = new ConcurrentHashMap<>();
    private static boolean enabled = true;

    /**
     * Key for identifying pooled ItemStacks
     */
    private static class PoolKey {
        final Material material;
        final int amount;
        final short durability;

        PoolKey(Material material, int amount, short durability) {
            this.material = material;
            this.amount = amount;
            this.durability = durability;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PoolKey)) return false;
            PoolKey poolKey = (PoolKey) o;
            return amount == poolKey.amount &&
                   durability == poolKey.durability &&
                   material == poolKey.material;
        }

        @Override
        public int hashCode() {
            int result = material.hashCode();
            result = 31 * result + amount;
            result = 31 * result + (int) durability;
            return result;
        }
    }

    /**
     * Acquires an ItemStack from the pool or creates a new one
     *
     * @param material The material
     * @param amount The stack amount
     * @return An ItemStack (pooled or new)
     */
    public static ItemStack acquire(Material material, int amount) {
        return acquire(material, amount, (short) 0);
    }

    /**
     * Acquires an ItemStack from the pool or creates a new one
     *
     * @param material The material
     * @param amount The stack amount
     * @param durability The durability/data value
     * @return An ItemStack (pooled or new)
     */
    public static ItemStack acquire(Material material, int amount, short durability) {
        if (!enabled) {
            return new ItemStack(material, amount, durability);
        }

        PoolKey key = new PoolKey(material, amount, durability);
        BlockingQueue<ItemStack> pool = pools.get(key);

        if (pool != null) {
            ItemStack item = pool.poll();
            if (item != null) {
                return item.clone(); // Return a clone to avoid shared references
            }
        }

        // Pool miss - create new
        return new ItemStack(material, amount, durability);
    }

    /**
     * Returns an ItemStack to the pool for reuse
     *
     * @param item The ItemStack to return
     */
    public static void release(ItemStack item) {
        if (!enabled || item == null) {
            return;
        }

        // Only pool simple items (no custom meta, enchantments, etc.)
        if (item.hasItemMeta() || !item.getEnchantments().isEmpty()) {
            return;
        }

        PoolKey key = new PoolKey(item.getType(), item.getAmount(), item.getDurability());
        BlockingQueue<ItemStack> pool = pools.computeIfAbsent(
            key,
            k -> new ArrayBlockingQueue<>(DEFAULT_POOL_SIZE)
        );

        // Try to add to pool (silently fails if full)
        pool.offer(item.clone());
    }

    /**
     * Pre-warms the pool with commonly used items
     *
     * @param material The material to pre-create
     * @param amount The stack amount
     * @param count Number of instances to create
     */
    public static void warmUp(Material material, int amount, int count) {
        if (!enabled) return;

        PoolKey key = new PoolKey(material, amount, (short) 0);
        BlockingQueue<ItemStack> pool = pools.computeIfAbsent(
            key,
            k -> new ArrayBlockingQueue<>(DEFAULT_POOL_SIZE)
        );

        for (int i = 0; i < count && i < DEFAULT_POOL_SIZE; i++) {
            pool.offer(new ItemStack(material, amount));
        }
    }

    /**
     * Pre-warms the pool with common GUI items
     */
    public static void warmUpCommonGuiItems() {
        // Glass panes (common borders)
        warmUp(Material.GRAY_STAINED_GLASS_PANE, 1, 20);
        warmUp(Material.BLACK_STAINED_GLASS_PANE, 1, 20);
        warmUp(Material.WHITE_STAINED_GLASS_PANE, 1, 20);

        // Common items
        warmUp(Material.ARROW, 1, 10);
        warmUp(Material.BARRIER, 1, 10);
        warmUp(Material.PAPER, 1, 10);
    }

    /**
     * Clears all pools and releases memory
     */
    public static void clearAll() {
        pools.clear();
    }

    /**
     * Clears a specific pool
     *
     * @param material The material
     * @param amount The stack amount
     */
    public static void clear(Material material, int amount) {
        PoolKey key = new PoolKey(material, amount, (short) 0);
        pools.remove(key);
    }

    /**
     * Gets the number of pooled ItemStacks for a material
     *
     * @param material The material
     * @param amount The stack amount
     * @return Number of pooled items
     */
    public static int getPoolSize(Material material, int amount) {
        PoolKey key = new PoolKey(material, amount, (short) 0);
        BlockingQueue<ItemStack> pool = pools.get(key);
        return pool != null ? pool.size() : 0;
    }

    /**
     * Gets total number of pooled ItemStacks across all pools
     *
     * @return Total pooled items
     */
    public static int getTotalPooled() {
        return pools.values().stream()
                .mapToInt(BlockingQueue::size)
                .sum();
    }

    /**
     * Gets the number of different pools
     *
     * @return Number of pools
     */
    public static int getPoolCount() {
        return pools.size();
    }

    /**
     * Enables or disables the pooling system
     *
     * @param enabled true to enable pooling
     */
    public static void setEnabled(boolean enabled) {
        ItemStackPool.enabled = enabled;
        if (!enabled) {
            clearAll();
        }
    }

    /**
     * Checks if pooling is enabled
     *
     * @return true if enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets statistics about the pool
     *
     * @return Human-readable statistics string
     */
    public static String getStats() {
        return String.format(
            "ItemStackPool Stats: %d pools, %d total items pooled, %s",
            getPoolCount(),
            getTotalPooled(),
            enabled ? "ENABLED" : "DISABLED"
        );
    }
}
