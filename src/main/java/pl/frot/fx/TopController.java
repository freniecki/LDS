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
import pl.frot.model.LabelType;
import pl.frot.model.MembershipType;
import pl.frot.model.NewLabelDto;

import java.util.*;
import java.util.logging.Logger;

public class TopController {

    private static final Logger logger = Logger.getLogger(TopController.class.getName());

    private MainController mainController;

    @FXML private Button createSingleSubjectSummariesButton;
    @FXML private Button saveSummariesButton;
    @FXML private Button createNewLabelButton;

    @FXML private TextField newLabelNameTextField;
    @FXML private ComboBox<LabelType> labelTypeComboBox;

    @FXML private ComboBox<DomainType> universeTypeComboBox;
    @FXML private GridPane universeParamsGridPane;
    private final Map<String, TextField> universeParamsMap = new HashMap<>();
    private final String[] universeParams = new String[]{"start", "end", "step"};

    @FXML private ComboBox<MembershipType> membershipTypeComboBox;
    @FXML private GridPane membershipParamsGridPane;
    private final Map<String, TextField> membershipParamsMap = new HashMap<>();
    private final String[] triangularParams = new String[]{"a", "b", "c"};
    private final String[] trapezoidalParams = new String[]{"a", "b", "c", "d"};
    private final String[] gaussianParams = new String[]{"a", "b", "center", "sigma"};


    @FXML
    private void initialize() {
        createSingleSubjectSummariesButton.setPadding(new Insets(10));
        createSingleSubjectSummariesButton.setText("Stwórz podsumowania jednopodmiotowe");

        saveSummariesButton.setPadding(new Insets(10));
        saveSummariesButton.setText("Zapisz podsumowania");

        labelTypeComboBox.setItems(FXCollections.observableArrayList(LabelType.values()));
        labelTypeComboBox.setValue(LabelType.QUANTIFIER_ABSOLUTE);

        universeTypeComboBox.setItems(FXCollections.observableArrayList(DomainType.values()));
        universeTypeComboBox.setOnAction(e -> addToGrid(universeParams, universeParamsGridPane, universeParamsMap));

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
        createSingleSubjectSummariesButton.setOnAction(e -> mainController.createSingleSubjectSummaries());
        saveSummariesButton.setOnAction(e -> mainController.getSummaryController().saveSummaries());
    }

    public void createNewLabel() {
        LabelType labelType = labelTypeComboBox.getValue();
        String labelName = newLabelNameTextField.getText();

        MembershipFunction<Double> membershipFunction = createMembershipFunction();
        Universe<Double> universe = createUniverse();
        FuzzySet<Double> fuzzySet = new FuzzySet<>(universe, membershipFunction);

        NewLabelDto newLabelDto = new NewLabelDto(labelType, labelName, fuzzySet);
        logger.info("""
                name: %s
                type: %s
                fuzzyUniverse: [%f, %f]
                """.formatted(labelName, labelType,
                fuzzySet.getUniverse().getSamples().getFirst(), fuzzySet.getUniverse().getSamples().getLast()));

        boolean isValid = mainController.getSummaryMachine().isNewLabelValid(newLabelDto);
        if (isValid) {
            mainController.getParametersController().addNewCustom(newLabelDto);
        }
    }

    // ======== UTILS ========

    private Universe<Double> createUniverse() {
        List<Double> params = readFromGrid(universeParams, universeParamsMap);

        return switch (universeTypeComboBox.getValue()) {
            case DISCRETE -> {
                List<Double> samples = new ArrayList<>();
                for (double i = params.getFirst(); i <= params.get(1); i += params.get(2)) {
                    samples.add(i);
                }
                yield new DiscreteUniverse<>(samples);
            }
            case CONTINUOUS -> new ContinousUniverse(params);
        };
    }

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
                if (params.size() != 4) {
                    throw new IllegalStateException("For gaussian expected 4 params, got: " + params.size());
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
