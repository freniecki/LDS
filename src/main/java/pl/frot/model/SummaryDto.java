package pl.frot.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public record SummaryDto(
        String summary,
        double degreeOfTruth,
        double degreeOfImprecision,
        double degreeOfCovering,
        double degreeOfAppropriateness,
        double summaryLength,
        String qualifier,
        BooleanProperty selected
) {
    public SummaryDto(String summary, double degreeOfTruth, double degreeOfImprecision,
                      double degreeOfCovering, double degreeOfAppropriateness,
                      double summaryLength, String qualifier) {
        this(summary, degreeOfTruth, degreeOfImprecision, degreeOfCovering, degreeOfAppropriateness, summaryLength, qualifier, new SimpleBooleanProperty(false));
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

}