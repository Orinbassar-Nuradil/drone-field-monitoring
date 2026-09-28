package fieldwatch.mission;

import fieldwatch.control.FlightController;
import fieldwatch.control.FlightException;
import fieldwatch.control.PayloadCommand;
import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;

import java.util.List;

/** Refined abstraction: high, wide lanes with a photo at every waypoint. */
public class MappingMission extends FieldMission {

    private static final double CRUISE_ALTITUDE_M = 60.0;
    private static final double SWATH_M = 30.0;

    public MappingMission(FlightController controller) {
        super(controller);
    }

    @Override
    public String name() {
        return "mapping";
    }

    @Override
    protected List<GeoPoint> planRoute(FieldArea area) {
        return LawnmowerPlanner.plan(area, SWATH_M);
    }

    @Override
    protected double cruiseAltitudeMeters() {
        return CRUISE_ALTITUDE_M;
    }

    @Override
    protected void atWaypoint(int index, int total, GeoPoint point) throws FlightException {
        controller.triggerPayload(PayloadCommand.CAPTURE_PHOTO);
    }
}
