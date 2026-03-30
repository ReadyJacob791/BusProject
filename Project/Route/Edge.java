package Project.Route;

public class Edge {
    private double weight;
    private Node from;
    private Node to;

    public Edge(Node from, Node to) {
        this.from = from;
        this.to = to;

        // Euclidean Distance calculation
        double latDiff = from.getStation().getLatitude() - to.getStation().getLatitude();
        double lonDiff = from.getStation().getLongitude() - to.getStation().getLongitude();
        this.weight = Math.sqrt((latDiff * latDiff) + (lonDiff * lonDiff));
    }

    public double getWeight() {
        return weight;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }
}