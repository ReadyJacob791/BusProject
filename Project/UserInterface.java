package Project;

import Project.Bus.BusClass;
import Project.Bus.BusManager;
import Project.BusStation.BusStationClass;
import Project.BusStation.BusStationManager;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

//https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
// After the login it should initialize the Weighted Graph

// ADD FUEL TYPE GAS / DESIL TO BUSES AND STATION TYPE REFUEL / NOT TO STATIONS

public class UserInterface {
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private DefaultTableModel busTable;
    private JPanel loginPanel;
    private JPanel dashboardPanel;
    private JPanel buspanel;
    private JPanel stationpanel;
    private BusManager bManager;
    private BusStationManager sManager;
    private DefaultTableModel stationTable;

    int selectedRow = -1;

    public UserInterface() {
        try {
            bManager = new BusManager();
            bManager.listBuses();
            sManager = new BusStationManager();
            sManager.listStations();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null,
                "System Warning: Unauthorized access is prohibited. Click OK to proceed.",
                "Security Alert",
                JOptionPane.WARNING_MESSAGE);
        new UserInterface().initialize();
    }

    public void initialize() {

        // Set size for UI window
        frame = new JFrame("Route Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 1000);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(logInPanel(), "LOGIN");
        cardPanel.add(dashboardPanel(), "DASHBOARD");
        cardPanel.add(manageBus(), "MANAGEBUS");
        cardPanel.add(manageBusStation(), "MANAGESTATION");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Actions");
        JMenuItem manageBus = new JMenuItem("Manage Bus");
        JMenuItem manageStation = new JMenuItem("Manage Station");
        JMenuItem logoutItem = new JMenuItem("Logout");

        manageBus.addActionListener(e -> {
            cardLayout.show(cardPanel, "MANAGEBUS"); // Route back to login
            frame.revalidate(); // Re-layout the frame with the new menu
            selectedRow = -1;

        });

        manageStation.addActionListener(e -> {
            cardLayout.show(cardPanel, "MANAGESTATION");
            frame.revalidate();
            selectedRow = -1;

        });

        logoutItem.addActionListener(e -> {
            frame.setJMenuBar(null); // Remove the top menu dynamically
            cardLayout.show(cardPanel, "LOGIN"); // Route back to login
            frame.revalidate(); // Re-layout the frame with the new menu
        });

        menu.add(manageBus);
        menu.add(manageStation);
        menu.add(logoutItem);
        menuBar.add(menu);
        return menuBar;
    }

    private JPanel logInPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");
        JButton addAccountBtn = new JButton("Add Account");

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(addAccountBtn);
        loginPanel.add(btnPanel, gbc);

        // Login Action
        loginBtn.addActionListener(e -> {
            // In a real app, you would validate credentials here. We will just transition.
            cardLayout.show(cardPanel, "DASHBOARD");
            frame.setJMenuBar(createMenuBar()); // Dynamically attach the menu bar
            frame.revalidate(); // Re-layout the frame with the new menu
        });

        addAccountBtn.addActionListener(e -> {
            showAddAccountDialog();
        });

        return loginPanel;
    }

    private boolean validateLogin(String username, String password) {
        File file = new File("Accounts.csv"); 

        if (!file.exists()) {
            return false; 
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line; 
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // split username and password with a comma

                if (parts.length >= 2) {
                    String storedUser = parts.trim(); 
                    String storedPass = parts.trim();
                    
                    if (storedUser.equals(username) && storedPass.equals(password)) {
                        return true; 
                    }
                }

            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not read Accounts.csv file");
            e.printStackTrace();
        }
        return false; 
    }

