package dev.aurora.Utilities.Sound;

import dev.aurora.Compatibility.XSound;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Utility for playing sound effects with GUI interactions
 * Provides common pre-configured sounds for different actions
 * Cross-version compatible (1.8-1.21.1)
 */
public class SoundEffect {
    private final XSound xSound;
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundEffect(XSound xSound, float volume, float pitch) {
        this.xSound = xSound;
        this.sound = xSound != null ? xSound.parseSound() : null;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Deprecated
    public SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.xSound = null;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void play(Player player) {
        if (player == null) return;

        if (xSound != null && xSound.isSupported()) {
            xSound.play(player, volume, pitch);
        } else if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    // Pre-configured sounds for common actions (cross-version)
    public static final SoundEffect CLICK = new SoundEffect(XSound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    public static final SoundEffect SUCCESS = new SoundEffect(XSound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    public static final SoundEffect ERROR = new SoundEffect(XSound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    public static final SoundEffect OPEN = new SoundEffect(XSound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
    public static final SoundEffect CLOSE = new SoundEffect(XSound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
    public static final SoundEffect PAGE_TURN = new SoundEffect(XSound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    public static final SoundEffect WARNING = new SoundEffect(XSound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    public static final SoundEffect WHOOSH = new SoundEffect(XSound.ENTITY_BAT_TAKEOFF, 1.0f, 1.5f);
    public static final SoundEffect PLING = new SoundEffect(XSound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);

    /**
     * Create custom sound effect from XSound (recommended)
     */
    public static SoundEffect of(XSound xSound, float volume, float pitch) {
        return new SoundEffect(xSound, volume, pitch);
    }

    /**
     * Create custom sound effect from legacy Sound (deprecated)
     */
    @Deprecated
    public static SoundEffect custom(Sound sound, float volume, float pitch) {
        return new SoundEffect(sound, volume, pitch);
    }

    public Sound getSound() {
        return sound;
    }

    public XSound getXSound() {
        return xSound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isSupported() {
        if (xSound != null) {
            return xSound.isSupported();
        }
        return sound != null;
    }
}
