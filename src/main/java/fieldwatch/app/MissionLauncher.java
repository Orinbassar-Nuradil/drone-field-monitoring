package fieldwatch.app;

import fieldwatch.control.FlightController;
import fieldwatch.mission.FieldMission;
import fieldwatch.mission.MissionReport;

/**
 * Chooses both sides of the Bridge at runtime from the request itself. The launcher never names a
 * concrete mission or controller class: it only asks the registries.
 */
public final class MissionLauncher {

    private final NamedRegistry<ControllerProvider> controllers;
    private final NamedRegistry<MissionProvider> missions;

    public MissionLauncher(NamedRegistry<ControllerProvider> controllers, NamedRegistry<MissionProvider> missions) {
        this.controllers = controllers;
        this.missions = missions;
    }

    public MissionReport launch(MissionRequest request) {
        FlightController controller = controllers.lookup(request.controllerScheme())
                .create(request.controllerArgument());
        FieldMission mission = missions.lookup(request.missionType()).create(controller);
        return mission.run(request.area());
    }
}
