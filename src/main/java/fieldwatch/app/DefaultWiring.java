package fieldwatch.app;

import fieldwatch.adapter.VendorDroneAdapter;
import fieldwatch.control.SimulatorFlightController;
import fieldwatch.control.WaypointFileFlightController;
import fieldwatch.mission.MappingMission;
import fieldwatch.mission.PerimeterPatrolMission;
import fieldwatch.mission.SprayingMission;
import fieldwatch.vendor.VendorDroneSdk;

import java.nio.file.Path;

/**
 * Composition root: the one place that lists the shipped implementors and missions. Adding a new
 * back-end or mission type means adding a class plus one {@code register} line here (or in any
 * other wiring code); no existing mission, controller or adapter is edited.
 */
public final class DefaultWiring {

    private DefaultWiring() {
    }

    public static NamedRegistry<ControllerProvider> defaultControllers() {
        return new NamedRegistry<ControllerProvider>("controller scheme")
                .register("sim", arg -> new SimulatorFlightController(arg.isBlank() ? "default" : arg))
                .register("file", arg -> {
                    if (arg.isBlank()) {
                        throw new IllegalArgumentException("file: needs a path, e.g. file:out/plan.txt");
                    }
                    return new WaypointFileFlightController(Path.of(arg));
                })
                .register("vendor", arg -> new VendorDroneAdapter(new VendorDroneSdk(arg)));
    }

    public static NamedRegistry<MissionProvider> defaultMissions() {
        return new NamedRegistry<MissionProvider>("mission type")
                .register("map", MappingMission::new)
                .register("spray", SprayingMission::new)
                .register("patrol", PerimeterPatrolMission::new);
    }

    public static MissionLauncher defaultLauncher() {
        return new MissionLauncher(defaultControllers(), defaultMissions());
    }
}
