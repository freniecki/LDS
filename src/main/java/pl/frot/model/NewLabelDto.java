package pl.frot.model;

import pl.frot.fuzzy.base.MembershipFunction;

public record NewLabelDto(LabelType labelType, String name, String lvName, MembershipFunction<Double> membershipFunction) {
}
