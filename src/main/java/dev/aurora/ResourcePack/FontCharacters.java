package dev.aurora.ResourcePack;

/**
 * Enum of special characters for GUI titles and text.
 * <p>
 * Provides Unicode symbols and negative space characters for pixel-perfect
 * title alignment in Minecraft GUIs. Custom resource pack characters can be
 * configured via ResourcePackConfig.
 * </p>
 *
 * @since 1.1.0
 */
public enum FontCharacters {

    // Unicode Symbols
    HEART('\u2764', 9),
    STAR('\u2605', 8),
    CHECKMARK('\u2713', 8),
    CROSS('\u2717', 8),
    ARROW_LEFT('\u2190', 7),
    ARROW_RIGHT('\u2192', 7),
    ARROW_UP('\u2191', 7),
    ARROW_DOWN('\u2193', 7),
    BULLET('\u2022', 4),
    CIRCLE('\u25CF', 7),
    DIAMOND('\u25C6', 7),
    SQUARE('\u25A0', 7),
    MUSIC('\u266B', 7),
    SKULL('\u2620', 9),

    // Negative space characters for pixel-perfect alignment
    // These move text backward to allow precise positioning
    SPACE_NEG_1('\uF801', -1),
    SPACE_NEG_2('\uF802', -2),
    SPACE_NEG_3('\uF803', -3),
    SPACE_NEG_4('\uF804', -4),
    SPACE_NEG_5('\uF805', -5),
    SPACE_NEG_6('\uF806', -6),
    SPACE_NEG_7('\uF807', -7),
    SPACE_NEG_8('\uF808', -8),
    SPACE_NEG_16('\uF809', -16),
    SPACE_NEG_32('\uF80A', -32),
    SPACE_NEG_64('\uF80B', -64),
    SPACE_NEG_128('\uF80C', -128),

    // Positive space characters (wider gaps)
    SPACE_1('\uF821', 1),
    SPACE_2('\uF822', 2),
    SPACE_3('\uF823', 3),
    SPACE_4('\uF824', 4),
    SPACE_5('\uF825', 5),
    SPACE_8('\uF828', 8),
    SPACE_16('\uF829', 16),
    SPACE_32('\uF82A', 32),
    SPACE_64('\uF82B', 64),
    SPACE_128('\uF82C', 128);

    private final char character;
    private final int width; // Approximate pixel width in default Minecraft font

    /**
     * Creates a FontCharacter with the given character and width.
     *
     * @param character The Unicode character
     * @param width The approximate pixel width (can be negative for spacing)
     */
    FontCharacters(char character, int width) {
        this.character = character;
        this.width = width;
    }

    /**
     * Gets the Unicode character.
     *
     * @return The character
     */
    public char getChar() {
        return character;
    }

    /**
     * Gets the approximate pixel width of this character.
     * <p>
     * Negative widths indicate backward spacing (negative space).
     * </p>
     *
     * @return The pixel width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Repeats this character n times.
     *
     * @param times Number of times to repeat
     * @return A string with the character repeated
     */
    public String repeat(int times) {
        if (times <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(times);
        for (int i = 0; i < times; i++) {
            sb.append(character);
        }
        return sb.toString();
    }

    /**
     * Creates a negative space string that shifts text backward by the specified pixels.
     * <p>
     * Uses the most efficient combination of negative space characters.
     * </p>
     *
     * @param pixels Number of pixels to shift backward (positive number)
     * @return A string of negative space characters
     */
    public static String negativeSpace(int pixels) {
        if (pixels <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int remaining = pixels;

        // Use largest negative spaces first for efficiency
        while (remaining >= 128) {
            result.append(SPACE_NEG_128.character);
            remaining -= 128;
        }
        while (remaining >= 64) {
            result.append(SPACE_NEG_64.character);
            remaining -= 64;
        }
        while (remaining >= 32) {
            result.append(SPACE_NEG_32.character);
            remaining -= 32;
        }
        while (remaining >= 16) {
            result.append(SPACE_NEG_16.character);
            remaining -= 16;
        }
        while (remaining >= 8) {
            result.append(SPACE_NEG_8.character);
            remaining -= 8;
        }
        while (remaining >= 7) {
            result.append(SPACE_NEG_7.character);
            remaining -= 7;
        }
        while (remaining >= 6) {
            result.append(SPACE_NEG_6.character);
            remaining -= 6;
        }
        while (remaining >= 5) {
            result.append(SPACE_NEG_5.character);
            remaining -= 5;
        }
        while (remaining >= 4) {
            result.append(SPACE_NEG_4.character);
            remaining -= 4;
        }
        while (remaining >= 3) {
            result.append(SPACE_NEG_3.character);
            remaining -= 3;
        }
        while (remaining >= 2) {
            result.append(SPACE_NEG_2.character);
            remaining -= 2;
        }
        while (remaining >= 1) {
            result.append(SPACE_NEG_1.character);
            remaining -= 1;
        }

        return result.toString();
    }

    /**
     * Creates a positive space string that adds the specified pixels of spacing.
     * <p>
     * Uses the most efficient combination of positive space characters.
     * </p>
     *
     * @param pixels Number of pixels of spacing to add
     * @return A string of positive space characters
     */
    public static String positiveSpace(int pixels) {
        if (pixels <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int remaining = pixels;

        // Use largest positive spaces first for efficiency
        while (remaining >= 128) {
            result.append(SPACE_128.character);
            remaining -= 128;
        }
        while (remaining >= 64) {
            result.append(SPACE_64.character);
            remaining -= 64;
        }
        while (remaining >= 32) {
            result.append(SPACE_32.character);
            remaining -= 32;
        }
        while (remaining >= 16) {
            result.append(SPACE_16.character);
            remaining -= 16;
        }
        while (remaining >= 8) {
            result.append(SPACE_8.character);
            remaining -= 8;
        }
        while (remaining >= 5) {
            result.append(SPACE_5.character);
            remaining -= 5;
        }
        while (remaining >= 4) {
            result.append(SPACE_4.character);
            remaining -= 4;
        }
        while (remaining >= 3) {
            result.append(SPACE_3.character);
            remaining -= 3;
        }
        while (remaining >= 2) {
            result.append(SPACE_2.character);
            remaining -= 2;
        }
        while (remaining >= 1) {
            result.append(SPACE_1.character);
            remaining -= 1;
        }

        return result.toString();
    }

    @Override
    public String toString() {
        return String.valueOf(character);
    }
}
