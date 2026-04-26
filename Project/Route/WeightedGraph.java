package Project.Route;

import Project.BusStation.BusStationClass;
import Project.BusStation.BusStationManager;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * An undirected weighted graph of bus stations and the roads between them.
 *
 * Vertices are Node objects (one per station).  Edges are stored
 * directly on each Node as directed Edge objects; because every
 * connection is bi-directional, every logical road appears as two directed
 * edges (A→B and B→A).
 *
 * Persistence is handled through two CSV files:
 *   BusStation.csv  – read by BusStationManager}, passed in via
 *       buildGraphFromCSV}.
 *   WeightedGraph.csv – one "from, to" pair per line; read and written
 *       by this class.
 */
public class WeightedGraph {

    /**
     * The complete list of vertices (station Nodes) in the graph.
     * Kept public so the UI's GraphPanel can iterate over it directly for
     * rendering without needing a getter.
     */
    public ArrayList<Node> vertices = new ArrayList<>();

    // Graph Mutation Methods

    /**
     * Adds a new vertex to the graph for the given bus station.
     * Called both when building the graph from CSV and when a new station is
     * created through the Station Manager UI.
     *
     * station The BusStationClass} to wrap in a new Node}.
     */
    public void addVertex(BusStationClass station) {
        vertices.add(new Node(station));
    }

    /**
     * Creates an undirected (two-way) connection between two existing Nodes by
     * adding a directed edge in each direction and registering each Node in the
     * other's neighbour list.
     *
     * v1 One endpoint of the new connection.
     * v2 The other endpoint of the new connection.
     */
    public void addEdge(Node v1, Node v2) {
        // Create and register the v1 → v2 directed edge
        Edge e1 = new Edge(v1, v2);
        v1.getEdges().add(e1);
        v1.getNeighbors().add(v2);

        // Create and register the v2 → v1 directed edge (making it undirected)
        Edge e2 = new Edge(v2, v1);
        v2.getEdges().add(e2);
        v2.getNeighbors().add(v1);
    }

    /**
     * Severs the two-way connection between two Nodes by removing the directed
     * edges in both directions and updating both neighbour lists.
     * If either Node is null the call is silently ignored.
     *
     * n1 One endpoint of the connection to remove.
     * n2 The other endpoint of the connection to remove.
     */
    public void removeEdge(Node n1, Node n2) {
        // Guard against null references (e.g. a station was already deleted)
        if (n1 == null || n2 == null) return;

        // Remove the n1 → n2 edge and the n2 neighbour entry from n1
        n1.getEdges().removeIf(e -> e.getTo().equals(n2));
        n1.getNeighbors().remove(n2);

        // Remove the n2 → n1 edge and the n1 neighbour entry from n2
        n2.getEdges().removeIf(e -> e.getTo().equals(n1));
        n2.getNeighbors().remove(n1);
    }

    /**
     * Completely removes a Node from the graph, severing every edge that
     * connects it to its neighbours before deleting the Node itself.
     * If n is null the call is silently ignored.
     *
     * n The Node to remove.
     */
    public void removeNode(Node n) {
        if (n == null) return;

        // For every neighbour of n, remove the edge back to n and
        // remove n from the neighbour's own neighbour list
        for (Node neighbor : n.getNeighbors()) {
            neighbor.getEdges().removeIf(e -> e.getTo().equals(n));
            neighbor.getNeighbors().remove(n);
        }

        // Clear n's own edge and neighbour data, then remove it from the graph
        n.getEdges().clear();
        n.getNeighbors().clear();
        vertices.remove(n);
    }

    // Lookup

    /**
     * Searches for a Node by station name (case-insensitive, trims whitespace).
     *
     * name The display name of the station to find.
     * return The matching Node, or null} if not found.
     */
    public Node getNodeByName(String name) {
        for (Node n : vertices) {
            if (n.getStation().getName().equalsIgnoreCase(name.trim())) {
                return n;
            }
        }
        return null; // No matching station found
    }

    // CSV I/O

