package RMI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;

public class RMI_Server extends UnicastRemoteObject
        implements ServerInterface{

    private int[][] sudoku;         // The sudoku that gets passed to the client (unsolved)
    private int[][] solvedSudoku;   // The solved sudoku (used to show the player the solution if he gives up)
    private SudokuSolver sudokuSolver;  // Object used to generate sudoku

    public RMI_Server() throws RemoteException {
        sudoku = new int[9][9];
        solvedSudoku = new int[9][9];
        sudokuSolver = new SudokuSolver();
    }

    @Override
    public boolean register(String name, String password) throws IOException {

        try
        {
            java.io.File loginFile = new File("login.txt");
            Scanner scanner = new Scanner(loginFile);

            while(scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                String username = line.split(" ")[0];

                if(username.equals(name))
                {
                    return false;
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter("login.txt", true));

            writer.append(name);
            writer.append(' ');

            writer.append(password);
            writer.append('\n');

            writer.close();
            return true;
        }
        catch (Exception ignored){}

        return false;
    }

    @Override
    public boolean login(String name, String password) {
        try
        {
            java.io.File loginFile = new File("login.txt");
            Scanner scanner = new Scanner(loginFile);

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] split = line.split(" ");
                String username = split[0];
                String pass = split[1];

                if (username.equals(name)) {
                    return pass.equals(password);
                }
            }
            return false;
        }
        catch (Exception ignored){}

        return false;
    }

    // generates sudoku of given difficulty
    @Override
    public void generateSudoku(Difficulty d) {

        int EmptySquares = 81 - d.getNumbers();

        sudokuSolver = new SudokuSolver();
        sudokuSolver.solve();
        sudoku = sudokuSolver.getSudoku();

        for(int i = 0; i < 9; i++)
        {
            System.arraycopy(sudoku[i], 0, solvedSudoku[i], 0, 9);
        }

        Random rnd = new Random();

        // Making random squares blank until the sudoku becomes of the given difficulty
        while(EmptySquares > 0)
        {
            int x = rnd.nextInt(9);
            int y = rnd.nextInt(9);

            if(sudoku[x][y] != 0)
            {
                sudoku[x][y] = 0;
                EmptySquares--;
            }
        }
    }

    // Function called when the game ends, stores the result
    @Override
    public void finishGame(String name, Difficulty difficulty, boolean won, int time) throws IOException {

        String wonString;
        if(won) wonString = "Won";
        else wonString = "Lost";

        BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt", true));

        writer.append(name);
        writer.append(' ');

        writer.append(difficulty.getDifficulty());
        writer.append(' ');

        writer.append(wonString);
        writer.append(' ');

        writer.append(Integer.toString(time));
        writer.append('\n');

        writer.close();
    }

    @Override
    public int[][] getSudoku() {
        return sudoku;
    }

    @Override
    public int[][] getSolvedSudoku() {
        return solvedSudoku;
    }
}
