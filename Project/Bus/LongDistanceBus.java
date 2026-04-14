package Project.Bus;

public class LongDistanceBus extends BusClass {
    public LongDistanceBus() {
        super();
        this.setType("LongDistanceBus");
    }

    public LongDistanceBus(String make, String model, String fuelType,
            double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        super(make, model, "LongDistanceBus", fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
    }
}