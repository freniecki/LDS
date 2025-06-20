package pl.frot.model.dtos;

import pl.frot.fuzzy.base.MembershipFunction;
import pl.frot.model.enums.LabelType;

public record NewLabelDto(LabelType labelType, String name, String lvName, MembershipFunction<Double> membershipFunction) {
}
