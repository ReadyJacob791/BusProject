package Project.Route;

import java.util.*;

/**
 * Provides route-finding and navigation utilities for the bus network graph.
 *
 * The two main responsibilities are:
 *   getShortestPath – finds the cheapest (shortest distance) path
 *       between two stations using Dijkstra's algorithm.
 *   calculateHeading – computes the compass bearing from one
 *       geographic coordinate to another using the forward-azimuth formula.
 * 
 */
public class RoutePlanner {

    /** The graph of stations and roads this planner operates on. */
    private WeightedGraph graph;

    // Constructor

    /**
     * Constructs a RoutePlanner backed by the supplied graph.
     *
     * graph The WeightedGraph to search for paths.
     */
    public RoutePlanner(WeightedGraph graph) {
        this.graph = graph;
    }

    // Pathfinding

    /**
     * Finds the shortest (minimum total distance) path between two Nodes using
     * Dijkstra's algorithm.
     *
     * Algorithm outline:
     *   Initialise every node's tentative distance to infinity, except the
     *       start node which gets distance 0.
     *   Use a PriorityQueue ordered by tentative distance so the
     *       closest unvisited node is always processed next.
     *   For each popped node, relax every outgoing edge: if going through
     *       the current node produces a shorter path to the neighbour, update
     *       the neighbour's distance and re-insert it into the queue.
     *   Stop early once the destination node is popped.
     *   Reconstruct the path by following the previous map
     *       backwards from the destination to the source.
     * 
     *
     * This method is public so the UI can call it per leg and report exactly
     * which station caused a "no path found" failure.
     *
     * start The Node to begin routing from.
     * end   The Node to route to.
     * return An ordered List of Nodes from start to end (both
     *         inclusive), or an empty list if no path exists.
     */
    public List<Node> getShortestPath(Node start, Node end) {
        // --- Initialisation ---

        // Maps each node to its current best-known distance from the start
        Map<Node, Double> distances = new HashMap<>();

        // Maps each node to the node that precedes it on the best-known path
        Map<Node, Node> previous = new HashMap<>();

        // Priority queue ordered by current tentative distance (smallest first)
        // The lambda reads from the live 'distances' map, so it always reflects
        // the latest relaxed values.
        PriorityQueue<Node> queue = new PriorityQueue<>(
                Comparator.comparing(distances::get));

        // Set every node's distance to "infinity" before we begin
        for (Node n : graph.vertices) {
            distances.put(n, Double.MAX_VALUE);
        }

        // The start node costs nothing to reach from itself
        distances.put(start, 0.0);
        queue.add(start);

        // --- Main loop ---
        while (!queue.isEmpty()) {
            // Pop the node with the smallest known distance
            Node current = queue.poll();

            // Early exit: we've found the optimal path to the destination
            if (current.equals(end)) break;

            // Relax every outgoing edge from the current node
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getTo();
                double newDist = distances.get(current) + edge.getWeight();

                if (newDist < distances.get(neighbor)) {
                    // Found a shorter path — update distance and predecessor
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);

                    // Re-insert with updated priority (PriorityQueue doesn't
                    // automatically resort, so we remove and re-add)
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Walk backwards from end to start using the 'previous' map
        List<Node> path = new ArrayList<>();
        Node curr = end;

        while (curr != null && previous.containsKey(curr)) {
            path.add(0, curr); // Prepend so the list ends up in forward order
            curr = previous.get(curr);
        }

        // If we reached the start node, a complete path exists — prepend it
        if (curr != null && curr.equals(start)) {
            path.add(0, start);
            return path;
        }

        // No path was found (graph is disconnected between start and end)
        return new ArrayList<>();
    }

    // Navigation / Heading

    /**
     * Calculates the initial compass bearing (forward azimuth) when travelling
     * from one geographic coordinate to another along a great-circle path.
     *
     * The result is formatted as "bearing° DIRECTION", e.g.
     * "45.0° NE".
     *
     * lat1 Latitude  of the departure point in decimal degrees.
     * lon1 Longitude of the departure point in decimal degrees.
     * lat2 Latitude  of the destination point in decimal degrees.
     * lon2 Longitude of the destination point in decimal degrees.
     * return A formatted string with the bearing and compass direction.
     */
    public String calculateHeading(double lat1, double lon1,
                                   double lat2, double lon2) {
        // Convert all coordinates from degrees to radians (required by Java's
        // trig functions)
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLon    = Math.toRadians(lon2 - lon1); // Longitude difference only

        // Forward-azimuth formula components:
        //   y = sin(Δlon) · cos(lat2)
        //   x = cos(lat1) · sin(lat2) − sin(lat1) · cos(lat2) · cos(Δlon)
        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad)
                 - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        // atan2 gives a bearing in the range −180° to +180°
        double heading = Math.toDegrees(Math.atan2(y, x));

        // Normalise to the conventional 0–360° clockwise-from-north range
        heading = (heading + 360) % 360;

        // Map the numeric bearing to a human-readable compass direction
        String compassDirection = getCompassDirection(heading);
        return String.format("%.1f° %s", heading, compassDirection);
    }

    /**
     * Maps a numeric bearing in degrees (0–360) to one of eight compass
     * abbreviations: N, NE, E, SE, S, SW, W, NW.
     *
     * Each sector is 45° wide and centred on its cardinal/intercardinal point.
     * The final "N" at index 8 handles the wrap-around case where rounding
     * a bearing close to 360° maps to index 8.
     *
     * heading A compass bearing in degrees, normalised to 0–360.
     * return The matching compass direction string.
     */
    private String getCompassDirection(double heading) {
        // Directions in clockwise order starting from North (0°)
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};

        // Divide the 360° circle into 8 equal 45° sectors; round to nearest
        return directions[(int) Math.round((heading % 360) / 45)];
    }
}
