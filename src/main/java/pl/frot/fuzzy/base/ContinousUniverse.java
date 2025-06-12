package pl.frot.fuzzy.base;

import java.util.LinkedList;
import java.util.List;

public class ContinousUniverse implements Universe<Double> {

    private final double start;
    private final double end;
    private final double step;

    public ContinousUniverse(double start, double end, double step) {
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be positive");
        }
        if (start > end) {
            throw new IllegalArgumentException("Start must be <= end");
        }

        this.start = start;
        this.end = end;
        this.step = step;
    }

    public ContinousUniverse(List<Double> params) {
        if (params.get(2) <= 0) {
            throw new IllegalArgumentException("Step must be positive");
        }
        if (params.getFirst() > params.get(1)) {
            throw new IllegalArgumentException("Start must be <= end");
        }

        this.start = params.getFirst();
        this.end = params.get(1);
        this.step = params.get(2);
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.CONTINUOUS;
    }

    @Override
    public boolean contains(Double element) {
        return element >= start && element <= end;
    }

    @Override
    public List<Double> getSamples() {
        List<Double> samples = new LinkedList<>();
        int numSamples = (int) Math.ceil((end - start) / step) + 1;

        for (int i = 0; i < numSamples; i++) {
            double val = start + i * step;
            if (val <= end) {
                samples.add(val);
            }
        }
        return samples;
    }

    @Override
    public double getLength() {
        return end - start;
    }
}
