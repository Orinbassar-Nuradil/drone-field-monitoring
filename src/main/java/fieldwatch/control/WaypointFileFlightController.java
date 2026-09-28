package fieldwatch.control;

import fieldwatch.geo.GeoPoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * Native implementor: does not fly, it exports the mission as a plain-text waypoint plan that a
 * ground station can load. I/O problems are translated to {@link FailureReason#STORAGE_ERROR}.
 */
public class WaypointFileFlightController implements FlightController {

    private final Path planFile;

    public WaypointFileFlightController(Path planFile) {
        this.planFile = planFile;
    }

    @Override
    public String name() {
        return "file:" + planFile;
    }

    @Override
    public void arm() throws FlightException {
        write("# fieldwatch waypoint plan" + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        append("ARM");
    }

    @Override
    public void takeOff(double altitudeMeters) throws FlightException {
        append(String.format(Locale.ROOT, "TAKEOFF %.1f", altitudeMeters));
    }

    @Override
    public void goTo(GeoPoint target, double altitudeMeters) throws FlightException {
        append(String.format(Locale.ROOT, "WAYPOINT %s %.1f", target, altitudeMeters));
    }

    @Override
    public void triggerPayload(PayloadCommand command) throws FlightException {
        append("PAYLOAD " + command);
    }

    @Override
    public void returnToHome() throws FlightException {
        append("RTH");
    }

    private void append(String line) throws FlightException {
        write(line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private void write(String text, StandardOpenOption... options) throws FlightException {
        try {
            Files.writeString(planFile, text, options);
        } catch (IOException e) {
            throw new FlightException(FailureReason.STORAGE_ERROR, "cannot write waypoint plan", e);
        }
    }
}
