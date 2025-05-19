package dev.aurora.ResourcePack;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for validating custom model references and detecting missing textures.
 * <p>
 * TextureValidator scans GUIs for CustomModelData usage and cross-references
 * them with the ModelRegistry to identify undefined or potentially missing textures.
 * </p>
 *
 * @since 1.1.0
 */
public class TextureValidator {

    private static Logger logger = Logger.getLogger("AuroraGuis");

    /**
     * Sets the logger for validator operations.
     *
     * @param newLogger The logger to use
     */
    public static void setLogger(Logger newLogger) {
        logger = newLogger;
    }

    /**
     * Validates if a CustomModelData value is registered in the ModelRegistry.
     *
     * @param material The base material
     * @param customModelData The CustomModelData value
     * @return true if valid/registered, false if not found in registry
     */
    public static boolean isValid(Material material, int customModelData) {
        if (material == null || customModelData == 0) {
            return true; // No custom model data - always valid
        }

        // Check if this CustomModelData is registered
        for (ModelData model : ModelRegistry.getAllModels()) {
            if (model.getBaseMaterial() == material &&
                model.getCustomModelData() == customModelData) {
                return true;
            }
        }

        return false;
    }

    /**
     * Scans a GUI for items with CustomModelData and identifies missing textures.
     * <p>
     * A texture is considered "missing" if it uses CustomModelData but is not
     * registered in the ModelRegistry.
     * </p>
     *
     * @param gui The GUI to scan
     * @return List of warning messages about missing textures
     */
    public static List<String> findMissingTextures(AuroraGui gui) {
        List<String> warnings = new ArrayList<>();

        if (gui == null) {
            return warnings;
        }

        // Get all items from the GUI
        ItemStack[] contents = gui.getInventory().getContents();

        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            // Check for CustomModelData
            try {
                Integer customModelData = meta.getCustomModelData();
                if (customModelData != null && customModelData != 0) {
                    // Check if registered
                    if (!isValid(item.getType(), customModelData)) {
                        String warning = "Slot " + slot + ": " +
                                       item.getType() + " uses CustomModelData " + customModelData +
                                       " but is not registered in ModelRegistry";
                        warnings.add(warning);
                    }
                }
            } catch (NoSuchMethodError e) {
                // CustomModelData not available in this version - ignore
            }
        }

