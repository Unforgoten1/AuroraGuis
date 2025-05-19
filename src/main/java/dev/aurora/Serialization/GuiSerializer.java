package dev.aurora.Serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * Serializes and deserializes AuroraGui instances to/from JSON
 * Limitations: Cannot serialize lambda-based click actions (use action registry instead)
 */
public class GuiSerializer {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    /**
     * Serializes a GUI to a JSON string
     *
     * @param gui The GUI to serialize
     * @return JSON string representation
     * @throws IllegalArgumentException if GUI cannot be serialized
     */
    public static String toJson(AuroraGui gui) {
        if (gui == null) {
            throw new IllegalArgumentException("GUI cannot be null");
        }

        try {
            SerializableGui dto = toSerializable(gui);
            return GSON.toJson(dto);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize GUI: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a GUI from a JSON string
     *
     * @param json The JSON string
     * @param manager The GuiManager to register with (optional, can be null)
     * @return The deserialized GUI
     * @throws IllegalArgumentException if JSON is invalid or cannot be deserialized
     */
    public static AuroraGui fromJson(String json, GuiManager manager) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON cannot be null or empty");
        }

        try {
            SerializableGui dto = GSON.fromJson(json, SerializableGui.class);

            if (!dto.hasValidSchema()) {
                throw new IllegalArgumentException("Unsupported schema version: " + dto.getSchemaVersion());
            }

            return fromSerializable(dto, manager);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize GUI: " + e.getMessage(), e);
        }
    }

    /**
     * Saves a GUI to a file
     *
     * @param gui The GUI to save
     * @param file The target file
     * @throws IOException if file cannot be written
     */
    public static void saveToFile(AuroraGui gui, File file) throws IOException {
        if (gui == null) {
            throw new IllegalArgumentException("GUI cannot be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        String json = toJson(gui);

        // Create parent directories if needed
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }

    /**
     * Loads a GUI from a file
     *
     * @param file The file to load from
     * @param manager The GuiManager to register with (optional, can be null)
     * @return The loaded GUI
     * @throws IOException if file cannot be read
     */
    public static AuroraGui loadFromFile(File file, GuiManager manager) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getPath());
        }

        String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        return fromJson(json, manager);
    }

    /**
     * Converts an AuroraGui to a SerializableGui DTO
     */
    private static SerializableGui toSerializable(AuroraGui gui) throws Exception {
        // Use reflection to access private fields
        Field nameField = AuroraGui.class.getDeclaredField("name");
        nameField.setAccessible(true);
        String name = (String) nameField.get(gui);

        Field titleField = AuroraGui.class.getDeclaredField("title");
        titleField.setAccessible(true);
        String title = (String) titleField.get(gui);

        Field rowsField = AuroraGui.class.getDeclaredField("rows");
        rowsField.setAccessible(true);
        int rows = (int) rowsField.get(gui);

        SerializableGui dto = new SerializableGui(name, title, rows);

        // Serialize inventory items
        Inventory inventory = gui.getInventory();
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && !item.getType().name().equals("AIR")) {
                    String base64 = ItemStackSerializer.toBase64(item);
                    if (base64 != null) {
                        SerializableGui.SerializableItem serializableItem =
                            new SerializableGui.SerializableItem(slot, base64);

                        // Note: Click actions cannot be serialized (lambdas)
                        // Custom actions would need to be registered and referenced by ID
                        serializableItem.setActionType("none");

                        dto.addItem(serializableItem);
                    }
                }
            }
        }

        // Store additional properties
        dto.setProperty("size", String.valueOf(gui.getSize()));

        return dto;
    }

    /**
     * Converts a SerializableGui DTO to an AuroraGui
     */
    private static AuroraGui fromSerializable(SerializableGui dto, GuiManager manager) {
        // Create new GUI
        AuroraGui gui = new AuroraGui(dto.getName())
                .title(dto.getTitle())
                .rows(dto.getRows());

        // Restore items
        for (SerializableGui.SerializableItem serializableItem : dto.getItems()) {
            ItemStack item = ItemStackSerializer.fromBase64(serializableItem.getItemBase64());
            if (item != null) {
                int slot = serializableItem.getSlot();

                // Set item without action (actions can't be serialized)
                gui.setItem(slot, item);

                // Future: Could add action registry here
                // if (serializableItem.getActionType().equals("custom")) {
                //     String actionId = serializableItem.getActionData();
                //     Consumer<InventoryClickEvent> action = ActionRegistry.get(actionId);
                //     gui.setClickAction(slot, action);
                // }
            }
        }

        // Register with manager if provided
        if (manager != null) {
            gui.register(manager);
        }

        return gui;
    }

    /**
     * Creates a deep copy of a GUI using serialization
     *
     * @param gui The GUI to copy
     * @param manager The GuiManager for the copy (optional)
     * @return A deep copy of the GUI
     */
    public static AuroraGui deepCopy(AuroraGui gui, GuiManager manager) {
        String json = toJson(gui);
        return fromJson(json, manager);
    }

    /**
     * Checks if a GUI can be serialized
     *
     * @param gui The GUI to check
     * @return true if serialization is likely to succeed
     */
    public static boolean canSerialize(AuroraGui gui) {
        if (gui == null) return false;

        try {
            toSerializable(gui);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
