package Project.Bus;

// finally made it

public class BusClass {
    String make = "make";
    String model = "model";
    String type = "type";
    double fuelCapacity = 0.0;
    double cruiseSpeed = 0.0;
    double fuelBurnRate = 0.0;

    BusClass(String make, String model, String type, double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        this.make = make;
        this.model = model;
        this.type = type;
        this.fuelCapacity = fuelCapacity;
        this.fuelBurnRate = fuelBurnRate;
        this.cruiseSpeed = cruiseSpeed;
    }

    public BusClass() {
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

    public void setCruiseSpeed(double CruiseSpeed) {
        this.cruiseSpeed = CruiseSpeed;
    }

    public double getCruiseSpeed() {
        return cruiseSpeed;
    }

    public double getFuelBurnRate() {
        return fuelBurnRate;
    }

    public void setFuelBurnRate(double fuelBurnRate) {
        this.fuelBurnRate = fuelBurnRate;
    }

    public String displayBusInfo() {
        String info = make + ", "
                + model + ", "
                + type + ", "
                + fuelCapacity + ", "
                + fuelBurnRate + ", "
                + cruiseSpeed;
        return info;
    }
}
