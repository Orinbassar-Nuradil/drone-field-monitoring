"""Generates docs/uml-class-diagram.(svg|png) from the class list below (needs Graphviz `dot`)."""
import subprocess, html

def cls(name, stereo=None, fields=(), methods=(), fill="#FFFFFF", abstract=False, italic_methods=()):
    title = f"<I>{html.escape(name)}</I>" if abstract else html.escape(name)
    head = ""
    if stereo:
        head += f'<TR><TD ALIGN="CENTER"><FONT POINT-SIZE="9">&laquo;{stereo}&raquo;</FONT></TD></TR>'
    head += f'<TR><TD ALIGN="CENTER"><B>{title}</B></TD></TR>'
    def block(lines, ital=()):
        if not lines:
            return '<TR><TD ALIGN="LEFT"> </TD></TR>'
        out = ""
        for l in lines:
            t = html.escape(l)
            if l in ital:
                t = f"<I>{t}</I>"
            out += f'<TR><TD ALIGN="LEFT" BALIGN="LEFT">{t}</TD></TR>'
        return out
    body = head + '<HR/>' + block(fields) + '<HR/>' + block(methods, italic_methods)
    return (f'{name.split("<")[0]} [shape=plain, label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" '
            f'CELLPADDING="3" BGCOLOR="{fill}">{body}</TABLE>>];')

