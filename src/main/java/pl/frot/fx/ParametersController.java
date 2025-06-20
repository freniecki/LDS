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
import pl.frot.model.dtos.CustomLabelDto;
import pl.frot.model.enums.PropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class ParametersController {
    private static final Logger logger = Logger.getLogger(ParametersController.class.getName());

    @Setter
    private MainController mainController;

    @FXML private TreeView<Object> linguisticVariableTreeView;
    @FXML private TreeView<Object> subjectsTreeView;

    // ==== INITIALIZATION ====

    public void prepareView() {
        linguisticVariableTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        linguisticVariableTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        updateLabelTreeView();

        subjectsTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        subjectsTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        updateSubjectsTreeView();
    }

    private void updateLabelTreeView() {
        CheckBoxTreeItem<Object> labelRoot = new CheckBoxTreeItem<>("Wybierz etykiety");
        labelRoot.setExpanded(true);
        for (LinguisticVariable linguisticVariable : mainController.getSummaryMachine().getLinguisticVariables()) {
            loadLinguisticVariableLabels(labelRoot, linguisticVariable);
        }
        linguisticVariableTreeView.setRoot(labelRoot);
    }

    private void loadLinguisticVariableLabels(CheckBoxTreeItem<Object> lvRoot, LinguisticVariable linguisticVariable) {
        CheckBoxTreeItem<Object> linguisticVariableTreeItem = new CheckBoxTreeItem<>(linguisticVariable.name());
        for (Label label : linguisticVariable.labels()) {
            CheckBoxTreeItem<Object> labelTreeItem = new CheckBoxTreeItem<>(label);
            linguisticVariableTreeItem.getChildren().add(labelTreeItem);
        }
        lvRoot.getChildren().add(linguisticVariableTreeItem);
    }

    private void updateSubjectsTreeView() {
        CheckBoxTreeItem<Object> subjectRoot = new CheckBoxTreeItem<>("Wybierz podmioty");
        subjectRoot.setExpanded(true);
        for (PropertyType subject : mainController.getSummaryMachine().getPropertiesByType().keySet()) {
            CheckBoxTreeItem<Object> subjectTreeItem = new CheckBoxTreeItem<>(subject);
            subjectRoot.getChildren().add(subjectTreeItem);
        }
        subjectsTreeView.setRoot(subjectRoot);
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

    public List<PropertyType> getToggledSubjects() {
        List<PropertyType> toggledSubjects = new ArrayList<>();
        for (TreeItem<Object> subjectItem : subjectsTreeView.getRoot().getChildren()) {
            CheckBoxTreeItem<Object> subjectTreeItem = (CheckBoxTreeItem<Object>) subjectItem;

            if (subjectTreeItem.isSelected()) {
                Object value = subjectTreeItem.getValue();
                if (Objects.requireNonNull(value) instanceof PropertyType propertyType) {
                    logger.fine("Selected PropertyType: " + propertyType);
                    toggledSubjects.add(propertyType);
                } else {
                    logger.warning("Other selected: " + value);
                }
            }
        }
        return toggledSubjects;
    }

    // ==== ADD NEW LABEL ====

    public void addCustomLabel(CustomLabelDto customLabelDto) {
        switch (customLabelDto.labelType()) {
            case SUMMARIZER, QUALIFIER -> addNewSummarizer(customLabelDto);
            case QUANTIFIER_ABSOLUTE -> addNewQuantifier(customLabelDto, QuantifierType.ABSOLUTE);
            case QUANTIFIER_RELATIVE -> addNewQuantifier(customLabelDto, QuantifierType.RELATIVE);
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

    private void addNewQuantifier(CustomLabelDto customLabelDto, QuantifierType quantifierType) {
        Quantifier quantifier = new Quantifier(customLabelDto.name(), quantifierType, customLabelDto.fuzzySet());
        mainController.getSummaryMachine().getQuantifiers().add(quantifier);
    }
}
