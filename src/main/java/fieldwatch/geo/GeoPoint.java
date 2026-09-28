package fieldwatch.geo;

import java.util.Locale;

/** A WGS-84 position in decimal degrees. */
public record GeoPoint(double latitude, double longitude) {

    public GeoPoint {
        if (Double.isNaN(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be within [-90, 90]: " + latitude);
        }
        if (Double.isNaN(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be within [-180, 180]: " + longitude);
        }
    }

    /** Parses "lat,lon", e.g. "48.1000,71.4000". */
    public static GeoPoint parse(String text) {
        String[] parts = text.trim().split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected 'lat,lon' but got: " + text);
        }
        try {
            return new GeoPoint(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Coordinates are not numbers: " + text, e);
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%.6f,%.6f", latitude, longitude);
    }
}
