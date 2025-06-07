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

    // GŁÓWNA LOGIKA: Obliczanie prawdziwości
    private double calculateDegreeOfTruth() {

        logger.info("🔍 Processing summary with " + summarizers.size() + " summarizers: " +
                summarizers.stream().map(s -> s.getName() + "(" + getAttributeNameFromLabel(s) + ")").toList());

        if (data.isEmpty()) {
            return 0.0;
        }

        double sigmaCount = 0.0;
        int validObjects = 0;

        // ✅ POPRAWNY ALGORYTM: Dla każdego obiektu z bazy danych
        for (Property property : data) {
            // Sprawdź kwalifikator (jeśli istnieje)
            boolean qualifierMatches = true;
            // ✅ POPRAWNY KOD:
            if (qualifier != null) {
                // Pobierz nazwę atrybutu z qualifier, NIE z summarizers!
                String qualifierAttributeName = getAttributeNameFromLabel(qualifier);
                Function<Property, Double> qualifierExtractor = attributeExtractors.get(qualifierAttributeName);

                if (qualifierExtractor != null) {
                    Double qualifierValue = qualifierExtractor.apply(property);
                    if (qualifierValue != null) {
                        double qualifierMembership = qualifier.getFuzzySet().membership(qualifierValue);
                        qualifierMatches = qualifierMembership > 0.0; // Próg można konfigurować
                    }
                }
            }

            if (qualifierMatches) {
                validObjects++;

                // Oblicz stopień przynależności do każdego summarizera
                double combinedMembership = 1.0; // Start z 1.0 dla intersection (T-norma minimum)

                for (Label summarizer : summarizers) {
                    String attributeName = getAttributeNameFromLabel(summarizer);
                    Function<Property, Double> extractor = attributeExtractors.get(attributeName);

                    if (extractor != null) {
                        Double attributeValue = extractor.apply(property);
                        if (attributeValue != null) {
                            double membership = summarizer.getFuzzySet().membership(attributeValue);
                            // ✅ T-norma minimum dla "AND" (intersection)
                            combinedMembership = Math.min(combinedMembership, membership);
                        } else {
                            combinedMembership = 0.0; // Null value = no membership
                            break;
                        }
                    } else {
                        logger.warning("No extractor found for attribute: " + attributeName);
                        combinedMembership = 0.0;
                        break;
                    }
                }

                sigmaCount += combinedMembership;
            }
        }

        // Oblicz wartość dla kwantyfikatora
        if (validObjects == 0) {
            return 0.0; // Unikamy dzielenia przez zero
        }

        double normalizedValue = switch (quantifier.type()) {
            case RELATIVE -> sigmaCount / validObjects;  // Proporcja ∈ [0,1]
            case ABSOLUTE -> sigmaCount;                 // Surowa liczba
        };

        double result = quantifier.fuzzySet().membership(normalizedValue);

        logger.info("Summary: " + this.toString());
        logger.info("sigmaCount: %.2f / validObjects: %d".formatted(sigmaCount, validObjects));
        logger.info("normalizedValue: %.2f".formatted(normalizedValue));
        logger.info("T1: %.2f".formatted(result));

        return result;
    }

    private String getAttributeNameFromLabel(Label label) {
        return label.getAttributeName();
    }

    // ===== MIARY JAKOŚCI - POZOSTAWIONE NA PRZYSZŁOŚĆ =====

    public double degreeOfImprecision() {
        // TODO: Implementacja T2
        return 0.0;
    }

    public double degreeOfCovering() {
        // TODO: Implementacja T3
        return 0.0;
    }

    public double degreeOfAppropriateness() {
        // TODO: Implementacja T4
        return 0.0;
    }

    public double summaryLength() {
        // TODO: Implementacja T5
        return 0.0;
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