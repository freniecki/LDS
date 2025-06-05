package pl.frot.fuzzy.base;

import java.util.Set;

public class DiscreteUniverse implements Universe<Double> {
    private final Set<Double> universe;

    public DiscreteUniverse(Set<Double> universe) {
        this.universe = universe;
    }

    public DiscreteUniverse(double start, double end, double step) {
        this.universe = new ContinousUniverse(start, end, step).getSamples();
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.DISCRETE;
    }

    @Override
    public boolean contains(Double element) {
        return universe.contains(element);
    }

    @Override
    public Set<Double> getSamples() {
        return universe;
    }
}
