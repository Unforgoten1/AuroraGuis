package dev.aurora.Config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for YAML configurations using SoftReferences
 * Allows garbage collection under memory pressure while providing fast access
 */
public class ConfigCache {
    private final Map<String, CachedConfig> cache;
    private boolean enabled;

    /**
     * Represents a cached configuration file
     */
    private static class CachedConfig {
        final File file;
        final long lastModified;
        final SoftReference<YamlConfiguration> configRef;

        CachedConfig(File file, YamlConfiguration config) {
            this.file = file;
            this.lastModified = file.lastModified();
            this.configRef = new SoftReference<>(config);
        }

        boolean isStale() {
            return file.lastModified() != lastModified;
        }

        YamlConfiguration get() {
            return configRef.get();
        }
    }

    /**
     * Creates a new configuration cache
     */
    public ConfigCache() {
        this.cache = new ConcurrentHashMap<>();
        this.enabled = true;
    }

    /**
     * Gets a configuration from cache or loads it from file
     *
     * @param file The configuration file
     * @return The loaded configuration
     */
    public YamlConfiguration get(File file) {
        if (!enabled) {
            return YamlConfiguration.loadConfiguration(file);
        }

        String path = file.getAbsolutePath();
        CachedConfig cached = cache.get(path);

        // Check if cached and still valid
        if (cached != null && !cached.isStale()) {
            YamlConfiguration config = cached.get();
            if (config != null) {
                return config;
            }
        }

        // Load from file
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        cache.put(path, new CachedConfig(file, config));

        return config;
    }

    /**
     * Invalidates a specific file in the cache
     *
     * @param file The file to invalidate
     */
    public void invalidate(File file) {
        cache.remove(file.getAbsolutePath());
    }

    /**
     * Invalidates all cached configurations
     */
    public void invalidateAll() {
        cache.clear();
    }

    /**
     * Cleans up entries that have been garbage collected
     *
     * @return Number of entries cleaned up
     */
    public int cleanup() {
        int removed = 0;
        for (Map.Entry<String, CachedConfig> entry : cache.entrySet()) {
            if (entry.getValue().get() == null || entry.getValue().isStale()) {
                cache.remove(entry.getKey());
                removed++;
            }
        }
        return removed;
    }

    /**
     * Gets the number of cached configurations
     *
     * @return Number of cached configs
     */
    public int size() {
        cleanup();
        return cache.size();
    }

    /**
     * Checks if a file is cached
     *
     * @param file The file to check
     * @return true if cached and valid
     */
    public boolean isCached(File file) {
        CachedConfig cached = cache.get(file.getAbsolutePath());
        return cached != null && !cached.isStale() && cached.get() != null;
    }

    /**
     * Enables or disables the cache
     *
     * @param enabled true to enable caching
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            invalidateAll();
        }
    }

    /**
     * Checks if the cache is enabled
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Preloads a configuration into the cache
     *
     * @param file The file to preload
     */
    public void preload(File file) {
        if (enabled) {
            get(file);
        }
    }

    /**
     * Preloads multiple configurations
     *
     * @param files The files to preload
     */
    public void preloadAll(File... files) {
        for (File file : files) {
            preload(file);
        }
    }

    /**
     * Gets cache statistics
     *
     * @return Human-readable statistics string
     */
    public String getStats() {
        cleanup();
        return String.format(
            "ConfigCache Stats: %d configs cached, %s",
            size(),
            enabled ? "ENABLED" : "DISABLED"
        );
    }

    /**
     * Gets memory usage estimate in bytes
     * Note: This is a rough estimate
     *
     * @return Estimated memory usage
     */
    public long estimateMemoryUsage() {
        long total = 0;
        for (CachedConfig cached : cache.values()) {
            YamlConfiguration config = cached.get();
            if (config != null) {
                // Rough estimate: 1KB per config + 100 bytes per key
                total += 1024 + (config.getKeys(true).size() * 100);
            }
        }
        return total;
    }
}
