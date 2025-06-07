package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TopController {
    private MainController mainController;

    // ==== VIEW FIELDS ====

    @FXML private Button createSingleSubjectSummariesButton;

    @FXML
    private void initialize() {
        createSingleSubjectSummariesButton.setText("Stwórz podsumowania jednopodmiotowe");
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        createSingleSubjectSummariesButton.setOnAction( e -> mainController.createSingleSubjectSummaries());
    }
}
