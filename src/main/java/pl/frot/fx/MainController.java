package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import pl.frot.model.dtos.CustomLabelDto;
import pl.frot.model.dtos.NewLabelDto;
import pl.frot.model.SummaryMachine;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class MainController {
    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    @Getter
    private ParametersController parametersController;
    @Getter
    private TopController topController;
    @Getter
    private SummaryController summaryController;

    @Getter
    @Setter
    private SummaryMachine summaryMachine;

    @FXML
    private HBox topContainer;
    @FXML
    private VBox parametersContainer;
    @FXML
    @Getter
    private Pane summaryContainer;

    @FXML
    public void initialize() throws IOException {
        // Load parameters view
        FXMLLoader parametersLoader = new FXMLLoader(getClass().getResource("parameters.fxml"));
        parametersContainer.getChildren().add(parametersLoader.load());
        parametersController = parametersLoader.getController();
        parametersController.setMainController(this);
        parametersController.prepareView();

        // Load top view
        FXMLLoader topLoader = new FXMLLoader(getClass().getResource("top.fxml"));
        topContainer.getChildren().add(topLoader.load());
        topController = topLoader.getController();
        topController.setMainController(this);

        // Load summary view
        FXMLLoader summaryLoader = new FXMLLoader(getClass().getResource("summaries.fxml"));
        summaryContainer.getChildren().add(summaryLoader.load());
        summaryController = summaryLoader.getController();
        summaryController.setMainController(this);
    }

    // ===== SUMMARIZING =====

    public void createSummaries(List<Double> measureWages) {
        summaryController.createSummaries(measureWages);
    }

    public void saveSummaries() {
        summaryController.saveSummaries();
    }

    // ===== ADD CUSTOM LABEL =====

    public void addCustomLabel(NewLabelDto newLabelDto) {
        CustomLabelDto customLabelDto = summaryMachine.isNewLabelValid(newLabelDto);
        parametersController.addCustomLabel(customLabelDto);
    }
}