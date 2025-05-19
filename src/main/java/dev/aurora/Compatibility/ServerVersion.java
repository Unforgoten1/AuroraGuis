package dev.aurora.Compatibility;

import org.bukkit.Bukkit;

/**
 * Server version detection and comparison utility
 * Supports Minecraft 1.8 through 1.21.1
 */
public class ServerVersion {
    private static ServerVersion instance;

    private final String versionString;
    private final int major;
    private final int minor;
    private final int patch;
    private final String nmsVersion;

    // Version constants
    public static final ServerVersion V1_8 = new ServerVersion(1, 8, 0);
    public static final ServerVersion V1_9 = new ServerVersion(1, 9, 0);
    public static final ServerVersion V1_10 = new ServerVersion(1, 10, 0);
    public static final ServerVersion V1_11 = new ServerVersion(1, 11, 0);
    public static final ServerVersion V1_12 = new ServerVersion(1, 12, 0);
    public static final ServerVersion V1_13 = new ServerVersion(1, 13, 0);
    public static final ServerVersion V1_14 = new ServerVersion(1, 14, 0);
    public static final ServerVersion V1_15 = new ServerVersion(1, 15, 0);
    public static final ServerVersion V1_16 = new ServerVersion(1, 16, 0);
    public static final ServerVersion V1_17 = new ServerVersion(1, 17, 0);
    public static final ServerVersion V1_18 = new ServerVersion(1, 18, 0);
    public static final ServerVersion V1_19 = new ServerVersion(1, 19, 0);
    public static final ServerVersion V1_20 = new ServerVersion(1, 20, 0);
    public static final ServerVersion V1_21 = new ServerVersion(1, 21, 0);

    private ServerVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.versionString = major + "." + minor + "." + patch;
        this.nmsVersion = null;
    }

    private ServerVersion() {
        this.versionString = Bukkit.getVersion();
        this.nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        // Parse version from string like "git-Paper-123 (MC: 1.20.1)"
        String version = extractVersion();
        String[] parts = version.split("\\.");

        this.major = parts.length > 0 ? parseInt(parts[0]) : 1;
        this.minor = parts.length > 1 ? parseInt(parts[1]) : 8;
        this.patch = parts.length > 2 ? parseInt(parts[2]) : 0;
    }

    public static ServerVersion getInstance() {
        if (instance == null) {
            instance = new ServerVersion();
        }
        return instance;
    }

    private String extractVersion() {
        String version = Bukkit.getBukkitVersion(); // e.g., "1.20.1-R0.1-SNAPSHOT"

        if (version.contains("-")) {
            version = version.split("-")[0];
        }

        return version;
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Check if server version is at least the specified version
     */
    public boolean isAtLeast(ServerVersion version) {
        if (this.major != version.major) {
            return this.major > version.major;
        }
        if (this.minor != version.minor) {
            return this.minor > version.minor;
        }
        return this.patch >= version.patch;
    }

    /**
     * Check if server version is at most the specified version
     */
    public boolean isAtMost(ServerVersion version) {
        if (this.major != version.major) {
            return this.major < version.major;
        }
        if (this.minor != version.minor) {
            return this.minor < version.minor;
        }
        return this.patch <= version.patch;
    }

    /**
     * Check if server version is between two versions (inclusive)
     */
    public boolean isBetween(ServerVersion min, ServerVersion max) {
        return isAtLeast(min) && isAtMost(max);
    }

    /**
     * Check if this is 1.13+ (the flattening)
     */
    public boolean isFlattening() {
        return isAtLeast(V1_13);
    }

    /**
     * Check if this is 1.14+ (PDC support)
     */
    public boolean hasPersistentData() {
        return isAtLeast(V1_14);
    }

    /**
     * Check if this is 1.16+ (hex color support)
     */
    public boolean hasHexColors() {
        return isAtLeast(V1_16);
    }

    /**
     * Check if this is 1.19.3+ (new sign API)
     */
    public boolean hasNewSignAPI() {
        return isAtLeast(new ServerVersion(1, 19, 3));
    }

    /**
     * Check if this is 1.20.5+ (component changes)
     */
    public boolean hasComponentChanges() {
        return isAtLeast(new ServerVersion(1, 20, 5));
    }

    public String getVersionString() {
        return major + "." + minor + "." + patch;
    }

    public String getNmsVersion() {
        return nmsVersion;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public String toString() {
        return getVersionString();
    }

    /**
     * Get simple version identifier (e.g., "1_20_R1")
     */
    public String getSimpleVersion() {
        return major + "_" + minor + "_R1";
    }
}