    /**
     * Builds the graph from existing data.
     *
     * Step 1 – Iterates over every station in isManager and creates a
     *          corresponding vertex.
     * Step 2 – Reads csvPath (WeightedGraph.csv) line by line.  Each
     *          line is expected to have the format:
     *          "Station A", "Station B"
     *          An undirected edge is created between the two named stations.
     *          Duplicate edges (both A→B and B→A listed) are ignored.
     *
     * sManager The manager whose stationList} provides the vertices.
     * csvPath  Path to the CSV file that defines the edges.
     */
    public void buildGraphFromCSV(BusStationManager sManager, String csvPath) {
        // --- Step 1: create all vertices ---
        for (BusStationClass station : sManager.stationList) {
            addVertex(station);
        }

        // --- Step 2: read edges from the CSV ---
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Strip surrounding quotes and leading/trailing whitespace
                line = line.replace("\"", "").trim();
                String[] parts = line.split(",\\s*");

                // Skip malformed lines (need at least two station names)
                if (parts.length < 2) continue;

                Node n1 = getNodeByName(parts[0]);
                Node n2 = getNodeByName(parts[1]);

                // Skip if either station name isn't in the vertex list
                if (n1 == null || n2 == null) continue;

                // Prevent duplicate undirected edges if the CSV lists both
                // A→B and B→A as separate lines
                boolean alreadyConnected = false;
                for (Edge e : n1.getEdges()) {
                    if (e.getTo().equals(n2)) {
                        alreadyConnected = true;
                        break;
                    }
                }

                if (!alreadyConnected) {
                    addEdge(n1, n2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends a single new edge entry to the CSV without overwriting existing
     * entries.  A leading newline is printed first so the new entry always
     * starts on its own line even if the file previously had no trailing newline.
     *
     * fromName The name of the origin station.
     * toName   The name of the destination station.
     * csvPath  Path to the CSV file to append to.
     */
    public void appendEdgeToCSV(String fromName, String toName, String csvPath) {
        // Open in append mode (true) so existing edges are preserved
        try (java.io.FileWriter fw = new java.io.FileWriter(csvPath, true);
             java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {

            // Leading \n ensures a fresh line even if the file had no trailing newline
            pw.print("\n\"" + fromName + "\", \"" + toName + "\"");

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overwrites the CSV file with the current in-memory graph state.
     * Called after a connection is removed so that the file stays consistent
     * with the live graph.
     *
     * Each undirected edge is written only once (whichever station name sorts
     * first alphabetically is used as the "from" end) to avoid duplicate lines.
     *
     * csvPath Path to the CSV file to overwrite.
     */
    public void rewriteCSV(String csvPath) {
        // Open with append=false to overwrite the entire file
        try (java.io.FileWriter fw = new java.io.FileWriter(csvPath, false);
             java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {

            // Track which edges have already been written to prevent duplicates
            java.util.Set<String> writtenEdges = new java.util.HashSet<>();

            for (Node n : vertices) {
                for (Edge e : n.getEdges()) {
                    String from = n.getStation().getName();
                    String to   = e.getTo().getStation().getName();

                    // Build a canonical key: alphabetically smaller name comes first
                    // so "A-B" and "B-A" map to the same key
                    String edgeKey = from.compareTo(to) < 0
                            ? from + "-" + to
                            : to   + "-" + from;

                    if (!writtenEdges.contains(edgeKey)) {
                        pw.println("\"" + from + "\", \"" + to + "\"");
                        writtenEdges.add(edgeKey);
                    }
                }
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    // Distance Utility

    /**
     * Calculates the great-circle (Haversine) distance between two stations
     * in statute miles.
     *
     * This method is available as a standalone utility for any caller that needs
     * an accurate mile distance without going through the Edge constructor.
     *
     * s1 The first  BusStationClass.
     * s2 The second BusStationClass.
     * return The surface distance between the two stations in miles.
     */
    public double calculateDistanceMiles(BusStationClass s1, BusStationClass s2) {
        // Mean radius of the Earth in statute miles
        final int R = 3958;

        // Convert degree differences to radians
        double latDistance = Math.toRadians(s2.getLatitude()  - s1.getLatitude());
        double lonDistance = Math.toRadians(s2.getLongitude() - s1.getLongitude());

        // Haversine intermediate value 'a'
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(s1.getLatitude()))
                * Math.cos(Math.toRadians(s2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        // Central angle
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in miles
    }
}
