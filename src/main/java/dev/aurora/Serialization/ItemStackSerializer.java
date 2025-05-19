package dev.aurora.Serialization;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Utility for serializing and deserializing ItemStacks to/from Base64 strings
 * Preserves NBT data and all item properties
 */
public class ItemStackSerializer {

    /**
     * Serializes an ItemStack to a Base64 string
     *
     * @param item The ItemStack to serialize
     * @return Base64-encoded string, or null if serialization fails
     */
    public static String toBase64(ItemStack item) {
        if (item == null) {
            return null;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deserializes an ItemStack from a Base64 string
     *
     * @param base64 The Base64-encoded ItemStack
     * @return The deserialized ItemStack, or null if deserialization fails
     */
    public static ItemStack fromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serializes an array of ItemStacks to Base64 strings
     *
     * @param items The ItemStack array
     * @return Array of Base64 strings (null entries for null items)
     */
    public static String[] toBase64Array(ItemStack[] items) {
        if (items == null) {
            return new String[0];
        }

        String[] result = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            result[i] = toBase64(items[i]);
        }
        return result;
    }

    /**
     * Deserializes an array of ItemStacks from Base64 strings
     *
     * @param base64Array The Base64 string array
     * @return Array of ItemStacks (null entries for null/invalid strings)
     */
    public static ItemStack[] fromBase64Array(String[] base64Array) {
        if (base64Array == null) {
            return new ItemStack[0];
        }

        ItemStack[] result = new ItemStack[base64Array.length];
        for (int i = 0; i < base64Array.length; i++) {
            result[i] = fromBase64(base64Array[i]);
        }
        return result;
    }
}
