package fieldwatch.mission;

import fieldwatch.control.FailureReason;
import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;
import fieldwatch.testsupport.RecordingController;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingMissionTest {

    /** 0.0004 degrees of latitude is about 44.5 m, so two 30 m lanes, four waypoints. */
    private static final FieldArea SMALL_FIELD =
            new FieldArea(new GeoPoint(48.0000, 71.0000), new GeoPoint(48.0004, 71.0010));

    @Test
    void delegatesTheWholeFlightToTheController() {
        RecordingController controller = new RecordingController();

        MissionReport report = new MappingMission(controller).run(SMALL_FIELD);

        assertEquals(List.of(
                "arm",
                "takeOff:60.0",
                "goTo:48.000100,71.000000@60.0", "payload:CAPTURE_PHOTO",
                "goTo:48.000100,71.001000@60.0", "payload:CAPTURE_PHOTO",
                "goTo:48.000300,71.001000@60.0", "payload:CAPTURE_PHOTO",
                "goTo:48.000300,71.000000@60.0", "payload:CAPTURE_PHOTO",
                "rth"), controller.calls);
        assertTrue(report.isCompleted());
        assertEquals(4, report.waypointsReached());
        assertEquals("recording", report.controllerName());
        assertNull(report.failureReason());
    }

    @Test
    void abortsWithTheControllersFailureReasonAndStillGoesHome() {
        RecordingController controller = RecordingController.failingOn("goTo", 2, FailureReason.BATTERY_LOW);

        MissionReport report = new MappingMission(controller).run(SMALL_FIELD);

        assertEquals(MissionStatus.ABORTED, report.status());
        assertEquals(FailureReason.BATTERY_LOW, report.failureReason());
        assertEquals(1, report.waypointsReached());
        assertEquals("rth", controller.calls.get(controller.calls.size() - 1));
    }
}
