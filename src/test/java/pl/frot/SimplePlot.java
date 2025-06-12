package pl.frot;

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
import pl.frot.data.TermDao;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class SimplePlot extends JPanel {
    private final static Logger logger = Logger.getLogger(SimplePlot.class.getName());

    private final XYSeriesCollection dataset = new XYSeriesCollection();
    private final JFreeChart chart;
    private final String dirPath;
    private static final Random random = new Random();

    public SimplePlot(String title, String dirPath) {
        this.dirPath = dirPath;

        chart = ChartFactory.createXYLineChart(
                title,
                title,
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

    private static void plot(String title, String dirPath, TermDao ranges) {
        SwingUtilities.invokeLater(() -> {
            SimplePlot plot = new SimplePlot(title, dirPath);

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

            if (ranges.name().equals("annualTaxAmount")) {
                LogAxis xAxis = new LogAxis("Wartość rocznego podatku ($) (skala log.)");
                xAxis.setBase(10);
                xAxis.setRange(50, 600000); // od 50 do 600,000
                plot.chart.getXYPlot().setDomainAxis(xAxis);
            }
            if (ranges.name().equals("taxAssessedValue")) {
                LogAxis xAxis = new LogAxis("Cena estymowana na podstawie podatku ($) (skala log.)");
                xAxis.setBase(10);
                xAxis.setRange(10000, 50000000); //  zakres do 50M
                plot.chart.getXYPlot().setDomainAxis(xAxis);
            }
            if (ranges.name().equals("listedPrice")) {
                LogAxis xAxis = new LogAxis("Cena oferty ($) (skala log.)");
                xAxis.setBase(10);
                xAxis.setRange(1000, 60000000); // od 1k do 60M (z marginesem)
                plot.chart.getXYPlot().setDomainAxis(xAxis);
            }

            if (ranges.name().equals("lastSoldPrice")) {
                LogAxis xAxis = new LogAxis("Ostatnia cena sprzedaży ($) (skala log.)");
                xAxis.setBase(10);
                xAxis.setRange(900, 45000000); // od 900 do 45M (z marginesem)
                plot.chart.getXYPlot().setDomainAxis(xAxis);
            }

            if (ranges.name().equals("soldPrice")) {
                LogAxis xAxis = new LogAxis("Cena sprzedaży ($) (skala log.)");
                xAxis.setBase(10);
                xAxis.setRange(90000, 100000000); // od 90k do 100M (z marginesem)
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
                        throw new IllegalStateException("params should be of 3 or 4 size, got: " + variable.getValue().size());
                };

                plot.dataset.addSeries(series);
                plot.setColor(plot.dataset.getSeriesCount() - 1, getPrettyPastelColor());
            }

            //showPlot(title, plot);
            saveToFile(title, plot);
        });
    }

    private static void showPlot(String title, SimplePlot plot) {
        JFrame frame = new JFrame(title);
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
            logger.warning("Cannot save plot to file");
        }
    }

    private static List<TermDao> getAttrRanges(String jsonPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                Path.of(jsonPath).toFile(),
                new TypeReference<>(){});
    }

    public static void main(String[] args) throws IOException {
        List<TermDao> ranges = getAttrRanges("src/main/resources/summarizers.json");

        plot("Odległość od najbliższej szkoły średniej", "src/main/resources/plot", ranges.get(5));
    }

}