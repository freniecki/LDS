package pl.frot.fuzzy.summaries;

import lombok.AllArgsConstructor;
import pl.frot.fuzzy.base.FuzzySet;

@AllArgsConstructor
public class Label {
    String name;
    FuzzySet<Double> fuzzySet;

    @Override
    public String toString() {
        return name;
    }
}
