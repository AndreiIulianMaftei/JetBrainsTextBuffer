public class Cell {
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
}
