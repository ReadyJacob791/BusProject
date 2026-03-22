package Project.Bus;

// finally made it

public class BusClass {
    String make;
    String model;
    String type;
    double fuelCapacity;
    double cruiseSpeed;
    double fuelBurnRate;

    BusClass(String make, String model, String type, double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        this.make = make;
        this.model = model;
        this.type = type;
        this.fuelCapacity = fuelCapacity;
        this.fuelBurnRate = fuelBurnRate;
        this.cruiseSpeed = cruiseSpeed;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getFuelCapacity() {
        return fuelCapacity;
    }

    public void setFuelCapacity(double fuelCapacity) {
        this.fuelCapacity = fuelCapacity;
    }

    public double getcruiseSpeed() {
        return cruiseSpeed;
    }

    public double getFuelBurnRate() {
        return fuelBurnRate;
    }

    public void setFuelBurnRate(double fuelBurnRate) {
        this.fuelBurnRate = fuelBurnRate;
    }

    public String displayBusInfo() {
        String info = make + ", " + model + ", " + type + ", " + fuelCapacity;
        return info;
    }
}
