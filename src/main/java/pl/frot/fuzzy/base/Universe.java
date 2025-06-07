package pl.frot.fuzzy.base;

import java.util.Set;

public interface Universe<T> {

    DomainType getDomainType();

    boolean contains(T element);

    Set<T> getSamples();

    double getLength();
}
