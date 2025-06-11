package pl.frot.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;

public record SummaryDto(
        String summary,
        double degreeOfTruth,
        double degreeOfImprecision,
        double degreeOfCovering,
        double degreeOfAppropriateness,
        double summaryLength,
        double degreeOfQuantifierImprecision,
        double degreeOfQuantifierCardinality,
        double degreeOfSummarizerCardinality,
        double degreeOfQualifierImprecision,
        double degreeOfQualifierCardinality,
        double qualifierLength,
        double optimal,
        BooleanProperty selected
) {

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public ObservableValue<Boolean> selectedProperty() {
        return selected;
    }
}