package Project.Route;

import Project.BusStation.BusStationClass;

public class Edge {
    double weight;
    BusStationClass from;
    BusStationClass to;

    public Edge() {

    }

    public Edge(BusStationClass from, BusStationClass to) {
        this.weight = (from.getLatitude() - to.getLatitude()) + (from.getLongitude() - to.getLongitude());
        this.from = from;
        this.to = to;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public BusStationClass getFrom() {
        return from;
    }

    public void setFrom(BusStationClass from) {
        this.from = from;
    }

    public BusStationClass getTo() {
        return to;
    }

    public void setTo(BusStationClass to) {
        this.to = to;
    }
}
