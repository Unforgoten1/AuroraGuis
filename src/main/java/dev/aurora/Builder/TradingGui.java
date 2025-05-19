package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Custom villager-style trading GUI
 * Mimics vanilla trading with custom trades
 */
public class TradingGui extends AuroraGui {
    private final List<Trade> trades;
    private int currentPage;
    private String merchantName;
    private int merchantLevel;
    private BiConsumer<Player, Trade> onTrade;

    /**
     * Represents a single trade
     */
    public static class Trade {
        private final ItemStack input1;
        private final ItemStack input2;
        private final ItemStack output;
        private int maxUses;
        private int uses;
        private int experience;
        private boolean disabled;

        public Trade(ItemStack input1, ItemStack input2, ItemStack output) {
            this.input1 = input1;
            this.input2 = input2;
            this.output = output;
            this.maxUses = -1; // Unlimited
            this.uses = 0;
            this.experience = 0;
            this.disabled = false;
        }

        public Trade(ItemStack input1, ItemStack output) {
            this(input1, null, output);
        }

        public ItemStack getInput1() { return input1; }
        public ItemStack getInput2() { return input2; }
        public ItemStack getOutput() { return output; }
        public int getMaxUses() { return maxUses; }
        public int getUses() { return uses; }
        public int getExperience() { return experience; }
        public boolean isDisabled() { return disabled; }

        public Trade setMaxUses(int max) {
            this.maxUses = max;
            return this;
        }

        public Trade setExperience(int exp) {
            this.experience = exp;
            return this;
        }

        public Trade setDisabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public boolean canTrade() {
            return !disabled && (maxUses < 0 || uses < maxUses);
        }

        public void incrementUses() {
            uses++;
            if (maxUses > 0 && uses >= maxUses) {
                disabled = true;
            }
        }

        public Trade clone() {
            Trade trade = new Trade(
                    input1 != null ? input1.clone() : null,
                    input2 != null ? input2.clone() : null,
                    output != null ? output.clone() : null
            );
            trade.maxUses = this.maxUses;
            trade.uses = this.uses;
            trade.experience = this.experience;
            trade.disabled = this.disabled;
            return trade;
        }
    }

    /**
     * Creates a new trading GUI
     *
     * @param name The GUI name
     */
    public TradingGui(String name) {
        super(name);
        this.rows(4);
        this.trades = new ArrayList<>();
        this.currentPage = 0;
        this.merchantName = "&6Merchant";
        this.merchantLevel = 1;

        initializeLayout();
    }

    /**
     * Initializes the trading layout
     */
    private void initializeLayout() {
        ItemStack bg = new dev.aurora.Utilities.Items.ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        // Fill background
        for (int i = 0; i < getSize(); i++) {
            setItem(i, bg);
        }
    }

    /**
     * Sets the merchant name
     *
     * @param name The merchant name
     * @return This GUI for chaining
     */
    public TradingGui setMerchantName(String name) {
        this.merchantName = name;
        title(merchantName);
        return this;
    }

    /**
     * Sets the merchant level
     *
     * @param level The level (1-5)
     * @return This GUI for chaining
     */
    public TradingGui setMerchantLevel(int level) {
        this.merchantLevel = Math.max(1, Math.min(5, level));
        return this;
    }

    /**
     * Adds a trade
     *
     * @param trade The trade to add
     * @return This GUI for chaining
     */
    public TradingGui addTrade(Trade trade) {
        trades.add(trade);
        refresh();
        return this;
    }

    /**
     * Sets the trade callback
     *
     * @param onTrade Callback when trade is executed
     * @return This GUI for chaining
     */
    public TradingGui onTrade(BiConsumer<Player, Trade> onTrade) {
        this.onTrade = onTrade;
        return this;
    }

