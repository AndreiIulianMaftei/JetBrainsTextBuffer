public final class Cell {

    public static final char EMPTY = '\0';

    private char ch;
    private TerminalColor fg;
    private TerminalColor bg;
    private int style;

    private boolean wide;


    private boolean continuation;

    public Cell() {
        reset();
    }

    public Cell(char ch, TerminalColor fg, TerminalColor bg, int style) {
        this.ch    = ch;
        this.fg    = fg;
        this.bg    = bg;
        this.style = style;
    }

    public void reset() {
        ch           = EMPTY;
        fg           = TerminalColor.DEFAULT;
        bg           = TerminalColor.DEFAULT;
        style        = TextStyle.NONE;
        wide         = false;
        continuation = false;
    }

    public Cell copy() {
        Cell c = new Cell(ch, fg, bg, style);
        c.wide         = this.wide;
        c.continuation = this.continuation;
        return c;
    }


    public char getChar()              { return ch; }
    public void setChar(char ch)       { this.ch = ch; }

    public TerminalColor getFg()               { return fg; }
    public void setFg(TerminalColor fg)        { this.fg = fg; }

    public TerminalColor getBg()               { return bg; }
    public void setBg(TerminalColor bg)        { this.bg = bg; }

    public int getStyle()              { return style; }
    public void setStyle(int style)    { this.style = style; }

    public boolean isWide()            { return wide; }

    public void setWide(boolean wide)  { this.wide = wide; }

    public boolean isContinuation()    { return continuation; }

    public void setContinuation(boolean continuation) { this.continuation = continuation; }

    public boolean isBold()      { return TextStyle.isSet(style, TextStyle.BOLD); }
    public boolean isItalic()    { return TextStyle.isSet(style, TextStyle.ITALIC); }
    public boolean isUnderline() { return TextStyle.isSet(style, TextStyle.UNDERLINE); }

    public boolean isEmpty()     { return ch == EMPTY || ch == ' '; }

    @Override
    public String toString() {
        if (continuation) return "";
        return isEmpty() ? " " : String.valueOf(ch);
    }
}

