package mineverse.Aust1n46.chat.utilities;

import mineverse.Aust1n46.chat.MineverseChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static mineverse.Aust1n46.chat.utilities.Format.BUKKIT_COLOR_CODE_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link Format}.
 */
public class FormatTest {
    private static MockedStatic<MineverseChat> mockedMineverseChat;

    private static MineverseChat mockPlugin;

    private List<String> filters;

    @BeforeClass
    public static void init() {
        mockedMineverseChat = Mockito.mockStatic(MineverseChat.class);
        mockPlugin = Mockito.mock(MineverseChat.class);
        Mockito.when(MineverseChat.getInstance()).thenReturn(mockPlugin);
    }

    @AfterClass
    public static void close() {
        mockedMineverseChat.close();
    }

    @Before
    public void setUp() {
        filters = new ArrayList<String>();
        filters.add("ass,donut");

        final FileConfiguration mockConfig = Mockito.mock(FileConfiguration.class);
        Mockito.when(mockPlugin.getConfig()).thenReturn(mockConfig);
        Mockito.when(mockConfig.getStringList("filters")).thenReturn(filters);
    }

    @After
    public void tearDown() {
        filters = new ArrayList<String>();
    }

    @Test
    public void testGetLastCodeSingleColor() {
        final String input = BUKKIT_COLOR_CODE_PREFIX + "cHello";
        final String expectedResult = BUKKIT_COLOR_CODE_PREFIX + "c";

        final String result = Format.getLastCode(input);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetLastCodeColorAfterFormat() {
        final String input = BUKKIT_COLOR_CODE_PREFIX + "o" + BUKKIT_COLOR_CODE_PREFIX + "cHello";
        final String expectedResult = BUKKIT_COLOR_CODE_PREFIX + "c";

        final String result = Format.getLastCode(input);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetLastCodeColorBeforeFormat() {
        final String input = BUKKIT_COLOR_CODE_PREFIX + "c" + BUKKIT_COLOR_CODE_PREFIX + "oHello";
        final String expectedResult = BUKKIT_COLOR_CODE_PREFIX + "c" + BUKKIT_COLOR_CODE_PREFIX + "o";

        final String result = Format.getLastCode(input);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testFilterChat() {
        final String test = "I am an ass";
        final String expectedResult = "I am an donut";

        final String result = Format.FilterChat(test);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIsValidColor() {
        final String color = "red";

        final boolean result = Format.isValidColor(color);
        assertTrue(result);
    }

    @Test
    public void testIsInvalidColor() {
        final String color = "randomString";

        final boolean result = Format.isValidColor(color);
        assertFalse(result);
    }

    @Test
    public void testIsValidHexColor() {
        final String hexColor = "#ff00ff";

        final boolean result = Format.isValidHexColor(hexColor);
        assertTrue(result);
    }

    @Test
    public void testIsInvalidHexColor() {
        final String hexColor = "#random";

        final boolean result = Format.isValidHexColor(hexColor);
        assertFalse(result);
    }

    @Test
    public void testConvertHexColorCodeToBukkitColorCode() {
        final String hexColor = "#ff00ff";
        final String expectedResult = BUKKIT_COLOR_CODE_PREFIX + "x" + BUKKIT_COLOR_CODE_PREFIX + "f"
                + BUKKIT_COLOR_CODE_PREFIX + "f" + BUKKIT_COLOR_CODE_PREFIX + "0" + BUKKIT_COLOR_CODE_PREFIX + "0"
                + BUKKIT_COLOR_CODE_PREFIX + "f" + BUKKIT_COLOR_CODE_PREFIX + "f";

        final String result = Format.convertHexColorCodeToBukkitColorCode(hexColor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testConvertHexColorCodeStringToBukkitColorCodeString() {
        final String input = "#ff00ffHello" + BUKKIT_COLOR_CODE_PREFIX + "cThere#00ff00Austin";
        final String expectedResult = BUKKIT_COLOR_CODE_PREFIX + "x" + BUKKIT_COLOR_CODE_PREFIX + "f"
                + BUKKIT_COLOR_CODE_PREFIX + "f" + BUKKIT_COLOR_CODE_PREFIX + "0" + BUKKIT_COLOR_CODE_PREFIX + "0"
                + BUKKIT_COLOR_CODE_PREFIX + "f" + BUKKIT_COLOR_CODE_PREFIX + "fHello" + BUKKIT_COLOR_CODE_PREFIX
                + "cThere" + BUKKIT_COLOR_CODE_PREFIX + "x" + BUKKIT_COLOR_CODE_PREFIX + "0" + BUKKIT_COLOR_CODE_PREFIX
                + "0" + BUKKIT_COLOR_CODE_PREFIX + "f" + BUKKIT_COLOR_CODE_PREFIX + "f" + BUKKIT_COLOR_CODE_PREFIX + "0"
                + BUKKIT_COLOR_CODE_PREFIX + "0Austin";

        final String result = Format.convertHexColorCodeStringToBukkitColorCodeString(input);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testFormatStringLegacyColor_NoColorCode() {
        final String input = "Hello There Austin";
        final String expectedResult = "Hello There Austin";

        final String result = Format.FormatStringLegacyColor(input);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testFormatStringLegacyColor_LegacyCodeOnly() {
        final String input = "Hello &cThere Austin";
        final String expectedResult = "Hello " + BUKKIT_COLOR_CODE_PREFIX + "cThere Austin";

        final String result = Format.FormatStringLegacyColor(input);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testFormatStringLegacyColor_SpigotHexCodeOnly() {
        final String input = "&x&f&f&f&f&f&fHello There Austin";
        final String expectedResult = "&x&f&f&f&f&f&fHello There Austin";

        final String result = Format.FormatStringLegacyColor(input);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testFormatStringLegacyColor_BothColorCodes() {
        final String input = "&x&f&f&f&f&f&f&cHello There Austin";
        final String expectedResult = "&x&f&f&f&f&f&f" + BUKKIT_COLOR_CODE_PREFIX + "cHello There Austin";

        final String result = Format.FormatStringLegacyColor(input);
        assertEquals(expectedResult, result);
    }
}
