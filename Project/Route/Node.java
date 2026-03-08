package Project.Route;

import Project.BusStation.BusStationClass;
import java.util.ArrayList;

public class Node {
    BusStationClass station;
    ArrayList<Node> neighbors = new ArrayList<Node>();
    ArrayList<Edge> edges = new ArrayList<Edge>();

    public Node() {

    }

    public Node(BusStationClass station) {
        this.station = station;
    }

    public BusStationClass getStation() {
        return station;
    }

    public void setStation(BusStationClass station) {
        this.station = station;
    }

    public ArrayList<Node> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Node> neighbors) {
        this.neighbors = neighbors;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
