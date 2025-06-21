package pl.frot.fx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;
import lombok.Setter;
import pl.frot.fuzzy.summaries.Label;
import pl.frot.fuzzy.summaries.MultisubjectSummary;
import pl.frot.fuzzy.summaries.Quantifier;
import pl.frot.fuzzy.summaries.SingleSubjectSummary;
import pl.frot.model.dtos.SummaryDto;
import pl.frot.model.enums.PropertyType;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SummaryController {
    private static final Logger logger = Logger.getLogger(SummaryController.class.getName());

    @Setter
    private MainController mainController;

    @FXML
    private TableView<SummaryDto> summaryTable;

    public void createSummaries(List<Double> measureWages) {
        List<List<Label>> labels = mainController.getParametersController().getToggledSummarizers();
        List<Label> qualifiers = labels.stream().flatMap(List::stream).toList();
        List<Quantifier> quantifiers = mainController.getSummaryMachine().getQuantifiers();
        List<PropertyType> subjects = mainController.getParametersController().getToggledSubjects();

        try {
            // Generuj podsumowania jednopodmiotowe (zawsze działają)
            List<SummaryDto> summaryDtoList = createSingleSubjectSummaries(labels, qualifiers, quantifiers, measureWages);

            // Generuj podsumowania wielopodmiotowe (mogą rzucić wyjątek)
            List<SummaryDto> multiSubjectSummaries = createMultiSubjectSummaries(labels, qualifiers, quantifiers, subjects);
            summaryDtoList.addAll(multiSubjectSummaries);

            // Wyświetl wyniki
            addSummariesToTable(summaryDtoList);

        } catch (IllegalArgumentException e) {
            // Pokaż błąd konfiguracji regionów
            showErrorDialog("Błąd konfiguracji", e.getMessage());

        } catch (Exception e) {
            // Inne nieoczekiwane błędy
            showErrorDialog("Błąd", "Nieoczekiwany błąd: " + e.getMessage());
        }
    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private List<SummaryDto> createSingleSubjectSummaries(List<List<Label>> labels, List<Label> qualifiers, List<Quantifier> quantifiers, List<Double> measureWages) {
        List<SingleSubjectSummary> summaries = mainController.getSummaryMachine().createSingleSubjectSummaries(
                quantifiers, qualifiers, labels, measureWages);

        return summaries.stream()
                .map(s -> new SummaryDto(
                        s.toString(),
                        s.getMeasures().get("T1"),
                        s.getMeasures().get("T2"),
                        s.getMeasures().get("T3"),
                        s.getMeasures().get("T4"),
                        s.getMeasures().get("T5"),
                        s.getMeasures().get("T6"),
                        s.getMeasures().get("T7"),
                        s.getMeasures().get("T8"),
                        s.getMeasures().get("T9"),
                        s.getMeasures().get("T10"),
                        s.getMeasures().get("T11"),
                        s.getMeasures().get("T*"),
                        new SimpleBooleanProperty(false))
                )
                .collect(Collectors.toList());
    }

    private List<SummaryDto> createMultiSubjectSummaries(List<List<Label>> labels, List<Label> qualifiers, List<Quantifier> quantifiers, List<PropertyType> subjects) {
        List<MultisubjectSummary> summaries = mainController.getSummaryMachine()
                .createMultisubjectSummaries(quantifiers, qualifiers, labels, subjects);

        return summaries.stream()
                .map(s -> new SummaryDto(
                        s.toString(),
                        s.degreeOfTruth(),
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        new SimpleBooleanProperty(false))
                ).collect(Collectors.toList());
    }

    private void addSummariesToTable(List<SummaryDto> summaryDtos) {
        summaryTable.getColumns().clear();

        TableColumn<SummaryDto, Number> indexCol = new TableColumn<>("Lp.");
        indexCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                if (isEmpty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        indexCol.setSortable(false);

        TableColumn<SummaryDto, Boolean> selectedCol = new TableColumn<>("Zapisz");
        selectedCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));
        selectedCol.setEditable(true);

        TableColumn<SummaryDto, String> summaryTextCol = new TableColumn<>("Podsumowanie");
        summaryTextCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().summary()));
        summaryTextCol.setPrefWidth(900);
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

        TableColumn<SummaryDto, Double> t6Col = new TableColumn<>("T6");
        t6Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfQuantifierImprecision()).asObject());
        t6Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t7Col = new TableColumn<>("T7");
        t7Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfQuantifierCardinality()).asObject());
        t7Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t8Col = new TableColumn<>("T8");
        t8Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfSummarizerCardinality()).asObject());
        t8Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t9Col = new TableColumn<>("T9");
        t9Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfQualifierImprecision()).asObject());
        t9Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t10Col = new TableColumn<>("T10");
        t10Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().degreeOfQualifierCardinality()).asObject());
        t10Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> t11Col = new TableColumn<>("T11");
        t11Col.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().qualifierLength()).asObject());
        t11Col.setCellFactory(SummaryController.getDoubleCellFactory(2));

        TableColumn<SummaryDto, Double> tOptCol = new TableColumn<>("T*");
        tOptCol.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().optimal()).asObject());
        tOptCol.setCellFactory(SummaryController.getDoubleCellFactory(2));

        summaryTable.getColumns().addAll(indexCol, selectedCol, summaryTextCol, t1Col, t2Col, t3Col, t4Col, t5Col,
                t6Col, t7Col, t8Col, t9Col, t10Col, t11Col, tOptCol);
        summaryTable.setEditable(true);

        // Ustawiamy dane
        summaryTable.getItems().clear();
        summaryTable.getItems().addAll(summaryDtos);
    }

    public void saveSummaries() {
        List<SummaryDto> selectedSummaries = summaryTable.getItems().stream().filter(SummaryDto::isSelected).toList();
        List<String> summaries = selectedSummaries.stream().map(
                s -> s.summary() + " | T1=" + s.degreeOfTruth()
        ).toList();

        mainController.getSummaryMachine().saveToFile(summaries);
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