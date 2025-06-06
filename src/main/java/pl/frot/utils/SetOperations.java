package pl.frot.utils;

import java.util.ArrayList;
import java.util.List;

public class SetOperations {

    private SetOperations() {}
    /**
     * Creates power set of the {@param list} without empty element, so size equals to 2^(list.size()) - 1
     */
    public static <T> List<List<T>> getPowerSet(List<T> list) {
        List<List<T>> powerSet = new ArrayList<>();
        for (int i = 1; i <= list.size(); i++) {
            powerSet.addAll(getCombinations(list, i));
        }
        return powerSet;
    }

    /**
     * Creates list of all possible {@param n}-sized combinations from elements of the {@param list}
     */
    public static <T> List<List<T>> getCombinations(List<T> list, int n) {
        List<List<T>> combinations = new ArrayList<>();
        generateCombinations(list, n, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private static <T> void generateCombinations(List<T> list, int n, int start, List<T> current, List<List<T>> combinations) {
        if (current.size() == n) {
            combinations.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            generateCombinations(list, n, i + 1, current, combinations);
            current.remove(current.size() - 1);  // backtrack
        }
    }
}
