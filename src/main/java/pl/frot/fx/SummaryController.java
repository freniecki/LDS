package pl.frot.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.Setter;
import pl.frot.fuzzy.summaries.Label;
import pl.frot.fuzzy.summaries.Quantifier;
import pl.frot.fuzzy.summaries.SingleSubjectSummary;
import pl.frot.model.SummaryDto;
import pl.frot.model.SummaryMachine;

import java.util.List;
import java.util.logging.Logger;

public class SummaryController {
    private static final Logger logger = Logger.getLogger(SummaryController.class.getName());

    @Setter
    private SummaryMachine summaryMachine;

    @Setter
    private MainController mainController;

    @FXML
    private TableView<SummaryDto> summaryTable;

    public void createSingleSubjectSummaries() {
        List<List<Label>> labels = mainController.getParametersController().getToggledSummarizers();
        logger.fine("Labels: " + labels);
        List<Quantifier> quantifiers = mainController.getParametersController().getToggledQuantifiers();
        logger.fine("Quantifiers: " + quantifiers);

        List<SingleSubjectSummary> summaries = summaryMachine.createFirstTypeSingleSubjectSummaries(quantifiers, labels);
        logger.fine("Summaries: " + summaries);

        List<SummaryDto> summaryDtos = summaries.stream().map(s -> new SummaryDto(s.toString(), s.degreeOfTruth())).toList();

        addSummariesToTable(summaryDtos);
    }

    private void addSummariesToTable(List<SummaryDto> summaryDtos) {
        summaryTable.getColumns().clear();

        // Tworzymy kolumnę dla pola summaryText
        TableColumn<SummaryDto, String> summaryTextCol = new TableColumn<>("Podsumowanie");
        summaryTextCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().summary()));

        // Tworzymy kolumnę dla pola value
        TableColumn<SummaryDto, Double> valueCol = new TableColumn<>("T1");
        valueCol.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfTruth()).asObject());

        // Dodajemy kolumny do tabeli
        summaryTable.getColumns().addAll(summaryTextCol, valueCol);

        // Ustawiamy dane
        summaryTable.getItems().clear();
        summaryTable.getItems().addAll(summaryDtos);
    }
}
