package Project.Route;

/**
 * Represents a directed connection (edge) between two Nodes in the route graph.
 *
 * Each Edge stores the two endpoint Nodes and the real-world distance between
 * them in miles, calculated using the Haversine formula.
 *
 * BUG FIX: The original implementation used a simple Euclidean distance on raw
 * latitude/longitude degree values (sqrt(dLat^2 + dLon^2)).  That formula
 * produces a unit-less degree-distance, not miles, so every distance displayed
 * in the UI as "miles" was wrong.  Replaced with the Haversine formula, which
 * correctly accounts for the curvature of the Earth and returns statute miles.
 */
public class Edge {

    /** The real-world distance between the two stations, in miles. */
    private double weight;

    /** The origin Node of this directed edge. */
    private Node from;

    /** The destination Node of this directed edge. */
    private Node to;

    // Constructor

    /**
     * Constructs an Edge from from to to and computes the
     * great-circle distance between the two stations using the Haversine formula.
     *
     * from The origin Node.
     * to   The destination Node.
     */
    public Edge(Node from, Node to) {
        this.from = from;
        this.to   = to;
        this.weight = haversineDistanceMiles(
                from.getStation().getLatitude(),
                from.getStation().getLongitude(),
                to.getStation().getLatitude(),
                to.getStation().getLongitude()
        );
    }

    // Distance helper

    /**
     * Calculates the great-circle distance between two geographic coordinates
     * using the Haversine formula.
     *
     * The Haversine formula accounts for the spherical shape of the Earth and
     * returns an accurate surface distance, unlike a simple Euclidean calculation
     * on raw degree values.
     *
     * Formula:
     *   a = sin²(Δlat/2) + cos(lat1) · cos(lat2) · sin²(Δlon/2)
     *   c = 2 · atan2(√a, √(1−a))
     *   d = R · c          (R = 3,958.8 miles, mean Earth radius)
     *
     * lat1 Latitude  of the origin station in decimal degrees.
     * lon1 Longitude of the origin station in decimal degrees.
     * lat2 Latitude  of the destination station in decimal degrees.
     * lon2 Longitude of the destination station in decimal degrees.
     * return The distance between the two points in statute miles.
     */
    private double haversineDistanceMiles(double lat1, double lon1,
                                          double lat2, double lon2) {
        // Mean radius of the Earth in statute miles
        final double EARTH_RADIUS_MILES = 3958.8;

        // Convert degree differences to radians for trig functions
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Convert the absolute latitudes to radians for the cosine terms
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        // Haversine intermediate value 'a'
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        // Central angle 'c' between the two points
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Multiply central angle by Earth's radius to get distance in miles
        return EARTH_RADIUS_MILES * c;
    }

    // Getters

    /**
     * Returns the distance (weight) of this edge in miles.
     *
     * return Distance in statute miles.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Returns the origin Node of this edge.
     *
     * return The Node where this edge starts.
     */
    public Node getFrom() {
        return from;
    }

    /**
     * Returns the destination Node of this edge.
     *
     * return The Node where this edge ends.
     */
    public Node getTo() {
        return to;
    }
}
