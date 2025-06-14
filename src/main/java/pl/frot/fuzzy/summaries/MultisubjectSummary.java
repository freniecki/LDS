package pl.frot.fuzzy.summaries;

import lombok.Getter;
import pl.frot.data.Property;
import pl.frot.model.PropertyType;

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
    @Getter
    private final boolean qualifierAppliesTo1; // true = P₁, false = P₂

    // Two populations to compare
    private final List<Property> population1;  // P₁
    private final List<Property> population2;  // P₂
    private final PropertyType populationType1;
    private final PropertyType populationType2;

    private final Map<String, Function<Property, Double>> attributeExtractors;

    public MultisubjectSummary(Quantifier quantifier,
                               Label qualifier,
                               List<Label> summarizers,
                               PropertyType populationType1,
                               PropertyType populationType2,
                               Map<PropertyType, List<Property>> propertiesByType,
                               Map<String, Function<Property, Double>> attributeExtractors,
                               boolean qualifierAppliesTo1) {
        this.quantifier = quantifier;
        this.qualifier = qualifier;

        if (summarizers.isEmpty()) {
            logger.warning("Summarizers must contain at least 1 summarizer");
            throw new IllegalArgumentException("Summarizers must contain at least 1 summarizer");
        }
        this.summarizers = summarizers;

        this.populationType1 = populationType1;
        this.populationType2 = populationType2;
        this.population1 = propertiesByType.get(populationType1);
        this.population2 = propertiesByType.get(populationType2);
        this.attributeExtractors = attributeExtractors;
        this.qualifierAppliesTo1 = qualifierAppliesTo1;
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
    /**
     * Calculate inclusion measure Inc(S(P₂), S(P₁)) correctly
     * Compares average membership patterns between populations
     */
    private double calculateInclusionMeasure(List<Double> membershipsP2, List<Double> membershipsP1) {
        if (membershipsP2.isEmpty() || membershipsP1.isEmpty()) {
            return 0.0;
        }

        // Calculate average membership degrees for both populations
        double avgP1 = membershipsP1.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avgP2 = membershipsP2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Simple inclusion: how much P₂'s average is contained in P₁'s average
        if (avgP1 == 0.0) return 0.0;

        return Math.min(avgP2 / avgP1, 1.0);
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


    /**
     * Get degree of truth for specific form (1-4)
     */
    public int getFormNumber() {
        if (quantifier == null) {
            return 4;  // Form 4: no quantifier
        } else if (qualifier == null) {
            return 1;  // Form 1: quantifier but no qualifier
        } else if (qualifierAppliesTo1) {
            return 3;  // Form 3: qualifier applies to P₁
        } else {
            return 2;  // Form 2: qualifier applies to P₂
        }
    }

    public double calculateFormByNumber(int formNumber) {
        return switch(formNumber) {
            case 1 -> calculateForm1();
            case 2 -> calculateForm2();
            case 3 -> calculateForm3();
            case 4 -> calculateForm4();
            default -> 0.0;
        };
    }
    // ===== GETTERS AND TOSTRING =====

    @Override
    public String toString() {
        StringBuilder summarizerValue = new StringBuilder(" jest ");
        summarizerValue.append(summarizers.get(0).getName());
        for (int i = 1; i < summarizers.size(); i++) {
            summarizerValue.append(" i ").append(summarizers.get(i).getName());
        }

        if (quantifier == null) {
            // Form 4: "Więcej P₁ niż P₂ jest S₁"
            return "Więcej nieruchomości w " + populationType1.toString().toLowerCase()
                    + " niż w " + populationType2.toString().toLowerCase()
                    + summarizerValue;
        } else if (qualifier == null) {
            // Form 1
            return quantifier.name()
                    + " nieruchomości w " + populationType1.toString().toLowerCase()
                    + " w odniesieniu do " + populationType2.toString().toLowerCase()
                    + summarizerValue;
        } else if (qualifierAppliesTo1) {
            // Form 3
            return quantifier.name()
                    + " nieruchomości w " + populationType1.toString().toLowerCase()
                    + " będących " + qualifier.getName()
                    + " w odniesieniu do " + populationType2.toString().toLowerCase()
                    + summarizerValue;
        } else {
            // Form 2
            return quantifier.name()
                    + " nieruchomości w " + populationType1.toString().toLowerCase()
                    + " w odniesieniu do " + populationType2.toString().toLowerCase()
                    + " będących " + qualifier.getName()
                    + summarizerValue;
        }
    }
}