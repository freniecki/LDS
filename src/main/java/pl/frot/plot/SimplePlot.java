package pl.frot.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SimplePlot extends JPanel {
    private final XYSeriesCollection dataset = new XYSeriesCollection();
    private final JFreeChart chart;
    private static String dirPath = "/home/firaanki/IOAD/ksr/projekt-2/pngs";

    public SimplePlot(String title, double domainMin, double domainMax) {
        chart = ChartFactory.createXYLineChart(
                title,
                "Wiek",
                "Przynależność",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setRange(0, 1.0);
        plot.getDomainAxis().setRange(domainMin, domainMax);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);

        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 16));

        setLayout(new BorderLayout());
        add(new ChartPanel(chart), BorderLayout.CENTER);
    }

    public void addTriangularFunction(String name, double a, double b, double c, Color color) {
        XYSeries series = new XYSeries(name);
        series.add(a, 0.0);
        series.add(b, 1.0);
        series.add(c, 0.0);
        dataset.addSeries(series);
        setColor(dataset.getSeriesCount() - 1, color);
    }

    public void addTrapezoidalFunction(String name, double a, double b, double c, double d, Color color) {
        XYSeries series = new XYSeries(name);
        series.add(a, 0.0);
        series.add(b, 1.0);
        series.add(c, 1.0);
        series.add(d, 0.0);
        dataset.addSeries(series);
        setColor(dataset.getSeriesCount() - 1, color);
    }

    private void setColor(int seriesIndex, Color color) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(seriesIndex, color);
        renderer.setSeriesStroke(seriesIndex, new BasicStroke(3.0f));
    }

    public static void main(String[] args) {
        plot("Rok budowy");
    }

    private static void plot(String title) {
        SwingUtilities.invokeLater(() -> {
            SimplePlot plot = new SimplePlot("Zmienna lingwistyczna: %s".formatted(title), 1850, 2020);

            plot.addTrapezoidalFunction("Przedwojenny", 1850, 1850, 1914, 1939, Color.BLUE);
            plot.addTrapezoidalFunction("Powojenny", 1918, 1945, 1960, 1970, Color.GREEN);
            plot.addTrapezoidalFunction("Współczesny", 1950, 1980, 2020, 2020, Color.RED);

            XYAreaRenderer renderer = new XYAreaRenderer();
            renderer.setSeriesPaint(0, new Color(0, 0, 255, 100));     // Młody
            renderer.setSeriesPaint(1, new Color(0, 200, 0, 100));     // Dojrzały
            renderer.setSeriesPaint(2, new Color(200, 0, 0, 100));     // Stary
            plot.chart.getXYPlot().setRenderer(renderer);

            JFrame frame = new JFrame("Zmienna lingwistyczna - wykres");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setContentPane(plot);
            frame.setVisible(true);

            saveToFile(title, plot);
        });
    }

    private static void saveToFile(String title, SimplePlot plot) {
        try {
            ChartUtils.saveChartAsPNG(new File(dirPath + "/%s_zmienne.png".formatted(title)), plot.chart, 800, 600);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
