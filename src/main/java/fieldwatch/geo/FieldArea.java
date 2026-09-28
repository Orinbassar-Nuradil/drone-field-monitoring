package fieldwatch.geo;

/** A rectangular field described by its south-west and north-east corners. */
public record FieldArea(GeoPoint southWest, GeoPoint northEast) {

    private static final double METERS_PER_DEGREE_LATITUDE = 111_320.0;

    public FieldArea {
        if (southWest == null || northEast == null) {
            throw new IllegalArgumentException("Both corners are required");
        }
        if (southWest.latitude() >= northEast.latitude() || southWest.longitude() >= northEast.longitude()) {
            throw new IllegalArgumentException(
                    "South-west corner must be south and west of the north-east corner");
        }
    }

    /** North-south extent of the field in meters (approximation good enough for lane planning). */
    public double heightMeters() {
        return (northEast.latitude() - southWest.latitude()) * METERS_PER_DEGREE_LATITUDE;
    }
}
