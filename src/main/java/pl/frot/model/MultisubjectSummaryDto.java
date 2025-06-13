package pl.frot.model;

import javafx.beans.property.SimpleBooleanProperty;

public record MultisubjectSummaryDto(
        String summary,
        double degreeOfTruth,    // Forms 1-3 result
        double form4Result,      // Form 4 result
        SimpleBooleanProperty selected
) {
    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }
}