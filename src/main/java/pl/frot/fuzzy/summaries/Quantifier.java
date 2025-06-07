package pl.frot.fuzzy.summaries;

import pl.frot.fuzzy.base.FuzzySet;

public record Quantifier(String name, QuantifierType type, FuzzySet<Double> fuzzySet) {
    @Override
    public String toString() {
        return name;
    }
}
