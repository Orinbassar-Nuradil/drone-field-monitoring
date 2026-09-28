package fieldwatch.mission;

import fieldwatch.control.FlightController;
import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;

import java.util.List;

/**
 * Refined abstraction: flies once around the field boundary. It was added without touching any
 * existing class, which is the Open/Closed Principle on the abstraction axis.
 */
public class PerimeterPatrolMission extends FieldMission {

    private static final double CRUISE_ALTITUDE_M = 40.0;

    public PerimeterPatrolMission(FlightController controller) {
        super(controller);
    }

    @Override
    public String name() {
        return "perimeter-patrol";
    }

    @Override
    protected List<GeoPoint> planRoute(FieldArea area) {
        GeoPoint sw = area.southWest();
        GeoPoint ne = area.northEast();
        return List.of(
                sw,
                new GeoPoint(sw.latitude(), ne.longitude()),
                ne,
                new GeoPoint(ne.latitude(), sw.longitude()),
                sw);
    }

    @Override
    protected double cruiseAltitudeMeters() {
        return CRUISE_ALTITUDE_M;
    }

    @Override
    protected void atWaypoint(int index, int total, GeoPoint point) {
        // pure observation flight: no payload action
    }
}
