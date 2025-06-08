package pl.frot.fuzzy.base;

import java.util.List;

public interface Universe<T> {

    DomainType getDomainType();

    boolean contains(T element);

    List<T> getSamples();

    double getLength();
}
