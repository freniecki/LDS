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
            String attributeName = summarizer.getAttributeName();
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

        return result;
    }

    public double degreeOfCovering() {
        if (properties == null || properties.isEmpty()) {
            logger.warning("T3: No data available");
            return 0.0;
        }

        if (qualifier == null) {
            // FORMA 1
            int supportCount = 0;
            for (Property property : properties) {
                if (calculateSummarizerMembership(property) > 0.0) {
                    supportCount++;
                }
            }
            double result = (double) supportCount / properties.size();
            return result;

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

            double result = (double) supportSAndW / supportW;
            return result;
        }
    }

    public double degreeOfAppropriateness() {
        if (properties == null || properties.isEmpty()) {
            logger.warning("T4: No data available");
            return 0.0;
        }

        double t3 = degreeOfCovering();
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

        double result = Math.abs(product - t3);
        return result;
    }

    public double summaryLength() {
        int summarizerCount = summarizers.size();
        return 2.0 * Math.pow(0.5, summarizerCount);
    }

    public double degreeOfQuantifierImprecision() {
        // T6 = 1 - |supp(Q)| / |XQ|
        int supportSize = quantifier.fuzzySet().getSupport().size();
        int universeSize = quantifier.fuzzySet().getUniverse().getSamples().size();

        if (universeSize == 0) {
            logger.warning("T6: Empty universe");
            return 0.0;
        }

        double result = 1.0 - ((double) supportSize / universeSize);
        logger.info("T6: " + String.format("%.3f", result) +
                " (support: " + supportSize + "/" + universeSize + ")");
        return result;
    }

    public double degreeOfQuantifierCardinality() {
        // T7 = 1 - |Q| / |XQ|
        double sigmaCount = quantifier.fuzzySet().getSigmaCount();
        int universeSize = quantifier.fuzzySet().getUniverse().getSamples().size();

        if (universeSize == 0) {
            logger.warning("T7: Empty universe");
            return 0.0;
        }

        double result = 1.0 - (sigmaCount / universeSize);
        logger.info("T7: " + String.format("%.3f", result) +
                " (sigma: " + String.format("%.3f", sigmaCount) + "/" + universeSize + ")");
        return result;
    }

    public double degreeOfSummarizerCardinality() {
        // T8 = 1 - (∏(j=1 to n) |Sj|/|Xj|)^(1/n)
        if (summarizers.isEmpty()) {
            logger.warning("T8: No summarizers available");
            return 0.0;
        }

        double product = 1.0;
        for (Label summarizer : summarizers) {
            double sigmaCount = summarizer.getFuzzySet().getSigmaCount();
            int universeSize = summarizer.getFuzzySet().getUniverse().getSamples().size();

            if (universeSize == 0) {
                logger.warning("T8: Empty universe for summarizer: " + summarizer.getName());
                return 0.0;
            }

            double ratio = sigmaCount / universeSize;
            product *= ratio;
        }

        double nthRoot = Math.pow(product, 1.0 / summarizers.size());
        double result = 1.0 - nthRoot;

        logger.info("T8: " + String.format("%.3f", result) + " (summarizers: " + summarizers.size() + ")");
        return result;
    }

    public double degreeOfQualifierImprecision() {
        // T9 = 1 - in(W)
        if (qualifier == null) {
            logger.info("T9: No qualifier (form 1) = 0.0");
            return 0.0;
        }

        double degreeOfFuzziness = qualifier.getFuzzySet().getDegreeOfFuzziness();
        double result = 1.0 - degreeOfFuzziness;

        logger.info("T9: " + String.format("%.3f", result) +
                " (qualifier: " + qualifier.getName() +
                ", fuzziness: " + String.format("%.3f", degreeOfFuzziness) + ")");
        return result;
    }

    public double degreeOfQualifierCardinality() {
        // T10 = 1 - |W|/|Xg|
        if (qualifier == null) {
            logger.info("T10: No qualifier (form 1) = 0.0");
            return 0.0;
        }

        double sigmaCount = qualifier.getFuzzySet().getSigmaCount();
        int universeSize = qualifier.getFuzzySet().getUniverse().getSamples().size();

        if (universeSize == 0) {
            logger.warning("T10: Empty universe for qualifier");
            return 0.0;
        }

        double result = 1.0 - (sigmaCount / universeSize);

        logger.info("T10: " + String.format("%.3f", result) +
                " (qualifier: " + qualifier.getName() +
                ", sigma: " + String.format("%.3f", sigmaCount) + "/" + universeSize + ")");
        return result;
    }

    public double qualifierLength() {
        // TODO: Implementacja T11
        return 0.0;
    }

    private double[] extractAttributeValues(Label summarizer) {
        String attributeName = summarizer.getAttributeName();
        Function<Property, Double> extractor = attributeExtractors.get(attributeName);

        return properties.stream()
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