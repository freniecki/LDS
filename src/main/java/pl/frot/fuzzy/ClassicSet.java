package pl.frot.fuzzy;

import java.util.Set;

public class ClassicSet<T> {

    private final Set<T> elements;

    public ClassicSet(Set<T> elements) {
        this.elements = elements;
    }

    boolean contains(T element) {
        return elements.contains(element);
    }

}
