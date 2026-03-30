package Project;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import Project.Bus.*;
import Project.BusStation.*;
import Project.Route.*;

public class UserInterface {

    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel loginPanel;
    private JPanel routePanel;
    private JPanel buspanel;
    private JPanel stationpanel;

    private DefaultTableModel busTable;
    private DefaultTableModel stationTable;

    private BusManager bManager;
    private BusStationManager sManager;
    private WeightedGraph routeGraph;
    private RoutePlanner routePlanner;

    private int selectedRow = -1;

    public UserInterface() {
        try {
            bManager = new BusManager();
            bManager.listBuses();
            sManager = new BusStationManager();
            sManager.listStations();
            routeGraph = new WeightedGraph();
            routeGraph.buildGraphFromCSV(sManager, "Project/Route/WeigthedGraph.csv");
            routePlanner = new RoutePlanner(routeGraph);
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
        frame = new JFrame("Route Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(logInPanel(), "LOGIN");
        cardPanel.add(routePanel(), "ROUTEPLANNER");
        cardPanel.add(manageBus(), "MANAGEBUS");
        cardPanel.add(manageBusStation(), "MANAGESTATION");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Actions");
        JMenuItem routePlanner = new JMenuItem("Route Planner");
        JMenuItem manageBus = new JMenuItem("Manage Bus");
        JMenuItem manageStation = new JMenuItem("Manage Station");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exit = new JMenuItem("EXIT");

        exit.addActionListener(e -> {
            frame.dispose();
        });

        routePlanner.addActionListener(e -> {
            cardLayout.show(cardPanel, "ROUTEPLANNER");
            frame.revalidate();
            selectedRow = -1;
        });

        manageBus.addActionListener(e -> {
            cardLayout.show(cardPanel, "MANAGEBUS");
            frame.revalidate();
            selectedRow = -1;
        });

        manageStation.addActionListener(e -> {
            cardLayout.show(cardPanel, "MANAGESTATION");
            frame.revalidate();
            selectedRow = -1;
        });

        logoutItem.addActionListener(e -> {
            frame.setJMenuBar(null);
            cardLayout.show(cardPanel, "LOGIN");
            frame.revalidate();
        });

        menu.add(routePlanner);
        menu.add(manageBus);
        menu.add(manageStation);
        menu.add(logoutItem);
        menu.add(exit);
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
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(addAccountBtn);
        loginPanel.add(btnPanel, gbc);

        loginBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "ROUTEPLANNER");
            frame.setJMenuBar(createMenuBar());
            frame.revalidate();
        });

        addAccountBtn.addActionListener(e -> {
            showAddAccountDialog();
        });

