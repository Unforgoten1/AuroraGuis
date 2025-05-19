package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Custom crafting table GUI
 * Allows custom crafting recipes with validation
 */
public class CraftingGui extends AuroraGui {
    private final Map<Integer, ItemStack> craftingGrid;
    private final List<CraftingRecipe> recipes;
    private final int[] gridSlots;
    private final int resultSlot;
    private BiConsumer<Player, ItemStack> onCraft;
    private boolean requireExactMatch;

    /**
     * Represents a crafting recipe
     */
    public static class CraftingRecipe {
        private final ItemStack[][] pattern;
        private final ItemStack result;
        private final Predicate<Player> condition;
        private final boolean shapeless;

        public CraftingRecipe(ItemStack[][] pattern, ItemStack result, boolean shapeless) {
            this.pattern = pattern;
            this.result = result;
            this.condition = null;
            this.shapeless = shapeless;
        }

        public CraftingRecipe(ItemStack[][] pattern, ItemStack result, Predicate<Player> condition, boolean shapeless) {
            this.pattern = pattern;
            this.result = result;
            this.condition = condition;
            this.shapeless = shapeless;
        }

        public ItemStack[][] getPattern() { return pattern; }
        public ItemStack getResult() { return result; }
        public boolean isShapeless() { return shapeless; }

        public boolean canCraft(Player player) {
            return condition == null || condition.test(player);
        }

        public boolean matches(ItemStack[] grid) {
            if (shapeless) {
                return matchesShapeless(grid);
            } else {
                return matchesShaped(grid);
            }
        }

        private boolean matchesShaped(ItemStack[] grid) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int gridIndex = row * 3 + col;
                    ItemStack gridItem = grid[gridIndex];
                    ItemStack patternItem = pattern[row][col];

                    if (!itemsMatch(gridItem, patternItem)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean matchesShapeless(ItemStack[] grid) {
            List<ItemStack> requiredItems = new ArrayList<>();
            for (ItemStack[] row : pattern) {
                for (ItemStack item : row) {
                    if (item != null && item.getType() != Material.AIR) {
                        requiredItems.add(item);
                    }
                }
            }

            List<ItemStack> gridItems = new ArrayList<>();
            for (ItemStack item : grid) {
                if (item != null && item.getType() != Material.AIR) {
                    gridItems.add(item);
                }
            }

            if (requiredItems.size() != gridItems.size()) {
                return false;
            }

            List<ItemStack> remaining = new ArrayList<>(requiredItems);
            for (ItemStack gridItem : gridItems) {
                boolean found = false;
                for (int i = 0; i < remaining.size(); i++) {
                    if (itemsMatch(gridItem, remaining.get(i))) {
                        remaining.remove(i);
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }

            return remaining.isEmpty();
        }

        private boolean itemsMatch(ItemStack item1, ItemStack item2) {
            if (item1 == null && item2 == null) return true;
            if (item1 == null || item2 == null) return false;
            if (item1.getType() == Material.AIR && item2.getType() == Material.AIR) return true;
            if (item1.getType() != item2.getType()) return false;
            return item1.getAmount() >= item2.getAmount();
        }
    }

    /**
     * Creates a new crafting GUI
     *
     * @param name The GUI name
     */
    public CraftingGui(String name) {
        super(name);
        this.rows(5);
        this.craftingGrid = new HashMap<>();
        this.recipes = new ArrayList<>();
        this.gridSlots = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};
        this.resultSlot = 24;
        this.requireExactMatch = false;

        initializeCraftingArea();
    }

    /**
     * Initializes the crafting area
     */
    private void initializeCraftingArea() {
        // Background
        ItemStack bg = new dev.aurora.Utilities.Items.ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < getSize(); i++) {
            if (!isGridSlot(i) && i != resultSlot) {
                setItem(i, bg);
            }
        }

        // Arrow indicator
        setItem(23, new dev.aurora.Utilities.Items.ItemBuilder(Material.ARROW)
                .name("&6Crafting →")
                .build());

        // Setup grid click handlers
        for (int slot : gridSlots) {
            setItem(slot, null, this::handleGridClick);
        }
    }

    /**
     * Adds a shaped recipe
     *
     * @param pattern The 3x3 pattern
     * @param result The result item
     * @return This GUI for chaining
     */
    public CraftingGui addShapedRecipe(ItemStack[][] pattern, ItemStack result) {
        recipes.add(new CraftingRecipe(pattern, result, false));
        return this;
    }

    /**
     * Adds a shapeless recipe
     *
     * @param ingredients The ingredients (order doesn't matter)
     * @param result The result item
     * @return This GUI for chaining
     */
    public CraftingGui addShapelessRecipe(ItemStack[] ingredients, ItemStack result) {
        ItemStack[][] pattern = new ItemStack[3][3];
        int idx = 0;
        for (int row = 0; row < 3 && idx < ingredients.length; row++) {
            for (int col = 0; col < 3 && idx < ingredients.length; col++) {
                pattern[row][col] = ingredients[idx++];
            }
        }
        recipes.add(new CraftingRecipe(pattern, result, true));
        return this;
    }

    /**
     * Sets the craft callback
     *
     * @param onCraft Callback when item is crafted
     * @return This GUI for chaining
     */
    public CraftingGui onCraft(BiConsumer<Player, ItemStack> onCraft) {
        this.onCraft = onCraft;
        return this;
    }

    /**
     * Handles click on crafting grid
     */
    private void handleGridClick(InventoryClickEvent event) {
        // Allow placing/removing items
        event.setCancelled(false);

        // Update result after a tick
        org.bukkit.Bukkit.getScheduler().runTaskLater(
                getManager().getPlugin(),
                this::updateResult,
                1L
        );
    }

    /**
     * Updates the result slot based on current grid
     */
    private void updateResult() {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = getInventory().getItem(gridSlots[i]);
        }

        // Find matching recipe
        CraftingRecipe matchedRecipe = null;
        for (CraftingRecipe recipe : recipes) {
            if (recipe.matches(grid)) {
                matchedRecipe = recipe;
                break;
            }
        }

        // Update result
        if (matchedRecipe != null) {
            setItem(resultSlot, matchedRecipe.getResult().clone(), this::handleCraft);
        } else {
            setItem(resultSlot, null);
        }
    }

    /**
     * Handles crafting the result
     */
    private void handleCraft(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getCurrentItem();

        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        // Consume ingredients
        for (int slot : gridSlots) {
            ItemStack item = getInventory().getItem(slot);
            if (item != null && item.getAmount() > 0) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    getInventory().setItem(slot, null);
                }
            }
        }

        // Give result
        player.getInventory().addItem(result.clone());

        // Call callback
        if (onCraft != null) {
            onCraft.accept(player, result);
        }

        // Update
        updateResult();
    }

    /**
     * Checks if slot is part of grid
     */
    private boolean isGridSlot(int slot) {
        for (int gridSlot : gridSlots) {
            if (slot == gridSlot) return true;
        }
        return false;
    }

    /**
     * Clears the crafting grid
     *
     * @return This GUI for chaining
     */
    public CraftingGui clearGrid() {
        for (int slot : gridSlots) {
            setItem(slot, null);
        }
        updateResult();
        return this;
    }

    @Override
    public CraftingGui title(String title) {
        super.title(title);
        return this;
    }
}
