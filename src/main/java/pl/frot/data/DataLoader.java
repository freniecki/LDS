package pl.frot.data;

import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.persistence.EntityManager;
import pl.frot.model.Property;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class DataLoader {

    private DataLoader() {}

    public static List<Property> getDataFromCSV(String path) throws FileNotFoundException {
        List<Property> properties = new CsvToBeanBuilder<Property>(new FileReader(path))
                .withType(Property.class)
                .build()
                .parse();

        return properties.stream()
                .filter(p -> p.getYearBuilt() != null && p.getYearBuilt() < 2021 && p.getYearBuilt() > 1852)
                .filter(p -> p.getLot() != null && p.getLot() > 433)
                .filter(p -> p.getTotalInteriorLivableArea() != null && p.getTotalInteriorLivableArea() > 300)
                .filter(p -> p.getElementarySchoolDistance() != null && p.getElementarySchoolDistance() > 0)
                .filter(p -> p.getMiddleSchoolDistance() != null && p.getMiddleSchoolDistance() > 0)
                .filter(p -> p.getHighSchoolDistance() != null && p.getHighSchoolDistance() > 0)
                .filter(p -> p.getTaxAssessedValue() != null && p.getTaxAssessedValue() > 14000)
                .filter(p -> p.getAnnualTaxAmount() != null && p.getAnnualTaxAmount() > 0)
                .filter(p -> p.getListedPrice() != null && p.getListedPrice() > 0)
                .filter(p -> p.getLastSoldPrice() != null && p.getLastSoldPrice() > 0)
                .filter(p -> p.getSoldPrice() != null && p.getSoldPrice() > 0)
                .toList();
    }

    public static void saveToDB(List<Property> properties) {
        try (EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();

            for (Property property : properties) {
                em.persist(property);
            }

            em.getTransaction().commit();
        }
    }
}