nodes = {}
# ---------------- mission package: Abstraction side ----------------
nodes["mission"] = [
 cls("FieldMission", "abstract", ["# controller : FlightController"],
     ["+ name() : String", "+ run(area : FieldArea) : MissionReport {final}",
      "# planRoute(area : FieldArea) : List<GeoPoint>", "# cruiseAltitudeMeters() : double",
      "# atWaypoint(index : int, total : int, point : GeoPoint)", "# onAbort()",
      "- bestEffortSafeStop()"],
     fill="#FFF4D6", abstract=True,
     italic_methods=["+ name() : String", "# planRoute(area : FieldArea) : List<GeoPoint>",
                     "# cruiseAltitudeMeters() : double", "# atWaypoint(index : int, total : int, point : GeoPoint)"]),
 cls("MappingMission", None, ["- CRUISE_ALTITUDE_M = 60.0", "- SWATH_M = 30.0"],
     ["+ name()", "# planRoute(area)", "# cruiseAltitudeMeters()", "# atWaypoint(index, total, point)"], fill="#FFF4D6"),
 cls("SprayingMission", None, ["- CRUISE_ALTITUDE_M = 4.0", "- SWATH_M = 10.0"],
     ["+ name()", "# planRoute(area)", "# cruiseAltitudeMeters()", "# atWaypoint(index, total, point)", "# onAbort()"], fill="#FFF4D6"),
 cls("PerimeterPatrolMission", None, ["- CRUISE_ALTITUDE_M = 40.0"],
     ["+ name()", "# planRoute(area)", "# cruiseAltitudeMeters()", "# atWaypoint(index, total, point)"], fill="#FFF4D6"),
 cls("LawnmowerPlanner", "utility", [], ["+ plan(area : FieldArea, swathMeters : double) : List<GeoPoint> {static}"]),
 cls("MissionReport", "record",
     ["missionName : String", "controllerName : String", "status : MissionStatus", "waypointsReached : int",
      "failureReason : FailureReason", "message : String"],
     ["+ completed(...) : MissionReport {static}", "+ aborted(...) : MissionReport {static}", "+ isCompleted() : boolean"]),
 cls("MissionStatus", "enumeration", ["COMPLETED", "ABORTED"], []),
]
# ---------------- control package: Implementor side ----------------
nodes["control"] = [
 cls("FlightController", "interface", [],
     ["+ name() : String", "+ arm()", "+ takeOff(altitudeMeters : double)",
      "+ goTo(target : GeoPoint, altitudeMeters : double)", "+ triggerPayload(command : PayloadCommand)",
      "+ returnToHome()", "(all operations: throws FlightException)"], fill="#DDEBFF"),
 cls("SimulatorFlightController", None,
     ["- name : String", "- flightLog : List<String>", "- armed : boolean", "- airborne : boolean"],
     ["+ name()", "+ flightLog() : List<String>", "+ arm()", "+ takeOff(altitudeMeters)", "+ goTo(target, altitudeMeters)",
      "+ triggerPayload(command)", "+ returnToHome()"], fill="#DDEBFF"),
 cls("WaypointFileFlightController", None, ["- planFile : Path"],
     ["+ name()", "+ arm()", "+ takeOff(altitudeMeters)", "+ goTo(target, altitudeMeters)",
      "+ triggerPayload(command)", "+ returnToHome()"], fill="#DDEBFF"),
 cls("FlightException", None, ["- reason : FailureReason"],
     ["+ reason() : FailureReason"]),
 cls("FailureReason", "enumeration",
     ["LINK_LOST", "BATTERY_LOW", "OUT_OF_BOUNDS", "PAYLOAD_FAULT", "COMMAND_REJECTED", "STORAGE_ERROR", "UNKNOWN"], []),
 cls("PayloadCommand", "enumeration", ["CAPTURE_PHOTO", "SPRAY_ON", "SPRAY_OFF"], []),
]
# ---------------- adapter + third-party ----------------
nodes["adapter"] = [
 cls("VendorDroneAdapter", "adapter", ["- FEET_PER_METER : double {static}", "- sdk : VendorDroneSdk"],
     ["+ name()", "+ arm()", "+ takeOff(altitudeMeters)", "+ goTo(target, altitudeMeters)",
      "+ triggerPayload(command)", "+ returnToHome()",
      "- execute(operation : String, call : Supplier<SdkStatus>)", "- channelFor(command) : int {static}",
      "- reasonFor(vendorCode : int) : FailureReason {static}"], fill="#DFF5DF"),
]
nodes["vendor"] = [
 cls("VendorDroneSdk", "adaptee, third-party",
     ["+ OK, ERR_LINK_TIMEOUT, ERR_BATTERY_CRITICAL, ERR_GEOFENCE,", "+ ERR_PAYLOAD_JAM, ERR_BAD_CHANNEL, ERR_NOT_READY : int",
      "+ CH_SHUTTER, CH_SPRAY_OPEN, CH_SPRAY_CLOSE : int", "- tailNumber : String", "- poweredOn, flying : boolean"],
     ["+ tail() : String", "+ powerOn() : SdkStatus", "+ liftOff(heightFeet : int) : SdkStatus",
      "+ navigate(altFeet : double, lon : double, lat : double) : SdkStatus",
      "+ firePayload(channel : int) : SdkStatus", "+ goHome() : SdkStatus"], fill="#EEEEEE"),
 cls("SdkStatus", "third-party", ["- code : int", "- detail : String"],
     ["+ code() : int", "+ detail() : String", "+ isOk() : boolean"], fill="#EEEEEE"),
]
# ---------------- geo ----------------
nodes["geo"] = [
 cls("GeoPoint", "record", ["latitude : double", "longitude : double"], ["+ parse(text : String) : GeoPoint {static}"]),
 cls("FieldArea", "record", ["southWest : GeoPoint", "northEast : GeoPoint"], ["+ heightMeters() : double"]),
]
# ---------------- app ----------------
nodes["app"] = [
 cls("Main", None, [], ["+ main(args : String[]) {static}"]),
 cls("MissionLauncher", None,
     ["- controllers : NamedRegistry<ControllerProvider>", "- missions : NamedRegistry<MissionProvider>"],
     ["+ launch(request : MissionRequest) : MissionReport"]),
 cls("NamedRegistry<T>", None, ["- kind : String", "- entries : Map<String, T>"],
     ["+ register(key : String, entry : T) : NamedRegistry<T>", "+ lookup(key : String) : T"]),
 cls("ControllerProvider", "interface", [], ["+ create(argument : String) : FlightController"]),
 cls("MissionProvider", "interface", [], ["+ create(controller : FlightController) : FieldMission"]),
 cls("MissionRequest", "record", ["missionType : String", "controllerSpec : String", "area : FieldArea"],
     ["+ parse(line : String) : MissionRequest {static}", "+ controllerScheme() : String", "+ controllerArgument() : String"]),
 cls("DefaultWiring", "composition root",  [],
     ["+ defaultControllers() : NamedRegistry<ControllerProvider> {static}",
      "+ defaultMissions() : NamedRegistry<MissionProvider> {static}", "+ defaultLauncher() : MissionLauncher {static}"]),
 cls("UnknownSelectionException", None, [], []),
]

titles = {"mission": "package fieldwatch.mission  (Abstraction side)", "control": "package fieldwatch.control  (Implementor side)",
          "adapter": "package fieldwatch.adapter", "vendor": "package fieldwatch.vendor (third-party, unmodified)",
          "geo": "package fieldwatch.geo", "app": "package fieldwatch.app  (runtime selection)"}

