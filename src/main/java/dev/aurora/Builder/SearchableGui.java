package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.Struct.Action.GuiActions;
import dev.aurora.Struct.BorderType;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Searchable paginated GUI with filtering capabilities
 * Allows players to search through large lists of items
 */
public class SearchableGui {
    private final GuiManager guiManager;
    private final JavaPlugin plugin;
    private String title = "&6Search";
    private List<SearchableItem> allItems;
    private BiPredicate<SearchableItem, String> searchPredicate;
    private Consumer<InventoryClickEvent> itemClickAction;
    private String currentQuery = "";
    private int rows = 6;

    public static class SearchableItem {
        private final ItemStack displayItem;
        private final String searchText;
        private final Object data;

        public SearchableItem(ItemStack displayItem, String searchText, Object data) {
            this.displayItem = displayItem;
            this.searchText = searchText.toLowerCase();
            this.data = data;
        }

        public ItemStack getDisplayItem() {
            return displayItem;
        }

        public String getSearchText() {
            return searchText;
        }

        public Object getData() {
            return data;
        }
    }

    public SearchableGui(GuiManager guiManager, JavaPlugin plugin) {
        this.guiManager = guiManager;
        this.plugin = plugin;
        this.allItems = new ArrayList<>();
        this.searchPredicate = (item, query) ->
            item.getSearchText().contains(query.toLowerCase());
    }

    /**
     * Set the title
     */
    public SearchableGui title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Set number of rows
     */
    public SearchableGui rows(int rows) {
        this.rows = rows;
        return this;
    }

    /**
     * Add items to search through
     */
    public SearchableGui items(List<SearchableItem> items) {
        this.allItems = new ArrayList<>(items);
        return this;
    }

    /**
     * Add a single item
     */
    public SearchableGui addItem(ItemStack displayItem, String searchText, Object data) {
        this.allItems.add(new SearchableItem(displayItem, searchText, data));
        return this;
    }

    /**
     * Set custom search predicate
     */
    public SearchableGui searchPredicate(BiPredicate<SearchableItem, String> predicate) {
        this.searchPredicate = predicate;
        return this;
    }

    /**
     * Set click action for items
     */
    public SearchableGui onItemClick(Consumer<InventoryClickEvent> action) {
        this.itemClickAction = action;
        return this;
    }

    /**
     * Build and open the GUI with optional initial search
     */
    public void open(Player player, String initialQuery) {
        this.currentQuery = initialQuery == null ? "" : initialQuery;
        AuroraGui gui = buildGui(player);
        gui.open(player);
    }

    /**
     * Build and open the GUI
     */
    public void open(Player player) {
        open(player, "");
    }

    private AuroraGui buildGui(Player player) {
        // Filter items based on search query
        List<ItemStack> filteredItems = allItems.stream()
            .filter(item -> currentQuery.isEmpty() || searchPredicate.test(item, currentQuery))
            .map(SearchableItem::getDisplayItem)
            .collect(Collectors.toList());

        String guiTitle = currentQuery.isEmpty()
            ? title
            : title + " - &7Search: &f" + currentQuery;

        AuroraGui gui = new AuroraGui("search-" + System.currentTimeMillis())
            .title(guiTitle)
            .rows(rows)
            .setBorder(BorderType.FULL, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name("&7")
                .durability(7)
                .build());

        // Add search button
        gui.addItem(4, new ItemBuilder(Material.COMPASS)
            .name("&e&lSEARCH")
            .lore(
                "&7Current: &f" + (currentQuery.isEmpty() ? "None" : currentQuery),
                "",
                "&7Click to search"
            )
            .build(), event -> {
                new InputGui(guiManager, plugin)
                    .title("&6Enter Search Query")
                    .prompt("&7Enter your search term:")
                    .onInput((p, input) -> {
                        open(p, input);
                    })
                    .returnTo(gui)
                    .open(player);
            });

        // Add clear search button if query exists
        if (!currentQuery.isEmpty()) {
            gui.addItem(8, new ItemBuilder(Material.BARRIER)
                .name("&cClear Search")
                .lore("&7Click to clear search")
                .build(), event -> open(player, ""));
        }

        // Add filtered items
        gui.addPaginatedItems(filteredItems, itemClickAction);

        // Add navigation buttons
        ItemStack prevButton = new ItemBuilder(Material.ARROW)
            .name("&7\u2190 Previous Page")
            .build();
        ItemStack nextButton = new ItemBuilder(Material.ARROW)
            .name("&7Next Page \u2192")
            .build();

        gui.addItem(rows * 9 - 9, prevButton, GuiActions.prevPage(gui));
        gui.addItem(rows * 9 - 1, nextButton, GuiActions.nextPage(gui));

        // Add results count
        gui.addItem(rows * 9 - 5, new ItemBuilder(Material.PAPER)
            .name("&aResults: &f" + filteredItems.size())
            .lore("&7Total items: &f" + allItems.size())
            .build(), null);

        gui.register(guiManager);
        return gui;
    }
}
