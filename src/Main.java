
public class Main {
    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;


        if (TextStyle.NONE == 0) { System.out.println("PASS: TextStyle.NONE == 0"); passed++; }
        else                     { System.out.println("FAIL: TextStyle.NONE == 0"); failed++; }

        int style = TextStyle.set(TextStyle.NONE, TextStyle.BOLD);
        if (TextStyle.isSet(style, TextStyle.BOLD)) { System.out.println("PASS: BOLD is set after set()"); passed++; }
        else                                         { System.out.println("FAIL: BOLD is set after set()"); failed++; }

        style = TextStyle.clear(style, TextStyle.BOLD);
        if (!TextStyle.isSet(style, TextStyle.BOLD)) { System.out.println("PASS: BOLD is cleared after clear()"); passed++; }
        else                                          { System.out.println("FAIL: BOLD is cleared after clear()"); failed++; }

        style = TextStyle.set(TextStyle.set(TextStyle.NONE, TextStyle.ITALIC), TextStyle.UNDERLINE);
        if (TextStyle.isSet(style, TextStyle.ITALIC) && TextStyle.isSet(style, TextStyle.UNDERLINE)) {
            System.out.println("PASS: ITALIC and UNDERLINE set together"); passed++;
        } else {
            System.out.println("FAIL: ITALIC and UNDERLINE set together"); failed++;
        }

        if (TerminalColor.DEFAULT != null) { System.out.println("PASS: TerminalColor.DEFAULT exists"); passed++; }
        else                               { System.out.println("FAIL: TerminalColor.DEFAULT exists"); failed++; }

        if (TerminalColor.valueOf("RED") == TerminalColor.RED) { System.out.println("PASS: TerminalColor.RED valueOf"); passed++; }
        else                                                    { System.out.println("FAIL: TerminalColor.RED valueOf"); failed++; }


        Cell cell = new Cell();
        if (cell != null) { System.out.println("PASS: Cell default constructor"); passed++; }
        else              { System.out.println("FAIL: Cell default constructor"); failed++; }

        Cell cell2 = new Cell('A', TerminalColor.GREEN, TerminalColor.BLACK, TextStyle.BOLD);
        if (cell2 != null) { System.out.println("PASS: Cell parameterised constructor"); passed++; }
        else               { System.out.println("FAIL: Cell parameterised constructor"); failed++; }

        try {
            cell2.reset();
            System.out.println("PASS: Cell.reset() runs without error"); passed++;
        } catch (Exception e) {
            System.out.println("FAIL: Cell.reset() threw " + e); failed++;
        }

        System.out.println("\nResults: " + passed + " passed, " + failed + " failed.");
    }
}