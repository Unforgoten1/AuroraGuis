package dev.aurora.Struct;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a menu item in a context menu
 */
public class MenuItem {
    private final ItemStack icon;
    private final String name;
    private final Consumer<Player> action;
    private final Predicate<Player> condition;
    private final boolean separator;

    private MenuItem(ItemStack icon, String name, Consumer<Player> action,
                    Predicate<Player> condition, boolean separator) {
        this.icon = icon;
        this.name = name;
        this.action = action;
        this.condition = condition;
        this.separator = separator;
    }

    /**
     * Creates a regular menu item
     *
     * @param icon The item icon
     * @param name The display name
     * @param action The action to execute
     * @return A new MenuItem
     */
    public static MenuItem create(ItemStack icon, String name, Consumer<Player> action) {
        return new MenuItem(icon, name, action, null, false);
    }

    /**
     * Creates a conditional menu item (only shown if condition passes)
     *
     * @param icon The item icon
     * @param name The display name
     * @param action The action to execute
     * @param condition Condition that must be true to show this item
     * @return A new MenuItem
     */
    public static MenuItem conditional(ItemStack icon, String name, Consumer<Player> action,
                                      Predicate<Player> condition) {
        return new MenuItem(icon, name, action, condition, false);
    }

    /**
     * Creates a separator item (divider)
     *
     * @param icon The separator icon
     * @return A new separator MenuItem
     */
    public static MenuItem separator(ItemStack icon) {
        return new MenuItem(icon, null, null, null, true);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public Consumer<Player> getAction() {
        return action;
    }

    public Predicate<Player> getCondition() {
        return condition;
    }

    public boolean isSeparator() {
        return separator;
    }

    /**
     * Checks if this item should be shown for the given player
     *
     * @param player The player to check
     * @return true if item should be shown
     */
    public boolean shouldShow(Player player) {
        if (separator) return true;
        if (condition == null) return true;
        return condition.test(player);
    }

    /**
     * Executes this item's action
     *
     * @param player The player who clicked
     */
    public void execute(Player player) {
        if (action != null && !separator) {
            action.accept(player);
        }
    }
}
