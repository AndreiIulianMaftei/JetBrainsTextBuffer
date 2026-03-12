
public class Main {
    public static void main(String[] args) {

            Cell cell = new Cell('A', TerminalColor.RED, TerminalColor.BLACK, TextStyle.BOLD);
            System.out.println("Cell character: " + cell.getChar());
            System.out.println("Cell foreground color: " + cell.getFg());
            System.out.println("Cell background color: " + cell.getBg());
            System.out.println("Cell is bold: " + cell.isBold());

            CellAttributes attrs = new CellAttributes(TerminalColor.GREEN, TerminalColor.DEFAULT, TextStyle.ITALIC);
            System.out.println("CellAttributes: " + attrs);
    }
}