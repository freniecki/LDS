package pl.frot.fuzzy;

import java.util.List;

/**
 * To define a classic set is required:
 * - universe of discourse
 * -
 */
public class CrispSet {

    private boolean isDense = false;

    private List<Double> universeOfDiscourse;

    CrispSet() {

    }

    public void setDense(boolean dense) {
        isDense = dense;
    }

    public CrispSet getComplementOfSet() {
        return new CrispSet();
    }

    public static CrispSet sumOfClassicSets(CrispSet a, CrispSet b) {
        return new CrispSet();
    }

    public static CrispSet intersectionOfClassicSets(CrispSet a, CrispSet b) {
        return new CrispSet();
    }

    public double getSupremum() {
        return 0.0;
    }
}
