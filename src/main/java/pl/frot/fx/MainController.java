package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import pl.frot.model.SummaryMachine;

import java.io.IOException;
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
    private MultisubjectSummaryController multisubjectSummaryController;

    @Getter @Setter
    private SummaryMachine summaryMachine;

    // Views for switching between single and multisubject summaries
    @Getter
    private Node singleSubjectView;
    @Getter
    private Node multisubjectView;

    @FXML private HBox topContainer;
    @FXML private VBox parametersContainer;
    @FXML @Getter private Pane summaryContainer;

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

        // Load single subject summary view
        FXMLLoader summaryLoader = new FXMLLoader(getClass().getResource("summaries.fxml"));
        singleSubjectView = summaryLoader.load();
        summaryController = summaryLoader.getController();
        summaryController.setMainController(this);

        // Load multisubject summary view
        FXMLLoader multisubjectLoader = new FXMLLoader(getClass().getResource("multisubject-summaries.fxml"));
        multisubjectView = multisubjectLoader.load();
        multisubjectSummaryController = multisubjectLoader.getController();
        multisubjectSummaryController.setMainController(this);

        // Show single subject view by default
        summaryContainer.getChildren().add(singleSubjectView);
    }

    // ===== SINGLE SUBJECT METHODS =====

    public void createSingleSubjectSummaries() {
        showSingleSubjectView();
        summaryController.createSingleSubjectSummaries();
    }

    public void showSingleSubjectView() {
        summaryContainer.getChildren().clear();
        summaryContainer.getChildren().add(singleSubjectView);
    }

    // ===== MULTISUBJECT METHODS =====

    public void createMultisubjectSummaries() {
        showMultisubjectView();
        multisubjectSummaryController.createMultisubjectSummaries();
    }

    public void showMultisubjectView() {
        summaryContainer.getChildren().clear();
        summaryContainer.getChildren().add(multisubjectView);
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if single subject view is currently active
     */
    public boolean isSingleSubjectViewActive() {
        return summaryContainer.getChildren().contains(singleSubjectView);
    }

    /**
     * Check if multisubject view is currently active
     */
    public boolean isMultisubjectViewActive() {
        return summaryContainer.getChildren().contains(multisubjectView);
    }
}