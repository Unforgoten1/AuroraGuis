package dev.aurora.Struct.Sort;

import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;

/**
 * Advanced sorting system for GUI items
 * Supports multi-criteria sorting with custom extractors
 */
public class GuiSorter {
    private final List<SortCriteria> criteria;
    private SortOrder defaultOrder;

    public enum SortOrder {
        ASCENDING, DESCENDING
    }

    /**
     * Represents a single sort criterion
     */
    public static class SortCriteria {
        private final String name;
        private final Function<ItemStack, Comparable<?>> extractor;
        private final SortOrder order;
        private final int priority;

        public SortCriteria(String name, Function<ItemStack, Comparable<?>> extractor, SortOrder order, int priority) {
            this.name = name;
            this.extractor = extractor;
            this.order = order;
            this.priority = priority;
        }

        public String getName() { return name; }
        public Function<ItemStack, Comparable<?>> getExtractor() { return extractor; }
        public SortOrder getOrder() { return order; }
        public int getPriority() { return priority; }
    }

    /**
     * Creates a new GUI sorter
     */
    public GuiSorter() {
        this.criteria = new ArrayList<>();
        this.defaultOrder = SortOrder.ASCENDING;
    }

    /**
     * Adds a sort criterion
     *
     * @param name The criterion name
     * @param extractor Function to extract comparable value
     * @param order Sort order
     * @param priority Priority (lower = higher priority)
     * @return This sorter for chaining
     */
    public GuiSorter addCriteria(String name, Function<ItemStack, Comparable<?>> extractor, SortOrder order, int priority) {
        criteria.add(new SortCriteria(name, extractor, order, priority));
        criteria.sort(Comparator.comparingInt(SortCriteria::getPriority));
        return this;
    }

    /**
     * Adds a criterion with default ascending order
     *
     * @param name The criterion name
     * @param extractor Function to extract comparable value
     * @return This sorter for chaining
     */
    public GuiSorter addCriteria(String name, Function<ItemStack, Comparable<?>> extractor) {
        return addCriteria(name, extractor, defaultOrder, criteria.size());
    }

    /**
     * Sets the default sort order
     *
     * @param order The default order
     * @return This sorter for chaining
     */
    public GuiSorter setDefaultOrder(SortOrder order) {
        this.defaultOrder = order;
        return this;
    }

    /**
     * Removes a criterion by name
     *
     * @param name The criterion name
     * @return This sorter for chaining
     */
    public GuiSorter removeCriteria(String name) {
        criteria.removeIf(c -> c.getName().equals(name));
        return this;
    }

    /**
     * Clears all criteria
     *
     * @return This sorter for chaining
     */
    public GuiSorter clearCriteria() {
        criteria.clear();
        return this;
    }

    /**
     * Sorts a list of items
     *
     * @param items The items to sort
     * @return Sorted list (new list, original unchanged)
     */
    public List<ItemStack> sort(List<ItemStack> items) {
        if (items == null || items.isEmpty() || criteria.isEmpty()) {
            return new ArrayList<>(items);
        }

        List<ItemStack> sorted = new ArrayList<>(items);
        sorted.sort(createComparator());
        return sorted;
    }

    /**
     * Creates a comparator from all criteria
     *
     * @return Composite comparator
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Comparator<ItemStack> createComparator() {
        return (item1, item2) -> {
            for (SortCriteria criterion : criteria) {
                Comparable val1 = criterion.getExtractor().apply(item1);
                Comparable val2 = criterion.getExtractor().apply(item2);

                // Handle nulls
                if (val1 == null && val2 == null) continue;
                if (val1 == null) return criterion.getOrder() == SortOrder.ASCENDING ? -1 : 1;
                if (val2 == null) return criterion.getOrder() == SortOrder.ASCENDING ? 1 : -1;

                int comparison = val1.compareTo(val2);
                if (comparison != 0) {
                    return criterion.getOrder() == SortOrder.ASCENDING ? comparison : -comparison;
                }
            }
            return 0;
        };
    }

    /**
     * Gets current criteria count
     *
     * @return Number of criteria
     */
    public int getCriteriaCount() {
        return criteria.size();
    }

    // ============= Pre-built Sort Criteria =============

    /**
     * Sort by material name
     */
    public static Function<ItemStack, Comparable<?>> byMaterial() {
        return item -> item != null ? item.getType().name() : "";
    }

    /**
     * Sort by item amount
     */
    public static Function<ItemStack, Comparable<?>> byAmount() {
        return item -> item != null ? item.getAmount() : 0;
    }

    /**
     * Sort by display name
     */
    public static Function<ItemStack, Comparable<?>> byDisplayName() {
        return item -> {
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                return "";
            }
            return item.getItemMeta().getDisplayName();
        };
    }

    /**
     * Sort by durability/damage
     */
    public static Function<ItemStack, Comparable<?>> byDurability() {
        return item -> {
            if (item == null) return 0;
            try {
                return item.getDurability();
            } catch (Exception e) {
                return 0;
            }
        };
    }

    /**
     * Sort by enchantment count
     */
    public static Function<ItemStack, Comparable<?>> byEnchantmentCount() {
        return item -> {
            if (item == null || !item.hasItemMeta()) return 0;
            return item.getEnchantments().size();
        };
    }

    /**
     * Sort by custom model data
     */
    public static Function<ItemStack, Comparable<?>> byCustomModelData() {
        return item -> {
            if (item == null || !item.hasItemMeta()) return 0;
            try {
                return item.getItemMeta().hasCustomModelData() ?
                    item.getItemMeta().getCustomModelData() : 0;
            } catch (Exception e) {
                return 0; // Not supported in older versions
            }
        };
    }

    /**
     * Sort by rarity (based on material)
     */
    public static Function<ItemStack, Comparable<?>> byRarity() {
        return item -> {
            if (item == null) return 0;
            String type = item.getType().name();

            // Diamond tier
            if (type.contains("DIAMOND") || type.contains("NETHERITE")) return 5;
            // Gold tier
            if (type.contains("GOLD")) return 4;
            // Iron tier
            if (type.contains("IRON")) return 3;
            // Stone tier
            if (type.contains("STONE")) return 2;
            // Wood tier
            if (type.contains("WOOD") || type.contains("WOODEN")) return 1;

            return 0;
        };
    }

    // ============= Builder Pattern =============

    /**
     * Creates a sorter for alphabetical sorting
     */
    public static GuiSorter alphabetical() {
        return new GuiSorter()
            .addCriteria("material", byMaterial(), SortOrder.ASCENDING, 0);
    }

    /**
     * Creates a sorter for quantity sorting
     */
    public static GuiSorter byQuantity() {
        return new GuiSorter()
            .addCriteria("amount", byAmount(), SortOrder.DESCENDING, 0);
    }

    /**
     * Creates a sorter for rarity sorting
     */
    public static GuiSorter byRaritySort() {
        return new GuiSorter()
            .addCriteria("rarity", byRarity(), SortOrder.DESCENDING, 0)
            .addCriteria("enchantments", byEnchantmentCount(), SortOrder.DESCENDING, 1);
    }

    /**
     * Alias for byRaritySort() - creates a sorter for rarity sorting
     */
    public static GuiSorter byRarityDesc() {
        return byRaritySort();
    }

    /**
     * Creates a multi-criteria sorter
     */
    public static GuiSorter multiCriteria() {
        return new GuiSorter()
            .addCriteria("rarity", byRarity(), SortOrder.DESCENDING, 0)
            .addCriteria("amount", byAmount(), SortOrder.DESCENDING, 1)
            .addCriteria("material", byMaterial(), SortOrder.ASCENDING, 2);
    }
}
