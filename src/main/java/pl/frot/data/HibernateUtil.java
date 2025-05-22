package pl.frot.data;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("ksrData");

    private HibernateUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
