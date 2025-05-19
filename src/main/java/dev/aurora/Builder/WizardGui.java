package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * Multi-page wizard GUI for step-by-step forms
 * Guides users through a process with validation and data collection
 */
public class WizardGui {
    private final String name;
    private final GuiManager manager;
    private final List<WizardStep> steps;
    private final Map<UUID, WizardSession> activeSessions;
    private BiConsumer<Player, Map<String, Object>> onComplete;
    private BiConsumer<Player, String> onCancel;
    private boolean allowBackNavigation;
    private boolean showProgress;

    /**
     * Represents a single step in the wizard
     */
    public static class WizardStep {
        private final String id;
        private final AuroraGui gui;
        private BiPredicate<Player, Map<String, Object>> validator;
        private BiConsumer<Player, Map<String, Object>> onEnter;
        private BiConsumer<Player, Map<String, Object>> onExit;
        private String nextButtonText;
        private String backButtonText;

        public WizardStep(String id, AuroraGui gui) {
            this.id = id;
            this.gui = gui;
            this.nextButtonText = "&a&lNext →";
            this.backButtonText = "&7&l← Back";
        }

        public String getId() { return id; }
        public AuroraGui getGui() { return gui; }
        public BiPredicate<Player, Map<String, Object>> getValidator() { return validator; }
        public BiConsumer<Player, Map<String, Object>> getOnEnter() { return onEnter; }
        public BiConsumer<Player, Map<String, Object>> getOnExit() { return onExit; }
        public String getNextButtonText() { return nextButtonText; }
        public String getBackButtonText() { return backButtonText; }

        public WizardStep validator(BiPredicate<Player, Map<String, Object>> validator) {
            this.validator = validator;
            return this;
        }

        public WizardStep onEnter(BiConsumer<Player, Map<String, Object>> onEnter) {
            this.onEnter = onEnter;
            return this;
        }

        public WizardStep onExit(BiConsumer<Player, Map<String, Object>> onExit) {
            this.onExit = onExit;
            return this;
        }

        public WizardStep nextButtonText(String text) {
            this.nextButtonText = text;
            return this;
        }

        public WizardStep backButtonText(String text) {
            this.backButtonText = text;
            return this;
        }
    }

    /**
     * Represents an active wizard session for a player
     */
    private static class WizardSession {
        private final Map<String, Object> data;
        private int currentStepIndex;
        private final List<Integer> visitedSteps;

        public WizardSession() {
            this.data = new HashMap<>();
            this.currentStepIndex = 0;
            this.visitedSteps = new ArrayList<>();
        }

        public Map<String, Object> getData() { return data; }
        public int getCurrentStepIndex() { return currentStepIndex; }
        public void setCurrentStepIndex(int index) { this.currentStepIndex = index; }
        public List<Integer> getVisitedSteps() { return visitedSteps; }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public Object getData(String key) {
            return data.get(key);
        }
    }

    /**
     * Creates a new wizard GUI
     *
     * @param name The wizard name
     * @param manager The GUI manager
     */
    public WizardGui(String name, GuiManager manager) {
        this.name = name;
        this.manager = manager;
        this.steps = new ArrayList<>();
        this.activeSessions = new HashMap<>();
        this.allowBackNavigation = true;
        this.showProgress = true;
    }

    /**
     * Adds a step to the wizard
     *
     * @param id The step identifier
     * @param gui The GUI for this step
     * @return The created wizard step
     */
    public WizardStep addStep(String id, AuroraGui gui) {
        WizardStep step = new WizardStep(id, gui);
        steps.add(step);
        return step;
    }

    /**
     * Adds a step to the wizard
     *
     * @param step The wizard step
     * @return This wizard for chaining
     */
    public WizardGui addStep(WizardStep step) {
        steps.add(step);
        return this;
    }

