package fieldwatch.app;

import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;

/**
 * The runtime input. Text form: {@code <missionType> <controllerSpec> <swLat,swLon> <neLat,neLon>},
 * e.g. {@code spray vendor:UN-4471 48.1000,71.4000 48.1020,71.4030}.
 * The controller spec is {@code scheme[:argument]}; the scheme selects the implementor.
 */
public record MissionRequest(String missionType, String controllerSpec, FieldArea area) {

    public static MissionRequest parse(String line) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length != 4) {
            throw new IllegalArgumentException(
                    "Expected '<missionType> <controllerSpec> <swLat,swLon> <neLat,neLon>' but got: " + line);
        }
        FieldArea area = new FieldArea(GeoPoint.parse(tokens[2]), GeoPoint.parse(tokens[3]));
        return new MissionRequest(tokens[0], tokens[1], area);
    }

    public String controllerScheme() {
        int colon = controllerSpec.indexOf(':');
        return colon < 0 ? controllerSpec : controllerSpec.substring(0, colon);
    }

    public String controllerArgument() {
        int colon = controllerSpec.indexOf(':');
        return colon < 0 ? "" : controllerSpec.substring(colon + 1);
    }
}
