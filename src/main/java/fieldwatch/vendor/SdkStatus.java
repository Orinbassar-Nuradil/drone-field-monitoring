package fieldwatch.vendor;

/** Result object returned by every {@link VendorDroneSdk} call (third-party class, do not modify). */
public final class SdkStatus {

    private final int code;
    private final String detail;

    public SdkStatus(int code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public int code() {
        return code;
    }

    public String detail() {
        return detail;
    }

    public boolean isOk() {
        return code == VendorDroneSdk.OK;
    }
}
