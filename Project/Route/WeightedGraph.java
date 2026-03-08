package Project.Route;

import java.util.ArrayList;
import Project.BusStation.BusStationClass;

public class WeightedGraph {
    int numStations = 0;

    ArrayList<BusStationClass> stations = new ArrayList<BusStationClass>();

    public void addStation(BusStationClass station) {
        stations.add(station);
        numStations++;

    }

    public void removeStation(BusStationClass station) {
        stations.remove(station);
        numStations--;
    }

    public void addEdge(BusStationClass from, BusStationClass to) {
        Edge newEdge = new Edge(from, to);
    }

    public void displayGraph() {
        for (BusStationClass station : stations) {
            station.displayInfo();
        }
    }
}
