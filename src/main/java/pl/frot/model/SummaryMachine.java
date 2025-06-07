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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class SummaryMachine {

    private static final Logger logger = Logger.getLogger(SummaryMachine.class.getName());
    private Map<String, Function<Property, Double>> attributeExtractors;
    List<Property> properties = new ArrayList<>();
    @Getter
    List<LinguisticVariable> linguisticVariables = new ArrayList<>();
    @Getter
    List<Quantifier> quantifiers = new ArrayList<>();

    public static void main(String[] args) {
        SummaryMachine sm = new SummaryMachine();
        sm.run();
    }


    private void initializeAttributeExtractors() {
        attributeExtractors = new HashMap<>();
        attributeExtractors.put("soldPrice", Property::getSoldPrice);
        attributeExtractors.put("totalInteriorLivableArea", Property::getTotalInteriorLivableArea);
        attributeExtractors.put("lot", Property::getLot);
        attributeExtractors.put("yearBuilt", this::getYearBuiltAsDouble);
        attributeExtractors.put("elementarySchoolDistance", Property::getElementarySchoolDistance);
        attributeExtractors.put("middleSchoolDistance", Property::getMiddleSchoolDistance);
        attributeExtractors.put("highSchoolDistance", Property::getHighSchoolDistance);
        attributeExtractors.put("annualTaxAmount", Property::getAnnualTaxAmount);
        attributeExtractors.put("taxAssessedValue", Property::getTaxAssessedValue);
        attributeExtractors.put("lastSoldPrice", Property::getLastSoldPrice);
        attributeExtractors.put("listedPrice", Property::getListedPrice);
    }

    private Double getYearBuiltAsDouble(Property property) {
        Integer yearBuilt = property.getYearBuilt();
        return yearBuilt != null ? yearBuilt.doubleValue() : null;
    }

    public void run() {
        if (!loadData()) {
            logger.warning("Failed to load data");
            return;
        }
        initializeAttributeExtractors();
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
            String attributeName = termDao.name(); // nazwa z JSON
            List<Double> uod = termDao.uod();
            Map<String, List<Double>> ranges = termDao.ranges();

            List<Label> labels = new ArrayList<>();
            for (Map.Entry<String, List<Double>> entry : ranges.entrySet()) {
                String labelValue = entry.getKey();
                FuzzySet<Double> fuzzySet = getDoubleFuzzySet(entry.getValue(), uod);

                // ✅ Użyj konstruktor z attributeName
                labels.add(new Label(labelValue, fuzzySet, attributeName));
            }
            linguisticVariables.add(new LinguisticVariable(attributeName, labels));
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
        logger.info("🔍 attributeExtractors keys: " + (attributeExtractors != null ? attributeExtractors.keySet() : "NULL"));
        logger.info("🎯 Input: " + chosenQuantifiers.size() + " quantifiers, " + chosenLabels.size() + " label groups");

        List<SingleSubjectSummary> summaries = new ArrayList<>();
        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 4);

        logger.info("🔄 Generated " + labelCombinations.size() + " label combinations");

        for (Quantifier quantifier : chosenQuantifiers) {
            for (List<Label> labelCombination : labelCombinations) {
                SingleSubjectSummary summary = new SingleSubjectSummary(
                        quantifier,
                        null,
                        labelCombination
                );

                // ✅ DODAJ TO - ustawienie danych na summary
                summary.setData(properties, attributeExtractors);

                summaries.add(summary);
            }
        }
        return summaries;
    }
}
