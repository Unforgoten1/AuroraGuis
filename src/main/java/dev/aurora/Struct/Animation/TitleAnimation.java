package dev.aurora.Struct.Animation;

import dev.aurora.Utilities.Strings.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Animated title that cycles through different frames
 * Used for eye-catching GUI titles
 */
public class TitleAnimation {
    private final List<TitleFrame> frames;
    private int currentFrame;
    private boolean loop;

    /**
     * Represents a single frame in the title animation
     */
    public static class TitleFrame {
        private final String text;
        private final int duration; // ticks

        public TitleFrame(String text, int duration) {
            this.text = ColorUtils.color(text);
            this.duration = duration;
        }

        public String getText() {
            return text;
        }

        public int getDuration() {
            return duration;
        }
    }

    /**
     * Creates a new title animation
     */
    public TitleAnimation() {
        this.frames = new ArrayList<>();
        this.currentFrame = 0;
        this.loop = true;
    }

    /**
     * Adds a frame to the animation
     *
     * @param text The title text (supports color codes)
     * @param durationTicks Duration in ticks (20 ticks = 1 second)
     * @return This animation for chaining
     */
    public TitleAnimation addFrame(String text, int durationTicks) {
        frames.add(new TitleFrame(text, durationTicks));
        return this;
    }

    /**
     * Adds multiple frames with the same duration
     *
     * @param durationTicks Duration for all frames
     * @param texts The frame texts
     * @return This animation for chaining
     */
    public TitleAnimation addFrames(int durationTicks, String... texts) {
        for (String text : texts) {
            addFrame(text, durationTicks);
        }
        return this;
    }

    /**
     * Sets whether the animation should loop
     *
     * @param loop true to loop
     * @return This animation for chaining
     */
    public TitleAnimation setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Gets the current frame
     *
     * @return The current TitleFrame
     */
    public TitleFrame getCurrentFrame() {
        if (frames.isEmpty()) {
            return new TitleFrame("", 20);
        }
        return frames.get(currentFrame);
    }

    /**
     * Gets the current frame text
     *
     * @return The title text
     */
    public String getCurrentText() {
        return getCurrentFrame().getText();
    }

    /**
     * Gets the current frame duration
     *
     * @return Duration in ticks
     */
    public int getCurrentDuration() {
        return getCurrentFrame().getDuration();
    }

    /**
     * Advances to the next frame
     *
     * @return true if animation should continue
     */
    public boolean nextFrame() {
        if (frames.isEmpty()) return false;

        currentFrame++;

        if (currentFrame >= frames.size()) {
            if (loop) {
                currentFrame = 0;
                return true;
            }
            return false;
        }

        return true;
    }

    /**
     * Resets the animation to the first frame
     */
    public void reset() {
        currentFrame = 0;
    }

    /**
     * Gets the number of frames
     *
     * @return Frame count
     */
    public int getFrameCount() {
        return frames.size();
    }

    /**
     * Checks if the animation is set to loop
     *
     * @return true if looping
     */
    public boolean isLoop() {
        return loop;
    }

    // ==================== Pre-built Animations ====================

    /**
     * Creates a pulsing animation that alternates between normal and bold
     *
     * @param text The base text
     * @return TitleAnimation
     */
    public static TitleAnimation pulse(String text) {
        return new TitleAnimation()
                .addFrame(text, 10)
                .addFrame("&l" + text, 10)
                .setLoop(true);
    }

    /**
     * Creates a color-shifting animation
     *
     * @param text The base text
     * @param colors The color codes to cycle through
     * @return TitleAnimation
     */
    public static TitleAnimation colorShift(String text, String... colors) {
        TitleAnimation anim = new TitleAnimation();
        for (String color : colors) {
            anim.addFrame(color + text, 15);
        }
        return anim.setLoop(true);
    }

    /**
     * Creates a rainbow animation
     *
     * @param text The base text
     * @return TitleAnimation
     */
    public static TitleAnimation rainbow(String text) {
        return colorShift(text, "&c", "&6", "&e", "&a", "&b", "&d");
    }

    /**
     * Creates a typewriter-style animation
     *
     * @param text The full text to type out
     * @return TitleAnimation
     */
    public static TitleAnimation typewriter(String text) {
        TitleAnimation anim = new TitleAnimation();
        String cleanText = ColorUtils.color(text);

        for (int i = 1; i <= cleanText.length(); i++) {
            anim.addFrame(cleanText.substring(0, i), 2);
        }

        return anim.setLoop(false);
    }

    /**
     * Creates a blinking animation
     *
     * @param text The text
     * @return TitleAnimation
     */
    public static TitleAnimation blink(String text) {
        return new TitleAnimation()
                .addFrame(text, 20)
                .addFrame("", 10)
                .setLoop(true);
    }

    /**
     * Creates a gradient shift animation between two colors
     *
     * @param text The text
     * @param color1 First color code
     * @param color2 Second color code
     * @return TitleAnimation
     */
    public static TitleAnimation gradient(String text, String color1, String color2) {
        return new TitleAnimation()
                .addFrame(color1 + text, 10)
                .addFrame(color2 + text, 10)
                .setLoop(true);
    }

    @Override
    public String toString() {
        return "TitleAnimation{frames=" + frames.size() +
               ", currentFrame=" + currentFrame +
               ", loop=" + loop + "}";
    }
}
