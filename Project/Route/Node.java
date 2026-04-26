package Project.Route;

import Project.BusStation.BusStationClass;
import java.util.ArrayList;

/**
 * Represents a single vertex (stop) in the route graph.
 *
 * Each Node wraps a BusStationClass and maintains two parallel
 * collections:
 *   neighbors – the adjacent Nodes reachable from this Node.
 *   edges     – the directed Edge objects that connect
 *                           this Node to each neighbour; each Edge stores the
 *                           distance weight.

 * Both lists are kept in sync by WeightedGraphaddEdge and
 * WeightedGraphremoveEdge.
 */
public class Node {

    /** The bus station data (name, latitude, longitude) this Node represents. */
    private BusStationClass station;

    /**
     * Direct references to every Node that can be reached in one hop from
     * this Node.  Used for fast neighbour lookups (e.g. when severing edges).
     */
    private ArrayList<Node> neighbors = new ArrayList<>();

    /**
     * The outgoing edges from this Node.  Each Edge points to one neighbour
     * and stores the distance in miles.  Dijkstra's algorithm iterates over
     * this list when exploring paths.
     */
    private ArrayList<Edge> edges = new ArrayList<>();

    // Constructor

    /**
     * Constructs a Node that represents the given bus station.
     *
     * station The BusStationClass this Node wraps.
     */
    public Node(BusStationClass station) {
        this.station = station;
    }

    // Getters and Setters

    /**
     * Returns the bus station associated with this Node.
     *
     * return The wrapped BusStationClass.
     */
    public BusStationClass getStation() {
        return station;
    }

    /**
     * Replaces the bus station associated with this Node.
     * Called by the Station Manager UI when a station's details are edited.
     *
     * station The updated BusStationClass object.
     */
    public void setStation(BusStationClass station) {
        this.station = station;
    }

    /**
     * Returns the list of neighbouring Nodes reachable from this Node.
     *
     * return A mutable ArrayList of adjacent Nodes.
     */
    public ArrayList<Node> getNeighbors() {
        return neighbors;
    }

    /**
     * Returns the list of outgoing edges from this Node.
     * Each Edge holds a reference to its destination Node and the
     * Haversine-calculated distance weight in miles.
     *
     * return A mutable ArrayList of outgoing edges.
     */
    public ArrayList<Edge> getEdges() {
        return edges;
    }

    /**
     * Appends a directed edge to this Node's edge list.
     * Note: WeightedGraphaddEdge is the preferred way to connect two
     * Nodes because it creates edges in both directions and updates both
     * neighbour lists simultaneously.
     *
     * edge The Edge to add.
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
