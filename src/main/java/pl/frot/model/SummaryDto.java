package pl.frot.model;

public record SummaryDto(
        String summary,
        double degreeOfTruth,
        String qualifier
) {}
