package Project.Bus;

/**
 * Represents a long-haul inter-city bus.
 *
 * This class extends BusClass and automatically sets the type
 * field to "LongDistanceBus".  All other attributes (make, model, fuel,
 * speed) are delegated to the parent class.
 *
 * Long-distance buses typically have larger fuel tanks and faster cruise speeds
 * than CityBus models, making them suitable for multi-station routes.
 */
public class LongDistanceBus extends BusClass {

    /**
     * No-argument constructor.
     * Creates a blank LongDistanceBus placeholder with the default field values
     * defined in BusClass, then overrides the type to
     * "LongDistanceBus".
     */
    public LongDistanceBus() {
        super();
        this.setType("LongDistanceBus");
    }

    /**
     * Full constructor.
     * Passes all attributes to the parent BusClass constructor and
     * locks the type to "LongDistanceBus" via the type argument.
     *
     * make         Manufacturer name.
     * model        Model designation.
     * fuelType     Fuel type ("Gas" or "Diesel").
     * fuelCapacity Tank capacity in gallons.
     * fuelBurnRate Fuel consumption rate in gallons per hour.
     * cruiseSpeed  Cruise speed in miles per hour.
     */
    public LongDistanceBus(String make, String model, String fuelType,
                           double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        super(make, model, "LongDistanceBus", fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
    }
}
