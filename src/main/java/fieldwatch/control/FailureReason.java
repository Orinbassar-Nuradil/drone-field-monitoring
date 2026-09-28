package fieldwatch.control;

/** The only failure vocabulary the mission side ever sees, whatever the implementor is. */
public enum FailureReason {
    LINK_LOST,
    BATTERY_LOW,
    OUT_OF_BOUNDS,
    PAYLOAD_FAULT,
    COMMAND_REJECTED,
    STORAGE_ERROR,
    UNKNOWN
}
