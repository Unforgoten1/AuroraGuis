package dev.aurora.ResourcePack;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static registry for custom model definitions used in resource packs.
 * <p>
 * The ModelRegistry allows developers to register custom models and reference them
 * by simple string identifiers (e.g., "ruby", "gold_coin") instead of remembering
 * base materials and CustomModelData values.
 * </p>
 * <p>
 * Models can be registered programmatically or loaded from a models.yml configuration file.
 * </p>
 *
 * @since 1.1.0
 */
public class ModelRegistry {

    private static final Map<String, ModelData> models = new ConcurrentHashMap<>();
    private static Logger logger = Logger.getLogger("AuroraGuis");

    /**
     * Sets the logger for registry operations.
     *
     * @param newLogger The logger to use
     */
    public static void setLogger(Logger newLogger) {
        logger = newLogger;
    }

    /**
     * Registers a custom model in the registry.
     *
     * @param id The unique identifier for this model (e.g., "ruby")
     * @param baseMaterial The base Minecraft material
     * @param customModelData The CustomModelData value from the resource pack
     * @return The created ModelData instance
     * @throws IllegalArgumentException if id is null/empty or baseMaterial is null
     */
    public static ModelData register(String id, Material baseMaterial, int customModelData) {
        return register(id, baseMaterial, customModelData, null, null);
    }

