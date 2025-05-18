package dev.aurora.Utilities.Items;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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

public class ItemBuilder extends ItemStack {
    public ItemBuilder(Material mat) {
        super(mat);
    }

    public ItemBuilder(Material mat, byte damage) {
        super(mat, 1, damage);
    }

    public ItemBuilder(ItemStack is) {
        super(is);
    }

    public ItemBuilder amount(int amount) {
        this.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ColorUtils.color(name));
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder appendName(String append) {
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ColorUtils.color(meta.hasDisplayName() ? meta.getDisplayName() : "") + append);
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String text) {
        ItemMeta meta = this.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList();
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
        ItemMeta meta = this.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList();
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

    public ItemBuilder type(Material material) {
        this.setType(material);
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = this.getItemMeta();
        meta.setLore(new ArrayList());
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearEnchantments() {
        this.getEnchantments().keySet().forEach(this::removeEnchantment);
        return this;
    }

    public ItemBuilder owner(String owner) {
        if (!(this.getItemMeta() instanceof SkullMeta)) {
            return this;
        } else {
            SkullMeta meta = (SkullMeta)this.getItemMeta();
            meta.setOwner(owner);
            this.setItemMeta(meta);
            return this;
        }
    }

    public ItemBuilder texture(String value) {
        if (!(this.getItemMeta() instanceof SkullMeta)) {
            return this;
        } else {
            SkullMeta meta = (SkullMeta)this.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", value));

            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException var6) {
                Exception ex = var6;
                ex.printStackTrace();
            }

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

    public ItemBuilder glow() {
        this.addUnsafeEnchantment(Enchantment.DURABILITY, 0);
        return this.flag(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder flag(ItemFlag flag) {
        ItemMeta meta = this.getItemMeta();
        meta.addItemFlags(flag);
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearFlags() {
        ItemMeta meta = this.getItemMeta();
        meta.getItemFlags().forEach((xva$0) -> {
            meta.removeItemFlags(xva$0);
        });
        this.setItemMeta(meta);
        return this;
    }
}

