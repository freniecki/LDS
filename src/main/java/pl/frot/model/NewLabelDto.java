package pl.frot.model;

import pl.frot.fuzzy.base.MembershipFunction;
import pl.frot.fuzzy.base.Universe;

public record NewLabelDto(LabelType labelType, String name, Universe<Double> universe, MembershipFunction<Double> membershipFunction) {
}
