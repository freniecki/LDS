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

    public double getHeight() {
        return domain.getSamples().stream()
                .mapToDouble(this::membership)
                .max()
                .orElse(0.0);
    }

    public boolean isNormal() {
        return getHeight() == 1.0;
    }

    public boolean isConvex() {
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
