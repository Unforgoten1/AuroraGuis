package dev.aurora.Integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Integration with Vault economy API
 * Provides economy operations for GUIs
 */
public class EconomyIntegration {
    private static Object economy = null;
    private static boolean vaultAvailable = false;
    private static boolean initialized = false;

    /**
     * Initializes Vault economy integration
     *
     * @return true if Vault and economy plugin are available
     */
    public static boolean initialize() {
        if (initialized) {
            return vaultAvailable;
        }

        initialized = true;

        // Check if Vault plugin exists
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        try {
            // Use reflection to avoid hard dependency on Vault
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(economyClass);

            if (rsp != null) {
                economy = rsp.getProvider();
                vaultAvailable = true;
                return true;
            }
        } catch (ClassNotFoundException e) {
            // Vault not available
        }

        return false;
    }

    /**
     * Checks if Vault economy is available
     *
     * @return true if available
     */
    public static boolean isAvailable() {
        if (!initialized) {
            initialize();
        }
        return vaultAvailable;
    }

    /**
     * Gets a player's balance
     *
     * @param player The player
     * @return The balance, or 0 if economy unavailable
     */
    public static double getBalance(Player player) {
        if (!isAvailable()) return 0.0;

        try {
            return (double) economy.getClass()
                .getMethod("getBalance", org.bukkit.OfflinePlayer.class)
                .invoke(economy, player);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Checks if a player has sufficient balance
     *
     * @param player The player
     * @param amount The amount to check
     * @return true if player has enough money
     */
    public static boolean has(Player player, double amount) {
        if (!isAvailable()) return false;

        try {
            return (boolean) economy.getClass()
                .getMethod("has", org.bukkit.OfflinePlayer.class, double.class)
                .invoke(economy, player, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Withdraws money from a player
     *
     * @param player The player
     * @param amount The amount to withdraw
     * @return true if successful
     */
    public static boolean withdraw(Player player, double amount) {
        if (!isAvailable()) return false;

        try {
            Object response = economy.getClass()
                .getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class)
                .invoke(economy, player, amount);

            return (boolean) response.getClass()
                .getMethod("transactionSuccess")
                .invoke(response);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deposits money to a player
     *
     * @param player The player
     * @param amount The amount to deposit
     * @return true if successful
     */
    public static boolean deposit(Player player, double amount) {
        if (!isAvailable()) return false;

        try {
            Object response = economy.getClass()
                .getMethod("depositPlayer", org.bukkit.OfflinePlayer.class, double.class)
                .invoke(economy, player, amount);

            return (boolean) response.getClass()
                .getMethod("transactionSuccess")
                .invoke(response);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Formats an amount as currency
     *
     * @param amount The amount
     * @return Formatted string (e.g., "$100.00")
     */
    public static String format(double amount) {
        if (!isAvailable()) {
            return String.format("$%.2f", amount);
        }

        try {
            return (String) economy.getClass()
                .getMethod("format", double.class)
                .invoke(economy, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return String.format("$%.2f", amount);
        }
    }

    /**
     * Gets the currency name (singular)
     *
     * @return The currency name (e.g., "Dollar")
     */
    public static String getCurrencyName() {
        if (!isAvailable()) return "Money";

        try {
            return (String) economy.getClass()
                .getMethod("currencyNameSingular")
                .invoke(economy);
        } catch (Exception e) {
            return "Money";
        }
    }

    /**
     * Gets the currency name (plural)
     *
     * @return The currency name (e.g., "Dollars")
     */
    public static String getCurrencyNamePlural() {
        if (!isAvailable()) return "Money";

        try {
            return (String) economy.getClass()
                .getMethod("currencyNamePlural")
                .invoke(economy);
        } catch (Exception e) {
            return "Money";
        }
    }

    // ============= GUI Helper Methods =============

    /**
     * Creates a price checker for GUI items
     *
     * @param price The price
     * @return Function that checks if player can afford
     */
    public static Function<Player, Boolean> canAfford(double price) {
        return player -> has(player, price);
    }

    /**
     * Creates a purchase action for GUI items
     *
     * @param price The price
     * @param onSuccess Action to run on successful purchase
     * @param onFailure Action to run on failed purchase
     * @return BiConsumer for GUI click action
     */
    public static BiConsumer<Player, String> createPurchaseAction(
            double price,
            BiConsumer<Player, String> onSuccess,
            BiConsumer<Player, String> onFailure) {

        return (player, item) -> {
            if (!isAvailable()) {
                player.sendMessage("§cEconomy system is not available!");
                if (onFailure != null) onFailure.accept(player, item);
                return;
            }

            if (!has(player, price)) {
                player.sendMessage("§cInsufficient funds! Need " + format(price));
                if (onFailure != null) onFailure.accept(player, item);
                return;
            }

            if (withdraw(player, price)) {
                player.sendMessage("§aPurchased for " + format(price));
                player.sendMessage("§7New balance: " + format(getBalance(player)));
                if (onSuccess != null) onSuccess.accept(player, item);
            } else {
                player.sendMessage("§cTransaction failed!");
                if (onFailure != null) onFailure.accept(player, item);
            }
        };
    }

    /**
     * Creates a sell action for GUI items
     *
     * @param price The sell price
     * @param onSuccess Action to run on successful sale
     * @return BiConsumer for GUI click action
     */
    public static BiConsumer<Player, String> createSellAction(
            double price,
            BiConsumer<Player, String> onSuccess) {

        return (player, item) -> {
            if (!isAvailable()) {
                player.sendMessage("§cEconomy system is not available!");
                return;
            }

            if (deposit(player, price)) {
                player.sendMessage("§aSold for " + format(price));
                player.sendMessage("§7New balance: " + format(getBalance(player)));
                if (onSuccess != null) onSuccess.accept(player, item);
            } else {
                player.sendMessage("§cTransaction failed!");
            }
        };
    }

    /**
     * Adds economy requirement lore to item lore
     *
     * @param currentLore The current lore
     * @param price The price
     * @param player The player (to check if they can afford)
     * @return Updated lore
     */
    public static java.util.List<String> addPriceLore(java.util.List<String> currentLore, double price, Player player) {
        java.util.List<String> newLore = new java.util.ArrayList<>(currentLore);

        newLore.add("");
        newLore.add("§7Price: §6" + format(price));

        if (player != null && isAvailable()) {
            if (has(player, price)) {
                newLore.add("§aYou can afford this!");
            } else {
                newLore.add("§cInsufficient funds!");
                double needed = price - getBalance(player);
                newLore.add("§7Need: §c" + format(needed) + " §7more");
            }
        }

        return newLore;
    }
}
