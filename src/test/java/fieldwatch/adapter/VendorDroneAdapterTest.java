package fieldwatch.adapter;

import fieldwatch.control.FailureReason;
import fieldwatch.control.FlightController;
import fieldwatch.control.FlightException;
import fieldwatch.control.PayloadCommand;
import fieldwatch.geo.GeoPoint;
import fieldwatch.testsupport.ScriptedSdk;
import fieldwatch.vendor.SdkStatus;
import fieldwatch.vendor.VendorDroneSdk;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VendorDroneAdapterTest {

    private static final String LEAKY_DETAIL = "vendor-internal detail";

    // ---- conversion (success path) ----

    @Test
    void convertsMetersToFeetAndReordersArguments() throws FlightException {
        ScriptedSdk sdk = new ScriptedSdk();
        FlightController adapter = new VendorDroneAdapter(sdk);

        adapter.arm();
        adapter.takeOff(60.0);
        adapter.goTo(new GeoPoint(48.1, 71.4), 10.0);
        adapter.returnToHome();

        assertEquals(List.of(
                "powerOn",
                "liftOff:197",
                "navigate:32.8084,71.4000,48.1000",   // (altitude in feet, lon, lat)
                "goHome"), sdk.calls);
    }

    @Test
    void mapsPayloadCommandsToVendorChannels() throws FlightException {
        ScriptedSdk sdk = new ScriptedSdk();
        FlightController adapter = new VendorDroneAdapter(sdk);

        adapter.triggerPayload(PayloadCommand.CAPTURE_PHOTO);
        adapter.triggerPayload(PayloadCommand.SPRAY_ON);
        adapter.triggerPayload(PayloadCommand.SPRAY_OFF);

        assertEquals(List.of("firePayload:1", "firePayload:2", "firePayload:3"), sdk.calls);
    }

    @Test
    void identifiesItselfWithoutExposingSdkTypes() {
        assertEquals("vendor:T-1", new VendorDroneAdapter(new ScriptedSdk()).name());
    }

    // ---- failure translation ----

    @Test
    void translatesEveryVendorStatusCodeToTheContractReason() {
        Map<Integer, FailureReason> expected = new LinkedHashMap<>();
        expected.put(VendorDroneSdk.ERR_LINK_TIMEOUT, FailureReason.LINK_LOST);
        expected.put(VendorDroneSdk.ERR_BATTERY_CRITICAL, FailureReason.BATTERY_LOW);
        expected.put(VendorDroneSdk.ERR_GEOFENCE, FailureReason.OUT_OF_BOUNDS);
        expected.put(VendorDroneSdk.ERR_PAYLOAD_JAM, FailureReason.PAYLOAD_FAULT);
        expected.put(VendorDroneSdk.ERR_BAD_CHANNEL, FailureReason.COMMAND_REJECTED);
        expected.put(VendorDroneSdk.ERR_NOT_READY, FailureReason.COMMAND_REJECTED);
        expected.put(999, FailureReason.UNKNOWN);

        for (Map.Entry<Integer, FailureReason> entry : expected.entrySet()) {
            ScriptedSdk sdk = new ScriptedSdk()
                    .answer("navigate", new SdkStatus(entry.getKey(), LEAKY_DETAIL));
            FlightController adapter = new VendorDroneAdapter(sdk);

            FlightException thrown = assertThrows(FlightException.class,
                    () -> adapter.goTo(new GeoPoint(48.1, 71.4), 10.0));

            assertEquals(entry.getValue(), thrown.reason());
        }
    }

    @Test
    void translatesFailuresOfEveryOperation() {
        SdkStatus failure = new SdkStatus(VendorDroneSdk.ERR_LINK_TIMEOUT, LEAKY_DETAIL);
        ScriptedSdk sdk = new ScriptedSdk()
                .answer("powerOn", failure).answer("liftOff", failure).answer("navigate", failure)
                .answer("firePayload", failure).answer("goHome", failure);
        FlightController adapter = new VendorDroneAdapter(sdk);

        List<org.junit.jupiter.api.function.Executable> operations = List.of(
                adapter::arm,
                () -> adapter.takeOff(30),
                () -> adapter.goTo(new GeoPoint(48.1, 71.4), 30),
                () -> adapter.triggerPayload(PayloadCommand.SPRAY_ON),
                adapter::returnToHome);

        for (org.junit.jupiter.api.function.Executable operation : operations) {
            FlightException thrown = assertThrows(FlightException.class, operation);
            assertEquals(FailureReason.LINK_LOST, thrown.reason());
        }
    }

    @Test
    void doesNotLeakVendorTextOrCodesInTheMessage() {
        ScriptedSdk sdk = new ScriptedSdk()
                .answer("liftOff", new SdkStatus(VendorDroneSdk.ERR_BATTERY_CRITICAL, LEAKY_DETAIL));
        FlightController adapter = new VendorDroneAdapter(sdk);

        FlightException thrown = assertThrows(FlightException.class, () -> adapter.takeOff(30));

        assertFalse(thrown.getMessage().contains(LEAKY_DETAIL));
        assertFalse(thrown.getMessage().contains("23"));
        assertTrue(thrown.getMessage().contains("takeOff"));
    }

    @Test
    void treatsANullStatusAsUnknownFailure() {
        FlightController adapter = new VendorDroneAdapter(new ScriptedSdk().answerNull("powerOn"));

        FlightException thrown = assertThrows(FlightException.class, adapter::arm);

        assertEquals(FailureReason.UNKNOWN, thrown.reason());
    }

    @Test
    void wrapsUnexpectedSdkRuntimeExceptionsInsteadOfLettingThemEscape() {
        IllegalStateException sdkBug = new IllegalStateException("boom inside vendor code");
        FlightController adapter = new VendorDroneAdapter(new ScriptedSdk().throwOn("navigate", sdkBug));

        FlightException thrown = assertThrows(FlightException.class,
                () -> adapter.goTo(new GeoPoint(48.1, 71.4), 10.0));

        assertEquals(FailureReason.UNKNOWN, thrown.reason());
        assertFalse(thrown.getMessage().contains("boom"));
    }
}
