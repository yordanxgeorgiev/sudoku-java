package RMI;

import java.util.Random;
import java.util.Stack;

public class SudokuSolver {

    private int[][] sudoku;           // The sudoku puzzle
    private int[] tryOrder;           // The order in which we try the numbers 1-9 in the backtracking algorithm, used to generate random sudoku
    private Stack<Integer[]> trace;   // Keeps track of the positions we can backtrack to

    SudokuSolver()
    {
        trace = new Stack<>();        // Creating empty stack
        sudoku = new int[9][9];       // Empty sudoku puzzle
        tryOrder = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        shuffle(tryOrder);            // Shuffling the numbers

        generateRandom(); // generating random sudoku
    }

    public int[][] getSudoku() {
        return sudoku;
    }

    private int countEmpty()
    {
        int emptyCount = 0;

        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                if(sudoku[i][j] == 0) emptyCount++;
            }
        }

        return emptyCount;
    }

    public void print()
    {
        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                System.out.print(sudoku[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }


    // Generating unique sudoku
    private void generateRandom()
    {
        // First we will set random numbers to the first row of the sudoku
        // (This way using the backtracking algorithm we'll get random sudoku, otherwise it would follow a pattern)
        int[] randomFirstRow = {1,2,3,4,5,6,7,8,9};
        shuffle(randomFirstRow);
        System.arraycopy(randomFirstRow, 0, sudoku[0], 0, 9);

        // The next function finds the first empty cell and pushes its position in the stack
        // The backtracking algorithm will start with this position
        findNextEmpty();
     }

    public void solve()
    {
        // While there are empty squares apply the backtrack algorithm
        while(countEmpty() > 0) backtrack();
    }

    // General idea:
    // 1) find first empty cell
    // 2) try to assign a number in the order given in the array tryOrder
    // 3) - if successful go to step 1)
    //    - otherwise empty the cell, backtrack to the last change and go to step 2)
   private void backtrack()
   {
           Integer[] backtrack = trace.peek();   // Getting the position of the element we should go to (as a pair (x,y))
           int x = backtrack[0];
           int y = backtrack[1];

           int index = 0;            // This index is used to determine which is the next number we should try in the cell

            // If the given cell is not empty that means that we have already put a number in it
            // To find the next number we should try, we seek the number in tryOrder and get its index + 1
           if (sudoku[x][y] != 0) {
               for(index = 0; index < 9; index++)
               {
                   if(tryOrder[index] == sudoku[x][y]) break;
               }
               index++;
           }

            // If the index gets to 10 in the last step, that means we have tried all possible numbers
            // Else we try all the elements in the remaining indexes to see if any fit in the cell
           for(int j = index; j < 9; j++)
           {
               if(canInsert(x,y,tryOrder[j])) // if we can insert the number in the cell (x,y)
               {
                   sudoku[x][y] = tryOrder[j];
                   findNextEmpty();     // Finding the next empty cell and pushing it in the stack
                   return;
               }
           }

           // If we couldn't put a number in the cell, we empty it and pop it from the stack
           sudoku[x][y] = 0;
           trace.pop();
   }


   // Finding the first empty cell and pushing its position in the stack
    private void findNextEmpty()
    {
        for(int k = 0; k < 9; k++)
        {
            for(int j = 0; j < 9; j++)
            {
                if(sudoku[k][j] == 0)
                {
                    trace.push(new Integer[]{k,j});
                    return;
                }
            }
        }
    }

    // Shuffling an array
    private void shuffle(int[] arr)
    {
        Random rnd = new Random();
        for(int i = 0; i < arr.length; i++)
        {
            int randomInt = rnd.nextInt(arr.length);
            int temp = arr[i];
            arr[i] = arr[randomInt];
            arr[randomInt] = temp;
        }
    }

    // Returns true if the number can be inserted in the given row and column
    private boolean canInsert(int row, int col, int number)
    {
        return !(rowCheck(row,number) || colCheck(col, number) || boxCheck(row, col, number));
    }

    // Checks if a row contains given number
    private boolean rowCheck(int row, int number)
    {
        for(int i = 0; i < 9; i++)
        {
            if(sudoku[row][i] == number) return true;
        }

        return false;
    }

    // Checks if a column contains given number
    private boolean colCheck(int col, int number)
    {
        for(int i = 0; i < 9; i++)
        {
            if(sudoku[i][col] == number) return true;
        }

        return false;
    }

    // Checks if a box contains given number
    private boolean boxCheck(int row, int col, int number)
    {
        int startingRow = row - row%3; // The starting row of the box
        int startingCol = col - col%3; // The starting column of the box

        // Iterating through the box and looking for the number
        for(int i = startingRow; i < startingRow + 3; i++)
        {
            for(int j = startingCol; j < startingCol + 3; j++)
            {
                if(sudoku[i][j] == number) return true;
            }
        }

        return false;
    }
}
