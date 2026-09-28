package fieldwatch.control;

import fieldwatch.geo.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Native implementor: an in-memory drone that only keeps a flight log. Useful for dry runs. */
public class SimulatorFlightController implements FlightController {

    private final String name;
    private final List<String> flightLog = new ArrayList<>();
    private boolean armed;
    private boolean airborne;

    public SimulatorFlightController(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return "sim:" + name;
    }

    public List<String> flightLog() {
        return Collections.unmodifiableList(flightLog);
    }

    @Override
    public void arm() {
        armed = true;
        flightLog.add("ARM");
    }

    @Override
    public void takeOff(double altitudeMeters) throws FlightException {
        if (!armed) {
            throw new FlightException(FailureReason.COMMAND_REJECTED, "takeOff rejected: not armed");
        }
        airborne = true;
        flightLog.add(String.format(Locale.ROOT, "TAKEOFF %.1fm", altitudeMeters));
    }

    @Override
    public void goTo(GeoPoint target, double altitudeMeters) throws FlightException {
        requireAirborne("goTo");
        flightLog.add(String.format(Locale.ROOT, "GOTO %s @%.1fm", target, altitudeMeters));
    }

    @Override
    public void triggerPayload(PayloadCommand command) throws FlightException {
        requireAirborne("triggerPayload");
        flightLog.add("PAYLOAD " + command);
    }

    @Override
    public void returnToHome() throws FlightException {
        requireAirborne("returnToHome");
        airborne = false;
        armed = false;
        flightLog.add("RTH");
    }

    private void requireAirborne(String operation) throws FlightException {
        if (!airborne) {
            throw new FlightException(FailureReason.COMMAND_REJECTED, operation + " rejected: not airborne");
        }
    }
}
