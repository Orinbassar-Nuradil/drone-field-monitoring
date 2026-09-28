package fieldwatch.app;

import fieldwatch.control.FlightController;
import fieldwatch.mission.FieldMission;

/** Creates a refined abstraction bound to an already chosen implementor. */
@FunctionalInterface
public interface MissionProvider {
    FieldMission create(FlightController controller);
}
