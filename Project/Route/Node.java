package Project.Route;

import Project.BusStation.BusStationClass;
import java.util.ArrayList;

public class Node {
    private BusStationClass station;
    private ArrayList<Node> neighbors = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();

    public Node(BusStationClass station) {
        this.station = station;
    }

    public BusStationClass getStation() {
        return station;
    }

    public ArrayList<Node> getNeighbors() {
        return neighbors;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}