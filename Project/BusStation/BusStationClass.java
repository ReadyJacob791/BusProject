package Project.BusStation;

/**
 * Represents a single bus station with a name and geographic coordinates.
 *
 * This is the base station class.  If a station also serves as a refuelling
 * point, use the RefuelBusStation subclass instead — it overrides
 * #displayStationInfo to write "True" in the refuel column.
 *
 * CSV format produced by displayStationInfo}:
 *   name, latitude, longitude, false
 */
public class BusStationClass {

    /** Internal database key (currently unused but reserved for future use). */
    int Key = 0;

    /** Human-readable station name, e.g. Columbia. South Carolina". */
    String name = "Name";

    /**
     * Geographic latitude of the station in decimal degrees.
     * Positive values are north of the equator; negative are south.
     */
    double latitude = 0;

    /**
     * Geographic longitude of the station in decimal degrees.
     * Positive values are east of the prime meridian; negative are west.
     * US stations must use NEGATIVE values (western hemisphere).
     */
    double longitude = 0;

    // Constructors

    /**
     * No-argument constructor.
     * Creates a placeholder station with default values; all fields should be
     * set before the station is used.
     */
    public BusStationClass() {
    }

    /**
     * Full constructor.
     *
     * name      The display name of the station.
     * latitude  Latitude in decimal degrees (negative = south).
     * longitude Longitude in decimal degrees (negative = west).
     */
    public BusStationClass(String name, double latitude, double longitude) {
        this.name      = name;
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    // Display / Serialisation

    /**
     * Returns a comma-separated string representation of this station for
     * writing to {@code BusStation.csv}.
     *
     * The fourth column is always {@code "false"} for a plain station.
     * RefuelBusStation} overrides this method to write {@code "True"}.
     *
     * @return CSV-formatted station data.
     */
    public String displayStationInfo() {
        return name      + ", "
             + latitude  + ", "
             + longitude + ", "
             + "false";
    }

    // Getters and Setters

    /** @return The station's display name. */
    public String getName() { return name; }

    /** name The display name to set. */
    public void setName(String name) { this.name = name; }

    /** @return The latitude in decimal degrees. */
    public double getLatitude() { return latitude; }

    /** latitude The latitude to set, in decimal degrees. */
    public void setLatitude(double latitude) { this.latitude = latitude; }

    /** @return The longitude in decimal degrees. */
    public double getLongitude() { return longitude; }

    /** longitude The longitude to set, in decimal degrees. */
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
