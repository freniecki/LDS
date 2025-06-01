package pl.frot.fuzzy;

import java.util.Set;

public class DiscreteUoD<T> implements UoD<T> {



    @Override
    public Set<T> getUniverseOfDiscourse() {
        return Set.of();
    }
}