        return warnings;
    }

    /**
     * Validates all items in a GUI and logs warnings.
     *
     * @param gui The GUI to validate
     * @param guiName The GUI name for logging
     * @return Number of validation warnings found
     */
    public static int validateGui(AuroraGui gui, String guiName) {
        List<String> warnings = findMissingTextures(gui);

        if (!warnings.isEmpty()) {
            logger.warning("GUI '" + guiName + "' has " + warnings.size() + " potential texture issue(s):");
            for (String warning : warnings) {
                logger.warning("  - " + warning);
            }
        } else {
            logger.fine("GUI '" + guiName + "' passed texture validation");
        }

        return warnings.size();
    }

    /**
     * Scans all items with CustomModelData and generates a report.
     *
     * @param gui The GUI to scan
     * @return Map of CustomModelData values to their usage count
     */
    public static Map<String, Integer> analyzeCustomModels(AuroraGui gui) {
        Map<String, Integer> usage = new HashMap<>();

        if (gui == null) {
            return usage;
        }

        ItemStack[] contents = gui.getInventory().getContents();

        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            try {
                Integer customModelData = meta.getCustomModelData();
                if (customModelData != null && customModelData != 0) {
                    String key = item.getType() + ":" + customModelData;
                    usage.put(key, usage.getOrDefault(key, 0) + 1);
                }
            } catch (NoSuchMethodError e) {
                // CustomModelData not available - ignore
            }
        }

        return usage;
    }

    /**
     * Generates a comprehensive resource pack integration guide.
     * <p>
     * Creates a markdown file with:
     * - Registered models
     * - Required CustomModelData values
     * - Example resource pack structure
     * - Integration instructions
     * </p>
     *
     * @param outputFile The file to write the guide to
     * @return true if generated successfully, false otherwise
     */
    public static boolean generateGuide(File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("# Resource Pack Integration Guide\n\n");
            writer.write("Generated for AuroraGuis resource pack integration.\n\n");

            // Registered Models Section
            writer.write("## Registered Models\n\n");
            Collection<ModelData> models = ModelRegistry.getAllModels();

            if (models.isEmpty()) {
                writer.write("*No models registered yet.*\n\n");
            } else {
                writer.write("Total registered models: " + models.size() + "\n\n");
                writer.write("| ID | Base Material | CustomModelData | Display Name |\n");
                writer.write("|---|---|---|---|\n");

                List<ModelData> sortedModels = new ArrayList<>(models);
                sortedModels.sort(Comparator.comparing(ModelData::getId));

                for (ModelData model : sortedModels) {
                    writer.write("| " + model.getId() +
                               " | " + model.getBaseMaterial() +
                               " | " + model.getCustomModelData() +
                               " | " + (model.getDisplayName() != null ? model.getDisplayName() : "-") +
                               " |\n");
                }
                writer.write("\n");
            }

            // Resource Pack Structure
            writer.write("## Resource Pack Structure\n\n");
            writer.write("Your resource pack should have the following structure:\n\n");
            writer.write("```\n");
            writer.write("custompack.zip\n");
            writer.write("├── pack.mcmeta\n");
            writer.write("└── assets/\n");
            writer.write("    └── minecraft/\n");
            writer.write("        ├── models/\n");
            writer.write("        │   └── item/\n");
            writer.write("        │       ├── emerald.json (for custom models)\n");
            writer.write("        │       └── ...\n");
            writer.write("        └── textures/\n");
            writer.write("            └── item/\n");
            writer.write("                ├── ruby.png\n");
            writer.write("                └── ...\n");
            writer.write("```\n\n");

            // Example Model JSON
            writer.write("## Example Model Definition\n\n");
            writer.write("For a custom model with CustomModelData=1001 on EMERALD:\n\n");
            writer.write("**File:** `assets/minecraft/models/item/emerald.json`\n\n");
            writer.write("```json\n");
            writer.write("{\n");
            writer.write("  \"parent\": \"item/generated\",\n");
            writer.write("  \"textures\": {\n");
            writer.write("    \"layer0\": \"item/emerald\"\n");
            writer.write("  },\n");
            writer.write("  \"overrides\": [\n");
            writer.write("    {\n");
            writer.write("      \"predicate\": {\"custom_model_data\": 1001},\n");
            writer.write("      \"model\": \"item/ruby\"\n");
            writer.write("    }\n");
            writer.write("  ]\n");
            writer.write("}\n");
            writer.write("```\n\n");

            // Integration Steps
            writer.write("## Integration Steps\n\n");
            writer.write("1. Create your resource pack with the models listed above\n");
            writer.write("2. Upload the pack to a file host (e.g., GitHub releases, Dropbox)\n");
            writer.write("3. Generate SHA-1 hash: `sha1sum custompack.zip`\n");
            writer.write("4. Configure `resource-pack.yml`:\n");
            writer.write("   ```yaml\n");
            writer.write("   resource-pack:\n");
            writer.write("     enabled: true\n");
            writer.write("     url: \"https://your-host.com/custompack.zip\"\n");
            writer.write("     hash: \"your-sha1-hash-here\"\n");
            writer.write("     required: false\n");
            writer.write("   ```\n");
            writer.write("5. Restart your server\n\n");

            // Validation Tips
            writer.write("## Validation Tips\n\n");
            writer.write("- Use TextureValidator.validateGui() before deploying\n");
            writer.write("- Test in a local world first\n");
            writer.write("- Verify CustomModelData values match between code and resource pack\n");
            writer.write("- Check console logs for validation warnings\n\n");

            logger.info("Generated resource pack integration guide: " + outputFile.getPath());
            return true;

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to generate resource pack guide", e);
            return false;
        }
    }

    /**
     * Finds all unique CustomModelData values used across a GUI.
     *
     * @param gui The GUI to analyze
     * @return Set of CustomModelData values in use
     */
    public static Set<Integer> getUsedCustomModelData(AuroraGui gui) {
        Set<Integer> cmdValues = new HashSet<>();

        if (gui == null) {
            return cmdValues;
        }

        ItemStack[] contents = gui.getInventory().getContents();

        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            try {
                Integer customModelData = meta.getCustomModelData();
                if (customModelData != null && customModelData != 0) {
                    cmdValues.add(customModelData);
                }
            } catch (NoSuchMethodError e) {
                // CustomModelData not available - ignore
            }
        }

        return cmdValues;
    }
}
