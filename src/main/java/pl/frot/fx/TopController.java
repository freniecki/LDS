package pl.frot.fx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import pl.frot.fuzzy.base.*;
import pl.frot.model.LabelType;
import pl.frot.model.MembershipType;
import pl.frot.model.NewLabelDto;

import java.util.*;

public class TopController {

    private MainController mainController;

    @FXML private Button createSingleSubjectSummariesButton;
    @FXML private Button saveSummariesButton;
    @FXML private Button createNewLabelButton;

    @FXML private TextField newLabelNameTextField;
    @FXML private ComboBox<LabelType> labelTypeComboBox;

    @FXML private ComboBox<DomainType> universeTypeComboBox;
    @FXML private GridPane universeParamsGridPane;

    @FXML private ComboBox<MembershipType> membershipTypeComboBox;
    @FXML private GridPane membershipParamsGridPane;

    private final String[] triangularParams = new String[]{"a", "b", "c"};
    private final String[] trapezoidalParams = new String[]{"a", "b", "c", "d"};
    private final String[] gaussianParams = new String[]{"a", "b", "center", "sigma"};

    private final String[] universeParams = new String[]{"start", "end", "step"};

    @FXML
    private void initialize() {
        createSingleSubjectSummariesButton.setPadding(new Insets(10));
        createSingleSubjectSummariesButton.setText("Stwórz podsumowania jednopodmiotowe");

        saveSummariesButton.setPadding(new Insets(10));
        saveSummariesButton.setText("Zapisz podsumowania");

        labelTypeComboBox.setItems(FXCollections.observableArrayList(LabelType.values()));
        labelTypeComboBox.setValue(LabelType.QUANTIFIER);

        universeTypeComboBox.setItems(FXCollections.observableArrayList(DomainType.values()));
        universeTypeComboBox.setOnAction(e -> addToGrid(universeParams, universeParamsGridPane));

        membershipTypeComboBox.setItems(FXCollections.observableArrayList());
        membershipTypeComboBox.setValue(MembershipType.TRIANGULAR);

        membershipTypeComboBox.setOnAction(e -> {
            String[] params = switch (membershipTypeComboBox.getValue()) {
                case TRIANGULAR -> triangularParams;
                case TRAPEZOIDAL -> trapezoidalParams;
                case GAUSSIAN -> gaussianParams;
            };
            addToGrid(params, membershipParamsGridPane);
        });

        createNewLabelButton.setOnAction(e -> createNewLabel());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        createSingleSubjectSummariesButton.setOnAction( e -> mainController.createSingleSubjectSummaries());
        saveSummariesButton.setOnAction( e -> mainController.getSummaryController().saveSummaries());
    }

    public void createNewLabel() {
        LabelType labelType = labelTypeComboBox.getValue();
        String labelName = newLabelNameTextField.getText();

        MembershipFunction<Double> membershipFunction = createMembershipFunction();

        Universe<Double> universe = createUniverse();

        mainController.getSummaryMachine().addNewLabel(new NewLabelDto(
                labelType,
                labelName,
                universe,
                membershipFunction
        ));
    }

    // ======== UTILS ========

    private void addToGrid(String[] labelParams, GridPane gridPane) {
        gridPane.getChildren().clear();
        for (int i = 0; i < labelParams.length; i++) {
            Label label = new Label(labelParams[i]);
            TextField textField = new TextField();
            gridPane.add(label, i, 0);
            gridPane.add(textField, i, 1);
        }
    }

    private List<Double> readFromGrid(GridPane gridPane) {
        List<Double> params = new ArrayList<>();
        for (int i = 0; i < gridPane.getRowCount(); i++) {
            Double value = Double.parseDouble(gridPane.getChildren().get(i + gridPane.getRowCount()).toString());
            params.add(value);
        }
        return params;
    }

    private MembershipFunction<Double> createMembershipFunction() {
        List<Double> params = readFromGrid(membershipParamsGridPane);

        return switch (membershipTypeComboBox.getValue()) {
            case TRIANGULAR -> {
                if (params.size() != 3) {
                    throw new IllegalStateException("For triangular expected 3 params, got: " + params.size());
                }
                yield new TriangularFunction(params);
            }
            case TRAPEZOIDAL -> {
                if (params.size() != 4) {
                    throw new IllegalStateException("For trapezoidal expected 4 params, got: " + params.size());
                }
                yield new TrapezoidalFunction(params);
            }
            case GAUSSIAN -> {
                if (params.size() != 4) {
                    throw new IllegalStateException("For gaussian expected 4 params, got: " + params.size());
                }
                yield new GaussianFunction(params);
            }
        };
    }

    private Universe<Double> createUniverse() {
        List<Double> params = readFromGrid(universeParamsGridPane);

        return switch (universeTypeComboBox.getValue()) {
            case DISCRETE -> new DiscreteUniverse<>(params);
            case CONTINUOUS -> new ContinousUniverse(params);
        };
    }
}
