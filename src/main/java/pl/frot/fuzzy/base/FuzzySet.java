package pl.frot.fuzzy.base;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FuzzySet<T> {

    private static final Logger logger = Logger.getLogger(FuzzySet.class.getName());

    private final Universe<T> domain;
    private final MembershipFunction<T> membershipFunction;

    public FuzzySet(Universe<T> domain, MembershipFunction<T> membershipFunction) {
        this.domain = domain;
        this.membershipFunction = membershipFunction;
    }

    public double membership(T element) {
        return domain.contains(element) ? membershipFunction.apply(element) : 0.0;
    }

    public Universe<T> getUniverse() {
        return domain;
    }

    public Set<T> getSupport() {
        return domain.getSamples().stream()
                .filter(x -> membership(x) > 0.0)
                .collect(Collectors.toSet());
    }

    public Set<T> getAlphaCut(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            logger.warning("Alpha must be in [0, 1]");
            throw new IllegalArgumentException();
        }
        return domain.getSamples().stream()
                .filter(x -> membership(x) >= alpha)
                .collect(Collectors.toSet());
    }

    public double getSigmaCount() {
        return domain.getSamples().stream()
                .mapToDouble(this::membership)
                .sum();
    }
    public double getDegreeOfFuzziness() {
        Set<T> support = getSupport();
        Set<T> universe = domain.getSamples();

        if (universe.isEmpty()) {
            logger.warning("Empty universe for degree of fuzziness calculation");
            return 0.0;
        }

        return (double) support.size() / universe.size();
    }

    /**
     * Alternative implementation using sigma-count normalization
     * (for continuous domains this might be more accurate)
     */
    public double getDegreeOfFuzzinessSigma() {
        double sigmaCount = getSigmaCount();
        int universeSize = domain.getSamples().size();

        if (universeSize == 0) {
            logger.warning("Empty universe for sigma-based degree of fuzziness");
            return 0.0;
        }

        // Normalize sigma-count by universe size
        return sigmaCount / universeSize;
    }
    public double getHeight() {
        return domain.getSamples().stream()
                .mapToDouble(this::membership)
                .max()
                .orElse(0.0);
    }
    public T findArgumentOfMaximum() {
        Set<T> samples = domain.getSamples();

        double maxMembership = 0.0;
        T argMax = null;

        for (T sample : samples) {
            double membership = membership(sample);
            if (membership > maxMembership) {
                maxMembership = membership;
                argMax = sample;
            }
        }

        return argMax;
    }

    public boolean isNormal() {
        return getHeight() == 1.0;
    }

    public boolean isConvex() {
        List<T> sortedSamples = domain.getSamples().stream()
                .sorted(Comparator.comparingDouble(a -> (Double) a))
                .toList();

        for (int i = 1; i < sortedSamples.size() - 1; i++) {
            T prev = sortedSamples.get(i - 1);
            T curr = sortedSamples.get(i);
            T next = sortedSamples.get(i + 1);

            // Sprawdź czy μ(curr) >= min(μ(prev), μ(next))
            double currMembership = membership(curr);
            double expectedMin = Math.min(membership(prev), membership(next));

            if (currMembership < expectedMin) {
                return false;
            }
        }
        return true;
    }

    public FuzzySet<T> complement() {
        MembershipFunction<T> complementFunction = x -> 1.0 - membership(x);
        return new FuzzySet<>(domain, complementFunction);
    }

    // ==== SET OPERATIONS ====

    public FuzzySet<T> union(FuzzySet<T> other) {
        validateCompatibility(other);

        MembershipFunction<T> unionFunction = x ->
                Math.max(this.membership(x), other.membership(x));

        return new FuzzySet<>(domain, unionFunction);
    }

    public FuzzySet<T> intersection(FuzzySet<T> other) {
        validateCompatibility(other);

        MembershipFunction<T> intersectionFunction = x ->
                Math.min(this.membership(x), other.membership(x));

        return new FuzzySet<>(domain, intersectionFunction);
    }

    // ===== UTILS =====

    private void validateCompatibility(FuzzySet<T> other) {
        if (!this.domain.equals(other.domain)) {
            throw new IllegalArgumentException("Fuzzy sets must share the same universe.");
        }
        if (this.domain.getDomainType() != other.domain.getDomainType()) {
            throw new IllegalArgumentException("Fuzzy sets must have the same domain type.");
        }
    }

}