"""Builds docs/design-rationale.md and docs/design-rationale.pdf from one text source (max 2 pages)."""
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle

TITLE = "Design Rationale: Bridge + Adapter for Crop-Field Drone Missions"

SECTIONS = [
("1. Problem chosen",
 ["A farm-technology service flies drones over crop fields. There are several <b>kinds of mission</b> "
  "(mapping with photos, low-altitude spraying, perimeter patrol) and several <b>ways to fly them</b> "
  "(an in-memory simulator for dry runs, an exporter that writes a ground-station waypoint plan, and a real "
  "drone controlled through a manufacturer SDK). Missions and flight back-ends must vary independently, and "
  "the manufacturer SDK cannot be changed."]),
("2. Why neither pattern is enough alone",
 ["<b>Bridge alone.</b> Bridge separates <i>FieldMission</i> (Abstraction; Mapping, Spraying, PerimeterPatrol as "
  "Refined Abstractions) from <i>FlightController</i> (Implementor; Simulator, WaypointFile, Vendor). It assumes "
  "that every implementor already speaks the Implementor interface. The SDK does not, so without a converter "
  "I would either bend the interface to the SDK (breaking the two native back-ends) or let missions call the "
  "SDK directly (leaking vendor types into the abstraction).",
  "<b>Adapter alone.</b> An adapter makes one SDK usable, but does nothing about the M x N combinations: "
  "3 missions x 3 back-ends is 9 subclasses (or if/else on the back-end inside each mission), and a fourth "
  "back-end costs 3 more classes. With Bridge it is 3 + 3 classes, and each new variant costs one class.",
  "The two solve different problems: Bridge removes the combinatorial explosion, Adapter makes the "
  "incompatible class a legal member of the Implementor hierarchy."]),
("3. Why the wrapped implementation is genuinely incompatible",
 ["<i>VendorDroneSdk</i> differs from the contract in every dimension required by the assignment, not only in names:"]),
]

TABLE = [
 ["Aspect", "FlightController (contract)", "VendorDroneSdk (adaptee)"],
 ["Names", "arm, takeOff, goTo, triggerPayload, returnToHome", "powerOn, liftOff, navigate, firePayload, goHome"],
 ["Arguments", "goTo(GeoPoint, altitude)", "navigate(altitude, lon, lat): reordered, lon before lat"],
 ["Units / types", "altitude in meters (double)", "feet; liftOff takes int feet (rounding)"],
 ["Payload", "PayloadCommand enum", "numeric channel: 1 shutter, 2 spray open, 3 spray close"],
 ["Failure", "FlightException + FailureReason", "SdkStatus with numeric code (0 = ok); may be null; may throw runtime exceptions"],
]

SECTIONS2 = [
("",
 ["<i>VendorDroneAdapter</i> converts units and argument order, maps payload commands to channels, and translates "
  "every failure into a <i>FlightException</i> (17 to LINK_LOST, 23 to BATTERY_LOW, 31 to OUT_OF_BOUNDS, 40 to "
  "PAYLOAD_FAULT, 44/52 to COMMAND_REJECTED, anything else, a null status or an SDK runtime exception to UNKNOWN). "
  "Messages contain only the operation and the contract reason, so no vendor code or text leaks. "
  "<i>FieldMission</i> and its subclasses depend only on <i>FlightController</i>; a test scans their sources to "
  "keep it that way."]),
("4. Required complexity module: dynamic implementor selection",
 ["The input is a text request such as <font face='Courier'>spray vendor:UN-4471 swLat,swLon neLat,neLon</font>. "
  "The scheme before the colon selects the implementor (<i>sim</i>, <i>file</i>, <i>vendor</i>) and the first "
  "word selects the mission. <i>MissionLauncher</i> asks two <i>NamedRegistry</i> objects for providers and never "
  "names a concrete class, so the client never hard-codes a back-end. The adapted implementor is chosen exactly "
  "like the native ones."]),
("5. Open/Closed Principle on both axes",
 ["<b>New abstraction:</b> <i>PerimeterPatrolMission</i> was added as a new subclass and a single register line; "
  "no existing class changed. <b>New implementor:</b> a new controller is one class plus one register line. "
  "Two tests demonstrate this with classes defined only in the test code (a new back-end serving all three "
  "missions, and a new mission running on the simulator and on the adapted vendor drone). The only place that "
  "lists shipped classes is the composition root <i>DefaultWiring</i>."]),
("6. One limitation",
 ["The <i>FlightController</i> contract is a lowest-common-denominator, synchronous, command-by-command interface "
  "with no telemetry (battery level, position feedback). Missions therefore cannot plan around remaining battery "
  "or monitor the flight; they only learn about a problem after a command fails, and then perform a best-effort "
  "safe stop (sprayer off, return home). Supporting telemetry would require extending the Implementor "
  "interface, which every implementor, including the adapter, would then have to satisfy."]),
]

