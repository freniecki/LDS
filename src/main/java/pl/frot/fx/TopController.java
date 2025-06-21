package pl.frot.fx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import pl.frot.fuzzy.base.*;
import pl.frot.model.enums.LabelType;
import pl.frot.model.enums.MembershipType;
import pl.frot.model.dtos.NewLabelDto;
import pl.frot.model.enums.PropertyType;

import java.util.*;
import java.util.function.UnaryOperator;
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

    @FXML private CheckBox useCustomWages;
    @FXML private GridPane measuresWagesGridPane;

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

        // ====== CUSTOM LABEL ======
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

        // ====== CUSTOM WAGES ======
        useCustomWages.setSelected(false);
        for (int i = 0; i < 11; i++) {
            measuresWagesGridPane.add(new Label("T" + (i + 1)), 2 * i, 0);
            measuresWagesGridPane.add(createPositiveDoubleField(),  2 * i + 1, 0);
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;

        createSummariesButton.setOnAction(e -> {
            // SPRAWDŹ REGIONY PRZED GENEROWANIEM
            List<PropertyType> subjects = mainController.getParametersController().getToggledSubjects();

            if (subjects.size() == 1) {
                showErrorDialog("Błąd konfiguracji",
                        "Wybrano tylko 1 region (" + subjects.getFirst() + "). Wybierz dokładnie 2 regiony do porównania lub odznacz wszystkie.");
                return;
            }

            if (subjects.size() > 2) {
                showErrorDialog("Błąd konfiguracji",
                        "Wybierz maksymalnie 2 regiony do porównania (zaznaczone: " + subjects.size() + ")");
                return; // Przerwij - nie generuj podsumowań
            }

            // Jeśli OK - kontynuuj normalnie
            if (useCustomWages.isSelected()) {
                validateCustomWages();
                mainController.createSummaries(readMeasureWagesFromGrid());
            } else {
                mainController.createSummaries(List.of());
            }
        });

        saveSummariesButton.setOnAction(e -> mainController.saveSummaries());

        List<String> lvNames = mainController.getSummaryMachine().getLinguisticVariablesNames();
        linguisticVariableComboBox.setItems(FXCollections.observableArrayList(lvNames));
        linguisticVariableComboBox.setValue(lvNames.getFirst());
    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private void validateCustomWages() {
        List<Double> wages = readMeasureWagesFromGrid();
        if (wages.size() != 11) {
            throw new IllegalStateException("Expected 11 wages, got: " + wages.size());
        }

        if (wages.stream().mapToDouble(Double::doubleValue).sum() != 1.0) {
            logger.warning("Sum of wages is not equal to 1");
            throw new IllegalStateException("Sum of wages is not equal to 1");
        }
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

    private List<Double> readMeasureWagesFromGrid() {
        List<Double> params = new ArrayList<>();
        var children = measuresWagesGridPane.getChildren();
        for (int i = 0; i < 11; i++) {
            TextField textField = (TextField) children.get(2 * i + 1);
            params.add(Double.parseDouble(textField.textProperty().getValue()));
        }
        return params;
    }

    public static TextField createPositiveDoubleField() {
        TextField textField = new TextField();
        textField.setPromptText("0.00");
        textField.setMaxWidth(50);

        UnaryOperator<TextFormatter.Change> doubleFilter = change -> {
            String newText = change.getControlNewText();
            // Regex: liczba zmiennoprzecinkowa, pusta wartość jest OK, kropka może wystąpić maks raz
            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };

        textField.setTextFormatter(new TextFormatter<>(doubleFilter));
        return textField;
    }
}
