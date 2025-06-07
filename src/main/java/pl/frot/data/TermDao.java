package pl.frot.data;

import java.util.List;
import java.util.Map;

public record TermDao(String name, List<Double> uod, Map<String, List<Double>> ranges) {

}
