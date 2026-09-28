package fieldwatch.testsupport;

import fieldwatch.vendor.SdkStatus;
import fieldwatch.vendor.VendorDroneSdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Test stub for the adapted class: subclasses the third-party SDK (which stays unmodified) and
 * answers each call with a scripted status, so failure translation can be tested exactly.
 */
public class ScriptedSdk extends VendorDroneSdk {

    public final List<String> calls = new ArrayList<>();
    private final Map<String, SdkStatus> scripted = new HashMap<>();
    private final Map<String, RuntimeException> thrown = new HashMap<>();

    public ScriptedSdk() {
        super("T-1");
    }

    public ScriptedSdk answer(String method, SdkStatus status) {
        scripted.put(method, status);
        return this;
    }

    public ScriptedSdk answerNull(String method) {
        scripted.put(method, null);
        return this;
    }

    public ScriptedSdk throwOn(String method, RuntimeException exception) {
        thrown.put(method, exception);
        return this;
    }

    private SdkStatus respond(String method, String description) {
        calls.add(description);
        if (thrown.containsKey(method)) {
            throw thrown.get(method);
        }
        if (scripted.containsKey(method)) {
            return scripted.get(method);
        }
        return new SdkStatus(OK, "ok");
    }

    @Override
    public SdkStatus powerOn() {
        return respond("powerOn", "powerOn");
    }

    @Override
    public SdkStatus liftOff(int heightFeet) {
        return respond("liftOff", "liftOff:" + heightFeet);
    }

    @Override
    public SdkStatus navigate(double altFeet, double lon, double lat) {
        return respond("navigate", String.format(Locale.ROOT, "navigate:%.4f,%.4f,%.4f", altFeet, lon, lat));
    }

    @Override
    public SdkStatus firePayload(int channel) {
        return respond("firePayload", "firePayload:" + channel);
    }

    @Override
    public SdkStatus goHome() {
        return respond("goHome", "goHome");
    }
}
