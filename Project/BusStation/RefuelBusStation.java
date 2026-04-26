package Project.BusStation;

/**
 * A bus station that also provides a refuelling service.
 *
 * Extends BusStationClass and overrides displayStationInfo
 * so the CSV serialisation writes "True" in the refuel column.
 *
 * The GraphPanel in the UI renders a ⛽ fuel-pump emoji instead of the normal
 * red dot when it detects that a Node's station is an instance of this class.
 */
public class RefuelBusStation extends BusStationClass {

    // Constructor

    /**
     * Constructs a refuelling station with the given name and coordinates.
     * Delegates to the parent constructor — no additional fields are needed
     * because the refuel flag is implied by the class type itself.
     *
     * name      The display name of the station.
     * latitude  Latitude in decimal degrees (negative = west for US).
     * longitude Longitude in decimal degrees (negative = west for US).
     */
    public RefuelBusStation(String name, double latitude, double longitude) {
        super(name, latitude, longitude);
    }

    // -------------------------------------------------------------------------
    // Overridden Serialisation
    // -------------------------------------------------------------------------

    /**
     * Returns a comma-separated string representation of this refuelling
     * station for writing to BusStation.csv.
     *
     * The only difference from the base class version is that the fourth column
     * is "True" instead of "false".
     * BusStationManager#listStations uses
     * Boolean.parseBoolean(String) on this column to decide which
     * subclass to instantiate when reloading from CSV.
     *
     * return CSV-formatted station data with the refuel flag set to True.
     */
    @Override
    public String displayStationInfo() {
        return name      + ", "
             + latitude  + ", "
             + longitude + ", "
             + "True";
    }
}
