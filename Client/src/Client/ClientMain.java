package Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// If the program doesn't compile add this to the configurations
// --module-path "C:\Program Files\Java\javafx-sdk-13\lib" --add-modules javafx.controls,javafx.fxml
// where the path to the lib folder is correct

public class ClientMain extends Application {

    private ServerInterface sudokuServerObject; // The server object

    private GridPane gp_sudoku;     // Gridpane for the sudoku
    private TextField[][] squares;   // The squares of the sudoku
    private Button startGame, giveUp, undo, redo; // Buttons
    private ChoiceBox choiceBoxDifficulty; // ChoiceBox for the difficulty
    private Label lblTime, clock;   // Labels for the time
    private TextField txtUserName;  // TextField for the username

    // Some css styles
    private static final String squareStyle = "-fx-font: 21 arial; -fx-background-color: white; -fx-font-weight: 600;";
    private static final String squareErrorStyle = "-fx-font: 21 arial; -fx-background-color: red; -fx-font-weight: 600;";
    private static final String squareWonStyle = "-fx-font: 21 arial; -fx-background-color: lime; -fx-font-weight: 600;";

    private ArrayList<SudokuChange> movesToUndo = new ArrayList<>(); // Moves made
    private ArrayList<SudokuChange> movesToRedo = new  ArrayList<>(); // Returned moves

    private Difficulty difficulty; // chosen difficulty
    private int givenNumbers;

    private Timer timer;
    private long startTime;
    private int finalTime;

    private String userName;
    boolean givenUp = false; // shows if the player has given up

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.setSecurityManager(new SecurityManager());

        Parent root = FXMLLoader.load(getClass().getResource("LoginFXML.fxml"));
        primaryStage.setTitle("Sudoku");
        Scene scene = new Scene(root, 550, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeRMI();
        initializeLoginControls(scene, primaryStage);
    }

