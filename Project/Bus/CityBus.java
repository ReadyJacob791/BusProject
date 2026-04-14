package Project.Bus;

public class CityBus extends BusClass {
    public CityBus() {
        super();
        this.setType("CityBus");
    }

    public CityBus(String make, String model, String fuelType,
            double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        super(make, model, "CityBus", fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
    }
}