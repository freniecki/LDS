package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import pl.frot.model.SummaryMachine;

import java.io.IOException;

public class MainController {

    @Getter
    private ParametersController parametersController;
    private TopController topController;
    private SummaryController summaryController;

    @Getter @Setter
    private SummaryMachine summaryMachine;

    @FXML private HBox topContainer;
    @FXML private VBox parametersContainer;
    @FXML private ScrollPane summaryContainer;

    @FXML
    public void initialize() throws IOException {
        // load views, put to containers, create Controllers for dependencies
        FXMLLoader parametersLoader = new FXMLLoader(getClass().getResource("parameters.fxml"));
        VBox parametersVbox = parametersLoader.load();
        parametersContainer.getChildren().add(parametersVbox);
        parametersController = parametersLoader.getController();

        FXMLLoader topLoader = new FXMLLoader(getClass().getResource("top.fxml"));
        HBox topHbox = topLoader.load();
        topContainer.getChildren().add(topHbox);
        topController = topLoader.getController();

        FXMLLoader summaryLoader = new FXMLLoader(getClass().getResource("summaries.fxml"));
        ScrollPane summaryVbox = summaryLoader.load();
        summaryContainer.setContent(summaryVbox);
        summaryController = summaryLoader.getController();

        // add dependencies between controllers
        parametersController.setMainController(this);
        topController.setMainController(this);
        summaryController.setMainController(this);

        // load model to controllers
        parametersController.setSummaryMachine(summaryMachine);
        summaryController.setSummaryMachine(summaryMachine);
    }

    public void createSingleSubjectSummaries() {
        summaryController.createSingleSubjectSummaries();
    }

}
