package pl.frot.fuzzy;

import java.util.List;

/**
 * To define a classic set is required:
 * - universe of discourse
 * -
 */
public class ClassicSet {

    private boolean isDense = false;

    private List<Double> universeOfDiscourse;

    ClassicSet() {

    }

    public void setDense(boolean dense) {
        isDense = dense;
    }

    public ClassicSet getComplementOfSet() {
        return new ClassicSet();
    }

    public static ClassicSet sumOfClassicSets(ClassicSet a, ClassicSet b) {
        return new ClassicSet();
    }

    public static ClassicSet intersectionOfClassicSets(ClassicSet a, ClassicSet b) {
        return new ClassicSet();
    }

    public double getSupremum() {
        return 0.0;
    }
}
