package dev.aurora.Compatibility;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cross-version Sound compatibility wrapper
 * Handles sound name changes across Minecraft versions
 */
public enum XSound {
    // UI Sounds
    UI_BUTTON_CLICK("CLICK", "UI_BUTTON_CLICK"),
    BLOCK_NOTE_BLOCK_PLING("NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING"),

    // Entity Sounds
    ENTITY_PLAYER_LEVELUP("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"),
    ENTITY_VILLAGER_NO("VILLAGER_NO", "ENTITY_VILLAGER_NO"),
    ENTITY_VILLAGER_YES("VILLAGER_YES", "ENTITY_VILLAGER_YES"),
    ENTITY_ITEM_PICKUP("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"),
    ENTITY_EXPERIENCE_ORB_PICKUP("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"),
    ENTITY_BAT_TAKEOFF("BAT_TAKEOFF", "ENTITY_BAT_TAKEOFF"),

    // Block Sounds
    BLOCK_CHEST_OPEN("CHEST_OPEN", "BLOCK_CHEST_OPEN"),
    BLOCK_CHEST_CLOSE("CHEST_CLOSE", "BLOCK_CHEST_CLOSE"),
    BLOCK_ANVIL_USE("ANVIL_USE", "BLOCK_ANVIL_USE"),
    BLOCK_ANVIL_LAND("ANVIL_LAND", "BLOCK_ANVIL_LAND"),

    // Item Sounds
    ITEM_BOOK_PAGE_TURN("ITEM_BOOK_PAGE_TURN"),

    // Note Sounds
    BLOCK_NOTE_BLOCK_BASS("NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BASS"),
    BLOCK_NOTE_BLOCK_GUITAR("NOTE_BASS_GUITAR", "BLOCK_NOTE_BLOCK_GUITAR", "BLOCK_NOTE_GUITAR"),
    BLOCK_NOTE_BLOCK_HARP("NOTE_PIANO", "BLOCK_NOTE_BLOCK_HARP", "BLOCK_NOTE_HARP"),

    // Ambience
    ENTITY_ENDERMAN_TELEPORT("ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT"),
    ENTITY_EXPERIENCE_BOTTLE_THROW("SPLASH", "ENTITY_EXPERIENCE_BOTTLE_THROW"),

    // Combat
    ENTITY_ARROW_HIT("ARROW_HIT", "ENTITY_ARROW_HIT"),
    ENTITY_ARROW_SHOOT("SHOOT_ARROW", "ENTITY_ARROW_SHOOT"),

    // Environment
    BLOCK_FIRE_EXTINGUISH("FIZZ", "BLOCK_FIRE_EXTINGUISH"),
    ENTITY_GENERIC_EXPLODE("EXPLODE", "ENTITY_GENERIC_EXPLODE"),

    // Animals
    ENTITY_CAT_MEOW("CAT_MEOW", "ENTITY_CAT_MEOW"),
    ENTITY_WOLF_BARK("WOLF_BARK", "ENTITY_WOLF_BARK"),
    ENTITY_CHICKEN_EGG("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG"),

    // Weather
    ENTITY_LIGHTNING_BOLT_THUNDER("AMBIENCE_THUNDER", "ENTITY_LIGHTNING_BOLT_THUNDER"),
    ENTITY_LIGHTNING_BOLT_IMPACT("AMBIENCE_THUNDER", "ENTITY_LIGHTNING_BOLT_IMPACT"),

    // Misc
    BLOCK_PORTAL_TRAVEL("PORTAL_TRAVEL", "BLOCK_PORTAL_TRAVEL"),
    BLOCK_PORTAL_TRIGGER("PORTAL_TRIGGER", "BLOCK_PORTAL_TRIGGER"),
    ENTITY_PLAYER_SPLASH("SPLASH", "ENTITY_PLAYER_SPLASH"),
    ENTITY_PLAYER_SWIM("SWIM", "ENTITY_PLAYER_SWIM"),

    // 1.9+ sounds
    ENTITY_SHULKER_BULLET_HIT("ENTITY_SHULKER_BULLET_HIT"),
    ENTITY_SHULKER_OPEN("ENTITY_SHULKER_OPEN"),

    // 1.11+ sounds
    ENTITY_LLAMA_SPIT("ENTITY_LLAMA_SPIT"),

    // 1.13+ sounds
    BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT("BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT"),
    BLOCK_CONDUIT_AMBIENT("BLOCK_CONDUIT_AMBIENT"),

    // 1.14+ sounds
    BLOCK_BELL_USE("BLOCK_BELL_USE"),
    BLOCK_GRINDSTONE_USE("BLOCK_GRINDSTONE_USE"),

    // 1.16+ sounds
    BLOCK_ANCIENT_DEBRIS_BREAK("BLOCK_ANCIENT_DEBRIS_BREAK"),
    BLOCK_NETHERITE_BLOCK_PLACE("BLOCK_NETHERITE_BLOCK_PLACE"),

    // 1.17+ sounds
    BLOCK_AMETHYST_BLOCK_CHIME("BLOCK_AMETHYST_BLOCK_CHIME"),

    // 1.19+ sounds
    BLOCK_SCULK_SENSOR_CLICKING("BLOCK_SCULK_SENSOR_CLICKING"),
    ENTITY_WARDEN_HEARTBEAT("ENTITY_WARDEN_HEARTBEAT");

    private final String[] names;
    private Sound sound;

    private static final Map<String, XSound> NAME_MAP = new HashMap<>();

    static {
        for (XSound xSound : values()) {
            for (String name : xSound.names) {
                NAME_MAP.put(name.toUpperCase(), xSound);
            }
        }
    }

    XSound(String... names) {
        this.names = names;
    }

    /**
     * Parse to Bukkit Sound
     */
    public Sound parseSound() {
        if (sound != null) return sound;

        // Try each name until we find one that works
        for (String name : names) {
            try {
                Sound s = Sound.valueOf(name);
                if (s != null) {
                    sound = s;
                    return sound;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Return null if not supported on this version
        return null;
    }

    /**
     * Check if this sound exists on current server version
     */
    public boolean isSupported() {
        return parseSound() != null;
    }

    /**
     * Play sound to player
     */
    public void play(Player player, float volume, float pitch) {
        Sound s = parseSound();
        if (s != null && player != null) {
            player.playSound(player.getLocation(), s, volume, pitch);
        }
    }

    /**
     * Play sound to player with default volume/pitch
     */
    public void play(Player player) {
        play(player, 1.0f, 1.0f);
    }

    /**
     * Play sound at location
     */
    public void play(Location location, float volume, float pitch) {
        Sound s = parseSound();
        if (s != null && location != null && location.getWorld() != null) {
            location.getWorld().playSound(location, s, volume, pitch);
        }
    }

    /**
     * Play sound at location with default volume/pitch
     */
    public void play(Location location) {
        play(location, 1.0f, 1.0f);
    }

    /**
     * Match XSound from string name
     */
    public static Optional<XSound> matchXSound(String name) {
        if (name == null) return Optional.empty();

        XSound sound = NAME_MAP.get(name.toUpperCase());
        return Optional.ofNullable(sound);
    }

    /**
     * Match XSound from Bukkit Sound
     */
    public static Optional<XSound> matchXSound(Sound sound) {
        if (sound == null) return Optional.empty();

        for (XSound xSound : values()) {
            if (xSound.parseSound() == sound) {
                return Optional.of(xSound);
            }
        }

        return Optional.empty();
    }
}
