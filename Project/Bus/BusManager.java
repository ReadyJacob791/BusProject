package Project.Bus;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Manages the in-memory list of buses and handles all CSV persistence.
 * 
 * Responsibilities:
 *   Loading the bus fleet from Bus.csv into busList.
 *   Saving the current busList back to Bus.csv.
 *   Removing a bus from both the list and the CSV.
 * The CSV format expected by this class and written by BusClass.displayBusInfo is:
 *   make, model, type, fuelType, fuelCapacity, fuelBurnRate, cruiseSpeed
 */
public class BusManager {

    /**
     * The live, in-memory list of all buses.
     * Public so the UI can iterate over it directly without a getter.
     */
    public ArrayList<BusClass> busList = new ArrayList<>();

    /** Path to the CSV file that stores bus data. */
    static String filename = "Project/Bus/Bus.csv";

    /** File reference to the bus CSV. */
    File file = new File(filename);

    // Temporary parsing variables reused across CSV rows
    String line, make, model, type, fuelType;
    double fuelBurnRate, fuelCapacity, cruiseSpeed;

    /** Placeholder for the bus object being constructed during CSV parsing. */
    BusClass bus;

    // Constructor

    /**
     * No-argument constructor.
     * The FileNotFoundException is declared so that the calling code
     * (UserInterface) is forced to handle the case where the CSV file is absent.
     *
     * throws FileNotFoundException If the bus CSV file does not exist.
     */
    public BusManager() throws FileNotFoundException {
    }

    // CSV Loading

    /**
     * Reads every row from Bus.csv and populates busList
     *
     * Each line is split on comma-plus-optional-whitespace.  Column indices:
     *   0 – make
     *   1 – model
     *   2 – type ("CityBus" or "LongDistanceBus")
     *   3 – fuelType
     *   4 – fuelCapacity
     *   5 – fuelBurnRate
     *   6 – cruiseSpeed
     * Depending on the type string the appropriate subclass is instantiated.
     */
    public void listBuses() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            // Read one CSV row per iteration
            while ((line = br.readLine()) != null) {

                // Split on ", " while allowing variable amounts of whitespace
                String[] columns = line.split(",\\s*");

                // Map column indices to named variables for clarity
                make     = columns[0];
                model    = columns[1];
                type     = columns[2];
                fuelType = columns[3];

                // Parse the numeric fields from their string representations
                fuelCapacity  = Double.parseDouble(columns[4]);
                fuelBurnRate  = Double.parseDouble(columns[5]);
                cruiseSpeed   = Double.parseDouble(columns[6]);

                // Instantiate the correct subclass based on the type column
                if (type.equalsIgnoreCase("CityBus")) {
                    bus = new CityBus(make, model, fuelType,
                                      fuelCapacity, fuelBurnRate, cruiseSpeed);
                } else {
                    bus = new LongDistanceBus(make, model, fuelType,
                                              fuelCapacity, fuelBurnRate, cruiseSpeed);
                }

                busList.add(bus);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CSV Saving

    /**
     * Writes the entire current busList to Bus.csv},
     * overwriting its previous contents.
     *
     * Each bus is serialised via BusClass#displayBusInfo(), which
     * produces the comma-separated format this class reads back on the next
     * listBuses() call.
     *
     * throws IOException If the file cannot be opened or written to.
     */
    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (BusClass b : busList) {
                pw.println(b.displayBusInfo());
            }
        }
    }

    // Bus Removal

    /**
     * Removes the bus at the given index from busList and immediately
     * saves the updated list to CSV.
     *
     * row Zero-based index of the bus to remove.
     * true if the bus was found and removed;
     * false if row is out of bounds.
     */
    public boolean removeBus(int row) {
        // Validate the index before attempting removal
        if (row >= 0 && row < busList.size()) {

            busList.remove(row);

            // Persist the change so the CSV stays in sync with memory
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;

        } else {
            return false; // Index was out of range — nothing was removed
        }
    }
}
