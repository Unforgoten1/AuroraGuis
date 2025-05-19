package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * GUI with advanced filtering and sorting capabilities
 * Supports multiple filters, combinators, and sorting criteria
 */
public class FilterableGui extends AuroraGui {
    private final List<ItemStack> allItems;
    private final Map<String, Function<ItemStack, Object>> filterCriteria;
    private final Map<String, Set<Object>> activeFilters;
    private final List<Comparator<ItemStack>> sorters;
    private FilterMode filterMode;
    private boolean multiSelectEnabled;
    private final Set<ItemStack> selectedItems;

    /**
     * Filter combination mode
     */
    public enum FilterMode {
        /** All filters must match (AND) */
        AND,
        /** Any filter can match (OR) */
        OR
    }

    /**
     * Creates a new filterable GUI
     *
     * @param name The GUI name
     */
    public FilterableGui(String name) {
        super(name);
        this.allItems = new ArrayList<>();
        this.filterCriteria = new HashMap<>();
        this.activeFilters = new HashMap<>();
        this.sorters = new ArrayList<>();
        this.filterMode = FilterMode.AND;
        this.multiSelectEnabled = false;
        this.selectedItems = new HashSet<>();
    }

    /**
     * Adds items to the filterable collection
     *
     * @param items The items to add
     * @return This GUI for chaining
     */
    public FilterableGui addItems(ItemStack... items) {
        Collections.addAll(allItems, items);
        refresh();
        return this;
    }

    /**
     * Adds items to the filterable collection
     *
     * @param items The items to add
     * @return This GUI for chaining
     */
    public FilterableGui addItems(Collection<ItemStack> items) {
        allItems.addAll(items);
        refresh();
        return this;
    }

    /**
     * Adds a filter criterion
     *
     * @param name The filter name
     * @param extractor Function to extract the filterable value from an item
     * @return This GUI for chaining
     */
    public FilterableGui addFilter(String name, Function<ItemStack, Object> extractor) {
        filterCriteria.put(name, extractor);
        return this;
    }

    /**
     * Adds a sorting criterion
     *
     * @param comparator The comparator for sorting
     * @return This GUI for chaining
     */
    public FilterableGui addSorter(Comparator<ItemStack> comparator) {
        sorters.add(comparator);
        refresh();
        return this;
    }

    /**
     * Adds a sorting criterion by extracted value
     *
     * @param name The sorter name
     * @param extractor Function to extract comparable value
     * @return This GUI for chaining
     */
    public FilterableGui addSorter(String name, Function<ItemStack, Comparable> extractor) {
        sorters.add(Comparator.comparing(extractor));
        refresh();
        return this;
    }

    /**
     * Sets the filter combination mode
     *
     * @param mode The filter mode (AND/OR)
     * @return This GUI for chaining
     */
    public FilterableGui setFilterMode(FilterMode mode) {
        this.filterMode = mode;
        refresh();
        return this;
    }

    /**
     * Enables or disables multi-select mode
     *
     * @param enabled true to enable
     * @return This GUI for chaining
     */
    public FilterableGui enableMultiSelect(boolean enabled) {
        this.multiSelectEnabled = enabled;
        return this;
    }

    /**
     * Activates a filter
     *
     * @param filterName The filter name
     * @param value The value to filter by
     * @return This GUI for chaining
     */
    public FilterableGui activateFilter(String filterName, Object value) {
        activeFilters.computeIfAbsent(filterName, k -> new HashSet<>()).add(value);
        refresh();
        return this;
    }

    /**
     * Deactivates a filter
     *
     * @param filterName The filter name
     * @param value The value to remove from filter
     * @return This GUI for chaining
     */
    public FilterableGui deactivateFilter(String filterName, Object value) {
        Set<Object> values = activeFilters.get(filterName);
        if (values != null) {
            values.remove(value);
            if (values.isEmpty()) {
                activeFilters.remove(filterName);
            }
        }
        refresh();
        return this;
    }

    /**
     * Clears all active filters
     *
     * @return This GUI for chaining
     */
    public FilterableGui clearFilters() {
        activeFilters.clear();
        refresh();
        return this;
    }

