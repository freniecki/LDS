package pl.frot.model;

import java.util.List;
import java.util.Map;

public record AttrRanges(String name, Map<String, List<Double>> ranges) {
}
