package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

public class TopController {

    @FXML private Button createSingleSubjectSummariesButton;
    @FXML private Button saveSummariesButton;

    @FXML
    private void initialize() {
        createSingleSubjectSummariesButton.setPadding(new Insets(10));
        createSingleSubjectSummariesButton.setText("Stwórz podsumowania jednopodmiotowe");

        saveSummariesButton.setPadding(new Insets(10));
        saveSummariesButton.setText("Zapisz podsumowania");
    }

    public void setMainController(MainController mainController) {
        createSingleSubjectSummariesButton.setOnAction( e -> mainController.createSingleSubjectSummaries());
        saveSummariesButton.setOnAction( e -> mainController.getSummaryController().saveSummaries());
    }


}
