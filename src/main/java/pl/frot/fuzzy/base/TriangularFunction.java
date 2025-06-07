package pl.frot.fuzzy.base;

import java.util.List;

public class TriangularFunction implements MembershipFunction<Double> {

    private final double a;
    private final double b;
    private final double c;

    public TriangularFunction(List<Double> params) {
        this.a = params.getFirst();
        this.b = params.get(1);
        this.c = params.get(2);
    }

    public TriangularFunction(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public double apply(Double x) {
        if (x <= a || x >= c) {
            return 0.0;
        }
        if (x <= b) {
            return (x - a) / (b - a);
        }
        return (c - x) / (c - b);
    }
}
