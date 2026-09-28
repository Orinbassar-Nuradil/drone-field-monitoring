package fieldwatch.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the rule "the Abstraction depends only on the Implementor interface": no source file on
 * the mission side (or in the Implementor interface itself) may mention the adapted class, its
 * status type, its constants or the adapter.
 */
class BridgeIsolationTest {

    private static final List<String> FORBIDDEN =
            List.of("fieldwatch.vendor", "VendorDroneSdk", "SdkStatus", "VendorDroneAdapter", "ERR_", "CH_SHUTTER");

    @Test
    void missionSideNeverReferencesTheAdaptedClass() throws IOException {
        Path missionDir = Path.of("src/main/java/fieldwatch/mission");
        assertTrue(Files.isDirectory(missionDir), "run the tests from the project root");

        try (Stream<Path> files = Files.walk(missionDir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                assertNoForbiddenToken(file);
            }
        }
        assertNoForbiddenToken(Path.of("src/main/java/fieldwatch/control/FlightController.java"));
    }

    private static void assertNoForbiddenToken(Path file) throws IOException {
        String source = Files.readString(file);
        for (String token : FORBIDDEN) {
            assertTrue(!source.contains(token), file + " must not mention " + token);
        }
    }
}
