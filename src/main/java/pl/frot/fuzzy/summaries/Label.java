package pl.frot.fuzzy.summaries;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.frot.fuzzy.base.FuzzySet;

@Getter
@AllArgsConstructor
public class Label {
    String name;
    FuzzySet<Double> fuzzySet;
    String attributeName;
    public Label(String name, FuzzySet<Double> fuzzySet) {
        this.name = name;
        this.fuzzySet = fuzzySet;
        this.attributeName = "unknown";
    }

    @Override
    public String toString() {
        return name;
    }
}