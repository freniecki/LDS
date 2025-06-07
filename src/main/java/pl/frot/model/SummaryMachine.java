package pl.frot.model;

import lombok.Getter;
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
    @Getter
    List<LinguisticVariable> linguisticVariables = new ArrayList<>();
    @Getter
    List<Quantifier> quantifiers = new ArrayList<>();

    public static void main(String[] args) {
        SummaryMachine sm = new SummaryMachine();
        sm.run();
    }

    public void run() {
        if (!loadData()) {
            logger.warning("Failed to load data");
        }
    }

    // ==== DATA LOADING ====

    public boolean loadData() {
        try {
            properties = DataLoader.loadProperties("src/main/resources/property.csv");
            logger.info(String.format("Loaded %s properties", properties.size()));
        } catch (FileNotFoundException e) {
            logger.warning("File 'property.csv' not found: " + e.getMessage());
            return false;
        }

        List<TermDao> linguisticVariablesDao;
        try {
            linguisticVariablesDao = DataLoader.loadTerms("src/main/resources/summarizers.json");
            logger.info(String.format("Loaded %s linguistic variables", linguisticVariablesDao.size()));
        } catch (IOException e) {
            logger.warning("File 'summarizers.json' not found: " + e.getMessage());
            return false;
        }
        loadLinguisticVariables(linguisticVariablesDao);

        List<TermDao> quantifiersDao;
        try {
            quantifiersDao = DataLoader.loadTerms("src/main/resources/quantifiers.json");
            logger.info(String.format("Loaded %s quantifiers", quantifiersDao.size()));
        } catch (IOException e) {
            logger.warning("File 'quantifiers.json' not found: " + e.getMessage());
            return false;
        }
        loadQuantifiers(quantifiersDao);

        logger.info("Data loaded successfully");
        return true;
    }

    private void loadLinguisticVariables(List<TermDao> linguisticVariablesDao) {
        for (TermDao termDao : linguisticVariablesDao) {
            String name = termDao.name();
            List<Double> uod = termDao.uod();
            Map<String, List<Double>> ranges = termDao.ranges();

            List<Label> labels = new ArrayList<>();
            for (Map.Entry<String, List<Double>> entry : ranges.entrySet()) {
                String labelValue = entry.getKey();
                FuzzySet<Double> fuzzySet = getDoubleFuzzySet(entry.getValue(), uod);

                labels.add(new Label(labelValue, fuzzySet));
            }
            linguisticVariables.add(new LinguisticVariable(name, labels));
        }
    }

    private void loadQuantifiers(List<TermDao> quantifiersDao) {
        for (TermDao quantifierDao : quantifiersDao) {
            QuantifierType type = switch (quantifierDao.name()) {
                case "relative" -> QuantifierType.RELATIVE;
                case "absolute" -> QuantifierType.ABSOLUTE;
                default -> throw new IllegalStateException("Unexpected value: " + quantifierDao.name());
            };

            List<Double> uod = quantifierDao.uod();
            Map<String, List<Double>> quantifiersLabels = quantifierDao.ranges();

            for (Map.Entry<String, List<Double>> entry : quantifiersLabels.entrySet()) {
                String labelValue = entry.getKey();
                FuzzySet<Double> fuzzySet = getDoubleFuzzySet(entry.getValue(), uod);

                quantifiers.add(new Quantifier(labelValue, type, fuzzySet));
            }
        }
    }

    private FuzzySet<Double> getDoubleFuzzySet(List<Double> funcParams, List<Double> uod) {
        MembershipFunction<Double> membershipFunction = switch (funcParams.size()) {
            case 3 -> new TriangularFunction(funcParams);
            case 4 -> new TrapezoidalFunction(funcParams);
            default -> throw new IllegalStateException("Unexpected value: " + funcParams.size());
        };

        return new FuzzySet<>(
                new ContinousUniverse(uod.getFirst(), uod.get(1), uod.get(2)),
                membershipFunction
        );
    }

    // ==== SUMMARIZING ====

    public List<SingleSubjectSummary> createFirstTypeSingleSubjectSummaries(List<Quantifier> chosenQuantifiers, List<List<Label>> chosenLabels) {
        List<SingleSubjectSummary> summaries = new ArrayList<>();

        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 4);

        for (Quantifier quantifier : chosenQuantifiers) {
            for (List<Label> labelCombination : labelCombinations) {
                summaries.add(new SingleSubjectSummary(
                        quantifier,
                        null,
                        labelCombination
                ));
            }
        }
        return summaries;
    }
}
