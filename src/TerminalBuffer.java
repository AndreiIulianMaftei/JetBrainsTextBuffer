import java.util.ArrayDeque;
import java.util.Deque;
public class TerminalBuffer {


    private int width;
    private int height;
    private final int maxScrollback;

    private Cell[][] screen;

    private final Deque<Cell[]> scrollback;

    private int cursorRow;
    private int cursorCol;

    private TerminalColor activeFg    = TerminalColor.DEFAULT;
    private TerminalColor activeBg    = TerminalColor.DEFAULT;
    private int           activeStyle = TextStyle.NONE;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width  <= 0) throw new IllegalArgumentException("width must be positive");
        if (height <= 0) throw new IllegalArgumentException("height must be positive");
        if (maxScrollback < 0) throw new IllegalArgumentException("maxScrollback must be >= 0");

        this.width         = width;
        this.height        = height;
        this.maxScrollback = maxScrollback;
        this.screen        = new Cell[height][width];
        this.scrollback    = new ArrayDeque<>();

        initScreen();
    }


    private void initScreen() {
        for (int r = 0; r < height; r++)
            screen[r] = blankLine();
    }

    private Cell[] blankLine() {
        return blankLine(width);
    }

    private static Cell[] blankLine(int w) {
        Cell[] line = new Cell[w];
        for (int c = 0; c < w; c++) line[c] = new Cell();
        return line;
    }

    private void scrollUp() {
        if (maxScrollback > 0) {
            scrollback.addLast(deepCopyLine(screen[0]));
            if (scrollback.size() > maxScrollback) scrollback.pollFirst();
        }
        for (int r = 0; r < height - 1; r++) screen[r] = screen[r + 1];
        screen[height - 1] = blankLine();
    }

    private Cell[] deepCopyLine(Cell[] line) {
        Cell[] copy = new Cell[width];
        for (int c = 0; c < width; c++) copy[c] = line[c].copy();
        return copy;
    }

    private void applyAttributes(Cell cell, char ch) {
        cell.setChar(ch);
        cell.setFg(activeFg);
        cell.setBg(activeBg);
        cell.setStyle(activeStyle);
        cell.setWide(false);
        cell.setContinuation(false);
    }

    private void applyWideAttributes(Cell primary, Cell cont, char ch) {
        primary.setChar(ch);
        primary.setFg(activeFg);
        primary.setBg(activeBg);
        primary.setStyle(activeStyle);
        primary.setWide(true);
        primary.setContinuation(false);

        if (cont != null) {
            cont.setChar(Cell.EMPTY);
            cont.setFg(activeFg);
            cont.setBg(activeBg);
            cont.setStyle(activeStyle);
            cont.setWide(false);
            cont.setContinuation(true);
        }
    }

    private void advanceCursor(int cols) {
        cursorCol += cols;
        if (cursorCol >= width) {
            cursorCol = 0;
            cursorRow++;
            if (cursorRow >= height) {
                scrollUp();
                cursorRow = height - 1;
            }
        }
    }

    private void advanceCursor() {
        advanceCursor(1);
    }

    private void eraseCellIfContinuation(int row, int col) {
        if (col > 0 && screen[row][col].isContinuation()) {
            screen[row][col - 1].reset();
        }
    }

    private Cell[] scrollbackLineAt(int index) {
        int i = 0;
        for (Cell[] line : scrollback) {
            if (i++ == index) return line;
        }
        throw new IndexOutOfBoundsException(index);
    }

    private static String trim(StringBuilder sb) {
        int end = sb.length();
        while (end > 0 && sb.charAt(end - 1) == ' ') end--;
        return sb.substring(0, end);
    }

    private static boolean isWideChar(char ch) {
        if (ch >= 0x4E00 && ch <= 0x9FFF) return true;
        if (ch >= 0x3400 && ch <= 0x4DBF) return true;
        if (ch >= 0xF900 && ch <= 0xFAFF) return true;
        if (ch >= 0x2E80 && ch <= 0x2EFF) return true;
        if (ch >= 0x2F00 && ch <= 0x2FDF) return true;
        if (ch >= 0x3000 && ch <= 0x303F) return true;
        if (ch >= 0x3040 && ch <= 0x309F) return true;
        if (ch >= 0x30A0 && ch <= 0x30FF) return true;
        if (ch >= 0x3100 && ch <= 0x312F) return true;
        if (ch >= 0x3130 && ch <= 0x318F) return true;
        if (ch >= 0xAC00 && ch <= 0xD7AF) return true;
        if (ch >= 0xFF01 && ch <= 0xFF60) return true;
        if (ch >= 0xFFE0 && ch <= 0xFFE6) return true;

        return false;
    }

    public void setActiveFg(TerminalColor fg)  { this.activeFg    = fg; }

    public void setActiveBg(TerminalColor bg)  { this.activeBg    = bg; }

    public void setActiveStyle(int style)      { this.activeStyle = style; }

    public TerminalColor getActiveFg()         { return activeFg; }
    public TerminalColor getActiveBg()         { return activeBg; }
    public int           getActiveStyle()      { return activeStyle; }

    public void resetActiveStyle() {
        activeFg    = TerminalColor.DEFAULT;
        activeBg    = TerminalColor.DEFAULT;
        activeStyle = TextStyle.NONE;
    }
  public void setCursor(int row, int col) {
        cursorRow = Math.max(0, Math.min(row, height - 1));
        cursorCol = Math.max(0, Math.min(col, width  - 1));
    }

    public int getCursorRow() { return cursorRow; }
    public int getCursorCol() { return cursorCol; }

    public void moveCursorUp(int n) {
        cursorRow = Math.max(0, cursorRow - n);
    }

    public void moveCursorDown(int n) {
        cursorRow = Math.min(height - 1, cursorRow + n);
    }

    public void moveCursorLeft(int n) {
        cursorCol = Math.max(0, cursorCol - n);
    }

    public void moveCursorRight(int n) {
        cursorCol = Math.min(width - 1, cursorCol + n);
    }

    public void writeText(String text) {
        if (text == null) return;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                cursorCol = 0;
                cursorRow++;
                if (cursorRow >= height) { scrollUp(); cursorRow = height - 1; }
            } else if (ch == '\r') {
                cursorCol = 0;
            } else if (isWideChar(ch)) {
                if (cursorCol + 1 >= width) {

                    cursorCol = 0;
                    cursorRow++;
                    if (cursorRow >= height) { scrollUp(); cursorRow = height - 1; }
                }
                eraseCellIfContinuation(cursorRow, cursorCol);
                Cell primary = screen[cursorRow][cursorCol];
                Cell cont    = screen[cursorRow][cursorCol + 1];
                if (cont.isWide() && cursorCol + 2 < width) screen[cursorRow][cursorCol + 2].reset();
                applyWideAttributes(primary, cont, ch);
                advanceCursor(2);
            } else {
                if (screen[cursorRow][cursorCol].isWide() && cursorCol + 1 < width)
                    screen[cursorRow][cursorCol + 1].reset();
                eraseCellIfContinuation(cursorRow, cursorCol);
                applyAttributes(screen[cursorRow][cursorCol], ch);
                advanceCursor();
            }
        }
    }

    public void writeChar(char ch) { writeText(String.valueOf(ch)); }

    public void writeString(String text) { writeText(text); }

    public void insertText(String text) {
        if (text == null || text.isEmpty()) return;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                insertLineBreakAtCursor();
            } else if (ch == '\r') {
                cursorCol = 0;
            } else if (isWideChar(ch)) {
                if (cursorCol + 1 >= width) {
                    cursorCol = 0;
                    cursorRow++;
                    if (cursorRow >= height) { scrollUp(); cursorRow = height - 1; }
                }
                Cell[] row = screen[cursorRow];
                for (int j = width - 1; j > cursorCol + 1; j--) {
                    row[j] = row[j - 2];
                }
                row[cursorCol]     = new Cell();
                row[cursorCol + 1] = new Cell();
                applyWideAttributes(row[cursorCol], row[cursorCol + 1], ch);
                advanceCursor(2);
            } else {
                Cell[] row = screen[cursorRow];
                for (int j = width - 1; j > cursorCol; j--) {
                    row[j] = row[j - 1];
                }
                row[cursorCol] = new Cell();
                applyAttributes(row[cursorCol], ch);
                advanceCursor();
            }
        }
    }

    private void insertLineBreakAtCursor() {
        Cell[] currentRow = screen[cursorRow];
        Cell[] overflow = new Cell[width];
        int overflowLen = width - cursorCol;
        for (int c = 0; c < overflowLen; c++) {
            overflow[c] = currentRow[cursorCol + c].copy();
            currentRow[cursorCol + c].reset();
        }
        for (int c = overflowLen; c < width; c++) overflow[c] = new Cell();

        cursorCol = 0;
        cursorRow++;
        if (cursorRow >= height) {
            scrollUp();
            cursorRow = height - 1;
        }
        Cell[] nextRow = screen[cursorRow];
        System.arraycopy(nextRow, 0, nextRow, overflowLen, width - overflowLen);
        System.arraycopy(overflow, 0, nextRow, 0, overflowLen);
    }
    public void fillLine(char ch) {
        fillLine(cursorRow, ch);
    }

    public void fillLine(int row, char ch) {
        if (row < 0 || row >= height) throw new IndexOutOfBoundsException("row " + row);
        if (ch == Cell.EMPTY) {
            for (int c = 0; c < width; c++) screen[row][c].reset();
        } else {
            for (int c = 0; c < width; c++) applyAttributes(screen[row][c], ch);
        }
    }
    public void insertLineAtBottom() {
        scrollUp();

    }

    public void clearScreen() {
        initScreen();
        cursorRow = 0;
        cursorCol = 0;
    }

    public void eraseScreen() { clearScreen(); }

    public void clearAll() {
        scrollback.clear();
        clearScreen();
    }

    public void eraseToEndOfLine() {
        for (int c = cursorCol; c < width; c++) screen[cursorRow][c].reset();
    }

    public void eraseCell(int row, int col) {
        getScreenCell(row, col).reset();
    }

    public Cell getCell(int row, int col) { return getScreenCell(row, col); }

    private Cell getScreenCell(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width)
            throw new IndexOutOfBoundsException("Screen (" + row + "," + col + ")");
        return screen[row][col];
    }

    public char getCharAt(int row, int col) {
        char ch = getScreenCell(row, col).getChar();
        return ch == Cell.EMPTY ? ' ' : ch;
    }

    public char getScrollbackCharAt(int sbRow, int col) {
        if (sbRow < 0 || sbRow >= scrollback.size() || col < 0 || col >= width)
            throw new IndexOutOfBoundsException("Scrollback (" + sbRow + "," + col + ")");
        char ch = scrollbackLineAt(sbRow)[col].getChar();
        return ch == Cell.EMPTY ? ' ' : ch;
    }

    public CellAttributes getAttributesAt(int row, int col) {
        Cell c = getScreenCell(row, col);
        return new CellAttributes(c.getFg(), c.getBg(), c.getStyle());
    }

    public CellAttributes getScrollbackAttributesAt(int sbRow, int col) {
        if (sbRow < 0 || sbRow >= scrollback.size() || col < 0 || col >= width)
            throw new IndexOutOfBoundsException("Scrollback (" + sbRow + "," + col + ")");
        Cell c = scrollbackLineAt(sbRow)[col];
        return new CellAttributes(c.getFg(), c.getBg(), c.getStyle());
    }

    public String getScreenLine(int row) {
        if (row < 0 || row >= height) throw new IndexOutOfBoundsException("row " + row);
        StringBuilder sb = new StringBuilder(width);
        for (int c = 0; c < width; c++) sb.append(screen[row][c]);
        return trim(sb);
    }
