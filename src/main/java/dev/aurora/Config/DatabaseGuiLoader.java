package dev.aurora.Config;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Loads GUIs from SQL databases
 * Supports MySQL, SQLite, PostgreSQL
 */
public class DatabaseGuiLoader {
    private final Connection connection;
    private final GuiManager manager;
    private final String guiTable;
    private final String itemTable;
    private boolean initialized;

    /**
     * Creates a new database GUI loader
     *
     * @param connection The database connection
     * @param manager The GUI manager
     */
    public DatabaseGuiLoader(Connection connection, GuiManager manager) {
        this.connection = connection;
        this.manager = manager;
        this.guiTable = "aurora_guis";
        this.itemTable = "aurora_gui_items";
        this.initialized = false;
    }

    /**
     * Initializes the database tables
     *
     * @return true if successful
     */
    public boolean initialize() {
        if (initialized) return true;

        try {
            // Create GUI table
            String createGuiTable = "CREATE TABLE IF NOT EXISTS " + guiTable + " (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) UNIQUE NOT NULL," +
                    "title VARCHAR(255) NOT NULL," +
                    "rows INTEGER NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";

            // Create items table
            String createItemTable = "CREATE TABLE IF NOT EXISTS " + itemTable + " (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "gui_name VARCHAR(255) NOT NULL," +
                    "slot INTEGER NOT NULL," +
                    "material VARCHAR(100) NOT NULL," +
                    "amount INTEGER DEFAULT 1," +
                    "display_name TEXT," +
                    "lore TEXT," +
                    "custom_model_data INTEGER," +
                    "action_type VARCHAR(50)," +
                    "action_data TEXT," +
                    "FOREIGN KEY (gui_name) REFERENCES " + guiTable + "(name) ON DELETE CASCADE," +
                    "UNIQUE (gui_name, slot)" +
                    ")";

            // SQLite uses different syntax
            if (connection.getMetaData().getURL().startsWith("jdbc:sqlite")) {
                createGuiTable = createGuiTable
                    .replace("AUTO_INCREMENT", "AUTOINCREMENT")
                    .replace("TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "TIMESTAMP");
                createItemTable = createItemTable
                    .replace("AUTO_INCREMENT", "AUTOINCREMENT");
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createGuiTable);
                stmt.execute(createItemTable);
            }

            initialized = true;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a GUI to the database
     *
     * @param gui The GUI to save
     * @return true if successful
     */
    public boolean saveGui(AuroraGui gui) {
        if (!initialized && !initialize()) {
            return false;
        }

        try {
            // Save GUI metadata
            String insertGui = "INSERT INTO " + guiTable + " (name, title, rows) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE title = VALUES(title), rows = VALUES(rows)";

            // SQLite uses different syntax
            if (connection.getMetaData().getURL().startsWith("jdbc:sqlite")) {
                insertGui = "INSERT OR REPLACE INTO " + guiTable + " (name, title, rows) VALUES (?, ?, ?)";
            }

            try (PreparedStatement stmt = connection.prepareStatement(insertGui)) {
                stmt.setString(1, gui.getName());
                stmt.setString(2, gui.getTitle());
                stmt.setInt(3, gui.getRows());
                stmt.executeUpdate();
            }

            // Delete existing items
            String deleteItems = "DELETE FROM " + itemTable + " WHERE gui_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteItems)) {
                stmt.setString(1, gui.getName());
                stmt.executeUpdate();
            }

            // Save items
            String insertItem = "INSERT INTO " + itemTable + " " +
                    "(gui_name, slot, material, amount, display_name, lore, custom_model_data) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(insertItem)) {
                for (int slot = 0; slot < gui.getSize(); slot++) {
                    ItemStack item = gui.getInventory().getItem(slot);
                    if (item == null || item.getType() == Material.AIR) continue;

                    stmt.setString(1, gui.getName());
                    stmt.setInt(2, slot);
                    stmt.setString(3, item.getType().name());
                    stmt.setInt(4, item.getAmount());

                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        stmt.setString(5, item.getItemMeta().getDisplayName());
                    } else {
                        stmt.setNull(5, Types.VARCHAR);
                    }

                    if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                        stmt.setString(6, String.join("\n", item.getItemMeta().getLore()));
                    } else {
                        stmt.setNull(6, Types.VARCHAR);
                    }

                    try {
                        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
                            stmt.setInt(7, item.getItemMeta().getCustomModelData());
                        } else {
                            stmt.setNull(7, Types.INTEGER);
                        }
                    } catch (NoSuchMethodError e) {
                        // Custom model data not available in this version
                        stmt.setNull(7, Types.INTEGER);
                    }

                    stmt.executeUpdate();
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a GUI from the database
     *
     * @param name The GUI name
     * @return The loaded GUI, or null if not found
     */
    public AuroraGui loadGui(String name) {
        if (!initialized && !initialize()) {
            return null;
        }

        try {
            // Load GUI metadata
            String selectGui = "SELECT title, rows FROM " + guiTable + " WHERE name = ?";
            String title;
            int rows;

            try (PreparedStatement stmt = connection.prepareStatement(selectGui)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null; // GUI not found
                    }
                    title = rs.getString("title");
                    rows = rs.getInt("rows");
                }
            }

            // Create GUI
            AuroraGui gui = new AuroraGui(name)
                    .title(title)
                    .rows(rows)
                    .register(manager);

            // Load items
            String selectItems = "SELECT slot, material, amount, display_name, lore, custom_model_data " +
                    "FROM " + itemTable + " WHERE gui_name = ? ORDER BY slot";

            try (PreparedStatement stmt = connection.prepareStatement(selectItems)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int slot = rs.getInt("slot");
                        String materialName = rs.getString("material");
                        int amount = rs.getInt("amount");
                        String displayName = rs.getString("display_name");
                        String loreStr = rs.getString("lore");
                        Integer customModelData = rs.getObject("custom_model_data", Integer.class);

                        // Parse material
                        Material material;
                        try {
                            material = Material.valueOf(materialName);
                        } catch (IllegalArgumentException e) {
                            // Material not available in this version, skip
                            continue;
                        }

                        // Build item
                        ItemBuilder builder = new ItemBuilder(material).amount(amount);

                        if (displayName != null) {
                            builder.name(displayName);
                        }

                        if (loreStr != null) {
                            builder.lore(Arrays.asList(loreStr.split("\n")));
                        }

                        if (customModelData != null) {
                            try {
                                builder.customModelData(customModelData);
                            } catch (NoSuchMethodError e) {
                                // Custom model data not available
                            }
                        }

                        gui.setItem(slot, builder.build());
                    }
                }
            }

            return gui;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a GUI asynchronously
     *
     * @param name The GUI name
     * @return CompletableFuture with the GUI
     */
    public CompletableFuture<AuroraGui> loadGuiAsync(String name) {
        return CompletableFuture.supplyAsync(() -> loadGui(name));
    }

    /**
     * Deletes a GUI from the database
     *
     * @param name The GUI name
     * @return true if successful
     */
    public boolean deleteGui(String name) {
        if (!initialized && !initialize()) {
            return false;
        }

        try {
            String delete = "DELETE FROM " + guiTable + " WHERE name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(delete)) {
                stmt.setString(1, name);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lists all GUI names in the database
     *
     * @return List of GUI names
     */
    public List<String> listGuis() {
        if (!initialized && !initialize()) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();

        try {
            String select = "SELECT name FROM " + guiTable + " ORDER BY name";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(select)) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return names;
    }

    /**
     * Checks if a GUI exists in the database
     *
     * @param name The GUI name
     * @return true if exists
     */
    public boolean guiExists(String name) {
        if (!initialized && !initialize()) {
            return false;
        }

        try {
            String select = "SELECT 1 FROM " + guiTable + " WHERE name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(select)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the number of items in a GUI
     *
     * @param name The GUI name
     * @return Item count
     */
    public int getItemCount(String name) {
        if (!initialized && !initialize()) {
            return 0;
        }

        try {
            String select = "SELECT COUNT(*) as count FROM " + itemTable + " WHERE gui_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(select)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Closes the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the GUI manager
     *
     * @return The manager
     */
    public GuiManager getManager() {
        return manager;
    }

    /**
     * Checks if the loader is initialized
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
