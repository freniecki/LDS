package pl.frot.fuzzy.base;

@FunctionalInterface
public interface MembershipFunction<T> {
    double apply(T x);
}