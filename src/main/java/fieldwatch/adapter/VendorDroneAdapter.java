package fieldwatch.adapter;

import fieldwatch.control.FailureReason;
import fieldwatch.control.FlightController;
import fieldwatch.control.FlightException;
import fieldwatch.control.PayloadCommand;
import fieldwatch.geo.GeoPoint;
import fieldwatch.vendor.SdkStatus;
import fieldwatch.vendor.VendorDroneSdk;

import java.util.function.Supplier;

/**
 * Adapter: brings {@link VendorDroneSdk} into the {@link FlightController} role without touching
 * the SDK. It converts names, argument order, units (meters to feet), payload commands to channel
 * numbers, and translates every status code (or unexpected runtime exception) into a
 * {@link FlightException}. Nothing vendor-specific (codes, texts, types) leaves this class.
 */
public final class VendorDroneAdapter implements FlightController {

    private static final double FEET_PER_METER = 3.280839895;

    private final VendorDroneSdk sdk;

    public VendorDroneAdapter(VendorDroneSdk sdk) {
        this.sdk = sdk;
    }

    @Override
    public String name() {
        return "vendor:" + sdk.tail();
    }

    @Override
    public void arm() throws FlightException {
        execute("arm", sdk::powerOn);
    }

    @Override
    public void takeOff(double altitudeMeters) throws FlightException {
        int feet = (int) Math.round(altitudeMeters * FEET_PER_METER);
        execute("takeOff", () -> sdk.liftOff(feet));
    }

    @Override
    public void goTo(GeoPoint target, double altitudeMeters) throws FlightException {
        double feet = altitudeMeters * FEET_PER_METER;
        execute("goTo", () -> sdk.navigate(feet, target.longitude(), target.latitude()));
    }

    @Override
    public void triggerPayload(PayloadCommand command) throws FlightException {
        int channel = channelFor(command);
        execute("triggerPayload", () -> sdk.firePayload(channel));
    }

    @Override
    public void returnToHome() throws FlightException {
        execute("returnToHome", sdk::goHome);
    }

    private static int channelFor(PayloadCommand command) {
        return switch (command) {
            case CAPTURE_PHOTO -> VendorDroneSdk.CH_SHUTTER;
            case SPRAY_ON -> VendorDroneSdk.CH_SPRAY_OPEN;
            case SPRAY_OFF -> VendorDroneSdk.CH_SPRAY_CLOSE;
        };
    }

    private void execute(String operation, Supplier<SdkStatus> call) throws FlightException {
        SdkStatus status;
        try {
            status = call.get();
        } catch (RuntimeException e) {
            throw new FlightException(FailureReason.UNKNOWN, operation + " failed: " + FailureReason.UNKNOWN, e);
        }
        if (status == null) {
            throw new FlightException(FailureReason.UNKNOWN, operation + " failed: " + FailureReason.UNKNOWN);
        }
        if (!status.isOk()) {
            FailureReason reason = reasonFor(status.code());
            throw new FlightException(reason, operation + " failed: " + reason);
        }
    }

    private static FailureReason reasonFor(int vendorCode) {
        return switch (vendorCode) {
            case VendorDroneSdk.ERR_LINK_TIMEOUT -> FailureReason.LINK_LOST;
            case VendorDroneSdk.ERR_BATTERY_CRITICAL -> FailureReason.BATTERY_LOW;
            case VendorDroneSdk.ERR_GEOFENCE -> FailureReason.OUT_OF_BOUNDS;
            case VendorDroneSdk.ERR_PAYLOAD_JAM -> FailureReason.PAYLOAD_FAULT;
            case VendorDroneSdk.ERR_BAD_CHANNEL, VendorDroneSdk.ERR_NOT_READY -> FailureReason.COMMAND_REJECTED;
            default -> FailureReason.UNKNOWN;
        };
    }
}
