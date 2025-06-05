package pl.frot.fuzzy.summaries;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class Summary {
    private Quantifier quantifier;
    private Label qualifier;
    private List<Label> summarizers;
    private Label compoundSummarizer;

    public double degreeOfTruth() {
        double sigmaCount = compoundSummarizer.fuzzySet.getSigmaCount();

        double M = switch (quantifier.type) {
            case ABSOLUTE -> 1.0;
            case RELATIVE -> compoundSummarizer.fuzzySet.getUniverse().getSamples().size();
        };

        return quantifier.fuzzySet.membership(sigmaCount / M);
    }

    public double degreeOfImprecision() {
        double value = 0.0;
        double n = (double) 1 / summarizers.size();

        for (Label label : summarizers) {
            value *= label.fuzzySet.getSigmaCount() / label.fuzzySet.getUniverse().getSamples().size();
        }

        return 1 - Math.pow(value, n);
    }

    public double degreeOfCovering() {
        if (qualifier == null) {
            return 0.0;
        }
        double m = 1; 
        return qualifier.fuzzySet.intersection(compoundSummarizer.fuzzySet).getSupport().size() / m;
    }

    public double degreeOfAppropriateness() {
        return 0.0;
    }

    public double summaryLength() {
        return 0.0;
    }

    public double degreeOfQuantifierImprecision() {
        return 0.0;
    }

    public double degreeOfQuantifierCardinality() {
        return 0.0;
    }

    public double degreeOfSummarizerCardinality() {
        return 0.0;
    }

    public double degreeOfQualifierImprecision() {
        return 0.0;
    }

    public double degreeOfQualifierCardinality() {
        return 0.0;
    }

    public double qualifierLength() {
        return 0.0;
    }
}