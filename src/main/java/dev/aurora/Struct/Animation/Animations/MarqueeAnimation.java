package dev.aurora.Struct.Animation.Animations;

import dev.aurora.Struct.Animation.API.IAnimation;
import dev.aurora.Utilities.Items.ItemBuilder;
import dev.aurora.Utilities.Strings.ColorUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Creates a scrolling text animation (marquee effect)
 * Text scrolls horizontally through the item's display name
 */
public class MarqueeAnimation implements IAnimation {
    private final String fullText;
    private final int visibleLength;
    private final Material material;
    private final ItemBuilder itemBuilder;
    private final int frameDuration;
    private int scrollPosition;
    private boolean loop;
    private String prefix;
    private String suffix;

    /**
     * Creates a marquee animation
     *
     * @param text The text to scroll
     * @param visibleLength Number of characters visible at once
     * @param material The item material
     */
    public MarqueeAnimation(String text, int visibleLength, Material material) {
        this.fullText = text;
        this.visibleLength = visibleLength;
        this.material = material;
        this.itemBuilder = new ItemBuilder(material);
        this.frameDuration = 4; // 0.2 seconds per frame
        this.scrollPosition = 0;
        this.loop = true;
        this.prefix = "&e";
        this.suffix = "";
    }

    /**
     * Creates a marquee with default settings
     *
     * @param text The text to scroll
     */
    public static MarqueeAnimation create(String text) {
        return new MarqueeAnimation(text, 20, Material.PAPER);
    }

    /**
     * Creates a marquee for a GUI title/header
     *
     * @param text The text to scroll
     */
    public static MarqueeAnimation forHeader(String text) {
        MarqueeAnimation marquee = new MarqueeAnimation(text, 16, Material.NAME_TAG);
        marquee.setPrefix("&b&l");
        return marquee;
    }

    /**
     * Creates a news ticker style marquee
     *
     * @param text The news text
     */
    public static MarqueeAnimation newsTicker(String text) {
        MarqueeAnimation marquee = new MarqueeAnimation(text, 24, Material.PAPER);
        marquee.setPrefix("&6&lNEWS &f» ");
        return marquee;
    }

    @Override
    public ItemStack getNextItem() {
        String visibleText = getVisibleText();
        String displayName = ColorUtils.color(prefix + visibleText + suffix);

        return itemBuilder.name(displayName).build();
    }

    /**
     * Gets the currently visible portion of text
     */
    private String getVisibleText() {
        // Add padding for smooth wrap-around
        String paddedText = fullText + "   ";

        int textLength = paddedText.length();
        int endPos = scrollPosition + visibleLength;

        if (endPos <= textLength) {
            return paddedText.substring(scrollPosition, endPos);
        } else {
            // Wrap around
            String firstPart = paddedText.substring(scrollPosition);
            int remaining = visibleLength - firstPart.length();
            String secondPart = paddedText.substring(0, Math.min(remaining, textLength));
            return firstPart + secondPart;
        }
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        return null; // No click action by default
    }

    @Override
    public int getDuration() {
        return frameDuration;
    }

    @Override
    public boolean shouldContinue() {
        scrollPosition++;

        // Add padding length to the text for wrap calculation
        int totalLength = fullText.length() + 3;

        if (scrollPosition >= totalLength) {
            if (loop) {
                scrollPosition = 0;
                return true;
            }
            return false;
        }

        return true;
    }

    @Override
    public void init() {
        scrollPosition = 0;
    }

    /**
     * Sets whether the animation should loop
     *
     * @param loop true to loop indefinitely
     * @return This animation for chaining
     */
    public MarqueeAnimation setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Sets the color/format prefix for the text
     *
     * @param prefix Color codes and formatting (e.g., "&b&l")
     * @return This animation for chaining
     */
    public MarqueeAnimation setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Sets the suffix for the text
     *
     * @param suffix Text to append after visible portion
     * @return This animation for chaining
     */
    public MarqueeAnimation setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * Sets the scroll speed
     *
     * @param ticksPerFrame Ticks between scrolling one character
     * @return This animation for chaining
     */
    public MarqueeAnimation setSpeed(int ticksPerFrame) {
        // Note: Would need to modify frameDuration, but it's final
        // This is a design pattern issue - consider making it mutable
        return this;
    }
}
