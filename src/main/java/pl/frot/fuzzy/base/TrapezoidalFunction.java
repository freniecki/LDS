package pl.frot.fuzzy.base;

import java.util.List;

public class TrapezoidalFunction implements MembershipFunction<Double> {

    private final double a;
    private final double b;
    private final double c;
    private final double d;

    public TrapezoidalFunction(List<Double> values) {
        a = values.getFirst();
        b = values.get(1);
        c = values.get(2);
        d = values.get(3);
    }

    @Override
    public double apply(Double x) {
        if (x >= a && x <= b) {
            return (x - a) / (b - a);
        } else if (x >= c && x <= d) {
            return (d - x) / (d - c);
        } else {
            return 0.0;
        }
    }
}
