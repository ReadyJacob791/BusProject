package Project.BusStation;

public class BusStationClass {
    int Key = 0;
    String name;
    double latitude;
    double longitude;

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

    public String getAbrevation() {
        String[] splitName = name.split(" ");
        String abreviation = "";
        for (String word : splitName) {
            abreviation += word.charAt(0);
        }
        return abreviation;
    }

    public String displayStationInfo() {
        String info = name + ", "
                + latitude + ", "
                + longitude;
        return info;
    }
}