import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {
    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(10, 5, 20);
    }

    @Test
    void dimensions_areStoredCorrectly() {
        TerminalBuffer b = new TerminalBuffer(80, 24, 100);
        assertEquals(80,  b.getWidth());
        assertEquals(24,  b.getHeight());
        assertEquals(100, b.getMaxScrollback());
    }

    @Test
    void constructor_rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0,  5, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(5,  0, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(5,  5, -1));
    }

    @Test
    void attributes_areAppliedToWrittenCell() {
        buf.setActiveFg(TerminalColor.RED);
        buf.setActiveBg(TerminalColor.BLUE);
        buf.setActiveStyle(TextStyle.BOLD | TextStyle.UNDERLINE);
        buf.writeChar('X');

        Cell c = buf.getCell(0, 0);
        assertEquals(TerminalColor.RED,  c.getFg());
        assertEquals(TerminalColor.BLUE, c.getBg());
        assertTrue(c.isBold());
        assertTrue(c.isUnderline());
        assertFalse(c.isItalic());
    }

    @Test
    void attributes_resetToDefaultAfterReset() {
        buf.setActiveFg(TerminalColor.RED);
        buf.setActiveStyle(TextStyle.BOLD);
        buf.resetActiveStyle();
        buf.writeChar('Y');

        Cell c = buf.getCell(0, 0);
        assertEquals(TerminalColor.DEFAULT, c.getFg());
        assertFalse(c.isBold());
    }

    @Test
    void cursor_setAndGet() {
        buf.setCursor(3, 7);
        assertEquals(3, buf.getCursorRow());
        assertEquals(7, buf.getCursorCol());
    }

    @Test
    void cursor_clampsToScreenBounds() {
        buf.setCursor(999, 999);
        assertEquals(4, buf.getCursorRow());
        assertEquals(9, buf.getCursorCol());

        buf.setCursor(-5, -5);
        assertEquals(0, buf.getCursorRow());
        assertEquals(0, buf.getCursorCol());
    }

    @Test
    void cursor_moveUpDown() {
        buf.setCursor(3, 0);
        buf.moveCursorUp(2);
        assertEquals(1, buf.getCursorRow());

        buf.moveCursorDown(3);
        assertEquals(4, buf.getCursorRow());
    }

    @Test
    void cursor_moveLeftRight() {
        buf.setCursor(0, 5);
        buf.moveCursorLeft(3);
        assertEquals(2, buf.getCursorCol());

        buf.moveCursorRight(4);
        assertEquals(6, buf.getCursorCol());
    }

    @Test
    void cursor_movementClampsAtBoundaries() {
        buf.setCursor(0, 0);
        buf.moveCursorUp(10);
        assertEquals(0, buf.getCursorRow());

        buf.moveCursorLeft(10);
        assertEquals(0, buf.getCursorCol());

        buf.setCursor(4, 9);
        buf.moveCursorDown(10);
        assertEquals(4, buf.getCursorRow());

        buf.moveCursorRight(10);
        assertEquals(9, buf.getCursorCol());
    }

    @Test
    void writeText_placesCharsAndAdvancesCursor() {
        buf.writeText("Hello");
        assertEquals('H', buf.getCharAt(0, 0));
        assertEquals('e', buf.getCharAt(0, 1));
        assertEquals('o', buf.getCharAt(0, 4));
        assertEquals(5, buf.getCursorCol());
        assertEquals(0, buf.getCursorRow());
    }

    @Test
    void writeText_newlineMovesCursorToNextRow() {
        buf.writeText("Hi\nThere");
        assertEquals(1, buf.getCursorRow());
        assertEquals('T', buf.getCharAt(1, 0));
    }

    @Test
    void writeText_overwritesExistingContent() {
        buf.writeText("Hello");
        buf.setCursor(0, 0);
        buf.writeText("Hi");

        assertEquals("Hillo", buf.getScreenLine(0));
    }

    @Test
    void writeText_wrapsAtRightEdge() {
        buf.writeText("ABCDEFGHIJ");

        assertEquals(1, buf.getCursorRow());
        assertEquals(0, buf.getCursorCol());

        assertEquals('A', buf.getCharAt(0, 0));
        assertEquals('J', buf.getCharAt(0, 9));

        buf.writeText("K");
        assertEquals('K', buf.getCharAt(1, 0));
        assertEquals(1, buf.getCursorCol());
    }

    @Test
    void writeText_scrollsWhenBottomIsExceeded() {
        buf.writeText("Row0\n");
        buf.writeText("Row1\n");
        buf.writeText("Row2\n");
        buf.writeText("Row3\n");
        buf.writeText("Row4\n");
        assertEquals(1, buf.scrollbackSize());
        assertEquals("Row0", buf.getScrollbackLine(0));
        assertEquals("Row1", buf.getScreenLine(0));
    }

    @Test
    void insertText_shiftsContentRight() {
        buf.writeText("ABCDE");
        buf.setCursor(0, 2);
        buf.insertText("XY");
        assertEquals("ABXYCDE", buf.getScreenLine(0));
    }

    @Test
    void insertText_advancesCursorPastInsertedChars() {
        buf.writeText("ABCDE");
        buf.setCursor(0, 2);
        buf.insertText("XY");
        assertEquals(4, buf.getCursorCol());
        assertEquals(0, buf.getCursorRow());
    }

    @Test
    void fillLine_fillsCursorRowWithChar() {
        buf.setCursor(2, 0);
        buf.fillLine('-');
        assertEquals("----------", buf.getScreenLine(2));
    }

    @Test
    void fillLine_appliesActiveAttributes() {
        buf.setCursor(1, 0);
        buf.setActiveFg(TerminalColor.GREEN);
        buf.fillLine('=');
        assertEquals(TerminalColor.GREEN, buf.getCell(1, 0).getFg());
    }

    @Test
    void fillLine_doesNotMoveCursor() {
        buf.setCursor(2, 3);
        buf.fillLine('-');
        assertEquals(2, buf.getCursorRow());
        assertEquals(3, buf.getCursorCol());
    }

    @Test
    void fillLine_withEmptyClears() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.setCursor(0, 0);
        buf.fillLine(Cell.EMPTY);
        assertEquals("", buf.getScreenLine(0));
    }

    @Test
    void insertLineAtBottom_pushesTopLineToScrollback() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4");
        buf.insertLineAtBottom();
        assertEquals(1, buf.scrollbackSize());
        assertEquals("Row0", buf.getScrollbackLine(0));
    }

    @Test
    void insertLineAtBottom_shiftsRowsUp() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4");
        buf.insertLineAtBottom();
        assertEquals("Row1", buf.getScreenLine(0));
        assertEquals("Row2", buf.getScreenLine(1));
    }

    @Test
    void insertLineAtBottom_newBottomRowIsBlank() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4");
        buf.insertLineAtBottom();
        assertEquals("", buf.getScreenLine(4));
    }

    @Test
    void clearScreen_wipesScreenAndResetsCursor() {
        buf.writeText("Hello\nWorld");
        buf.clearScreen();
        assertEquals("", buf.getScreenLine(0));
        assertEquals("", buf.getScreenLine(1));
        assertEquals(0, buf.getCursorRow());
        assertEquals(0, buf.getCursorCol());
    }

    @Test
    void clearScreen_preservesScrollback() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4\n");
        int sbSize = buf.scrollbackSize();
        buf.clearScreen();
        assertEquals(sbSize, buf.scrollbackSize());
    }

    @Test
    void clearAll_wipesScreenAndScrollback() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4\n");
        buf.clearAll();
        assertEquals(0, buf.scrollbackSize());
        assertEquals("", buf.getScreenLine(0));
        assertEquals(0, buf.getCursorRow());
        assertEquals(0, buf.getCursorCol());
    }

    @Test
    void getCharAt_returnsCorrectChar() {
        buf.writeText("ABC");
        assertEquals('A', buf.getCharAt(0, 0));
        assertEquals('B', buf.getCharAt(0, 1));
        assertEquals('C', buf.getCharAt(0, 2));
    }

    @Test
    void getCharAt_returnsSpaceForEmptyCell() {
        assertEquals(' ', buf.getCharAt(0, 0));
    }

    @Test
    void getScrollbackCharAt_returnsCorrectChar() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4\n");
        assertEquals('R', buf.getScrollbackCharAt(0, 0));
        assertEquals('o', buf.getScrollbackCharAt(0, 1));
    }

    @Test
    void getAttributesAt_returnsCorrectAttributes() {
        buf.setActiveFg(TerminalColor.YELLOW);
        buf.setActiveStyle(TextStyle.ITALIC);
        buf.writeText("Hi");

        CellAttributes attr = buf.getAttributesAt(0, 0);
        assertEquals(TerminalColor.YELLOW, attr.getFg());
        assertTrue(attr.isItalic());
    }

    @Test
    void getScrollbackAttributesAt_returnsCorrectAttributes() {
        buf.setActiveFg(TerminalColor.CYAN);
        buf.setActiveStyle(TextStyle.BOLD);
        buf.writeText("Hi\n\n\n\n\n");
        buf.resetActiveStyle();

        CellAttributes attr = buf.getScrollbackAttributesAt(0, 0);
        assertEquals(TerminalColor.CYAN, attr.getFg());
        assertTrue(attr.isBold());
    }

    @Test
    void getScreenLine_returnsCorrectTrimmedLine() {
        buf.writeText("Hello");
        assertEquals("Hello", buf.getScreenLine(0));
        assertEquals("",      buf.getScreenLine(1));
    }

    @Test
    void getScrollbackLine_returnsCorrectLine() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4\n");
        assertEquals("Row0", buf.getScrollbackLine(0));
    }

    @Test
    void getScreenContent_returnsAllRowsJoined() {
        buf.writeText("AAA\nBBB\nCCC");
        String content = buf.getScreenContent();
        assertTrue(content.startsWith("AAA\nBBB\nCCC"));
    }

    @Test
    void getFullContent_returnsScrollbackThenScreen() {
        TerminalBuffer b = new TerminalBuffer(10, 2, 20);
        b.writeText("Line1\nLine2\nLine3");
        String full = b.getFullContent();
        int line1pos = full.indexOf("Line1");
        int line2pos = full.indexOf("Line2");
        int line3pos = full.indexOf("Line3");
        assertTrue(line1pos < line2pos);
        assertTrue(line2pos < line3pos);
    }

    @Test
    void scrollback_isCappedAtMaxSize() {
        TerminalBuffer b = new TerminalBuffer(10, 2, 3);
        for (int i = 1; i <= 7; i++) b.writeText("L" + i + "\n");
        assertEquals(3, b.scrollbackSize());
    }

    @Test
    void scrollback_keepsNewestLinesWhenCapped() {
        TerminalBuffer b = new TerminalBuffer(10, 2, 3);
        for (int i = 1; i <= 7; i++) b.writeText("L" + i + "\n");

        assertEquals("L4", b.getScrollbackLine(0));
        assertEquals("L6", b.getScrollbackLine(2));
    }

    @Test
    void scrollback_disabledWhenMaxIsZero() {
        TerminalBuffer b = new TerminalBuffer(10, 2, 0);
        b.writeText("Line1\nLine2\nLine3\n");
        assertEquals(0, b.scrollbackSize());
    }

    @Test
    void wideChar_occupiesTwoCells() {
        buf.writeText("日");
        Cell primary = buf.getCell(0, 0);
        Cell cont    = buf.getCell(0, 1);
        assertTrue(primary.isWide(),         "primary cell must be wide");
        assertTrue(cont.isContinuation(),    "next cell must be a continuation");
        assertEquals('日', primary.getChar());
        assertEquals(Cell.EMPTY, cont.getChar());
    }

    @Test
    void wideChar_advancesCursorByTwo() {
        buf.writeText("日");
        assertEquals(2, buf.getCursorCol());
        assertEquals(0, buf.getCursorRow());
    }

    @Test
    void wideChar_wrapsToNextLineWhenAtLastColumn() {
        buf.setCursor(0, 9);
        buf.writeText("字");
        assertEquals(1, buf.getCursorRow());
        assertEquals(2, buf.getCursorCol());
        assertTrue(buf.getCell(1, 0).isWide());
        assertTrue(buf.getCell(1, 1).isContinuation());
    }

    @Test
    void wideChar_mixedWithNarrow() {
        buf.writeText("A日B");
        assertEquals('A',   buf.getCell(0, 0).getChar());
        assertTrue(buf.getCell(0, 1).isWide());
        assertTrue(buf.getCell(0, 2).isContinuation());
        assertEquals('B',   buf.getCell(0, 3).getChar());
        assertEquals(4, buf.getCursorCol());
    }

    @Test
    void wideChar_overwritingPrimaryErasesContinuation() {
        buf.writeText("日");
        buf.setCursor(0, 0);
        buf.writeText("X");
        assertFalse(buf.getCell(0, 0).isWide());
        assertFalse(buf.getCell(0, 1).isContinuation());
    }

    @Test
    void wideChar_overwritingContinuationErasesPrimary() {
        buf.writeText("日");
        buf.setCursor(0, 1);
        buf.writeText("X");
        assertFalse(buf.getCell(0, 0).isWide());
        assertFalse(buf.getCell(0, 1).isContinuation());
        assertEquals('X', buf.getCell(0, 1).getChar());
    }

    @Test
    void wideChar_getScreenLineSkipsContinuation() {
        buf.writeText("日本");
        String line = buf.getScreenLine(0);
        assertEquals("日本", line);
    }

    @Test
    void wideChar_insertTextHandlesWideChar() {
        buf.writeText("ABCDE");
        buf.setCursor(0, 2);
        buf.insertText("語");

        Cell primary = buf.getCell(0, 2);
        Cell cont    = buf.getCell(0, 3);
        assertTrue(primary.isWide());
        assertTrue(cont.isContinuation());
        assertEquals('A', buf.getCell(0, 0).getChar());
        assertEquals('B', buf.getCell(0, 1).getChar());

        assertEquals(4, buf.getCursorCol());
    }

    @Test
    void wideChar_attributesInheritedByBothCells() {
        buf.setActiveFg(TerminalColor.CYAN);
        buf.setActiveBg(TerminalColor.RED);
        buf.writeText("字");
        Cell primary = buf.getCell(0, 0);
        Cell cont    = buf.getCell(0, 1);
        assertEquals(TerminalColor.CYAN, primary.getFg());
        assertEquals(TerminalColor.RED,  primary.getBg());
        assertEquals(TerminalColor.CYAN, cont.getFg());
        assertEquals(TerminalColor.RED,  cont.getBg());
    }

    @Test
    void resize_updatesWidthAndHeight() {
        buf.resize(20, 10);
        assertEquals(20, buf.getWidth());
        assertEquals(10, buf.getHeight());
    }

    @Test
    void resize_rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> buf.resize(0, 5));
        assertThrows(IllegalArgumentException.class, () -> buf.resize(5, 0));
        assertThrows(IllegalArgumentException.class, () -> buf.resize(-1, 5));
    }

    @Test
    void resize_widerPreservesContent() {
        buf.writeText("Hello");
        buf.resize(20, 5);
        assertEquals("Hello", buf.getScreenLine(0));
        assertEquals(20, buf.getWidth());
    }

    @Test
    void resize_narrowerTruncatesContent() {
        buf.writeText("Hello World");
        buf.resize(5, 5);
        assertEquals("Hello", buf.getScreenLine(0));
    }

    @Test
    void resize_tallerAddsBlankRows() {
        buf.writeText("Hi");
        buf.resize(10, 8);
        assertEquals(8, buf.getHeight());
        assertEquals("Hi", buf.getScreenLine(0));
        assertEquals("",   buf.getScreenLine(7));
    }

    @Test
    void resize_shorterDropsBottomRows() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4");
        buf.resize(10, 3);
        assertEquals(3, buf.getHeight());
        assertEquals("Row0", buf.getScreenLine(0));
        assertEquals("Row1", buf.getScreenLine(1));
        assertEquals("Row2", buf.getScreenLine(2));
    }

    @Test
    void resize_clampsCursor() {
        buf.setCursor(4, 9);
        buf.resize(4, 3);
        assertEquals(2, buf.getCursorRow());
        assertEquals(3, buf.getCursorCol());
    }

    @Test
    void resize_preservesScrollback() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4\n");
        assertEquals(1, buf.scrollbackSize());
        buf.resize(10, 5);
        assertEquals(1, buf.scrollbackSize());
        assertEquals("Row0", buf.getScrollbackLine(0));
    }

    @Test
    void resize_scrollbackLinesAreResizedToo() {
        buf.writeText("Row0\nRow1\nRow2\nRow3\nRow4\n");
        buf.resize(3, 5);
        assertEquals("Row", buf.getScrollbackLine(0));
    }

    @Test
    void resize_wideCellSplitByNarrowIsErased() {
        buf.writeText("日");
        buf.resize(1, 5);
        assertFalse(buf.getCell(0, 0).isWide());
    }
}

