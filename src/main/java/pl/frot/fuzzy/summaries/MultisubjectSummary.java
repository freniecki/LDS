package pl.frot.fuzzy.summaries;

import lombok.Getter;
import pl.frot.data.Property;
import pl.frot.fuzzy.base.DiscreteUniverse;
import pl.frot.fuzzy.base.FuzzySet;
import pl.frot.fuzzy.base.MembershipFunction;
import pl.frot.fuzzy.base.Universe;
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
    /**
     * FORM 1: T(Q P₁ w odniesieniu do P₂ jest S₁)
     * Wzór (6.8) - używa nfo-count, nie sigma-count
     */
    public double calculateForm1() {
        // nfo-count(S̃(P₁)) - liczba elementów z pełną przynależnością
        double nfoCountP1 = 0.0;
        for (Property property : population1) {
            if (calculateSummarizerMembership(property) == 1.0) {
                nfoCountP1 += 1.0;
            }
        }

        // nfo-count(S̃(P₂)) - liczba elementów z pełną przynależnością
        double nfoCountP2 = 0.0;
        for (Property property : population2) {
            if (calculateSummarizerMembership(property) == 1.0) {
                nfoCountP2 += 1.0;
            }
        }

        int mP1 = population1.size();
        int mP2 = population2.size();

        double numerator = (1.0 / mP1) * nfoCountP1;
        double denominator = (1.0 / mP1) * nfoCountP1 + (1.0 / mP2) * nfoCountP2;

        if (denominator == 0.0) {
            // Jeśli brak elementów spełniających warunek w obu zbiorach
            // dla kwantyfikatora "mało" powinno zwrócić wysoką wartość
            return quantifier.fuzzySet().membership(0.0);
        }

        double ratio = numerator / denominator;
        return quantifier.fuzzySet().membership(ratio);
    }

    /**
     * FORM 2: T(Q P₁ w odniesieniu do P₂ będących S₂ jest S₁)
     * Wzór (6.13) - kwalifikator W̃ odnosi się do P₂
     */
    public double calculateForm2() {
        // nfo-count(S̃(P₁) ∩ W̃) - licznik
        double nfoCountS1AndWP1 = 0.0;
        for (Property property : population1) {
            double summarizerMembership = calculateSummarizerMembership(property);
            double qualifierMembership = calculateQualifierMembership(property);

            if (summarizerMembership == 1.0 && qualifierMembership == 1.0) {
                nfoCountS1AndWP1 += 1.0;
            }
        }

        // nfo-count(S̃(P₁)) - pierwszy składnik mianownika
        double nfoCountS1P1 = 0.0;
        for (Property property : population1) {
            if (calculateSummarizerMembership(property) == 1.0) {
                nfoCountS1P1 += 1.0;
            }
        }

        // nfo-count(S̃(P₂) ∩ W̃) - drugi składnik mianownika
        // Dla P₂ sprawdzamy czy property należy do P₂ I ma kwalifikator W̃
        double nfoCountS1AndWP2 = 0.0;
        for (Property property : population2) {
            double summarizerMembership = calculateSummarizerMembership(property);
            // W Formie 2 kwalifikator odnosi się do P₂, więc sprawdzamy go dla P₂
            double qualifierMembership = calculateQualifierMembership(property);

            if (summarizerMembership == 1.0 && qualifierMembership == 1.0) {
                nfoCountS1AndWP2 += 1.0;
            }
        }

        int mP1 = population1.size();
        int mP2 = population2.size();

        double numerator = (1.0 / mP1) * nfoCountS1AndWP1;
        double denominator = (1.0 / mP1) * nfoCountS1P1 + (1.0 / mP2) * nfoCountS1AndWP2;

        if (denominator == 0.0) return 0.0;

        double ratio = numerator / denominator;
        return quantifier.fuzzySet().membership(ratio);
    }

    /**
     * FORM 3: T(Q P₁ będących S₂ w odniesieniu do P₂ jest S₁)
     * Wzór (6.17) - kwalifikator W̃ odnosi się do P₁
     */
    public double calculateForm3() {
        // nfo-count(S̃(P₁) ∩ W̃) - licznik (identyczny jak w Form 2)
        double nfoCountS1AndWP1 = 0.0;
        for (Property property : population1) {
            double summarizerMembership = calculateSummarizerMembership(property);
            double qualifierMembership = calculateQualifierMembership(property);

            if (summarizerMembership == 1.0 && qualifierMembership == 1.0) {
                nfoCountS1AndWP1 += 1.0;
            }
        }

        // POPRAWKA: pierwszy składnik mianownika to nfo-count(S̃(P₁) ∩ W̃), nie nfo-count(S̃(P₁))
        // Zgodnie ze wzorem (6.17)
        double nfoCountS1AndWP1_denominator = nfoCountS1AndWP1; // ten sam co licznik

        // nfo-count(S̃(P₂)) - drugi składnik mianownika
        double nfoCountS1P2 = 0.0;
        for (Property property : population2) {
            if (calculateSummarizerMembership(property) == 1.0) {
                nfoCountS1P2 += 1.0;
            }
        }

        int mP1 = population1.size();
        int mP2 = population2.size();

        double numerator = (1.0 / mP1) * nfoCountS1AndWP1;
        // POPRAWKA: używamy nfoCountS1AndWP1, nie nfoCountS1P1
        double denominator = (1.0 / mP1) * nfoCountS1AndWP1_denominator + (1.0 / mP2) * nfoCountS1P2;

        if (denominator == 0.0) return 0.0;

        double ratio = numerator / denominator;
        return quantifier.fuzzySet().membership(ratio);
    }

    /**
     * FORM 4: T(Więcej P₁ niż P₂ jest S̃) = 1 - m(Inc(S̃(P₁), S̃(P₂)))
     * Kompletna implementacja wzoru (6.20)
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

        // KROK 1: Utwórz S̃(P₁) - zbiór rozmyty dla population1
        FuzzySet<String> sP1 = createSummarizerFuzzySet("P1");

        // KROK 2: Utwórz S̃(P₂) - zbiór rozmyty dla population2
        FuzzySet<String> sP2 = createSummarizerFuzzySet("P2");

        // KROK 3: Oblicz Inc(S̃(P₁), S̃(P₂)) - zbiór rozmyty inkluzji
        FuzzySet<String> incSet = calculateInclusionFuzzySet(sP1, sP2);

        // KROK 4: Zastosuj m() - fuzzy measure (degree of fuzziness)
        double m = incSet.getDegreeOfFuzziness();

        // KROK 5: Return 1 - m(Inc(S̃(P₁), S̃(P₂)))
        return 1.0 - m;
    }

    /**
     * Utwórz zbiór rozmyty sumaryzatora S̃(P) dla danej populacji
     */
    private FuzzySet<String> createSummarizerFuzzySet(String populationName) {
        // Wybierz populację
        List<Property> population = populationName.equals("P1") ? population1 : population2;

        // Uniwersum: ID wszystkich nieruchomości w populacji
        List<String> universe = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            universe.add(populationName + "_property_" + i);
        }

        Universe<String> summarizerUniverse = new DiscreteUniverse<>(universe);

        // Funkcja przynależności: oblicz membership każdej nieruchomości do sumaryzatorów
        MembershipFunction<String> membershipFunction = propertyId -> {
            // Wyciągnij indeks z ID (np. "P1_property_3" -> 3)
            String[] parts = propertyId.split("_");
            if (parts.length != 3) return 0.0;

            try {
                int index = Integer.parseInt(parts[2]);
                if (index >= 0 && index < population.size()) {
                    Property property = population.get(index);
                    return calculateSummarizerMembership(property);
                }
            } catch (NumberFormatException e) {
                logger.warning("Invalid property ID: " + propertyId);
            }
            return 0.0;
        };

        return new FuzzySet<>(summarizerUniverse, membershipFunction);
    }

    /**
     * Oblicz Inc(S̃(P₁), S̃(P₂)) jako zbiór rozmyty inkluzji
     * Używa implikatora Łukasiewicza: I_Ł(a,b) = min(1, 1-a+b)
     */
    private FuzzySet<String> calculateInclusionFuzzySet(FuzzySet<String> sP1, FuzzySet<String> sP2) {
        // Uniwersum inkluzji = unia elementów z obu zbiorów
        Set<String> allElements = new HashSet<>();
        allElements.addAll(sP1.getUniverse().getSamples());
        allElements.addAll(sP2.getUniverse().getSamples());

        List<String> inclusionUniverse = new ArrayList<>(allElements);
        Universe<String> incUniverse = new DiscreteUniverse<>(inclusionUniverse);

        // Funkcja przynależności inkluzji: Inc(A,B)(x) = I_Ł(μA(x), μB(x))
        MembershipFunction<String> inclusionFunction = element -> {
            double membershipP1 = sP1.membership(element);
            double membershipP2 = sP2.membership(element);

            // Implicator Łukasiewicza: I_Ł(a,b) = min(1, 1-a+b)
            return Math.min(1.0, 1.0 - membershipP1 + membershipP2);
        };

        return new FuzzySet<>(incUniverse, inclusionFunction);
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
            return "Więcej nieruchomości w " + populationType1.propertyTypeName
                    + " niż w " + populationType2.propertyTypeName
                    + summarizerValue;
        } else if (qualifier == null) {
            // Form 1
            return quantifier.name()
                    + " nieruchomości w " + populationType1.propertyTypeName
                    + " w odniesieniu do " + populationType2.propertyTypeName
                    + summarizerValue;
        } else if (qualifierAppliesTo1) {
            // Form 3
            return quantifier.name()
                    + " nieruchomości w " + populationType1.propertyTypeName
                    + " będących " + qualifier.getName()
                    + " w odniesieniu do " + populationType2.propertyTypeName
                    + summarizerValue;
        } else {
            // Form 2
            return quantifier.name()
                    + " nieruchomości w " + populationType1.propertyTypeName
                    + " w odniesieniu do " + populationType2.propertyTypeName
                    + " będących " + qualifier.getName()
                    + summarizerValue;
        }
    }
}