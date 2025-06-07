package pl.frot.fx;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import pl.frot.fuzzy.summaries.Label;
import pl.frot.fuzzy.summaries.LinguisticVariable;
import pl.frot.model.SummaryMachine;

public class ParametersController {
    private SummaryMachine summaryMachine;

    @FXML TreeView<Object> linguisticVariableTreeView;
    @FXML TreeView<Object> qualifiersTreeView;
    @FXML TreeView<Object> quantifiersTreeView;

    public void setSummaryMachine(SummaryMachine summaryMachine) {
        this.summaryMachine = summaryMachine;
        updateView();
    }

    private void updateView() {
        TreeItem<Object> labelRoot = new TreeItem<>("Zmienne lingiwstyczne");
        labelRoot.setExpanded(true);

        for (LinguisticVariable linguisticVariable : summaryMachine.getLinguisticVariables()) {
            TreeItem<Object> linguisticVariableTreeItem = new TreeItem<>(linguisticVariable.name());
            for (Label label : linguisticVariable.labels()) {
                TreeItem<Object> labelTreeItem = new TreeItem<>(label);
                
                linguisticVariableTreeItem.getChildren().add(labelTreeItem);
            }
            labelRoot.getChildren().add(linguisticVariableTreeItem);
        }
        linguisticVariableTreeView.setRoot(labelRoot);
    }
}
