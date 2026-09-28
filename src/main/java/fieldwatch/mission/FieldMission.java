package fieldwatch.mission;

import fieldwatch.control.FailureReason;
import fieldwatch.control.FlightController;
import fieldwatch.control.FlightException;
import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;

import java.util.List;
import java.util.Objects;

/**
 * Abstraction role of the Bridge. It knows <em>what</em> a mission does and delegates <em>how</em>
 * to a {@link FlightController}. It depends on that interface only: no back-end class, back-end
 * exception or back-end constant is visible from here or from any refined abstraction.
 */
public abstract class FieldMission {

    protected final FlightController controller;

    protected FieldMission(FlightController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    public abstract String name();

    /** Template method: the same flight skeleton for every mission, variation via hooks. */
    public final MissionReport run(FieldArea area) {
        Objects.requireNonNull(area, "area");
        List<GeoPoint> route = planRoute(area);
        int reached = 0;
        try {
            controller.arm();
            controller.takeOff(cruiseAltitudeMeters());
            for (int i = 0; i < route.size(); i++) {
                controller.goTo(route.get(i), cruiseAltitudeMeters());
                reached++;
                atWaypoint(i, route.size(), route.get(i));
            }
            controller.returnToHome();
            return MissionReport.completed(name(), controller.name(), reached);
        } catch (FlightException e) {
            bestEffortSafeStop();
            return MissionReport.aborted(name(), controller.name(), reached, e.reason(), e.getMessage());
        }
    }

    protected abstract List<GeoPoint> planRoute(FieldArea area);

    protected abstract double cruiseAltitudeMeters();

    /** Called after each waypoint has been reached. */
    protected abstract void atWaypoint(int index, int total, GeoPoint point) throws FlightException;

    /** Hook to put the payload into a safe state when a mission is aborted. Default: nothing. */
    protected void onAbort() throws FlightException {
    }

    /**
     * Failures during the safe stop are deliberately not reported: the original failure that caused
     * the abort is the one the caller needs, and the link may well be the thing that is broken.
     */
    private void bestEffortSafeStop() {
        try {
            onAbort();
        } catch (FlightException ignored) {
            // see method comment
        }
        try {
            controller.returnToHome();
        } catch (FlightException ignored) {
            // see method comment
        }
    }
}
