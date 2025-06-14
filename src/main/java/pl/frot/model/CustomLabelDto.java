package pl.frot.model;

import pl.frot.fuzzy.base.FuzzySet;

public record CustomLabelDto(LabelType labelType, String name, String lvName, FuzzySet<Double> fuzzySet) {
}