    /**
     * Registers a custom model with default display name and lore.
     *
     * @param id The unique identifier for this model
     * @param baseMaterial The base Minecraft material
     * @param customModelData The CustomModelData value
     * @param displayName Optional default display name
     * @param lore Optional default lore lines
     * @return The created ModelData instance
     * @throws IllegalArgumentException if id is null/empty or baseMaterial is null
     */
    public static ModelData register(String id, Material baseMaterial, int customModelData, String displayName, List<String> lore) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Model ID cannot be null or empty");
        }
        if (baseMaterial == null) {
            throw new IllegalArgumentException("Base material cannot be null");
        }

        ModelData modelData = new ModelData(id, baseMaterial, customModelData, displayName, lore);

        if (models.containsKey(id)) {
            logger.warning("Overwriting existing model with ID: " + id);
        }

        models.put(id, modelData);
        logger.fine("Registered custom model: " + id + " (" + baseMaterial + ", CMD=" + customModelData + ")");

        return modelData;
    }

    /**
     * Registers a ModelData instance directly.
     *
     * @param modelData The ModelData to register
     * @return The registered ModelData instance
     * @throws IllegalArgumentException if modelData is null
     */
    public static ModelData register(ModelData modelData) {
        if (modelData == null) {
            throw new IllegalArgumentException("ModelData cannot be null");
        }

        String id = modelData.getId();
        if (models.containsKey(id)) {
            logger.warning("Overwriting existing model with ID: " + id);
        }

        models.put(id, modelData);
        logger.fine("Registered custom model: " + modelData);

        return modelData;
    }

    /**
     * Gets a registered model by its ID.
     *
     * @param id The model identifier
     * @return The ModelData, or null if not found
     */
    public static ModelData get(String id) {
        return models.get(id);
    }

    /**
     * Creates an ItemStack from a registered model.
     * <p>
     * This is a convenience method equivalent to:
     * <pre>
     * ModelData model = ModelRegistry.get(modelId);
     * ItemStack item = model != null ? model.toItemStack() : null;
     * </pre>
     * </p>
     *
     * @param modelId The model identifier
     * @return An ItemStack with the model applied, or null if model not found
     */
    public static ItemStack createItem(String modelId) {
        ModelData model = get(modelId);
        return model != null ? model.toItemStack() : null;
    }

    /**
     * Checks if a model is registered.
     *
     * @param id The model identifier
     * @return true if the model exists, false otherwise
     */
    public static boolean exists(String id) {
        return models.containsKey(id);
    }

    /**
     * Gets all registered model IDs.
     *
     * @return Unmodifiable set of model identifiers
     */
    public static Set<String> getRegisteredIds() {
        return Collections.unmodifiableSet(models.keySet());
    }

    /**
     * Gets all registered models.
     *
     * @return Unmodifiable collection of ModelData instances
     */
    public static Collection<ModelData> getAllModels() {
        return Collections.unmodifiableCollection(models.values());
    }

    /**
     * Unregisters a model.
     *
     * @param id The model identifier to remove
     * @return true if the model was removed, false if it didn't exist
     */
    public static boolean unregister(String id) {
        ModelData removed = models.remove(id);
        if (removed != null) {
            logger.fine("Unregistered custom model: " + id);
            return true;
        }
        return false;
    }

    /**
     * Clears all registered models.
     */
    public static void clear() {
        int count = models.size();
        models.clear();
        logger.fine("Cleared " + count + " registered models");
    }

    /**
     * Loads models from a models.yml configuration file.
     * <p>
     * Expected YAML format:
     * <pre>
     * models:
     *   ruby:
     *     base-material: EMERALD
     *     custom-model-data: 1001
     *     name: "&cRuby"
     *     lore:
     *       - "&7A rare gemstone"
     *   gold_coin:
     *     base-material: GOLD_NUGGET
     *     custom-model-data: 2001
     *     name: "&6Gold Coin"
     * </pre>
     * </p>
     *
     * @param configFile The models.yml file
     * @return Number of models loaded
     */
    public static int loadFromConfig(File configFile) {
        if (!configFile.exists()) {
            logger.warning("Model config file not found: " + configFile.getPath());
            return 0;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection modelsSection = config.getConfigurationSection("models");

            if (modelsSection == null) {
                logger.warning("No 'models' section found in " + configFile.getName());
                return 0;
            }

            int loaded = 0;
            for (String modelId : modelsSection.getKeys(false)) {
                ConfigurationSection modelSection = modelsSection.getConfigurationSection(modelId);
                if (modelSection == null) {
                    logger.warning("Invalid model configuration for: " + modelId);
                    continue;
                }

                try {
                    // Parse base material
                    String materialName = modelSection.getString("base-material");
                    if (materialName == null) {
                        logger.warning("Missing 'base-material' for model: " + modelId);
                        continue;
                    }

                    Material baseMaterial;
                    try {
                        baseMaterial = Material.valueOf(materialName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid material '" + materialName + "' for model: " + modelId);
                        continue;
                    }

                    // Parse CustomModelData
                    if (!modelSection.contains("custom-model-data")) {
                        logger.warning("Missing 'custom-model-data' for model: " + modelId);
                        continue;
                    }
                    int customModelData = modelSection.getInt("custom-model-data");

                    // Parse optional display name
                    String displayName = modelSection.getString("name");

                    // Parse optional lore
                    List<String> lore = null;
                    if (modelSection.contains("lore")) {
                        lore = modelSection.getStringList("lore");
                    } else if (modelSection.contains("description")) {
                        // Support 'description' as alternative to 'lore'
                        String description = modelSection.getString("description");
                        if (description != null) {
                            lore = Collections.singletonList(description);
                        }
                    }

                    // Register the model
                    register(modelId, baseMaterial, customModelData, displayName, lore);
                    loaded++;

                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error loading model: " + modelId, e);
                }
            }

            logger.info("Loaded " + loaded + " custom model(s) from " + configFile.getName());
            return loaded;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load models from " + configFile.getPath(), e);
            return 0;
        }
    }

    /**
     * Validates all registered models.
     * <p>
     * Checks if each model can be applied (1.14+ support, valid materials).
     * </p>
     *
     * @return List of model IDs that failed validation
     */
    public static List<String> validateAll() {
        List<String> failures = new ArrayList<>();

        for (Map.Entry<String, ModelData> entry : models.entrySet()) {
            if (!entry.getValue().validate()) {
                failures.add(entry.getKey());
            }
        }

        if (!failures.isEmpty()) {
            logger.warning("Failed to validate " + failures.size() + " model(s): " + failures);
        }

        return failures;
    }
}
