public final class CellAttributes {
    private final TerminalColor fg;
    private final TerminalColor bg;
    private final int style;

    public CellAttributes(TerminalColor fg, TerminalColor bg, int style) {
        this.fg    = fg;
        this.bg    = bg;
        this.style = style;
    }

    public TerminalColor getFg()         { return fg; }
    public TerminalColor getBg()         { return bg; }
    public int           getStyle()      { return style; }
    public boolean       isBold()        { return TextStyle.isSet(style, TextStyle.BOLD); }
    public boolean       isItalic()      { return TextStyle.isSet(style, TextStyle.ITALIC); }
    public boolean       isUnderline()   { return TextStyle.isSet(style, TextStyle.UNDERLINE); }

    @Override
    public String toString() {
        return "CellAttributes{fg=" + fg + ", bg=" + bg
                + ", bold=" + isBold() + ", italic=" + isItalic()
                + ", underline=" + isUnderline() + "}";
    }
}