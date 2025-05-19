package dev.aurora.Compatibility;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cross-version NBT and Persistent Data Container compatibility
 * Handles data storage across 1.8-1.21.1
 */
public class NBTCompat {
    private static final boolean HAS_PDC = ServerVersion.getInstance().hasPersistentData();
    private static final Map<UUID, Map<String, Object>> FALLBACK_STORAGE = new HashMap<>();

    /**
     * Set a string value on an item
     * Uses PDC on 1.14+, fallback storage on older versions
     */
    public static ItemStack setString(ItemStack item, String key, String value) {
        if (item == null || key == null) return item;

        if (HAS_PDC) {
            try {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // Use PDC for 1.14+
                    Object pdc = meta.getClass().getMethod("getPersistentDataContainer").invoke(meta);
                    Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                    Object nsKey = namespacedKeyClass.getConstructor(String.class, String.class)
                        .newInstance("auroragui", key);

                    Class<?> pdcClass = pdc.getClass();
                    Class<?> dataTypeClass = Class.forName("org.bukkit.persistence.PersistentDataType");
                    Object stringType = dataTypeClass.getField("STRING").get(null);

                    Method setMethod = pdcClass.getMethod("set", namespacedKeyClass, dataTypeClass, Object.class);
                    setMethod.invoke(pdc, nsKey, stringType, value);

                    item.setItemMeta(meta);
                }
            } catch (Exception e) {
                // Fallback if PDC fails
                useFallback(item, key, value);
            }
        } else {
            useFallback(item, key, value);
        }

        return item;
    }

    /**
     * Get a string value from an item
     */
    public static String getString(ItemStack item, String key) {
        if (item == null || key == null) return null;

        if (HAS_PDC) {
            try {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    Object pdc = meta.getClass().getMethod("getPersistentDataContainer").invoke(meta);
                    Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                    Object nsKey = namespacedKeyClass.getConstructor(String.class, String.class)
                        .newInstance("auroragui", key);

                    Class<?> pdcClass = pdc.getClass();
                    Class<?> dataTypeClass = Class.forName("org.bukkit.persistence.PersistentDataType");
                    Object stringType = dataTypeClass.getField("STRING").get(null);

                    Method getMethod = pdcClass.getMethod("get", namespacedKeyClass, dataTypeClass);
                    return (String) getMethod.invoke(pdc, nsKey, stringType);
                }
            } catch (Exception e) {
                // Fallback
                return getFallback(item, key);
            }
        }

        return getFallback(item, key);
    }

    /**
     * Check if item has a key
     */
    public static boolean hasKey(ItemStack item, String key) {
        if (item == null || key == null) return false;

        if (HAS_PDC) {
            try {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    Object pdc = meta.getClass().getMethod("getPersistentDataContainer").invoke(meta);
                    Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                    Object nsKey = namespacedKeyClass.getConstructor(String.class, String.class)
                        .newInstance("auroragui", key);

                    Class<?> pdcClass = pdc.getClass();
                    Class<?> dataTypeClass = Class.forName("org.bukkit.persistence.PersistentDataType");
                    Object stringType = dataTypeClass.getField("STRING").get(null);

                    Method hasMethod = pdcClass.getMethod("has", namespacedKeyClass, dataTypeClass);
                    return (Boolean) hasMethod.invoke(pdc, nsKey, stringType);
                }
            } catch (Exception e) {
                // Fallback
                return hasFallback(item, key);
            }
        }

        return hasFallback(item, key);
    }

    /**
     * Remove a key from item
     */
    public static ItemStack remove(ItemStack item, String key) {
        if (item == null || key == null) return item;

        if (HAS_PDC) {
            try {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    Object pdc = meta.getClass().getMethod("getPersistentDataContainer").invoke(meta);
                    Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                    Object nsKey = namespacedKeyClass.getConstructor(String.class, String.class)
                        .newInstance("auroragui", key);

                    Method removeMethod = pdc.getClass().getMethod("remove", namespacedKeyClass);
                    removeMethod.invoke(pdc, nsKey);

                    item.setItemMeta(meta);
                }
            } catch (Exception e) {
                // Fallback
                removeFallback(item, key);
            }
        } else {
            removeFallback(item, key);
        }

        return item;
    }

    // Fallback storage methods for pre-1.14
    private static void useFallback(ItemStack item, String key, Object value) {
        UUID id = getItemId(item);
        FALLBACK_STORAGE.computeIfAbsent(id, k -> new HashMap<>()).put(key, value);
    }

    private static String getFallback(ItemStack item, String key) {
        UUID id = getItemId(item);
        Map<String, Object> data = FALLBACK_STORAGE.get(id);
        if (data != null) {
            Object value = data.get(key);
            return value != null ? value.toString() : null;
        }
        return null;
    }

    private static boolean hasFallback(ItemStack item, String key) {
        UUID id = getItemId(item);
        Map<String, Object> data = FALLBACK_STORAGE.get(id);
        return data != null && data.containsKey(key);
    }

    private static void removeFallback(ItemStack item, String key) {
        UUID id = getItemId(item);
        Map<String, Object> data = FALLBACK_STORAGE.get(id);
        if (data != null) {
            data.remove(key);
            if (data.isEmpty()) {
                FALLBACK_STORAGE.remove(id);
            }
        }
    }

    private static UUID getItemId(ItemStack item) {
        // Generate pseudo-unique ID based on item properties
        // This is not perfect but works for most cases
        int hash = item.getType().hashCode();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            hash = hash * 31 + item.getItemMeta().getDisplayName().hashCode();
        }
        return new UUID(hash, System.identityHashCode(item));
    }

    /**
     * Clear fallback storage (call on plugin disable)
     */
    public static void clearFallbackStorage() {
        FALLBACK_STORAGE.clear();
    }
}
