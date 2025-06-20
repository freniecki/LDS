package pl.frot.fx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import pl.frot.fuzzy.base.*;
import pl.frot.model.enums.LabelType;
import pl.frot.model.enums.MembershipType;
import pl.frot.model.dtos.NewLabelDto;

import java.util.*;
import java.util.logging.Logger;

public class TopController {

    private static final Logger logger = Logger.getLogger(TopController.class.getName());

    private MainController mainController;

    @FXML private Button createSummariesButton;
    @FXML private Button saveSummariesButton;
    @FXML private Button createNewLabelButton;

    @FXML private TextField newLabelNameTextField;
    @FXML private ComboBox<LabelType> labelTypeComboBox;

    @FXML private ComboBox<String> linguisticVariableComboBox;

    @FXML private ComboBox<MembershipType> membershipTypeComboBox;
    @FXML private GridPane membershipParamsGridPane;
    private final Map<String, TextField> membershipParamsMap = new HashMap<>();
    private final String[] triangularParams = new String[]{"a", "b", "c"};
    private final String[] trapezoidalParams = new String[]{"a", "b", "c", "d"};
    private final String[] gaussianParams = new String[]{"center", "sigma"};


    @FXML
    private void initialize() {
        createSummariesButton.setPadding(new Insets(10));
        createSummariesButton.setText("Stwórz podsumowania");

        saveSummariesButton.setPadding(new Insets(10));
        saveSummariesButton.setText("Zapisz podsumowania");

        labelTypeComboBox.setItems(FXCollections.observableArrayList(LabelType.values()));
        labelTypeComboBox.setValue(LabelType.QUANTIFIER_ABSOLUTE);

        labelTypeComboBox.setOnAction(e -> {
            boolean isLV = labelTypeComboBox.getValue() == LabelType.SUMMARIZER || labelTypeComboBox.getValue() == LabelType.QUALIFIER;
            linguisticVariableComboBox.setVisible(isLV);
        });

        membershipTypeComboBox.setItems(FXCollections.observableArrayList(MembershipType.values()));
        membershipTypeComboBox.setValue(MembershipType.TRIANGULAR);
        membershipTypeComboBox.setOnAction(e -> {
            String[] params = switch (membershipTypeComboBox.getValue()) {
                case TRIANGULAR -> triangularParams;
                case TRAPEZOIDAL -> trapezoidalParams;
                case GAUSSIAN -> gaussianParams;
            };
            addToGrid(params, membershipParamsGridPane, membershipParamsMap);
        });

        createNewLabelButton.setOnAction(e -> createNewLabel());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        createSummariesButton.setOnAction(e -> mainController.createSummaries());

        List<String> lvNames = mainController.getSummaryMachine().getLinguisticVariablesNames();
        linguisticVariableComboBox.setItems(FXCollections.observableArrayList(lvNames));
        linguisticVariableComboBox.setValue(lvNames.getFirst());
    }

    public void createNewLabel() {
        LabelType labelType = labelTypeComboBox.getValue();
        String labelName = newLabelNameTextField.getText();
        String lvName = "";
        if (labelType == LabelType.SUMMARIZER || labelType == LabelType.QUALIFIER) {
            lvName = linguisticVariableComboBox.getValue();
        }
        MembershipFunction<Double> membershipFunction = createMembershipFunction();

        logger.info("""
                name: %s
                type: %s
                lvName: %s
                memFun: %s
                """.formatted(labelName, labelType, lvName, membershipFunction.getClass().getName()));
        mainController.addCustomLabel(new NewLabelDto(labelType, labelName, lvName, membershipFunction));
    }

    // ======== UTILS ========

    private MembershipFunction<Double> createMembershipFunction() {
        List<Double> params;

        return switch (membershipTypeComboBox.getValue()) {
            case TRIANGULAR -> {
                params = readFromGrid(triangularParams, membershipParamsMap);
                if (params.size() != 3) {
                    throw new IllegalStateException("For triangular expected 3 params, got: " + params.size());
                }
                yield new TriangularFunction(params);
            }
            case TRAPEZOIDAL -> {
                params = readFromGrid(trapezoidalParams, membershipParamsMap);
                if (params.size() != 4) {
                    throw new IllegalStateException("For trapezoidal expected 4 params, got: " + params.size());
                }
                yield new TrapezoidalFunction(params);
            }
            case GAUSSIAN -> {
                params = readFromGrid(gaussianParams, membershipParamsMap);
                if (params.size() != 2) {
                    throw new IllegalStateException("For gaussian expected 2 params, got: " + params.size());
                }
                yield new GaussianFunction(params);
            }
        };
    }

    private void addToGrid(String[] labelParams, GridPane gridPane, Map<String, TextField> mapForReference) {
        gridPane.getChildren().clear();
        for (int i = 0; i < labelParams.length; i++) {
            Label label = new Label(labelParams[i]);
            TextField textField = new TextField();
            gridPane.add(label, 0, i);
            gridPane.add(textField, 1, i);
            mapForReference.put(labelParams[i], textField);
        }
    }

    private List<Double> readFromGrid(String[] paramsLabels, Map<String, TextField> mapForReference) {
        List<Double> params = new ArrayList<>();
        for (String paramsLabel : paramsLabels) {
            params.add(Double.parseDouble(mapForReference.get(paramsLabel).getText()));
        }
        return params;
    }
}
