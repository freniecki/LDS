package pl.frot.fuzzy.summaries;

import lombok.Getter;
import pl.frot.data.Property;
import pl.frot.model.enums.PropertyType;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public class MultisubjectSummary {
    private static final Logger logger = Logger.getLogger(MultisubjectSummary.class.getName());

    @Getter
    private final Quantifier quantifier;
    @Getter
    private final Label qualifier;
    @Getter
    private final List<Label> summarizers;

    private final MultiSubjectForm multiSubjectForm;

    // Two populations to compare
    private final List<Property> population1;  // P₁
    private final List<Property> population2;  // P₂
    private final PropertyType populationType1;
    private final PropertyType populationType2;

    private final Map<String, Function<Property, Double>> attributeExtractors;

    public MultisubjectSummary(Quantifier quantifier,
                               Label qualifier,
                               List<Label> summarizers,
                               MultiSubjectForm multiSubjectForm,
                               PropertyType populationType1,
                               PropertyType populationType2,
                               Map<PropertyType, List<Property>> propertiesByType,
                               Map<String, Function<Property, Double>> attributeExtractors) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;

        if (summarizers.isEmpty()) {
            logger.warning("Summarizers must contain at least 1 summarizer");
            throw new IllegalArgumentException("Summarizers must contain at least 1 summarizer");
        }
        this.summarizers = summarizers;

        this.multiSubjectForm = multiSubjectForm;

        this.populationType1 = populationType1;
        this.populationType2 = populationType2;
        this.population1 = propertiesByType.get(populationType1);
        this.population2 = propertiesByType.get(populationType2);
        this.attributeExtractors = attributeExtractors;
    }

    /**
     * Calculates degree of truth for multisubject summary (Forms 1-3)
     * Automatically determines form based on qualifier presence and application
     */
    public double degreeOfTruth() {
        if (population1 == null || population2 == null) {
            logger.warning("Population data not found for multisubject summary!");
            return 0.0;
        }

        if (population1.isEmpty() || population2.isEmpty()) {
            logger.warning("Both populations must be non-empty for multisubject summary!");
            return 0.0;
        }

        return switch (multiSubjectForm) {
            case FIRST -> calculateForm1();
            case SECOND -> calculateForm2();
            case THIRD -> calculateForm3();
            case FOURTH -> calculateForm4();
        };
    }

    /**
     * FORM 1: T(Q P₁ w odniesieniu do P₂ jest S₁)
     */
    public double calculateForm1() {
        double sigmaCountP1 = 0.0;
        for (Property property : population1) {
            sigmaCountP1 += calculateSummarizerMembership(property);
        }

        double sigmaCountP2 = 0.0;
        for (Property property : population2) {
            sigmaCountP2 += calculateSummarizerMembership(property);
        }

        int mP1 = population1.size();
        int mP2 = population2.size();

        double numerator = (1.0 / mP1) * sigmaCountP1;
        double denominator = (1.0 / mP1) * sigmaCountP1 + (1.0 / mP2) * sigmaCountP2;

        if (denominator == 0.0) return 0.0;

        double ratio = numerator / denominator;
        return quantifier.fuzzySet().membership(ratio);
    }

    /**
     * FORM 2: T(Q P₁ w odniesieniu do P₂ będących S₂ jest S₁)
     * Qualifier applies to P₂
     */
    public double calculateForm2() {
        // Σ-count(S₁_P₁ ∩ S₂_P₁) for P₁
        double sigmaCountS1AndS2P1 = 0.0;
        for (Property property : population1) {
            double summarizerMembership = calculateSummarizerMembership(property);
            double qualifierMembership = calculateQualifierMembership(property);
            sigmaCountS1AndS2P1 += Math.min(summarizerMembership, qualifierMembership);
        }

        // Σ-count(S₂_P₁) for P₁
        double sigmaCountS2P1 = 0.0;
        for (Property property : population1) {
            sigmaCountS2P1 += calculateQualifierMembership(property);
        }

        // Σ-count(S₁_P₂) for P₂
        double sigmaCountS1P2 = 0.0;
        for (Property property : population2) {
            sigmaCountS1P2 += calculateSummarizerMembership(property);
        }

        int mP1 = population1.size();
        int mP2 = population2.size();

        double numerator = (1.0 / mP1) * sigmaCountS1AndS2P1;
        double denominator = (1.0 / mP1) * sigmaCountS2P1 + (1.0 / mP2) * sigmaCountS1P2;

        if (denominator == 0.0) return 0.0;

        double ratio = numerator / denominator;
        return quantifier.fuzzySet().membership(ratio);
    }

    /**
     * FORM 3: T(Q P₁ będących S₂ w odniesieniu do P₂ jest S₁)
     * Qualifier applies to P₁
     */
    public double calculateForm3() {
        // Σ-count(S₁_P₁ ∩ S₂_P₁) for P₁
        double sigmaCountS1AndS2P1 = 0.0;
        for (Property property : population1) {
            double summarizerMembership = calculateSummarizerMembership(property);
            double qualifierMembership = calculateQualifierMembership(property);
            sigmaCountS1AndS2P1 += Math.min(summarizerMembership, qualifierMembership);
        }

        // Σ-count(S₁_P₁) for P₁
        double sigmaCountS1P1 = 0.0;
        for (Property property : population1) {
            sigmaCountS1P1 += calculateSummarizerMembership(property);
        }

        // Σ-count(S₁_P₂) for P₂
        double sigmaCountS1P2 = 0.0;
        for (Property property : population2) {
            sigmaCountS1P2 += calculateSummarizerMembership(property);
        }

        int mP1 = population1.size();
        int mP2 = population2.size();

        double numerator = (1.0 / mP1) * sigmaCountS1AndS2P1;
        double denominator = (1.0 / mP1) * sigmaCountS1P1 + (1.0 / mP2) * sigmaCountS1P2;

        if (denominator == 0.0) return 0.0;

        double ratio = numerator / denominator;
        return quantifier.fuzzySet().membership(ratio);
    }

    /**
     * FORM 4: MLS4: Więcej P₁ niż P₂ jest S₁
     * Formula: T() = 1 - m(Inc(S(P₂), S(P₁)))
     */
    public double calculateForm4() {
        if (population1 == null || population2 == null) {
            logger.warning("Population data not found for form 4 calculation!");
            return 0.0;
        }

        if (population1.isEmpty() || population2.isEmpty()) {
            logger.warning("Both populations must be non-empty for form 4 calculation!");
            return 0.0;
        }

        // Get summarizer memberships for both populations
        List<Double> membershipsP1 = new ArrayList<>();
        for (Property property : population1) {
            membershipsP1.add(calculateSummarizerMembership(property));
        }

        List<Double> membershipsP2 = new ArrayList<>();
        for (Property property : population2) {
            membershipsP2.add(calculateSummarizerMembership(property));
        }

        // Calculate inclusion measure Inc(S(P₂), S(P₁))
        double inclusion = calculateInclusionMeasure(membershipsP2, membershipsP1);

        // Return 1 - m(inclusion)
        return 1.0 - inclusion;
    }

    /**
     * Calculate inclusion measure Inc(A, B) = |A ∩ B| / |A|
     * For fuzzy sets: Σ min(μA(x), μB(x)) / Σ μA(x)
     */
    private double calculateInclusionMeasure(List<Double> membershipsA, List<Double> membershipsB) {
        if (membershipsA.isEmpty() || membershipsB.isEmpty()) {
            return 0.0;
        }

        double intersectionSum = 0.0;
        double sumA = 0.0;

        // Use minimum of the two sizes for comparison
        int minSize = Math.min(membershipsA.size(), membershipsB.size());

        for (int i = 0; i < minSize; i++) {
            double membershipA = membershipsA.get(i);
            double membershipB = membershipsB.get(i);

            intersectionSum += Math.min(membershipA, membershipB);
            sumA += membershipA;
        }

        // Add remaining elements from A if A is larger
        for (int i = minSize; i < membershipsA.size(); i++) {
            sumA += membershipsA.get(i);
        }

        if (sumA == 0.0) {
            return 0.0;
        }

        return intersectionSum / sumA;
    }

    /**
     * Calculate membership degree for summarizers
     */
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

    /**
     * Calculate membership degree for qualifier
     */
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

    // ===== GETTERS AND TOSTRING =====

    @Override
    public String toString() {
        StringBuilder summarizerValue = new StringBuilder(" jest ");
        summarizerValue.append(summarizers.getFirst().getName());
        for (int i = 1; i < summarizers.size(); i++) {
            summarizerValue.append(" i ").append(summarizers.get(i).getName());
        }

        return switch (multiSubjectForm) {
            case FIRST -> quantifier.name()
                    + " nieruchomości w " + populationType1.propertyTypeName
                    + " w odniesieniu do " + populationType2.propertyTypeName
                    + summarizerValue;
            case SECOND -> quantifier.name()
                    + " nieruchomości w " + populationType1.propertyTypeName
                    + " w odniesieniu do " + populationType2.propertyTypeName
                    + " będących " + qualifier.getName()
                    + summarizerValue;
            case THIRD -> quantifier.name()
                    + " nieruchomości w " + populationType1.propertyTypeName
                    + " będących " + qualifier.getName()
                    + " w odniesieniu do " + populationType2.propertyTypeName
                    + summarizerValue;
            case FOURTH -> "Więcej nieruchomości w " + populationType1.propertyTypeName
                    + " niż w " + populationType2.propertyTypeName
                    + summarizerValue;
        };
    }
}