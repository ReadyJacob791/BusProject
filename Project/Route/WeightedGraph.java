package Project.Route;

import Project.BusStation.BusStationClass;
import Project.BusStation.BusStationManager;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WeightedGraph {
    public ArrayList<Node> vertices = new ArrayList<>();

    public void addVertex(Node v) {
        vertices.add(v);
    }

    // Creates an undirected (two-way) connection
    public void addEdge(Node v1, Node v2) {
        Edge e1 = new Edge(v1, v2);
        v1.getEdges().add(e1);
        v1.getNeighbors().add(v2);

        Edge e2 = new Edge(v2, v1);
        v2.getEdges().add(e2);
        v2.getNeighbors().add(v1);
    }

    public Node getNodeByName(String name) {
        for (Node n : vertices) {
            if (n.getStation().getName().equalsIgnoreCase(name.trim())) {
                return n;
            }
        }
        return null;
    }

    public void buildGraphFromCSV(BusStationManager sManager, String csvPath) {
        // 1. Populate all vertices
        for (BusStationClass station : sManager.stationList) {
            addVertex(new Node(station));
        }

        // 2. Read the CSV to build edges
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\"", "").trim();
                String[] parts = line.split(",\\s*");

                if (parts.length >= 2) {
                    Node n1 = getNodeByName(parts[0]);
                    Node n2 = getNodeByName(parts[1]);

                    if (n1 != null && n2 != null) {
                        // Prevent duplicate undirected edges if CSV lists both A->B and B->A
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendEdgeToCSV(String fromName, String toName, String csvPath) {
        try (java.io.FileWriter fw = new java.io.FileWriter(csvPath, true);
                java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {

            // The \n ensures that even if the file didn't end with a line break,
            // the new connection is pushed to a fresh line.
            pw.print("\n\"" + fromName + "\", \"" + toName + "\"");

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // 1. Removes the two-way connection between two nodes
    public void removeEdge(Node n1, Node n2) {
        if (n1 == null || n2 == null)
            return;

        n1.getEdges().removeIf(e -> e.getTo().equals(n2));
        n1.getNeighbors().remove(n2);

        n2.getEdges().removeIf(e -> e.getTo().equals(n1));
        n2.getNeighbors().remove(n1);
    }

    // 2. Completely removes a node and severs all ties from its neighbors
    public void removeNode(Node n) {
        if (n == null)
            return;

        for (Node neighbor : n.getNeighbors()) {
            neighbor.getEdges().removeIf(e -> e.getTo().equals(n));
            neighbor.getNeighbors().remove(n);
        }
        n.getEdges().clear();
        n.getNeighbors().clear();
        vertices.remove(n);
    }

    // 3. Wipes the CSV and rewrites it based on the current graph memory
    public void rewriteCSV(String csvPath) {
        try (java.io.FileWriter fw = new java.io.FileWriter(csvPath, false); // false = overwrite
                java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {

            java.util.Set<String> writtenEdges = new java.util.HashSet<>();

            for (Node n : vertices) {
                for (Edge e : n.getEdges()) {
                    String from = n.getStation().getName();
                    String to = e.getTo().getStation().getName();

                    // Create an alphabetical key so A->B and B->A aren't printed twice
                    String edgeKey = from.compareTo(to) < 0 ? from + "-" + to : to + "-" + from;

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

    public double calculateDistanceMiles(BusStationClass s1, BusStationClass s2) {
        final int R = 3958; // Radius of the earth in miles

        double latDistance = Math.toRadians(s2.getLatitude() - s1.getLatitude());
        double lonDistance = Math.toRadians(s2.getLongitude() - s1.getLongitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(s1.getLatitude())) * Math.cos(Math.toRadians(s2.getLatitude()))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Returns distance in miles
    }
}