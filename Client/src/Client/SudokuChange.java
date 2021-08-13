package Client;

// This class is used to store the player moves, every object represents 1 change made in the sudoku
public class SudokuChange {
    private int i; // First coordinate of a square
    private int j; // Second coordinate of a square
    private int n; // The new number in the square

    SudokuChange(int a, int b, int c)
    {
        i = a;
        j = b;
        n = c;
    }

    // returns true if the 2 objects share the same coordinates
    public boolean sameSquare(Object obj) {
        SudokuChange other = (SudokuChange) obj;

        return other.getI() == i &&
                other.getJ() == j;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getN() {
        return n;
    }
}
