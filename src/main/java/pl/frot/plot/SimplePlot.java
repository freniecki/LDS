package pl.frot.plot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import pl.frot.model.AttrRanges;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimplePlot extends JPanel {
    private final XYSeriesCollection dataset = new XYSeriesCollection();
    private final JFreeChart chart;
    private final String dirPath;
    private static final Random random = new Random();

    public SimplePlot(String title, String dirPath) {
        this.dirPath = dirPath;

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

        plot.getDomainAxis().setAutoRange(true);
        plot.getRangeAxis().setAutoRange(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);

        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 16));

        setLayout(new BorderLayout());
        add(new ChartPanel(chart), BorderLayout.CENTER);
    }

    public XYSeries createTriangularSeries(String name, double a, double b, double c) {
        XYSeries series = new XYSeries(name);
        series.add(a, 0.0);
        series.add(b, 1.0);
        series.add(c, 0.0);
        return series;
    }

    public XYSeries createTrapezoidalSeries(String name, double a, double b, double c, double d) {
        XYSeries series = new XYSeries(name);
        series.add(a, 0.0);
        series.add(b, 1.0);
        series.add(c, 1.0);
        series.add(d, 0.0);
        return series;
    }

    private void setColor(int seriesIndex, Color color) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(seriesIndex, color);
        renderer.setSeriesStroke(seriesIndex, new BasicStroke(3.0f));
    }

    private static Color getPrettyPastelColor(){
        float hue = random.nextFloat();
        // Saturation between 0.1 and 0.3 -> changed to 0.5-0.7, too bright
        float saturation = (random.nextInt(2000) + 5000) / 10000f;
        float luminance = 0.9f;
        return Color.getHSBColor(hue, saturation, luminance);
    }

    private static void plot(String title, String dirPath, AttrRanges ranges) {
        SwingUtilities.invokeLater(() -> {
            SimplePlot plot = new SimplePlot("Zmienna lingwistyczna: %s".formatted(title), dirPath);

            if (ranges.name().equals("lot")) {
                LogAxis xAxis = new LogAxis("Powierzchnia działki (skala log.)");
                xAxis.setBase(10);
                xAxis.setTickUnit(new NumberTickUnit(0.5));
                plot.chart.getXYPlot().setDomainAxis(xAxis);

                for (Map.Entry<String, List<Double>> variable : ranges.ranges().entrySet()) {
                    List<Double> scaledValues = variable.getValue().stream().map(v -> v / 1000).toList();
                    ranges.ranges().put(variable.getKey(), scaledValues);
                }
            }

            if (ranges.name().equals("totalInteriorLivableArea")) {
                LogAxis xAxis = new LogAxis("Powierzchnia użytkowa (skala log.)");
                xAxis.setBase(10);
                xAxis.setTickUnit(new NumberTickUnit(0.1));
                plot.chart.getXYPlot().setDomainAxis(xAxis);
            }

            for (Map.Entry<String, List<Double>> variable : ranges.ranges().entrySet()) {
                String name = variable.getKey();
                double a = variable.getValue().getFirst();
                double b = variable.getValue().get(1);
                double c = variable.getValue().get(2);

                XYSeries series = switch (variable.getValue().size()) {
                    case 3:
                         yield plot.createTriangularSeries(name, a, b, c);
                    case 4:
                        double d = variable.getValue().get(3);
                        yield plot.createTrapezoidalSeries(name, a, b, c, d);
                    default:
                        throw new RuntimeException("dupa twojej starej");
                };

                plot.dataset.addSeries(series);
                plot.setColor(plot.dataset.getSeriesCount() - 1, getPrettyPastelColor());
            }

            showPlot(title, plot);
            saveToFile(title, plot);
        });
    }

    private static void showPlot(String title, SimplePlot plot) {
        JFrame frame = new JFrame("Zmienna lingwistyczna - %s".formatted(title));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setContentPane(plot);
        frame.setVisible(true);
    }

    private static void saveToFile(String title, SimplePlot plot) {
        try {
            ChartUtils.saveChartAsPNG(
                    Path.of(plot.dirPath + "/%s_zmienne.png".formatted(title)).toFile(),
                    plot.chart, 600, 400);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<AttrRanges> getAttrRanges(String jsonPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                Path.of(jsonPath).toFile(),
                new TypeReference<>(){});
    }

    public static void main(String[] args) throws IOException {
        List<AttrRanges> ranges = getAttrRanges("src/main/resources/ranges.json");

        plot(
                "Powierzchnia_użytkowa",
                "src/main/resources/plot",
                ranges.get(2)
        );
    }
}
