package Project;

/**
 * These are the imports required for the UI, data management,
 * and security features.
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
 * Main entry point and primary GUI class for the Bus Route Planner application.
 *
 * Architecture – CardLayout multi screen design:
 * A single JFrame hosts a CardLayout panel.  Each functional
 * area (Login, Route Planner, Manage Bus, Manage Station) is a JPanel
 * added to that card stack.  Switching screens is done by calling
 * cardLayout.show(cardPanel, "CARD_NAME").
 *
 * Security model:
 * Passwords are never stored in plain text.  On registration they are hashed
 * with SHA 256 before being written to Accounts.csv.  On login the
 * entered password is hashed and compared to the stored hash.
 */
public class UserInterface {

    // GUI Components

    /** The top level application window. */
    private JFrame frame;

    /** Container managed by #cardLayout that holds every screen. */
    private JPanel cardPanel;

    /** Swaps between the different application screens. */
    private CardLayout cardLayout;

    /** The login screen panel (declared as a field for potential future reference). */
    private JPanel loginPanel;

    // Table Models

    /** Table model for the bus management screen, drives the JTable display. */
    private DefaultTableModel busTable;

    /** Table model for the station management screen. */
    private DefaultTableModel stationTable;

    // Backend Logic & Data Managers

    /** Manages the in memory bus list and Bus.csv persistence. */
    private BusManager bManager;

    /** Manages the in memory station list and BusStation.csv persistence. */
    private BusStationManager sManager;

    /** The weighted graph of stations and roads used for pathfinding. */
    private WeightedGraph routeGraph;

    /** Dijkstra based pathfinder linked to #routeGraph. */
    private RoutePlanner routePlanner;

    // Shared Interactive Controls

    /**
     * Dropdown of available buses, populated from BusManager#busList.
     * Declared here so it can be refreshed from the menu bar action listener.
     */
    JComboBox<String> busDropdown = new JComboBox<>();

    /**
     * Dropdown of available stations, populated from
     * BusStationManager#stationList.
     * Declared here so it can be refreshed from the menu bar action listener.
     */
    JComboBox<String> stationDropDown = new JComboBox<>();

    /**
     * Zero based index of the row currently selected in whichever table is
     * active ( 1 means no selection).  Shared between the bus and
     * station management panels because only one can be active at a time.
     */
    private int selectedRow = -1;

    // Constructor

