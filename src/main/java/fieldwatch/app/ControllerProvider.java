package fieldwatch.app;

import fieldwatch.control.FlightController;

/** Creates a concrete implementor from the argument part of the request, e.g. "UN-4471". */
@FunctionalInterface
public interface ControllerProvider {
    FlightController create(String argument);
}
