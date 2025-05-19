package dev.aurora.Packet.Validation;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * Creates a unique fingerprint for an ItemStack to detect tampering
 * Uses SHA-256 hashing of NBT data to prevent NBT injection exploits
 *
 * Key anti-dupe principle: Items can't be duplicated or modified without detection
 */
public class ItemFingerprint {

    private static final ItemFingerprint EMPTY = new ItemFingerprint();

    private final String materialId;
    private final int amount;
    private final short durability;
    private final String nbtHash;
    private final long creationTime;

    /**
     * Private constructor for empty fingerprint
     */
    private ItemFingerprint() {
        this.materialId = Material.AIR.name();
        this.amount = 0;
        this.durability = 0;
        this.nbtHash = "";
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * Private constructor with all fields
     */
    private ItemFingerprint(String materialId, int amount, short durability, String nbtHash) {
        this.materialId = materialId;
        this.amount = amount;
        this.durability = durability;
        this.nbtHash = nbtHash;
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * Creates a fingerprint for an ItemStack
     * @param item The item to fingerprint
     * @return The fingerprint
     */
    public static ItemFingerprint create(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return EMPTY;
        }

        String nbtHash = "";
        if (item.hasItemMeta()) {
            nbtHash = hashNBT(item.getItemMeta());
        }

        return new ItemFingerprint(
                item.getType().name(),
                item.getAmount(),
                item.getDurability(),
                nbtHash
        );
    }

    /**
     * Verifies that an ItemStack matches this fingerprint
     * @param item The item to check
     * @return true if matches, false if tampered
     */
    public boolean matches(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return this == EMPTY || amount == 0;
        }

        // Check material
        if (!item.getType().name().equals(materialId)) {
            return false;
        }

        // Check amount
        if (item.getAmount() != amount) {
            return false;
        }

        // Check durability
        if (item.getDurability() != durability) {
            return false;
        }

        // Check NBT hash (prevents NBT injection)
        if (item.hasItemMeta()) {
            String itemNbtHash = hashNBT(item.getItemMeta());
            return nbtHash.equals(itemNbtHash);
        }

        return nbtHash.isEmpty();
    }

    /**
     * Checks if two ItemStacks have matching fingerprints
     * @param item1 First item
     * @param item2 Second item
     * @return true if fingerprints match
     */
    public static boolean matches(ItemStack item1, ItemStack item2) {
        ItemFingerprint fp1 = create(item1);
        return fp1.matches(item2);
    }

    /**
     * Computes SHA-256 hash of ItemMeta NBT data
     * This prevents NBT injection exploits
     *
     * @param meta The item metadata
     * @return Base64-encoded SHA-256 hash
     */
    private static String hashNBT(ItemMeta meta) {
        if (meta == null) {
            return "";
        }

        try {
            // Serialize metadata to string
            StringBuilder nbtData = new StringBuilder();

            // Display name
            if (meta.hasDisplayName()) {
                nbtData.append("name:").append(meta.getDisplayName()).append(";");
            }

            // Lore
            if (meta.hasLore()) {
                nbtData.append("lore:");
                for (String line : meta.getLore()) {
                    nbtData.append(line).append("|");
                }
                nbtData.append(";");
            }

            // Enchantments
            if (meta.hasEnchants()) {
                nbtData.append("enchants:");
                meta.getEnchants().forEach((enchant, level) ->
                        nbtData.append(enchant.getKey().getKey()).append(":").append(level).append("|")
                );
                nbtData.append(";");
            }

            // Unbreakable
            if (meta.isUnbreakable()) {
                nbtData.append("unbreakable:true;");
            }

            // Custom model data (1.14+)
            try {
                if (meta.hasCustomModelData()) {
                    nbtData.append("customModelData:").append(meta.getCustomModelData()).append(";");
                }
            } catch (NoSuchMethodError ignored) {
                // Method doesn't exist in older versions
            }

            // Compute SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(nbtData.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always be available
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Gets the material ID
     * @return Material name
     */
    public String getMaterialId() {
        return materialId;
    }

    /**
     * Gets the item amount
     * @return Amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the item durability
     * @return Durability
     */
    public short getDurability() {
        return durability;
    }

    /**
     * Gets the NBT hash
     * @return Base64-encoded SHA-256 hash
     */
    public String getNbtHash() {
        return nbtHash;
    }

    /**
     * Gets the creation timestamp
     * @return Milliseconds since epoch
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Checks if this is an empty fingerprint
     * @return true if empty
     */
    public boolean isEmpty() {
        return this == EMPTY || amount == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemFingerprint that = (ItemFingerprint) o;
        return amount == that.amount &&
                durability == that.durability &&
                Objects.equals(materialId, that.materialId) &&
                Objects.equals(nbtHash, that.nbtHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(materialId, amount, durability, nbtHash);
    }

    @Override
    public String toString() {
        return "ItemFingerprint{" +
                "material=" + materialId +
                ", amount=" + amount +
                ", durability=" + durability +
                ", nbtHash=" + nbtHash.substring(0, Math.min(8, nbtHash.length())) + "..." +
                ", age=" + (System.currentTimeMillis() - creationTime) + "ms" +
                '}';
    }
}
