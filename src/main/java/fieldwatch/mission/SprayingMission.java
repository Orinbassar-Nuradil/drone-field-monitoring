package fieldwatch.mission;

import fieldwatch.control.FlightController;
import fieldwatch.control.FlightException;
import fieldwatch.control.PayloadCommand;
import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;

import java.util.List;

/** Refined abstraction: low, narrow lanes; sprayer on at the first waypoint, off at the last. */
public class SprayingMission extends FieldMission {

    private static final double CRUISE_ALTITUDE_M = 4.0;
    private static final double SWATH_M = 10.0;

    public SprayingMission(FlightController controller) {
        super(controller);
    }

    @Override
    public String name() {
        return "spraying";
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
        if (index == 0) {
            controller.triggerPayload(PayloadCommand.SPRAY_ON);
        }
        if (index == total - 1) {
            controller.triggerPayload(PayloadCommand.SPRAY_OFF);
        }
    }

    @Override
    protected void onAbort() throws FlightException {
        controller.triggerPayload(PayloadCommand.SPRAY_OFF);
    }
}
