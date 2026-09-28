package fieldwatch.control;

import fieldwatch.geo.GeoPoint;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeControllersTest {

    @Test
    void simulatorRejectsTakeOffWhenNotArmed() {
        SimulatorFlightController simulator = new SimulatorFlightController("t");

        FlightException thrown = assertThrows(FlightException.class, () -> simulator.takeOff(10));

        assertEquals(FailureReason.COMMAND_REJECTED, thrown.reason());
    }

    @Test
    void waypointFileControllerWritesAPlan() throws IOException, FlightException {
        Path plan = Files.createTempDirectory("fieldwatch").resolve("plan.txt");
        WaypointFileFlightController controller = new WaypointFileFlightController(plan);

        controller.arm();
        controller.takeOff(40);
        controller.goTo(new GeoPoint(48.1, 71.4), 40);
        controller.returnToHome();

        String content = Files.readString(plan);
        assertTrue(content.contains("TAKEOFF 40.0"));
        assertTrue(content.contains("WAYPOINT 48.100000,71.400000 40.0"));
        assertTrue(content.contains("RTH"));
    }

    @Test
    void waypointFileControllerReportsStorageErrors() throws IOException {
        Path directoryInsteadOfFile = Files.createTempDirectory("fieldwatch");
        WaypointFileFlightController controller = new WaypointFileFlightController(directoryInsteadOfFile);

        FlightException thrown = assertThrows(FlightException.class, controller::arm);

        assertEquals(FailureReason.STORAGE_ERROR, thrown.reason());
    }
}
