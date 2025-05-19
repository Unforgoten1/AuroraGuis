package dev.aurora.ResourcePack;

import dev.aurora.Utilities.Strings.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent API for creating pixel-perfect GUI titles with custom fonts and spacing.
 * <p>
 * TitleBuilder allows precise control over title formatting by combining text,
 * special characters, and negative/positive spacing to achieve pixel-perfect alignment.
 * </p>
 * <p>
 * The default Minecraft inventory title width is 176 pixels.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * String title = new TitleBuilder()
 *     .icon(FontCharacters.STAR)
 *     .space(4)
 *     .text("&6&lShop")
 *     .space(4)
 *     .icon(FontCharacters.STAR)
 *     .center()
 *     .build();
 * </pre>
 * </p>
 *
 * @since 1.1.0
 */
public class TitleBuilder {

    /**
     * Default inventory title width in pixels (Minecraft default).
     */
    public static final int DEFAULT_TITLE_WIDTH = 176;

    /**
     * Width of bold characters increases by this amount.
     */
    private static final int BOLD_WIDTH_INCREASE = 1;

    private final List<TitleSegment> segments = new ArrayList<>();
    private boolean centered = false;

    /**
     * Adds a text segment to the title.
     * <p>
     * Supports Minecraft color codes using & symbol (e.g., "&6&lShop").
     * </p>
     *
     * @param text The text to add (supports color codes)
     * @return This builder for chaining
     */
    public TitleBuilder text(String text) {
        if (text != null && !text.isEmpty()) {
            segments.add(new TitleSegment(SegmentType.TEXT, ColorUtils.color(text)));
        }
        return this;
    }

    /**
     * Adds a special character icon to the title.
     *
     * @param character The FontCharacter to add
     * @return This builder for chaining
     */
    public TitleBuilder icon(FontCharacters character) {
        if (character != null) {
            segments.add(new TitleSegment(SegmentType.ICON, String.valueOf(character.getChar())));
        }
        return this;
    }

    /**
     * Adds spacing to the title.
     * <p>
     * Positive values add space, negative values remove space (shift backward).
     * </p>
     *
     * @param pixels Number of pixels of spacing (can be negative)
     * @return This builder for chaining
     */
    public TitleBuilder space(int pixels) {
        if (pixels != 0) {
            String spaceString;
            if (pixels > 0) {
                spaceString = FontCharacters.positiveSpace(pixels);
            } else {
                spaceString = FontCharacters.negativeSpace(-pixels);
            }
            segments.add(new TitleSegment(SegmentType.SPACE, spaceString));
        }
        return this;
    }

    /**
     * Adds a custom Unicode character to the title.
     *
     * @param character The Unicode character to add
     * @return This builder for chaining
     */
    public TitleBuilder character(char character) {
        segments.add(new TitleSegment(SegmentType.ICON, String.valueOf(character)));
        return this;
    }

    /**
     * Centers the title within the default inventory title width (176 pixels).
     * <p>
     * This calculates the total width of all segments and adds equal padding
     * on both sides to center the content.
     * </p>
     *
     * @return This builder for chaining
     */
    public TitleBuilder center() {
        this.centered = true;
        return this;
    }

    /**
     * Centers the title within a custom width.
     *
     * @param totalWidth The total width in pixels to center within
     * @return This builder for chaining
     */
    public TitleBuilder center(int totalWidth) {
        this.centered = true;
        // We'll handle custom width in build()
        return this;
    }

    /**
     * Builds the final title string.
     * <p>
     * If center() was called, the title will be padded to center it within
     * the default inventory title width.
     * </p>
     *
     * @return The formatted title string ready for use in GUIs
     */
    public String build() {
        return build(DEFAULT_TITLE_WIDTH);
    }

    /**
     * Builds the final title string centered within a custom width.
     *
     * @param totalWidth The total width in pixels to center within
     * @return The formatted title string
     */
    public String build(int totalWidth) {
        if (segments.isEmpty()) {
            return "";
        }

        // Build the base title
        StringBuilder title = new StringBuilder();
        for (TitleSegment segment : segments) {
            title.append(segment.content);
        }

        // If centering, calculate padding
        if (centered) {
            int contentWidth = calculateWidth(title.toString());
            int paddingNeeded = (totalWidth - contentWidth) / 2;

            if (paddingNeeded > 0) {
                // Add negative space at the start to center
                String padding = FontCharacters.positiveSpace(paddingNeeded);
                title.insert(0, padding);
            }
        }

        return title.toString();
    }

    /**
     * Calculates the pixel width of a string using DefaultFontInfo.
     * <p>
     * Takes into account Minecraft color codes and bold formatting.
     * </p>
     *
     * @param text The text to measure
     * @return The pixel width
     */
    private int calculateWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int width = 0;
        boolean isBold = false;
        boolean nextIsColorCode = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Check for color code
            if (c == '§' || c == '&') {
                nextIsColorCode = true;
                continue;
            }

            if (nextIsColorCode) {
                nextIsColorCode = false;
                // Check if this is bold formatting
                if (c == 'l' || c == 'L') {
                    isBold = true;
                } else if (c == 'r' || c == 'R') {
                    // Reset formatting
                    isBold = false;
                } else if ("0123456789abcdefABCDEF".indexOf(c) >= 0) {
                    // Color code (resets bold)
                    isBold = false;
                }
                continue;
            }

            // Get character width
            ColorUtils.DefaultFontInfo fontInfo = ColorUtils.DefaultFontInfo.getDefaultFontInfo(c);
            width += isBold ? fontInfo.getBoldLength() : fontInfo.getLength();

            // Add 1 pixel spacing between characters
            if (i < text.length() - 1) {
                width += 1;
            }
        }

        return width;
    }

    /**
     * Clears all segments from this builder.
     *
     * @return This builder for chaining
     */
    public TitleBuilder clear() {
        segments.clear();
        centered = false;
        return this;
    }

    /**
     * Gets the current pixel width of the title (without centering applied).
     *
     * @return The pixel width
     */
    public int getWidth() {
        StringBuilder temp = new StringBuilder();
        for (TitleSegment segment : segments) {
            temp.append(segment.content);
        }
        return calculateWidth(temp.toString());
    }

    /**
     * Internal class representing a title segment.
     */
    private static class TitleSegment {
        final SegmentType type;
        final String content;

        TitleSegment(SegmentType type, String content) {
            this.type = type;
            this.content = content;
        }
    }

    /**
     * Types of title segments.
     */
    private enum SegmentType {
        TEXT,
        ICON,
        SPACE
    }
}
