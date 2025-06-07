package pl.frot.fuzzy.summaries;

import pl.frot.fuzzy.base.FuzzySet;

import java.util.List;

public class SingleSubjectSummary {
    private final Quantifier quantifier;
    private final Label qualifier;
    private final List<Label> summarizers;
    private Label compoundSummarizer;

    public SingleSubjectSummary(Quantifier quantifier, Label qualifier, List<Label> summarizers) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;
        this.summarizers = summarizers;

//        FuzzySet<Double> compoundFuzzySet = summarizers.getFirst().fuzzySet;
//        for (int i = 1; i < summarizers.size(); i++) {
//            compoundFuzzySet = compoundFuzzySet.intersection(summarizers.get(i).fuzzySet);
//        }
//
//        this.compoundSummarizer = new Label("compound", compoundFuzzySet);
    }

    public double degreeOfTruth() {
        double sigmaCount = compoundSummarizer.fuzzySet.getSigmaCount();

        double M = switch (quantifier.type()) {
            case ABSOLUTE -> 1.0;
            case RELATIVE -> compoundSummarizer.fuzzySet.getUniverse().getSamples().size();
        };

        return quantifier.fuzzySet().membership(sigmaCount / M);
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

    @Override
    public String toString() {
        String qualifierValue = "";
        if (qualifier != null) {
            qualifierValue = " będących " + qualifier.name;
        }

        StringBuilder summarizerValue = new StringBuilder(" jest ");
        summarizerValue.append(summarizers.getFirst().name);
        for (int i = 1; i < summarizers.size(); i++) {
            summarizerValue.append(" i ").append(summarizers.get(i).name);
        }

        return quantifier.name()
                + " nieruchomości"
                + qualifierValue
                + summarizerValue;
    }
}