def strip(t):
    import re
    t = t.replace("<b>", "**").replace("</b>", "**").replace("<i>", "*").replace("</i>", "*")
    t = re.sub(r"<font[^>]*>", "`", t).replace("</font>", "`")
    return t

# ---- markdown ----
md = [f"# {TITLE}", ""]
for head, paras in SECTIONS:
    md += [f"## {head}", ""] + [strip(p) + "\n" for p in paras]
md += ["| " + " | ".join(TABLE[0]) + " |", "|" + "---|" * 3]
md += ["| " + " | ".join(r) + " |" for r in TABLE[1:]] + [""]
for head, paras in SECTIONS2:
    if head:
        md += [f"## {head}", ""]
    md += [strip(p) + "\n" for p in paras]
open("docs/design-rationale.md", "w").write("\n".join(md))

# ---- pdf ----
body = ParagraphStyle("b", fontName="Helvetica", fontSize=10.5, leading=14, spaceAfter=5)
h = ParagraphStyle("h", fontName="Helvetica-Bold", fontSize=11.5, leading=14, spaceBefore=7, spaceAfter=3,
                   textColor=colors.HexColor("#1F3A5F"))
t = ParagraphStyle("t", fontName="Helvetica-Bold", fontSize=14, leading=17, spaceAfter=2)
cell = ParagraphStyle("c", fontName="Helvetica", fontSize=9, leading=11)
cellb = ParagraphStyle("cb", parent=cell, fontName="Helvetica-Bold")
story = [Paragraph(TITLE, t),
         Paragraph("Assignment 3 - individual project. Implementor: FlightController. Adapted class: VendorDroneSdk. "
                   "Complexity module: dynamic implementor selection.", body)]
for head, paras in SECTIONS:
    story.append(Paragraph(head, h))
    story += [Paragraph(p, body) for p in paras]
rows = [[Paragraph(c, cellb if i == 0 else cell) for c in r] for i, r in enumerate(TABLE)]
tbl = Table(rows, colWidths=[24*mm, 62*mm, 88*mm], repeatRows=1)
tbl.setStyle(TableStyle([("GRID", (0, 0), (-1, -1), 0.4, colors.grey),
                         ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#DDEBFF")),
                         ("VALIGN", (0, 0), (-1, -1), "TOP"),
                         ("TOPPADDING", (0, 0), (-1, -1), 2), ("BOTTOMPADDING", (0, 0), (-1, -1), 2)]))
story += [tbl, Spacer(1, 5)]
for head, paras in SECTIONS2:
    if head:
        story.append(Paragraph(head, h))
    story += [Paragraph(p, body) for p in paras]
SimpleDocTemplate("docs/design-rationale.pdf", pagesize=A4, leftMargin=18*mm, rightMargin=18*mm,
                  topMargin=15*mm, bottomMargin=15*mm, title=TITLE).build(story)
print("built")
