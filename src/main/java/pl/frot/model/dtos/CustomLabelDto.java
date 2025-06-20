package pl.frot.model.dtos;

import pl.frot.fuzzy.base.FuzzySet;
import pl.frot.model.enums.LabelType;

public record CustomLabelDto(LabelType labelType, String name, String lvName, FuzzySet<Double> fuzzySet) {
}
