package Project;

/**
 * These are the imports required for the UI, Data Management, 
 * and Security features.
 */
import Project.Bus.*;
import Project.BusStation.*;
import Project.Route.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * The UserInterface class serves as the main entry point for the GUI.
 * It manages the different "pages" of the application using a CardLayout.
 */
public class UserInterface {

    //  GUI Components 
    private JFrame frame;
    private JPanel cardPanel;       // The container that holds different screens
    private CardLayout cardLayout; // The manager that swaps between screens
    private JPanel loginPanel;
    private JPanel routePanel;
    private JPanel buspanel;
    private JPanel stationpanel;

    //  Table Models for Data Display 
    private DefaultTableModel busTable;
    private DefaultTableModel stationTable;

    //  Logic & Data Managers 
    private BusManager bManager;
    private BusStationManager sManager;
    private WeightedGraph routeGraph;
    private RoutePlanner routePlanner;

    //  Interactive UI Elements 
    JComboBox<String> busDropdown = new JComboBox<>();
    JComboBox<String> stationDropDown = new JComboBox<>();
    private int selectedRow = -1; // Tracks which row is selected in tables

    /**
     * Constructor: Initializes the backend managers and data structures.
     * It attempts to load the necessary data before the UI is built.
     */
    public UserInterface() {
        try {
            // Instantiate the Bus management logic
            bManager = new BusManager();

            // Instantiate the Station management logic
            sManager = new BusStationManager();

            // Create the graph structure for the map/routes
            routeGraph = new WeightedGraph();

            // Link the route planner to the graph
            routePlanner = new RoutePlanner(routeGraph);

        } catch (FileNotFoundException e) {
            // Error handling if data files are missing
            e.printStackTrace();
        }
    }

    /**
     * Main method: The starting point of the Java application.
     */
    public static void main(String[] args) {
        // Show a security disclaimer before the app opens
        JOptionPane.showMessageDialog(null,
                "System Warning: Unauthorized use is prohibited. Click OK to proceed.",
                "Security Alert",
                JOptionPane.WARNING_MESSAGE);
        
        // Launch the UI
        new UserInterface().initialize();
    }

