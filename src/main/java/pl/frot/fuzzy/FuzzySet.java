package pl.frot.fuzzy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Representation of a fuzzy set according to the theory of
 * Methods for the Linguistic Summarization of Data:
 * Applications of Fuzzy Sets and Their Extensions, A. Niewiadomski.
 * All references in brackets are number of equation from the book.
 */
public class FuzzySet<T> {
    private static final Logger logger = Logger.getLogger(FuzzySet.class.getName());

    private final Map<T, Double> membershipMap;

    public FuzzySet(Map<T, Double> membershipMap) {
        this.membershipMap = membershipMap;
    }

    public double membership(T element) {
        return membershipMap.getOrDefault(element, 0.0);
    }

    ClassicSet<T> getUniverseOfDiscourse() {
        return new ClassicSet<>(membershipMap.keySet());
    }

    /**
     * Retrieves elements of the fuzzy set that membership is greater than 0. (2.28)
     * @return Set of elements
     */
    ClassicSet<T> getSupport() {
        return new ClassicSet<>(
                (Set) membershipMap.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0.0)
                        .map(Map.Entry::getKey)
                        .toList()
        );
    }

    /**
     * Retrieves elements whose membership degrees are greater than or equal to {@code alpha}.
     *
     * @param alpha the threshold value for membership degree
     * @return Set of elements
     */
    ClassicSet<T> getAlphaCut(Double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            logger.warning("alpha must be in [0,1] range");
            throw new RuntimeException();
        }
        return new ClassicSet<>(
                (Set) membershipMap.entrySet().stream()
                        .filter(entry -> entry.getValue() >= alpha)
                        .map(Map.Entry::getKey)
                        .toList()
        );
    }

    /**
     * Calculate cardinality of fuzzy set as sum of all its membership degrees.
     * @return Cardinality
     */
    double getSigmaCount() {
       return membershipMap.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Simply gets supremum (maximum value) of given set.
     * @return Supremum of {@code this}
     */
    double getHeight() {
        return membershipMap.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    FuzzySet<T> normalize() {
        double height = getHeight();

        membershipMap.replaceAll((k, v) -> v / height);

        return this;
    }

    /**
     * Asserts if given set has membership degrees on span up to 1.0.
     * @return True if set is normal
     */
    public boolean isNormal() {
        return this.getHeight() == 1.0;
    }

    boolean isConvex() {
        return false;
    }

    FuzzySet<T> complement() {
        Map<T, Double> newMembershipMap = new HashMap<>();
        for (Map.Entry<T, Double> entry : membershipMap.entrySet()) {
            newMembershipMap.put(entry.getKey(), 1.0 - entry.getValue());
        }
        return new FuzzySet<>(newMembershipMap);
    }
}
