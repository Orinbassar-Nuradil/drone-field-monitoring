package fieldwatch.mission;

import fieldwatch.control.FailureReason;

/** Outcome of a mission run. {@code failureReason} and {@code message} describe an abort. */
public record MissionReport(
        String missionName,
        String controllerName,
        MissionStatus status,
        int waypointsReached,
        FailureReason failureReason,
        String message) {

    public static MissionReport completed(String missionName, String controllerName, int waypointsReached) {
        return new MissionReport(missionName, controllerName, MissionStatus.COMPLETED,
                waypointsReached, null, "ok");
    }

    public static MissionReport aborted(String missionName, String controllerName, int waypointsReached,
                                        FailureReason reason, String message) {
        return new MissionReport(missionName, controllerName, MissionStatus.ABORTED,
                waypointsReached, reason, message);
    }

    public boolean isCompleted() {
        return status == MissionStatus.COMPLETED;
    }
}
