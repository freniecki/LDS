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

    // NOWE: Dane i mapping atrybutów
    private List<Property> data;
    private Map<String, Function<Property, Double>> attributeExtractors;

    public SingleSubjectSummary(Quantifier quantifier, Label qualifier, List<Label> summarizers) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;
        this.summarizers = summarizers;
    }

    public void setData(List<Property> data, Map<String, Function<Property, Double>> attributeExtractors) {
        this.data = data;
        this.attributeExtractors = attributeExtractors;
    }

    public double degreeOfTruth() {
        if (data == null || attributeExtractors == null) {
            logger.warning("No data set for summary calculation!");
            return 0.0;
        }

        return calculateDegreeOfTruth();
    }

    // ===== POPRAWIONE OBLICZANIE PRAWDZIWOŚCI =====
    private double calculateDegreeOfTruth() {
        logger.info("🔍 Processing summary with " + summarizers.size() + " summarizers: " +
                summarizers.stream().map(s -> s.getName() + "(" + getAttributeNameFromLabel(s) + ")").toList());

        if (data.isEmpty()) {
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
        // T(1. forma) = μQ(Σ-count(S₁) / M)
        // gdzie M = |X| dla OBUDWU typów (różnica w interpretacji kwantyfikatora)

        double sigmaCountS1 = 0.0;  // Σ-count(S₁) - suma przynależności do sumaryzatora

        for (Property property : data) {
            double summarizerMembership = calculateSummarizerMembership(property);
            sigmaCountS1 += summarizerMembership;
        }

        double normalizedValue;

        if (quantifier.type() == QuantifierType.RELATIVE) {
            // Dla RELATIVE: proporcja ∈ [0,1]
            normalizedValue = sigmaCountS1 / data.size();
        } else {
            // Dla ABSOLUTE: surowa liczba
            normalizedValue = sigmaCountS1;
        }

        double result = quantifier.fuzzySet().membership(normalizedValue);

        logger.info("FORMA PIERWSZA - Summary: " + this.toString());
        logger.info("Σ-count(S₁): %.2f / data.size(): %d".formatted(sigmaCountS1, data.size()));
        logger.info("normalizedValue: %.2f".formatted(normalizedValue));
        logger.info("T1: %.2f".formatted(result));

        return result;
    }

    private double calculateSecondForm() {
        // T(2. forma) = μQ(Σ-count(S₁ ∩ S₂) / Σ-count(S₂))
        // gdzie S₁ = sumaryzator, S₂ = kwalifikator
        // POPRAWNY WZÓR: stosunek obiektów spełniających ZARÓWNO S i W do obiektów spełniających W

        double sigmaCountS2 = 0.0;           // Σ-count(S₂) - suma przynależności do kwalifikatora
        double sigmaCountS1andS2 = 0.0;     // Σ-count(S₁ ∩ S₂) - suma przynależności do przecięcia

        for (Property property : data) {
            double summarizerMembership = calculateSummarizerMembership(property);  // μS₁(x)
            double qualifierMembership = calculateQualifierMembership(property);    // μS₂(x)

            sigmaCountS2 += qualifierMembership;

            // T-norma (minimum) dla przecięcia S₁ ∩ S₂
            double intersectionMembership = Math.min(summarizerMembership, qualifierMembership);
            sigmaCountS1andS2 += intersectionMembership;
        }

        if (sigmaCountS2 == 0.0) {
            return 0.0; // Unikamy dzielenia przez zero
        }

        double normalizedValue = sigmaCountS1andS2 / sigmaCountS2; // ZAWSZE ∈ [0,1] ✅
        double result = quantifier.fuzzySet().membership(normalizedValue);

        logger.info("FORMA DRUGA - Summary: " + this.toString());
        logger.info("Σ-count(S₁ ∩ S₂): %.2f / Σ-count(S₂): %.2f".formatted(sigmaCountS1andS2, sigmaCountS2));
        logger.info("normalizedValue: %.2f".formatted(normalizedValue));
        logger.info("T1: %.2f".formatted(result));

        return result;
    }

    // ===== METODY POMOCNICZE DLA T1 =====
    // oblicza przeciecie wszytkich sumaryzatorow
    private double calculateSummarizerMembership(Property property) {
        double membership = 1.0; // T-norma (AND)

        for (Label summarizer : summarizers) {
            String attributeName = getAttributeNameFromLabel(summarizer);
            Function<Property, Double> extractor = attributeExtractors.get(attributeName);
            if (extractor != null) {
                Double value = extractor.apply(property);
                if (value != null) {
                    membership = Math.min(membership, summarizer.getFuzzySet().membership(value));
                } else {
                    return 0.0; // Null value = no membership
                }
            } else {
                logger.warning("No extractor found for attribute: " + attributeName);
                return 0.0;
            }
        }

        return membership;
    }

    private double calculateQualifierMembership(Property property) {
        if (qualifier == null) return 1.0;

        String attributeName = getAttributeNameFromLabel(qualifier);
        Function<Property, Double> extractor = attributeExtractors.get(attributeName);
        if (extractor != null) {
            Double value = extractor.apply(property);
            if (value != null) {
                return qualifier.getFuzzySet().membership(value);
            }
        }
        return 0.0;
    }

    private String getAttributeNameFromLabel(Label label) {
        return label.getAttributeName();
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