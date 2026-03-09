package Project.BusStation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class BusStationManager {
    public void addBusStation() {
        try {
            Path path = Paths.get("Project/BusStation/BusStation.csv");
            long count = Files.lines(path).count();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        // Pointing to the specific sub-folder where your file lives
        String fileName = "Project/BusStation/BusStation.csv";
        File file = new File(fileName);

        if (!file.exists()) {
            System.err.println("ERROR: Could not find the file!");
            System.err.println("Looked at: " + file.getAbsolutePath());
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // Split by comma
                String[] columns = line.split(",");
                // Assuming the CSV has columns: Key, Name, Latitude, Longitude
                if (columns.length >= 4) {
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}