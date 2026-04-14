package Project.Bus;

import java.util.ArrayList;
import Project.Bus.BusClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;

public class BusManager {
    public ArrayList<BusClass> busList = new ArrayList<BusClass>();
    static String filename = "Project/Bus/Bus.csv";
    File file = new File(filename);
    String line, make, model, type, fuelType;
    double fuelBurnRate, fuelCapacity, cruiseSpeed;

    public BusManager() throws FileNotFoundException {
    }

    public void listBuses() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                line = line.replace("\"", "");
                String[] columns = line.split(",\\s*");

                if (columns.length < 7)
                    continue;

                make = columns[0];
                model = columns[1];
                type = columns[2];
                fuelType = columns[3];

                // Use a helper method to handle the conversion safely
                fuelCapacity = tryParse(columns[4]);
                fuelBurnRate = tryParse(columns[5]);
                cruiseSpeed = tryParse(columns[6]);

                BusClass bus;
                if (type.equalsIgnoreCase("CityBus")) {
                    bus = new CityBus(make, model, fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
                } else if (type.equalsIgnoreCase("LongDistanceBus")) {
                    bus = new LongDistanceBus(make, model, fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
                } else {
                    bus = new BusClass(make, model, type, fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
                }
                busList.add(bus);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add this helper method to BusManager.java
    private double tryParse(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            // If the text isn't a number (like "fuelCapacity"), return 0.0 instead of
            // crashing
            return 0.0;
        }
    }

    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (BusClass b : busList) {
                String line = b.displayBusInfo();
                pw.println(line);
            }
        }
    }

    public boolean removeBus(int row) {
        if (row >= 0 && row < busList.size()) {
            busList.remove(row);
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else
            return false;
    }
}
