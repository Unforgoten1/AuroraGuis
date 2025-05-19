package dev.aurora.Config;

/**
 * Configuration class for global GUI settings
 * Allows customization of default behaviors
 */
public class GuiConfig {
    private static GuiConfig instance;

    // Animation settings
    private int defaultAnimationSpeed = 10;
    private boolean optimizeAnimations = true;

    // Sound settings
    private boolean soundsEnabled = true;
    private float defaultSoundVolume = 1.0f;
    private float defaultSoundPitch = 1.0f;

    // GUI settings
    private int defaultRows = 3;
    private int maxHistorySize = 10;
    private boolean autoCloseOnError = false;

    // Pagination settings
    private int defaultItemsPerPage = 21;
    private boolean showPageNumbers = true;

    // Debug settings
    private boolean debugMode = false;
    private boolean verboseLogging = false;

    // Performance settings
    private int maxConcurrentAnimations = 100;
    private boolean useAsyncItemLoading = true;

    private GuiConfig() {}

    public static GuiConfig getInstance() {
        if (instance == null) {
            instance = new GuiConfig();
        }
        return instance;
    }

    // Animation getters/setters
    public int getDefaultAnimationSpeed() {
        return defaultAnimationSpeed;
    }

    public void setDefaultAnimationSpeed(int speed) {
        this.defaultAnimationSpeed = Math.max(1, speed);
    }

    public boolean isOptimizeAnimations() {
        return optimizeAnimations;
    }

    public void setOptimizeAnimations(boolean optimize) {
        this.optimizeAnimations = optimize;
    }

    // Sound getters/setters
    public boolean isSoundsEnabled() {
        return soundsEnabled;
    }

    public void setSoundsEnabled(boolean enabled) {
        this.soundsEnabled = enabled;
    }

    public float getDefaultSoundVolume() {
        return defaultSoundVolume;
    }

    public void setDefaultSoundVolume(float volume) {
        this.defaultSoundVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public float getDefaultSoundPitch() {
        return defaultSoundPitch;
    }

    public void setDefaultSoundPitch(float pitch) {
        this.defaultSoundPitch = Math.max(0.5f, Math.min(2.0f, pitch));
    }

    // GUI getters/setters
    public int getDefaultRows() {
        return defaultRows;
    }

    public void setDefaultRows(int rows) {
        this.defaultRows = Math.max(1, Math.min(6, rows));
    }

    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    public void setMaxHistorySize(int size) {
        this.maxHistorySize = Math.max(1, size);
    }

    public boolean isAutoCloseOnError() {
        return autoCloseOnError;
    }

    public void setAutoCloseOnError(boolean autoClose) {
        this.autoCloseOnError = autoClose;
    }

    // Pagination getters/setters
    public int getDefaultItemsPerPage() {
        return defaultItemsPerPage;
    }

    public void setDefaultItemsPerPage(int items) {
        this.defaultItemsPerPage = Math.max(1, items);
    }

    public boolean isShowPageNumbers() {
        return showPageNumbers;
    }

    public void setShowPageNumbers(boolean show) {
        this.showPageNumbers = show;
    }

    // Debug getters/setters
    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public void setVerboseLogging(boolean verbose) {
        this.verboseLogging = verbose;
    }

    // Performance getters/setters
    public int getMaxConcurrentAnimations() {
        return maxConcurrentAnimations;
    }

    public void setMaxConcurrentAnimations(int max) {
        this.maxConcurrentAnimations = Math.max(1, max);
    }

    public boolean isUseAsyncItemLoading() {
        return useAsyncItemLoading;
    }

    public void setUseAsyncItemLoading(boolean useAsync) {
        this.useAsyncItemLoading = useAsync;
    }

    /**
     * Reset all settings to defaults
     */
    public void resetToDefaults() {
        defaultAnimationSpeed = 10;
        optimizeAnimations = true;
        soundsEnabled = true;
        defaultSoundVolume = 1.0f;
        defaultSoundPitch = 1.0f;
        defaultRows = 3;
        maxHistorySize = 10;
        autoCloseOnError = false;
        defaultItemsPerPage = 21;
        showPageNumbers = true;
        debugMode = false;
        verboseLogging = false;
        maxConcurrentAnimations = 100;
        useAsyncItemLoading = true;
    }
}