edges = [
 # Bridge
 'FieldMission -> FlightController [dir=both, arrowtail=odiamond, arrowhead=vee, label="  controller  (the bridge)", fontsize=11, penwidth=2, color="#B8860B", fontcolor="#B8860B"];',
 # generalisation
 'MappingMission -> FieldMission [arrowhead=empty];',
 'SprayingMission -> FieldMission [arrowhead=empty];',
 'PerimeterPatrolMission -> FieldMission [arrowhead=empty];',
 # realisation
 'SimulatorFlightController -> FlightController [style=dashed, arrowhead=empty];',
 'WaypointFileFlightController -> FlightController [style=dashed, arrowhead=empty];',
 'VendorDroneAdapter -> FlightController [style=dashed, arrowhead=empty, penwidth=2, color="#2E7D32"];',
 # adapter wraps adaptee
 'VendorDroneAdapter -> VendorDroneSdk [dir=both, arrowtail=odiamond, arrowhead=vee, label="  wraps  sdk", fontsize=11, penwidth=2, color="#2E7D32", fontcolor="#2E7D32"];',
 'VendorDroneAdapter -> SdkStatus [style=dashed, arrowhead=vee, label="  translates", fontsize=10];',
 'VendorDroneSdk -> SdkStatus [style=dashed, arrowhead=vee, label="  returns", fontsize=10];',
 # contract vocabulary
 'FlightController -> FlightException [style=dashed, arrowhead=vee, label="  throws", fontsize=10];',
 'FlightException -> FailureReason [arrowhead=vee];',
 'FlightController -> PayloadCommand [style=dashed, arrowhead=vee];',
 'FlightController -> GeoPoint [style=dashed, arrowhead=vee];',
 # mission internals
 'FieldMission -> MissionReport [style=dashed, arrowhead=vee, label="  creates", fontsize=10];',
 'FieldMission -> FieldArea [style=dashed, arrowhead=vee];',
 'MappingMission -> LawnmowerPlanner [style=dashed, arrowhead=vee];',
 'SprayingMission -> LawnmowerPlanner [style=dashed, arrowhead=vee];',
 'LawnmowerPlanner -> FieldArea [style=dashed, arrowhead=vee];',
 'MissionReport -> MissionStatus [arrowhead=vee];',
 'MissionReport -> FailureReason [arrowhead=vee];',
 'FieldArea -> GeoPoint [dir=both, arrowtail=diamond, arrowhead=vee, headlabel="2", labeldistance=1.5, fontsize=10];',
 # app
 'Main -> MissionLauncher [style=dashed, arrowhead=vee];',
 'Main -> MissionRequest [style=dashed, arrowhead=vee];',
 'Main -> DefaultWiring [style=dashed, arrowhead=vee];',
 'MissionLauncher -> NamedRegistry [dir=both, arrowtail=odiamond, arrowhead=vee, headlabel="2", labeldistance=1.5, fontsize=10];',
 'NamedRegistry -> ControllerProvider [style=dashed, arrowhead=vee, label="  T =", fontsize=9];',
 'NamedRegistry -> MissionProvider [style=dashed, arrowhead=vee, label="  T =", fontsize=9];',
 'MissionLauncher -> MissionRequest [style=dashed, arrowhead=vee];',
 'MissionLauncher -> MissionReport [style=dashed, arrowhead=vee];',
 'NamedRegistry -> UnknownSelectionException [style=dashed, arrowhead=vee, label="  throws", fontsize=10];',
 'ControllerProvider -> FlightController [style=dashed, arrowhead=vee, label="  creates", fontsize=10];',
 'MissionProvider -> FieldMission [style=dashed, arrowhead=vee, label="  creates", fontsize=10];',
 'MissionProvider -> FlightController [style=dashed, arrowhead=vee];',
 'MissionRequest -> FieldArea [arrowhead=vee];',
 'DefaultWiring -> NamedRegistry [style=dashed, arrowhead=vee, label="  registers providers for\\nSimulator, WaypointFile, VendorDroneAdapter,\\nMapping, Spraying, PerimeterPatrol", fontsize=9];',
 'DefaultWiring -> MissionLauncher [style=dashed, arrowhead=vee];',
]

dot = ['digraph G {', 'rankdir=BT; nodesep=0.5; ranksep=0.8; compound=true; splines=true;',
       'graph [fontname="Helvetica", fontsize=13, labeljust=l]; node [fontname="Helvetica", fontsize=10]; edge [fontname="Helvetica"];']
for key, items in nodes.items():
    dot.append(f'subgraph cluster_{key} {{ label="{titles[key]}"; style="rounded,dashed"; color="#888888";')
    dot += items
    dot.append('}')
dot += edges
dot.append('}')
src = "\n".join(dot)
open("docs/uml-class-diagram.dot", "w").write(src)
for fmt in ("svg", "png"):
    extra = ["-Gdpi=130"] if fmt == "png" else []
    subprocess.run(["dot", f"-T{fmt}", *extra, "docs/uml-class-diagram.dot", "-o", f"docs/uml-class-diagram.{fmt}"], check=True)
print("ok")
