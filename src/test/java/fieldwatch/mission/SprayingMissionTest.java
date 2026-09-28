package fieldwatch.mission;

import fieldwatch.control.FailureReason;
import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;
import fieldwatch.testsupport.RecordingController;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SprayingMissionTest {

    /** 0.0002 degrees of latitude is about 22 m, so three 10 m lanes, six waypoints. */
    private static final FieldArea SMALL_FIELD =
            new FieldArea(new GeoPoint(48.0000, 71.0000), new GeoPoint(48.0002, 71.0010));

    @Test
    void flowsLowAndSwitchesTheSprayerOnAtFirstAndOffAtLastWaypoint() {
        RecordingController controller = new RecordingController();

        MissionReport report = new SprayingMission(controller).run(SMALL_FIELD);

        assertTrue(report.isCompleted());
        assertEquals(6, report.waypointsReached());
        assertEquals(List.of("takeOff:4.0"), controller.callsOf("takeOff"));
        assertEquals(List.of("payload:SPRAY_ON", "payload:SPRAY_OFF"), controller.callsOf("payload"));
        assertEquals("goTo", controller.calls.get(2).substring(0, 4));
        assertEquals("payload:SPRAY_ON", controller.calls.get(3));
    }

    @Test
    void closesTheSprayerBeforeReturningWhenTheFlightFailsMidRoute() {
        RecordingController controller = RecordingController.failingOn("goTo", 3, FailureReason.LINK_LOST);

        MissionReport report = new SprayingMission(controller).run(SMALL_FIELD);

        assertEquals(MissionStatus.ABORTED, report.status());
        assertEquals(FailureReason.LINK_LOST, report.failureReason());
        assertEquals(2, report.waypointsReached());
        int size = controller.calls.size();
        assertEquals("payload:SPRAY_OFF", controller.calls.get(size - 2));
        assertEquals("rth", controller.calls.get(size - 1));
    }

    @Test
    void reportsAnAbortWhenArmingFailsAndReachesNoWaypoint() {
        RecordingController controller = RecordingController.failingOn("arm", 1, FailureReason.COMMAND_REJECTED);

        MissionReport report = new SprayingMission(controller).run(SMALL_FIELD);

        assertEquals(MissionStatus.ABORTED, report.status());
        assertEquals(0, report.waypointsReached());
        assertEquals(FailureReason.COMMAND_REJECTED, report.failureReason());
    }
}
