package dev.aurora.GUI;

import dev.aurora.Struct.BorderType;
import dev.aurora.Utilities.Items.ItemBuilder;
import dev.aurora.test.TestBase;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AuroraGui core functionality
 */
public class AuroraGuiTest extends TestBase {

    @Test
    public void testGuiCreation() {
        AuroraGui gui = new AuroraGui("test-gui");

        assertNotNull(gui);
        assertEquals("test-gui", gui.getName());
    }

    @Test
    public void testTitleSetting() {
        AuroraGui gui = new AuroraGui("test")
            .title("&6Test Title")
            .rows(3);

        assertEquals("test", gui.getName());
    }

    @Test
    public void testRowsValidation() {
        AuroraGui gui = new AuroraGui("test");

        // Valid rows
        assertDoesNotThrow(() -> gui.rows(3));
        assertDoesNotThrow(() -> gui.rows(1));
        assertDoesNotThrow(() -> gui.rows(6));

        // Invalid rows
        assertThrows(IllegalArgumentException.class, () -> gui.rows(0));
        assertThrows(IllegalArgumentException.class, () -> gui.rows(7));
    }

    @Test
    public void testAddItem() {
        ItemStack item = new ItemBuilder(Material.DIAMOND)
            .name("&bTest Item")
            .build();

        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .addItem(13, item, null)
            .register(guiManager);

        assertNotNull(gui.getInventory());
        assertEquals(item, gui.getInventory().getItem(13));
    }

    @Test
    public void testBorderSetting() {
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build();

        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .setBorder(BorderType.FULL, borderItem)
            .register(guiManager);

        // Check top-left corner
        assertEquals(borderItem, gui.getInventory().getItem(0));

        // Check top-right corner
        assertEquals(borderItem, gui.getInventory().getItem(8));

        // Check bottom-left corner
        assertEquals(borderItem, gui.getInventory().getItem(18));

        // Check bottom-right corner
        assertEquals(borderItem, gui.getInventory().getItem(26));
    }

    @Test
    public void testRegistration() {
        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .register(guiManager);

        assertEquals(gui, guiManager.getGui("test"));
    }

    @Test
    public void testOpenGui() {
        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .register(guiManager);

        gui.open(player);

        assertEquals(gui, guiManager.getActiveGui(player));
        assertNotNull(player.getOpenInventory());
    }

    @Test
    public void testPagination() {
        AuroraGui gui = new AuroraGui("test")
            .rows(6)
            .setItemsPerPage(21)
            .register(guiManager);

        assertEquals(0, gui.getCurrentPage());
        assertFalse(gui.hasNextPage());
        assertFalse(gui.hasPrevPage());
    }

    @Test
    public void testPaginationWithItems() {
        AuroraGui gui = new AuroraGui("test")
            .rows(6)
            .setItemsPerPage(10)
            .register(guiManager);

        // Add 25 items
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        for (int i = 0; i < 25; i++) {
            items.add(new ItemBuilder(Material.PAPER)
                .name("&eItem " + i)
                .build());
        }

        gui.addPaginatedItems(items, null);

        assertEquals(0, gui.getCurrentPage());
        assertEquals(3, gui.getTotalPages()); // 25 items / 10 per page = 3 pages
        assertTrue(gui.hasNextPage());
        assertFalse(gui.hasPrevPage());

        // Navigate to next page
        gui.nextPage();
        assertEquals(1, gui.getCurrentPage());
        assertTrue(gui.hasNextPage());
        assertTrue(gui.hasPrevPage());
    }

    @Test
    public void testClearGui() {
        ItemStack item = new ItemBuilder(Material.DIAMOND).build();
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).build();

        AuroraGui gui = new AuroraGui("test")
            .rows(3)
            .setBorder(BorderType.FULL, borderItem)
            .addItem(13, item, null)
            .register(guiManager);

        // Clear without preserving border
        gui.clearGui(false);
        assertNull(gui.getInventory().getItem(0)); // Border should be gone
        assertNull(gui.getInventory().getItem(13)); // Item should be gone

        // Add again with border and clear preserving border
        gui.setBorder(BorderType.FULL, borderItem)
            .addItem(13, item, null);

        gui.clearGui(true);
        assertNotNull(gui.getInventory().getItem(0)); // Border preserved
        assertNull(gui.getInventory().getItem(13)); // Item cleared
    }

    @Test
    public void testNullItemThrows() {
        AuroraGui gui = new AuroraGui("test").rows(3);

        assertThrows(IllegalArgumentException.class, () ->
            gui.addItem(null, null)
        );

        assertThrows(IllegalArgumentException.class, () ->
            gui.addItem(13, null, null)
        );
    }
}
