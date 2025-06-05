package pl.frot.fuzzy.summaries;

import java.util.List;

public class MultisubjectSummary {
    private Quantifier quantifier;
    private Label qualifier;
    private List<Label> summarizers;
    private String attributeName1;
    private String attributeName2;

    public MultisubjectSummary(Quantifier quantifier, Label qualifier, List<Label> summarizers, String attributeName1, String attributeName2) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;
        this.summarizers = summarizers;
        this.attributeName1 = attributeName1;
        this.attributeName2 = attributeName2;
    }

    public double degreeOfTruth() {
        return 0.0;
    }
}
