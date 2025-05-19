package dev.aurora.Struct;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Predefined validators for drag-and-drop operations
 */
public class DragValidator {

    /**
     * Allows any item
     */
    public static BiPredicate<Player, ItemStack> any() {
        return (player, item) -> true;
    }

    /**
     * Rejects all items (effectively read-only)
     */
    public static BiPredicate<Player, ItemStack> none() {
        return (player, item) -> false;
    }

    /**
     * Only allows specific material types
     */
    public static BiPredicate<Player, ItemStack> materials(Material... materials) {
        Set<Material> allowed = new HashSet<>(Arrays.asList(materials));
        return (player, item) -> item != null && allowed.contains(item.getType());
    }

    /**
     * Only allows items with specific display names
     */
    public static BiPredicate<Player, ItemStack> namedItems(String... names) {
        Set<String> allowed = new HashSet<>(Arrays.asList(names));
        return (player, item) -> {
            if (item == null || !item.hasItemMeta()) return false;
            if (!item.getItemMeta().hasDisplayName()) return false;
            return allowed.contains(item.getItemMeta().getDisplayName());
        };
    }

    /**
     * Only allows items if player has permission
     */
    public static BiPredicate<Player, ItemStack> permission(String permission) {
        return (player, item) -> player.hasPermission(permission);
    }

    /**
     * Only allows items with minimum stack size
     */
    public static BiPredicate<Player, ItemStack> minAmount(int minAmount) {
        return (player, item) -> item != null && item.getAmount() >= minAmount;
    }

    /**
     * Only allows items with maximum stack size
     */
    public static BiPredicate<Player, ItemStack> maxAmount(int maxAmount) {
        return (player, item) -> item != null && item.getAmount() <= maxAmount;
    }

    /**
     * Combines multiple validators with AND logic
     */
    public static BiPredicate<Player, ItemStack> and(BiPredicate<Player, ItemStack>... validators) {
        return (player, item) -> {
            for (BiPredicate<Player, ItemStack> validator : validators) {
                if (!validator.test(player, item)) return false;
            }
            return true;
        };
    }

    /**
     * Combines multiple validators with OR logic
     */
    public static BiPredicate<Player, ItemStack> or(BiPredicate<Player, ItemStack>... validators) {
        return (player, item) -> {
            for (BiPredicate<Player, ItemStack> validator : validators) {
                if (validator.test(player, item)) return true;
            }
            return false;
        };
    }

    /**
     * Negates a validator
     */
    public static BiPredicate<Player, ItemStack> not(BiPredicate<Player, ItemStack> validator) {
        return (player, item) -> !validator.test(player, item);
    }
}
