package Project.BusStation;

import java.util.ArrayList;
import java.io.*;

/**
 * Manages the in-memory list of bus stations and handles all CSV persistence.
 *
 * Responsibilities:
 *   Loading station records from BusStation.csv into
 *       stationList.
 *   Saving the current stationList back to BusStation.csv}.
 *   Removing a station from both the list and the file.
 * 
 *
 * Expected CSV format (one station per line):
 *      name, latitude, longitude, refuelFlag
 * where refuelFlag is "True" for a RefuelBusStation
 * and "false" (or absent) for a plain BusStationClass.
 */
public class BusStationManager {

    /**
     * The live, in-memory list of all stations.
     * Public so the UI and graph builder can iterate over it directly.
     */
    public ArrayList<BusStationClass> stationList = new ArrayList<>();

    /** Path to the CSV file that stores station data. */
    static String fileName = "Project/BusStation/BusStation.csv";

    /** File reference to the station CSV. */
    File file = new File(fileName);

    // Constructor

    /**
     * No-argument constructor.
     * The FileNotFoundException is declared so that the calling code
     * (UserInterface) is forced to handle the case where the CSV file is absent.
     *
     * FileNotFoundException If the station CSV file does not exist.
     */
    public BusStationManager() throws FileNotFoundException {
    }

    // CSV Loading

    /**
     * Clears stationList} and reloads it from BusStation.csv}.
     *
     * Column indices:
     *   0 – station name
     *   1 – latitude (decimal degrees; negative for western hemisphere)
     *   2 – longitude (decimal degrees; negative for western hemisphere)
     *   3 – refuel flag ("True" → RefuelBusStation}
     *            anything else → BusStationClass)
     *
     * Lines with fewer than three columns are skipped silently.
     */
    public void listStations() {
        stationList.clear(); // Remove stale data before re-reading

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(", ");

                // Skip malformed rows that don't have at least name, lat, lon
                if (columns.length < 3) continue;

                // Strip any surrounding quotes added by CSV writers
                String name = columns[0].replace("\"", "");
                double lat  = Double.parseDouble(columns[1]);
                double lon  = Double.parseDouble(columns[2]);

                // The refuel column is optional; default to false if absent
                boolean isRefuel = false;
                if (columns.length >= 4) {
                    isRefuel = Boolean.parseBoolean(columns[3]);
                }

                // Instantiate the correct subclass based on the refuel flag
                if (isRefuel) {
                    stationList.add(new RefuelBusStation(name, lat, lon));
                } else {
                    stationList.add(new BusStationClass(name, lat, lon));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CSV Saving

    /**
     * Writes the entire current stationList} to BusStation.csv},
     * overwriting its previous contents.
     *
     * Each station is serialised via BusStationClass.displayStationInfo(),
     * which includes the refuel flag in the fourth column.
     *
     * throws IOException If the file cannot be opened or written to.
     */
    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (BusStationClass s : stationList) {
                pw.println(s.displayStationInfo());
            }
        }
    }

    // Station Removal

    /**
     * Removes the station at the given index from stationList} and
     * immediately saves the updated list to CSV.
     *
     * row index of the station to remove.
     * True if the station was found and removed
     *         false if row is out of bounds.
     */
    public boolean removeStation(int row) {
        if (row >= 0 && row < stationList.size()) {

            stationList.remove(row);

            // Persist the change so the CSV stays in sync with memory
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false; // Index was out of range — nothing was removed
    }
}
