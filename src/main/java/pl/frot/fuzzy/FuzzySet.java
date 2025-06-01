package pl.frot.fuzzy;

/**
 * Representation of a fuzzy set according to the theory of
 * Methods for the Linguistic Summarization of Data:
 * Applications of Fuzzy Sets and Their Extensions, A. Niewiadomski.
 * All references in brackets are number of equation from the book.
 */
public class FuzzySet {
    public FuzzySet() {

    }

    CrispSet getUniverseOfDiscourse() {
        return new CrispSet();
    }

    /**
     * Retrieves elements of the fuzzy set that membership is greater than 0. (2.28)
     * @return Set of elements
     */
    CrispSet getSupport() {
        return new CrispSet();
    }

    /**
     * Retrieves elements whose membership degrees are greater than or equal to {@code alpha}.
     *
     * @param alpha the threshold value for membership degree
     * @return Set of elements
     */
    CrispSet getAlphaCut(Double alpha) {
        return new CrispSet();
    }

    /**
     * Calculate cardinality of fuzzy set as sum of all its membership degrees.
     * @return Cardinality
     */
    double getSigmaCount() {
       return 0.0;
    }

    /**
     * Simply gets supremum (maximum value) of given set.
     * @return Supremum of {@code this}
     */
    double getHeight() {
        return 0.0;
    }

    FuzzySet normalize() {
        return new FuzzySet();
    }

    /**
     * Asserts if given set has membership degrees on span up to 1.0.
     * @return True if set is normal
     */
    boolean isNormal() {
        return this.getHeight() == 1.0;
    }

    boolean isConvex() {
        return false;
    }
}
