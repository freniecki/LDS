package pl.frot.model;

import lombok.Getter;
import pl.frot.data.DataWriter;
import pl.frot.data.Property;
import pl.frot.data.TermDao;
import pl.frot.data.DataLoader;
import pl.frot.fuzzy.base.*;
import pl.frot.fuzzy.summaries.*;
import pl.frot.utils.SetOperations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public class SummaryMachine {

    private static final Logger logger = Logger.getLogger(SummaryMachine.class.getName());
    private final Map<String, Function<Property, Double>> attributeExtractors = new HashMap<>();
    List<Property> properties = new ArrayList<>();
    @Getter
    List<LinguisticVariable> linguisticVariables = new ArrayList<>();
    @Getter
    List<Quantifier> quantifiers = new ArrayList<>();

    private void initializeAttributeExtractors() {
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
        initializeAttributeExtractors();

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

    private void loadLinguisticVariables(List<TermDao> linguisticVariablesDaoList) {
        for (TermDao linguisticVariableDao : linguisticVariablesDaoList) {
            String attributeName = linguisticVariableDao.name(); // nazwa z JSON
            Map<String, List<Double>> ranges = linguisticVariableDao.ranges();
            List<Double> uod = properties.stream()
                    .map(p -> attributeExtractors.get(attributeName).apply(p))
                    .toList();

            List<Label> labels = new ArrayList<>();
            for (Map.Entry<String, List<Double>> entry : ranges.entrySet()) {
                String labelValue = entry.getKey();
                FuzzySet<Double> fuzzySet = new FuzzySet<>(
                        new DiscreteUniverse<>(uod),
                        getMembershipFunction(entry.getValue()));

                labels.add(new Label(labelValue, fuzzySet, attributeName));
            }
            linguisticVariables.add(new LinguisticVariable(attributeName, labels));
        }
    }

    private void loadQuantifiers(List<TermDao> quantifiersDaoList) {
        for (TermDao quantifierDao : quantifiersDaoList) {
            QuantifierType type = switch (quantifierDao.name()) {
                case "relative" -> QuantifierType.RELATIVE;
                case "absolute" -> QuantifierType.ABSOLUTE;
                default -> throw new IllegalStateException("Unexpected value: " + quantifierDao.name());
            };

            List<Double> uod = quantifierDao.uod();
            Map<String, List<Double>> quantifiersLabels = quantifierDao.ranges();

            for (Map.Entry<String, List<Double>> entry : quantifiersLabels.entrySet()) {
                String labelValue = entry.getKey();
                FuzzySet<Double> fuzzySet = new FuzzySet<>(
                        new ContinousUniverse(uod.get(0), uod.get(1), uod.get(2)),
                        getMembershipFunction(entry.getValue())
                );

                quantifiers.add(new Quantifier(labelValue, type, fuzzySet));
            }
        }
    }

    private MembershipFunction<Double> getMembershipFunction(List<Double> funcParams) {
        return switch (funcParams.size()) {
            case 3 -> new TriangularFunction(funcParams);
            case 4 -> new TrapezoidalFunction(funcParams);
            default -> throw new IllegalStateException("Unexpected value: " + funcParams.size());
        };
    }

    // ==== SUMMARIZING ====

    public List<SingleSubjectSummary> createSingleSubjectSummaries(
            List<Quantifier> quantifiers,
            List<Label> qualifiers,
            List<List<Label>> summarizers) {

        List<SingleSubjectSummary> allSummaries = createFirstTypeSingleSubjectSummaries(quantifiers, summarizers);

        if (!qualifiers.isEmpty()) {
            allSummaries.addAll(createSecondTypeSingleSubjectSummaries(quantifiers, qualifiers, summarizers));
        }

        return allSummaries;
    }

    public List<SingleSubjectSummary> createFirstTypeSingleSubjectSummaries(List<Quantifier> chosenQuantifiers, List<List<Label>> chosenLabels) {
        List<SingleSubjectSummary> summaries = new ArrayList<>();
        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 3);

        logger.info("🔄 Generated " + labelCombinations.size() + " label combinations");

        for (Quantifier quantifier : chosenQuantifiers) {
            for (List<Label> labelCombination : labelCombinations) {
                SingleSubjectSummary summary = new SingleSubjectSummary(
                        quantifier,
                        null,
                        labelCombination,
                        properties,
                        attributeExtractors
                );

                summaries.add(summary);
            }
        }
        return summaries;
    }

    public List<SingleSubjectSummary> createSecondTypeSingleSubjectSummaries(
            List<Quantifier> chosenQuantifiers,
            List<Label> chosenQualifiers,
            List<List<Label>> chosenLabels) {

        List<SingleSubjectSummary> summaries = new ArrayList<>();
        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 3);

        for (Quantifier quantifier : chosenQuantifiers) {
            if (quantifier.type() == QuantifierType.ABSOLUTE) {
                continue;
            }

            for (Label qualifier : chosenQualifiers) {           // ← Iteruj po wybranych kwalifikatorach
                for (List<Label> summarizers : labelCombinations) { // ← Wszystkie jako sumaryzatory
                    if (summarizers.contains(qualifier)) {
                        continue;
                    }

                    SingleSubjectSummary summary = new SingleSubjectSummary(
                            quantifier,
                            qualifier,
                            summarizers,
                            properties,
                            attributeExtractors
                    );

                    summaries.add(summary);
                }
            }
        }

        return summaries;
    }

    // ==== UTILS ====

    public void saveToFile(List<String> strings) {
        DataWriter.saveToFile(strings);
    }
}
