package Project.BusStation;

import java.util.ArrayList;
import Project.Bus.BusClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;

public class BusStationManager {
    public ArrayList<BusStationClass> stationList = new ArrayList<BusStationClass>();
    static String fileName = "Project/BusStation/BusStation.csv";
    File file = new File(fileName);
    String line;
    String name = "";
    double lat = 0;
    double lon = 0;

    public BusStationManager() throws FileNotFoundException {
    }

    public void listBuses() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                line = line.replace("\"", "");
                String[] columns = line.split(", ");
                name = columns[0];
                lat = Double.parseDouble(columns[1]);
                lon = Double.parseDouble(columns[2]);
                BusStationClass station = new BusStationClass();
                stationList.add(station);

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (BusStationClass s : stationList) {
                String line = s.displayStationInfo();
                pw.println(line);
            }
        }
    }

    public boolean removeStation(int row) {
        if (row >= 0 && row < stationList.size()) {
            stationList.remove(row);
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
