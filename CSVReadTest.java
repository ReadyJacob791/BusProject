import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class CSVReadTest {
    public static void main(String[] args) {
        // Pointing to the specific sub-folder where your file lives
        String fileName = "Test.csv";
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

                // Clean and print each column
                for (String col : columns) {
                    // This removes the quotes and the extra spaces
                    String cleanValue = col.replace("\"", "").trim();
                    System.out.print("[" + cleanValue + "] ");
                }
                System.out.println(); // Next line for next row
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}