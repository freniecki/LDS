package pl.frot.data;

import java.util.List;

public record SummaryDto(String summary, List<Double> measures) {
}
