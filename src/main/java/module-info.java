module LDS {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.fasterxml.jackson.databind;
    requires com.opencsv;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.logging;

    requires static lombok;

    exports pl.frot.fx;
    exports pl.frot.fuzzy.summaries;
    exports pl.frot.data;

    opens pl.frot.fx to javafx.fxml;
    opens pl.frot.data to org.hibernate.orm.core, com.opencsv, com.fasterxml.jackson.databind;
    opens pl.frot.model to com.fasterxml.jackson.databind, com.opencsv, org.hibernate.orm.core;
    opens pl.frot.model.enums to com.fasterxml.jackson.databind, com.opencsv, org.hibernate.orm.core;
    opens pl.frot.model.dtos to com.fasterxml.jackson.databind, com.opencsv, org.hibernate.orm.core;
}
