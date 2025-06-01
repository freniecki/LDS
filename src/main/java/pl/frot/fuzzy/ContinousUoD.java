package pl.frot.fuzzy;

import java.util.Map;
import java.util.Set;

public class ContinousUoD<T> implements UoD<T> {

    private Map<T, Double> membershipMap;

    public ContinousUoD() {

    }

    @Override
    public Set<T> getUniverseOfDiscourse() {
        return Set.of();
    }
}
