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
import pl.frot.model.SummaryMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ParametersController {
    private static final Logger logger = Logger.getLogger(ParametersController.class.getName());

    @Setter
    private MainController mainController;

    private SummaryMachine summaryMachine;

    @FXML TreeView<Object> linguisticVariableTreeView;
    @FXML TreeView<Object> qualifiersTreeView;
    @FXML TreeView<Object> quantifiersTreeView;

    public void setSummaryMachine(SummaryMachine summaryMachine) {
        linguisticVariableTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        qualifiersTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        quantifiersTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        linguisticVariableTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        quantifiersTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());

        this.summaryMachine = summaryMachine;
        updateView();
    }

    private void updateView() {
        updateLabelTreeView();
        updateQuantifierTreeView();
    }

    private void updateLabelTreeView() {
        CheckBoxTreeItem<Object> labelRoot = new CheckBoxTreeItem<>("Zmienne lingiwstyczne");
        labelRoot.setExpanded(true);
        for (LinguisticVariable linguisticVariable : summaryMachine.getLinguisticVariables()) {
            CheckBoxTreeItem<Object> linguisticVariableTreeItem = new CheckBoxTreeItem<>(linguisticVariable.name());
            for (Label label : linguisticVariable.labels()) {
                CheckBoxTreeItem<Object> labelTreeItem = new CheckBoxTreeItem<>(label);
                linguisticVariableTreeItem.getChildren().add(labelTreeItem);
            }
            labelRoot.getChildren().add(linguisticVariableTreeItem);
        }
        linguisticVariableTreeView.setRoot(labelRoot);
    }

    private void updateQuantifierTreeView() {
        CheckBoxTreeItem<Object> quantifierRoot = new CheckBoxTreeItem<>("Kwantyfikatory");
        quantifierRoot.setExpanded(true);
        for (Quantifier quantifier : summaryMachine.getQuantifiers()) {
            CheckBoxTreeItem<Object> quantifierTreeItem = new CheckBoxTreeItem<>(quantifier);
            quantifierTreeItem.setSelected(false);
            quantifierRoot.getChildren().add(quantifierTreeItem);
        }
        quantifiersTreeView.setRoot(quantifierRoot);
    }

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
}
