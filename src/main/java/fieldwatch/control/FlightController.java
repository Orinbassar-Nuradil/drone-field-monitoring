package fieldwatch.control;

import fieldwatch.geo.GeoPoint;

/**
 * Implementor role of the Bridge.
 *
 * <p>Contract: altitudes are meters above the take-off point; every failure is reported
 * <em>only</em> as a {@link FlightException} carrying a {@link FailureReason}. Implementations
 * must never return error codes or null, and must not let back-end specific exceptions escape.
 */
public interface FlightController {

    /** Human readable identifier of the concrete back-end, used in reports. */
    String name();

    void arm() throws FlightException;

    void takeOff(double altitudeMeters) throws FlightException;

    void goTo(GeoPoint target, double altitudeMeters) throws FlightException;

    void triggerPayload(PayloadCommand command) throws FlightException;

    void returnToHome() throws FlightException;
}
