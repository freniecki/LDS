package pl.frot.data;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.List;

class DataLoaderTest {
    public static void main(String[] args) throws FileNotFoundException {
        List<Property> properties = DataLoader.loadProperties("src/main/resources/property.csv");

        List<Double> lots = properties.stream().map(Property::getLot).toList();

        List<Double> lotsFiltered = lots.stream().filter(l -> l < 1E6).toList();

        System.out.println("all lots: " + lots.size());
        System.out.println("lotsFiltered: " + lotsFiltered.size());
        System.out.println(lotsFiltered.stream().max(Double::compare).orElse(0.0));

        getHistogram(lotsFiltered, "Lot");
    }

    private static void getHistogram(List<Double> data, String dataName) {
        HistogramDataset dataset = new HistogramDataset();
        double[] dataArray = data.stream().mapToDouble(Double::doubleValue).toArray();
        dataset.addSeries(dataName, dataArray, 50);

        JFreeChart histogram = ChartFactory.createHistogram(
                "Histogram attribute: " + dataName,
                "Lot",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,   // legenda
                true,   // tooltips
                false   // URLs
        );
        jFrame(histogram);
    }

    private static void jFrame(JFreeChart chart) {
        JFrame frame = new JFrame("Histogram Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
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