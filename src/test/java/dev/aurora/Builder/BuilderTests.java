package dev.aurora.Builder;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.test.TestBase;
import dev.aurora.Utilities.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GUI builder classes
 */
public class BuilderTests extends TestBase {

    @Test
    public void testConfirmationGuiBuilder() {
        ConfirmationGui confirmationGui = new ConfirmationGui(guiManager);

        assertNotNull(confirmationGui);

        // Test fluent API
        confirmationGui
            .title("&cConfirm Delete?")
            .onConfirm(event -> {
                // Confirm action
            })
            .onCancel(event -> {
                // Cancel action
            });

        AuroraGui gui = confirmationGui.build();
        assertNotNull(gui);
        assertNotNull(gui.getInventory());
    }

    @Test
    public void testConfirmationGuiStatic() {
        boolean[] confirmed = {false};

        ConfirmationGui.confirm(guiManager, player, "&6Test?", () -> {
            confirmed[0] = true;
        });

        // Player should have GUI open
        assertNotNull(guiManager.getActiveGui(player));
    }

    @Test
    public void testSelectorGuiBuilder() {
        SelectorGui<String> selector = new SelectorGui<>(guiManager);

        selector
            .title("&6Select Option")
            .rows(3)
            .addOption(new ItemBuilder(Material.DIAMOND).name("&bOption 1").build(), "option1")
            .addOption(new ItemBuilder(Material.EMERALD).name("&aOption 2").build(), "option2")
            .onSelect((p, value) -> {
                // Selection handler
            });

        AuroraGui gui = selector.build();
        assertNotNull(gui);
    }

    @Test
    public void testSelectorGuiSimple() {
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3");

        SelectorGui<String> selector = SelectorGui.simple(guiManager, "&6Choose", options);
        assertNotNull(selector);

        AuroraGui gui = selector.build();
        assertNotNull(gui);
    }

    @Test
    public void testInputGuiBuilder() {
        InputGui inputGui = new InputGui(guiManager, plugin);

        inputGui
            .prompt("&7Enter your name:")
            .validator(InputGui.alphanumeric())
            .invalidMessage("&cInvalid name!")
            .timeout(30)
            .onInput((p, input) -> {
                // Input handler
            });

        assertNotNull(inputGui);
    }

    @Test
    public void testInputGuiValidators() {
        assertTrue(InputGui.notEmpty().test("test"));
        assertFalse(InputGui.notEmpty().test(""));
        assertFalse(InputGui.notEmpty().test(null));

        assertTrue(InputGui.numeric().test("123"));
        assertFalse(InputGui.numeric().test("abc"));

        assertTrue(InputGui.alphanumeric().test("abc123"));
        assertFalse(InputGui.alphanumeric().test("abc-123"));

        assertTrue(InputGui.minLength(3).test("test"));
        assertFalse(InputGui.minLength(5).test("test"));

        assertTrue(InputGui.maxLength(10).test("test"));
        assertFalse(InputGui.maxLength(3).test("test"));

        assertTrue(InputGui.lengthBetween(3, 10).test("test"));
        assertFalse(InputGui.lengthBetween(5, 10).test("test"));
    }

    @Test
    public void testSearchableGuiBuilder() {
        SearchableGui searchableGui = new SearchableGui(guiManager, plugin);

        searchableGui
            .title("&6Search Items")
            .rows(6)
            .addItem(new ItemBuilder(Material.DIAMOND).name("Diamond").build(), "diamond", "diamond_data")
            .addItem(new ItemBuilder(Material.EMERALD).name("Emerald").build(), "emerald", "emerald_data")
            .onItemClick(event -> {
                // Click handler
            });

        assertNotNull(searchableGui);
    }
}
