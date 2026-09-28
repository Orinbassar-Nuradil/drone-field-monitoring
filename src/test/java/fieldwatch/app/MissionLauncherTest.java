package fieldwatch.app;

import fieldwatch.control.FlightController;
import fieldwatch.control.FlightException;
import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;
import fieldwatch.mission.FieldMission;
import fieldwatch.mission.MissionReport;
import fieldwatch.testsupport.RecordingController;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissionLauncherTest {

    private static final String FIELD = "48.0000,71.0000 48.0004,71.0010";

    // ---- dynamic implementor selection: the input alone decides ----

    @Test
    void theSchemeInTheInputSelectsTheImplementorIncludingTheAdaptedOne() {
        MissionLauncher launcher = DefaultWiring.defaultLauncher();

        MissionReport onSimulator = launcher.launch(MissionRequest.parse("map sim:demo " + FIELD));
        MissionReport onVendor = launcher.launch(MissionRequest.parse("map vendor:UN-9 " + FIELD));

        assertEquals("sim:demo", onSimulator.controllerName());
        assertEquals("vendor:UN-9", onVendor.controllerName());
        assertTrue(onSimulator.isCompleted());
        assertTrue(onVendor.isCompleted());
    }

    @Test
    void theMissionTypeInTheInputSelectsTheAbstraction() {
        MissionLauncher launcher = DefaultWiring.defaultLauncher();

        assertEquals("mapping", launcher.launch(MissionRequest.parse("map sim: " + FIELD)).missionName());
        assertEquals("spraying", launcher.launch(MissionRequest.parse("spray sim: " + FIELD)).missionName());
        assertEquals("perimeter-patrol", launcher.launch(MissionRequest.parse("patrol sim: " + FIELD)).missionName());
    }

    @Test
    void fileSchemeWritesAPlanToTheGivenPath() throws IOException {
        Path plan = Files.createTempDirectory("fieldwatch").resolve("plan.txt");

        MissionReport report = DefaultWiring.defaultLauncher()
                .launch(MissionRequest.parse("patrol file:" + plan + " " + FIELD));

        assertTrue(report.isCompleted());
        assertTrue(Files.readString(plan).contains("TAKEOFF 40.0"));
    }

    @Test
    void unknownSchemeOrMissionTypeIsRejectedWithAHelpfulMessage() {
        MissionLauncher launcher = DefaultWiring.defaultLauncher();

        UnknownSelectionException badScheme = assertThrows(UnknownSelectionException.class,
                () -> launcher.launch(MissionRequest.parse("map ghost:x " + FIELD)));
        UnknownSelectionException badType = assertThrows(UnknownSelectionException.class,
                () -> launcher.launch(MissionRequest.parse("dance sim: " + FIELD)));

        assertTrue(badScheme.getMessage().contains("vendor"));
        assertTrue(badType.getMessage().contains("spray"));
    }

    @Test
    void malformedRequestsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> MissionRequest.parse("map sim:"));
        assertThrows(IllegalArgumentException.class, () -> MissionRequest.parse("map sim: 1,2 abc"));
    }

    // ---- Open/Closed on the implementor axis ----

    @Test
    void aBrandNewImplementorPlugsInWithoutChangingAnyExistingClass() {
        RecordingController newBackend = new RecordingController();
        NamedRegistry<ControllerProvider> controllers = DefaultWiring.defaultControllers()
                .register("lab-rig", arg -> newBackend);
        MissionLauncher launcher = new MissionLauncher(controllers, DefaultWiring.defaultMissions());

        for (String type : List.of("map", "spray", "patrol")) {
            MissionReport report = launcher.launch(MissionRequest.parse(type + " lab-rig: " + FIELD));
            assertTrue(report.isCompleted());
            assertEquals("recording", report.controllerName());
        }
        assertTrue(newBackend.calls.contains("arm"));
    }

    // ---- Open/Closed on the abstraction axis ----

    /** A mission type invented by this test; no production class knows about it. */
    private static final class HoverCheckMission extends FieldMission {
        HoverCheckMission(FlightController controller) {
            super(controller);
        }

        @Override
        public String name() {
            return "hover-check";
        }

        @Override
        protected List<GeoPoint> planRoute(FieldArea area) {
            return List.of(area.southWest());
        }

        @Override
        protected double cruiseAltitudeMeters() {
            return 15.0;
        }

        @Override
        protected void atWaypoint(int index, int total, GeoPoint point) throws FlightException {
            // hover only
        }
    }

    @Test
    void aBrandNewMissionWorksOnEveryExistingImplementor() {
        NamedRegistry<MissionProvider> missions = DefaultWiring.defaultMissions()
                .register("hover", HoverCheckMission::new);
        MissionLauncher launcher = new MissionLauncher(DefaultWiring.defaultControllers(), missions);

        MissionReport onSimulator = launcher.launch(MissionRequest.parse("hover sim: " + FIELD));
        MissionReport onVendor = launcher.launch(MissionRequest.parse("hover vendor:UN-9 " + FIELD));

        assertEquals("hover-check", onSimulator.missionName());
        assertTrue(onSimulator.isCompleted());
        assertTrue(onVendor.isCompleted());
        assertEquals(1, onVendor.waypointsReached());
    }
}
