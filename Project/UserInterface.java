package Project;

import Project.Bus.BusClass;
import Project.Bus.BusManager;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

//https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
// After the login it should initialize the Weighted Graph

public class UserInterface {
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private DefaultTableModel busTable;
    private JPanel loginPanel;
    private JPanel dashboardPanel;
    private JPanel buspanel;
    private BusManager bManager;
    int selectedRow = -1;

    public UserInterface() {
        try {
            bManager = new BusManager();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bManager.listBuses();
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

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Actions");
        JMenuItem manageBus = new JMenuItem("Manage Bus");
        JMenuItem logoutItem = new JMenuItem("Logout");

        manageBus.addActionListener(e -> {
            cardLayout.show(cardPanel, "MANAGEBUS"); // Route back to login
            frame.revalidate(); // Re-layout the frame with the new menu
        });

        logoutItem.addActionListener(e -> {
            frame.setJMenuBar(null); // Remove the top menu dynamically
            cardLayout.show(cardPanel, "LOGIN"); // Route back to login
            frame.revalidate(); // Re-layout the frame with the new menu
        });

        menu.add(manageBus);
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

    // current account dialog
    private void showAddAccountDialog() {

        JDialog dialog = new JDialog(frame, "Create New Account", true);
        dialog.setLayout(new FlowLayout());

        // dialog box for new usernmae
        dialog.add(new JLabel("New Username:"));
        dialog.add(new JTextField(12));

        // dialog box for a new password
        dialog.add(new JLabel("New Password:"));
        dialog.add(new JTextField(12));

        // dialog box for verifying the new password is the same
        dialog.add(new JLabel("Verify Password:"));
        dialog.add(new JTextField(12));

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Account stored successfully!");
            dialog.dispose(); // Closes the dialog and returns to the login screen
        });

        dialog.add(submitBtn);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true); // Halts execution here until dialog is disposed
    }

    // Route Manager Page
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
        JPanel westWrapper = new JPanel();
        westWrapper.setLayout(new BoxLayout(westWrapper, BoxLayout.Y_AXIS));
        // Add a little padding around the left panel so it isn't squeezed against the
        // edge
        westWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        westWrapper.add(inputPanel);
        westWrapper.add(buttonPanel);

        // Add the single wrapper panel to the West
        buspanel.add(westWrapper, BorderLayout.WEST);

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
}
