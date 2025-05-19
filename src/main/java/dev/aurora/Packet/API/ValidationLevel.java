package dev.aurora.Packet.API;

/**
 * Enum defining the levels of validation for packet-based GUIs
 * Each level provides different trade-offs between security and performance
 */
public enum ValidationLevel {

    /**
     * BASIC validation - Uses only Bukkit events (same as standard AuroraGui)
     * - No packet interception
     * - No additional overhead (0ms)
     * - Basic protection only
     *
     * Use for: Non-economy GUIs, cosmetic menus
     */
    BASIC,

    /**
     * PACKET validation - Packet interception + server-side truth tracking
     * - Intercepts WINDOW_CLICK and WINDOW_CLOSE packets
     * - Validates timing (min 50ms between clicks)
     * - Validates cursor state
     * - Server-side inventory truth tracking
     * - Overhead: ~1-2ms per click
     *
     * Use for: Shops, trading, most economy GUIs
     */
    PACKET,

    /**
     * ADVANCED validation - All PACKET features + fingerprinting + transaction verification
     * - Everything from PACKET level
     * - Item fingerprinting (SHA-256 NBT hashing)
     * - Post-click transaction verification
     * - Shift-click loop detection
     * - NBT tampering detection
     * - Overhead: ~3-5ms per click
     *
     * Use for: Banks, premium shops, high-value trading
     */
    ADVANCED;

    /**
     * Checks if this level includes packet interception
     * @return true if PACKET or ADVANCED
     */
    public boolean usesPackets() {
        return this == PACKET || this == ADVANCED;
    }

    /**
     * Checks if this level includes item fingerprinting
     * @return true if ADVANCED
     */
    public boolean usesFingerprinting() {
        return this == ADVANCED;
    }

    /**
     * Gets the recommended minimum click delay for this level
     * @return Milliseconds between clicks
     */
    public int getRecommendedClickDelay() {
        switch (this) {
            case BASIC:
                return 0;
            case PACKET:
                return 50;
            case ADVANCED:
                return 100;
            default:
                return 50;
        }
    }

    /**
     * Gets a human-readable description of this validation level
     * @return Description string
     */
    public String getDescription() {
        switch (this) {
            case BASIC:
                return "Basic Bukkit event validation (0ms overhead)";
            case PACKET:
                return "Packet interception + truth tracking (~1-2ms overhead)";
            case ADVANCED:
                return "Full anti-dupe with fingerprinting (~3-5ms overhead)";
            default:
                return "Unknown";
        }
    }
}