    /**
     * Sets the completion callback
     *
     * @param onComplete Callback with player and collected data
     * @return This wizard for chaining
     */
    public WizardGui onComplete(BiConsumer<Player, Map<String, Object>> onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    /**
     * Sets the cancellation callback
     *
     * @param onCancel Callback with player and step ID where cancelled
     * @return This wizard for chaining
     */
    public WizardGui onCancel(BiConsumer<Player, String> onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    /**
     * Sets whether back navigation is allowed
     *
     * @param allow true to allow back navigation
     * @return This wizard for chaining
     */
    public WizardGui allowBackNavigation(boolean allow) {
        this.allowBackNavigation = allow;
        return this;
    }

    /**
     * Sets whether to show progress indicator
     *
     * @param show true to show progress
     * @return This wizard for chaining
     */
    public WizardGui showProgress(boolean show) {
        this.showProgress = show;
        return this;
    }

    /**
     * Starts the wizard for a player
     *
     * @param player The player
     */
    public void start(Player player) {
        if (steps.isEmpty()) {
            player.sendMessage("§cWizard has no steps configured.");
            return;
        }

        WizardSession session = new WizardSession();
        activeSessions.put(player.getUniqueId(), session);

        showStep(player, 0);
    }

    /**
     * Shows a specific step to the player
     *
     * @param player The player
     * @param stepIndex The step index
     */
    private void showStep(Player player, int stepIndex) {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            return;
        }

        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        WizardStep step = steps.get(stepIndex);
        session.setCurrentStepIndex(stepIndex);
        session.getVisitedSteps().add(stepIndex);

        // Call onEnter callback
        if (step.getOnEnter() != null) {
            step.getOnEnter().accept(player, session.getData());
        }

        // Update GUI title with progress if enabled
        AuroraGui gui = step.getGui();
        if (showProgress) {
            String progressTitle = String.format("§7[%d/%d] §r%s",
                    stepIndex + 1, steps.size(), gui.getTitle());
            gui.title(progressTitle);
        }

        // Add navigation buttons
        addNavigationButtons(gui, player, stepIndex);

        // Open the GUI
        gui.open(player);
    }

    /**
     * Adds navigation buttons to the GUI
     */
    private void addNavigationButtons(AuroraGui gui, Player player, int stepIndex) {
        WizardStep step = steps.get(stepIndex);
        int size = gui.getSize();

        // Back button (if not first step and allowed)
        if (allowBackNavigation && stepIndex > 0) {
            gui.setItem(size - 9, new dev.aurora.Utilities.Items.ItemBuilder(
                    org.bukkit.Material.ARROW)
                    .name(step.getBackButtonText())
                    .build(),
                    event -> goBack(player));
        }

        // Next/Complete button
        boolean isLastStep = stepIndex == steps.size() - 1;
        String buttonText = isLastStep ? "§a§lComplete ✓" : step.getNextButtonText();

        gui.setItem(size - 1, new dev.aurora.Utilities.Items.ItemBuilder(
                isLastStep ? org.bukkit.Material.EMERALD : org.bukkit.Material.LIME_STAINED_GLASS_PANE)
                .name(buttonText)
                .build(),
                event -> {
                    if (isLastStep) {
                        complete(player);
                    } else {
                        goNext(player);
                    }
                });

        // Cancel button
        gui.setItem(size - 5, new dev.aurora.Utilities.Items.ItemBuilder(
                org.bukkit.Material.BARRIER)
                .name("§c§lCancel")
                .build(),
                event -> cancel(player));
    }

    /**
     * Goes to the next step
     */
    private void goNext(Player player) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        int currentIndex = session.getCurrentStepIndex();
        WizardStep currentStep = steps.get(currentIndex);

        // Validate current step
        if (currentStep.getValidator() != null) {
            if (!currentStep.getValidator().test(player, session.getData())) {
                player.sendMessage("§cPlease complete this step before continuing.");
                return;
            }
        }

        // Call onExit callback
        if (currentStep.getOnExit() != null) {
            currentStep.getOnExit().accept(player, session.getData());
        }

        // Show next step
        showStep(player, currentIndex + 1);
    }

    /**
     * Goes back to the previous step
     */
    private void goBack(Player player) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        int currentIndex = session.getCurrentStepIndex();
        if (currentIndex > 0) {
            showStep(player, currentIndex - 1);
        }
    }

    /**
     * Completes the wizard
     */
    private void complete(Player player) {
        WizardSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        // Validate final step
        WizardStep finalStep = steps.get(session.getCurrentStepIndex());
        if (finalStep.getValidator() != null) {
            if (!finalStep.getValidator().test(player, session.getData())) {
                player.sendMessage("§cPlease complete this step before finishing.");
                activeSessions.put(player.getUniqueId(), session); // Restore session
                return;
            }
        }

        player.closeInventory();

        // Call completion callback
        if (onComplete != null) {
            onComplete.accept(player, session.getData());
        }
    }

    /**
     * Cancels the wizard
     */
    private void cancel(Player player) {
        WizardSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        player.closeInventory();

        // Call cancel callback
        if (onCancel != null) {
            String stepId = steps.get(session.getCurrentStepIndex()).getId();
            onCancel.accept(player, stepId);
        }
    }

    /**
     * Gets a player's wizard session
     *
     * @param player The player
     * @return The session, or null if not active
     */
    public WizardSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    /**
     * Sets data in a player's session
     *
     * @param player The player
     * @param key The data key
     * @param value The data value
     */
    public void setSessionData(Player player, String key, Object value) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.setData(key, value);
        }
    }

    /**
     * Gets data from a player's session
     *
     * @param player The player
     * @param key The data key
     * @return The data value, or null if not found
     */
    public Object getSessionData(Player player, String key) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        return session != null ? session.getData(key) : null;
    }

    /**
     * Gets the number of steps
     *
     * @return Step count
     */
    public int getStepCount() {
        return steps.size();
    }

    /**
     * Checks if a player has an active wizard session
     *
     * @param player The player
     * @return true if active
     */
    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * Clears all active sessions
     */
    public void clearAllSessions() {
        activeSessions.clear();
    }
}
