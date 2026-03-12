import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {


    @Test
    void textStyleNoneIsZero() {
        assertEquals(0, TextStyle.NONE);
    }

    @Test
    void textStyleSetAndIsSet() {
        int style = TextStyle.set(TextStyle.NONE, TextStyle.BOLD);
        assertTrue(TextStyle.isSet(style, TextStyle.BOLD));
    }

    @Test
    void textStyleClear() {
        int style = TextStyle.set(TextStyle.NONE, TextStyle.BOLD);
        style = TextStyle.clear(style, TextStyle.BOLD);
        assertFalse(TextStyle.isSet(style, TextStyle.BOLD));
    }

    @Test
    void textStyleMultipleFlags() {
        int style = TextStyle.set(TextStyle.set(TextStyle.NONE, TextStyle.ITALIC), TextStyle.UNDERLINE);
        assertTrue(TextStyle.isSet(style, TextStyle.ITALIC));
        assertTrue(TextStyle.isSet(style, TextStyle.UNDERLINE));
    }

    @Test
    void terminalColorDefaultExists() {
        assertNotNull(TerminalColor.DEFAULT);
    }

    @Test
    void terminalColorValueOf() {
        assertSame(TerminalColor.RED, TerminalColor.valueOf("RED"));
    }

    @Test
    void cellDefaultConstructor() {
        assertDoesNotThrow(() -> new Cell());
    }

    @Test
    void cellParameterisedConstructor() {
        assertDoesNotThrow(() -> new Cell('A', TerminalColor.GREEN, TerminalColor.BLACK, TextStyle.BOLD));
    }

    @Test
    void cellResetDoesNotThrow() {
        Cell cell = new Cell('A', TerminalColor.GREEN, TerminalColor.BLACK, TextStyle.BOLD);
        assertDoesNotThrow(cell::reset);
    }
}

