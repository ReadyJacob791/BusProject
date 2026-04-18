package Project.BusStation;

import java.util.ArrayList;
import java.io.*;

public class BusStationManager {
    public ArrayList<BusStationClass> stationList = new ArrayList<>();
    static String fileName = "Project/BusStation/BusStation.csv";
    File file = new File(fileName);

    public BusStationManager() throws FileNotFoundException {
    }

    public void listStations() {
        stationList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(", ");
                if (columns.length < 3)
                    continue;

                String name = columns[0].replace("\"", "");
                double lat = Double.parseDouble(columns[1]);
                double lon = Double.parseDouble(columns[2]);

                boolean isRefuel = false;
                if (columns.length >= 4) {
                    isRefuel = Boolean.parseBoolean(columns[3]);
                }

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

    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (BusStationClass s : stationList) {
                pw.println(s.displayStationInfo());
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
        }
        return false;
    }
}