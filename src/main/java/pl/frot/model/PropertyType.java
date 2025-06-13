package pl.frot.model;

public enum  PropertyType {
    LOS_ANGELES_AREA("region Los Angeles"),
    SAN_FRANCISCO_PENINSULA("półwysep San Francisco"),
    CENTRAL_CALIFORNIA("centralna Kalifornia"),
    SAN_DIEGO_REGION("region San Diego"),
    NORTHERN_CALIFORNIA("północna Kalifornia"),
    MOUNTAIN_NORTHEAST("region górski");

    public final String propertyTypeName;

    PropertyType(String s) {
        this.propertyTypeName = s;
    }
}
