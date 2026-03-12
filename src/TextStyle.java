public final class TextStyle {
    public static final int NONE      = 0;
    public static final int BOLD      = 1;
    public static final int ITALIC    = 1 << 1;
    public static final int UNDERLINE = 1 << 2;

    public static int set(int flags, int flag)         { return flags | flag; }
    public static int clear(int flags, int flag)       { return flags & ~flag; }
    public static boolean isSet(int flags, int flag)   { return (flags & flag) != 0; }

    private TextStyle() {}
}