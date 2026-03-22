package Project;

import Project.Bus.BusManager;
import Project.Bus.BusClass;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

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
        frame.setSize(800, 600);
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
        buspanel = new JPanel(new BorderLayout());

        // Current Buses Table
        String tablename[] = { "Current Busses" };
        busTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(busTable);
        JScrollPane pane = new JScrollPane(table);

        buspanel.add(pane, BorderLayout.EAST);

        // --- NEW WRAPPER PANEL FOR THE WEST SIDE ---
        // We use a vertical BoxLayout so the inputs sit above the buttons
        JPanel westWrapper = new JPanel();
        westWrapper.setLayout(new BoxLayout(westWrapper, BoxLayout.Y_AXIS));

        Dimension boxSize = new Dimension(500, 150);

        // 1. Input Boxes Panel (BoxLayout)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS)); // Vertical stacking

        // Bus Name Input
        JLabel name = new JLabel("Name:");
        JTextField nameBox = new JTextField(15); // Suggested column width
        nameBox.setMaximumSize(boxSize);
        inputPanel.add(name);
        inputPanel.add(nameBox);

        // Example of a second input
        JLabel model = new JLabel("Model:");
        JTextField modelBox = new JTextField(15);
        modelBox.setMaximumSize(boxSize);
        inputPanel.add(model);
        inputPanel.add(modelBox);

        JLabel type = new JLabel("Type:");
        JTextField typeBox = new JTextField(15);
        typeBox.setMaximumSize(boxSize);
        inputPanel.add(type);
        inputPanel.add(typeBox);

        JLabel cruiseSpeed = new JLabel("Cruise Speed:");
        JTextField cruiseSpeedBox = new JTextField(15);
        cruiseSpeedBox.setMaximumSize(boxSize);
        inputPanel.add(cruiseSpeed);
        inputPanel.add(cruiseSpeedBox);

        JLabel fuelBurnRate = new JLabel("Fuel Burn Rate:");
        JTextField fuelBurnRateBox = new JTextField(15);
        fuelBurnRateBox.setMaximumSize(boxSize);
        inputPanel.add(fuelBurnRate);
        inputPanel.add(fuelBurnRateBox);

        JLabel fuelCapacity = new JLabel("Fuel Capacity:");
        JTextField fuelCapacityBox = new JTextField(15);
        fuelCapacityBox.setMaximumSize(boxSize);
        inputPanel.add(fuelCapacity);
        inputPanel.add(fuelCapacityBox);

        inputPanel.add(Box.createVerticalGlue());

        // 2. Button Panel (FlowLayout)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout()); // Default is side-by-side

        JButton addBtn = new JButton("Submit");
        JButton removeBtn = new JButton("Remove");
        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);

        // 3. Add the input and button panels to the wrapper
        westWrapper.add(inputPanel);
        westWrapper.add(buttonPanel);

        // Add the single wrapper panel to the West of the main layout
        buspanel.add(westWrapper, BorderLayout.WEST);

        return buspanel;
    }
}
