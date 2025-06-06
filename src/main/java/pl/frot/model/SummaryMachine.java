package pl.frot.model;

import pl.frot.data.Property;
import pl.frot.data.TermDao;
import pl.frot.data.DataLoader;
import pl.frot.fuzzy.base.*;
import pl.frot.fuzzy.summaries.*;
import pl.frot.utils.SetOperations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SummaryMachine {

    private static final Logger logger = Logger.getLogger(SummaryMachine.class.getName());

    List<Property> properties = new ArrayList<>();
    List<LinguisticVariable> linguisticVariables = new ArrayList<>();
    List<List<Label>> labelCombinations = new ArrayList<>();
    List<Quantifier> quantifiers = new ArrayList<>();

    List<SingleSubjectSummary> summaries = new ArrayList<>();
    List<MultisubjectSummary> multisubjectSummaries = new ArrayList<>();


    public static void main(String[] args) {
        SummaryMachine sm = new SummaryMachine();
        sm.run();
    }

    public void run() {
        if (!loadData()) {
            logger.warning("Failed to load data");
            return;
        }
        logger.info("Loaded data successfully");

        createFirstTypeSingleSubjectSummaries();
        logger.info(String.format("Created %s I type single subject summaries.", summaries.size()));
    }

    // ==== DATA LOADING ====

    public boolean loadData() {
        try {
            properties = DataLoader.loadProperties("src/main/resources/property.csv");
        } catch (FileNotFoundException e) {
            logger.warning("File 'property.csv' not found: " + e.getMessage());
            return false;
        }

        List<TermDao> linguisticVariablesDao;
        try {
            linguisticVariablesDao = DataLoader.loadTerms("src/main/resources/summarizers.json");

        } catch (IOException e) {
            logger.warning("File 'summarizers.json' not found: " + e.getMessage());
            return false;
        }
        loadLinguisticVariables(linguisticVariablesDao);

        List<TermDao> quantifiersDao;
        try {
            quantifiersDao = DataLoader.loadTerms("src/main/resources/quantifiers.json");
        } catch (IOException e) {
            logger.warning("File 'quantifiers.json' not found: " + e.getMessage());
            return false;
        }
        loadQuantifiers(quantifiersDao);

        return true;
    }

    private void loadLinguisticVariables(List<TermDao> linguisticVariablesDao) {
        for (TermDao termDao : linguisticVariablesDao) {
            String name = termDao.name();
            List<Label> labels = new ArrayList<>();
            Map<String, List<Double>> ranges = termDao.ranges();

            for (Map.Entry<String, List<Double>> entry : ranges.entrySet()) {
                String labelValue = entry.getKey();
                FuzzySet<Double> fuzzySet = getDoubleFuzzySet(entry);

                labels.add(new Label(labelValue, fuzzySet));
            }
            linguisticVariables.add(new LinguisticVariable(name, labels));
        }

        List<Label> allLabels = new ArrayList<>();
        for (LinguisticVariable linguisticVariable : linguisticVariables) {
            allLabels.addAll(linguisticVariable.labels());
        }

        labelCombinations = SetOperations.getCombinations(allLabels, 4);
    }

    private void loadQuantifiers(List<TermDao> quantifiersDao) {
        for (TermDao quantifierDao : quantifiersDao) {
            QuantifierType type = switch (quantifierDao.name()) {
                case "relative" -> QuantifierType.RELATIVE;
                case "absolute" -> QuantifierType.ABSOLUTE;
                default -> throw new IllegalStateException("Unexpected value: " + quantifierDao.name());
            };

            Map<String, List<Double>> quantifiersLabels = quantifierDao.ranges();

            for (Map.Entry<String, List<Double>> entry : quantifiersLabels.entrySet()) {
                String labelValue = entry.getKey();
                FuzzySet<Double> fuzzySet = getDoubleFuzzySet(entry);

                quantifiers.add(new Quantifier(labelValue, type, fuzzySet));
            }
        }
    }

    private FuzzySet<Double> getDoubleFuzzySet(Map.Entry<String, List<Double>> entry) {
        List<Double> values = entry.getValue();

        MembershipFunction<Double> membershipFunction = switch (values.size()) {
            case 3 -> new TriangularFunction(values);
            case 4 -> new TrapezoidalFunction(values);
            default -> throw new IllegalStateException("Unexpected value: " + values.size());
        };

        return new FuzzySet<>(
                new DiscreteUniverse(values.get(0), values.get(1), values.get(2)),
                membershipFunction
        );
    }

    // ==== SUMMARIZING ====

    public void createFirstTypeSingleSubjectSummaries() {
        for (Quantifier quantifier : quantifiers) {
            for (List<Label> labelCombination : labelCombinations) {
                summaries.add(new SingleSubjectSummary(
                        quantifier,
                        null,
                        labelCombination,
                        null
                ));
            }
        }
    }
}