    /**
     * Clears all sorters
     *
     * @return This GUI for chaining
     */
    public FilterableGui clearSorters() {
        sorters.clear();
        refresh();
        return this;
    }

    /**
     * Applies filters and returns filtered items
     *
     * @return Filtered list of items
     */
    public List<ItemStack> getFilteredItems() {
        if (activeFilters.isEmpty()) {
            return new ArrayList<>(allItems);
        }

        List<ItemStack> filtered = allItems.stream()
                .filter(this::matchesFilters)
                .collect(Collectors.toList());

        // Apply sorting
        if (!sorters.isEmpty()) {
            Comparator<ItemStack> combinedComparator = sorters.get(0);
            for (int i = 1; i < sorters.size(); i++) {
                combinedComparator = combinedComparator.thenComparing(sorters.get(i));
            }
            filtered.sort(combinedComparator);
        }

        return filtered;
    }

    /**
     * Checks if an item matches active filters
     */
    private boolean matchesFilters(ItemStack item) {
        if (activeFilters.isEmpty()) return true;

        boolean matches = filterMode == FilterMode.AND;

        for (Map.Entry<String, Set<Object>> entry : activeFilters.entrySet()) {
            String filterName = entry.getKey();
            Set<Object> allowedValues = entry.getValue();

            Function<ItemStack, Object> extractor = filterCriteria.get(filterName);
            if (extractor == null) continue;

            Object itemValue = extractor.apply(item);
            boolean filterMatches = allowedValues.contains(itemValue);

            if (filterMode == FilterMode.AND) {
                matches &= filterMatches;
            } else {
                matches |= filterMatches;
            }
        }

        return matches;
    }

    /**
     * Refreshes the displayed items
     *
     * @return This GUI for chaining
     */
    public FilterableGui refresh() {
        // Clear current items
        for (int i = 0; i < getSize(); i++) {
            setItem(i, null);
        }

        // Display filtered items
        List<ItemStack> filtered = getFilteredItems();
        for (int i = 0; i < Math.min(filtered.size(), getSize()); i++) {
            ItemStack item = filtered.get(i);
            setItem(i, item, event -> {
                if (multiSelectEnabled) {
                    toggleSelection(item);
                }
            });
        }

        return this;
    }

    /**
     * Toggles selection of an item (multi-select mode)
     */
    private void toggleSelection(ItemStack item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
    }

    /**
     * Gets selected items
     *
     * @return Set of selected items
     */
    public Set<ItemStack> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    /**
     * Clears all selections
     *
     * @return This GUI for chaining
     */
    public FilterableGui clearSelections() {
        selectedItems.clear();
        return this;
    }

    /**
     * Gets the number of filtered items
     *
     * @return Item count
     */
    public int getFilteredCount() {
        return getFilteredItems().size();
    }

    /**
     * Gets the total number of items
     *
     * @return Total item count
     */
    public int getTotalCount() {
        return allItems.size();
    }

    // ==================== Pre-built Filters ====================

    /**
     * Material type filter
     */
    public static Function<ItemStack, Object> byMaterial() {
        return ItemStack::getType;
    }

    /**
     * Display name filter
     */
    public static Function<ItemStack, Object> byName() {
        return item -> item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
                item.getItemMeta().getDisplayName() : item.getType().name();
    }

    /**
     * Amount filter
     */
    public static Function<ItemStack, Object> byAmount() {
        return ItemStack::getAmount;
    }

    /**
     * Enchantment presence filter
     */
    public static Function<ItemStack, Object> byEnchanted() {
        return item -> !item.getEnchantments().isEmpty();
    }

    // ==================== Pre-built Sorters ====================

    /**
     * Sort by material name
     */
    public static Comparator<ItemStack> sortByMaterial() {
        return Comparator.comparing(item -> item.getType().name());
    }

    /**
     * Sort by amount (descending)
     */
    public static Comparator<ItemStack> sortByAmount() {
        return Comparator.comparingInt(ItemStack::getAmount).reversed();
    }

    /**
     * Sort by display name
     */
    public static Comparator<ItemStack> sortByName() {
        return Comparator.comparing(item ->
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
                        item.getItemMeta().getDisplayName() : item.getType().name()
        );
    }
}
