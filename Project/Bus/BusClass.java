package Project.Bus;

/**
 * Base class for all bus types in the fleet management system.
 *
 * Stores the seven core attributes shared by every bus:
 * make, model, type (CityBus / LongDistanceBus), fuel type, fuel capacity,
 * fuel burn rate, and cruise speed.
 *
 * Subclasses CityBus and LongDistanceBus inherit all fields
 * and simply set the type field to the appropriate category string.
 */
public class BusClass {

    // Fields

    /** Manufacturer / brand name (e.g. "Blue Bird", "MCI"). */
    String make = "make";

    /** Model designation (e.g. "J4500", "XD"). */
    String model = "model";

    /**
     * Category of the bus.  Expected values are "CityBus" or
     * "LongDistanceBus"; set by the subclass constructor.
     */
    String type = "CityBus";

    /** Fuel type used by the bus (e.g. "Gas", "Diesel"). */
    String fuelType = "Gas";

    /**
     * Maximum amount of fuel the bus can carry, in gallons.
     * Used by the route planner to decide if a route is feasible.
     */
    double fuelCapacity = 0.0;

    /** Maximum operational speed in miles per hour. */
    double cruiseSpeed = 0.0;

    /**
     * Rate at which the bus consumes fuel while driving, in gallons per hour.
     * Combined with trip time to estimate total fuel used for a route.
     */
    double fuelBurnRate = 0.0;

    // Constructors

    /**
     * Full constructor — sets every attribute explicitly.
     * Used when loading a bus from a CSV file or saving a new entry with
     * complete data.
     *
     * make         Manufacturer name.
     * model        Model designation.
     * type         Bus category string ("CityBus" or "LongDistanceBus").
     * fuelType     Fuel type ("Gas" or "Diesel").
     * fuelCapacity Tank capacity in gallons.
     * fuelBurnRate Fuel consumption rate in gallons per hour.
     * cruiseSpeed  Cruise speed in miles per hour.
     */
    public BusClass(String make, String model, String type, String fuelType,
                    double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        this.make         = make;
        this.model        = model;
        this.type         = type;
        this.fuelType     = fuelType;
        this.fuelCapacity = fuelCapacity;
        this.fuelBurnRate = fuelBurnRate;
        this.cruiseSpeed  = cruiseSpeed;
    }

    /**
     * No-argument constructor.
     * Used when creating a blank placeholder bus through the UI's "New Bus"
     * button; the user then fills in the fields and submits.
     */
    public BusClass() {
    }

    // Display

    /**
     * Returns a comma-separated summary of all bus attributes, matching the
     * format expected by {@link BusManager} when writing to Bus.csv.
     *
     * Format: {@code make, model, type, fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed}
     *
     * @return A formatted string suitable for CSV serialisation.
     */
    public String displayBusInfo() {
        return make        + ", "
             + model       + ", "
             + type        + ", "
             + fuelType    + ", "
             + fuelCapacity + ", "
             + fuelBurnRate + ", "
             + cruiseSpeed;
    }

    // Getters and Setters

    /** @return The manufacturer name. */
    public String getMake() { return make; }

    /** make The manufacturer name to set. */
    public void setMake(String make) { this.make = make; }

    /** @return The model designation. */
    public String getModel() { return model; }

    /** model The model designation to set. */
    public void setModel(String model) { this.model = model; }

    /** @return The bus category ("CityBus" or "LongDistanceBus"). */
    public String getType() { return type; }

    /** type The bus category to set. */
    public void setType(String type) { this.type = type; }

    /** @return The fuel type ("Gas" or "Diesel"). */
    public String getFuelType() { return fuelType; }

    /** fuelType The fuel type to set. */
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    /** @return The fuel tank capacity in gallons. */
    public double getFuelCapacity() { return fuelCapacity; }

    /** fuelCapacity The fuel tank capacity to set, in gallons. */
    public void setFuelCapacity(double fuelCapacity) { this.fuelCapacity = fuelCapacity; }

    /** @return The cruise speed in miles per hour. */
    public double getCruiseSpeed() { return cruiseSpeed; }

    /** cruiseSpeed The cruise speed to set, in miles per hour. */
    public void setCruiseSpeed(double cruiseSpeed) { this.cruiseSpeed = cruiseSpeed; }

    /** @return The fuel burn rate in gallons per hour. */
    public double getFuelBurnRate() { return fuelBurnRate; }

    /** fuelBurnRate The fuel burn rate to set, in gallons per hour. */
    public void setFuelBurnRate(double fuelBurnRate) { this.fuelBurnRate = fuelBurnRate; }
}
