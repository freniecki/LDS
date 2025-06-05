package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pl.frot.data.SummaryDto;

import java.io.IOException;

public class MainController {

    @FXML private HBox topContainer;
    @FXML private VBox parametersContainer;
    @FXML private TableView<SummaryDto> summaryContainer;

    @FXML
    public void initialize() throws IOException {
        FXMLLoader topLoader = new FXMLLoader(getClass().getResource("top.fxml"));
        HBox topHbox = topLoader.load();
        TopController topController = topLoader.getController();
        topContainer.getChildren().add(topHbox);

        FXMLLoader parametersLoader = new FXMLLoader(getClass().getResource("parameters.fxml"));
        VBox parametersVbox = parametersLoader.load();
        ParametersController parametersController = parametersLoader.getController();
        parametersContainer.getChildren().add(parametersVbox);

        FXMLLoader summaryLoader = new FXMLLoader(getClass().getResource("summaries.fxml"));
        VBox summaryVbox = summaryLoader.load();
        SummaryController summaryController = summaryLoader.getController();
        //summaryContainer.getChildren().add(summaryVbox);
    }


}