    // current account dialog
    private void showAddAccountDialog() {
<<<<<<< HEAD
        JDialog dialog = new JDialog(frame, "Create New Account", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
=======

    JDialog dialog = new JDialog(frame, "Create New Account", true);
    dialog.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
>>>>>>> 6d35547c3ae4894c1659064a518e9953586272ba

        JTextField newUsernameField = new JTextField(15);
        JPasswordField newPasswordField = new JPasswordField(15);
        JPasswordField verifyPasswordField = new JPasswordField(15);
        JButton submitBtn = new JButton("Submit");

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(newUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Verify Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(verifyPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        submitBtn.addActionListener(e -> {
            String username = newUsernameField.getText();
            String password = new String(newPasswordField.getPassword());
            String verify = new String(verifyPasswordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot be empty! Please try again.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else if (!password.equals(verify)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match! Please try again.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                // stores account info into csv file 'Accounts.csv'
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("Accounts.csv", true))) {

                    writer.write(username + ", " + password);
                    writer.newLine();

                    JOptionPane.showMessageDialog(dialog, "The account '" + username + "' was stored successfully!");
                    dialog.dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error writing to file: " + ex.getMessage(), "File Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        dialog.add(submitBtn, gbc);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private JPanel dashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        String[] dropdownOptions = { "Choose Option", "Manage Bus" };
        JComboBox<String> comboBox = new JComboBox<>(dropdownOptions);
        comboBox.addActionListener(e -> {
            String selected = (String) comboBox.getSelectedItem();
            if (!selected.startsWith("Choose")) {
                cardLayout.show(cardPanel, "MANAGEBUS");
            }
        });
        dashboardPanel.add(comboBox, BorderLayout.NORTH);

        return dashboardPanel;
    }

    // Add / Edit / remove Bus page
    private JPanel manageBus() {
        JPanel buspanel = new JPanel(new BorderLayout());

        // --- Define Larger Fonts ---
        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        // --- Current Buses Table ---
        String tablename[] = { "Make", "Model", "Type", "Fuel Capacity", "Fuel Burn Rate", "Cruise Speed" };
        DefaultTableModel busTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(busTable);

        // Make the table text and rows bigger
        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));

        JScrollPane pane = new JScrollPane(table);

        for (Object b : bManager.busList) {
            String s = ((BusClass) b).displayBusInfo();
            String[] col = s.split(", ");
            busTable.addRow(new Object[] { col[0], col[1], col[2], col[3], col[4], col[5] });
        }

        // Change from EAST to CENTER so the table expands to fill the remaining space
        buspanel.add(pane, BorderLayout.CENTER);

        // --- NEW WRAPPER PANEL FOR THE WEST SIDE ---
        JPanel busWrapper = new JPanel();
        busWrapper.setLayout(new BoxLayout(busWrapper, BoxLayout.Y_AXIS));
        // Add a little padding around the left panel so it isn't squeezed against the
        // edge
        busWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Dimension boxSize = new Dimension(800, 40); // Increased height for bigger text

        // 1. Input Boxes Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        // Bus Name Input
        JLabel make = new JLabel("Make:");
        make.setFont(labelFont);
        JTextField makeBox = new JTextField(15);
        makeBox.setFont(inputFont);
        makeBox.setMaximumSize(boxSize);
        inputPanel.add(make);
        inputPanel.add(makeBox);
        inputPanel.add(Box.createVerticalStrut(10)); // Adds spacing between fields

        // Model Input
        JLabel model = new JLabel("Model:");
        model.setFont(labelFont);
        JTextField modelBox = new JTextField(15);
        modelBox.setFont(inputFont);
        modelBox.setMaximumSize(boxSize);
        inputPanel.add(model);
        inputPanel.add(modelBox);
        inputPanel.add(Box.createVerticalStrut(10));

        // Type Input
        JLabel type = new JLabel("Type:");
        type.setFont(labelFont);
        JTextField typeBox = new JTextField(15);
        typeBox.setFont(inputFont);
        typeBox.setMaximumSize(boxSize);
        inputPanel.add(type);
        inputPanel.add(typeBox);
        inputPanel.add(Box.createVerticalStrut(10));

        // Cruise Speed Input
        JLabel cruiseSpeed = new JLabel("Cruise Speed:");
        cruiseSpeed.setFont(labelFont);
        JTextField cruiseSpeedBox = new JTextField(15);
        cruiseSpeedBox.setFont(inputFont);
        cruiseSpeedBox.setMaximumSize(boxSize);
        inputPanel.add(cruiseSpeed);
        inputPanel.add(cruiseSpeedBox);
        inputPanel.add(Box.createVerticalStrut(10));

        // Fuel Burn Rate Input
        JLabel fuelBurnRate = new JLabel("Fuel Burn Rate:");
        fuelBurnRate.setFont(labelFont);
        JTextField fuelBurnRateBox = new JTextField(15);
        fuelBurnRateBox.setFont(inputFont);
        fuelBurnRateBox.setMaximumSize(boxSize);
        inputPanel.add(fuelBurnRate);
        inputPanel.add(fuelBurnRateBox);
        inputPanel.add(Box.createVerticalStrut(10));

        // Fuel Capacity Input
        JLabel fuelCapacity = new JLabel("Fuel Capacity:");
        fuelCapacity.setFont(labelFont);
        JTextField fuelCapacityBox = new JTextField(15);
        fuelCapacityBox.setFont(inputFont);
        fuelCapacityBox.setMaximumSize(boxSize);
        inputPanel.add(fuelCapacity);
        inputPanel.add(fuelCapacityBox);

        inputPanel.add(Box.createVerticalGlue());

        // 2. Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton submitBus = new JButton("Submit");
        JButton removeBus = new JButton("Remove");
        JButton newBus = new JButton("New Bus");

        // Make buttons bigger too
        submitBus.setFont(labelFont);
        removeBus.setFont(labelFont);
        newBus.setFont(labelFont);

        buttonPanel.add(submitBus);
        buttonPanel.add(newBus);
        buttonPanel.add(removeBus);

        // 3. Add to wrapper
        busWrapper.add(inputPanel);
        busWrapper.add(buttonPanel);

        // Add the single wrapper panel to the West
        buspanel.add(busWrapper, BorderLayout.WEST);

        // change the text boxes besed on the table

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    makeBox.setText(bManager.busList.get(selectedRow).getMake());
                    modelBox.setText(bManager.busList.get(selectedRow).getModel());
                    typeBox.setText(bManager.busList.get(selectedRow).getType());
                    cruiseSpeedBox.setText(String.valueOf(bManager.busList.get(selectedRow).getCruiseSpeed()));
                    fuelBurnRateBox.setText(String.valueOf(bManager.busList.get(selectedRow).getFuelBurnRate()));
                    fuelCapacityBox.setText(String.valueOf(bManager.busList.get(selectedRow).getFuelCapacity()));
                } else {
                    makeBox.setText("");
                    modelBox.setText("");
                    typeBox.setText("");
                    cruiseSpeedBox.setText("");
                    fuelBurnRateBox.setText("");
                    fuelCapacityBox.setText("");
                }
            }

        });

        submitBus.addActionListener(e -> {
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a bus first.");
                return;
            }

            StringBuilder errorLog = new StringBuilder();
            boolean isValid = true;

            // --- 1. STRICT STRING VALIDATION (No symbols allowed) ---
            String makeVal = makeBox.getText().trim();
            String modelVal = modelBox.getText().trim();
            String typeVal = typeBox.getText().trim();

            // Regex: Only letters, numbers, and spaces. No !@#$%^&*() or 'f' suffixes here.
            String alphaNumRegex = "^[a-zA-Z0-9 ]+$";

            if (!makeVal.matches(alphaNumRegex)) {
                errorLog.append("- 'Make' has invalid symbols or is empty.\n");
                isValid = false;
            }
            if (!modelVal.matches(alphaNumRegex)) {
                errorLog.append("- 'Model' has invalid symbols or is empty.\n");
                isValid = false;
            }
            if (!typeVal.matches(alphaNumRegex)) {
                errorLog.append("- 'Type' has invalid symbols or is empty.\n");
                isValid = false;
            }

            // --- 2. STRICT DOUBLE VALIDATION (Digits and decimal ONLY) ---
            // This Regex ensures NO letters (like 'f') can get through.
            // ^[0-9]*\\.?[0-9]+$ means: optional digits, optional dot, required digits.
            String numericRegex = "^[0-9]*\\.?[0-9]+$";

            String speedTxt = cruiseSpeedBox.getText().trim();
            String burnTxt = fuelBurnRateBox.getText().trim();
            String capTxt = fuelCapacityBox.getText().trim();

            if (!speedTxt.matches(numericRegex)) {
                errorLog.append("- 'Cruise Speed' must be a pure number (no letters/symbols).\n");
                isValid = false;
            }
            if (!burnTxt.matches(numericRegex)) {
                errorLog.append("- 'Fuel Burn Rate' must be a pure number (no letters/symbols).\n");
                isValid = false;
            }
            if (!capTxt.matches(numericRegex)) {
                errorLog.append("- 'Fuel Capacity' must be a pure number (no letters/symbols).\n");
                isValid = false;
            }

            // --- 3. FINAL EXECUTION ---
            if (!isValid) {
                JOptionPane.showMessageDialog(frame, errorLog.toString(), "Input Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                // Now that we KNOW they are pure numbers, parsing is safe
                BusClass currentBus = bManager.busList.get(selectedRow);
                currentBus.setMake(makeVal);
                currentBus.setModel(modelVal);
                currentBus.setType(typeVal);
                currentBus.setCruiseSpeed(Double.parseDouble(speedTxt));
                currentBus.setFuelBurnRate(Double.parseDouble(burnTxt));
                currentBus.setFuelCapacity(Double.parseDouble(capTxt));

                // Update Table
                busTable.setValueAt(makeVal, selectedRow, 0);
                busTable.setValueAt(modelVal, selectedRow, 1);
                busTable.setValueAt(typeVal, selectedRow, 2);
                busTable.setValueAt(speedTxt, selectedRow, 3);
                busTable.setValueAt(burnTxt, selectedRow, 4);
                busTable.setValueAt(capTxt, selectedRow, 5);

                try {
                    bManager.save();
                    JOptionPane.showMessageDialog(frame, "Changes Saved!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        // Remove Button
        removeBus.addActionListener(e -> {
            if (bManager.removeBus(selectedRow)) {
                busTable.removeRow(selectedRow);
            }
        });

        // New Bus
        newBus.addActionListener(e -> {
            BusClass nb = new BusClass();
            bManager.busList.add(nb);
            String s = ((BusClass) nb).displayBusInfo();
            String[] col = s.split(", ");
            busTable.addRow(new Object[] { col[0], col[1], col[2], col[3], col[4], col[5] });
            selectedRow = busTable.getRowCount() - 1;
            makeBox.setText(bManager.busList.get(selectedRow).getMake());
            modelBox.setText(bManager.busList.get(selectedRow).getModel());
            typeBox.setText(bManager.busList.get(selectedRow).getType());
            cruiseSpeedBox.setText(String.valueOf(bManager.busList.get(selectedRow).getCruiseSpeed()));
            fuelBurnRateBox.setText(String.valueOf(bManager.busList.get(selectedRow).getFuelBurnRate()));
            fuelCapacityBox.setText(String.valueOf(bManager.busList.get(selectedRow).getFuelCapacity()));
        });

        return buspanel;
    }

    // Add / Edit / remove Bus page
    private JPanel manageBusStation() {
        JPanel stationpanel = new JPanel(new BorderLayout());

        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        String tablename[] = { "Name", "Latitude", "Longitude" };

        // FIX 1: Remove "DefaultTableModel" prefix to use the class-level variable
        stationTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(stationTable);

        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        JScrollPane pane = new JScrollPane(table);

        // Populate table from sManager
        for (Object st : sManager.stationList) {
            BusStationClass station = (BusStationClass) st;
            stationTable.addRow(new Object[] {
                    station.getName(),
                    station.getLatitude(),
                    station.getLongitude()
            });
        }

        stationpanel.add(pane, BorderLayout.CENTER);

        JPanel stationWrapper = new JPanel();
        stationWrapper.setLayout(new BoxLayout(stationWrapper, BoxLayout.Y_AXIS));
        stationWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Dimension boxSize = new Dimension(800, 40);

        // UI Fields
        JLabel sName = new JLabel("Station Name:");
        sName.setFont(labelFont);
        JTextField sNameBox = new JTextField(15);
        sNameBox.setFont(inputFont);
        sNameBox.setMaximumSize(boxSize);

        JLabel latitude = new JLabel("Latitude:");
        latitude.setFont(labelFont);
        JTextField latitudeBox = new JTextField(15);
        latitudeBox.setFont(inputFont);
        latitudeBox.setMaximumSize(boxSize);

        JLabel longitude = new JLabel("Longitude:");
        longitude.setFont(labelFont);
        JTextField longitudeBox = new JTextField(15);
        longitudeBox.setFont(inputFont);
        longitudeBox.setMaximumSize(boxSize);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(sName);
        inputPanel.add(sNameBox);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(latitude);
        inputPanel.add(latitudeBox);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(longitude);
        inputPanel.add(longitudeBox);

        JButton submitStation = new JButton("Submit");
        JButton removeStation = new JButton("Remove");
        JButton newStation = new JButton("New Station");
        submitStation.setFont(labelFont);
        removeStation.setFont(labelFont);
        newStation.setFont(labelFont);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitStation);
        buttonPanel.add(removeStation);
        buttonPanel.add(newStation);

        stationWrapper.add(inputPanel);
        stationWrapper.add(buttonPanel);
        stationpanel.add(stationWrapper, BorderLayout.WEST);

        // FIX 2: Correct the Selection Listener to use Station variables
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    // Get from sManager, NOT bManager
                    BusStationClass s = sManager.stationList.get(selectedRow);
                    sNameBox.setText(s.getName());
                    latitudeBox.setText(String.valueOf(s.getLatitude()));
                    longitudeBox.setText(String.valueOf(s.getLongitude()));
                } else {
                    sNameBox.setText("");
                    latitudeBox.setText("");
                    longitudeBox.setText("");
                }
            }
        });

        submitStation.addActionListener(e -> {
            // 1. Check if a row is actually selected
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a station first.");
                return;
            }

            StringBuilder errorLog = new StringBuilder();
            boolean isValid = true;

            // 2. Get and trim input values
            String nameVal = sNameBox.getText().trim();
            String latTxt = latitudeBox.getText().trim();
            String lonTxt = longitudeBox.getText().trim();

            // 3. CASE-INSENSITIVE DUPLICATE CHECK
            // We loop through the manager's list and compare names, skipping the selected
            // row
            for (int i = 0; i < sManager.stationList.size(); i++) {
                if (i == selectedRow)
                    continue; // Don't compare the station to itself

                BusStationClass existingStation = sManager.stationList.get(i);
                if (existingStation.getName().equalsIgnoreCase(nameVal)) {
                    errorLog.append("- A station with the name '").append(nameVal).append("' already exists.\n");
                    isValid = false;
                    break; // Found a duplicate, no need to keep looking
                }
            }

            // 4. REGEX FORMAT VALIDATION
            if (!nameVal.matches("^[a-zA-Z0-9 ]+$") && nameVal.length() < 25) {
                errorLog.append("- Name must be alphanumeric and less than 25 characters.\n");
                isValid = false;
            }

            String coordRegex = "^-?[0-9]*\\.?[0-9]+$";
            if (!latTxt.matches(coordRegex) && latTxt.length() < 15) {
                errorLog.append("- Latitude must be a valid number.\n");
                isValid = false;
            }
            if (!lonTxt.matches(coordRegex) && lonTxt.length() < 15) {
                errorLog.append("- Longitude must be a valid number.\n");
                isValid = false;
            }

            // 5. FINAL SUCCESS OR FAILURE LOGIC
            if (!isValid) {
                JOptionPane.showMessageDialog(frame, errorLog.toString(), "Input Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                // Update the Station Object in the List
                BusStationClass currentStation = sManager.stationList.get(selectedRow);
                currentStation.setName(nameVal);
                currentStation.setLatitude(Double.parseDouble(latTxt));
                currentStation.setLongitude(Double.parseDouble(lonTxt));

                // Update the UI Table
                stationTable.setValueAt(nameVal, selectedRow, 0);
                stationTable.setValueAt(latTxt, selectedRow, 1);
                stationTable.setValueAt(lonTxt, selectedRow, 2);

                // Save to File/Persistence
                try {
                    sManager.save();
                    JOptionPane.showMessageDialog(frame, "Station Updated Successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving data: " + ex.getMessage(), "File Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        removeStation.addActionListener(e -> {
            if (selectedRow != -1) {
                if (sManager.removeStation(selectedRow)) {
                    stationTable.removeRow(selectedRow);
                    selectedRow = -1;
                }
            }
        });

        newStation.addActionListener(e -> {
            String baseName = "New Station";
            String finalName = baseName;
            int counter = 1;

            // Loop until we find a name that doesn't exist (New Station, New Station 1,
            // etc.)
            boolean nameExists = true;
            while (nameExists) {
                nameExists = false;
                for (BusStationClass s : sManager.stationList) {
                    if (s.getName().equalsIgnoreCase(finalName)) {
                        nameExists = true;
                        finalName = baseName + " " + counter;
                        counter++;
                        break;
                    }
                }
            }

            BusStationClass ns = new BusStationClass(finalName, 0.0, 0.0);
            sManager.stationList.add(ns);
            stationTable.addRow(new Object[] { finalName, "0.0", "0.0" });
            table.setRowSelectionInterval(stationTable.getRowCount() - 1, stationTable.getRowCount() - 1);
        });

        return stationpanel;
    }
}