package Project.Bus;

import java.util.ArrayList;
import Project.Bus.BusClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class BusManager {
    public static ArrayList<BusClass> busList = new ArrayList<BusClass>();
    static String filename = "Project\\Bus\\Bus.csv";
    static File file = new File(filename);

    public static void listBuses() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line, make, model, type;
            double fuelBurnRate, fuelCapacity, cruiseSpeed;

            while ((line = br.readLine()) != null) {
                // Split by comma
                String[] columns = line.split(", ");
                make = columns[0];
                model = columns[1];
                type = columns[2];
                fuelCapacity = Double.parseDouble(columns[3]);
                fuelBurnRate = Double.parseDouble(columns[4]);
                cruiseSpeed = Double.parseDouble(columns[5]);
                BusClass bus = new BusClass(make, model, type, fuelCapacity, fuelBurnRate, cruiseSpeed);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
