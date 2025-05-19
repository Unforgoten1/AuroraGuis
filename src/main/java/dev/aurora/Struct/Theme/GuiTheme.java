package dev.aurora.Struct.Theme;

import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Defines a visual theme for GUIs
 * Provides consistent colors, materials, and styling
 */
public class GuiTheme {
    private final String name;
    private final Material background;
    private final Material accent;
    private final Material success;
    private final Material error;
    private final Material warning;
    private final Material info;
    private final String primaryColor;
    private final String secondaryColor;
    private final String textColor;

    private GuiTheme(Builder builder) {
        this.name = builder.name;
        this.background = builder.background;
        this.accent = builder.accent;
        this.success = builder.success;
        this.error = builder.error;
        this.warning = builder.warning;
        this.info = builder.info;
        this.primaryColor = builder.primaryColor;
        this.secondaryColor = builder.secondaryColor;
        this.textColor = builder.textColor;
    }

    /**
     * Creates a themed background item
     *
     * @return Background ItemStack
     */
    public ItemStack createBackground() {
        return new ItemBuilder(background)
                .name(" ")
                .build();
    }

    /**
     * Creates a themed accent item
     *
     * @param name The item name
     * @return Accent ItemStack
     */
    public ItemStack createAccent(String name) {
        return new ItemBuilder(accent)
                .name(primaryColor + name)
                .build();
    }

    /**
     * Creates a themed success item
     *
     * @param name The item name
     * @return Success ItemStack
     */
    public ItemStack createSuccess(String name) {
        return new ItemBuilder(success)
                .name("&a" + name)
                .build();
    }

    /**
     * Creates a themed error item
     *
     * @param name The item name
     * @return Error ItemStack
     */
    public ItemStack createError(String name) {
        return new ItemBuilder(error)
                .name("&c" + name)
                .build();
    }

    /**
     * Creates a themed warning item
     *
     * @param name The item name
     * @return Warning ItemStack
     */
    public ItemStack createWarning(String name) {
        return new ItemBuilder(warning)
                .name("&e" + name)
                .build();
    }

    /**
     * Creates a themed info item
     *
     * @param name The item name
     * @return Info ItemStack
     */
    public ItemStack createInfo(String name) {
        return new ItemBuilder(info)
                .name("&b" + name)
                .build();
    }

    /**
     * Applies theme color to text
     *
     * @param text The text
     * @return Colored text
     */
    public String colorText(String text) {
        return textColor + text;
    }

    /**
     * Applies primary color to text
     *
     * @param text The text
     * @return Colored text
     */
    public String colorPrimary(String text) {
        return primaryColor + text;
    }

    /**
     * Applies secondary color to text
     *
     * @param text The text
     * @return Colored text
     */
    public String colorSecondary(String text) {
        return secondaryColor + text;
    }

    // Getters
    public String getName() { return name; }
    public Material getBackground() { return background; }
    public Material getAccent() { return accent; }
    public Material getSuccess() { return success; }
    public Material getError() { return error; }
    public Material getWarning() { return warning; }
    public Material getInfo() { return info; }
    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public String getTextColor() { return textColor; }

    /**
     * Builder for creating themes
     */
    public static class Builder {
        private String name = "custom";
        private Material background = Material.BLACK_STAINED_GLASS_PANE;
        private Material accent = Material.PURPLE_STAINED_GLASS_PANE;
        private Material success = Material.LIME_STAINED_GLASS_PANE;
        private Material error = Material.RED_STAINED_GLASS_PANE;
        private Material warning = Material.ORANGE_STAINED_GLASS_PANE;
        private Material info = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        private String primaryColor = "&d";
        private String secondaryColor = "&5";
        private String textColor = "&f";

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder background(Material material) {
            this.background = material;
            return this;
        }

        public Builder accent(Material material) {
            this.accent = material;
            return this;
        }

        public Builder success(Material material) {
            this.success = material;
            return this;
        }

        public Builder error(Material material) {
            this.error = material;
            return this;
        }

        public Builder warning(Material material) {
            this.warning = material;
            return this;
        }

