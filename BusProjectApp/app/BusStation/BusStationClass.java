package Project.BusStation;

public class BusStationClass {
    int Key = 0;
    String name = "Name";
    double latitude = 0;
    double longitude = 0;

    public BusStationClass() {

    }

    public BusStationClass(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String displayStationInfo() {
        String info = name + ", "
                + latitude + ", "
                + longitude + ", "
                + "false";
        return info;
    }
}