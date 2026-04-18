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

// This class is used to add all buses to an active list from the CSV when the program is running and add, edit, or remove them.
// This class also saves the bus list to the CSV
public class BusManager {

    // This is the active bus list
    public ArrayList<BusClass> busList = new ArrayList<BusClass>();

    // This block is declaring the file to read and write buses
    static String filename = "Project/Bus/Bus.csv";
    File file = new File(filename);

    // This is the declaration of the Bus asstributes
    String line, make, model, type, fuelType;
    double fuelBurnRate, fuelCapacity, cruiseSpeed;

    // This is the declaration of a blank bus
    BusClass bus;

    // This is a blank constructor
    public BusManager() throws FileNotFoundException {
    }

    // This Function is used to add all the buses into a list by reading the CSV.
    public void listBuses() {

        // This line sets up the Buffered reader to read the file
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) { // This line splits the file into lines add loops

                // This line splits each attribute of the the bus into different Strings
                String[] columns = line.split(",\\s*");

                // This block is used to declare which columns are which for clearity
                make = columns[0];
                model = columns[1];
                type = columns[2];
                fuelType = columns[3];

                // This block is used to parse the doubles from the strings
                fuelCapacity = Double.parseDouble(columns[4]);
                fuelBurnRate = Double.parseDouble(columns[5]);
                cruiseSpeed = Double.parseDouble(columns[6]);

                // This block is used to find wether the Bus is a city or Long distance bus, and
                // to declare a different Class
                if (type.equalsIgnoreCase("CityBus")) {
                    bus = new CityBus(make, model, fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
                } else {
                    bus = new LongDistanceBus(make, model, fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed);
                }

                // This line adds the bus to the list
                busList.add(bus);
            }
            // This is the error handling
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This function is used to save all buses into the CSV
    public void save() throws IOException {

        // This block is used to write to the file using the bus info
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (BusClass b : busList) {
                String line = b.displayBusInfo();
                pw.println(line);
            }
        }
    }

    // This function is used to remove a bus from the active list as well as the CSV
    // file. It returns a boolean based on if it was able to delete the bus
    public boolean removeBus(int row) {

        // This line is used to make sure the list has that many buses
        if (row >= 0 && row < busList.size()) {

            // This Block removes the bus the saves the file.
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
