package solvexis.ui;

import solvexis.service.*;
import solvexis.service.transport.*;
import solvexis.model.Station;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetroUIApp extends JFrame {

    private final MetroRoutePlanner planner;
    private final LastMileService lastMileService;
    private final PlatformCrowdDensityService crowdService;
    private final CarbonFootprintService carbonService;
    private UserStatsService statsService;
    private String currentUser;

    private JComboBox<String> startBox;
    private JComboBox<String> endBox;
    private JTable routeTable;
    private JTable lastMileTable;
    private JTable statsTable;
    private JTable crowdTable;

    private JTextField lastMileAddressField;
    private JTextField lastMileDistanceField;

    private List<Station> currentRouteStations = new ArrayList<>();
    private List<LastMileService.TransportOption> currentLastMileOptions = new ArrayList<>();
    private double currentMetroFare = 0.0;
    
    private JTextArea detailsArea;
    private JTextArea historyArea;
    private JTextArea carbonArea;

    private JMenuBar menuBar;
    private JMenu accountMenu;
    private JMenuItem logoutItem;

    public MetroUIApp() {
        this.planner = new MetroRoutePlanner();
        this.lastMileService = new LastMileService();
        this.crowdService = new PlatformCrowdDensityService();
        this.carbonService = new CarbonFootprintService();
        this.statsService = null;

        setTitle("SOLVEXIS Metro Planner - Modern UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        initMenuBar();
        initWelcomeUI();
    }

    private void initMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(new Color(26, 26, 26));
        
        accountMenu = new JMenu("Account");
        accountMenu.setForeground(Color.WHITE);
        logoutItem = new JMenuItem("Logout");
        
        logoutItem.addActionListener(e -> handleLogout());
        logoutItem.setEnabled(false);
        
        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);
        setJMenuBar(menuBar);
    }

    private void initWelcomeUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(12, 35, 64));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("SOLVEXIS");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(new Color(0xFFCC33));

        JLabel subtitleLabel = new JLabel("Smart Metro Planner - Routes, Last Mile & Eco Stats");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);

        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(30));

        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false);
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createLineBorder(new Color(30, 136, 229), 2));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField userField = new JTextField(18);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JPasswordField passField = new JPasswordField(18);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton startButton = new JButton("Start Application");
        startButton.setBackground(new Color(0x1E88E5));
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(startButton, gbc);

        centerPanel.add(loginPanel);
        centerPanel.add(Box.createVerticalStrut(40));

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        JLabel footerLabel = new JLabel("© SOLVEXIS - Academic Demo Build");
        footerLabel.setForeground(new Color(180, 180, 180));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        rootPanel.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(rootPanel);

        startButton.addActionListener(e -> handleLogin(userField, passField));
        passField.addActionListener(e -> startButton.doClick());
    }

    private void handleLogin(JTextField userField, JPasswordField passField) {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (!validateLogin(username, password)) {
            return;
        }
        
        currentUser = username;
        statsService = FileHandler.loadStats(currentUser);
        logoutItem.setEnabled(true);
        initMainUI();
    }

    private boolean validateLogin(String username, String password) {
        if (username.isEmpty()) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Validation Error", "Please enter a username.");
            return false;
        }
        
        if (!username.matches("[A-Za-z][A-Za-z0-9_]{2,15}")) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Validation Error", 
                "Username must start with a letter and be 3-16 characters.");
            return false;
        }
        
        if (password.length() < 4) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Validation Error", 
                "Password must be at least 4 characters.");
            return false;
        }
        
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Validation Error", 
                "Password must contain letters and numbers.");
            return false;
        }
        
        showAlert(JOptionPane.INFORMATION_MESSAGE, "Welcome", "Welcome " + username + "! 🚇");
        return true;
    }

    private void showAlert(int type, String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    private void initMainUI() {
        getContentPane().removeAll();
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(240, 248, 255));
        tabs.setForeground(new Color(15, 32, 60));
        tabs.addTab("Metro Ticket", buildMetroTicketPanel());
        tabs.addTab("Last Mile", buildLastMilePanel());
        tabs.addTab("Crowd Indicator", buildCrowdPanel());
        tabs.addTab("Carbon Indicator", buildCarbonPanel());
        tabs.addTab("Stations", buildStationsPanel());
        tabs.addTab("Trip History", buildHistoryPanel());
        tabs.addTab("Stats", buildStatsPanel());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void handleLogout() {
        currentUser = null;
        statsService = null;
        currentRouteStations.clear();
        currentLastMileOptions.clear();
        currentMetroFare = 0.0;
        logoutItem.setEnabled(false);
        getContentPane().removeAll();
        revalidate();
        repaint();
        initWelcomeUI();
    }

    private JPanel buildMetroTicketPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input area with border
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(30, 136, 229), 2),
            "Route Selection", 0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(30, 136, 229)));

        List<String> stations = new ArrayList<>(planner.getStationNames());
        Collections.sort(stations);
        String[] stationArray = stations.toArray(new String[0]);

        startBox = new JComboBox<>(stationArray);
        endBox = new JComboBox<>(stationArray);
        
        startBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        endBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        inputPanel.add(createLabel("Start Station:"));
        inputPanel.add(startBox);
        inputPanel.add(createLabel("End Station:"));
        inputPanel.add(endBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton findRouteButton = new JButton("🔍 Find Route");
        findRouteButton.setBackground(new Color(0x43A047));
        findRouteButton.setForeground(Color.WHITE);
        findRouteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        findRouteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(findRouteButton);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(inputPanel, BorderLayout.CENTER);
        topSection.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topSection, BorderLayout.NORTH);

        // Route table
        routeTable = createStyledTable();
        JScrollPane routeScrollPane = new JScrollPane(routeTable);
        routeScrollPane.setBorder(BorderFactory.createTitledBorder("Route Details"));

        // Details area
        detailsArea = new JTextArea(8, 50);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        detailsArea.setBackground(new Color(245, 245, 245));
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Trip Information"));

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, routeScrollPane, detailsScrollPane);
        centerSplit.setDividerLocation(200);
        panel.add(centerSplit, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton startRideButton = new JButton("▶ Start Ride");
        startRideButton.setBackground(new Color(0x1E88E5));
        startRideButton.setForeground(Color.WHITE);
        startRideButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startRideButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JButton saveMetroButton = new JButton("💾 Save Metro Trip");
        saveMetroButton.setBackground(new Color(0xFB8C00));
        saveMetroButton.setForeground(Color.WHITE);
        saveMetroButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveMetroButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        bottomPanel.add(startRideButton);
        bottomPanel.add(saveMetroButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        findRouteButton.addActionListener(e -> handleFindRoute());
        startRideButton.addActionListener(e -> startRideSimulation());
        saveMetroButton.addActionListener(e -> handleSaveMetroOnlyTrip());

        return panel;
    }

    private JTable createStyledTable() {
        JTable table = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setGridColor(new Color(200, 200, 200));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.getTableHeader().setBackground(new Color(30, 136, 229));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        return table;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return label;
    }

    private void handleFindRoute() {
        String startStation = (String) startBox.getSelectedItem();
        String endStation = (String) endBox.getSelectedItem();
        
        if (startStation == null || endStation == null) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Selection Error", "Please select both start and end stations.");
            return;
        }
        
        try {
            List<String> routePath = planner.findRoute(startStation, endStation);
            currentMetroFare = planner.calculateFareBetween(startStation, endStation);
            int estimatedTime = planner.estimateTimeMinutes(routePath);
            currentRouteStations = planner.getStationsOnRoute(startStation, endStation);

            // Populate table
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Station");
            model.addColumn("Metro Line");
            model.addColumn("Type");

            for (Station station : currentRouteStations) {
                String line = getLineNameForCorridor(station.getCorridorNumber());
                String type = station.getClass().getSimpleName();
                model.addRow(new Object[]{station.getName(), line, type});
            }
            routeTable.setModel(model);

            // Update details
            double distance = carbonService.calculateDistance(currentRouteStations);
            double co2Saved = carbonService.calculateCO2Saved(distance);
            
            StringBuilder details = new StringBuilder();
            details.append("═══════════════════ JOURNEY DETAILS ═══════════════════\n\n");
            details.append("Total Stations:  ").append(currentRouteStations.size()).append("\n");
            details.append("Total Distance:  ").append(String.format("%.1f km", distance)).append("\n");
            details.append("Estimated Time:  ").append(estimatedTime).append(" minutes\n");
            details.append("Metro Fare:      ₹").append(String.format("%.0f", currentMetroFare)).append("\n");
            details.append("CO₂ Saved:       ").append(carbonService.formatCO2Saved(co2Saved)).append("\n\n");
            details.append("Route: ").append(String.join(" → ", routePath));

            detailsArea.setText(details.toString());

        } catch (Exception ex) {
            showAlert(JOptionPane.ERROR_MESSAGE, "Route Error", "Error finding route: " + ex.getMessage());
        }
    }

    private void startRideSimulation() {
        if (currentRouteStations == null || currentRouteStations.isEmpty()) {
            showAlert(JOptionPane.WARNING_MESSAGE, "No Route", "Please find a route first.");
            return;
        }
        List<Station> route = new ArrayList<>(currentRouteStations);
        detailsArea.append("\n\n══════════════════ JOURNEY STARTED ══════════════════\n");
        final int[] index = {0};
        int totalStations = route.size();
        int delay = Math.max(400, 5000 / totalStations);

        javax.swing.Timer timer = new javax.swing.Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index[0] >= route.size()) {
                    detailsArea.append("══════════════════ JOURNEY COMPLETED ══════════════════\n");
                    timer.stop();
                    return;
                }
                Station st = route.get(index[0]);
                String msg;
                if (index[0] == 0) {
                    msg = "✓ Departing from: " + st.getName();
                } else if (index[0] == route.size() - 1) {
                    msg = "✓ Arrived at destination: " + st.getName();
                } else {
                    msg = "→ Next station: " + st.getName() + " (" + getLineNameForCorridor(st.getCorridorNumber()) + ")";
                }
                detailsArea.append("[" + (index[0] + 1) + "] " + msg + "\n");
                detailsArea.setCaretPosition(detailsArea.getDocument().getLength());
                index[0]++;
            }
        });
        timer.start();
    }
    
    private String getLineNameForCorridor(int corridorNumber) {
        return switch(corridorNumber) {
            case 1 -> "Red Line";
            case 2 -> "Blue Line";
            case 3 -> "Green Line";
            default -> "Unknown";
        };
    }

    private void handleSaveMetroOnlyTrip() {
        if (currentRouteStations == null || currentRouteStations.isEmpty()) {
            showAlert(JOptionPane.WARNING_MESSAGE, "No Trip", "No route to save. Find a route first.");
            return;
        }
        
        try {
            double metroDistance = carbonService.calculateDistance(currentRouteStations);
            double co2Saved = carbonService.calculateCO2Saved(metroDistance);
            
            FileHandler.recordTrip(currentUser, currentRouteStations, 0.0, co2Saved);
            FileHandler.saveTrip(currentUser, currentRouteStations, null, null, currentMetroFare);
            
            showAlert(JOptionPane.INFORMATION_MESSAGE, "Success", "✓ Metro trip saved successfully!");
        } catch (Exception ex) {
            showAlert(JOptionPane.ERROR_MESSAGE, "Save Error", "Error saving metro trip: " + ex.getMessage());
        }
    }

    private JPanel buildLastMilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input section
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 137, 123), 2),
            "Last Mile Settings", 0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(0, 137, 123)));

        lastMileAddressField = new JTextField();
        lastMileDistanceField = new JTextField();

        inputPanel.add(createLabel("Destination Address:"));
        inputPanel.add(lastMileAddressField);
        inputPanel.add(createLabel("Distance (km):"));
        inputPanel.add(lastMileDistanceField);

        JPanel buttonPanelTop = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loadOptionsButton = new JButton("🔄 Show Options");
        loadOptionsButton.setBackground(new Color(0x00897B));
        loadOptionsButton.setForeground(Color.WHITE);
        loadOptionsButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loadOptionsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanelTop.add(loadOptionsButton);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(inputPanel, BorderLayout.CENTER);
        topSection.add(buttonPanelTop, BorderLayout.SOUTH);

        panel.add(topSection, BorderLayout.NORTH);

        // Last mile table
        lastMileTable = createStyledTable();
        JScrollPane scrollPane = new JScrollPane(lastMileTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Transport Modes"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton startLastMileButton = new JButton("▶ Start Last Mile Ride");
        startLastMileButton.setBackground(new Color(0x1E88E5));
        startLastMileButton.setForeground(Color.WHITE);
        startLastMileButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startLastMileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JButton saveTripButton = new JButton("💾 Save Full Trip");
        saveTripButton.setBackground(new Color(0xFB8C00));
        saveTripButton.setForeground(Color.WHITE);
        saveTripButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveTripButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        bottomPanel.add(startLastMileButton);
        bottomPanel.add(saveTripButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        loadOptionsButton.addActionListener(e -> handleLoadLastMileOptions());
        startLastMileButton.addActionListener(e -> startLastMileRide());
        saveTripButton.addActionListener(e -> handleSaveTrip());

        return panel;
    }

    private void handleLoadLastMileOptions() {
        if (currentRouteStations == null || currentRouteStations.isEmpty()) {
            showAlert(JOptionPane.WARNING_MESSAGE, "No Route", "Please find a route first.");
            return;
        }
        
        String destination = lastMileAddressField.getText().trim();
        if (destination.isEmpty()) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Input Error", "Enter destination address.");
            return;
        }
        
        double distance;
        try {
            distance = Double.parseDouble(lastMileDistanceField.getText().trim());
            if (distance <= 0) {
                showAlert(JOptionPane.WARNING_MESSAGE, "Input Error", "Distance must be a positive number.");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Input Error", "Invalid distance. Please enter a number.");
            return;
        }

        List<LastMileService.TransportOption> options =
            lastMileService.getAvailableOptions(destination, distance);

        currentLastMileOptions = new ArrayList<>();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Transport Mode");
        model.addColumn("Fare (₹)");
        model.addColumn("Est. Time");
        model.addColumn("CO₂ Emission");

        for (LastMileService.TransportOption option : options) {
            TransportMode mode = option.getMode();
            if (mode == TransportMode.RTC_BUS ||
                mode == TransportMode.OLA ||
                mode == TransportMode.UBER ||
                mode == TransportMode.RAPIDO) {
                
                currentLastMileOptions.add(option);
                model.addRow(new Object[]{
                    mode.getDisplayName(),
                    String.format("₹%.0f", option.getFare()),
                    option.getEstimatedMinutes() + " min",
                    String.format("%.1f kg", option.getFare() * 0.1)
                });
            }
        }

        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"No options", "—", "—", "—"});
        }
        
        lastMileTable.setModel(model);
        showAlert(JOptionPane.INFORMATION_MESSAGE, "Success", "✓ Transport options loaded!");
    }

    private void startLastMileRide() {
        int idx = lastMileTable.getSelectedRow();
        if (idx < 0 || idx >= currentLastMileOptions.size()) {
            showAlert(JOptionPane.WARNING_MESSAGE, "Selection Error", "Select a transport option first.");
            return;
        }
        LastMileService.TransportOption option = currentLastMileOptions.get(idx);
        showAlert(JOptionPane.INFORMATION_MESSAGE, "Ride Started", 
            "✓ Last mile ride started with " + option.getMode().getDisplayName() + "!\n" +
            "Estimated time: " + option.getEstimatedMinutes() + " minutes");
    }

    private void handleSaveTrip() {
        if (currentRouteStations == null || currentRouteStations.isEmpty()) {
            showAlert(JOptionPane.WARNING_MESSAGE, "No Trip", "No route to save. Find a route first.");
            return;
        }
        
        String destination = lastMileAddressField.getText().trim();
        double lastMileDistance = 0.0;
        
        try {
            if (!lastMileDistanceField.getText().trim().isEmpty()) {
                lastMileDistance = Double.parseDouble(lastMileDistanceField.getText().trim());
            }
        } catch (NumberFormatException ex) {
            // Ignore
        }

        LastMileService.TransportOption selectedOption = null;
        int selectedIndex = lastMileTable.getSelectedRow();
        if (selectedIndex >= 0 && selectedIndex < currentLastMileOptions.size()) {
            selectedOption = currentLastMileOptions.get(selectedIndex);
        }

        try {
            double metroDistance = carbonService.calculateDistance(currentRouteStations);
            double co2Saved = carbonService.calculateCO2Saved(metroDistance);
            
            FileHandler.recordTrip(currentUser, currentRouteStations, lastMileDistance, co2Saved);
            FileHandler.saveTrip(currentUser, currentRouteStations, selectedOption,
                destination.isEmpty() ? null : destination, currentMetroFare);
            
            showAlert(JOptionPane.INFORMATION_MESSAGE, "Success", "✓ Trip saved and statistics updated!");
        } catch (Exception ex) {
            showAlert(JOptionPane.ERROR_MESSAGE, "Save Error", "Error saving trip: " + ex.getMessage());
        }
    }

    private JPanel buildCrowdPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        crowdTable = createStyledTable();
        JScrollPane scrollPane = new JScrollPane(crowdTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Platform Crowd Density Information"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("🔄 Refresh Crowd Information");
        refreshButton.setBackground(new Color(0x00897B));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadCrowdInfo());
        loadCrowdInfo();

        return panel;
    }

    private void loadCrowdInfo() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Station");
        model.addColumn("PCDI Value");
        model.addColumn("Level");
        model.addColumn("Status");

        if (currentRouteStations == null || currentRouteStations.isEmpty()) {
            model.addRow(new Object[]{"—", "—", "—", "Plan a route to see crowd levels"});
        } else {
            for (Station station : currentRouteStations) {
                double pcdi = crowdService.getCrowdDensity(station.getName());
                int level = crowdService.getCrowdLevel(station.getName());
                String emoji = crowdService.getCrowdLevelEmoji(level);
                String description = crowdService.getCrowdLevelDescription(level);
                
                model.addRow(new Object[]{
                    station.getName(),
                    String.format("%.2f", pcdi),
                    "Level " + level,
                    emoji + " " + description
                });
            }
        }
        crowdTable.setModel(model);
    }

    private JPanel buildCarbonPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        carbonArea = new JTextArea();
        carbonArea.setEditable(false);
        carbonArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        carbonArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(carbonArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Carbon Footprint Analysis"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("🔄 Refresh Carbon Information");
        refreshButton.setBackground(new Color(0x00796B));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadCarbonInfo());
        loadCarbonInfo();

        return panel;
    }

    private void loadCarbonInfo() {
        if (currentRouteStations == null || currentRouteStations.isEmpty()) {
            carbonArea.setText("Plan a route in the Metro Ticket tab to see your carbon savings.\n\n" +
                "Selecting metro over private vehicles reduces:\n" +
                "• CO₂ emissions\n" +
                "• Air pollution\n" +
                "• Traffic congestion\n" +
                "• Energy consumption");
            return;
        }
        
        double distance = carbonService.calculateDistance(currentRouteStations);
        double co2Saved = carbonService.calculateCO2Saved(distance);
        
        StringBuilder info = new StringBuilder();
        info.append("════════════════ CARBON FOOTPRINT ANALYSIS ════════════════\n\n");
        info.append("Distance Traveled:           ").append(String.format("%.1f km", distance)).append("\n");
        info.append("CO₂ Saved vs. Private Car:   ").append(carbonService.formatCO2Saved(co2Saved)).append("\n");
        info.append("\n════════════════════════════════════════════════════════════\n");
        info.append("\n✓ Environmental Impact:\n");
        info.append("  • Trees equivalent: ").append(String.format("%.1f", co2Saved / 0.021)).append("\n");
        info.append("  • Fuel saved: ").append(String.format("%.2f L", distance * 0.07)).append("\n");
        info.append("  • Cleaner air for all!\n");
        info.append("\nYou're making an eco-friendly choice! 🌱\n");
        info.append("════════════════════════════════════════════════════════════\n");
        
        carbonArea.setText(info.toString());
    }

    private JPanel buildStationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 248, 255));

        JLabel titleLabel = new JLabel("🚇 SOLVEXIS METRO NETWORK - ALL LINES");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(15, 32, 60));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel linesPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        linesPanel.setBackground(new Color(245, 248, 255));

        linesPanel.add(buildLinePanel("🔴 Red Line", "Miyapur → LB Nagar", new String[]{
            "Miyapur", "JNTU College", "KPHB Colony", "Kukatpally", "Balanagar",
            "Moosapet", "Bharat Nagar", "Erragadda", "ESI Hospital", "SR Nagar",
            "Ameerpet", "Punjagutta", "Irrum Manzil", "Khairatabad", "Lakdi-ka-pul",
            "Assembly", "Nampally", "Gandhi Bhavan", "Osmania Medical College",
            "MGBS", "Malakpet", "New Market", "Musarambagh", "Dilsukhnagar",
            "Chaitanyapuri", "Victoria Memorial", "LB Nagar"
        }, new Color(0xB71C1C), new Color(255, 200, 200)));

        linesPanel.add(buildLinePanel("🔵 Blue Line", "Nagole → Raidurg", new String[]{
            "Nagole", "Uppal", "Stadium", "NGRI", "Habsiguda", "Tarnaka",
            "Mettuguda", "Secunderabad East", "Parade Ground", "Paradise", "Rasoolpura",
            "Prakash Nagar", "Begumpet", "Ameerpet", "Madhura Nagar", "Yusufguda",
            "Road No. 5 Jubilee Hills", "Jubilee Hills Check Post", "Peddamma Gudi",
            "Madhapur", "Durgam Cheruvu", "Hitec City", "Raidurg"
        }, new Color(0x0D47A1), new Color(173, 216, 230)));

        linesPanel.add(buildLinePanel("🟢 Green Line", "JBS Parade → MGBS", new String[]{
            "JBS Parade Ground", "Secunderabad West", "Gandhi Hospital", "Musheerabad",
            "RTC Cross Roads", "Chikkadpally", "Narayanaguda", "Sultan Bazaar", "MGBS"
        }, new Color(0x1B5E20), new Color(144, 238, 144)));

        panel.add(linesPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildLinePanel(String title, String route, String[] stations, Color lineColor, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(lineColor, 4),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(lineColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        // Route info
        JLabel routeLabel = new JLabel(route);
        routeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        routeLabel.setForeground(new Color(70, 70, 70));
        routeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(routeLabel);

        // Divider
        panel.add(Box.createVerticalStrut(8));
        JSeparator separator = new JSeparator();
        separator.setForeground(lineColor);
        panel.add(separator);
        panel.add(Box.createVerticalStrut(8));

        // Station count
        JLabel countLabel = new JLabel("Stations: " + stations.length);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        countLabel.setForeground(lineColor);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(countLabel);
        panel.add(Box.createVerticalStrut(10));

        // Stations in scrollable area
        JPanel stationsContainer = new JPanel();
        stationsContainer.setLayout(new BoxLayout(stationsContainer, BoxLayout.Y_AXIS));
        stationsContainer.setOpaque(false);

        for (int i = 0; i < stations.length; i++) {
            JPanel stationRow = new JPanel(new BorderLayout(8, 0));
            stationRow.setOpaque(false);
            stationRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

            // Station number circle
            JLabel numberLabel = new JLabel(String.format("%02d", i + 1));
            numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            numberLabel.setForeground(Color.WHITE);
            numberLabel.setBackground(lineColor);
            numberLabel.setOpaque(true);
            numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
            numberLabel.setPreferredSize(new Dimension(28, 28));
            numberLabel.setBorder(BorderFactory.createLineBorder(lineColor, 2));

            // Station name
            JLabel stationLabel = new JLabel("◆ " + stations[i]);
            stationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            stationLabel.setForeground(new Color(40, 40, 40));

            stationRow.add(numberLabel, BorderLayout.WEST);
            stationRow.add(stationLabel, BorderLayout.CENTER);

            stationsContainer.add(stationRow);
            stationsContainer.add(Box.createVerticalStrut(4));
        }

        JScrollPane scrollPane = new JScrollPane(stationsContainer);
        scrollPane.setOpaque(false);
        
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane);

        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        historyArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Trip History"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("🔄 Refresh Trip History");
        refreshButton.setBackground(new Color(0x5E35B1));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadTripHistory());
        loadTripHistory();

        return panel;
    }

    private void loadTripHistory() {
        String text = FileHandler.loadTripHistory(currentUser);
        historyArea.setText(text);
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statsTable = createStyledTable();
        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Travel Statistics"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("🔄 Refresh Statistics");
        refreshButton.setBackground(new Color(0x3949AB));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadStats());
        loadStats();

        return panel;
    }

    private void loadStats() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Metric");
        model.addColumn("Value");

        if (statsService == null) {
            model.addRow(new Object[]{"Status", "No statistics available. Take some trips first!"});
        } else {
            model.addRow(new Object[]{"Total Trips Taken", statsService.getTotalTrips()});
            model.addRow(new Object[]{"Total Distance", String.format("%.1f km", statsService.getTotalKmTraveled())});
            model.addRow(new Object[]{"CO₂ Saved", String.format("%.2f kg", statsService.getTotalCO2Saved())});
            model.addRow(new Object[]{"Longest Route", String.format("%.1f km", statsService.getLongestRoute())});
            model.addRow(new Object[]{"Points Earned", statsService.getPointsEarned() + " 🏆"});
        }
        
        statsTable.setModel(model);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MetroUIApp app = new MetroUIApp();
            app.setVisible(true);
        });
    }
}
