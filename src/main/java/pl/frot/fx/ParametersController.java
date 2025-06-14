package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import lombok.Setter;
import pl.frot.fuzzy.summaries.Label;
import pl.frot.fuzzy.summaries.LinguisticVariable;
import pl.frot.fuzzy.summaries.Quantifier;
import pl.frot.fuzzy.summaries.QuantifierType;
import pl.frot.model.CustomLabelDto;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ParametersController {
    private static final Logger logger = Logger.getLogger(ParametersController.class.getName());

    @Setter
    private MainController mainController;

    @FXML TreeView<Object> linguisticVariableTreeView;
    @FXML TreeView<Object> qualifiersTreeView;
    @FXML TreeView<Object> quantifiersTreeView;

    // ==== INITIALIZATION ====

    public void prepareView() {
        linguisticVariableTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        qualifiersTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        quantifiersTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        linguisticVariableTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        qualifiersTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        quantifiersTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());

        updateLabelTreeView();
        updateQualifierTreeView();
        updateQuantifierTreeView();
    }

    private void updateLabelTreeView() {
        CheckBoxTreeItem<Object> labelRoot = new CheckBoxTreeItem<>("Zmienne lingwistyczne");
        labelRoot.setExpanded(true);
        for (LinguisticVariable linguisticVariable : mainController.getSummaryMachine().getLinguisticVariables()) {
            loadLinguisticVariableLabels(labelRoot, linguisticVariable);
        }
        linguisticVariableTreeView.setRoot(labelRoot);
    }

    private void updateQualifierTreeView() {
        CheckBoxTreeItem<Object> qualifierRoot = new CheckBoxTreeItem<>("Kwalifikatory");
        qualifierRoot.setExpanded(true);
        for (LinguisticVariable linguisticVariable : mainController.getSummaryMachine().getLinguisticVariables()) {
            loadLinguisticVariableLabels(qualifierRoot, linguisticVariable);
        }
        qualifiersTreeView.setRoot(qualifierRoot);
    }

    private void updateQuantifierTreeView() {
        CheckBoxTreeItem<Object> quantifierRoot = new CheckBoxTreeItem<>("Kwantyfikatory");
        quantifierRoot.setExpanded(true);
        for (Quantifier quantifier : mainController.getSummaryMachine().getQuantifiers()) {
            CheckBoxTreeItem<Object> quantifierTreeItem = new CheckBoxTreeItem<>(quantifier);
            quantifierTreeItem.setSelected(false);
            quantifierRoot.getChildren().add(quantifierTreeItem);
        }
        quantifiersTreeView.setRoot(quantifierRoot);
    }

    private void loadLinguisticVariableLabels(CheckBoxTreeItem<Object> lvRoot, LinguisticVariable linguisticVariable) {
        CheckBoxTreeItem<Object> linguisticVariableTreeItem = new CheckBoxTreeItem<>(linguisticVariable.name());
        for (Label label : linguisticVariable.labels()) {
            CheckBoxTreeItem<Object> labelTreeItem = new CheckBoxTreeItem<>(label);
            linguisticVariableTreeItem.getChildren().add(labelTreeItem);
        }
        lvRoot.getChildren().add(linguisticVariableTreeItem);
    }

    // ==== ACTIONS ====

    public List<List<Label>> getToggledSummarizers() {
        List<List<Label>> toggledLabels = new ArrayList<>();
        for (TreeItem<Object> linguisticVariableItem : linguisticVariableTreeView.getRoot().getChildren()) {
            List<Label> labels = new ArrayList<>();
            for (TreeItem<Object> item : linguisticVariableItem.getChildren()) {
                CheckBoxTreeItem<Object> labelItem = (CheckBoxTreeItem<Object>) item;

                if (labelItem.isSelected()) {
                    Object value = labelItem.getValue();
                    switch (value) {
                        case Label label -> {
                            logger.fine("Selected Label: " + label);
                            labels.add(label);
                        }
                        case String variableName -> logger.info("Selected Linguistic Variable (unexpected at leaf): " + variableName);
                        default -> logger.warning("Other selected: " + value);
                    }
                }
            }
            if (!labels.isEmpty()) {
                toggledLabels.add(labels);
            }
        }
        return toggledLabels;
    }

    public List<Label> getToggledQualifiers() {
        List<Label> toggledQualifiers = new ArrayList<>();
        for (TreeItem<Object> linguisticVariableItem : qualifiersTreeView.getRoot().getChildren()) {
            for (TreeItem<Object> item : linguisticVariableItem.getChildren()) {
                CheckBoxTreeItem<Object> labelItem = (CheckBoxTreeItem<Object>) item;
                if (labelItem.isSelected()) {
                    Object value = labelItem.getValue();
                    if (value instanceof Label label) {
                        toggledQualifiers.add(label);
                    }
                }
            }
        }
        return toggledQualifiers;
    }

    public List<Quantifier> getToggledQuantifiers() {
        List<Quantifier> toggledQuantifiers = new ArrayList<>();
        for (TreeItem<Object> item : quantifiersTreeView.getRoot().getChildren()) {
            CheckBoxTreeItem<Object> quantifierItem = (CheckBoxTreeItem<Object>) item;

            if (quantifierItem.isSelected()) {
                Object value = quantifierItem.getValue();

                if (value instanceof Quantifier quantifier) {
                    logger.fine("Selected Quantifier: " + quantifier);
                    toggledQuantifiers.add(quantifier);
                } else {
                    logger.warning("Sus quantifier in treeview: " + value);
                }
            }
        }
        return toggledQuantifiers;
    }

    // ==== ADD NEW LABEL ====

    public void addNewCustom(CustomLabelDto customLabelDto) {
        switch (customLabelDto.labelType()) {
            case QUANTIFIER_ABSOLUTE:
                addNewQuantifier(customLabelDto, QuantifierType.ABSOLUTE);
                break;
            case QUANTIFIER_RELATIVE:
                addNewQuantifier(customLabelDto, QuantifierType.RELATIVE);
                break;
            case QUALIFIER:
                addNewQualifier(customLabelDto);
                break;
            case SUMMARIZER:
                addNewSummarizer(customLabelDto);
        }
        logger.info("New label added: " + customLabelDto.name());
    }

    public void addNewQuantifier(CustomLabelDto customLabelDto, QuantifierType quantifierType) {
        Quantifier quantifier = new Quantifier(customLabelDto.name(), quantifierType, customLabelDto.fuzzySet());
        CheckBoxTreeItem<Object> quantifierTreeItem = new CheckBoxTreeItem<>(quantifier);
        quantifierTreeItem.setSelected(false);
        quantifiersTreeView.getRoot().getChildren().add(quantifierTreeItem);
    }

    private void addNewQualifier(CustomLabelDto customLabelDto) {
        Label label = new Label(customLabelDto.name(), customLabelDto.fuzzySet(), customLabelDto.lvName());
        CheckBoxTreeItem<Object> labelTreeItem = new CheckBoxTreeItem<>(label);
        labelTreeItem.setSelected(false);
        for (TreeItem<Object> linguisticVariableItem : qualifiersTreeView.getRoot().getChildren()) {
            if (linguisticVariableItem.getValue().equals(customLabelDto.lvName())) {
                logger.info("Adding custom qualifier to linguistic variable: " + customLabelDto.lvName());
                linguisticVariableItem.getChildren().add(labelTreeItem);
                break;
            }
        }
    }

    public void addNewSummarizer(CustomLabelDto customLabelDto) {
        Label label = new Label(customLabelDto.name(), customLabelDto.fuzzySet(), customLabelDto.lvName());
        CheckBoxTreeItem<Object> labelTreeItem = new CheckBoxTreeItem<>(label);
        labelTreeItem.setSelected(false);
        for (TreeItem<Object> linguisticVariableItem : linguisticVariableTreeView.getRoot().getChildren()) {
            if (linguisticVariableItem.getValue().equals(customLabelDto.lvName())) {
                logger.info("Adding custom summarizer to linguistic variable: " + customLabelDto.lvName());
                linguisticVariableItem.getChildren().add(labelTreeItem);
                break;
            }
        }
    }
}
