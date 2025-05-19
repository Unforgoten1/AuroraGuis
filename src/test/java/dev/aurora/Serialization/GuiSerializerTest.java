package dev.aurora.Serialization;

import dev.aurora.GUI.AuroraGui;
import dev.aurora.Manager.GuiManager;
import dev.aurora.test.TestBase;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GUI serialization and deserialization
 */
public class GuiSerializerTest extends TestBase {

    @Test
    public void testSerializeToJson() {
        AuroraGui gui = new AuroraGui("test-gui")
                .title("Test GUI")
                .rows(3);

        gui.setItem(0, new ItemStack(Material.DIAMOND));
        gui.setItem(13, new ItemStack(Material.GOLD_INGOT));

        String json = GuiSerializer.toJson(gui);

        assertNotNull(json);
        assertTrue(json.contains("test-gui"));
        assertTrue(json.contains("Test GUI"));
        assertTrue(json.contains("\"rows\": 3"));
    }

    @Test
    public void testDeserializeFromJson() {
        AuroraGui original = new AuroraGui("original-gui")
                .title("Original Title")
                .rows(4);

        original.setItem(5, new ItemStack(Material.EMERALD));

        String json = GuiSerializer.toJson(original);
        AuroraGui deserialized = GuiSerializer.fromJson(json, null);

        assertNotNull(deserialized);
        assertEquals("Original Title", deserialized.getTitle());
        assertEquals(4, deserialized.getRows());
        assertNotNull(deserialized.getInventory().getItem(5));
        assertEquals(Material.EMERALD, deserialized.getInventory().getItem(5).getType());
    }

    @Test
    public void testRoundTripSerialization() {
        AuroraGui original = new AuroraGui("round-trip")
                .title("Round Trip Test")
                .rows(6);

        original.setItem(0, new ItemStack(Material.STONE, 64));
        original.setItem(8, new ItemStack(Material.DIRT, 32));
        original.setItem(53, new ItemStack(Material.BEDROCK));

        String json = GuiSerializer.toJson(original);
        AuroraGui restored = GuiSerializer.fromJson(json, null);

        assertEquals(original.getTitle(), restored.getTitle());
        assertEquals(original.getRows(), restored.getRows());
        assertEquals(original.getSize(), restored.getSize());

        // Check items
        assertNotNull(restored.getInventory().getItem(0));
        assertEquals(Material.STONE, restored.getInventory().getItem(0).getType());
        assertEquals(64, restored.getInventory().getItem(0).getAmount());

        assertNotNull(restored.getInventory().getItem(8));
        assertEquals(Material.DIRT, restored.getInventory().getItem(8).getType());
        assertEquals(32, restored.getInventory().getItem(8).getAmount());

        assertNotNull(restored.getInventory().getItem(53));
        assertEquals(Material.BEDROCK, restored.getInventory().getItem(53).getType());
    }

    @Test
    public void testSaveToFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("test-gui.json").toFile();

        AuroraGui gui = new AuroraGui("file-test")
                .title("File Test")
                .rows(2);

        gui.setItem(0, new ItemStack(Material.APPLE));

        GuiSerializer.saveToFile(gui, file);

        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    public void testLoadFromFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("load-test.json").toFile();

        AuroraGui original = new AuroraGui("load-test")
                .title("Load Test")
                .rows(3);

        original.setItem(10, new ItemStack(Material.DIAMOND_SWORD));

        GuiSerializer.saveToFile(original, file);
        AuroraGui loaded = GuiSerializer.loadFromFile(file, null);

        assertNotNull(loaded);
        assertEquals("Load Test", loaded.getTitle());
        assertEquals(3, loaded.getRows());
        assertNotNull(loaded.getInventory().getItem(10));
        assertEquals(Material.DIAMOND_SWORD, loaded.getInventory().getItem(10).getType());
    }

    @Test
    public void testDeepCopy() {
        AuroraGui original = new AuroraGui("original")
                .title("Original")
                .rows(1);

        original.setItem(4, new ItemStack(Material.GOLD_BLOCK));

        AuroraGui copy = GuiSerializer.deepCopy(original, null);

        assertNotNull(copy);
        assertEquals(original.getTitle(), copy.getTitle());
        assertEquals(original.getRows(), copy.getRows());

        // Verify it's a deep copy (not same object)
        assertNotSame(original, copy);
        assertNotSame(original.getInventory(), copy.getInventory());

        // Verify contents are equal
        assertEquals(
            original.getInventory().getItem(4).getType(),
            copy.getInventory().getItem(4).getType()
        );
    }

    @Test
    public void testCanSerialize() {
        AuroraGui validGui = new AuroraGui("valid")
                .title("Valid")
                .rows(1);

        assertTrue(GuiSerializer.canSerialize(validGui));
        assertFalse(GuiSerializer.canSerialize(null));
    }

    @Test
    public void testNullGuiThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GuiSerializer.toJson(null);
        });
    }

    @Test
    public void testNullJsonThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GuiSerializer.fromJson(null, null);
        });
    }

    @Test
    public void testEmptyJsonThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GuiSerializer.fromJson("", null);
        });
    }

    @Test
    public void testFileNotFoundThrowsException() {
        assertThrows(Exception.class, () -> {
            GuiSerializer.loadFromFile(new File("nonexistent.json"), null);
        });
    }

    @Test
    public void testSerializableGuiDto() {
        SerializableGui dto = new SerializableGui("test", "Test", 3);

        assertEquals("test", dto.getName());
        assertEquals("Test", dto.getTitle());
        assertEquals(3, dto.getRows());
        assertTrue(dto.hasValidSchema());

        dto.setProperty("key", "value");
        assertEquals("value", dto.getProperty("key"));

        SerializableGui.SerializableItem item = new SerializableGui.SerializableItem(5, "base64data");
        item.setActionType("command");
        item.setActionData("/give {player} diamond");

        dto.addItem(item);
        assertEquals(1, dto.getItems().size());
        assertEquals(5, dto.getItems().get(0).getSlot());
        assertEquals("command", dto.getItems().get(0).getActionType());
    }
}
