package dev.aurora.ResourcePack;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable data class representing a custom model definition for resource packs.
 * <p>
 * ModelData instances define the mapping between a custom model identifier (e.g., "ruby")
 * and the underlying Minecraft configuration (base material, CustomModelData value).
 * </p>
 *
 * @since 1.1.0
 */
public class ModelData {

    private final String id;
    private final Material baseMaterial;
    private final int customModelData;
    private final String displayName;
    private final List<String> lore;

    /**
     * Creates a new ModelData instance.
     *
     * @param id The unique identifier for this model (e.g., "ruby", "gold_coin")
     * @param baseMaterial The base Minecraft material this model uses
     * @param customModelData The CustomModelData value from the resource pack
     * @param displayName Optional default display name (can be null)
     * @param lore Optional default lore lines (can be null)
     */
    public ModelData(String id, Material baseMaterial, int customModelData, String displayName, List<String> lore) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Model ID cannot be null or empty");
        }
        if (baseMaterial == null) {
            throw new IllegalArgumentException("Base material cannot be null");
        }

        this.id = id;
        this.baseMaterial = baseMaterial;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.lore = lore != null ? new ArrayList<>(lore) : null;
    }

    /**
     * Creates a ModelData instance without default display name or lore.
     *
     * @param id The unique identifier for this model
     * @param baseMaterial The base Minecraft material
     * @param customModelData The CustomModelData value
     */
    public ModelData(String id, Material baseMaterial, int customModelData) {
        this(id, baseMaterial, customModelData, null, null);
    }

    /**
     * Gets the unique identifier for this model.
     *
     * @return The model ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the base Minecraft material this model uses.
     *
     * @return The base material
     */
    public Material getBaseMaterial() {
        return baseMaterial;
    }

    /**
     * Gets the CustomModelData value from the resource pack.
     *
     * @return The CustomModelData integer value
     */
    public int getCustomModelData() {
        return customModelData;
    }

    /**
     * Gets the optional default display name.
     *
     * @return The display name, or null if not set
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the optional default lore lines.
     *
     * @return Unmodifiable list of lore lines, or null if not set
     */
    public List<String> getLore() {
        return lore != null ? Collections.unmodifiableList(lore) : null;
    }

    /**
     * Creates an ItemStack with this model's CustomModelData applied.
     * <p>
     * If this ModelData has a default display name or lore, they will be applied.
     * </p>
     *
     * @return A new ItemStack with CustomModelData applied
     */
    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Apply CustomModelData (1.14+)
            try {
                meta.setCustomModelData(customModelData);
            } catch (NoSuchMethodError e) {
                // CustomModelData not available in this version - silently ignore
            }

            // Apply default display name if set
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }

            // Apply default lore if set
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(new ArrayList<>(lore));
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Validates that this model can be applied.
     * <p>
     * Checks if CustomModelData is supported (1.14+) and if the base material is valid.
     * </p>
     *
     * @return true if the model is valid and can be applied, false otherwise
     */
    public boolean validate() {
        // Check if material exists
        if (baseMaterial == null || baseMaterial == Material.AIR) {
            return false;
        }

        // Try to create an item and apply CustomModelData
        try {
            ItemStack testItem = new ItemStack(baseMaterial);
            ItemMeta meta = testItem.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                return true;
            }
        } catch (NoSuchMethodError e) {
            // CustomModelData not supported in this version
            return false;
        }

        return false;
    }

    @Override
    public String toString() {
        return "ModelData{" +
                "id='" + id + '\'' +
                ", baseMaterial=" + baseMaterial +
                ", customModelData=" + customModelData +
                ", displayName='" + displayName + '\'' +
                ", lore=" + (lore != null ? lore.size() + " lines" : "none") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelData modelData = (ModelData) o;
        return id.equals(modelData.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
