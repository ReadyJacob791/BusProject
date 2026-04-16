package Project.Bus;

// This class is a subclass of BusClasss
public class LongDistanceBus extends BusClass {

    // This functions is the blank constructor to set that it is a city bus
    public LongDistanceBus() {
        super();
        this.setType("LongDistanceBus");
    }

    // This function is the constructor to set that it is a city bus
    public LongDistanceBus(String make, String model, String fuelType,
            double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        super(make, model, "LongDistanceBus", fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
    }
}