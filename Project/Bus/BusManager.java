package Project.Bus;

import java.util.ArrayList;
import Project.Bus.BusClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class BusManager {
    public ArrayList<BusClass> busList = new ArrayList<BusClass>();
    static String filename = "Project\\Bus\\Bus.csv";
    static File file = new File(filename);

    public BusManager() {

    }

    public void listBuses() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line, make, model, type;
            double fuelBurnRate, fuelCapacity, cruiseSpeed;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                line = line.replace("\"", "");
                String[] columns = line.split(", ");
                make = columns[0];
                model = columns[1];
                type = columns[2];
                fuelCapacity = Double.parseDouble(columns[3]);
                fuelBurnRate = Double.parseDouble(columns[4]);
                cruiseSpeed = Double.parseDouble(columns[5]);
                BusClass bus = new BusClass(make, model, type, fuelCapacity, fuelBurnRate,
                        cruiseSpeed);
                busList.add(bus);

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public ArrayList<BusClass> returnBus() {
        return busList;
    }
}