public String getScrollbackLine(int index) {
        if (index < 0 || index >= scrollback.size())
            throw new IndexOutOfBoundsException("Scrollback index " + index);
        Cell[] line = scrollbackLineAt(index);
        StringBuilder sb = new StringBuilder(width);
        for (Cell cell : line) sb.append(cell);
        return trim(sb);
    }

    public String getScreenContent() {
        StringBuilder sb = new StringBuilder(height * (width + 1));
        for (int r = 0; r < height; r++) {
            sb.append(getScreenLine(r));
            if (r < height - 1) sb.append('\n');
        }
        return sb.toString();
    }

    public String getFullContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scrollback.size(); i++) {
            sb.append(getScrollbackLine(i)).append('\n');
        }
        sb.append(getScreenContent());
        return sb.toString();
    }


    public int scrollbackSize() { return scrollback.size(); }

    public Cell[] getScrollbackLineRaw(int index) {
        if (index < 0 || index >= scrollback.size())
            throw new IndexOutOfBoundsException("Scrollback index " + index);
        return scrollbackLineAt(index);
    }


    public int getWidth()         { return width; }
    public int getHeight()        { return height; }
    public int getMaxScrollback() { return maxScrollback; }

    public void resize(int newWidth, int newHeight) {
        if (newWidth  <= 0) throw new IllegalArgumentException("newWidth must be positive");
        if (newHeight <= 0) throw new IllegalArgumentException("newHeight must be positive");

        Cell[][] newScreen = new Cell[newHeight][newWidth];
        int copyRows = Math.min(height, newHeight);
        for (int r = 0; r < copyRows; r++) {
            newScreen[r] = resizeLine(screen[r], newWidth);
        }
        for (int r = copyRows; r < newHeight; r++) {
            newScreen[r] = blankLine(newWidth);
        }

        Deque<Cell[]> resizedScrollback = new ArrayDeque<>(scrollback.size());
        for (Cell[] line : scrollback) {
            resizedScrollback.addLast(resizeLine(line, newWidth));
        }
        scrollback.clear();
        scrollback.addAll(resizedScrollback);

        this.screen = newScreen;
        this.width  = newWidth;
        this.height = newHeight;

        cursorRow = Math.min(cursorRow, height - 1);
        cursorCol = Math.min(cursorCol, width  - 1);
    }

    private static Cell[] resizeLine(Cell[] line, int newWidth) {
        Cell[] result = new Cell[newWidth];
        int copy = Math.min(line.length, newWidth);
        for (int c = 0; c < copy; c++) result[c] = line[c].copy();
        for (int c = copy; c < newWidth; c++) result[c] = new Cell();

        if (copy > 0 && result[copy - 1].isWide()) {
            result[copy - 1].reset();
        }
        return result;
    }
    public String renderScreen()                  { return getScreenContent(); }
    public String renderScrollbackLine(int index) { return getScrollbackLine(index); }
}