        return loginPanel;
    }

    private void showAddAccountDialog() {
        JDialog dialog = new JDialog(frame, "Create New Account", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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

    private JPanel routePanel() {
        JPanel mainDash = new JPanel(new BorderLayout());

        JPanel leftCardPanel = new JPanel(new CardLayout());
        CardLayout leftLayout = (CardLayout) leftCardPanel.getLayout();

        leftCardPanel.setPreferredSize(new Dimension(450, 0));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Font largeFont = new Font("SansSerif", Font.PLAIN, 18);
        Font boldFont = new Font("SansSerif", Font.BOLD, 18);

        JLabel routeLabel = new JLabel("Build Route");
        routeLabel.setFont(boldFont);
        routeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel busLabel = new JLabel("Assigned Bus:");
        busLabel.setFont(boldFont);
        busLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<String> busDropdown = new JComboBox<>();
        busDropdown.setFont(largeFont);
        busDropdown.setMaximumSize(new Dimension(400, 40));
        for (Object bObj : bManager.busList) {
            BusClass b = (BusClass) bObj;
            busDropdown.addItem(b.getMake() + " " + b.getModel());
        }

        controlPanel.add(busLabel);
        controlPanel.add(busDropdown);
        controlPanel.add(Box.createVerticalStrut(15));

        JComboBox<String> addDropdown = new JComboBox<>();
        addDropdown.setFont(largeFont);
        for (BusStationClass s : sManager.stationList) {
            addDropdown.addItem(s.getName());
        }
        addDropdown.setMaximumSize(new Dimension(400, 40));

        JButton addBtn = new JButton("Add Station");
        addBtn.setFont(largeFont);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        DefaultListModel<String> routeStopsModel = new DefaultListModel<>();
        JList<String> routeStopsList = new JList<>(routeStopsModel);
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

        JLabel edgeLabel = new JLabel("Add Connection (Edge)");
        edgeLabel.setFont(boldFont);
        edgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<String> edgeFromDrop = new JComboBox<>();
        JComboBox<String> edgeToDrop = new JComboBox<>();
        edgeFromDrop.setFont(largeFont);
        edgeToDrop.setFont(largeFont);
        edgeFromDrop.setMaximumSize(new Dimension(400, 40));
        edgeToDrop.setMaximumSize(new Dimension(400, 40));

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

        removeEdgeBtn.addActionListener(e -> {
            String fromName = (String) edgeFromDrop.getSelectedItem();
            String toName = (String) edgeToDrop.getSelectedItem();

            if (fromName.equals(toName)) {
                JOptionPane.showMessageDialog(frame, "Select two different stations.");
                return;
            }

            Node n1 = routeGraph.getNodeByName(fromName);
            Node n2 = routeGraph.getNodeByName(toName);

            routeGraph.removeEdge(n1, n2);
            routeGraph.rewriteCSV("Project/Route/WeigthedGraph.csv");
            JOptionPane.showMessageDialog(frame, "Connection severed and saved.");
        });

        controlPanel.add(routeLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(addDropdown);
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

        leftCardPanel.add(controlPanel, "CONTROLS");
        leftCardPanel.add(resultsPanel, "RESULTS");

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.DARK_GRAY);
        JLabel graphPlaceholder = new JLabel("Graph Rendering Canvas (Future)", SwingConstants.CENTER);
        graphPlaceholder.setForeground(Color.WHITE);
        graphPlaceholder.setFont(new Font("SansSerif", Font.BOLD, 24));
        centerPanel.add(graphPlaceholder, BorderLayout.CENTER);

        mainDash.add(leftCardPanel, BorderLayout.WEST);
        mainDash.add(centerPanel, BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            String selected = (String) addDropdown.getSelectedItem();
            if (selected != null) {
                routeStopsModel.addElement(selected);
            }
        });

        removeBtn.addActionListener(e -> {
            int selectedIdx = routeStopsList.getSelectedIndex();
            if (selectedIdx != -1) {
                routeStopsModel.remove(selectedIdx);
            }
        });

        addEdgeBtn.addActionListener(e -> {
            String fromName = (String) edgeFromDrop.getSelectedItem();
            String toName = (String) edgeToDrop.getSelectedItem();

            if (fromName.equals(toName)) {
                JOptionPane.showMessageDialog(frame, "Cannot connect a station to itself.");
                return;
            }

            Node n1 = routeGraph.getNodeByName(fromName);
            Node n2 = routeGraph.getNodeByName(toName);

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
            routeGraph.appendEdgeToCSV(fromName, toName, "Project/Route/WeigthedGraph.csv");
            JOptionPane.showMessageDialog(frame,
                    "Edge created between " + fromName + " and " + toName + " and saved to file!");
        });

        backBtn.addActionListener(e -> leftLayout.show(leftCardPanel, "CONTROLS"));

        makeRouteBtn.addActionListener(e -> {
            if (routeStopsModel.size() < 2) {
                JOptionPane.showMessageDialog(frame, "Please add at least 2 stations to the list to create a route.");
                return;
            }

            java.util.List<Node> finalRoute = new ArrayList<>();
            double totalDistance = 0.0;

            for (int i = 0; i < routeStopsModel.size() - 1; i++) {
                Node currentStation = routeGraph.getNodeByName(routeStopsModel.get(i));
                Node nextStation = routeGraph.getNodeByName(routeStopsModel.get(i + 1));

                java.util.List<Node> legPath = routePlanner.getShortestPath(currentStation, nextStation);

                if (legPath.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Pathfinding failed!\n\nThe bus gets stuck at: " + currentStation.getStation().getName() +
                                    "\nIt cannot reach: " + nextStation.getStation().getName() +
                                    "\n\nPlease add a connecting edge to complete this route.",
                            "Route Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

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

                if (i > 0) {
                    legPath.remove(0);
                }
                finalRoute.addAll(legPath);
            }

            int selectedBusIdx = busDropdown.getSelectedIndex();
            BusClass selectedBus = (BusClass) bManager.busList.get(selectedBusIdx);

            double speed = selectedBus.getCruiseSpeed();
            double burnRate = selectedBus.getFuelBurnRate();
            double capacity = selectedBus.getFuelCapacity();

            double timeRequired = speed > 0 ? totalDistance / speed : 0;
            double fuelRequired = timeRequired * burnRate;

            boolean canComplete = (fuelRequired <= capacity) && (speed > 0);

            StringBuilder sb = new StringBuilder();
            sb.append("Total Distance: ").append(String.format("%.2f", totalDistance)).append(" units\n");
            sb.append("Bus Selected: ").append(selectedBus.getMake()).append(" ").append(selectedBus.getModel())
                    .append("\n");
            sb.append("Est. Fuel Consumption: ").append(String.format("%.2f", fuelRequired))
                    .append(" / ").append(String.format("%.2f", capacity)).append(" max capacity\n\n");

            if (canComplete) {
                sb.append("✅ STATUS: ROUTE APPROVED (Fuel Sufficient)\n");
            } else {
                if (speed <= 0) {
                    sb.append("❌ STATUS: ROUTE FAILED (Bus cruise speed is 0)\n");
                } else {
                    sb.append("❌ STATUS: ROUTE FAILED (Insufficient Fuel Capacity)\n");
                }
            }
            sb.append("--------------------------------------------------\n\n");

            for (int i = 0; i < finalRoute.size(); i++) {
                sb.append("Stop ").append(i + 1).append(": \n");
                sb.append("   ").append(finalRoute.get(i).getStation().getName()).append("\n\n");
            }

            resultsTextArea.setText(sb.toString());
            leftLayout.show(leftCardPanel, "RESULTS");
        });
        return mainDash;
    }

    private JPanel manageBus() {
        JPanel buspanel = new JPanel(new BorderLayout());

        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        String tablename[] = { "Make", "Model", "Type", "Fuel Capacity", "Fuel Burn Rate", "Cruise Speed" };
        DefaultTableModel busTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(busTable);

        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));

        JScrollPane pane = new JScrollPane(table);

        for (Object b : bManager.busList) {
            String s = ((BusClass) b).displayBusInfo();
            String[] col = s.split(", ");
            busTable.addRow(new Object[] { col[0], col[1], col[2], col[3], col[4], col[5] });
        }

        buspanel.add(pane, BorderLayout.CENTER);

        JPanel busWrapper = new JPanel();
        busWrapper.setLayout(new BoxLayout(busWrapper, BoxLayout.Y_AXIS));
        busWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Dimension boxSize = new Dimension(800, 40);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JLabel make = new JLabel("Make:");
        make.setFont(labelFont);
        JTextField makeBox = new JTextField(15);
        makeBox.setFont(inputFont);
        makeBox.setMaximumSize(boxSize);
        inputPanel.add(make);
        inputPanel.add(makeBox);
        inputPanel.add(Box.createVerticalStrut(10));

        JLabel model = new JLabel("Model:");
        model.setFont(labelFont);
        JTextField modelBox = new JTextField(15);
        modelBox.setFont(inputFont);
        modelBox.setMaximumSize(boxSize);
        inputPanel.add(model);
        inputPanel.add(modelBox);
        inputPanel.add(Box.createVerticalStrut(10));

        JLabel type = new JLabel("Type:");
        type.setFont(labelFont);
        JTextField typeBox = new JTextField(15);
        typeBox.setFont(inputFont);
        typeBox.setMaximumSize(boxSize);
        inputPanel.add(type);
        inputPanel.add(typeBox);
        inputPanel.add(Box.createVerticalStrut(10));

        JLabel cruiseSpeed = new JLabel("Cruise Speed:");
        cruiseSpeed.setFont(labelFont);
        JTextField cruiseSpeedBox = new JTextField(15);
        cruiseSpeedBox.setFont(inputFont);
        cruiseSpeedBox.setMaximumSize(boxSize);
        inputPanel.add(cruiseSpeed);
        inputPanel.add(cruiseSpeedBox);
        inputPanel.add(Box.createVerticalStrut(10));

        JLabel fuelBurnRate = new JLabel("Fuel Burn Rate:");
        fuelBurnRate.setFont(labelFont);
        JTextField fuelBurnRateBox = new JTextField(15);
        fuelBurnRateBox.setFont(inputFont);
        fuelBurnRateBox.setMaximumSize(boxSize);
        inputPanel.add(fuelBurnRate);
        inputPanel.add(fuelBurnRateBox);
        inputPanel.add(Box.createVerticalStrut(10));

        JLabel fuelCapacity = new JLabel("Fuel Capacity:");
        fuelCapacity.setFont(labelFont);
        JTextField fuelCapacityBox = new JTextField(15);
        fuelCapacityBox.setFont(inputFont);
        fuelCapacityBox.setMaximumSize(boxSize);
        inputPanel.add(fuelCapacity);
        inputPanel.add(fuelCapacityBox);

        inputPanel.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton submitBus = new JButton("Submit");
        JButton removeBus = new JButton("Remove");
        JButton newBus = new JButton("New Bus");

        submitBus.setFont(labelFont);
        removeBus.setFont(labelFont);
        newBus.setFont(labelFont);

        buttonPanel.add(submitBus);
        buttonPanel.add(newBus);
        buttonPanel.add(removeBus);

        busWrapper.add(inputPanel);
        busWrapper.add(buttonPanel);

        buspanel.add(busWrapper, BorderLayout.WEST);

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

            String makeVal = makeBox.getText().trim();
            String modelVal = modelBox.getText().trim();
            String typeVal = typeBox.getText().trim();
            String speedTxt = cruiseSpeedBox.getText().trim();
            String burnTxt = fuelBurnRateBox.getText().trim();
            String capTxt = fuelCapacityBox.getText().trim();

            for (int i = 0; i < bManager.busList.size(); i++) {
                if (i == selectedRow)
                    continue;

                BusClass existingBus = (BusClass) bManager.busList.get(i);
                if (existingBus.getMake().equalsIgnoreCase(makeVal) &&
                        existingBus.getModel().equalsIgnoreCase(modelVal)) {
                    errorLog.append("- A bus with the make '").append(makeVal)
                            .append("' and model '").append(modelVal).append("' already exists.\n");
                    isValid = false;
                    break;
                }
            }

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
                BusClass currentBus = (BusClass) bManager.busList.get(selectedRow);
                currentBus.setMake(makeVal);
                currentBus.setModel(modelVal);
                currentBus.setType(typeVal);
                currentBus.setCruiseSpeed(Double.parseDouble(speedTxt));
                currentBus.setFuelBurnRate(Double.parseDouble(burnTxt));
                currentBus.setFuelCapacity(Double.parseDouble(capTxt));

                busTable.setValueAt(makeVal, selectedRow, 0);
                busTable.setValueAt(modelVal, selectedRow, 1);
                busTable.setValueAt(typeVal, selectedRow, 2);
                busTable.setValueAt(capTxt, selectedRow, 3);
                busTable.setValueAt(burnTxt, selectedRow, 4);
                busTable.setValueAt(speedTxt, selectedRow, 5);

                try {
                    bManager.save();
                    JOptionPane.showMessageDialog(frame, "Changes Saved!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        removeBus.addActionListener(e -> {
            if (bManager.removeBus(selectedRow)) {
                busTable.removeRow(selectedRow);
                selectedRow = -1;
            }
        });

        newBus.addActionListener(e -> {
            String baseMake = "Make";

            String finalMake = baseMake;
            int counter = 1;

            boolean duplicateFound = true;
            while (duplicateFound) {
                duplicateFound = false;
                for (Object b : bManager.busList) {
                    BusClass existingBus = (BusClass) b;
                    if (existingBus.getMake().equalsIgnoreCase(finalMake)) {
                        duplicateFound = true;
                        finalMake = baseMake + " (" + counter + ")";
                        counter++;
                        break;
                    }
                }
            }

            BusClass nb = new BusClass();
            nb.setMake(finalMake);
            nb.setType("Standard");
            nb.setCruiseSpeed(0.0);
            nb.setFuelBurnRate(0.0);
            nb.setFuelCapacity(0.0);

            bManager.busList.add(nb);
            busTable.addRow(new Object[] {
                    nb.getMake(),
                    nb.getModel(),
                    nb.getType(),
                    nb.getFuelCapacity(),
                    nb.getFuelBurnRate(),
                    nb.getCruiseSpeed()
            });

            selectedRow = busTable.getRowCount() - 1;
            table.setRowSelectionInterval(selectedRow, selectedRow);

            makeBox.setText(nb.getMake());
            modelBox.setText(nb.getModel());
            typeBox.setText(nb.getType());
            cruiseSpeedBox.setText(String.valueOf(nb.getCruiseSpeed()));
            fuelBurnRateBox.setText(String.valueOf(nb.getFuelBurnRate()));
            fuelCapacityBox.setText(String.valueOf(nb.getFuelCapacity()));
        });

        return buspanel;
    }

    private JPanel manageBusStation() {
        JPanel stationpanel = new JPanel(new BorderLayout());

        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 18);
        Font tableFont = new Font("SansSerif", Font.PLAIN, 16);

        String tablename[] = { "Name", "Latitude", "Longitude" };

        stationTable = new DefaultTableModel(tablename, 0);
        JTable table = new JTable(stationTable);

        table.setFont(tableFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        JScrollPane pane = new JScrollPane(table);

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

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
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
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a station first.");
                return;
            }

            StringBuilder errorLog = new StringBuilder();
            boolean isValid = true;

            String nameVal = sNameBox.getText().trim();
            String latTxt = latitudeBox.getText().trim();
            String lonTxt = longitudeBox.getText().trim();

            for (int i = 0; i < sManager.stationList.size(); i++) {
                if (i == selectedRow)
                    continue;

                BusStationClass existingStation = sManager.stationList.get(i);
                if (existingStation.getName().equalsIgnoreCase(nameVal)) {
                    errorLog.append("- A station with the name '").append(nameVal).append("' already exists.\n");
                    isValid = false;
                    break;
                }
            }

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

            if (!isValid) {
                JOptionPane.showMessageDialog(frame, errorLog.toString(), "Input Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                BusStationClass currentStation = sManager.stationList.get(selectedRow);
                currentStation.setName(nameVal);
                currentStation.setLatitude(Double.parseDouble(latTxt));
                currentStation.setLongitude(Double.parseDouble(lonTxt));

                stationTable.setValueAt(nameVal, selectedRow, 0);
                stationTable.setValueAt(latTxt, selectedRow, 1);
                stationTable.setValueAt(lonTxt, selectedRow, 2);

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
                BusStationClass s = sManager.stationList.get(selectedRow);
                if (sManager.removeStation(selectedRow)) {
                    stationTable.removeRow(selectedRow);

                    if (routeGraph != null) {
                        Node nodeToRemove = routeGraph.getNodeByName(s.getName());
                        if (nodeToRemove != null) {
                            routeGraph.removeNode(nodeToRemove);
                            routeGraph.rewriteCSV("Project/Route/WeigthedGraph.csv");
                        }
                    }

                    selectedRow = -1;
                }
            }
        });

        newStation.addActionListener(e -> {
            String baseName = "New Station";
            String finalName = baseName;
            int counter = 1;

            boolean nameExists = true;
            while (nameExists) {
                nameExists = false;
                for (BusStationClass s : sManager.stationList) {
                    if (s.getName().equalsIgnoreCase(finalName)) {
                        nameExists = true;
                        finalName = baseName + " (" + counter + ")";
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