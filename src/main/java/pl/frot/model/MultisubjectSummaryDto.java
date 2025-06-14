package pl.frot.model;

import javafx.beans.property.SimpleBooleanProperty;

public record MultisubjectSummaryDto(
        String summary,
        double degreeOfTruth,    // T value for the specific form
        int formNumber,          // Which form (1-4) is being used
        SimpleBooleanProperty selected
) {
    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }
}