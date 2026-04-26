package Project.Bus;

/**
 * Represents a short-haul city bus.
 *
 * This class extends BusClass and automatically sets the type
 * field to "CityBus".  All other attributes (make, model, fuel, speed)
 * are delegated to the parent class.
 *
 * City buses are typically smaller and have shorter range than
 * LongDistanceBus models.
 */
public class CityBus extends BusClass {

    /**
     * No-argument constructor.
     * Creates a blank CityBus placeholder with the default field values defined
     * in BusClass, then overrides the type to "CityBus".
     */
    public CityBus() {
        super();
        this.setType("CityBus");
    }

    /**
     * Full constructor.
     * Passes all attributes to the parent BusClass constructor and
     * locks the type to "CityBus" via the type argument.
     *
     * @param make         Manufacturer name.
     * @param model        Model designation.
     * @param fuelType     Fuel type ("Gas" or "Diesel").
     * @param fuelCapacity Tank capacity in gallons.
     * @param fuelBurnRate Fuel consumption rate in gallons per hour.
     * @param cruiseSpeed  Cruise speed in miles per hour.
     */
    public CityBus(String make, String model, String fuelType,
                   double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        super(make, model, "CityBus", fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
    }
}
