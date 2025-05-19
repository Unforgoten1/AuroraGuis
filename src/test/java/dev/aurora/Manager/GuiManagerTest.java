package dev.aurora.Manager;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.test.TestBase;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GuiManager functionality
 */
public class GuiManagerTest extends TestBase {

    @Test
    public void testManagerCreation() {
        assertNotNull(guiManager);
        assertNotNull(guiManager.getPlugin());
        assertNotNull(guiManager.getAnimationScheduler());
    }

    @Test
    public void testGuiRegistration() {
        AuroraGui gui = new AuroraGui("test-gui")
            .rows(3);

        gui.register(guiManager);

        assertEquals(gui, guiManager.getGui("test-gui"));
    }

    @Test
    public void testMultipleGuiRegistration() {
        AuroraGui gui1 = new AuroraGui("gui-1").rows(3).register(guiManager);
        AuroraGui gui2 = new AuroraGui("gui-2").rows(3).register(guiManager);
        AuroraGui gui3 = new AuroraGui("gui-3").rows(3).register(guiManager);

        assertEquals(gui1, guiManager.getGui("gui-1"));
        assertEquals(gui2, guiManager.getGui("gui-2"));
        assertEquals(gui3, guiManager.getGui("gui-3"));
    }

    @Test
    public void testOpenGui() {
        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .register(guiManager);

        guiManager.openGui(player, gui);

        assertEquals(gui, guiManager.getActiveGui(player));
    }

    @Test
    public void testOpenGuiByName() {
        new AuroraGui("test")
            .rows(3)
            .register(guiManager);

        guiManager.openGui(player, "test");

        assertNotNull(guiManager.getActiveGui(player));
        assertEquals("test", guiManager.getActiveGui(player).getName());
    }

    @Test
    public void testGetActiveGuiReturnsNull() {
        assertNull(guiManager.getActiveGui(player));
    }

    @Test
    public void testCloseGuiCleansUp() {
        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .register(guiManager);

        gui.open(player);
        assertEquals(gui, guiManager.getActiveGui(player));

        // Close inventory
        player.closeInventory();

        // Process the close event
        waitForScheduler(1);

        // Active GUI should be removed
        assertNull(guiManager.getActiveGui(player));
    }

    @Test
    public void testShutdownCleansUp() {
        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .register(guiManager);

        gui.open(player);

        guiManager.shutdown();

        assertNull(guiManager.getGui("test"));
        assertNull(guiManager.getActiveGui(player));
    }

    @Test
    public void testMultiplePlayersOpenDifferentGuis() {
        var player2 = createPlayer("Player2");

        AuroraGui gui1 = new AuroraGui("gui-1").rows(3).register(guiManager);
        AuroraGui gui2 = new AuroraGui("gui-2").rows(3).register(guiManager);

        gui1.open(player);
        gui2.open(player2);

        assertEquals(gui1, guiManager.getActiveGui(player));
        assertEquals(gui2, guiManager.getActiveGui(player2));
    }
}
