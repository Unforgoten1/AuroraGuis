package dev.aurora.Serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for GUI serialization
 * Represents a GUI configuration that can be serialized to/from JSON
 */
public class SerializableGui {
    private String schemaVersion = "1.0";
    private String name;
    private String title;
    private int rows;
    private List<SerializableItem> items;
    private Map<String, String> properties;

    public SerializableGui() {
        this.items = new ArrayList<>();
        this.properties = new HashMap<>();
    }

    public SerializableGui(String name, String title, int rows) {
        this();
        this.name = name;
        this.title = title;
        this.rows = rows;
    }

    /**
     * Represents a serializable item in the GUI
     */
    public static class SerializableItem {
        private int slot;
        private String itemBase64; // Base64-encoded ItemStack with NBT
        private String actionType; // "command", "open_gui", "close", "custom", "none"
        private String actionData; // Command string, GUI name, or custom action ID
        private Map<String, String> metadata;

        public SerializableItem() {
            this.metadata = new HashMap<>();
        }

        public SerializableItem(int slot, String itemBase64) {
            this();
            this.slot = slot;
            this.itemBase64 = itemBase64;
            this.actionType = "none";
        }

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public String getItemBase64() {
            return itemBase64;
        }

        public void setItemBase64(String itemBase64) {
            this.itemBase64 = itemBase64;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public String getActionData() {
            return actionData;
        }

        public void setActionData(String actionData) {
            this.actionData = actionData;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Represents a border configuration
     */
    public static class SerializableBorder {
        private String itemBase64;
        private List<Integer> slots;

        public SerializableBorder() {
            this.slots = new ArrayList<>();
        }

        public String getItemBase64() {
            return itemBase64;
        }

        public void setItemBase64(String itemBase64) {
            this.itemBase64 = itemBase64;
        }

        public List<Integer> getSlots() {
            return slots;
        }

        public void setSlots(List<Integer> slots) {
            this.slots = slots;
        }
    }

    // Getters and setters

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<SerializableItem> getItems() {
        return items;
    }

    public void setItems(List<SerializableItem> items) {
        this.items = items;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Adds an item to the serialized GUI
     *
     * @param item The item to add
     */
    public void addItem(SerializableItem item) {
        this.items.add(item);
    }

    /**
     * Adds a property to the GUI
     *
     * @param key The property key
     * @param value The property value
     */
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    /**
     * Gets a property value
     *
     * @param key The property key
     * @return The property value, or null if not found
     */
    public String getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * Checks if this DTO has a valid schema version
     *
     * @return true if the schema version is supported
     */
    public boolean hasValidSchema() {
        return schemaVersion != null && schemaVersion.equals("1.0");
    }
}
