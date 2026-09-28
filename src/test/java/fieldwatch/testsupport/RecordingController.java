package fieldwatch.testsupport;

import fieldwatch.control.FailureReason;
import fieldwatch.control.FlightController;
import fieldwatch.control.FlightException;
import fieldwatch.control.PayloadCommand;
import fieldwatch.geo.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Test stub for the Implementor role: records every call and can be told to fail on the n-th call
 * of one operation (the failing call is recorded too, later calls succeed again).
 */
public class RecordingController implements FlightController {

    public final List<String> calls = new ArrayList<>();

    private String failOperation;
    private int failOccurrence;
    private FailureReason failReason;
    private final java.util.Map<String, Integer> counters = new java.util.HashMap<>();

    public static RecordingController failingOn(String operation, int occurrence, FailureReason reason) {
        RecordingController controller = new RecordingController();
        controller.failOperation = operation;
        controller.failOccurrence = occurrence;
        controller.failReason = reason;
        return controller;
    }

    public List<String> callsOf(String prefix) {
        return calls.stream().filter(c -> c.startsWith(prefix)).toList();
    }

    @Override
    public String name() {
        return "recording";
    }

    @Override
    public void arm() throws FlightException {
        record("arm", "arm");
    }

    @Override
    public void takeOff(double altitudeMeters) throws FlightException {
        record("takeOff", String.format(Locale.ROOT, "takeOff:%.1f", altitudeMeters));
    }

    @Override
    public void goTo(GeoPoint target, double altitudeMeters) throws FlightException {
        record("goTo", String.format(Locale.ROOT, "goTo:%s@%.1f", target, altitudeMeters));
    }

    @Override
    public void triggerPayload(PayloadCommand command) throws FlightException {
        record("payload", "payload:" + command);
    }

    @Override
    public void returnToHome() throws FlightException {
        record("rth", "rth");
    }

    private void record(String operation, String description) throws FlightException {
        calls.add(description);
        int seen = counters.merge(operation, 1, Integer::sum);
        if (operation.equals(failOperation) && seen == failOccurrence) {
            throw new FlightException(failReason, operation + " failed on purpose");
        }
    }
}
