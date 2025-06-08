package pl.frot.fuzzy.base;

import pl.frot.fuzzy.summaries.Label;
import pl.frot.fuzzy.summaries.Quantifier;
import pl.frot.fuzzy.summaries.QuantifierType;
import pl.frot.fuzzy.summaries.SingleSubjectSummary;

import java.util.List;

class FuzzySetTest {
    public static void main(String[] args) {
        // create label
        FuzzySet<Double> fuzzySetS1 = new FuzzySet<>(
                new ContinousUniverse(0.0, 10.0, 0.1),
                new TriangularFunction(1.0, 5.0, 9.0));

        Label labelS1 = new Label("S1", fuzzySetS1);

        // create quantifier
        FuzzySet<Double> fuzzySetQ1 = new FuzzySet<>(
                new ContinousUniverse(0.0, 10.0, 0.1),
                new TriangularFunction(1.0, 5.0, 9.0));

        Quantifier quantifier = new Quantifier("Q1", QuantifierType.ABSOLUTE, fuzzySetQ1);
    }
}