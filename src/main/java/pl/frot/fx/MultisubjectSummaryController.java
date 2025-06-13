package pl.frot.fx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import lombok.Setter;
import pl.frot.fuzzy.summaries.Label;
import pl.frot.fuzzy.summaries.MultisubjectSummary;
import pl.frot.fuzzy.summaries.Quantifier;
import pl.frot.model.MultisubjectSummaryDto;

import java.util.List;
import java.util.logging.Logger;

public class MultisubjectSummaryController {
    private static final Logger logger = Logger.getLogger(MultisubjectSummaryController.class.getName());

    @Setter
    private MainController mainController;

    @FXML
    private TableView<MultisubjectSummaryDto> multisubjectTable;

    public void createMultisubjectSummaries() {
        List<List<Label>> labels = mainController.getParametersController().getToggledSummarizers();
        List<Label> qualifiers = mainController.getParametersController().getToggledQualifiers();
        List<Quantifier> quantifiers = mainController.getParametersController().getToggledQuantifiers();

        List<MultisubjectSummary> summaries = mainController.getSummaryMachine().createMultisubjectSummaries(
                quantifiers, qualifiers, labels);

        List<MultisubjectSummaryDto> summaryDtos = summaries.stream()
                .map(s -> new MultisubjectSummaryDto(
                        s.toString(),
                        s.degreeOfTruth(),      // Forms 1-3
                        s.calculateForm4(),     // Form 4
                        new SimpleBooleanProperty(false))
                )
                .toList();

        addSummariesToTable(summaryDtos);
    }

    private void addSummariesToTable(List<MultisubjectSummaryDto> summaryDtos) {
        multisubjectTable.getColumns().clear();

        // Index column
        TableColumn<MultisubjectSummaryDto, Number> indexCol = new TableColumn<>("Lp.");
        indexCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                setText(isEmpty ? null : String.valueOf(getIndex() + 1));
            }
        });
        indexCol.setSortable(false);

        // Selection column
        TableColumn<MultisubjectSummaryDto, Boolean> selectedCol = new TableColumn<>("Zapisz");
        selectedCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));
        selectedCol.setEditable(true);

        // Summary text column
        TableColumn<MultisubjectSummaryDto, String> summaryTextCol = new TableColumn<>("Podsumowanie");
        summaryTextCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().summary()));

        // Forms 1-3 result column
        TableColumn<MultisubjectSummaryDto, Double> formsCol = new TableColumn<>("Forms 1-3");
        formsCol.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfTruth()).asObject());
        formsCol.setCellFactory(getDoubleCellFactory(3));

        // Form 4 result column
        TableColumn<MultisubjectSummaryDto, Double> form4Col = new TableColumn<>("Form 4");
        form4Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().form4Result()).asObject());
        form4Col.setCellFactory(getDoubleCellFactory(3));

        multisubjectTable.getColumns().addAll(indexCol, selectedCol, summaryTextCol, formsCol, form4Col);
        multisubjectTable.setEditable(true);

        multisubjectTable.getItems().clear();
        multisubjectTable.getItems().addAll(summaryDtos);
    }

    public void saveSummaries() {
        List<MultisubjectSummaryDto> selectedSummaries = multisubjectTable.getItems().stream()
                .filter(MultisubjectSummaryDto::isSelected)
                .toList();
        List<String> summaries = selectedSummaries.stream()
                .map(MultisubjectSummaryDto::summary)
                .toList();

        mainController.getSummaryMachine().saveToFile(summaries);
    }

    // Utility method for double formatting
    private static <T> javafx.util.Callback<TableColumn<T, Double>, TableCell<T, Double>> getDoubleCellFactory(int decimalPlaces) {
        return column -> new TableCell<T, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%." + decimalPlaces + "f", item));
                }
            }
        };
    }
}