package pl.frot.model;

import pl.frot.fuzzy.base.FuzzySet;

public record NewLabelDto(LabelType labelType, String name, FuzzySet<Double> fuzzySet) {
}
