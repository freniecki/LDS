package pl.frot.fuzzy.summaries;

import lombok.Getter;
import pl.frot.data.Property;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class SingleSubjectSummary {
    private static final Logger logger = Logger.getLogger(SingleSubjectSummary.class.getName());

    @Getter
    private final Quantifier quantifier;
    @Getter
    private final Label qualifier;
    @Getter
    private final List<Label> summarizers;

    private final List<Property> properties;
    private final Map<String, Function<Property, Double>> attributeExtractors;

    public SingleSubjectSummary(Quantifier quantifier, Label qualifier, List<Label> summarizers,
                                List<Property> properties,
                                Map<String, Function<Property, Double>> attributeExtractors) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;
        this.summarizers = summarizers;
        this.properties = properties;
        this.attributeExtractors = attributeExtractors;
    }

    public double degreeOfTruth() {
        if (properties.isEmpty()) {
            logger.warning("No data set for summary calculation!");
            return 0.0;
        }

        if (qualifier == null) {
            // ===== FORMA PIERWSZA: Q P are/have S =====
            return calculateFirstForm();
        } else {
            // ===== FORMA DRUGA: Q P being/having W are/have S =====
            return calculateSecondForm();
        }
    }

    private double calculateFirstForm() {
        double sigmaCountS = 0.0;

        for (Property property : properties) {
            double summarizerMembership = calculateSummarizerMembership(property);
            sigmaCountS += summarizerMembership;
        }

        double normalizedValue = switch (quantifier.type()) {
            case ABSOLUTE -> sigmaCountS;
            case RELATIVE -> sigmaCountS / properties.size();
        };

        return quantifier.fuzzySet().membership(normalizedValue);
    }

    private double calculateSecondForm() {
        double sigmaCountW = 0.0;
        double sigmaCountSandW = 0.0;

        for (Property property : properties) {
            double summarizerMembership = calculateSummarizerMembership(property);
            double qualifierMembership = calculateQualifierMembership(property);

            sigmaCountW += qualifierMembership;

            double intersectionMembership = Math.min(summarizerMembership, qualifierMembership);
            sigmaCountSandW += intersectionMembership;
        }

        if (sigmaCountW == 0.0) {
            return 0.0;
        }

        return quantifier.fuzzySet().membership(sigmaCountSandW / sigmaCountW);
    }

    // ===== METODY POMOCNICZE DLA T1 =====
    private double calculateSummarizerMembership(Property property) {
        double membership = 1.0; // T-norma (AND)

        for (Label summarizer : summarizers) {
            String attributeName = summarizer.attributeName;
            Function<Property, Double> extractor = attributeExtractors.get(attributeName);
            if (extractor == null) {
                logger.warning("No extractor found for attribute: " + attributeName);
                return 0.0;
            } else {
                Double value = extractor.apply(property);
                if (value != null) {
                    membership = Math.min(membership, summarizer.getFuzzySet().membership(value));
                } else {
                    return 0.0; // Null value = no membership
                }
            }
        }

        return membership;
    }

    private double calculateQualifierMembership(Property property) {
        if (qualifier == null) return 1.0;

        String attributeName = qualifier.attributeName;
        Function<Property, Double> extractor = attributeExtractors.get(attributeName);
        if (extractor != null) {
            Double value = extractor.apply(property);
            if (value != null) {
                return qualifier.getFuzzySet().membership(value);
            }
        }

        return 0.0;
    }

    // ===== MIARY JAKOŚCI - POZOSTAWIONE NA PRZYSZŁOŚĆ =====

    public double degreeOfImprecision() {
        // TODO: Implementacja T2
        return 0.0;
    }

    public double degreeOfCovering() {
        // TODO: Implementacja T3
        return 0.0;
    }

    public double degreeOfAppropriateness() {
        // TODO: Implementacja T4
        return 0.0;
    }

    public double summaryLength() {
        // TODO: Implementacja T5
        return 0.0;
    }

    public double degreeOfQuantifierImprecision() {
        // TODO: Implementacja T6
        return 0.0;
    }

    public double degreeOfQuantifierCardinality() {
        // TODO: Implementacja T7
        return 0.0;
    }

    public double degreeOfSummarizerCardinality() {
        // TODO: Implementacja T8
        return 0.0;
    }

    public double degreeOfQualifierImprecision() {
        // TODO: Implementacja T9
        return 0.0;
    }

    public double degreeOfQualifierCardinality() {
        // TODO: Implementacja T10
        return 0.0;
    }

    public double qualifierLength() {
        // TODO: Implementacja T11
        return 0.0;
    }

    // ===== GETTERY I TOSTRING =====

    @Override
    public String toString() {
        String qualifierValue = "";
        if (qualifier != null) {
            qualifierValue = " będących " + qualifier.getName();
        }

        StringBuilder summarizerValue = new StringBuilder(" jest ");
        summarizerValue.append(summarizers.get(0).getName());
        for (int i = 1; i < summarizers.size(); i++) {
            summarizerValue.append(" i ").append(summarizers.get(i).getName());
        }

        return quantifier.name()
                + " nieruchomości"
                + qualifierValue
                + summarizerValue;
    }
}