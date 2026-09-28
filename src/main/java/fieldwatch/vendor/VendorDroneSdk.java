package fieldwatch.vendor;

/**
 * Stand-in for a closed-source drone manufacturer SDK. Treated as third-party code: it is never
 * modified for this project. Deliberately incompatible with the FlightController contract:
 * different method names, altitude in integer/decimal FEET, argument order (altitude, lon, lat),
 * payload addressed by numeric channel, and failures reported as {@link SdkStatus} codes.
 */
public class VendorDroneSdk {

    public static final int OK = 0;
    public static final int ERR_LINK_TIMEOUT = 17;
    public static final int ERR_BATTERY_CRITICAL = 23;
    public static final int ERR_GEOFENCE = 31;
    public static final int ERR_PAYLOAD_JAM = 40;
    public static final int ERR_BAD_CHANNEL = 44;
    public static final int ERR_NOT_READY = 52;

    public static final int CH_SHUTTER = 1;
    public static final int CH_SPRAY_OPEN = 2;
    public static final int CH_SPRAY_CLOSE = 3;

    private static final double MAX_ALTITUDE_FEET = 400.0;

    private final String tailNumber;
    private boolean poweredOn;
    private boolean flying;

    public VendorDroneSdk(String tailNumber) {
        this.tailNumber = tailNumber;
    }

    public String tail() {
        return tailNumber;
    }

    public SdkStatus powerOn() {
        poweredOn = true;
        return new SdkStatus(OK, "powered on");
    }

    public SdkStatus liftOff(int heightFeet) {
        if (!poweredOn) {
            return new SdkStatus(ERR_NOT_READY, "vehicle not powered");
        }
        if (heightFeet > MAX_ALTITUDE_FEET) {
            return new SdkStatus(ERR_GEOFENCE, "ceiling exceeded");
        }
        flying = true;
        return new SdkStatus(OK, "airborne");
    }

    public SdkStatus navigate(double altFeet, double lon, double lat) {
        if (!flying) {
            return new SdkStatus(ERR_NOT_READY, "vehicle not airborne");
        }
        if (altFeet > MAX_ALTITUDE_FEET) {
            return new SdkStatus(ERR_GEOFENCE, "ceiling exceeded");
        }
        return new SdkStatus(OK, "arrived");
    }

    public SdkStatus firePayload(int channel) {
        if (!flying) {
            return new SdkStatus(ERR_NOT_READY, "vehicle not airborne");
        }
        if (channel < CH_SHUTTER || channel > CH_SPRAY_CLOSE) {
            return new SdkStatus(ERR_BAD_CHANNEL, "unknown channel " + channel);
        }
        return new SdkStatus(OK, "payload fired");
    }

    public SdkStatus goHome() {
        if (!flying) {
            return new SdkStatus(ERR_NOT_READY, "vehicle not airborne");
        }
        flying = false;
        return new SdkStatus(OK, "landed");
    }
}
