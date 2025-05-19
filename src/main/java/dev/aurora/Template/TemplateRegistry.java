package dev.aurora.Template;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for GUI templates
 * Allows storing and retrieving reusable GUI templates
 */
public class TemplateRegistry {
    private static final Map<String, GuiTemplate> templates = new ConcurrentHashMap<>();

    /**
     * Register a template
     * @param template The template to register
     */
    public static void register(GuiTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        templates.put(template.getName(), template);
    }

    /**
     * Get a template by name
     * @param name Template name
     * @return The template, or null if not found
     */
    public static GuiTemplate get(String name) {
        return templates.get(name);
    }

    /**
     * Unregister a template
     * @param name Template name to remove
     */
    public static void unregister(String name) {
        templates.remove(name);
    }

    /**
     * Check if a template exists
     * @param name Template name
     * @return true if template exists
     */
    public static boolean exists(String name) {
        return templates.containsKey(name);
    }

    /**
     * Clear all templates
     */
    public static void clear() {
        templates.clear();
    }

    /**
     * Get number of registered templates
     * @return Template count
     */
    public static int count() {
        return templates.size();
    }
}
