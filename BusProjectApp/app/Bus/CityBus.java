package Project.Bus;

// This class is a subclass of BusClass.
public class CityBus extends BusClass {

    // This functions is the blank constructor to set that it is a city bus
    public CityBus() {
        super();
        this.setType("CityBus");
    }

    // This function is the constructor to set that it is a city bus
    public CityBus(String make, String model, String fuelType,
            double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        super(make, model, "CityBus", fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
    }
}