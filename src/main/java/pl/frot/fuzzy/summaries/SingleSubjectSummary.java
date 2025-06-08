package pl.frot.fuzzy.summaries;

import lombok.Getter;
import pl.frot.data.Property;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class SingleSubjectSummary {
    private static final Logger logger = Logger.getLogger(SingleSubjectSummary.class.getName());

    @Getter
    private final Quantifier quantifier;
    @Getter
    private final Label qualifier;
    @Getter
    private final List<Label> summarizers;

    private final List<Property> properties;
    private final Map<String, Function<Property, Double>> attributeExtractors;

    public SingleSubjectSummary(Quantifier quantifier, Label qualifier, List<Label> summarizers,
                                List<Property> properties,
                                Map<String, Function<Property, Double>> attributeExtractors) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;
        this.summarizers = summarizers;
        this.properties = properties;
        this.attributeExtractors = attributeExtractors;
    }

    public double degreeOfTruth() {
        if (properties.isEmpty()) {
            logger.warning("No data set for summary calculation!");
            return 0.0;
        }

        if (qualifier == null) {
            // ===== FORMA PIERWSZA: Q P are/have S =====
            return calculateFirstForm();
        } else {
            // ===== FORMA DRUGA: Q P being/having W are/have S =====
            return calculateSecondForm();
        }
    }

    private double calculateFirstForm() {
        double sigmaCountS = 0.0;

        for (Property property : properties) {
            double summarizerMembership = calculateSummarizerMembership(property);
            sigmaCountS += summarizerMembership;
        }

        double normalizedValue = switch (quantifier.type()) {
            case ABSOLUTE -> sigmaCountS;
            case RELATIVE -> sigmaCountS / properties.size();
        };

        return quantifier.fuzzySet().membership(normalizedValue);
    }

    private double calculateSecondForm() {
        double sigmaCountW = 0.0;
        double sigmaCountSandW = 0.0;

        for (Property property : properties) {
            double summarizerMembership = calculateSummarizerMembership(property);
            double qualifierMembership = calculateQualifierMembership(property);

            sigmaCountW += qualifierMembership;

            double intersectionMembership = Math.min(summarizerMembership, qualifierMembership);
            sigmaCountSandW += intersectionMembership;
        }

        if (sigmaCountW == 0.0) {
            return 0.0;
        }

        return quantifier.fuzzySet().membership(sigmaCountSandW / sigmaCountW);
    }

    // ===== METODY POMOCNICZE DLA T1 =====
    private double calculateSummarizerMembership(Property property) {
        double membership = 1.0; // T-norma (AND)

        for (Label summarizer : summarizers) {
            String attributeName = summarizer.attributeName;
            Function<Property, Double> extractor = attributeExtractors.get(attributeName);
            if (extractor == null) {
                logger.warning("No extractor found for attribute: " + attributeName);
                return 0.0;
            } else {
                Double value = extractor.apply(property);
                if (value != null) {
                    membership = Math.min(membership, summarizer.getFuzzySet().membership(value));
                } else {
                    return 0.0; // Null value = no membership
                }
            }
        }

        return membership;
    }

    private double calculateQualifierMembership(Property property) {
        if (qualifier == null) return 1.0;

        String attributeName = qualifier.attributeName;
        Function<Property, Double> extractor = attributeExtractors.get(attributeName);
        if (extractor != null) {
            Double value = extractor.apply(property);
            if (value != null) {
                return qualifier.getFuzzySet().membership(value);
            }
        }
        return 0.0;
    }

    // ===== MIARY JAKOŚCI - POZOSTAWIONE NA PRZYSZŁOŚĆ =====

    public double degreeOfImprecision() {
        if (summarizers.isEmpty()) {
            logger.warning("T2: No summarizers available");
            return 0.0;
        }

        double product = 1.0;
        for (Label summarizer : summarizers) {
            // POPRAWKA: Użyj nowej metody z FuzzySet
            double degreeOfFuzziness = summarizer.getFuzzySet().getDegreeOfFuzziness();
            product *= degreeOfFuzziness;

            // Debug log
            logger.fine("Summarizer '" + summarizer.getName() + "' degree of fuzziness: " +
                    String.format("%.3f", degreeOfFuzziness));
        }

        double nthRoot = Math.pow(product, 1.0 / summarizers.size());
        double result = 1.0 - nthRoot;

        logger.info("T2: " + String.format("%.3f", result) + " (summarizers: " + summarizers.size() + ")");
        return result;
    }
    public double degreeOfCovering() {
        if (data == null || data.isEmpty()) {
            logger.warning("T3: No data available");
            return 0.0;
        }

        if (qualifier == null) {
            // FORMA 1
            int supportCount = 0;
            for (Property property : data) {
                if (calculateSummarizerMembership(property) > 0.0) {
                    supportCount++;
                }
            }
            double result = (double) supportCount / data.size();
            logger.info("T3 (Form 1): " + supportCount + "/" + data.size() + " = " + String.format("%.3f", result));
            return result;

        } else {
            // FORMA 2
            int supportW = 0;
            int supportSAndW = 0;

            for (Property property : data) {
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

            double result = (double) supportSAndW / supportW;
            logger.info("T3 (Form 2): " + supportSAndW + "/" + supportW + " = " + String.format("%.3f", result));
            return result;
        }
    }

    public double degreeOfAppropriateness() {
        if (data == null || data.isEmpty()) {
            logger.warning("T4: No data available");
            return 0.0;
        }

        double t3 = degreeOfCovering();
        double product = 1.0;

        for (Label summarizer : summarizers) {
            String attributeName = getAttributeNameFromLabel(summarizer);
            Function<Property, Double> extractor = attributeExtractors.get(attributeName);

            if (extractor == null) {
                logger.warning("T4: No extractor for attribute: " + attributeName);
                return 0.0;
            }

            int countSatisfying = 0;
            for (Property property : data) {
                Double value = extractor.apply(property);
                if (value != null && summarizer.getFuzzySet().membership(value) > 0.0) {
                    countSatisfying++;
                }
            }

            double rj = (double) countSatisfying / data.size();
            product *= rj;
        }

        double result = Math.abs(product - t3);
        logger.info("T4: " + String.format("%.3f", result) + " (product: " + String.format("%.3f", product) + ", T3: " + String.format("%.3f", t3) + ")");
        return result;
    }

    public double summaryLength() {
        int summarizerCount = summarizers.size();
        return 2.0 * Math.pow(0.5, summarizerCount);
    }

    public double degreeOfQuantifierImprecision() {
        // TODO: Implementacja T6
        return 0.0;
    }

    public double degreeOfQuantifierCardinality() {
        // TODO: Implementacja T7
        return 0.0;
    }

    public double degreeOfSummarizerCardinality() {
        // TODO: Implementacja T8
        return 0.0;
    }

    public double degreeOfQualifierImprecision() {
        // TODO: Implementacja T9
        return 0.0;
    }

    public double degreeOfQualifierCardinality() {
        // TODO: Implementacja T10
        return 0.0;
    }

    public double qualifierLength() {
        // TODO: Implementacja T11
        return 0.0;
    }

    private double[] extractAttributeValues(Label summarizer) {
        String attributeName = getAttributeNameFromLabel(summarizer);
        Function<Property, Double> extractor = attributeExtractors.get(attributeName);

        return data.stream()
                .mapToDouble(property -> {
                    Double value = extractor.apply(property);
                    return value != null ? value : 0.0;
                })
                .toArray();
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