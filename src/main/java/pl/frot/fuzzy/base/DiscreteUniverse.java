package pl.frot.fuzzy.base;

import java.util.List;
import java.util.Set;

public class DiscreteUniverse<T> implements Universe<T> {
    private final List<T> universe;

    public DiscreteUniverse(List<T> universe) {
        this.universe = universe;
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.DISCRETE;
    }

    @Override
    public boolean contains(T element) {
        return universe.contains(element);
    }

    @Override
    public List<T> getSamples() {
        return universe;
    }

    @Override
    public double getLength() {
        return universe.size();
    }
}
