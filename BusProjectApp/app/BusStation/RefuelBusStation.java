package Project.BusStation;

public class RefuelBusStation extends BusStationClass {

    public RefuelBusStation(String name, double latitude, double longitude) {
        super(name, latitude, longitude);
    }

    @Override
    public String displayStationInfo() {
        String info = name + ", "
                + latitude + ", "
                + longitude + ", "
                + "True";
        return info;
    }
}