package pl.frot.utils;

import java.util.ArrayList;
import java.util.List;

public class SetOperations {

    private SetOperations() {}

    public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> result = new ArrayList<>();
        result.add(new ArrayList<>()); // start with empty tuple

        for (List<T> list : lists) {
            List<List<T>> newResult = new ArrayList<>();
            for (List<T> partial : result) {
                for (T element : list) {
                    List<T> newPartial = new ArrayList<>(partial);
                    newPartial.add(element);
                    newResult.add(newPartial);
                }
            }
            result = newResult;
        }

        return result;
    }

    /**
     * Creates list of all possible {@param n}-sized combinations from elements of the {@param list}
     */
    public static <T> List<List<T>> getCombinations(List<T> list, int n) {
        List<List<T>> combinations = new ArrayList<>();
        generateCombinations(list, n, 0, new ArrayList<>(), combinations);
        return combinations;
    }

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

    /**
     * Creates all combinations of elements taken from different lists in {@param listOfLists}
     * for combination sizes from 1 up to {@param maxLength}.
     * Each combination contains elements from different lists (one element per list).
     */
    public static <T> List<List<T>> getCrossListCombinations(List<List<T>> listOfLists, int maxLength) {
        List<List<T>> allCombinations = new ArrayList<>();
        int n = listOfLists.size();

        int limit = Math.min(maxLength, n);

        // For each combination length r (1..limit) generate combinations of indices of lists,
        // then build cartesian product of chosen lists' elements at those indices.
        for (int r = 1; r <= limit; r++) {
            // Get all index combinations of lists of size r
            List<List<Integer>> indexCombinations = getCombinations(generateIndexList(n), r);

            for (List<Integer> indices : indexCombinations) {
                // Extract lists corresponding to these indices
                List<List<T>> selectedLists = new ArrayList<>();
                for (int index : indices) {
                    selectedLists.add(listOfLists.get(index));
                }
                // Compute cartesian product of elements from selected lists
                List<List<T>> products = cartesianProduct(selectedLists);
                allCombinations.addAll(products);
            }
        }

        return allCombinations;
    }

    // Helper method to create list [0,1,...,n-1]
    private static List<Integer> generateIndexList(int n) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            indexes.add(i);
        }
        return indexes;
    }
}
