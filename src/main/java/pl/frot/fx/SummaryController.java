package pl.frot.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
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
    private MainController mainController;

    @FXML
    private TableView<SummaryDto> summaryTable;

    private void addSummariesToTable(List<SummaryDto> summaryDtos) {
        summaryTable.getColumns().clear();

        // Kolumna dla podsumowania
        TableColumn<SummaryDto, String> summaryTextCol = new TableColumn<>("Podsumowanie");
        summaryTextCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().summary()));

        TableColumn<SummaryDto, Double> t1Col = new TableColumn<>("T1");
        t1Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfTruth()).asObject());
        t1Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t2Col = new TableColumn<>("T2");
        t2Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfImprecision()).asObject());
        t2Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t3Col = new TableColumn<>("T3");
        t3Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfCovering()).asObject());
        t3Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t4Col = new TableColumn<>("T4");
        t4Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfAppropriateness()).asObject());
        t4Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t5Col = new TableColumn<>("T5");
        t5Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().summaryLength()).asObject());
        t5Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        // Dodaj wszystkie kolumny
        summaryTable.getColumns().addAll(summaryTextCol, t1Col, t2Col, t3Col, t4Col, t5Col);

        // Ustawiamy dane
        summaryTable.getItems().clear();
        summaryTable.getItems().addAll(summaryDtos);
    }
    public void createSingleSubjectSummaries() {
        List<List<Label>> labels = mainController.getParametersController().getToggledSummarizers();
        List<Label> qualifiers = mainController.getParametersController().getToggledQualifiers();
        List<Quantifier> quantifiers = mainController.getParametersController().getToggledQuantifiers();

        List<SingleSubjectSummary> summaries = mainController.getSummaryMachine().createSingleSubjectSummaries(
                quantifiers, qualifiers, labels);

        List<SummaryDto> summaryDtos = summaries.stream()
                .map(s -> new SummaryDto(
                        s.toString(),
                        s.degreeOfTruth(),
                        s.degreeOfImprecision(),
                        s.degreeOfCovering(),
                        s.degreeOfAppropriateness(),
                        s.summaryLength(),
                        s.getQualifier() != null ? s.getQualifier().getName() : null))
                .toList();

        addSummariesToTable(summaryDtos);
    }

    // ==== UTILS ====

    public static <T> Callback<TableColumn<T, Double>, TableCell<T, Double>> getDoubleCellFactory(int decimalPlaces) {
        return column -> new TableCell<T, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String format = "%." + decimalPlaces + "f";
                    setText(String.format(format, item));
                }
            }
        };
    }
}
