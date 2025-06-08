package pl.frot.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public record SummaryDto(
        String summary,
        double degreeOfTruth,                    // T1
        double degreeOfImprecision,              // T2
        double degreeOfCovering,                 // T3
        double degreeOfAppropriateness,          // T4
        double summaryLength,                    // T5
        double degreeOfQuantifierImprecision,    // T6
        double degreeOfQuantifierCardinality,    // T7
        double degreeOfSummarizerCardinality,    // T8
        double degreeOfQualifierImprecision,     // T9
        double degreeOfQualifierCardinality,     // T10
        String qualifier
) {
    public SummaryDto(String summary, double degreeOfTruth, String qualifier) {
        this(summary, degreeOfTruth, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, qualifier);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

}