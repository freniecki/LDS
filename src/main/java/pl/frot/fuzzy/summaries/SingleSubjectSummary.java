package pl.frot.fuzzy.summaries;

import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pl.frot.data.Property;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public class SingleSubjectSummary {
    private static final Logger logger = Logger.getLogger(SingleSubjectSummary.class.getName());
    private static final Log log = LogFactory.getLog(SingleSubjectSummary.class);

    @Getter
    private final Quantifier quantifier;
    @Getter
    private final Label qualifier;
    @Getter
    private final List<Label> summarizers;

    private final List<Property> properties;
    private final Map<String, Function<Property, Double>> attributeExtractors;

    @Getter
    private Map<String, Double> measures;

    public SingleSubjectSummary(Quantifier quantifier, Label qualifier, List<Label> summarizers,
                                List<Property> properties,
                                Map<String, Function<Property, Double>> attributeExtractors) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;
        if (summarizers.isEmpty()) {
            logger.warning("Summarizers must contain at least 1 summarizer");
            throw new RuntimeException("dupa");
        }
        this.summarizers = summarizers;
        this.properties = properties;
        this.attributeExtractors = attributeExtractors;

        createMeasures();
    }

    private void createMeasures() {
        measures = new LinkedHashMap<>();
        measures.put("T1", degreeOfTruth());
        measures.put("T2", degreeOfImprecision());
        measures.put("T3", degreeOfCovering());
        measures.put("T4", degreeOfAppropriateness());
        measures.put("T5", summaryLength());
        measures.put("T6", degreeOfQuantifierImprecision());
        measures.put("T7", degreeOfQuantifierCardinality());
        measures.put("T8", degreeOfSummarizerCardinality());
        measures.put("T9", degreeOfQualifierImprecision());
        measures.put("T10", degreeOfQualifierCardinality());
        measures.put("T11", qualifierLength());
        measures.put("T*", optimalMeasure(List.of()));
    }

    public double degreeOfTruth() {
        if (properties.isEmpty()) {
            logger.warning("No data set for summary calculation!");
            return 0.0;
        }

        return qualifier == null ? calculateFirstForm() : calculateSecondForm();
    }

    private double calculateFirstForm() {
        double sigmaCountS = 0.0;

        for (Property property : properties) {
            sigmaCountS += calculateSummarizerMembership(property);
        }

        if (quantifier.type() == QuantifierType.RELATIVE) {
            sigmaCountS /= properties.size();
        }

        return quantifier.fuzzySet().membership(sigmaCountS);
    }

    private double calculateSecondForm() {
        double sigmaCountW = 0.0;
        double sigmaCountSandW = 0.0;

        for (Property property : properties) {
            double summarizerMembership = calculateSummarizerMembership(property);
            double qualifierMembership = calculateQualifierMembership(property);

            sigmaCountSandW += Math.min(summarizerMembership, qualifierMembership);
            sigmaCountW += qualifierMembership;
        }

        if (sigmaCountW == 0.0) {
            return 0.0;
        }

        return quantifier.fuzzySet().membership(sigmaCountSandW / sigmaCountW);
    }

    public double degreeOfImprecision() {
        if (summarizers.isEmpty()) {
            logger.warning("T2: No summarizers available");
            return 0.0;
        }

        double product = 1.0;
        for (Label summarizer : summarizers) {
            product *= summarizer.getFuzzySet().getDegreeOfFuzziness();
        }

        return 1.0 - Math.pow(product, 1.0 / summarizers.size());
    }

    public double degreeOfCovering() {
        if (qualifier == null) {
            // FORMA 1
            int supportCount = 0;
            for (Property property : properties) {
                if (calculateSummarizerMembership(property) > 0.0) {
                    supportCount++;
                }
            }
            return (double) supportCount / properties.size();

        } else {
            // FORMA 2
            int supportW = 0;
            int supportSAndW = 0;

            for (Property property : properties) {
                double qualifierMembership = calculateQualifierMembership(property);
                if (qualifierMembership > 0.0) {
                    supportW++;
                    if (calculateSummarizerMembership(property) > 0.0) {
                        supportSAndW++;
                    }
                }
            }

            if (supportW == 0) {
                logger.warning("T3: No objects satisfy qualifier '" + qualifier.getName() + "'");
                return 0.0;
            }

            return (double) supportSAndW / supportW;
        }
    }

    public double degreeOfAppropriateness() {
        double t3 = measures.get("T3");
        double product = 1.0;

        for (Label summarizer : summarizers) {
            String attributeName = summarizer.getAttributeName();
            Function<Property, Double> extractor = attributeExtractors.get(attributeName);

            if (extractor == null) {
                logger.warning("T4: No extractor for attribute: " + attributeName);
                return 0.0;
            }

            int countSatisfying = 0;
            for (Property property : properties) {
                Double value = extractor.apply(property);
                if (value != null && summarizer.getFuzzySet().membership(value) > 0.0) {
                    countSatisfying++;
                }
            }

            double rj = (double) countSatisfying / properties.size();
            product *= rj;
        }

        return Math.abs(product - t3);
    }

    public double summaryLength() {
        int summarizerCount = summarizers.size();
        return 2.0 * Math.pow(0.5, summarizerCount);
    }

    public double degreeOfQuantifierImprecision() {
        int supportSize = quantifier.fuzzySet().getSupport().size();
        int universeSize = quantifier.fuzzySet().getUniverse().getSamples().size();

        if (universeSize == 0) {
            logger.warning("T6: Empty universe");
            return 0.0;
        }

        return 1.0 - ((double) supportSize / universeSize);
    }

    public double degreeOfQuantifierCardinality() {
        double sigmaCount = quantifier.fuzzySet().getSigmaCount();
        int universeSize = quantifier.fuzzySet().getUniverse().getSamples().size();

        if (universeSize == 0) {
            logger.warning("T7: Empty universe");
            return 0.0;
        }

        return  1.0 - (sigmaCount / universeSize);
    }

    public double degreeOfSummarizerCardinality() {
        if (summarizers.isEmpty()) {
            logger.warning("T8: No summarizers available");
            return 0.0;
        }

        double product = 1.0;
        for (Label summarizer : summarizers) {
            double sigmaCount = summarizer.getFuzzySet().getSigmaCount();
            double universeSize = summarizer.getFuzzySet().getUniverse().getLength();

            if (universeSize == 0) {
                logger.warning("T8: Empty universe for summarizer: " + summarizer.getName());
                return 0.0;
            }

            double ratio = sigmaCount / universeSize;
            product *= ratio;
        }

        double nthRoot = Math.pow(product, 1.0 / summarizers.size());
        return 1.0 - nthRoot;
    }

    public double degreeOfQualifierImprecision() {
        if (qualifier == null) {
            return 0.0;
        }

        double degreeOfFuzziness = qualifier.getFuzzySet().getDegreeOfFuzziness();
        return  1.0 - degreeOfFuzziness;
    }

    public double degreeOfQualifierCardinality() {
        if (qualifier == null) {
            return 0.0;
        }

        double sigmaCount = qualifier.getFuzzySet().getSigmaCount();
        int universeSize = qualifier.getFuzzySet().getUniverse().getSamples().size();

        if (universeSize == 0) {
            logger.warning("T10: Empty universe for qualifier");
            return 0.0;
        }

        return  1.0 - (sigmaCount / universeSize);
    }

    public double qualifierLength() {
        return 2 * 0.5;
    }

    public double optimalMeasure(List<Double> wages) {
        if (wages.isEmpty()) {
            wages = new ArrayList<>(List.of(0.7));
            wages.addAll(Collections.nCopies(10, 0.03));
            logger.info(wages.toString());
        }

        logger.info(measures.values().toString());

        return measures.get("T1") * wages.getFirst()
                + measures.get("T2") * wages.get(1)
                + measures.get("T3") * wages.get(2)
                + measures.get("T4") * wages.get(3)
                + measures.get("T5") * wages.get(4)
                + measures.get("T6") * wages.get(5)
                + measures.get("T7") * wages.get(6)
                + measures.get("T8") * wages.get(7)
                + measures.get("T9") * wages.get(8)
                + measures.get("T10") * wages.get(9)
                + measures.get("T11") * wages.get(10);
    }

    public void recalculateMeasures(List<Double> wages) {
        if (wages.size() != 11) {
            logger.warning("Wages must contain 11 values");
            return;
        }
        double optimal = optimalMeasure(wages);
        measures.put("T*", optimal);
    }
    
    // ============ UTILS ============

    private double calculateSummarizerMembership(Property property) {
        double membership = 1.0;

        for (Label summarizer : summarizers) {
            String attributeName = summarizer.getAttributeName();
            Function<Property, Double> extractor = attributeExtractors.get(attributeName);
            if (extractor == null) {
                logger.warning("No extractor found for attribute: " + attributeName);
                return 0.0;
            }

            Double value = extractor.apply(property);
            if (value == null) {
                return 0.0;
            }

            membership = Math.min(membership, summarizer.getFuzzySet().membership(value));
        }

        return membership;
    }

    private double calculateQualifierMembership(Property property) {
        if (qualifier == null) return 1.0;

        String attributeName = qualifier.getAttributeName();
        Function<Property, Double> extractor = attributeExtractors.get(attributeName);
        if (extractor != null) {
            Double value = extractor.apply(property);
            if (value != null) {
                return qualifier.getFuzzySet().membership(value);
            }
        }
        return 0.0;
    }

    // ===== GETTERY I TOSTRING =====

    @Override
    public String toString() {
        String qualifierValue = "";
        if (qualifier != null) {
            qualifierValue = " będących " + qualifier.getName();
        }

        StringBuilder summarizerValue = new StringBuilder(" jest ");
        summarizerValue.append(summarizers.get(0).getName());
        for (int i = 1; i < summarizers.size(); i++) {
            summarizerValue.append(" i ").append(summarizers.get(i).getName());
        }

        return quantifier.name()
                + " nieruchomości"
                + qualifierValue
                + summarizerValue;
    }

}