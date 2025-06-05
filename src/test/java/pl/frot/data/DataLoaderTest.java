package pl.frot.data;

import java.io.FileNotFoundException;
import java.util.List;

class DataLoaderTest {
    public static void main(String[] args) throws FileNotFoundException {
        List<Property> properties = DataLoader.loadProperties("src/main/resources/property.csv");

        System.out.println("size: " + properties.size());
        //printInfo(properties.stream().map(Property::getHighSchoolDistance).sorted().toList());
    }

    private static void printInfo(List<Double> attributes) {
        double min = attributes.getFirst();
        double max = attributes.getLast();
        double avg = attributes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        System.out.println("Min: " + min + " | Max: " + max + " | Avg: " + avg);
        System.out.println("[===============================================]");
        for ( double attribute : attributes) {
            System.out.print(attribute + " | ");
        }
    }
}