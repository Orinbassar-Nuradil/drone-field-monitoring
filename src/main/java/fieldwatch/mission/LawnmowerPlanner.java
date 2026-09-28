package fieldwatch.mission;

import fieldwatch.geo.FieldArea;
import fieldwatch.geo.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/** Plans parallel east-west lanes ("lawnmower" pattern) that cover a rectangular field. */
public final class LawnmowerPlanner {

    private LawnmowerPlanner() {
    }

    public static List<GeoPoint> plan(FieldArea area, double swathMeters) {
        if (swathMeters <= 0) {
            throw new IllegalArgumentException("Swath width must be positive");
        }
        int lanes = Math.max(1, (int) Math.ceil(area.heightMeters() / swathMeters));
        double south = area.southWest().latitude();
        double laneStep = (area.northEast().latitude() - south) / lanes;
        double west = area.southWest().longitude();
        double east = area.northEast().longitude();

        List<GeoPoint> route = new ArrayList<>();
        for (int lane = 0; lane < lanes; lane++) {
            double latitude = south + (lane + 0.5) * laneStep;
            boolean eastbound = lane % 2 == 0;
            route.add(new GeoPoint(latitude, eastbound ? west : east));
            route.add(new GeoPoint(latitude, eastbound ? east : west));
        }
        return route;
    }
}
