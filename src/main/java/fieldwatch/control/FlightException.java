package fieldwatch.control;

/** The single failure mechanism of the {@link FlightController} contract. */
public class FlightException extends Exception {

    private final FailureReason reason;

    public FlightException(FailureReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public FlightException(FailureReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public FailureReason reason() {
        return reason;
    }
}
