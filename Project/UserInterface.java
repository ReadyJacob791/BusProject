package Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

// After the login it should initialize the Weighted Graph

public class UserInterface {
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public static void main(String[] args) {
        new UserInterface().initialize();
    }

    public void initialize() {
        JOptionPane.showMessageDialog(null,
                "System Warning: Unauthorized access is prohibited. Click OK to proceed.",
                "Security Alert",
                JOptionPane.WARNING_MESSAGE);

        // Set size for UI window
        frame = new JFrame("Route Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocation(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(logInPanel(), "LOGIN");
        cardPanel.add(dashboardPanel(), "DASHBOARD")
    }

    private JPanel logInPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
    }

    private JPanel
}