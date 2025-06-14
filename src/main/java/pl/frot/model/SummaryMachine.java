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
    Map<PropertyType, List<Property>> propertiesByType = new EnumMap<>(PropertyType.class);
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

        checkProperties();

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

    private void checkProperties() {
        for (PropertyType propertyType : PropertyType.values()) {
            propertiesByType.put(propertyType, new ArrayList<>());
        }

        int[] counts = new int[7];
        for (Property property : properties) {
            char c = property.getZip().charAt(1);
            int i = c - '0';
            counts[i] += 1;
            switch (c) {
                case '0', '1':
                    propertiesByType.get(PropertyType.LOS_ANGELES_AREA).add(property);
                    break;
                case '2':
                    propertiesByType.get(PropertyType.SAN_FRANCISCO_PENINSULA).add(property);
                    break;
                case '3':
                    propertiesByType.get(PropertyType.CENTRAL_CALIFORNIA).add(property);
                    break;
                case '4':
                    propertiesByType.get(PropertyType.SAN_DIEGO_REGION).add(property);
                    break;
                case '5':
                    propertiesByType.get(PropertyType.NORTHERN_CALIFORNIA).add(property);
                    break;
                case '6':
                    propertiesByType.get(PropertyType.MOUNTAIN_NORTHEAST).add(property);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid 2nd number in zip code");
            }
        }
        logger.info("""
                Created properties by type map.
                Zip codes distribution: %s
                Zip codes sum: %s
                """.formatted(Arrays.toString(counts), Arrays.stream(counts).sum()));
    }

    private void loadLinguisticVariables(List<TermDao> linguisticVariablesDaoList) {
        for (TermDao linguisticVariableDao : linguisticVariablesDaoList) {
            String attributeName = linguisticVariableDao.name();
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

    // ==== MULTISUBJECT SUMMARIZING ====

    public List<MultisubjectSummary> createMultisubjectSummaries(
            List<Quantifier> quantifiers,
            List<Label> qualifiers,
            List<List<Label>> summarizers) {

        List<MultisubjectSummary> allSummaries = createFirstTypeMultisubjectSummaries(quantifiers, summarizers);

        if (!qualifiers.isEmpty()) {
            allSummaries.addAll(createSecondTypeMultisubjectSummaries(quantifiers, qualifiers, summarizers));
            allSummaries.addAll(createThirdTypeMultisubjectSummaries(quantifiers, qualifiers, summarizers));
        }

        allSummaries.addAll(createFourthTypeMultisubjectSummaries(summarizers));

        return allSummaries;
    }

    public List<MultisubjectSummary> createFirstTypeMultisubjectSummaries(List<Quantifier> chosenQuantifiers, List<List<Label>> chosenLabels) {
        List<MultisubjectSummary> summaries = new ArrayList<>();
        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 3);

        // Create all possible pairs of property types
        PropertyType[] types = PropertyType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                PropertyType type1 = types[i];
                PropertyType type2 = types[j];

                // Skip if either population is empty
                if (propertiesByType.get(type1).isEmpty() || propertiesByType.get(type2).isEmpty()) {
                    continue;
                }

                for (Quantifier quantifier : chosenQuantifiers) {
                    // Only relative quantifiers for multisubject
                    if (quantifier.type() != QuantifierType.RELATIVE) {
                        continue;
                    }

                    for (List<Label> labelCombination : labelCombinations) {
                        MultisubjectSummary summary = new MultisubjectSummary(
                                quantifier,
                                null,  // No qualifier for Form 1
                                labelCombination,
                                type1,
                                type2,
                                propertiesByType,
                                attributeExtractors,
                                false  // qualifierAppliesTo1 (not relevant when no qualifier)
                        );
                        summaries.add(summary);
                    }
                }
            }
        }

        logger.info("🔄 Generated " + summaries.size() + " Form 1 multisubject summaries");
        return summaries;
    }

    public List<MultisubjectSummary> createSecondTypeMultisubjectSummaries(
            List<Quantifier> chosenQuantifiers,
            List<Label> chosenQualifiers,
            List<List<Label>> chosenLabels) {

        List<MultisubjectSummary> summaries = new ArrayList<>();
        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 3);

        // Create all possible pairs of property types
        PropertyType[] types = PropertyType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                PropertyType type1 = types[i];
                PropertyType type2 = types[j];

                if (propertiesByType.get(type1).isEmpty() || propertiesByType.get(type2).isEmpty()) {
                    continue;
                }

                for (Quantifier quantifier : chosenQuantifiers) {
                    if (quantifier.type() != QuantifierType.RELATIVE) {
                        continue;
                    }

                    for (Label qualifier : chosenQualifiers) {
                        for (List<Label> labelCombination : labelCombinations) {
                            if (labelCombination.contains(qualifier)) {
                                continue;
                            }

                            MultisubjectSummary summary = new MultisubjectSummary(
                                    quantifier,
                                    qualifier,
                                    labelCombination,
                                    type1,
                                    type2,
                                    propertiesByType,
                                    attributeExtractors,
                                    false  // Form 2: qualifier applies to P₂
                            );
                            summaries.add(summary);
                        }
                    }
                }
            }
        }

        logger.info("🔄 Generated " + summaries.size() + " Form 2 multisubject summaries");
        return summaries;
    }

    public List<MultisubjectSummary> createThirdTypeMultisubjectSummaries(
            List<Quantifier> chosenQuantifiers,
            List<Label> chosenQualifiers,
            List<List<Label>> chosenLabels) {

        List<MultisubjectSummary> summaries = new ArrayList<>();
        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 3);

        // Create all possible pairs of property types
        PropertyType[] types = PropertyType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                PropertyType type1 = types[i];
                PropertyType type2 = types[j];

                if (propertiesByType.get(type1).isEmpty() || propertiesByType.get(type2).isEmpty()) {
                    continue;
                }

                for (Quantifier quantifier : chosenQuantifiers) {
                    if (quantifier.type() != QuantifierType.RELATIVE) {
                        continue;
                    }

                    for (Label qualifier : chosenQualifiers) {
                        for (List<Label> labelCombination : labelCombinations) {
                            if (labelCombination.contains(qualifier)) {
                                continue;
                            }

                            MultisubjectSummary summary = new MultisubjectSummary(
                                    quantifier,
                                    qualifier,
                                    labelCombination,
                                    type1,
                                    type2,
                                    propertiesByType,
                                    attributeExtractors,
                                    true  // Form 3: qualifier applies to P₁
                            );
                            summaries.add(summary);
                        }
                    }
                }
            }
        }

        logger.info("🔄 Generated " + summaries.size() + " Form 3 multisubject summaries");
        return summaries;
    }

    public List<MultisubjectSummary> createFourthTypeMultisubjectSummaries(List<List<Label>> chosenLabels) {
        List<MultisubjectSummary> summaries = new ArrayList<>();
        List<List<Label>> labelCombinations = SetOperations.getCrossListCombinations(chosenLabels, 3);

        PropertyType[] types = PropertyType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                PropertyType type1 = types[i];
                PropertyType type2 = types[j];

                if (propertiesByType.get(type1).isEmpty() || propertiesByType.get(type2).isEmpty()) {
                    continue;
                }

                for (List<Label> labelCombination : labelCombinations) {
                    MultisubjectSummary summary = new MultisubjectSummary(
                            null,  // No quantifier for Form 4
                            null,  // No qualifier for Form 4
                            labelCombination,
                            type1,
                            type2,
                            propertiesByType,
                            attributeExtractors,
                            false
                    );
                    summaries.add(summary);
                }
            }
        }

        logger.info("🔄 Generated " + summaries.size() + " Form 4 multisubject summaries");
        return summaries;
    }
    // ==== UTILS ====

    public boolean isNewLabelValid(NewLabelDto newLabelDto) {
        FuzzySet<Double> newFuzzySet = newLabelDto.fuzzySet();

        if (!newFuzzySet.isConvex()) {
            logger.warning("Proposed fuzzy set is not convex!");
            throw new IllegalArgumentException("Proposed fuzzy set is not convex!");
        }

        if (!newFuzzySet.isNormal()) {
            newFuzzySet.normalize();
        }

        LabelType labelType = newLabelDto.labelType();
        if (labelType == LabelType.QUANTIFIER_ABSOLUTE || labelType == LabelType.QUANTIFIER_RELATIVE) {
            if (newFuzzySet.getUniverse().getDomainType() != DomainType.CONTINUOUS) {
                logger.warning("UoD for quantifier must be of continuous type!");
                throw new IllegalArgumentException("UoD for quantifier must be of continuous type!");
            }

            if (labelType == LabelType.QUANTIFIER_RELATIVE
                    && newFuzzySet.getUniverse().getSamples().getFirst() != 0
                    && newFuzzySet.getUniverse().getSamples().getLast() != 1) {
                logger.warning("Relative quantifier must have UoD of [0,1]");
                throw new IllegalArgumentException("Relative quantifier must have UoD of [0,1]");
            }

            if (labelType == LabelType.QUANTIFIER_ABSOLUTE
                    && newFuzzySet.getUniverse().getSamples().getFirst() != 1
                    && newFuzzySet.getUniverse().getSamples().getLast() != properties.size()) {
                logger.warning("Absolute quantifier must have UoD of [1, size] | size=" + properties.size());
                throw new IllegalArgumentException("Absolute quantifier must have UoD of properties size");
            }
        }

        return true;
    }

    public void saveToFile(List<String> strings) {
        DataWriter.saveToFile(strings);
    }
}