# Drone field monitoring: Bridge + Adapter

Individual Assignment 3. A mission (mapping, spraying, perimeter patrol) is flown through a
flight back-end (simulator, waypoint-file exporter, manufacturer drone SDK). Bridge keeps the two
hierarchies independent; Adapter makes the incompatible manufacturer SDK a legal back-end.

## Build and test (single command)

    mvn test          # Java 17+, Maven 3.8+; compiles and runs all JUnit 5 tests

## Run

    mvn -q compile
    java -cp target/classes fieldwatch.app.Main < examples/requests.txt

Request format: `<missionType> <controllerSpec> <swLat,swLon> <neLat,neLon>`
(mission types `map`, `spray`, `patrol`; controllers `sim:<name>`, `file:<path>`, `vendor:<tail>`).
The controller is chosen at runtime from the request (complexity module: dynamic implementor selection).

## Where things are

| Requirement | Location |
|---|---|
| Abstraction + Refined Abstractions | `fieldwatch.mission` (`FieldMission`, `MappingMission`, `SprayingMission`, `PerimeterPatrolMission`) |
| Implementor + native implementations | `fieldwatch.control` (`FlightController`, `SimulatorFlightController`, `WaypointFileFlightController`) |
| Adapted class (unmodified third party) | `fieldwatch.vendor` (`VendorDroneSdk`, `SdkStatus`) |
| Adapter + failure translation | `fieldwatch.adapter.VendorDroneAdapter` |
| Dynamic implementor selection | `fieldwatch.app` (`MissionRequest`, `NamedRegistry`, `MissionLauncher`, `DefaultWiring`) |
| Tests | `src/test/java` (stubs `RecordingController`, `ScriptedSdk`) |
| UML class diagram | `docs/uml-class-diagram.svg` / `.png` (source: `docs/make_uml.py`) |
| Design rationale (2 pages) | `docs/design-rationale.pdf` / `.md` |
