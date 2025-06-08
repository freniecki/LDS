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

    private void addSummariesToTable(List<SummaryDto> summaryDtos) {
        summaryTable.getColumns().clear();

        // Kolumna dla podsumowania
        TableColumn<SummaryDto, String> summaryTextCol = new TableColumn<>("Podsumowanie");
        summaryTextCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().summary()));

        // ✅ NOWA KOLUMNA dla kwalifikatora
        TableColumn<SummaryDto, String> qualifierCol = new TableColumn<>("Kwalifikator");
        qualifierCol.setCellValueFactory(cellData -> {
            // Wyciągnij kwalifikator z SummaryDto (trzeba dodać do SummaryDto)
            String qualifier = cellData.getValue().qualifier();
            return new SimpleStringProperty(qualifier != null ? qualifier : "brak");
        });

        // Kolumna dla T1
        TableColumn<SummaryDto, Double> valueCol = new TableColumn<>("T1");
        valueCol.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfTruth()).asObject());

        // Dodaj wszystkie kolumny
        summaryTable.getColumns().addAll(summaryTextCol, qualifierCol, valueCol); // ← DODAJ qualifierCol

        // Ustawiamy dane
        summaryTable.getItems().clear();
        summaryTable.getItems().addAll(summaryDtos);
    }

    // ZMIEŃ createSingleSubjectSummaries() - dodaj sortowanie:
    public void createSingleSubjectSummaries() {
        List<List<Label>> labels = mainController.getParametersController().getToggledSummarizers();
        List<Label> qualifiers = mainController.getParametersController().getToggledQualifiers();
        List<Quantifier> quantifiers = mainController.getParametersController().getToggledQuantifiers();

        logger.fine("Labels: " + labels);
        logger.fine("Qualifiers: " + qualifiers);
        logger.fine("Quantifiers: " + quantifiers);

        List<SingleSubjectSummary> summaries = summaryMachine.createSingleSubjectSummaries(
                quantifiers, qualifiers, labels);

        logger.fine("Summaries: " + summaries);

        List<SummaryDto> summaryDtos = summaries.stream()
                .map(s -> new SummaryDto(
                        s.toString(),
                        s.degreeOfTruth(),
                        s.getQualifier() != null ? s.getQualifier().getName() : null)) // ← DODAJ kwalifikator
                .sorted((s1, s2) -> Double.compare(s2.degreeOfTruth(), s1.degreeOfTruth())) // ← SORTOWANIE
                .toList();

        addSummariesToTable(summaryDtos);
    }
}