    private void initializeLoginControls(Scene scene, Stage primaryStage)
    {
        Button btnLogin = (Button) scene.lookup("#btnLogin");
        Button btnRegister = (Button) scene.lookup("#btnRegister");
        TextField txtUsername = (TextField) scene.lookup("#txtUsername");
        TextField txtPassword = (TextField) scene.lookup("#txtPassword");
        Label lblError = (Label) scene.lookup("#lblError");

        btnLogin.setOnAction(actionEvent ->  {
            try {
                if(!sudokuServerObject.login(txtUsername.getText(), txtPassword.getText()))
                {
                    lblError.setText("Error, could not login.");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            userName = txtUsername.getText();

            try {
                Parent root = FXMLLoader.load(getClass().getResource("ClientFXML.fxml"));
                primaryStage.setTitle("Sudoku");
                Scene newScene = new Scene(root, 650, 450);
                primaryStage.setScene(newScene);

                mainSudokuScreen(newScene, primaryStage);
            } catch (Exception ignored) {
            }
            });

        btnRegister.setOnAction(actionEvent -> {
            try {
                if(!sudokuServerObject.register(txtUsername.getText(), txtPassword.getText()))
                {
                    lblError.setText("Error, could not register.");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                Parent root = FXMLLoader.load(getClass().getResource("ClientFXML.fxml"));
                primaryStage.setTitle("Sudoku");
                Scene newScene = new Scene(root, 650, 450);
                primaryStage.setScene(newScene);

                mainSudokuScreen(newScene, primaryStage);
            } catch (Exception ignored) {
            }
        });

    }

    private void mainSudokuScreen(Scene scene, Stage primaryStage)
    {
        squares = new TextField[9][9];

        // Finds all the controls by their ID
        initializeControls(scene);

        // Sets listener to each square
        setSquareListeners();

        // Sets all the buttons
        setButtons();

        // Sets listener for the username
        setUsernameListener();

        primaryStage.show();
    }

    private void initializeRMI()
    {
        String host = "localhost";

        try
        {
            Registry registry = LocateRegistry.getRegistry(host, 1001);
            sudokuServerObject  = (ServerInterface) registry.lookup("RemoteObjectName");
            System.out.println("Server object " + sudokuServerObject + "found");
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    // Finds all the controls by their ID, makes some of them invisible for now
    private void initializeControls(Scene scene)
    {
        startGame = (Button) scene.lookup("#btnStartGame");
        giveUp = (Button) scene.lookup("#btnGiveUp");
        giveUp.setVisible(false);
        undo = (Button) scene.lookup("#btnUndo");
        undo.setVisible(false);
        redo = (Button) scene.lookup("#btnRedo");
        redo.setVisible(false);
        choiceBoxDifficulty = (ChoiceBox) scene.lookup("#choiceBoxDifficulty");
        choiceBoxDifficulty.setStyle("-fx-font: 17 arial; -fx-font-weight: bold");
        gp_sudoku = (GridPane) scene.lookup("#gp_sudoku");
        clock = (Label) scene.lookup("#lblClock");
        lblTime = (Label) scene.lookup("#lblTime");
        lblTime.setVisible(false);
        txtUserName = (TextField) scene.lookup("#txtUser");
        txtUserName.setText(userName);
        txtUserName.setEditable(false);
    }

    private void setButtons()
    {
        setStartGameButton();
        setUndoButton();
        setRedoButton();
        setGiveUpButton();
    }

    // generates sudoku through the RMI server
    private int[][] genSudoku(Difficulty difficulty)
    {
        int[][] sudoku = new int[9][9];
        try
        {
            sudokuServerObject.generateSudoku(difficulty);
            sudoku = sudokuServerObject.getSudoku();
        } catch (RemoteException e) {
            System.out.println(e);
        }
        return sudoku;
    }

    // checks if a sudoku is solved
    private boolean checkSudoku(int[][] sudoku)
    {
        whiteSquares();
        boolean solved = true;
        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                if(sudoku[i][j] == 0) solved = false;
                else
                {
                    for(int k = 0; k < 9; k++)
                    {
                        if(k != j && sudoku[i][k] == sudoku[i][j])
                        {
                            squares[i][j].setStyle(squareErrorStyle);
                            solved = false;
                        }
                    }
                    for(int k = 0; k < 9; k++)
                    {
                        if(k != i && sudoku[k][j] == sudoku[i][j])
                        {
                            squares[i][j].setStyle(squareErrorStyle);
                            solved = false;
                        }
                    }
                    int startingRow = i - i%3; // The starting row of the box
                    int startingCol = j - j%3; // The starting column of the box

                    for(int k = startingRow; k < startingRow + 3; k++)
                    {
                        for(int l = startingCol; l < startingCol + 3; l++)
                        {
                            if((k != i || l != j) && sudoku[k][l] == sudoku[i][j])
                            {
                                squares[i][j].setStyle(squareErrorStyle);
                                solved = false;
                            }
                        }
                    }
                }
            }
        }
        return solved;
    }

    // makes all the squares white
    private void whiteSquares()
    {
        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                squares[i][j].setStyle(squareStyle);
            }
        }
    }

    private void setStartGameButton()
    {
        startGame.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                scheduleTimer(); // scheduling a timer

                movesToUndo.clear();
                movesToRedo.clear();
                difficulty = (Difficulty) choiceBoxDifficulty.getValue();
                givenNumbers = difficulty.getNumbers();

                // making certain buttons visible (or not)
                giveUp.setVisible(true);
                undo.setVisible(true);
                redo.setVisible(true);
                startGame.setVisible(false);
                txtUserName.setEditable(false);
                choiceBoxDifficulty.setDisable(true);

                // generating sudoku through the RMI server and initializing the squares
                int[][] sudoku = genSudoku(difficulty);
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        squares[i][j].clear();
                        if (sudoku[i][j] != 0) {
                            String number = Integer.toString(sudoku[i][j]);
                            squares[i][j].setText(number);
                            squares[i][j].setEditable(false);
                        } else {
                            squares[i][j].setEditable(true);
                        }
                        if (!gp_sudoku.getChildren().contains(squares[i][j])) {
                            squares[i][j].setMaxWidth(38);
                            squares[i][j].setMinHeight(10);
                            squares[i][j].setMaxHeight(32);
                            squares[i][j].setAlignment(Pos.CENTER);
                            squares[i][j].setStyle(squareStyle);
                            gp_sudoku.add(squares[i][j], i, j, 1, 1);
                            GridPane.setMargin(squares[i][j], new Insets(1, 0, 1, 0));
                        }
                    }
                }
            }
        });
    }

    // checks for errors made by the player, also ends the game if the sudoku is solved
    private void errorCheck() throws IOException {
        int[][] sudoku = new int[9][9];

        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                String s = squares[i][j].getText();
                if(!s.equals("")) sudoku[i][j] = Integer.parseInt(s);
                else sudoku[i][j] = 0;
            }
        }
        if(checkSudoku(sudoku))
        {
            for(int i = 0; i < 9; i++)
            {
                for(int j = 0; j < 9; j++)
                {
                    squares[i][j].setStyle(squareWonStyle);
                    squares[i][j].setEditable(false);
                }
            }

            endTimer();
            sudokuServerObject.finishGame(userName,difficulty,true,finalTime);

            undo.setVisible(false);
            redo.setVisible(false);
            giveUp.setVisible(false);
        }
    }

    private void setGiveUpButton()
    {
        giveUp.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                givenUp = true;
                endTimer();
                try {
                    sudokuServerObject.finishGame(userName, difficulty, false, finalTime);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startGame.setVisible(true);
                giveUp.setVisible(false);
                undo.setVisible(false);
                redo.setVisible(false);
                txtUserName.setEditable(false);
                choiceBoxDifficulty.setDisable(false);

                // showing the solution, since the player gave up
                int[][] solvedSudoku = new int[9][9];
                try {
                    solvedSudoku = sudokuServerObject.getSolvedSudoku();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                for(int i = 0; i < 9; i++)
                {
                    for(int j = 0; j < 9; j++)
                    {
                        squares[i][j].setText(Integer.toString(solvedSudoku[i][j]));
                        squares[i][j].setEditable(false);
                    }
                }
                whiteSquares();
            }
        });
    }

    private void setUndoButton() {

        undo.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if(movesToUndo.isEmpty()) return;

                // The last changed square
                SudokuChange lastChange = movesToUndo.get(movesToUndo.size()-1);
                // Adding the undone move to the moves to redo
                movesToRedo.add(lastChange);

                squares[lastChange.getI()][lastChange.getJ()].setText("");

                // We look for the last previous change of the same square to restore its value
                for(int i = movesToUndo.size()-2; i >= 0; i--)
                {
                    SudokuChange oldChange = movesToUndo.get(i);
                    if(lastChange.sameSquare(oldChange))
                    {
                        squares[lastChange.getI()][lastChange.getJ()].setText(Integer.toString(oldChange.getN()));
                        movesToUndo.remove(movesToUndo.size()-1);
                        break;
                    }
                }
                movesToUndo.remove(movesToUndo.size()-1);
            }
        });
    }

    private void setRedoButton()
    {
        redo.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if(movesToRedo.isEmpty()) return;

                SudokuChange change = movesToRedo.get(movesToRedo.size()-1);
                squares[change.getI()][change.getJ()].setText(Integer.toString(change.getN()));

               movesToRedo.remove(movesToRedo.size()-1);
            }
        });
    }

    private void setSquareListeners()
    {
        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                squares[i][j] = new TextField();
                int finalI = i;
                int finalJ = j;

                // Listening for changes of each square
                squares[i][j].textProperty().addListener(new ChangeListener<>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                        try
                        {
                            if(!givenUp)
                            {
                                whiteSquares();
                                char[] input = t1.toCharArray();
                                char n = input[0];
                                if(t1.length() > 1 || n < '1' || n > '9') Platform.runLater(() -> squares[finalI][finalJ].clear());
                                else
                                {
                                    int newNumber = n - '0';
                                    if(givenNumbers == 0) movesToUndo.add(new SudokuChange(finalI, finalJ, newNumber));
                                    else givenNumbers--;
                                }
                                errorCheck();
                            }
                        }
                        catch (Exception ignored){}
                    }
                });
            }
        }
    }

    private void setUsernameListener()
    {
        txtUserName.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                userName = t1;
            }
        });
    }

    // schedules the timer
    private void scheduleTimer()
    {
        lblTime.setVisible(true);
        startTime = System.nanoTime();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override public void run() {
                int elapsedTime = getElapsedTime();
                Platform.runLater(()->clock.setText(Long.toString(elapsedTime)));
            }
        }, 0L, 1000L);
    }

    // returns the elapsed time in seconds
    private int getElapsedTime()
    {
        long l = System.nanoTime()-startTime;
        return (int)((double) l/(1_000_000_000));
    }

    // ends the timer
    private void endTimer()
    {
        timer.cancel();
        finalTime = getElapsedTime();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
