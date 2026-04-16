package Project.Bus;

// this class is the basic bus class and is used as the shared attributes and methods of the city and long distance buses
public class BusClass {

    // This block is used to declare the Basic attributes of a bus
    String make = "make";
    String model = "model";
    String type = "CityBus";
    String fuelType = "Gas";
    double fuelCapacity = 0.0;
    double cruiseSpeed = 0.0;
    double fuelBurnRate = 0.0;

    // This is the cunstructor for the BusClass
    public BusClass(String make, String model, String type, String fuelType,
            double fuelCapacity, double fuelBurnRate, double cruiseSpeed) {
        this.make = make;
        this.model = model;
        this.type = type;
        this.fuelType = fuelType;
        this.fuelCapacity = fuelCapacity;
        this.fuelBurnRate = fuelBurnRate;
        this.cruiseSpeed = cruiseSpeed;
    }

    // This is a blank constructor
    public BusClass() {
    }

    // This funtion is to display the bus info in a string.
    public String displayBusInfo() {
        String info = make + ", "
                + model + ", "
                + type + ", "
                + fuelType + ", "
                + fuelCapacity + ", "
                + fuelBurnRate + ", "
                + cruiseSpeed;
        return info;
    }

    // The bellow funtions are all geters and seters
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

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public double getFuelCapacity() {
        return fuelCapacity;
    }

    public void setFuelCapacity(double fuelCapacity) {
        this.fuelCapacity = fuelCapacity;
    }

    public double getCruiseSpeed() {
        return cruiseSpeed;
    }

    public void setCruiseSpeed(double CruiseSpeed) {
        this.cruiseSpeed = CruiseSpeed;
    }

    public double getFuelBurnRate() {
        return fuelBurnRate;
    }

    public void setFuelBurnRate(double fuelBurnRate) {
        this.fuelBurnRate = fuelBurnRate;
    }
}
