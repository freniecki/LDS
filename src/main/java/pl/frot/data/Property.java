package pl.frot.data;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table (name = "properties")
public class Property {
    @Id
    @CsvBindByName(column = "Id")
    Long id;
    @CsvBindByName(column = "Type")
    String type;
    @CsvBindByName(column = "City")
    String city;

    @CsvBindByName(column = "Year built")
    Integer yearBuilt;

    @CsvBindByName(column = "Lot")
    Double lot;
    @CsvBindByName(column = "Total interior livable area")
    Double totalInteriorLivableArea;

    @CsvBindByName(column = "Elementary School Distance")
    Double elementarySchoolDistance;
    @CsvBindByName(column = "Middle School Distance")
    Double middleSchoolDistance;
    @CsvBindByName(column = "High School Distance")
    Double highSchoolDistance;

    @CsvBindByName(column = "Annual tax amount")
    Double annualTaxAmount;

    @CsvBindByName(column = "Tax assessed value")
    Double taxAssessedValue;
    @CsvBindByName(column = "Last Sold Price")
    Double lastSoldPrice;
    @CsvBindByName(column = "Listed Price")
    Double listedPrice;
    @CsvBindByName(column = "Sold Price")
    Double soldPrice;

    @CsvBindByName(column = "Zip")
    String zip;
}