    /**
     * Initialize: Configures the JFrame, loads CSV data into memory, 
     * and constructs the visual layout.
     */
    public void initialize() {

        // Load data from external CSV files into the Manager lists
        bManager.listBuses();
        sManager.listStations();

        // Build the graph using the loaded stations and the connection CSV
        // NOTE: Check the spelling of "WeigthedGraph.csv" (See Error list below)
        routeGraph.buildGraphFromCSV(sManager, "Project/Route/WeightedGraph.csv");

        //  JFrame Setup 
        frame = new JFrame("Route Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Opens the app in full screen
        frame.setLocationRelativeTo(null); // Centers the frame (though MAXIMIZED overrides this)

        //  CardLayout Setup 
        // CardLayout allows us to "stack" panels and show only one at a time
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add the various functional panels to the stack
        // These methods (logInPanel, etc.) must return a JPanel object
        cardPanel.add(logInPanel(), "LOGIN");
        cardPanel.add(routePanel(), "ROUTEPLANNER");
        cardPanel.add(manageBus(), "MANAGEBUS");
        cardPanel.add(manageBusStation(), "MANAGESTATION");

        // Finalize the frame and make it visible to the user
        frame.add(cardPanel);
        frame.setVisible(true);
    }

    /**
     * createMenuBar: Generates a navigation bar that changes based 
     * on the user's permission level (Admin vs Regular).
     * currentUser is The Account object representing the logged-in user.
     * it will return A fully constructed JMenuBar.
     */
    private JMenuBar createMenuBar(Account currentUser) {

        // Initialize the Bar and the primary Menu category
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        // Define the individual clickable navigation items
        JMenuItem routePlanner = new JMenuItem("Route Planner");
        JMenuItem manageBus = new JMenuItem("Manage Bus");
        JMenuItem manageStation = new JMenuItem("Manage Station");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exit = new JMenuItem("EXIT");

        // Styling the menu fonts for readability
        Font menuFont = new Font("Arial", Font.PLAIN, 18);
        menu.setFont(menuFont);
        routePlanner.setFont(menuFont);
        manageBus.setFont(menuFont);
        manageStation.setFont(menuFont);
        logoutItem.setFont(menuFont);
        exit.setFont(menuFont);

        //  Admin Specific Feature 
        // Check if the user has admin privileges before showing the account list option
        if (currentUser.isAdmin()) {
            JMenuItem seeAccounts = new JMenuItem("See All Accounts");
            seeAccounts.setFont(menuFont);

            // Listener to open a popup showing all registered accounts
            seeAccounts.addActionListener(e -> showAllAccountsDialog());
            menu.addSeparator(); 
            menu.add(seeAccounts); 
            menu.addSeparator();
        }

        //  Action Listeners for Navigation 

        // Exit: Closes the entire application
        exit.addActionListener(e -> {
            frame.dispose();
        });

        // Switch to Route Planner: Also refreshes dropdown data
        routePlanner.addActionListener(e -> {
            // Swap the view
            cardLayout.show(cardPanel, "ROUTEPLANNER");

            // Clear and Re-populate the Bus dropdown from the latest bManager list
            busDropdown.removeAllItems();
            for (Object bObj : bManager.busList) {
                BusClass b = (BusClass) bObj;
                busDropdown.addItem(b.getMake() + " " + b.getModel());
            }

            // Clear and Re-populate the Station dropdown from the latest sManager list
            stationDropDown.removeAllItems();
            for (Object sObj : sManager.stationList) {
                BusStationClass s = (BusStationClass) sObj;
                stationDropDown.addItem(s.getName());
            }

            // Tell the UI to redraw/update
            frame.revalidate();
            selectedRow = -1; // Reset selection to prevent modifying wrong data
        });

        // Switch to Bus Manager view
        manageBus.addActionListener(e -> {
            cardLayout.show(cardPanel, "MANAGEBUS");
            frame.revalidate();
            selectedRow = -1;
        });

        // Switch to Station Manager view
        manageStation.addActionListener(e -> {
            cardLayout.show(cardPanel, "MANAGESTATION");
            frame.revalidate();
            selectedRow = -1;
        });

        // Logout logic: Removes the menu bar and returns to the login screen
        logoutItem.addActionListener(e -> {
            frame.setJMenuBar(null); // Hide the menu so logged-out users can't navigate
            cardLayout.show(cardPanel, "LOGIN");
            frame.revalidate();
        });

        // Assemble the menu items into the menu, then the menu into the bar
        menu.add(routePlanner);
        menu.add(manageBus);
        menu.add(manageStation);
        menu.add(logoutItem);
        menu.add(exit);
        menuBar.add(menu);

        return menuBar;
    }

/**
     * Constructs the Login Panel UI.
     * Uses GridBagLayout for precise positioning of labels, fields, and buttons.
     */
    private JPanel logInPanel() {
        // Initialize the panel with a GridBagLayout manager
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding around components

        // Create input components
        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15); // Masks characters for security
        JButton loginBtn = new JButton("Login");
        JButton addAccountBtn = new JButton("Add Account");

        // Layout: Username Label and Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(userField, gbc);

        // Layout: Password Label and Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passField, gbc);

        // Layout: Buttons (Grouped in a sub-panel for better alignment)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span across two columns
        JPanel btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(addAccountBtn);
        loginPanel.add(btnPanel, gbc);

        /**
         * Action Listener for the Login Button.
         * Validates input, hashes the password, and checks the CSV database.
         */
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            // Convert char array to String (Standard practice for JPasswordField)
            String password = new String(passField.getPassword());

            //  Client-side Validation 
            if (username.length() < 3) {
                JOptionPane.showMessageDialog(frame, "Username must be at least 3 characters long!", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return; // Stop execution
            }

            if (password.length() < 5) {
                JOptionPane.showMessageDialog(frame, "Password must be at least 5 characters long!", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            //  Server-side Verification 
            // Retrieve the hashed version of the password from the CSV
            String storedHash = getStoredPasswordHash(username);
            if (storedHash == null) {
                JOptionPane.showMessageDialog(frame, "User not found, please create an account first!",
                        "Account Required", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Hash the user's input to see if it matches the stored hash
            String inputHash = hashPassword(password);
            if (inputHash.equals(storedHash)) {
                // Successful Login
                String roleCSV = getRoleForUser(username); 
                Account loggedInAccount = new Account(username, inputHash, roleCSV); 
                
                // Set up the menu bar based on user permissions
                frame.setJMenuBar(createMenuBar(loggedInAccount)); 

                // Switch to the main application view
                cardLayout.show(cardPanel, "ROUTEPLANNER");
                frame.revalidate();
                
                // Clear fields for security
                userField.setText("");
                passField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Incorrect password! Please try again.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Opens the account creation dialog
        addAccountBtn.addActionListener(e -> {
            showAddAccountDialog();
        });

        return loginPanel;
    }

    /**
     * Reads the Accounts CSV to determine the user's role (ADMIN or USER).
     */
    private String getRoleForUser(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("Project\\Accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                // Index 0: User, Index 1: Hash, Index 2: Role
                if (parts.length >= 3 && parts[0].equalsIgnoreCase(username)) {
                    return parts[2].trim(); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "USER"; // Default fallback
    }

    /**
     * Admin Tool: Displays a list of all accounts stored in the CSV.
     */
    private void showAllAccountsDialog() {
        JDialog managementDialog = new JDialog(frame, "Account Management", true);
        managementDialog.setLayout(new BorderLayout(10, 10));

        // Use a ListModel so we can dynamically update the UI list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> accountList = new JList<>(listModel);
        
        refreshAccountList(listModel);

        JButton removeBtn = new JButton("Remove Selected Account");
        removeBtn.addActionListener(e -> {
            String selected = accountList.getSelectedValue();
            if (selected != null) {
                // Extract the username part before the dash
                String userToRemove = selected.split(" - ")[0];
                performAccountRemoval(userToRemove);
                refreshAccountList(listModel); // Refresh UI after file update
            }
        });

        managementDialog.add(new JScrollPane(accountList), BorderLayout.CENTER);
        managementDialog.add(removeBtn, BorderLayout.SOUTH);

        managementDialog.pack();
        managementDialog.setSize(300, 400);
        managementDialog.setLocationRelativeTo(frame);
        managementDialog.setVisible(true);
    }

    /**
     * Deletes an account by copying all lines EXCEPT the target user 
     * to a temporary file, then replacing the original file.
     */
    private void performAccountRemoval(String targetUser) {
        File originalFile = new File("Project\\Accounts.csv");
        File tempFile = new File("Project\\Accounts_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                // If it's not the user we want to delete, write it to the new file
                if (parts.length > 0 && !parts[0].equalsIgnoreCase(targetUser)) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // File swap logic
        if (originalFile.delete()) {
            tempFile.renameTo(originalFile);
        } else {
            JOptionPane.showMessageDialog(frame, "Error updating the database file.");
        }
    }

    /**
     * Re-reads the CSV file and populates the JList model for the Admin dialog.
     */
    private void refreshAccountList(DefaultListModel<String> model) {
        model.clear(); 
        File file = new File("Project\\Accounts.csv");
        
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length >= 3) {
                    model.addElement(parts[0].trim() + " - (" + parts[2].trim() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error refreshing list: " + e.getMessage());
        }
    }

    /**
     * UI Dialog for creating a new user or admin account.
     */
    private void showAddAccountDialog() {
        JDialog dialog = new JDialog(frame, "Create New Account", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dropdown for selecting account privilege level
        String[] roles = {"USER", "ADMIN"}; 
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        gbc.gridx = 0; gbc.gridy = 3; // Note: Adjusted position for logic
        dialog.add(new JLabel("Account Type"), gbc);
        gbc.gridx = 1; 
        dialog.add(roleComboBox, gbc);

        JTextField newUsernameField = new JTextField(15);
        JPasswordField newPasswordField = new JPasswordField(15);
        JPasswordField verifyPasswordField = new JPasswordField(15);
        JButton submitBtn = new JButton("Submit");

        // UI Layout
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("New Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(newUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(newPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Verify Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(verifyPasswordField, gbc);

        // Submit Logic
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        submitBtn.addActionListener(e -> {
            String username = newUsernameField.getText().trim();
            String password = new String(newPasswordField.getPassword());
            String verify = new String(verifyPasswordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            // Validation Checks
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!password.equals(verify)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (username.length() < 3) {
                JOptionPane.showMessageDialog(dialog, "Username too short!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (password.length() < 5) {
                JOptionPane.showMessageDialog(dialog, "Password too short!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (getStoredPasswordHash(username) != null) {
                JOptionPane.showMessageDialog(dialog, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Save to CSV if all checks pass
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("Project\\Accounts.csv", true))) {
                    String securePassword = hashPassword(password);
                    writer.write(username + ", " + securePassword + ", " + role);
                    writer.newLine();

                    JOptionPane.showMessageDialog(dialog, "Account created successfully!");
                    dialog.dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        dialog.add(submitBtn, gbc);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    /**
     * Security: Converts a plain text password into a SHA-256 Hex string.
     * This ensures passwords are never stored in plain text.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                // Convert byte to hex format
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm SHA-256 not found", e);
        }
    }

    /**
     * Reads the CSV to find the hash associated with a username.
     * return he stored hash string, or null if user doesn't exist.
     */
    private String getStoredPasswordHash(String username) {
        File file = new File("Project\\Accounts.csv");
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length >= 2 && parts[0].equalsIgnoreCase(username.trim())) {
                    return parts[1]; 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

/**
     * Builds and returns the main Route Planning panel.
     * Note: This method assumes `bManager`, `sManager`, `frame`, `routeGraph`, 
     * and `routePlanner` are declared as class-level instance variables.
     */
    private JPanel routePanel() {
        // Main container using BorderLayout (North, South, East, West, Center)
        JPanel routePan = new JPanel(new BorderLayout());

        // We use a CardLayout for the left panel so we can swap between the "Controls" view
        // and the "Results" view without opening a new window.
        JPanel leftCardPanel = new JPanel(new CardLayout());
        CardLayout leftLayout = (CardLayout) leftCardPanel.getLayout();

        // Fix the left panel's width to 450px, letting height stretch automatically
        leftCardPanel.setPreferredSize(new Dimension(450, 0));

        // --- CONTROLS PANEL SETUP ---
        // BoxLayout with Y_AXIS stacks components vertically from top to bottom
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Reusable fonts for UI consistency
        Font largeFont = new Font("SansSerif", Font.PLAIN, 18);
        Font boldFont = new Font("SansSerif", Font.BOLD, 18);

        // Header for the route building section
        JLabel routeLabel = new JLabel("Build Route");
        routeLabel.setFont(boldFont);
        routeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel busLabel = new JLabel("Assigned Bus:");
        busLabel.setFont(boldFont);
        busLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIX: Declared busDropdown and stationDropDown locally to prevent compilation errors.
        // If these are class-level variables, remove the 'JComboBox<String>' type declaration here.
        JComboBox<String> busDropdown = new JComboBox<>();
        JComboBox<String> stationDropDown = new JComboBox<>();

        // Populate the Bus selection dropdown
        busDropdown.setFont(largeFont);
        busDropdown.setMaximumSize(new Dimension(400, 40));
        for (Object bObj : bManager.busList) {
            BusClass b = (BusClass) bObj;
            busDropdown.addItem(b.getMake() + " " + b.getModel());
        }

        // Add bus selection elements to the control panel
        controlPanel.add(busLabel);
        controlPanel.add(busDropdown);
        controlPanel.add(Box.createVerticalStrut(15)); // Adds visual vertical spacing

        // Populate the Station selection dropdown
        stationDropDown.setFont(largeFont);
        for (BusStationClass s : sManager.stationList) {
            stationDropDown.addItem(s.getName());
        }
        stationDropDown.setMaximumSize(new Dimension(400, 40));

        JButton addBtn = new JButton("Add Station");
        addBtn.setFont(largeFont);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Use DefaultListModel to allow dynamic adding/removing of route stops in the JList
        DefaultListModel<String> routeStopsModel = new DefaultListModel<>();
        JList<String> routeStopsList = new JList<>(routeStopsModel);
        routeStopsList.setFont(largeFont);
        
        // Wrap the list in a scroll pane in case the user adds many stops
        JScrollPane listScroller = new JScrollPane(routeStopsList);
        listScroller.setPreferredSize(new Dimension(400, 300));

        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.setFont(largeFont);
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton makeRouteBtn = new JButton("Calculate Route");
        makeRouteBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        makeRouteBtn.setBackground(new Color(200, 230, 255)); // Light blue color
        makeRouteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- GRAPH EDGE MODIFICATION SECTION ---
        JLabel edgeLabel = new JLabel("Add Connection (Edge)");
        edgeLabel.setFont(boldFont);
        edgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<String> edgeFromDrop = new JComboBox<>();
        JComboBox<String> edgeToDrop = new JComboBox<>();
        edgeFromDrop.setFont(largeFont);
        edgeToDrop.setFont(largeFont);
        edgeFromDrop.setMaximumSize(new Dimension(400, 40));
        edgeToDrop.setMaximumSize(new Dimension(400, 40));

        // Populate from/to dropdowns for managing graph edges
        for (BusStationClass s : sManager.stationList) {
            edgeFromDrop.addItem(s.getName());
            edgeToDrop.addItem(s.getName());
        }

        JButton addEdgeBtn = new JButton("Add Connect");
        addEdgeBtn.setFont(largeFont);
        addEdgeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton removeEdgeBtn = new JButton("Remove Connection");
        removeEdgeBtn.setFont(largeFont);
        removeEdgeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble the rest of the control panel
        controlPanel.add(routeLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stationDropDown);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(addBtn);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(listScroller);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(removeBtn);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(makeRouteBtn);
        controlPanel.add(Box.createVerticalStrut(30));
        controlPanel.add(new JSeparator()); // Horizontal line separator
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(edgeLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(new JLabel("From:"));
        controlPanel.add(edgeFromDrop);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(new JLabel("To:"));
        controlPanel.add(edgeToDrop);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(addEdgeBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(removeEdgeBtn);

        // --- RESULTS PANEL SETUP ---
        JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel resultsHeader = new JLabel("Route Itinerary", SwingConstants.CENTER);
        resultsHeader.setFont(new Font("SansSerif", Font.BOLD, 24));

        // Text area to display final calculations
        JTextArea resultsTextArea = new JTextArea();
        resultsTextArea.setFont(new Font("SansSerif", Font.PLAIN, 20));
        resultsTextArea.setEditable(false); // Make it read-only
        resultsTextArea.setLineWrap(true);
        resultsTextArea.setWrapStyleWord(true); // Wrap at word boundaries, not mid-word
        JScrollPane resultsScroller = new JScrollPane(resultsTextArea);

        // Button to flip back to the building controls
        JButton backBtn = new JButton("Back to Route Builder");
        backBtn.setFont(boldFont);
        backBtn.setBackground(new Color(255, 200, 200));

        resultsPanel.add(resultsHeader, BorderLayout.NORTH);
        resultsPanel.add(resultsScroller, BorderLayout.CENTER);
        resultsPanel.add(backBtn, BorderLayout.SOUTH);

        // Add both panels to the CardLayout container
        leftCardPanel.add(controlPanel, "CONTROLS");
        leftCardPanel.add(resultsPanel, "RESULTS");

        // The center panel handles drawing the custom map/graph
        GraphPanel centerPanel = new GraphPanel();

        // Add the left menu and center map to the main layout
        routePan.add(leftCardPanel, BorderLayout.WEST);
        routePan.add(centerPanel, BorderLayout.CENTER);

        // --- ACTION LISTENERS (BUTTON BEHAVIORS) ---

        // Adds the currently selected station to the route list
        addBtn.addActionListener(e -> {
            String selected = (String) stationDropDown.getSelectedItem();
            if (selected != null) {
                routeStopsModel.addElement(selected);
            }
        });

        // Removes the highlighted station from the route list
        removeBtn.addActionListener(e -> {
            int selectedIdx = routeStopsList.getSelectedIndex();
            if (selectedIdx != -1) {
                routeStopsModel.remove(selectedIdx);
            }
        });

        // Removes an edge (road/connection) between two stations
        removeEdgeBtn.addActionListener(e -> {
            String fromName = (String) edgeFromDrop.getSelectedItem();
            String toName = (String) edgeToDrop.getSelectedItem();

            if (fromName == null || toName == null || fromName.equals(toName)) {
                JOptionPane.showMessageDialog(frame, "Select two different valid stations.");
                return;
            }

            Node n1 = routeGraph.getNodeByName(fromName);
            Node n2 = routeGraph.getNodeByName(toName);

            // Prevent NullPointerException if the station was deleted from the system
            if (n1 == null || n2 == null) {
                JOptionPane.showMessageDialog(frame,
                        "Error: One of these stations no longer exists.\nPlease close and reopen the Route Builder to refresh the dropdown lists.",
                        "Station Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            routeGraph.removeEdge(n1, n2);
            routeGraph.rewriteCSV("Project/Route/WeightedGraph.csv"); 
            JOptionPane.showMessageDialog(frame, "Connection severed and saved.");

            centerPanel.repaint(); // Redraw map to reflect missing connection
        });

        // Adds an edge (road/connection) between two stations
        addEdgeBtn.addActionListener(e -> {
            String fromName = (String) edgeFromDrop.getSelectedItem();
            String toName = (String) edgeToDrop.getSelectedItem();

            if (fromName == null || toName == null || fromName.equals(toName)) {
                JOptionPane.showMessageDialog(frame, "Cannot connect a station to itself.");
                return;
            }

            Node n1 = routeGraph.getNodeByName(fromName);
            Node n2 = routeGraph.getNodeByName(toName);

            if (n1 == null || n2 == null) {
                JOptionPane.showMessageDialog(frame,
                        "Error: One of these stations no longer exists.\nPlease close and reopen the Route Builder to refresh the dropdown lists.",
                        "Station Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if the edge already exists to prevent duplicate lines
            boolean alreadyConnected = false;
            for (Edge edge : n1.getEdges()) {
                if (edge.getTo().equals(n2)) {
                    alreadyConnected = true;
                    break;
                }
            }

            if (alreadyConnected) {
                JOptionPane.showMessageDialog(frame, "These stations are already connected!");
                return;
            }

            routeGraph.addEdge(n1, n2);
            // FIX: Fixed typo in filename here as well
            routeGraph.appendEdgeToCSV(fromName, toName, "Project/Route/WeightedGraph.csv");
            JOptionPane.showMessageDialog(frame,
                    "Road connected between " + fromName + " and " + toName + " and saved!");

            centerPanel.repaint(); // Redraw map to show new connection
        });

        // Flips the CardLayout back to the controls view
        backBtn.addActionListener(e -> leftLayout.show(leftCardPanel, "CONTROLS"));

        // CORE LOGIC: Calculates the actual path and fuel statistics
        makeRouteBtn.addActionListener(e -> {
            if (routeStopsModel.size() < 2) {
                JOptionPane.showMessageDialog(frame, "Please add at least 2 stations to the list to create a route.");
                return;
            }

            java.util.List<Node> finalRoute = new ArrayList<>();
            double totalDistance = 0.0;

            // Loop through each consecutive pair of stations in the user's list
            for (int i = 0; i < routeStopsModel.size() - 1; i++) {
                Node currentStation = routeGraph.getNodeByName(routeStopsModel.get(i));
                Node nextStation = routeGraph.getNodeByName(routeStopsModel.get(i + 1));

                // Ask the routePlanner (Dijkstra/A* algorithm likely) for the shortest path
                java.util.List<Node> legPath = routePlanner.getShortestPath(currentStation, nextStation);

                // FIX: Added 'legPath == null' safeguard
                if (legPath == null || legPath.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Pathfinding failed!\n\nThe bus gets stuck at: " + currentStation.getStation().getName() +
                                    "\nIt cannot reach: " + nextStation.getStation().getName() +
                                    "\n\nPlease add a connecting edge to complete this route.",
                            "Route Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Calculate the distance of this specific leg by checking edge weights
                for (int j = 0; j < legPath.size() - 1; j++) {
                    Node a = legPath.get(j);
                    Node b = legPath.get(j + 1);
                    for (Edge edge : a.getEdges()) {
                        if (edge.getTo().equals(b)) {
                            totalDistance += edge.getWeight();
                            break;
                        }
                    }
                }

                // FIX: Used subList instead of .remove(0). 
                // If legPath is an immutable list, .remove(0) throws an UnsupportedOperationException.
                // We skip the first node on subsequent legs so we don't list the same station twice (e.g. A->B, B->C).
                if (i > 0 && !legPath.isEmpty()) {
                    finalRoute.addAll(legPath.subList(1, legPath.size()));
                } else {
                    finalRoute.addAll(legPath);
                }
            }

            // Grab coordinates for heading calculation
            Node firstStation = routeGraph.getNodeByName(routeStopsModel.firstElement());
            Node lastStation = routeGraph.getNodeByName(routeStopsModel.lastElement());

            double startLat = firstStation.getStation().getLatitude();
            double startLon = firstStation.getStation().getLongitude();
            double endLat = lastStation.getStation().getLatitude();
            double endLon = lastStation.getStation().getLongitude();

            String overallHeading = routePlanner.calculateHeading(startLat, startLon, endLat, endLon);

            // Calculate physics and fuel limits based on chosen bus
            int selectedBusIdx = busDropdown.getSelectedIndex();
            BusClass selectedBus = (BusClass) bManager.busList.get(selectedBusIdx);

            double speed = selectedBus.getCruiseSpeed();
            double burnRate = selectedBus.getFuelBurnRate();
            double capacity = selectedBus.getFuelCapacity();

            // Prevent division by zero if speed is 0
            double timeRequired = speed > 0 ? totalDistance / speed : 0;
            // Note: This assumes burnRate is in 'gallons per hour'. If it is MPG, it should be (totalDistance / burnRate)
            double fuelRequired = timeRequired * burnRate; 

            boolean canComplete = (fuelRequired <= capacity) && (speed > 0);

            // Build the formatted results string
            StringBuilder sb = new StringBuilder();
            sb.append("Heading: ").append(overallHeading);
            sb.append("\nTotal Distance: ").append(String.format("%.2f", totalDistance)).append(" miles\n");
            sb.append("Bus Selected: ").append(selectedBus.getMake()).append(" ").append(selectedBus.getModel()).append("\n");
            sb.append("Est. Trip Time: ").append(String.format("%.2f", timeRequired)).append(" hours\n");
            sb.append("Est. Fuel Required: ").append(String.format("%.2f", fuelRequired)).append(" gallons\n");
            sb.append("Fuel Capacity: ").append(String.format("%.2f", capacity)).append(" gallons\n\n");
            
            if (canComplete) {
                sb.append("ROUTE APPROVED\n");
            } else {
                if (speed <= 0) {
                    sb.append("ROUTE FAILED (Bus cruise speed is 0)\n");
                } else {
                    sb.append("ROUTE FAILED (Insufficient Fuel Capacity)\n");
                }
            }
            sb.append("--\n\n");

            // Print the final station-by-station itinerary
            for (int i = 0; i < finalRoute.size(); i++) {
                sb.append("Stop ").append(i + 1).append(": \n");
                sb.append("   ").append(finalRoute.get(i).getStation().getName()).append("\n\n");
            }

            // Display results and swap the CardLayout view
            resultsTextArea.setText(sb.toString());
            leftLayout.show(leftCardPanel, "RESULTS");
        });
        
        return routePan;
    }

    /**
     * Inner class responsible for drawing the nodes and edges on a custom 2D canvas.
     */
    private class GraphPanel extends JPanel {

        public GraphPanel() {
            setBackground(Color.DARK_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Always call super to ensure proper rendering

            Graphics2D g2d = (Graphics2D) g;
            // Enable anti-aliasing for smooth lines and text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (routeGraph.vertices == null || routeGraph.vertices.isEmpty())
                return;

            // Step 1: Find the bounding box (Min/Max Lat and Lon) to dynamically scale the map
            double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
            double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

            for (Node n : routeGraph.vertices) {
                double lat = n.getStation().getLatitude();
                double lon = n.getStation().getLongitude();
                if (lat < minLat) minLat = lat;
                if (lat > maxLat) maxLat = lat;
                if (lon < minLon) minLon = lon;
                if (lon > maxLon) maxLon = lon;
            }

            // Define margins so dots don't draw exactly on the edge of the screen
            int paddingX = 150;
            int paddingY = 60;
            int usableWidth = getWidth() - (2 * paddingX);
            int usableHeight = getHeight() - (2 * paddingY);

            g2d.setStroke(new BasicStroke(2)); // Thicker lines for roads

            // Step 2: Draw the edges (Lines) FIRST so they appear underneath the nodes
            for (Node n : routeGraph.vertices) {
                int x1 = mapLonToX(n.getStation().getLongitude(), minLon, maxLon, usableWidth) + paddingX;
                int y1 = mapLatToY(n.getStation().getLatitude(), minLat, maxLat, usableHeight) + paddingY;

                for (Edge e : n.getEdges()) {
                    Node target = e.getTo();
                    int x2 = mapLonToX(target.getStation().getLongitude(), minLon, maxLon, usableWidth) + paddingX;
                    int y2 = mapLatToY(target.getStation().getLatitude(), minLat, maxLat, usableHeight) + paddingY;

                    // Draw connecting line
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawLine(x1, y1, x2, y2);

                    // Calculate midpoint of the line to draw the distance text
                    int midX = (x1 + x2) / 2;
                    int midY = (y1 + y2) / 2;

                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                    String weightText = String.format("%.1f mi", e.getWeight());
                    g2d.drawString(weightText, midX, midY - 5);
                }
            }

            // Step 3: Draw the Nodes (Stations) SECOND so they sit on top of the lines
            int nodeSize = 16;
            for (Node n : routeGraph.vertices) {
                int x = mapLonToX(n.getStation().getLongitude(), minLon, maxLon, usableWidth) + paddingX;
                int y = mapLatToY(n.getStation().getLatitude(), minLat, maxLat, usableHeight) + paddingY;

                // Special icon for Refuel Stations, regular dot for standard stations
                if (n.getStation() instanceof RefuelBusStation) {
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
                    g2d.drawString("⛽", x - 12, y + 7);
                } else {
                    g2d.setColor(new Color(200, 50, 50)); // Red dot
                    g2d.fillOval(x - (nodeSize / 2), y - (nodeSize / 2), nodeSize, nodeSize);
                }

                // Draw Station Name
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                g2d.drawString(n.getStation().getName(), x + 15, y + 4);

                // Draw Coordinates slightly smaller and below the name
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                String coordText = String.format("Lat: %.3f, Lon: %.3f",
                        n.getStation().getLatitude(), n.getStation().getLongitude());
                g2d.drawString(coordText, x + 15, y + 20);
            }
        }

        /**
         * Normalizes a Longitude value to an X pixel coordinate.
         */
        private int mapLonToX(double lon, double minLon, double maxLon, int width) {
            // Prevent divide-by-zero if there's only one station or all have identical longitude
            if (maxLon == minLon) return width / 2;
            
            // Percentage formula: (current - min) / (max - min)
            return (int) (((lon - minLon) / (maxLon - minLon)) * width);
        }

        /**
         * Normalizes a Latitude value to a Y pixel coordinate.
         * Note: Y is inverted (height - value) because screen pixel Y-coordinates 
         * increase top-to-bottom, while map Latitude increases bottom-to-top (South to North).
         */
        private int mapLatToY(double lat, double minLat, double maxLat, int height) {
            // Prevent divide-by-zero
            if (maxLat == minLat) return height / 2;
            
            return height - (int) (((lat - minLat) / (maxLat - minLat)) * height);
        }
    }
    /**
     * Constructs the Bus Management panel.
     * Uses a BorderLayout: Table in the Center, Form on the West (Left).
     */
    private JPanel manageBus() {
        // The main container for this section of the app
        JPanel buspanel = new JPanel(new BorderLayout());

        //  FONT SETTINGS 
        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        //  TABLE INITIALIZATION 
        // Define the columns based on Bus properties
        String tablename[] = { "Make", "Model", "Type", "Fuel Type", "Fuel Capacity", "Fuel Burn Rate", "Cruise Speed" };
        
        // DefaultTableModel allows us to modify rows (add/remove/update) at runtime
        DefaultTableModel busTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(busTable);

        // Styling the table for better visibility
        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));

        // JScrollPane handles overflow if the list of buses is long
        JScrollPane pane = new JScrollPane(table);

        //  DATA LOADING 
        // Iterate through the backend bus list and populate the UI table
        for (Object b : bManager.busList) {
            // Assume BusClass has a method that returns a comma-separated string of its data
            String s = ((BusClass) b).displayBusInfo();
            String[] col = s.split(", ");
            
            // Populate the row using the split data
            busTable.addRow(new Object[] { col[0], col[1], col[2], col[3], col[4], col[5], col[6] });
        }

        // Add the table to the central area of the panel
        buspanel.add(pane, BorderLayout.CENTER);

        //  FORM LAYOUT SETUP (LEFT SIDE) 
        JPanel busWrapper = new JPanel();
        busWrapper.setLayout(new BoxLayout(busWrapper, BoxLayout.Y_AXIS));
        busWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Dimension boxSize = new Dimension(800, 40); // Standard sizing for input boxes

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        // Define individual input components
        JTextField makeBox = new JTextField(15);
        JTextField modelBox = new JTextField(15);

        // Dropdowns (JComboBox) for fixed choices
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "CityBus", "LongDistanceBus" });
        typeBox.setFont(inputFont);
        typeBox.setMaximumSize(boxSize);

        JComboBox<String> fuelTypeBox = new JComboBox<>(new String[] { "Gas", "Diesel" });
        fuelTypeBox.setFont(inputFont);
        fuelTypeBox.setMaximumSize(boxSize);

        JTextField cruiseSpeedBox = new JTextField(15);
        JTextField fuelBurnRateBox = new JTextField(15);
        JTextField fuelCapacityBox = new JTextField(15);

        //  ASSEMBLING THE FORM 
        // autoAdd is a helper method (defined below) to add Label + Component + Strut (spacing)
        autoAdd(inputPanel, new JLabel("Make:"), makeBox, labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Model:"), modelBox, labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Type:"), typeBox, labelFont, boxSize);

        // Manual addition for Fuel Type to handle the JComboBox specifically
        inputPanel.add(new JLabel("Fuel Type:") {
            { setFont(labelFont); }
        });
        inputPanel.add(fuelTypeBox);
        inputPanel.add(Box.createVerticalStrut(10)); // Vertical spacing

        autoAdd(inputPanel, new JLabel("Fuel Capacity:"), fuelCapacityBox, labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Fuel Burn Rate:"), fuelBurnRateBox, labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Cruise Speed:"), cruiseSpeedBox, labelFont, boxSize);

        //  BUTTONS 
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitBus = new JButton("Submit");
        JButton removeBus = new JButton("Remove");
        JButton newBus = new JButton("New Bus");

        submitBus.setFont(labelFont);
        removeBus.setFont(labelFont);
        newBus.setFont(labelFont);

        buttonPanel.add(submitBus);
        buttonPanel.add(newBus);
        buttonPanel.add(removeBus);

        // Combine input fields and buttons into the wrapper
        busWrapper.add(inputPanel);
        busWrapper.add(buttonPanel);

        // Place the entire form on the West (Left) side
        buspanel.add(busWrapper, BorderLayout.WEST);

        //  EVENT LISTENERS 

        // Listener: Sync Table Selection -> Form Fields
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    // Get the actual Bus object from the manager's list
                    BusClass selected = (BusClass) bManager.busList.get(selectedRow);
                    
                    // Update the text boxes with the object's data
                    makeBox.setText(selected.getMake());
                    modelBox.setText(selected.getModel());
                    typeBox.setSelectedItem(selected.getType());
                    cruiseSpeedBox.setText(String.valueOf(selected.getCruiseSpeed()));
                    fuelBurnRateBox.setText(String.valueOf(selected.getFuelBurnRate()));
                    fuelCapacityBox.setText(String.valueOf(selected.getFuelCapacity()));
                }
            }
        });

        // Listener: Handle "Submit" (Update existing bus)
        submitBus.addActionListener(e -> {
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a bus first.");
                return;
            }

            StringBuilder errorLog = new StringBuilder();
            boolean isValid = true;

            // Clean input values
            String makeVal = makeBox.getText().trim();
            String modelVal = modelBox.getText().trim();
            String typeVal = typeBox.getSelectedItem().toString();
            String fuelTypeVal = fuelTypeBox.getSelectedItem().toString();
            String speedTxt = cruiseSpeedBox.getText().trim();
            String burnTxt = fuelBurnRateBox.getText().trim();
            String capTxt = fuelCapacityBox.getText().trim();

            // VALIDATION: Check for duplicate Make/Model in the fleet
            for (int i = 0; i < bManager.busList.size(); i++) {
                if (i == selectedRow) continue; // Skip the bus we are currently editing

                BusClass existingBus = (BusClass) bManager.busList.get(i);
                if (existingBus.getMake().equalsIgnoreCase(makeVal) &&
                    existingBus.getModel().equalsIgnoreCase(modelVal)) {
                    errorLog.append("- A bus with the make '").append(makeVal)
                            .append("' and model '").append(modelVal).append("' already exists.\n");
                    isValid = false;
                    break;
                }
            }

            // VALIDATION: Alphanumeric checks (No special symbols in names)
            String alphaNumRegex = "^[a-zA-Z0-9 ]+$";
            if (!makeVal.matches(alphaNumRegex)) {
                errorLog.append("- 'Make' has invalid symbols or is empty.\n");
                isValid = false;
            }
            if (!modelVal.matches(alphaNumRegex)) {
                errorLog.append("- 'Model' has invalid symbols or is empty.\n");
                isValid = false;
            }

            // VALIDATION: Numeric checks (Must be valid numbers/decimals)
            String numericRegex = "^[0-9]*\\.?[0-9]+$";
            if (!speedTxt.matches(numericRegex)) {
                errorLog.append("- 'Cruise Speed' must be a pure number.\n");
                isValid = false;
            }
            if (!burnTxt.matches(numericRegex)) {
                errorLog.append("- 'Fuel Burn Rate' must be a pure number.\n");
                isValid = false;
            }
            if (!capTxt.matches(numericRegex)) {
                errorLog.append("- 'Fuel Capacity' must be a pure number.\n");
                isValid = false;
            }

            if (!isValid) {
                JOptionPane.showMessageDialog(frame, errorLog.toString(), "Input Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                // APPLY CHANGES: Update the Backend Object
                BusClass currentBus = (BusClass) bManager.busList.get(selectedRow);
                currentBus.setMake(makeVal);
                currentBus.setModel(modelVal);
                currentBus.setType(typeVal);
                currentBus.setFuelType(fuelTypeVal);
                currentBus.setCruiseSpeed(Double.parseDouble(speedTxt));
                currentBus.setFuelBurnRate(Double.parseDouble(burnTxt));
                currentBus.setFuelCapacity(Double.parseDouble(capTxt));

                // APPLY CHANGES: Update the UI Table view
                busTable.setValueAt(makeVal, selectedRow, 0);
                busTable.setValueAt(modelVal, selectedRow, 1);
                busTable.setValueAt(typeVal, selectedRow, 2);
                busTable.setValueAt(fuelTypeVal, selectedRow, 3);
                busTable.setValueAt(capTxt, selectedRow, 4);
                busTable.setValueAt(burnTxt, selectedRow, 5);
                busTable.setValueAt(speedTxt, selectedRow, 6);

                try {
                    bManager.save(); // Save changes to the external file
                    JOptionPane.showMessageDialog(frame, "Changes Saved!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving: " + ex.getMessage());
                }
            }
        });

        // Listener: Remove the selected bus
        removeBus.addActionListener(e -> {
            if (bManager.removeBus(selectedRow)) {
                busTable.removeRow(selectedRow);
                selectedRow = -1; // Reset selection
            }
        });

        // Listener: Create a new Bus entry
        newBus.addActionListener(e -> {
            String baseMake = "Make";
            String finalMake = baseMake;
            int counter = 1;

            // Ensure the new "placeholder" make name is unique (e.g. Make, Make1, Make2...)
            boolean duplicateFound = true;
            while (duplicateFound) {
                duplicateFound = false;
                for (Object b : bManager.busList) {
                    BusClass existingBus = (BusClass) b;
                    if (existingBus.getMake().equalsIgnoreCase(finalMake)) {
                        duplicateFound = true;
                        finalMake = baseMake + counter;
                        counter++;
                        break;
                    }
                }
            }

            // Create the new object with defaults
            BusClass nb = new BusClass();
            nb.setMake(finalMake);
            nb.setType("CityBus");
            nb.setCruiseSpeed(0.0);
            nb.setFuelBurnRate(0.0);
            nb.setFuelCapacity(0.0);

            // Add to manager list and UI table
            bManager.busList.add(nb);
            busTable.addRow(new Object[] { nb.getMake(), nb.getModel(), nb.getType(), "Gas", 0.0, 0.0, 0.0 });

            // Auto-select the new row and update the form fields
            selectedRow = busTable.getRowCount() - 1;
            table.setRowSelectionInterval(selectedRow, selectedRow);

            makeBox.setText(nb.getMake());
            modelBox.setText(nb.getModel());
            cruiseSpeedBox.setText("0.0");
            fuelBurnRateBox.setText("0.0");
            fuelCapacityBox.setText("0.0");
        });

        return buspanel;
    }

    /**
     * Helper method to standardize the addition of labeled input components.
     * * @param p The panel to add to.
     * @param l The label for the component.
     * @param c The component (JTextField, JComboBox, etc.).
     * @param f The font for the label.
     * @param d The maximum size for the component.
     */
    private void autoAdd(JPanel p, JLabel l, JComponent c, Font f, Dimension d) {
        l.setFont(f);
        c.setFont(new Font("SansSerif", Font.PLAIN, 18));
        c.setMaximumSize(d);
        p.add(l);
        p.add(c);
        p.add(Box.createVerticalStrut(10)); // Add consistent spacing below the input
    }
/*
 * Creates and returns a JPanel for managing bus stations.
 * This includes a table view of all stations and a side form for adding/editing/removing stations.
 */
    private JPanel manageBusStation() {
        // Main container using BorderLayout to separate the Table (Center) from the Form (West)
        JPanel stationpanel = new JPanel(new BorderLayout());

        //  FONT DEFINITIONS 
        // Standardizing fonts for a consistent look across the UI
        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        //  TABLE SETUP 
        // Define column headers for the station list
        String[] tablename = { "Name", "Latitude", "Longitude", "Refuel?" };

        // DefaultTableModel allows us to dynamically add/remove rows without recreating the JTable
        stationTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(stationTable);

        // Styling the table for readability
        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        JScrollPane pane = new JScrollPane(table); // Add scrolling capability to the table

        //  INITIAL DATA LOADING 
        // Populate the table with existing stations from the station manager
        for (Object st : sManager.stationList) {
            BusStationClass station = (BusStationClass) st;
            // Check if this station is a specific subclass to determine the "Refuel" status
            boolean isRefuel = station instanceof RefuelBusStation;
            stationTable.addRow(new Object[] {
                    station.getName(),
                    station.getLatitude(),
                    station.getLongitude(),
                    isRefuel ? "Yes" : "No"
            });
        }

        // Place the scrollable table in the center of the main panel
        stationpanel.add(pane, BorderLayout.CENTER);

        //  FORM UI SETUP (Left Side) 
        // stationWrapper stacks the input fields and buttons vertically
        JPanel stationWrapper = new JPanel();
        stationWrapper.setLayout(new BoxLayout(stationWrapper, BoxLayout.Y_AXIS));
        stationWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margin padding
        Dimension boxSize = new Dimension(800, 40); // Standardize input box size

        // Create labels and text fields for station data
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

        JCheckBox refuelCheckBox = new JCheckBox("Is Refuel Station?");
        refuelCheckBox.setFont(labelFont);

        //  ASSEMBLY OF INPUT PANEL 
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(sName);
        inputPanel.add(sNameBox);
        inputPanel.add(Box.createVerticalStrut(10)); // Add spacing between components
        inputPanel.add(latitude);
        inputPanel.add(latitudeBox);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(longitude);
        inputPanel.add(longitudeBox);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(refuelCheckBox);

        //  BUTTON CONTROLS 
        JButton submitStation = new JButton("Submit");
        JButton removeStation = new JButton("Remove");
        JButton newStation = new JButton("New Station");

        submitStation.setFont(labelFont);
        removeStation.setFont(labelFont);
        newStation.setFont(labelFont);

        // FlowLayout keeps buttons side-by-side
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitStation);
        buttonPanel.add(removeStation);
        buttonPanel.add(newStation);

        // Add inputs and buttons to the left-side wrapper
        stationWrapper.add(inputPanel);
        stationWrapper.add(buttonPanel);
        stationpanel.add(stationWrapper, BorderLayout.WEST);

        //  EVENT LISTENERS 

        // Listener: When a row in the table is clicked, fill the form with that station's data
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Ensure we only trigger once per click
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    BusStationClass s = sManager.stationList.get(selectedRow);
                    sNameBox.setText(s.getName());
                    latitudeBox.setText(String.valueOf(s.getLatitude()));
                    longitudeBox.setText(String.valueOf(s.getLongitude()));
                    refuelCheckBox.setSelected(s instanceof RefuelBusStation);
                } else {
                    // Clear form if nothing is selected
                    sNameBox.setText("");
                    latitudeBox.setText("");
                    longitudeBox.setText("");
                    refuelCheckBox.setSelected(false);
                }
            }
        });

        // Listener: Validate and Save changes to the selected station
        submitStation.addActionListener(e -> {
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a station first.");
                return;
            }

            StringBuilder errorLog = new StringBuilder();
            boolean isValid = true;

            // Collect and clean input
            String nameVal = sNameBox.getText().trim();
            String latTxt = latitudeBox.getText().trim();
            String lonTxt = longitudeBox.getText().trim();
            boolean isRefuel = refuelCheckBox.isSelected();

            // VALIDATION: Check for duplicate station names (excluding the current selection)
            for (int i = 0; i < sManager.stationList.size(); i++) {
                if (i == selectedRow) continue;
                if (sManager.stationList.get(i).getName().equalsIgnoreCase(nameVal)) {
                    errorLog.append("- Station name already exists.\n");
                    isValid = false;
                    break;
                }
            }

            // VALIDATION: Ensure name contains only alphanumeric characters and spaces
            String alphaNumRegex = "^[a-zA-Z0-9 ]+$";
            if (!nameVal.matches(alphaNumRegex)) {
                errorLog.append("- Name has invalid symbols or is empty.\n");
                isValid = false;
            }

            // VALIDATION: Ensure Latitude is a valid number
            try {
                Double.parseDouble(latTxt); 
            } catch (NumberFormatException e1) {
                errorLog.append("- Latitude is incorrect (must be a valid number).\n");
                isValid = false;
            }

            // VALIDATION: Ensure Longitude is a valid number
            try {
                Double.parseDouble(lonTxt);
            } catch (NumberFormatException e1) {
                errorLog.append("- Longitude is incorrect (must be a valid number).\n");
                isValid = false;
            }

            if (!isValid) {
                JOptionPane.showMessageDialog(frame, errorLog.toString(), "Input Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                // APPLY CHANGES
                double lat = Double.parseDouble(latTxt);
                double lon = Double.parseDouble(lonTxt);

                // Create appropriate object based on "Refuel" checkbox
                BusStationClass newStationObj = isRefuel ? 
                    new RefuelBusStation(nameVal, lat, lon) : 
                    new BusStationClass(nameVal, lat, lon);

                // Update Backend List and UI Table
                sManager.stationList.set(selectedRow, newStationObj);
                stationTable.setValueAt(nameVal, selectedRow, 0);
                stationTable.setValueAt(latTxt, selectedRow, 1);
                stationTable.setValueAt(lonTxt, selectedRow, 2);
                stationTable.setValueAt(isRefuel ? "Yes" : "No", selectedRow, 3);

                try {
                    sManager.save(); // Persist changes to disk
                    // Update the station inside the graph system as well
                    routeGraph.vertices.get(selectedRow).setStation(newStationObj);

                    frame.repaint(); // Force UI refresh
                    JOptionPane.showMessageDialog(frame, "Station Updated Successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving data", "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Listener: Delete the selected station
        removeStation.addActionListener(e -> {
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a station first.");
                return;
            }

            try {
                // Note: The logic here removes from the list twice (likely a bug in the source), 
                // but effectively it removes the station from the manager, graph, and UI.
                String stationToRemove = sManager.stationList.get(selectedRow).getName();
                routeGraph.removeNode(routeGraph.getNodeByName(stationToRemove));
                
                sManager.stationList.remove(selectedRow);
                stationTable.removeRow(selectedRow);
                sManager.save();

                // Reset form fields after deletion
                sNameBox.setText("");
                latitudeBox.setText("");
                longitudeBox.setText("");
                refuelCheckBox.setSelected(false);
                table.clearSelection();
                selectedRow = -1;
                
                frame.repaint();
                JOptionPane.showMessageDialog(frame, "Station Removed Successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error removing data", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Listener: Create a new station placeholder
        newStation.addActionListener(e -> {
            String baseName = "New Station";
            String finalName = baseName;
            int counter = 1;

            // Logic to ensure the "New Station" name is unique (e.g., New Station 1, New Station 2)
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

            // Add the new station to the manager and the UI table
            BusStationClass ns = new BusStationClass(finalName, 0.0, 0.0);
            sManager.stationList.add(ns);
            stationTable.addRow(new Object[] { finalName, "0.0", "0.0", "No" });
            
            // Auto-select the newly created row
            table.setRowSelectionInterval(stationTable.getRowCount() - 1, stationTable.getRowCount() - 1);

            try {
                sManager.save();
                routeGraph.addVertex(ns); // Add to the graph
                frame.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error saving data", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return stationpanel;
    }
}