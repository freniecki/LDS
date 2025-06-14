package pl.frot.fx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

import java.util.ArrayList;
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

        List<MultisubjectSummaryDto> summaryDtos = new ArrayList<>();

        for (MultisubjectSummary summary : summaries) {
            int formNumber = summary.getFormNumber();
            double degreeOfTruth = summary.calculateFormByNumber(formNumber);

            MultisubjectSummaryDto dto = new MultisubjectSummaryDto(
                    summary.toString(),
                    degreeOfTruth,
                    formNumber,
                    new SimpleBooleanProperty(false)
            );

            summaryDtos.add(dto);
        }

        addSummariesToTable(summaryDtos);
    }

    private void addSummariesToTable(List<MultisubjectSummaryDto> summaryDtos) {
        multisubjectTable.getColumns().clear();

        TableColumn<MultisubjectSummaryDto, Number> indexCol = new TableColumn<>("Lp.");
        indexCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                setText(isEmpty ? null : String.valueOf(getIndex() + 1));
            }
        });

        TableColumn<MultisubjectSummaryDto, Boolean> selectedCol = new TableColumn<>("Zapisz");
        selectedCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));
        selectedCol.setEditable(true);

        TableColumn<MultisubjectSummaryDto, String> summaryTextCol = new TableColumn<>("Podsumowanie");
        summaryTextCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().summary()));

        TableColumn<MultisubjectSummaryDto, Integer> formNumberCol = new TableColumn<>("Forma");
        formNumberCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().formNumber()).asObject());

        TableColumn<MultisubjectSummaryDto, Double> degreeOfTruthCol = new TableColumn<>("Stopień prawdy");
        degreeOfTruthCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().degreeOfTruth()).asObject());
        degreeOfTruthCol.setCellFactory(column -> new TableCell<MultisubjectSummaryDto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.3f", item));
            }
        });

        multisubjectTable.getColumns().addAll(indexCol, selectedCol, summaryTextCol, formNumberCol, degreeOfTruthCol);
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
}