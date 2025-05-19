package dev.aurora.Packet.API;

/**
 * Configuration for packet-based GUI validation
 * Allows fine-tuning of anti-dupe settings and performance characteristics
 */
public class PacketGuiConfig {

    private ValidationLevel validationLevel;
    private int minClickDelayMs;
    private int maxClicksPerSecond;
    private boolean autoRollbackOnViolation;
    private boolean logViolations;
    private boolean kickOnViolation;
    private int violationKickThreshold;

    // Anti withhold-close-packet protection
    private boolean detectStaleSession;
    private long sessionTimeoutMs;
    private long inactivityCheckIntervalMs;
    private boolean forceCloseOnTimeout;

    /**
     * Creates a default configuration with PACKET validation level
     */
    public PacketGuiConfig() {
        this.validationLevel = ValidationLevel.PACKET;
        this.minClickDelayMs = 50;
        this.maxClicksPerSecond = 20;
        this.autoRollbackOnViolation = true;
        this.logViolations = true;
        this.kickOnViolation = false;
        this.violationKickThreshold = 5;

        // Anti withhold-close-packet defaults
        this.detectStaleSession = true;
        this.sessionTimeoutMs = 300000; // 5 minutes
        this.inactivityCheckIntervalMs = 30000; // Check every 30 seconds
        this.forceCloseOnTimeout = true;
    }

    // Preset configurations

    /**
     * Lenient configuration - Minimal validation, better performance
     * @return Lenient config
     */
    public static PacketGuiConfig lenient() {
        PacketGuiConfig config = new PacketGuiConfig();
        config.validationLevel = ValidationLevel.BASIC;
        config.minClickDelayMs = 30;
        config.maxClicksPerSecond = 30;
        config.autoRollbackOnViolation = true;
        config.logViolations = false;
        config.sessionTimeoutMs = 600000; // 10 minutes (more lenient)
        return config;
    }

    /**
     * Normal configuration - Balanced validation and performance (default)
     * @return Normal config
     */
    public static PacketGuiConfig normal() {
        return new PacketGuiConfig(); // Already set to normal defaults
    }

    /**
     * Strict configuration - Maximum validation, lower performance
     * @return Strict config
     */
    public static PacketGuiConfig strict() {
        PacketGuiConfig config = new PacketGuiConfig();
        config.validationLevel = ValidationLevel.ADVANCED;
        config.minClickDelayMs = 100;
        config.maxClicksPerSecond = 10;
        config.autoRollbackOnViolation = true;
        config.logViolations = true;
        config.kickOnViolation = true;
        config.violationKickThreshold = 3;
        config.sessionTimeoutMs = 120000; // 2 minutes (strict)
        config.inactivityCheckIntervalMs = 15000; // Check every 15 seconds
        return config;
    }

    // Fluent setters

    public PacketGuiConfig validationLevel(ValidationLevel level) {
        this.validationLevel = level;
        return this;
    }

    public PacketGuiConfig minClickDelayMs(int milliseconds) {
        this.minClickDelayMs = milliseconds;
        return this;
    }

    public PacketGuiConfig maxClicksPerSecond(int clicks) {
        this.maxClicksPerSecond = clicks;
        return this;
    }

    public PacketGuiConfig autoRollbackOnViolation(boolean enabled) {
        this.autoRollbackOnViolation = enabled;
        return this;
    }

    public PacketGuiConfig logViolations(boolean enabled) {
        this.logViolations = enabled;
        return this;
    }

    public PacketGuiConfig kickOnViolation(boolean enabled) {
        this.kickOnViolation = enabled;
        return this;
    }

    public PacketGuiConfig violationKickThreshold(int threshold) {
        this.violationKickThreshold = threshold;
        return this;
    }

    public PacketGuiConfig detectStaleSession(boolean enabled) {
        this.detectStaleSession = enabled;
        return this;
    }

    public PacketGuiConfig sessionTimeoutMs(long milliseconds) {
        this.sessionTimeoutMs = milliseconds;
        return this;
    }

    public PacketGuiConfig inactivityCheckIntervalMs(long milliseconds) {
        this.inactivityCheckIntervalMs = milliseconds;
        return this;
    }

    public PacketGuiConfig forceCloseOnTimeout(boolean enabled) {
        this.forceCloseOnTimeout = enabled;
        return this;
    }

    // Getters

    public ValidationLevel getValidationLevel() {
        return validationLevel;
    }

    public int getMinClickDelayMs() {
        return minClickDelayMs;
    }

    public int getMaxClicksPerSecond() {
        return maxClicksPerSecond;
    }

    public boolean isAutoRollbackOnViolation() {
        return autoRollbackOnViolation;
    }

    public boolean isLogViolations() {
        return logViolations;
    }

    public boolean isKickOnViolation() {
        return kickOnViolation;
    }

    public int getViolationKickThreshold() {
        return violationKickThreshold;
    }

    public void setValidationLevel(ValidationLevel validationLevel) {
        this.validationLevel = validationLevel;
    }

    public void setMinClickDelayMs(int minClickDelayMs) {
        this.minClickDelayMs = minClickDelayMs;
    }

    public void setMaxClicksPerSecond(int maxClicksPerSecond) {
        this.maxClicksPerSecond = maxClicksPerSecond;
    }

    public void setAutoRollbackOnViolation(boolean autoRollbackOnViolation) {
        this.autoRollbackOnViolation = autoRollbackOnViolation;
    }

    public void setLogViolations(boolean logViolations) {
        this.logViolations = logViolations;
    }

    public void setKickOnViolation(boolean kickOnViolation) {
        this.kickOnViolation = kickOnViolation;
    }

    public void setViolationKickThreshold(int violationKickThreshold) {
        this.violationKickThreshold = violationKickThreshold;
    }

    public boolean isDetectStaleSession() {
        return detectStaleSession;
    }

    public void setDetectStaleSession(boolean detectStaleSession) {
        this.detectStaleSession = detectStaleSession;
    }

    public long getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(long sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public long getInactivityCheckIntervalMs() {
        return inactivityCheckIntervalMs;
    }

    public void setInactivityCheckIntervalMs(long inactivityCheckIntervalMs) {
        this.inactivityCheckIntervalMs = inactivityCheckIntervalMs;
    }

    public boolean isForceCloseOnTimeout() {
        return forceCloseOnTimeout;
    }

    public void setForceCloseOnTimeout(boolean forceCloseOnTimeout) {
        this.forceCloseOnTimeout = forceCloseOnTimeout;
    }

    @Override
    public String toString() {
        return "PacketGuiConfig{" +
                "validationLevel=" + validationLevel +
                ", minClickDelayMs=" + minClickDelayMs +
                ", maxClicksPerSecond=" + maxClicksPerSecond +
                ", autoRollback=" + autoRollbackOnViolation +
                ", logViolations=" + logViolations +
                ", kickOnViolation=" + kickOnViolation +
                ", kickThreshold=" + violationKickThreshold +
                '}';
    }
}
