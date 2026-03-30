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
}