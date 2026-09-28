package fieldwatch.app;

import fieldwatch.mission.MissionReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Command line client. Reads one mission request per line (from the arguments or from standard
 * input) and prints the report. It has no idea which implementor a request will use.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        MissionLauncher launcher = DefaultWiring.defaultLauncher();
        if (args.length > 0) {
            handle(launcher, String.join(" ", args));
            return;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.isBlank() && !line.trim().startsWith("#")) {
                    handle(launcher, line);
                }
            }
        }
    }

    private static void handle(MissionLauncher launcher, String line) {
        try {
            MissionReport report = launcher.launch(MissionRequest.parse(line));
            System.out.println(report);
        } catch (IllegalArgumentException | UnknownSelectionException e) {
            System.out.println("REJECTED: " + e.getMessage());
        }
    }
}
