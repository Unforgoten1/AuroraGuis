package dev.aurora.Utilities.Items;

// Authlib not available in standard Spigot API
// import com.mojang.authlib.GameProfile;
// import com.mojang.authlib.properties.Property;
import dev.aurora.ResourcePack.ModelData;
import dev.aurora.ResourcePack.ModelRegistry;
import dev.aurora.Utilities.Strings.ColorUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Fluent builder for creating customized ItemStacks
 * Provides chainable methods for setting name, lore, enchantments, flags, and more
 * All text values support color codes via ColorUtils
 */
public class ItemBuilder extends ItemStack {
    /**
     * Creates a new ItemBuilder with the specified material
     *
     * @param mat The material for the item
     */
    public ItemBuilder(Material mat) {
        super(mat);
    }

    /**
     * Creates a new ItemBuilder with material and damage value (for legacy versions)
     *
     * @param mat The material for the item
     * @param damage The damage/durability value
     */
    public ItemBuilder(Material mat, byte damage) {
        super(mat, 1, damage);
    }

    /**
     * Creates a new ItemBuilder from an existing ItemStack
     *
     * @param is The ItemStack to copy
     */
    public ItemBuilder(ItemStack is) {
        super(is);
    }

    /**
     * Sets the stack size
     *
     * @param amount The amount (1-64)
     * @return This builder for chaining
     */
    public ItemBuilder amount(int amount) {
        this.setAmount(amount);
        return this;
    }

    /**
     * Sets the display name (supports color codes)
     *
     * @param name The display name (supports & color codes)
     * @return This builder for chaining
     * @throws IllegalArgumentException if name is null
     */
    public ItemBuilder name(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        ItemMeta meta = this.getItemMeta();
        if (meta == null) return this;
        meta.setDisplayName(ColorUtils.color(name));
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder appendName(String append) {
        if (append == null) {
            throw new IllegalArgumentException("Append text cannot be null");
        }
        ItemMeta meta = this.getItemMeta();
        if (meta == null) return this;
        meta.setDisplayName(ColorUtils.color(meta.hasDisplayName() ? meta.getDisplayName() : "") + append);
        this.setItemMeta(meta);
        return this;
    }

    /**
     * Adds a single line to the lore (supports color codes)
     *
     * @param text The lore line to add (supports & color codes)
     * @return This builder for chaining
     * @throws IllegalArgumentException if text is null
     */
    public ItemBuilder lore(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Lore text cannot be null");
        }
        ItemMeta meta = this.getItemMeta();
        if (meta == null) return this;
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.add(ColorUtils.color(text));
        meta.setLore(lore);
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String... text) {
        return this.lore(Arrays.asList(text));
    }

    public ItemBuilder lore(List<String> text) {
        if (text == null) {
            throw new IllegalArgumentException("Lore list cannot be null");
        }
        ItemMeta meta = this.getItemMeta();
        if (meta == null) return this;
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.addAll(ColorUtils.color(text));
        meta.setLore(lore);
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder durability(int durability) {
        this.setDurability((short)durability);
        return this;
    }

    public ItemBuilder data(int data) {
        this.setData(new MaterialData(this.getType(), (byte)data));
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        this.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment) {
        this.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder enchantments(Map<Enchantment, Integer> enchantments) {
        this.addUnsafeEnchantments(enchantments);
        return this;
    }

    /**
     * Alias for enchantment() method
     * @param enchantment The enchantment to add
     * @param level The enchantment level
     * @return This ItemBuilder for chaining
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return enchantment(enchantment, level);
    }

    /**
     * Sets custom model data (1.14+)
     * @param data The custom model data value
     * @return This ItemBuilder for chaining
     */
    public ItemBuilder customModelData(int data) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            try {
                meta.setCustomModelData(data);
                this.setItemMeta(meta);
            } catch (NoSuchMethodError e) {
                // Custom model data not available in this version
            }
        }
        return this;
    }