    /**
     * Displays trades on current page
     */
    private void refresh() {
        int tradesPerPage = 7;
        int startIndex = currentPage * tradesPerPage;

        // Trade slots: 10-16 (row 2)
        for (int i = 0; i < tradesPerPage; i++) {
            int tradeIndex = startIndex + i;
            int slot = 10 + i;

            if (tradeIndex < trades.size()) {
                displayTrade(trades.get(tradeIndex), slot);
            } else {
                setItem(slot, null);
            }
        }

        // Navigation
        if (currentPage > 0) {
            setItem(18, new dev.aurora.Utilities.Items.ItemBuilder(Material.ARROW)
                    .name("&7← Previous")
                    .build(), event -> previousPage());
        } else {
            setItem(18, null);
        }

        if ((currentPage + 1) * tradesPerPage < trades.size()) {
            setItem(26, new dev.aurora.Utilities.Items.ItemBuilder(Material.ARROW)
                    .name("&7Next →")
                    .build(), event -> nextPage());
        } else {
            setItem(26, null);
        }
    }

    /**
     * Displays a single trade
     */
    private void displayTrade(Trade trade, int slot) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Cost:");
        lore.add("&8• &f" + trade.getInput1().getAmount() + "x " + trade.getInput1().getType());
        if (trade.getInput2() != null) {
            lore.add("&8• &f" + trade.getInput2().getAmount() + "x " + trade.getInput2().getType());
        }
        lore.add("");
        lore.add("&7Receive:");
        lore.add("&8• &a" + trade.getOutput().getAmount() + "x " + trade.getOutput().getType());

        if (trade.getMaxUses() > 0) {
            lore.add("");
            lore.add("&7Uses: &f" + trade.getUses() + "&7/&f" + trade.getMaxUses());
        }

        if (!trade.canTrade()) {
            lore.add("");
            lore.add("&c&lOut of Stock!");
        }

        ItemStack display = new dev.aurora.Utilities.Items.ItemBuilder(
                        trade.canTrade() ? Material.EMERALD : Material.BARRIER)
                .name(trade.canTrade() ? "&aClick to Trade" : "&cUnavailable")
                .lore(lore)
                .build();

        setItem(slot, display, event -> executeTrade((Player) event.getWhoClicked(), trade));
    }

    /**
     * Executes a trade
     */
    private void executeTrade(Player player, Trade trade) {
        if (!trade.canTrade()) {
            player.sendMessage("§cThis trade is no longer available!");
            return;
        }

        // Check if player has required items
        if (!hasItems(player, trade.getInput1(), trade.getInput2())) {
            player.sendMessage("§cYou don't have the required items!");
            return;
        }

        // Remove items
        removeItems(player, trade.getInput1(), trade.getInput2());

        // Give output
        player.getInventory().addItem(trade.getOutput().clone());

        // Increment uses
        trade.incrementUses();

        // Call callback
        if (onTrade != null) {
            onTrade.accept(player, trade);
        }

        // Refresh display
        refresh();

        player.sendMessage("§aTrade successful!");
    }

    /**
     * Checks if player has required items
     */
    private boolean hasItems(Player player, ItemStack item1, ItemStack item2) {
        if (!player.getInventory().containsAtLeast(item1, item1.getAmount())) {
            return false;
        }
        if (item2 != null && !player.getInventory().containsAtLeast(item2, item2.getAmount())) {
            return false;
        }
        return true;
    }

    /**
     * Removes items from player inventory
     */
    private void removeItems(Player player, ItemStack item1, ItemStack item2) {
        player.getInventory().removeItem(item1);
        if (item2 != null) {
            player.getInventory().removeItem(item2);
        }
    }

    /**
     * Goes to previous page
     */
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            refresh();
        }
    }

    /**
     * Goes to next page
     */
    @Override
    public TradingGui nextPage() {
        currentPage++;
        refresh();
        return this;
    }

    /**
     * Gets number of trades
     *
     * @return Trade count
     */
    public int getTradeCount() {
        return trades.size();
    }

    @Override
    public TradingGui title(String title) {
        super.title(title);
        return this;
    }
}
