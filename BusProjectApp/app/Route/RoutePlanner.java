package Project.Route;

import java.util.*;

public class RoutePlanner {
    private WeightedGraph graph;

    public RoutePlanner(WeightedGraph graph) {
        this.graph = graph;
    }

    // Expose this as public so the UI can calculate leg-by-leg and report errors
    public List<Node> getShortestPath(Node start, Node end) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparing(distances::get));

        for (Node n : graph.vertices) {
            distances.put(n, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(end))
                break;

            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getTo();
                double newDist = distances.get(current) + edge.getWeight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        List<Node> path = new ArrayList<>();
        Node curr = end;
        while (curr != null && previous.containsKey(curr)) {
            path.add(0, curr);
            curr = previous.get(curr);
        }

        // If the start node is added, a path exists.
        if (curr != null && curr.equals(start)) {
            path.add(0, start);
            return path;
        }

        return new ArrayList<>(); // Empty list means no path found
    }
    public String calculateHeading(double lat1, double lon1, double lat2, double lon2) {
        // Convert coordinates to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);
        
        double dLon = lon2Rad - lon1Rad;

        // Calculate forward azimuth
        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double heading = Math.toDegrees(Math.atan2(y, x));
        
        // Normalize to 0-360 degrees
        heading = (heading + 360) % 360;

        String compassDirection = getCompassDirection(heading);
        return String.format("%.1f° %s", heading, compassDirection);
    }

    private String getCompassDirection(double heading) {
        // Correct clockwise order: N (0), NE (45), E (90), SE (135), S (180), SW (225), W (270), NW (315)
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        return directions[(int) Math.round((heading % 360) / 45)];
    }
}