    /**
     * Sets custom model from ModelRegistry by ID
     * <p>
     * This is a convenience method that looks up a registered model and applies
     * its base material, CustomModelData, and optional display name.
     * </p>
     *
     * @param modelId The model identifier (e.g., "ruby", "gold_coin")
     * @return This ItemBuilder for chaining
     * @since 1.1.0
     */
    public ItemBuilder customModel(String modelId) {
        ModelData model = ModelRegistry.get(modelId);
        if (model != null) {
            // Apply base material
            type(model.getBaseMaterial());

            // Apply CustomModelData
            customModelData(model.getCustomModelData());

            // Apply default display name if set
            if (model.getDisplayName() != null) {
                name(model.getDisplayName());
            }

            // Apply default lore if set
            if (model.getLore() != null && !model.getLore().isEmpty()) {
                lore(model.getLore());
            }
        }
        return this;
    }

    public ItemBuilder type(Material material) {
        this.setType(material);
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = this.getItemMeta();
        if (meta == null) return this;
        meta.setLore(new ArrayList<>());
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearEnchantments() {
        this.getEnchantments().keySet().forEach(this::removeEnchantment);
        return this;
    }

    public ItemBuilder owner(String owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        if (!(this.getItemMeta() instanceof SkullMeta)) {
            return this;
        } else {
            SkullMeta meta = (SkullMeta)this.getItemMeta();
            if (meta == null) return this;
            meta.setOwner(owner);
            this.setItemMeta(meta);
            return this;
        }
    }

    public ItemBuilder texture(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Texture value cannot be null");
        }
        if (!(this.getItemMeta() instanceof SkullMeta)) {
            return this;
        } else {
            SkullMeta meta = (SkullMeta)this.getItemMeta();
            if (meta == null) return this;
            // GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            // profile.getProperties().put("textures", new Property("textures", value));

            // Skull texture setting disabled - requires authlib
            /*
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException var6) {
                Exception ex = var6;
                ex.printStackTrace();
            }
            */

            this.setItemMeta(meta);
            return this;
        }
    }

    public ItemBuilder color(Color color) {
        if (this.getType() != Material.LEATHER_BOOTS && this.getType() != Material.LEATHER_CHESTPLATE && this.getType() != Material.LEATHER_HELMET && this.getType() != Material.LEATHER_LEGGINGS) {
            throw new IllegalArgumentException("color() only applicable for leather armor!");
        } else {
            LeatherArmorMeta meta = (LeatherArmorMeta)this.getItemMeta();
            meta.setColor(color);
            this.setItemMeta(meta);
            return this;
        }
    }

    /**
     * Adds a glow effect to the item without showing enchantments
     *
     * @return This builder for chaining
     */
    public ItemBuilder glow() {
        this.addUnsafeEnchantment(Enchantment.DURABILITY, 0);
        return this.flag(ItemFlag.HIDE_ENCHANTS);
    }

    /**
     * Adds an ItemFlag to hide certain item attributes
     *
     * @param flag The flag to add
     * @return This builder for chaining
     * @throws IllegalArgumentException if flag is null
     */
    public ItemBuilder flag(ItemFlag flag) {
        if (flag == null) {
            throw new IllegalArgumentException("ItemFlag cannot be null");
        }
        ItemMeta meta = this.getItemMeta();
        if (meta == null) return this;
        meta.addItemFlags(flag);
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearFlags() {
        ItemMeta meta = this.getItemMeta();
        if (meta == null) return this;
        meta.getItemFlags().forEach((xva$0) -> {
            meta.removeItemFlags(xva$0);
        });
        this.setItemMeta(meta);
        return this;
    }

    /**
     * Builds and returns the ItemStack
     * Since ItemBuilder extends ItemStack, this simply returns itself
     *
     * @return This ItemStack
     */
    public ItemStack build() {
        return this;
    }
}