        public Builder info(Material material) {
            this.info = material;
            return this;
        }

        public Builder primaryColor(String color) {
            this.primaryColor = color;
            return this;
        }

        public Builder secondaryColor(String color) {
            this.secondaryColor = color;
            return this;
        }

        public Builder textColor(String color) {
            this.textColor = color;
            return this;
        }

        public GuiTheme build() {
            return new GuiTheme(this);
        }
    }

    // ==================== Default Themes ====================

    /**
     * Dark purple theme (default)
     */
    public static GuiTheme DARK = new Builder()
            .name("dark")
            .background(Material.BLACK_STAINED_GLASS_PANE)
            .accent(Material.PURPLE_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.ORANGE_STAINED_GLASS_PANE)
            .info(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .primaryColor("&d")
            .secondaryColor("&5")
            .textColor("&f")
            .build();

    /**
     * Light theme with white/gray
     */
    public static GuiTheme LIGHT = new Builder()
            .name("light")
            .background(Material.WHITE_STAINED_GLASS_PANE)
            .accent(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.YELLOW_STAINED_GLASS_PANE)
            .info(Material.CYAN_STAINED_GLASS_PANE)
            .primaryColor("&f")
            .secondaryColor("&7")
            .textColor("&8")
            .build();

    /**
     * Ocean blue theme
     */
    public static GuiTheme OCEAN = new Builder()
            .name("ocean")
            .background(Material.CYAN_STAINED_GLASS_PANE)
            .accent(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.YELLOW_STAINED_GLASS_PANE)
            .info(Material.BLUE_STAINED_GLASS_PANE)
            .primaryColor("&b")
            .secondaryColor("&3")
            .textColor("&f")
            .build();

    /**
     * Forest green theme
     */
    public static GuiTheme FOREST = new Builder()
            .name("forest")
            .background(Material.GREEN_STAINED_GLASS_PANE)
            .accent(Material.LIME_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.YELLOW_STAINED_GLASS_PANE)
            .info(Material.CYAN_STAINED_GLASS_PANE)
            .primaryColor("&a")
            .secondaryColor("&2")
            .textColor("&f")
            .build();

    /**
     * Fire red/orange theme
     */
    public static GuiTheme FIRE = new Builder()
            .name("fire")
            .background(Material.RED_STAINED_GLASS_PANE)
            .accent(Material.ORANGE_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.YELLOW_STAINED_GLASS_PANE)
            .info(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .primaryColor("&c")
            .secondaryColor("&6")
            .textColor("&f")
            .build();

    /**
     * Nether theme with purple/red
     */
    public static GuiTheme NETHER = new Builder()
            .name("nether")
            .background(Material.MAGENTA_STAINED_GLASS_PANE)
            .accent(Material.PURPLE_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.ORANGE_STAINED_GLASS_PANE)
            .info(Material.PINK_STAINED_GLASS_PANE)
            .primaryColor("&5")
            .secondaryColor("&c")
            .textColor("&f")
            .build();

    /**
     * End theme with black/purple
     */
    public static GuiTheme END = new Builder()
            .name("end")
            .background(Material.BLACK_STAINED_GLASS_PANE)
            .accent(Material.PURPLE_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.MAGENTA_STAINED_GLASS_PANE)
            .info(Material.PINK_STAINED_GLASS_PANE)
            .primaryColor("&5")
            .secondaryColor("&d")
            .textColor("&f")
            .build();

    /**
     * Gold/yellow luxurious theme
     */
    public static GuiTheme GOLD = new Builder()
            .name("gold")
            .background(Material.YELLOW_STAINED_GLASS_PANE)
            .accent(Material.ORANGE_STAINED_GLASS_PANE)
            .success(Material.LIME_STAINED_GLASS_PANE)
            .error(Material.RED_STAINED_GLASS_PANE)
            .warning(Material.ORANGE_STAINED_GLASS_PANE)
            .info(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .primaryColor("&6")
            .secondaryColor("&e")
            .textColor("&f")
            .build();
}
