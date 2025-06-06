package pl.frot.utils;

import java.util.ArrayList;
import java.util.List;

class SetOperationsTest {

    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        names.add("A");
        names.add("B");
        names.add("C");
        names.add("D");

        List<List<String>> combinations = SetOperations.getPowerSet(names);
        System.out.println(combinations);
    }
}