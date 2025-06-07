package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import pl.frot.data.SummaryDto;
import pl.frot.model.SummaryMachine;

import java.io.IOException;

public class MainController {

    @Getter @Setter
    private SummaryMachine summaryMachine;

    @FXML private HBox topContainer;
    @FXML private VBox parametersContainer;
    @FXML private ScrollPane summaryContainer;

    @FXML
    public void initialize() throws IOException {
        FXMLLoader topLoader = new FXMLLoader(getClass().getResource("top.fxml"));
        HBox topHbox = topLoader.load();
        topContainer.getChildren().add(topHbox);
        //TopController topController = topLoader.getController();

        FXMLLoader parametersLoader = new FXMLLoader(getClass().getResource("parameters.fxml"));
        VBox parametersVbox = parametersLoader.load();
        parametersContainer.getChildren().add(parametersVbox);
        ParametersController parametersController = parametersLoader.getController();
        parametersController.setSummaryMachine(summaryMachine);

        FXMLLoader summaryLoader = new FXMLLoader(getClass().getResource("summaries.fxml"));
        ScrollPane summaryVbox = summaryLoader.load();
        summaryContainer.setContent(summaryVbox);
        //SummaryController summaryController = summaryLoader.getController();
    }


}
