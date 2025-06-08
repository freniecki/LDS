package pl.frot.model;

public record SummaryDto(
        String summary,
        double degreeOfTruth,        // T1
        double degreeOfImprecision,  // T2
        double degreeOfCovering,
        double degreeOfAppropriateness,
        double summaryLength,
        String qualifier
) {
    public SummaryDto(String summary, double degreeOfTruth, String qualifier) {
        this(summary, degreeOfTruth, 0.0, 0.0,0.0, 0.0, qualifier);
    }
}