    /**
     * Initialises all backend managers and data structures.
     * Data is not yet loaded here — that happens in #initialize() so
     * the GUI is ready before any file I/O errors can surface.
     */
    public UserInterface() {
        try {
            bManager     = new BusManager();
            sManager     = new BusStationManager();
            routeGraph   = new WeightedGraph();
            routePlanner = new RoutePlanner(routeGraph);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Entry Point

    /**
     * Application entry point.
     * Shows a security disclaimer, then launches the Swing UI on the Event
     * Dispatch Thread.
     *
     * args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Display a one time security warning before the app opens
        JOptionPane.showMessageDialog(
                null,
                "System Warning: Unauthorized use is prohibited. Click OK to proceed.",
                "Security Alert",
                JOptionPane.WARNING_MESSAGE);

        // Launch the UI (Swing is not thread safe; use invokeLater in production)
        new UserInterface().initialize();
    }

    // Initialization

    /**
     * Loads CSV data, builds the route graph, constructs the JFrame, and
     * makes the application visible.
     *
     * Steps:
     * 
     *   Load buses and stations from their respective CSV files.
     *   Build the weighted graph using the stations and WeightedGraph.csv.
     *   Create the JFrame with a CardLayout and add all screen panels.
     *   Show the frame (starts on the LOGIN card).
     * 
     */
    public void initialize() {

        //  Load data from CSV files 
        bManager.listBuses();
        sManager.listStations();

        // Build the in memory graph: vertices from stations, edges from CSV
        routeGraph.buildGraphFromCSV(sManager, "Project/Route/WeightedGraph.csv");

        //  JFrame setup 
        frame = new JFrame("Route Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Open maximised/full screen
        frame.setLocationRelativeTo(null);             // Centre on screen (overridden by MAXIMIZED)

        //  CardLayout setup 
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        // Register each screen with a unique string key
        cardPanel.add(logInPanel(),        "LOGIN");
        cardPanel.add(routePanel(),        "ROUTEPLANNER");
        cardPanel.add(manageBus(),         "MANAGEBUS");
        cardPanel.add(manageBusStation(),  "MANAGESTATION");

        frame.add(cardPanel);
        frame.setVisible(true); // The LOGIN card is shown by default
    }

    // Menu Bar

    /**
     * Builds and returns a navigation menu bar tailored to the logged in
     * user's permission level.
     *
     * Regular users see: Route Planner, Manage Bus, Manage Station, Logout, Exit.
     * Admin users additionally see: See All Accounts (listed first, separated
     * from the navigation items by dividers).
     *
     * currentUser The Account whose role determines the menu contents.
     * return A fully configured JMenuBar.
     */
    private JMenuBar createMenuBar(Account currentUser) {

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        // Define the navigation items
        JMenuItem routePlanner  = new JMenuItem("Route Planner");
        JMenuItem manageBus     = new JMenuItem("Manage Bus");
        JMenuItem manageStation = new JMenuItem("Manage Station");
        JMenuItem logoutItem    = new JMenuItem("Logout");
        JMenuItem exit          = new JMenuItem("EXIT");

        // Apply a consistent font to all menu elements
        Font menuFont = new Font("Arial", Font.PLAIN, 18);
        menu.setFont(menuFont);
        routePlanner.setFont(menuFont);
        manageBus.setFont(menuFont);
        manageStation.setFont(menuFont);
        logoutItem.setFont(menuFont);
        exit.setFont(menuFont);

        //  Admin only item 
        // Only shown when the logged in account has the ADMIN role
        if (currentUser.isAdmin()) {
            JMenuItem seeAccounts = new JMenuItem("See All Accounts");
            seeAccounts.setFont(menuFont);

            // Open the account management dialog when clicked
            seeAccounts.addActionListener(e  -> showAllAccountsDialog());

            menu.addSeparator();
            menu.add(seeAccounts);
            menu.addSeparator();
        }

        //  Action Listeners 

        // Exit: dispose the JFrame, which triggers EXIT_ON_CLOSE
        exit.addActionListener(e  -> frame.dispose());

        // Route Planner: switch screen and refresh all dropdowns from live data
        routePlanner.addActionListener(e  -> {
            cardLayout.show(cardPanel, "ROUTEPLANNER");

            // Repopulate the bus dropdown from the latest manager state
            busDropdown.removeAllItems();
            for (BusClass b : bManager.busList) {
                busDropdown.addItem(b.getMake() + " " + b.getModel());
            }

            // Repopulate the station dropdown from the latest manager state
            stationDropDown.removeAllItems();
            for (BusStationClass s : sManager.stationList) {
                stationDropDown.addItem(s.getName());
            }

            frame.revalidate();
            selectedRow =  -1; // Clear selection so stale data can't be modified
        });

        // Manage Bus: simple screen swap
        manageBus.addActionListener(e  -> {
            cardLayout.show(cardPanel, "MANAGEBUS");
            frame.revalidate();
            selectedRow =  -1;
        });

        // Manage Station: simple screen swap
        manageStation.addActionListener(e  -> {
            cardLayout.show(cardPanel, "MANAGESTATION");
            frame.revalidate();
            selectedRow =  -1;
        });

        // Logout: remove the menu bar (hides navigation) and return to login
        logoutItem.addActionListener(e  -> {
            frame.setJMenuBar(null); // Hide the menu so unauthenticated users can't navigate
            cardLayout.show(cardPanel, "LOGIN");
            frame.revalidate();
        });

        // Assemble the menu in display order
        menu.add(routePlanner);
        menu.add(manageBus);
        menu.add(manageStation);
        menu.add(logoutItem);
        menu.add(exit);
        menuBar.add(menu);

        return menuBar;
    }

    // Login Panel

    /**
     * Constructs and returns the Login screen panel.
     *
     * Layout: GridBagLayout centres a username field, a password field
     * (masked), a Login button, and an Add Account button on screen.
     *
     * Login flow:
     * 
     *   Validate that username ≥ 3 characters and password ≥ 5 characters.
     *   Look up the stored SHA 256 hash for the username in Accounts.csv.
     *   Hash the entered password and compare to the stored hash.
     *   On success: load the user's role, build the menu bar, switch screens.
     * 
     *
     * return The configured login JPanel.
     */
    private JPanel logInPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField    userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15); // Characters shown as dots
        JButton loginBtn      = new JButton("Login");
        JButton addAccountBtn = new JButton("Add Account");

        // Row 0: username
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(userField, gbc);

        // Row 1: password
        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passField, gbc);

        // Row 2: buttons (span two columns)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(addAccountBtn);
        loginPanel.add(btnPanel, gbc);

        //  Login button action 
        loginBtn.addActionListener(e  -> {
            String username = userField.getText().trim();
            // Convert char[] to String (standard JPasswordField practice)
            String password = new String(passField.getPassword());

            // Client side length validation
            if (username.length() < 3) {
                JOptionPane.showMessageDialog(frame,
                        "Username must be at least 3 characters long!",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.length() < 5) {
                JOptionPane.showMessageDialog(frame,
                        "Password must be at least 5 characters long!",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Look up the stored hash for this username
            String storedHash = getStoredPasswordHash(username);
            if (storedHash == null) {
                JOptionPane.showMessageDialog(frame,
                        "User not found, please create an account first!",
                        "Account Required", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Hash the entered password and compare
            String inputHash = hashPassword(password);
            if (inputHash.equals(storedHash)) {
                //  Successful login 
                String roleCSV = getRoleForUser(username);
                Account loggedInAccount = new Account(username, inputHash, roleCSV);

                // Install a role aware menu bar
                frame.setJMenuBar(createMenuBar(loggedInAccount));
                cardLayout.show(cardPanel, "ROUTEPLANNER");
                frame.revalidate();

                // Clear fields so credentials aren't visible if the user returns
                userField.setText("");
                passField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Incorrect password! Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Open the account creation dialog
        addAccountBtn.addActionListener(e  -> showAddAccountDialog());

        return loginPanel;
    }

    // Account Helpers

    /**
     * Reads Accounts.csv to find the role string for a given username.
     * Returns "USER" as a safe default if the role column is missing or
     * the username is not found.
     *
     * CSV format expected: username, passwordHash, role
     *
     * username The username to look up (case insensitive).
     * return The role string, or "USER" if not found.
     */
    private String getRoleForUser(String username) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader("Project\\Accounts.csv"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                // parts[0] = username, parts[1] = hash, parts[2] = role
                if (parts.length >= 3 && parts[0].equalsIgnoreCase(username)) {
                    return parts[2].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "USER"; // Safe default if role column is absent or user not found
    }

    /**
     * Admin tool: opens a modal dialog listing every account in the CSV with
     * a button to remove a selected account.
     */
    private void showAllAccountsDialog() {
        JDialog managementDialog = new JDialog(frame, "Account Management", true);
        managementDialog.setLayout(new BorderLayout(10, 10));

        // Use a mutable ListModel so removal refreshes the UI automatically
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> accountList = new JList<>(listModel);

        refreshAccountList(listModel);

        JButton removeBtn = new JButton("Remove Selected Account");
        removeBtn.addActionListener(e  -> {
            String selected = accountList.getSelectedValue();
            if (selected != null) {
                // The list shows "username   (ROLE)"; extract just the username
                String userToRemove = selected.split("   ")[0];
                performAccountRemoval(userToRemove);
                refreshAccountList(listModel); // Refresh the list after the file update
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
     * Removes a specific account from Accounts.csv by copying every
     * line except the target user's to a temporary file, then swapping the
     * temporary file in place of the original.
     *
     * targetUser The username to delete (case insensitive).
     */
    private void performAccountRemoval(String targetUser) {
        File originalFile = new File("Project\\Accounts.csv");
        File tempFile     = new File("Project\\Accounts_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                // Write every account EXCEPT the one being deleted
                if (parts.length > 0 && !parts[0].equalsIgnoreCase(targetUser)) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Atomic file swap: delete original, rename temp to original
        if (originalFile.delete()) {
            tempFile.renameTo(originalFile);
        } else {
            JOptionPane.showMessageDialog(frame, "Error updating the database file.");
        }
    }

    /**
     * Clears and repopulates a DefaultListModel with all accounts from
     * the CSV, formatted as "username   (ROLE)".
     *
     * model The list model to refresh.
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
                    model.addElement(parts[0].trim() + "   (" + parts[2].trim() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error refreshing list: " + e.getMessage());
        }
    }

    /**
     * Opens a modal dialog that lets any visitor (including non logged in users)
     * register a new account.
     *
     * Validation checks:
     * 
     *   Username and password must not be empty.
     *   Password and confirm password must match.
     *   Username must be at least 3 characters.
     *   Password must be at least 5 characters.
     *   Username must not already exist in the CSV.
     * 
     * On success the password is hashed before being written to the CSV.
     */
    private void showAddAccountDialog() {
        JDialog dialog = new JDialog(frame, "Create New Account", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Role selector — USER or ADMIN
        String[] roles = {"USER", "ADMIN"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);

        JTextField    newUsernameField  = new JTextField(15);
        JPasswordField newPasswordField  = new JPasswordField(15);
        JPasswordField verifyPasswordField = new JPasswordField(15);
        JButton submitBtn = new JButton("Submit");

        // Layout rows
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

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Account Type"), gbc);
        gbc.gridx = 1;
        dialog.add(roleComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(submitBtn, gbc);

        //  Submit action 
        submitBtn.addActionListener(e  -> {
            String username = newUsernameField.getText().trim();
            String password = new String(newPasswordField.getPassword());
            String verify   = new String(verifyPasswordField.getPassword());
            String role     = (String) roleComboBox.getSelectedItem();

            // Sequential validation with descriptive error messages
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot be empty!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!password.equals(verify)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (username.length() < 3) {
                JOptionPane.showMessageDialog(dialog, "Username too short!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (password.length() < 5) {
                JOptionPane.showMessageDialog(dialog, "Password too short!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (getStoredPasswordHash(username) != null) {
                JOptionPane.showMessageDialog(dialog, "Username already exists!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // All checks passed — hash password and append to CSV
                try (BufferedWriter writer = new BufferedWriter(
                        new FileWriter("Project\\Accounts.csv", true))) {
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

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    // Security / Hashing

    /**
     * Produces a SHA 256 hex string from a plain text password.
     *
     * SHA 256 is a one way hash function: given the hash it is computationally
     * infeasible to recover the original password.  The result is used both
     * when storing a new account and when verifying a login attempt.
     *
     * password The plain text password to hash.
     * return A 64 character lowercase hex string.
     * throws RuntimeException If SHA 256 is unavailable (should never occur
     *                          on a standard JVM).
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convert each byte to a two character hex representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0'); // Pad single digit hex
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm SHA 256 not found", e);
        }
    }

    /**
     * Searches Accounts.csv for the password hash associated with the
     * given username.
     *
     * CSV format: username, passwordHash, role
     *
     * username The username to look up (case insensitive, whitespace trimmed).
     * return The stored SHA 256 hash string, or null if the username
     *         is not found or the file does not exist.
     */
    private String getStoredPasswordHash(String username) {
        File file = new File("Project\\Accounts.csv");
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                // parts[0] = username, parts[1] = hash
                if (parts.length >= 2 && parts[0].equalsIgnoreCase(username.trim())) {
                    return parts[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Username not found
    }

    // Route Planner Panel

    /**
     * Constructs and returns the Route Planner screen.
     *
     * Layout:
     * 
     *   LEFT  – a nested CardLayout that flips between the route builder
     *               controls and the results itinerary.
     *   CENTER – a GraphPanel that draws the station graph.
     * 
     *
     * Features:
     * 
     *   Add/remove stations to build a multi stop itinerary.
     *   Calculate the shortest Dijkstra path across all legs.
     *   Display heading, distance, time, fuel use, and approval status.
     *   Add or remove edges (road connections) between stations.
     * 
     *
     * return The configured route planner JPanel.
     */
    private JPanel routePanel() {
        JPanel routePan = new JPanel(new BorderLayout());

        // Left panel uses its own nested CardLayout to swap between
        // "build route" controls and the results page
        JPanel    leftCardPanel = new JPanel(new CardLayout());
        CardLayout leftLayout   = (CardLayout) leftCardPanel.getLayout();
        leftCardPanel.setPreferredSize(new Dimension(450, 0));

        //   Controls sub panel  
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Font largeFont = new Font("SansSerif", Font.PLAIN, 18);
        Font boldFont  = new Font("SansSerif", Font.BOLD, 18);

        // Section header
        JLabel routeLabel = new JLabel("Build Route");
        routeLabel.setFont(boldFont);
        routeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel busLabel = new JLabel("Assigned Bus:");
        busLabel.setFont(boldFont);
        busLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Bus selection dropdown — populated from the live busList
        busDropdown.setFont(largeFont);
        busDropdown.setMaximumSize(new Dimension(400, 40));
        for (BusClass b : bManager.busList) {
            busDropdown.addItem(b.getMake() + " " + b.getModel());
        }

        controlPanel.add(busLabel);
        controlPanel.add(busDropdown);
        controlPanel.add(Box.createVerticalStrut(15));

        // Station selection dropdown — populated from the live stationList
        stationDropDown.setFont(largeFont);
        for (BusStationClass s : sManager.stationList) {
            stationDropDown.addItem(s.getName());
        }
        stationDropDown.setMaximumSize(new Dimension(400, 40));

        JButton addBtn = new JButton("Add Station");
        addBtn.setFont(largeFont);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ordered list of stops the user has chosen for their route
        DefaultListModel<String> routeStopsModel = new DefaultListModel<>();
        JList<String>  routeStopsList = new JList<>(routeStopsModel);
        routeStopsList.setFont(largeFont);
        JScrollPane listScroller = new JScrollPane(routeStopsList);
        listScroller.setPreferredSize(new Dimension(400, 300));

        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.setFont(largeFont);
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton makeRouteBtn = new JButton("Calculate Route");
        makeRouteBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        makeRouteBtn.setBackground(new Color(200, 230, 255));
        makeRouteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        //   Edge management controls  
        JLabel edgeLabel = new JLabel("Add Connection (Edge)");
        edgeLabel.setFont(boldFont);
        edgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<String> edgeFromDrop = new JComboBox<>();
        JComboBox<String> edgeToDrop   = new JComboBox<>();
        edgeFromDrop.setFont(largeFont);
        edgeToDrop.setFont(largeFont);
        edgeFromDrop.setMaximumSize(new Dimension(400, 40));
        edgeToDrop.setMaximumSize(new Dimension(400, 40));

        // Populate both edge dropdowns from the station list
        for (BusStationClass s : sManager.stationList) {
            edgeFromDrop.addItem(s.getName());
            edgeToDrop.addItem(s.getName());
        }

        JButton addEdgeBtn    = new JButton("Add Connect");
        JButton removeEdgeBtn = new JButton("Remove Connection");
        addEdgeBtn.setFont(largeFont);
        removeEdgeBtn.setFont(largeFont);
        addEdgeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeEdgeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble all controls into the controlPanel
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
        controlPanel.add(new JSeparator());
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

        //   Results sub panel  
        JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel resultsHeader = new JLabel("Route Itinerary", SwingConstants.CENTER);
        resultsHeader.setFont(new Font("SansSerif", Font.BOLD, 24));

        JTextArea resultsTextArea = new JTextArea();
        resultsTextArea.setFont(new Font("SansSerif", Font.PLAIN, 20));
        resultsTextArea.setEditable(false);
        resultsTextArea.setLineWrap(true);
        resultsTextArea.setWrapStyleWord(true);
        JScrollPane resultsScroller = new JScrollPane(resultsTextArea);

        JButton backBtn = new JButton("Back to Route Builder");
        backBtn.setFont(boldFont);
        backBtn.setBackground(new Color(255, 200, 200));

        resultsPanel.add(resultsHeader, BorderLayout.NORTH);
        resultsPanel.add(resultsScroller, BorderLayout.CENTER);
        resultsPanel.add(backBtn, BorderLayout.SOUTH);

        // Register both sub panels with the left card layout
        leftCardPanel.add(controlPanel, "CONTROLS");
        leftCardPanel.add(resultsPanel, "RESULTS");

        // The graph canvas goes in the centre of the main route panel
        GraphPanel centerPanel = new GraphPanel();
        routePan.add(leftCardPanel, BorderLayout.WEST);
        routePan.add(centerPanel, BorderLayout.CENTER);

        //   Action Listeners  

        // Add the selected station name to the stop list
        addBtn.addActionListener(e  -> {
            String selected = (String) stationDropDown.getSelectedItem();
            if (selected != null) routeStopsModel.addElement(selected);
        });

        // Remove the highlighted stop from the list
        removeBtn.addActionListener(e  -> {
            int idx = routeStopsList.getSelectedIndex();
            if (idx !=  -1) routeStopsModel.remove(idx);
        });

        // Remove an edge (road connection) between two stations
        removeEdgeBtn.addActionListener(e  -> {
            String fromName = (String) edgeFromDrop.getSelectedItem();
            String toName   = (String) edgeToDrop.getSelectedItem();

            if (fromName == null || toName == null || fromName.equals(toName)) {
                JOptionPane.showMessageDialog(frame, "Select two different valid stations.");
                return;
            }

            Node n1 = routeGraph.getNodeByName(fromName);
            Node n2 = routeGraph.getNodeByName(toName);

            // Guard against a station that was deleted without refreshing dropdowns
            if (n1 == null || n2 == null) {
                JOptionPane.showMessageDialog(frame,
                        "Error: One of these stations no longer exists.\n"
                        + "Please close and reopen the Route Builder to refresh the lists.",
                        "Station Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            routeGraph.removeEdge(n1, n2);

            //Corrected typo "WeigthedGraph.csv" -> "WeightedGraph.csv"
            routeGraph.rewriteCSV("Project/Route/WeightedGraph.csv");
            JOptionPane.showMessageDialog(frame, "Connection severed and saved.");
            centerPanel.repaint();
        });

        // Add an edge (road connection) between two stations
        addEdgeBtn.addActionListener(e  -> {
            String fromName = (String) edgeFromDrop.getSelectedItem();
            String toName   = (String) edgeToDrop.getSelectedItem();

            if (fromName == null || toName == null || fromName.equals(toName)) {
                JOptionPane.showMessageDialog(frame, "Cannot connect a station to itself.");
                return;
            }

            Node n1 = routeGraph.getNodeByName(fromName);
            Node n2 = routeGraph.getNodeByName(toName);

            if (n1 == null || n2 == null) {
                JOptionPane.showMessageDialog(frame,
                        "Error: One of these stations no longer exists.\n"
                        + "Please close and reopen the Route Builder to refresh the lists.",
                        "Station Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Prevent adding a duplicate connection
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
            routeGraph.appendEdgeToCSV(fromName, toName, "Project/Route/WeightedGraph.csv");
            JOptionPane.showMessageDialog(frame,
                    "Road connected between " + fromName + " and " + toName + " and saved!");
            centerPanel.repaint();
        });

        // Return from the results screen to the route builder
        backBtn.addActionListener(e  -> leftLayout.show(leftCardPanel, "CONTROLS"));

        // Calculate and display the full route itinerary
        makeRouteBtn.addActionListener(e  -> {
            if (routeStopsModel.size() < 2) {
                JOptionPane.showMessageDialog(frame,
                        "Please add at least 2 stations to the list to create a route.");
                return;
            }

            java.util.List<Node> finalRoute = new ArrayList<>();
            double totalDistance = 0.0;

            // Process each leg: from stop i to stop i+1
            for (int i = 0; i < routeStopsModel.size()   -1; i++) {
                Node currentStation = routeGraph.getNodeByName(routeStopsModel.get(i));
                Node nextStation    = routeGraph.getNodeByName(routeStopsModel.get(i + 1));

                java.util.List<Node> legPath =
                        routePlanner.getShortestPath(currentStation, nextStation);

                // If no path exists between these two stops, abort and report
                if (legPath.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Pathfinding failed!\n\n"
                            + "The bus gets stuck at: " + currentStation.getStation().getName()
                            + "\nIt cannot reach: " + nextStation.getStation().getName()
                            + "\n\nPlease add a connecting edge to complete this route.",
                            "Route Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Sum the edge weights along this leg
                for (int j = 0; j < legPath.size()   -1; j++) {
                    Node a = legPath.get(j);
                    Node b = legPath.get(j + 1);
                    for (Edge edge : a.getEdges()) {
                        if (edge.getTo().equals(b)) {
                            totalDistance += edge.getWeight();
                            break;
                        }
                    }
                }

                // Remove the first node of legs after the first to avoid duplicating
                // intermediate stations where legs join
                if (i > 0) legPath.remove(0);
                finalRoute.addAll(legPath);
            }

            // Overall heading: from the very first stop to the very last
            Node firstStation = routeGraph.getNodeByName(routeStopsModel.firstElement());
            Node lastStation  = routeGraph.getNodeByName(routeStopsModel.lastElement());

            String overallHeading = routePlanner.calculateHeading(
                    firstStation.getStation().getLatitude(),
                    firstStation.getStation().getLongitude(),
                    lastStation.getStation().getLatitude(),
                    lastStation.getStation().getLongitude());

            // Retrieve the selected bus's performance specs
            int selectedBusIdx = busDropdown.getSelectedIndex();
            BusClass selectedBus = bManager.busList.get(selectedBusIdx);

            double speed    = selectedBus.getCruiseSpeed();   // mph
            double burnRate = selectedBus.getFuelBurnRate();  // gallons/hour
            double capacity = selectedBus.getFuelCapacity();  // gallons

            // Derived values
            double timeRequired = speed > 0 ? totalDistance / speed : 0; // hours
            double fuelRequired = timeRequired * burnRate;                 // gallons

            // Route is approved only if the bus can physically complete it
            boolean canComplete = (fuelRequired <= capacity) && (speed > 0);

            // Build the itinerary string
            StringBuilder sb = new StringBuilder();
            sb.append("Heading: ").append(overallHeading).append("\n");
            sb.append("Total Distance: ").append(String.format("%.2f", totalDistance)).append(" miles\n");
            sb.append("Bus Selected: ")
              .append(selectedBus.getMake()).append(" ").append(selectedBus.getModel()).append("\n");
            sb.append("Est. Trip Time: ").append(String.format("%.2f", timeRequired)).append(" hours\n");
            sb.append("Est. Fuel Required: ").append(String.format("%.2f", fuelRequired)).append(" gallons\n");
            sb.append("Fuel Capacity: ").append(String.format("%.2f", capacity)).append(" gallons\n\n");

            if (canComplete) {
                sb.append("ROUTE APPROVED\n");
            } else if (speed <= 0) {
                sb.append("ROUTE FAILED (Bus cruise speed is 0)\n");
            } else {
                sb.append("ROUTE FAILED (Insufficient Fuel Capacity)\n");
            }

            sb.append("  \n\n");

            // List every stop in order
            for (int i = 0; i < finalRoute.size(); i++) {
                sb.append("Stop ").append(i + 1).append(": \n");
                sb.append("   ").append(finalRoute.get(i).getStation().getName()).append("\n\n");
            }

            resultsTextArea.setText(sb.toString());
            leftLayout.show(leftCardPanel, "RESULTS");
        });

        return routePan;
    }

    // Graph Visualisation Inner Class

    /**
     * A custom JPanel that draws the current state of the route graph.
     *
     * Rendering steps:
     * 
     *   Compute the bounding box (min/max lat and lon) of all stations.
     *   Map each station's geographic coordinates to pixel coordinates
     *       within the panel, applying padding on all sides.
     *   Draw edges as grey lines with the distance weight labelled at the
     *       midpoint.
     *   Draw each station as a red dot (or ⛽ emoji for refuel stations)
     *       with its name and coordinates.
     * 
     */
    private class GraphPanel extends JPanel {

        /** Sets the background to dark grey for good contrast. */
        public GraphPanel() {
            setBackground(Color.DARK_GRAY);
        }

        /**
         * Called by Swing whenever this panel needs to be redrawn.
         * Renders edges first (so nodes appear on top), then nodes.
         *
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            // Enable anti aliasing for smoother lines and text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);

            if (routeGraph.vertices == null || routeGraph.vertices.isEmpty()) return;

            // Find the geographic bounding box so we can scale to fit the panel
            double minLat = Double.MAX_VALUE, maxLat =  -Double.MAX_VALUE;
            double minLon = Double.MAX_VALUE, maxLon =  -Double.MAX_VALUE;

            for (Node n : routeGraph.vertices) {
                double lat = n.getStation().getLatitude();
                double lon = n.getStation().getLongitude();
                if (lat < minLat) minLat = lat;
                if (lat > maxLat) maxLat = lat;
                if (lon < minLon) minLon = lon;
                if (lon > maxLon) maxLon = lon;
            }

            // Padding keeps station labels from being clipped at the edges
            int paddingX   = 150;
            int paddingY   = 60;
            int usableWidth  = getWidth()  -  (2 * paddingX);
            int usableHeight = getHeight()  - (2 * paddingY);

            g2d.setStroke(new BasicStroke(2));

            //  Draw nodes 
            int nodeSize = 16;
            for (Node n : routeGraph.vertices) {
                int x = mapLonToX(n.getStation().getLongitude(), minLon, maxLon, usableWidth)  + paddingX;
                int y = mapLatToY(n.getStation().getLatitude(),  minLat, maxLat, usableHeight) + paddingY;

                if (n.getStation() instanceof RefuelBusStation) {
                    // Refuel stations use a fuel pump emoji instead of a coloured dot
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
                    g2d.drawString("⛽", x - 12, y + 7);
                } else {
                    // Regular stations are drawn as filled red circles
                    g2d.setColor(new Color(200, 50, 50));
                    g2d.fillOval(x - (nodeSize / 2), y - (nodeSize / 2), nodeSize, nodeSize);
                }

                // Station name
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                g2d.drawString(n.getStation().getName(), x + 15, y + 4);

                // Coordinates shown in smaller text beneath the name
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2d.drawString(
                        String.format("Lat: %.3f, Lon: %.3f",
                                n.getStation().getLatitude(),
                                n.getStation().getLongitude()),
                        x + 15, y + 20);
            }
            //  Draw edges 
            for (Node n : routeGraph.vertices) {
                int x1 = mapLonToX(n.getStation().getLongitude(), minLon, maxLon, usableWidth)  + paddingX;
                int y1 = mapLatToY(n.getStation().getLatitude(),  minLat, maxLat, usableHeight) + paddingY;

                for (Edge e : n.getEdges()) {
                    Node target = e.getTo();
                    int x2 = mapLonToX(target.getStation().getLongitude(), minLon, maxLon, usableWidth)  + paddingX;
                    int y2 = mapLatToY(target.getStation().getLatitude(),  minLat, maxLat, usableHeight) + paddingY;

                    // Draw the line
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawLine(x1, y1, x2, y2);

                    // Label the weight at the midpoint of the edge
                    int midX = (x1 + x2) / 2;
                    int midY = (y1 + y2) / 2;
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                    g2d.drawString(String.format("%.1f mi", e.getWeight()), midX, midY - 5);
                }
            }


        }

        /**
         * Maps a longitude value to a horizontal pixel coordinate within the
         * usable drawing area.
         *
         * lon    The longitude to map.
         * minLon The minimum longitude in the dataset (left edge).
         * maxLon The maximum longitude in the dataset (right edge).
         * width  The pixel width of the drawing area.
         * return The x pixel coordinate.
         */
        private int mapLonToX(double lon, double minLon, double maxLon, int width) {
            if (maxLon == minLon) return width / 2; // Single station: centre it
            return (int) (((lon - minLon) / (maxLon - minLon)) * width);
        }

        /**
         * Maps a latitude value to a vertical pixel coordinate within the
         * usable drawing area.  Y is inverted because screen coordinates
         * increase downward while latitude increases upward.
         *
         * lat    The latitude to map.
         * minLat The minimum latitude in the dataset (bottom of map).
         * maxLat The maximum latitude in the dataset (top of map).
         * height The pixel height of the drawing area.
         * return The y pixel coordinate.
         */
        private int mapLatToY(double lat, double minLat, double maxLat, int height) {
            if (maxLat == minLat) return height / 2;
            return height -  (int)(((lat - minLat) / (maxLat - minLat)) * height);
        }
    }

    // Manage Bus Panel

    /**
     * Constructs and returns the Manage Bus screen.
     *
     * Layout:
     * 
     *   CENTER – a JTable listing all buses with their seven attributes.
     *   LEFT   – input fields pre populated when a row is selected, plus
     *                Submit, New Bus, and Remove buttons.
     * 
     *
     * return The configured bus management JPanel.
     */
    private JPanel manageBus() {
        JPanel buspanel = new JPanel(new BorderLayout());

        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        // Seven column table matching displayBusInfo() order
        String[] tablename = {"Make", "Model", "Type", "Fuel Type",
                              "Fuel Capacity", "Fuel Burn Rate", "Cruise Speed"};
        DefaultTableModel busTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(busTable);
        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        JScrollPane pane = new JScrollPane(table);

        // Populate the table from the live busList
        for (BusClass b : bManager.busList) {
            String[] col = b.displayBusInfo().split(", ");
            busTable.addRow(new Object[]{col[0], col[1], col[2], col[3], col[4], col[5], col[6]});
        }

        buspanel.add(pane, BorderLayout.CENTER);

        //   Input panel (left side)  
        JPanel busWrapper = new JPanel();
        busWrapper.setLayout(new BoxLayout(busWrapper, BoxLayout.Y_AXIS));
        busWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Dimension boxSize = new Dimension(800, 40);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JTextField makeBox         = new JTextField(15);
        JTextField modelBox        = new JTextField(15);
        JTextField cruiseSpeedBox  = new JTextField(15);
        JTextField fuelBurnRateBox = new JTextField(15);
        JTextField fuelCapacityBox = new JTextField(15);

        JComboBox<String> typeBox     = new JComboBox<>(new String[]{"CityBus", "LongDistanceBus"});
        JComboBox<String> fuelTypeBox = new JComboBox<>(new String[]{"Gas", "Diesel"});
        typeBox.setFont(inputFont);
        typeBox.setMaximumSize(boxSize);
        fuelTypeBox.setFont(inputFont);
        fuelTypeBox.setMaximumSize(boxSize);

        autoAdd(inputPanel, new JLabel("Make:"),         makeBox,         labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Model:"),        modelBox,        labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Type:"),         typeBox,         labelFont, boxSize);

        // Fuel Type is a combo box so it needs manual layout (not autoAdd)
        inputPanel.add(new JLabel("Fuel Type:") {{ setFont(labelFont); }});
        inputPanel.add(fuelTypeBox);
        inputPanel.add(Box.createVerticalStrut(10));

        autoAdd(inputPanel, new JLabel("Fuel Capacity:"),  fuelCapacityBox,  labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Fuel Burn Rate:"), fuelBurnRateBox,  labelFont, boxSize);
        autoAdd(inputPanel, new JLabel("Cruise Speed:"),   cruiseSpeedBox,   labelFont, boxSize);

        //   Buttons  
        JButton submitBus = new JButton("Submit");
        JButton removeBus = new JButton("Remove");
        JButton newBus    = new JButton("New Bus");
        submitBus.setFont(labelFont);
        removeBus.setFont(labelFont);
        newBus.setFont(labelFont);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitBus);
        buttonPanel.add(newBus);
        buttonPanel.add(removeBus);

        busWrapper.add(inputPanel);
        busWrapper.add(buttonPanel);
        buspanel.add(busWrapper, BorderLayout.WEST);

        //   Row selection: populate input fields from selected bus  
        table.getSelectionModel().addListSelectionListener(e  -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = table.getSelectedRow();
                if (selectedRow !=  -1) {
                    BusClass selected = bManager.busList.get(selectedRow);
                    makeBox.setText(selected.getMake());
                    modelBox.setText(selected.getModel());
                    typeBox.setSelectedItem(selected.getType());
                    fuelTypeBox.setSelectedItem(selected.getFuelType());
                    cruiseSpeedBox.setText(String.valueOf(selected.getCruiseSpeed()));
                    fuelBurnRateBox.setText(String.valueOf(selected.getFuelBurnRate()));
                    fuelCapacityBox.setText(String.valueOf(selected.getFuelCapacity()));
                }
            }
        });

        //   Submit: validate and save changes to the selected bus  
        submitBus.addActionListener(e  -> {
            if (selectedRow ==  -1) {
                JOptionPane.showMessageDialog(frame, "Please select a bus first.");
                return;
            }

            StringBuilder errorLog = new StringBuilder();
            boolean isValid = true;

            String makeVal     = makeBox.getText().trim();
            String modelVal    = modelBox.getText().trim();
            String typeVal     = typeBox.getSelectedItem().toString();
            String fuelTypeVal = fuelTypeBox.getSelectedItem().toString();
            String speedTxt    = cruiseSpeedBox.getText().trim();
            String burnTxt     = fuelBurnRateBox.getText().trim();
            String capTxt      = fuelCapacityBox.getText().trim();

            // Check for duplicate make+model (ignore the currently selected row)
            for (int i = 0; i < bManager.busList.size(); i++) {
                if (i == selectedRow) continue;
                BusClass existing = bManager.busList.get(i);
                if (existing.getMake().equalsIgnoreCase(makeVal) &&
                    existing.getModel().equalsIgnoreCase(modelVal)) {
                    errorLog.append("  A bus with make '").append(makeVal)
                            .append("' and model '").append(modelVal).append("' already exists.\n");
                    isValid = false;
                    break;
                }
            }

            // Make, model, and type must contain only letters, digits, and spaces
            String alphaNumRegex = "^[a zA Z0 9 ]+$";
            if (!makeVal.matches(alphaNumRegex)) {
                errorLog.append("  'Make' has invalid symbols or is empty.\n");
                isValid = false;
            }
            if (!modelVal.matches(alphaNumRegex)) {
                errorLog.append("  'Model' has invalid symbols or is empty.\n");
                isValid = false;
            }

            // Numeric fields must be non negative numbers
            String numericRegex = "^[0 9]*\\.?[0 9]+$";
            if (!speedTxt.matches(numericRegex)) {
                errorLog.append("  'Cruise Speed' must be a pure number.\n");
                isValid = false;
            }
            if (!burnTxt.matches(numericRegex)) {
                errorLog.append("  'Fuel Burn Rate' must be a pure number.\n");
                isValid = false;
            }
            if (!capTxt.matches(numericRegex)) {
                errorLog.append("  'Fuel Capacity' must be a pure number.\n");
                isValid = false;
            }

            if (!isValid) {
                JOptionPane.showMessageDialog(frame, errorLog.toString(),
                        "Input Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                // Apply changes to the in memory object
                BusClass currentBus = bManager.busList.get(selectedRow);
                currentBus.setMake(makeVal);
                currentBus.setModel(modelVal);
                currentBus.setType(typeVal);
                currentBus.setFuelType(fuelTypeVal);
                currentBus.setCruiseSpeed(Double.parseDouble(speedTxt));
                currentBus.setFuelBurnRate(Double.parseDouble(burnTxt));
                currentBus.setFuelCapacity(Double.parseDouble(capTxt));

                // Reflect changes in the table display
                busTable.setValueAt(makeVal,     selectedRow, 0);
                busTable.setValueAt(modelVal,    selectedRow, 1);
                busTable.setValueAt(typeVal,     selectedRow, 2);
                busTable.setValueAt(fuelTypeVal, selectedRow, 3);
                busTable.setValueAt(capTxt,      selectedRow, 4);
                busTable.setValueAt(burnTxt,     selectedRow, 5);
                busTable.setValueAt(speedTxt,    selectedRow, 6);

                try {
                    bManager.save();
                    JOptionPane.showMessageDialog(frame, "Changes Saved!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        //   Remove: delete the selected bus  
        removeBus.addActionListener(e  -> {
            if (bManager.removeBus(selectedRow)) {
                busTable.removeRow(selectedRow);
                selectedRow =  1;
            }
        });

        //   New Bus: add a placeholder bus, auto resolving duplicate makes  
        newBus.addActionListener(e  -> {
            // Generate a unique make name by appending a counter if necessary
            String baseMake  = "Make";
            String finalMake = baseMake;
            int    counter   = 1;

            boolean duplicateFound = true;
            while (duplicateFound) {
                duplicateFound = false;
                for (BusClass b : bManager.busList) {
                    if (b.getMake().equalsIgnoreCase(finalMake)) {
                        duplicateFound = true;
                        finalMake = baseMake + counter;
                        counter++;
                        break;
                    }
                }
            }

            // Create a blank bus with default values
            BusClass nb = new BusClass();
            nb.setMake(finalMake);
            nb.setType("CityBus");
            nb.setCruiseSpeed(0.0);
            nb.setFuelBurnRate(0.0);
            nb.setFuelCapacity(0.0);

            bManager.busList.add(nb);

            // The original code only added 6 items and was missing
            // Fuel Type entirely.  Now all 7 columns are included in the correct order:
            // Make, Model, Type, Fuel Type, Fuel Capacity, Fuel Burn Rate, Cruise Speed
            busTable.addRow(new Object[]{
                    nb.getMake(),
                    nb.getModel(),
                    nb.getType(),
                    nb.getFuelType(),       
                    nb.getFuelCapacity(),
                    nb.getFuelBurnRate(),
                    nb.getCruiseSpeed()
            });

            // Auto select the new row and populate the input fields
            selectedRow = busTable.getRowCount()  -1;
            table.setRowSelectionInterval(selectedRow, selectedRow);

            makeBox.setText(nb.getMake());
            modelBox.setText(nb.getModel());
            typeBox.setSelectedItem(nb.getType());
            fuelTypeBox.setSelectedItem(nb.getFuelType());
            cruiseSpeedBox.setText(String.valueOf(nb.getCruiseSpeed()));
            fuelBurnRateBox.setText(String.valueOf(nb.getFuelBurnRate()));
            fuelCapacityBox.setText(String.valueOf(nb.getFuelCapacity()));
        });

        return buspanel;
    }

    // Layout Helper

    /**
     * Convenience method that applies a font to a label, sets the maximum size
     * of a component, and adds both (plus a vertical spacer) to a panel in
     * one call.
     *
     * p The panel to add to.
     * l The label to display above the component.
     * c The input component (text field, combo box, etc.).
     * f The font to apply to the label.
     * d The maximum size to assign to the component.
     */
    private void autoAdd(JPanel p, JLabel l, JComponent c, Font f, Dimension d) {
        l.setFont(f);
        c.setFont(new Font("SansSerif", Font.PLAIN, 18));
        c.setMaximumSize(d);
        p.add(l);
        p.add(c);
        p.add(Box.createVerticalStrut(10));
    }

    // Manage Bus Station Panel

    /**
     * Constructs and returns the Manage Bus Station screen.
     *
     * Layout:
     * 
     *   CENTER – a JTable listing all stations with name, lat, lon, refuel.
     *   LEFT   – input fields pre populated on row selection, plus Submit,
     *                New Station, and Remove buttons.
     * 
     *
     * return The configured station management JPanel}.
     */
    private JPanel manageBusStation() {
        JPanel stationpanel = new JPanel(new BorderLayout());

        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        String[] tablename = {"Name", "Latitude", "Longitude", "Refuel?"};
        stationTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(stationTable);
        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        JScrollPane pane = new JScrollPane(table);

        // Populate the table from the live stationList
        for (BusStationClass station : sManager.stationList) {
            boolean isRefuel = station instanceof RefuelBusStation;
            stationTable.addRow(new Object[]{
                    station.getName(),
                    station.getLatitude(),
                    station.getLongitude(),
                    isRefuel ? "Yes" : "No"
            });
        }

        stationpanel.add(pane, BorderLayout.CENTER);

        //   Input panel (left side)  
        JPanel stationWrapper = new JPanel();
        stationWrapper.setLayout(new BoxLayout(stationWrapper, BoxLayout.Y_AXIS));
        stationWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Dimension boxSize = new Dimension(800, 40);

        JLabel     sName       = new JLabel("Station Name:");
        JTextField sNameBox    = new JTextField(15);
        JLabel     latitude    = new JLabel("Latitude:");
        JTextField latitudeBox = new JTextField(15);
        JLabel     longitude   = new JLabel("Longitude:");
        JTextField longitudeBox = new JTextField(15);
        JCheckBox  refuelCheckBox = new JCheckBox("Is Refuel Station?");

        sName.setFont(labelFont);      sNameBox.setFont(inputFont);    sNameBox.setMaximumSize(boxSize);
        latitude.setFont(labelFont);   latitudeBox.setFont(inputFont);  latitudeBox.setMaximumSize(boxSize);
        longitude.setFont(labelFont);  longitudeBox.setFont(inputFont); longitudeBox.setMaximumSize(boxSize);
        refuelCheckBox.setFont(labelFont);

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
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(refuelCheckBox);

        JButton submitStation = new JButton("Submit");
        JButton removeStation = new JButton("Remove");
        JButton newStation    = new JButton("New Station");
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

        //   Row selection: populate input fields from selected station  
        table.getSelectionModel().addListSelectionListener(e  -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = table.getSelectedRow();
                if (selectedRow !=  -1) {
                    BusStationClass s = sManager.stationList.get(selectedRow);
                    sNameBox.setText(s.getName());
                    latitudeBox.setText(String.valueOf(s.getLatitude()));
                    longitudeBox.setText(String.valueOf(s.getLongitude()));
                    refuelCheckBox.setSelected(s instanceof RefuelBusStation);
                } else {
                    sNameBox.setText("");
                    latitudeBox.setText("");
                    longitudeBox.setText("");
                    refuelCheckBox.setSelected(false);
                }
            }
        });

        //   Submit: validate and save changes to the selected station  
        submitStation.addActionListener(e  -> {
            if (selectedRow ==  -1) {
                JOptionPane.showMessageDialog(frame, "Please select a station first.");
                return;
            }

            StringBuilder errorLog = new StringBuilder();
            boolean isValid = true;

            String nameVal = sNameBox.getText().trim();
            String latTxt  = latitudeBox.getText().trim();
            String lonTxt  = longitudeBox.getText().trim();
            boolean isRefuel = refuelCheckBox.isSelected();

            // Duplicate name check (ignore the currently selected row)
            for (int i = 0; i < sManager.stationList.size(); i++) {
                if (i == selectedRow) continue;
                if (sManager.stationList.get(i).getName().equalsIgnoreCase(nameVal)) {
                    errorLog.append("  Station name already exists.\n");
                    isValid = false;
                    break;
                }
            }

            // Station name must contain only letters, digits, spaces, and dots
            if (!nameVal.matches("^[a zA Z0 9 .]+$")) {
                errorLog.append("  Name has invalid symbols or is empty.\n");
                isValid = false;
            }

            // Latitude must parse as a valid decimal number
            try {
                Double.parseDouble(latTxt);
            } catch (NumberFormatException ex) {
                errorLog.append("  Latitude is incorrect (must be a valid number).\n");
                isValid = false;
            }

            // Longitude must parse as a valid decimal number
            try {
                Double.parseDouble(lonTxt);
            } catch (NumberFormatException ex) {
                errorLog.append("  Longitude is incorrect (must be a valid number).\n");
                isValid = false;
            }

            if (!isValid) {
                JOptionPane.showMessageDialog(frame, errorLog.toString(),
                        "Input Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                double lat = Double.parseDouble(latTxt);
                double lon = Double.parseDouble(lonTxt);

                // Instantiate the correct subclass based on the refuel checkbox
                BusStationClass newStationObj = isRefuel
                        ? new RefuelBusStation(nameVal, lat, lon)
                        : new BusStationClass(nameVal, lat, lon);

                sManager.stationList.set(selectedRow, newStationObj);
                stationTable.setValueAt(nameVal, selectedRow, 0);
                stationTable.setValueAt(latTxt, selectedRow, 1);
                stationTable.setValueAt(lonTxt, selectedRow, 2);
                stationTable.setValueAt(isRefuel ? "Yes" : "No", selectedRow, 3);

                try {
                    sManager.save();
                    // Sync the in memory graph node so the canvas redraws correctly
                    routeGraph.vertices.get(selectedRow).setStation(newStationObj);
                    frame.repaint();
                    JOptionPane.showMessageDialog(frame, "Station Updated Successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving data",
                            "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeStation.addActionListener(e  -> {
            if (selectedRow ==  -1) {
                JOptionPane.showMessageDialog(frame, "Please select a station first.");
                return;
            }

            try {
                // Capture the node reference BEFORE modifying any list
                String stationName = sManager.stationList.get(selectedRow).getName();
                Node nodeToRemove  = routeGraph.getNodeByName(stationName);

                // Remove from the graph first (so its edges are properly cleaned up)
                routeGraph.removeNode(nodeToRemove);

                // Now remove from the station manager list (exactly once)
                sManager.stationList.remove(selectedRow);
                sManager.save();
                frame.repaint();
                JOptionPane.showMessageDialog(frame, "Station Removed Successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error saving data",
                        "File Error", JOptionPane.ERROR_MESSAGE);
                return; // Don't continue if persistence failed
            }

            // Update the table display and reset the selection state
            stationTable.removeRow(selectedRow);
            sNameBox.setText("");
            latitudeBox.setText("");
            longitudeBox.setText("");
            refuelCheckBox.setSelected(false);
            table.clearSelection();
            selectedRow =  1;
        });

        //   New Station: add a placeholder with a unique name  
        newStation.addActionListener(e  -> {
            // Auto generate a unique name by appending a counter when necessary
            String baseName  = "New Station";
            String finalName = baseName;
            int    counter   = 1;

            boolean nameExists = true;
            while (nameExists) {
                nameExists = false;
                for (BusStationClass s : sManager.stationList) {
                    if (s.getName().equalsIgnoreCase(finalName)) {
                        nameExists = true;
                        finalName = baseName + counter;
                        counter++;
                        break;
                    }
                }
            }

            // Create the placeholder station at 0,0
            BusStationClass ns = new BusStationClass(finalName, 0.0, 0.0);
            sManager.stationList.add(ns);
            stationTable.addRow(new Object[]{finalName, "0.0", "0.0", "No"});

            // Auto select the new row
            int newRowIdx = stationTable.getRowCount() - 1;
            table.setRowSelectionInterval(newRowIdx, newRowIdx);

            try {
                sManager.save();
                routeGraph.addVertex(ns); // Add a corresponding node to the graph
                frame.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error saving data",
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return stationpanel;
    }
}
