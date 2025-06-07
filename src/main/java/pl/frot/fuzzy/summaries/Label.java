package pl.frot.fuzzy.summaries;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.frot.fuzzy.base.FuzzySet;

@AllArgsConstructor
public class Label {
    @Getter
    String name;
    FuzzySet<Double> fuzzySet;

    @Override
    public String toString() {
        return name;
    }
}
