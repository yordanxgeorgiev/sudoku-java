package Client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private Label lblSelectDifficulty;

    @FXML
    private Button btnStartGame;

    @FXML
    private ChoiceBox<Difficulty> choiceBoxDifficulty;

    @FXML
    private Button btnGiveUp;

    @FXML
    private TextField txtUser;

    @FXML
    private GridPane gp_sudoku;

    @FXML
    private Line line1;

    @FXML
    private Line line2;

    @FXML
    private Line line3;

    @FXML
    private Line line4;

    @FXML
    private Button btnUndo;

    @FXML
    private Button btnRedo;

    @FXML
    private Label lblTime;

    @FXML
    private Label lblClock;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        choiceBoxDifficulty.getItems().clear();

        for(Difficulty d: Difficulty.values())
        {
            choiceBoxDifficulty.getItems().add(d);
        }

        choiceBoxDifficulty.setValue(Difficulty.values()[0]);
        choiceBoxDifficulty.setStyle("-fx-font: 15 arial");
    }
}
