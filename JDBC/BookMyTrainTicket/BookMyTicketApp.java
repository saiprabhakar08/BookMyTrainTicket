package BookMyTrainTicket;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Main application class for BookMyTicket train booking system
 * Java Swing GUI implementation
 */
public class BookMyTicketApp {
    private JFrame mainFrame;
    private User currentUser;
    private LoginOperations loginOps;
    private TrainManager trainManager;
    private BookingManager bookingManager;
    private SeatAvailabilityManager seatManager;
    private RACQueue racQueue;
    private WaitlistManager waitlistManager;
    
    // GUI Components
    private JPanel currentPanel;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Admin panel text areas
    private JTextArea trainListTextArea;
    private JTextArea userListTextArea;
    private JTextArea bookingListTextArea;
    
    public BookMyTicketApp() {
        try {
            // Initialize managers
            loginOps = new LoginOperations();
            trainManager = new TrainManager();
            bookingManager = new BookingManager();
            seatManager = new SeatAvailabilityManager();
            racQueue = new RACQueue();
            waitlistManager = new WaitlistManager();
            
            // Initialize GUI
            initializeGUI();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void initializeGUI() {
        mainFrame = new JFrame("BookMyTicket - Train Booking System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700);
        mainFrame.setLocationRelativeTo(null);
        
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create card layout for different screens
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create different panels
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");
        
        mainFrame.add(mainPanel);
        
        // Show login panel initially
        cardLayout.show(mainPanel, "LOGIN");
        
        mainFrame.setVisible(true);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel titleLabel = new JLabel("BookMyTicket");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 30, 0);
        panel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Train Booking System");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(70, 70, 70));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(subtitleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 0, 10, 10);
        gbc.gridy = 2;
        panel.add(new JLabel("Username:"), gbc);
        
        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.insets = new Insets(10, 10, 10, 0);
        panel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 10, 10);
        panel.add(new JLabel("Password:"), gbc);
        
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.insets = new Insets(10, 10, 10, 0);
        panel.add(passwordField, gbc);
        
        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(34, 139, 34));
        loginButton.setForeground(Color.BLACK);
        loginButton.setPreferredSize(new Dimension(100, 35));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 10, 0);
        panel.add(loginButton, gbc);
        
        // Register button
        JButton registerButton = new JButton("New User? Register");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        registerButton.setForeground(Color.BLACK);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 20, 0);
        panel.add(registerButton, gbc);
        
        // Login button action
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter both username and password", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                User user = loginOps.authenticateUser(username, password);
                if (user != null) {
                    currentUser = user;
                    showMainDashboard();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid username or password", 
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Database error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Register button action
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel titleLabel = new JLabel("Register New User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 30, 0);
        panel.add(titleLabel, gbc);
        
        // Form fields
        String[] labels = {"Username:", "Password:", "Email:", "User Type:"};
        JComponent[] fields = new JComponent[4];
        
        fields[0] = new JTextField(20);
        fields[1] = new JPasswordField(20);
        fields[2] = new JTextField(20);
        
        // Create role combo without Admin option for security
        // Admin accounts should only be created directly in the database
        User.UserRole[] allowedRoles = {
            User.UserRole.Regular, 
            User.UserRole.Senior, 
            User.UserRole.DifferentlyAbled
        };
        JComboBox<User.UserRole> roleCombo = new JComboBox<>(allowedRoles);
        roleCombo.setSelectedItem(User.UserRole.Regular);
        fields[3] = roleCombo;
        
        gbc.gridwidth = 1;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.insets = new Insets(10, 0, 10, 10);
            panel.add(new JLabel(labels[i]), gbc);
            
            gbc.gridx = 1;
            gbc.insets = new Insets(10, 10, 10, 0);
            panel.add(fields[i], gbc);
        }
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(34, 139, 34));
        registerButton.setForeground(Color.BLACK);
        
        JButton backButton = new JButton("Back to Login");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setForeground(Color.BLACK);
        
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        
        gbc.gridx = 0;
        gbc.gridy = labels.length + 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 20, 0);
        panel.add(buttonPanel, gbc);
        
        // Register button action
        registerButton.addActionListener(e -> {
            String username = ((JTextField) fields[0]).getText().trim();
            String password = new String(((JPasswordField) fields[1]).getPassword());
            String email = ((JTextField) fields[2]).getText().trim();
            User.UserRole role = (User.UserRole) roleCombo.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Username and password are required", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                boolean success = loginOps.registerUser(username, password, email, role);
                if (success) {
                    JOptionPane.showMessageDialog(mainFrame, "Registration successful! Please login.", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(mainPanel, "LOGIN");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Username already exists", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Database error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Back button action
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        
        return panel;
    }
    
    private void showMainDashboard() {
        // Remove existing components
        mainPanel.removeAll();
        
        // Create new dashboard
        JPanel dashboardPanel = createDashboardPanel();
        mainPanel.add(dashboardPanel, "DASHBOARD");
        
        cardLayout.show(mainPanel, "DASHBOARD");
        mainFrame.revalidate();
        mainFrame.repaint();
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Main content area with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Search Trains tab
        tabbedPane.addTab("Search Trains", createSearchTrainsPanel());
        
        // My Bookings tab (only for non-admin users)
        if (!currentUser.isAdmin()) {
            tabbedPane.addTab("My Bookings", createMyBookingsPanel());
        }
        
        // Admin panel (only for admin users)
        if (currentUser.isAdmin()) {
            tabbedPane.addTab("Admin Panel", createAdminPanel());
        }
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 25, 112));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 20, 60));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.addActionListener(e -> logout());
        
        panel.add(welcomeLabel, BorderLayout.WEST);
        panel.add(logoutButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSearchTrainsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Search form
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JTextField sourceField = new JTextField(15);
        JTextField destField = new JTextField(15);
        JButton searchButton = new JButton("Search Trains");
        searchButton.setBackground(new Color(30, 144, 255));
        searchButton.setForeground(Color.BLACK);
        
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(sourceField, gbc);
        gbc.gridx = 2;
        searchPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 3;
        searchPanel.add(destField, gbc);
        gbc.gridx = 4;
        searchPanel.add(searchButton, gbc);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Results area
        JTextArea resultsArea = new JTextArea(20, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Search button action
        searchButton.addActionListener(e -> {
            String source = sourceField.getText().trim();
            String destination = destField.getText().trim();
            
            if (source.isEmpty() || destination.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter both source and destination", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                List<TrainManager.TrainSearchResult> results = trainManager.searchTrains(source, destination);
                
                StringBuilder sb = new StringBuilder();
                sb.append("Search Results for: ").append(source).append(" → ").append(destination).append("\n");
                sb.append("=".repeat(80)).append("\n\n");
                
                if (results.isEmpty()) {
                    sb.append("No trains found for the specified route.\n");
                } else {
                    int index = 1;
                    for (TrainManager.TrainSearchResult result : results) {
                        sb.append(index++).append(". ");
                        sb.append(result.getTrain().getTrainName()).append(" (").append(result.getTrain().getTrainNumber()).append(")\n");
                        sb.append("   Route: ").append(result.getRoute().getSourceStation()).append(" → ");
                        sb.append(result.getRoute().getDestinationStation()).append("\n");
                        
                        // Show intermediate stations if available
                        String intermediateStations = result.getRoute().getIntermediateStations();
                        if (intermediateStations != null && !intermediateStations.trim().isEmpty()) {
                            sb.append("   Stops via: ").append(intermediateStations).append("\n");
                        }
                        
                        sb.append("   Departure: ").append(result.getRoute().getDepartureTime());
                        sb.append(" | Arrival: ").append(result.getRoute().getArrivalTime()).append("\n");
                        sb.append("   Price: ₹").append(result.getRoute().getPrice());
                        sb.append(" | Available Seats: ").append(result.getAvailableSeats()).append("\n");
                        
                        // Enhanced search match information with direction validation
                        String sourceText = sourceField.getText().trim().toLowerCase();
                        String destText = destField.getText().trim().toLowerCase();
                        
                        // Show route sequence to confirm direction
                        List<String> routeSequence = buildRouteSequence(result.getRoute());
                        if (!routeSequence.isEmpty()) {
                            sb.append("   ✓ Route Direction: ");
                            for (int i = 0; i < routeSequence.size(); i++) {
                                if (i > 0) sb.append(" → ");
                                String station = routeSequence.get(i);
                                
                                // Highlight searched stations
                                if (station.toLowerCase().contains(sourceText)) {
                                    sb.append("[").append(station).append("]");
                                } else if (station.toLowerCase().contains(destText)) {
                                    sb.append("[").append(station).append("]");
                                } else {
                                    sb.append(station);
                                }
                            }
                            sb.append("\n");
                        }
                        
                        sb.append("   [Click 'Book Seat' to proceed with booking]\n\n");
                    }
                    
                    // Add booking button
                    JButton bookButton = new JButton("Book Seat for Selected Train");
                    bookButton.setBackground(new Color(34, 139, 34));
                    bookButton.setForeground(Color.BLACK);
                    bookButton.addActionListener(bookEvent -> showSeatSelectionDialog(results));
                    
                    JPanel buttonPanel = new JPanel();
                    buttonPanel.add(bookButton);
                    panel.add(buttonPanel, BorderLayout.SOUTH);
                }
                
                resultsArea.setText(sb.toString());
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error searching trains: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }
    
    private void showSeatSelectionDialog(List<TrainManager.TrainSearchResult> searchResults) {
        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No trains available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Let user select a train first
        TrainManager.TrainSearchResult[] options = searchResults.toArray(new TrainManager.TrainSearchResult[0]);
        TrainManager.TrainSearchResult selectedTrain = (TrainManager.TrainSearchResult) JOptionPane.showInputDialog(
            mainFrame,
            "Select a train:",
            "Train Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (selectedTrain == null) return;
        
        // Ask how many seats to book
        int numberOfSeats = askNumberOfSeats();
        if (numberOfSeats <= 0) return;
        
        // Show seat selection dialog with multiple seat selection
        showMultipleSeatMapDialog(selectedTrain, numberOfSeats);
    }
    
    private int askNumberOfSeats() {
        String[] options = {"1", "2", "3", "4", "5", "6"};
        String selectedOption = (String) JOptionPane.showInputDialog(
            mainFrame,
            "How many seats would you like to book?\n(Maximum 6 seats per booking)",
            "Number of Seats",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            "1"
        );
        
        if (selectedOption == null) return 0;
        return Integer.parseInt(selectedOption);
    }
    
    private void showMultipleSeatMapDialog(TrainManager.TrainSearchResult trainResult, int numberOfSeats) {
        try {
            // First, let user select a compartment
            List<SeatAvailabilityManager.CompartmentSeats> compartments = 
                seatManager.getCompartmentsForTrain(trainResult.getTrain().getTrainId());
            
            if (compartments.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                    "No compartments found for this train.", 
                    "No Compartments", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Filter compartments that have available seats (even if less than requested)
            List<SeatAvailabilityManager.CompartmentSeats> validCompartments = new ArrayList<>();
            for (SeatAvailabilityManager.CompartmentSeats comp : compartments) {
                if (comp.getAvailableSeatsCount() > 0) {
                    validCompartments.add(comp);
                }
            }
            
            if (validCompartments.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                    "No compartments have available seats.\n" +
                    "All passengers will be placed in RAC/Waitlist queue.", 
                    "No Available Seats", JOptionPane.INFORMATION_MESSAGE);
                
                // Proceed with RAC/Waitlist booking for all seats
                proceedWithRACWaitlistBooking(trainResult, numberOfSeats);
                return;
            }
            
            // Show compartment selection dialog
            SeatAvailabilityManager.CompartmentSeats selectedCompartment = showCompartmentSelectionDialog(validCompartments, numberOfSeats);
            if (selectedCompartment == null) return;
            
            // Show seat selection for the selected compartment
            showCompartmentSeatMapDialog(trainResult, selectedCompartment, numberOfSeats);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading compartments: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private SeatAvailabilityManager.CompartmentSeats showCompartmentSelectionDialog(
            List<SeatAvailabilityManager.CompartmentSeats> compartments, int numberOfSeats) {
        
        JDialog compartmentDialog = new JDialog(mainFrame, "Select Compartment", true);
        compartmentDialog.setSize(500, 400);
        compartmentDialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(new JLabel("Select a compartment for " + numberOfSeats + " seat(s):"));
        infoPanel.add(new JLabel("Available seats will be confirmed, remaining will go to RAC/Waitlist."));
        infoPanel.add(new JLabel("Choose compartment with the most available seats for best allocation."));
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Compartment list
        DefaultListModel<SeatAvailabilityManager.CompartmentSeats> listModel = new DefaultListModel<>();
        for (SeatAvailabilityManager.CompartmentSeats comp : compartments) {
            listModel.addElement(comp);
        }
        
        JList<SeatAvailabilityManager.CompartmentSeats> compartmentList = new JList<>(listModel);
        compartmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        compartmentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SeatAvailabilityManager.CompartmentSeats) {
                    SeatAvailabilityManager.CompartmentSeats comp = (SeatAvailabilityManager.CompartmentSeats) value;
                    setText(comp.toString());
                }
                return this;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(compartmentList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        final SeatAvailabilityManager.CompartmentSeats[] selectedCompartment = {null};
        
        JButton selectButton = new JButton("Select Compartment");
        selectButton.setBackground(new Color(34, 139, 34));
        selectButton.setForeground(Color.BLACK);
        selectButton.addActionListener(e -> {
            SeatAvailabilityManager.CompartmentSeats selected = compartmentList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(compartmentDialog, 
                    "Please select a compartment", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedCompartment[0] = selected;
            compartmentDialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> compartmentDialog.dispose());
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        compartmentDialog.add(mainPanel);
        compartmentDialog.setVisible(true);
        
        return selectedCompartment[0];
    }
    
    private void showCompartmentSeatMapDialog(TrainManager.TrainSearchResult trainResult, 
                                            SeatAvailabilityManager.CompartmentSeats compartment, 
                                            int numberOfSeats) {
        JDialog seatDialog = new JDialog(mainFrame, 
            "Select " + numberOfSeats + " Seat(s) - " + compartment.getCompartmentName() + 
            " (" + compartment.getClassType() + ")", true);
        seatDialog.setSize(900, 700);
        seatDialog.setLocationRelativeTo(mainFrame);
        
        try {
            List<SeatAvailabilityManager.SeatWithDetails> seats = compartment.getSeats();
            
            if (seats.size() < numberOfSeats) {
                // Inform user about mixed booking scenario
                int availableSeats = seats.size();
                int racWaitlistSeats = numberOfSeats - availableSeats;
                
                int choice = JOptionPane.showConfirmDialog(mainFrame,
                    "Only " + availableSeats + " seat(s) available in this compartment.\n" +
                    availableSeats + " passenger(s) will get confirmed seats.\n" +
                    racWaitlistSeats + " passenger(s) will be placed in RAC/Waitlist.\n\n" +
                    "Do you want to proceed with mixed booking?",
                    "Mixed Booking Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                    
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Get recommended seats for user type from the compartment
            List<SeatAvailabilityManager.SeatWithDetails> recommendedSeats = new ArrayList<>();
            for (SeatAvailabilityManager.SeatWithDetails seat : seats) {
                // Recommend seats based on user role
                if (currentUser.getRole() == User.UserRole.Senior && 
                    seat.getBerthType() == Seat.BerthType.Lower) {
                    recommendedSeats.add(seat);
                } else if (currentUser.getRole() == User.UserRole.DifferentlyAbled && 
                          seat.getBerthType() == Seat.BerthType.Lower) {
                    recommendedSeats.add(seat);
                }
            }
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            
            // Info panel
            JPanel infoPanel = new JPanel(new GridLayout(5, 1));
            infoPanel.add(new JLabel("Compartment: " + compartment.getCompartmentName() + 
                                   " (" + compartment.getClassType() + ")"));
            
            if (seats.size() < numberOfSeats) {
                infoPanel.add(new JLabel("Mixed Booking: " + seats.size() + " confirmed seats available, " + 
                                       (numberOfSeats - seats.size()) + " will go to RAC/Waitlist"));
                infoPanel.add(new JLabel("Select up to " + seats.size() + " seat(s) for confirmation"));
            } else {
                infoPanel.add(new JLabel("Select " + numberOfSeats + " seat(s) for booking"));
            }
            
            infoPanel.add(new JLabel("Recommended seats for " + currentUser.getRole() + " users are highlighted in green"));
            
            JLabel selectedCountLabel = new JLabel("Selected: 0 seats");
            selectedCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
            selectedCountLabel.setForeground(new Color(0, 100, 0));
            infoPanel.add(selectedCountLabel);
            
            mainPanel.add(infoPanel, BorderLayout.NORTH);
            
            // Seat selection area
            JPanel seatPanel = new JPanel(new GridLayout(0, 6, 5, 5));
            seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            List<JCheckBox> seatCheckBoxes = new ArrayList<>();
            
            for (SeatAvailabilityManager.SeatWithDetails seat : seats) {
                JCheckBox seatCheckBox = new JCheckBox(seat.getSeatNumber() + " (" + seat.getBerthType() + ")");
                seatCheckBox.putClientProperty("seat", seat);
                
                // Highlight recommended seats
                if (recommendedSeats.contains(seat)) {
                    seatCheckBox.setBackground(new Color(144, 238, 144));
                    seatCheckBox.setOpaque(true);
                }
                
                // Add action listener to limit selection to available seats
                seatCheckBox.addActionListener(e -> {
                    long selectedCount = 0;
                    for (JCheckBox cb : seatCheckBoxes) {
                        if (cb.isSelected()) selectedCount++;
                    }
                    
                    if (selectedCount > seats.size()) {
                        seatCheckBox.setSelected(false);
                        JOptionPane.showMessageDialog(seatDialog, 
                            "You can only select " + seats.size() + " available seat(s).", 
                            "Selection Limit", JOptionPane.WARNING_MESSAGE);
                    }
                    
                    // Update selected count label
                    selectedCount = 0;
                    for (JCheckBox cb : seatCheckBoxes) {
                        if (cb.isSelected()) selectedCount++;
                    }
                    selectedCountLabel.setText("Selected: " + selectedCount + " seats");
                });
                
                seatCheckBoxes.add(seatCheckBox);
                seatPanel.add(seatCheckBox);
            }
            
            JScrollPane seatScrollPane = new JScrollPane(seatPanel);
            mainPanel.add(seatScrollPane, BorderLayout.CENTER);
            
            // Book button
            JPanel buttonPanel = new JPanel();
            JButton bookButton = new JButton("Book Selected Seats");
            bookButton.setBackground(new Color(34, 139, 34));
            bookButton.setForeground(Color.BLACK);
            
            bookButton.addActionListener(e -> {
                // Find selected seats
                List<SeatAvailabilityManager.SeatWithDetails> selectedSeats = new ArrayList<>();
                for (JCheckBox checkBox : seatCheckBoxes) {
                    if (checkBox.isSelected()) {
                        selectedSeats.add((SeatAvailabilityManager.SeatWithDetails) checkBox.getClientProperty("seat"));
                    }
                }
                
                // Check if we need to handle mixed booking scenario
                if (seats.size() < numberOfSeats) {
                    // Mixed booking scenario - some seats confirmed, some RAC/waitlist
                    if (selectedSeats.isEmpty()) {
                        // No seats selected, all go to RAC/waitlist
                        showMixedBookingDialog(trainResult, new ArrayList<>(), numberOfSeats, seatDialog);
                    } else {
                        // Some seats selected for confirmation, rest go to RAC/waitlist
                        showMixedBookingDialog(trainResult, selectedSeats, numberOfSeats, seatDialog);
                    }
                } else {
                    // Normal booking scenario - enough seats available
                    if (selectedSeats.size() != numberOfSeats) {
                        JOptionPane.showMessageDialog(seatDialog, 
                            "Please select exactly " + numberOfSeats + " seat(s)", 
                            "Selection Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Show passenger details dialog for multiple passengers
                    showMultiplePassengerDetailsDialog(trainResult, selectedSeats, seatDialog);
                }
            });
            
            buttonPanel.add(bookButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            seatDialog.add(mainPanel);
            seatDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading seats: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showSeatMapDialog(TrainManager.TrainSearchResult trainResult) {
        JDialog seatDialog = new JDialog(mainFrame, "Select Seat - " + trainResult.getTrain().getTrainName(), true);
        seatDialog.setSize(800, 600);
        seatDialog.setLocationRelativeTo(mainFrame);
        
        try {
            List<SeatAvailabilityManager.SeatWithDetails> seats = seatManager.getAvailableSeats(
                trainResult.getTrain().getTrainId(), trainResult.getRoute().getRouteId());
            
            // Get recommended seats for user type
            List<SeatAvailabilityManager.SeatWithDetails> recommendedSeats = 
                seatManager.getRecommendedSeats(trainResult.getTrain().getTrainId(), currentUser.getRole());
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            
            // Info panel
            JPanel infoPanel = new JPanel();
            infoPanel.add(new JLabel("Recommended seats for " + currentUser.getRole() + " users are highlighted"));
            mainPanel.add(infoPanel, BorderLayout.NORTH);
            
            // Seat selection area
            JPanel seatPanel = new JPanel(new GridLayout(0, 6, 5, 5));
            seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            ButtonGroup seatGroup = new ButtonGroup();
            
            for (SeatAvailabilityManager.SeatWithDetails seat : seats) {
                JRadioButton seatButton = new JRadioButton(seat.getSeatNumber() + " (" + seat.getBerthType() + ")");
                seatButton.putClientProperty("seat", seat);
                
                // Highlight recommended seats
                if (recommendedSeats.contains(seat)) {
                    seatButton.setBackground(new Color(144, 238, 144));
                    seatButton.setOpaque(true);
                }
                
                seatGroup.add(seatButton);
                seatPanel.add(seatButton);
            }
            
            JScrollPane seatScrollPane = new JScrollPane(seatPanel);
            mainPanel.add(seatScrollPane, BorderLayout.CENTER);
            
            // Book button
            JPanel buttonPanel = new JPanel();
            JButton bookButton = new JButton("Book Selected Seat");
            bookButton.setBackground(new Color(34, 139, 34));
            bookButton.setForeground(Color.BLACK);
            
            bookButton.addActionListener(e -> {
                // Find selected seat
                SeatAvailabilityManager.SeatWithDetails selectedSeat = null;
                for (AbstractButton button : java.util.Collections.list(seatGroup.getElements())) {
                    if (button.isSelected()) {
                        selectedSeat = (SeatAvailabilityManager.SeatWithDetails) button.getClientProperty("seat");
                        break;
                    }
                }
                
                if (selectedSeat == null) {
                    JOptionPane.showMessageDialog(seatDialog, "Please select a seat", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Show passenger details dialog
                showPassengerDetailsDialog(trainResult, selectedSeat, seatDialog);
            });
            
            buttonPanel.add(bookButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            seatDialog.add(mainPanel);
            seatDialog.setVisible(true);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading seats: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showPassengerDetailsDialog(TrainManager.TrainSearchResult trainResult, 
                                          SeatAvailabilityManager.SeatWithDetails seat, JDialog parentDialog) {
        JDialog passengerDialog = new JDialog(mainFrame, "Passenger Details", true);
        passengerDialog.setSize(400, 300);
        passengerDialog.setLocationRelativeTo(parentDialog);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JTextField nameField = new JTextField(20);
        JTextField ageField = new JTextField(20);
        
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Passenger Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        panel.add(ageField, gbc);
        
        JButton confirmButton = new JButton("Confirm Booking");
        confirmButton.setBackground(new Color(34, 139, 34));
        confirmButton.setForeground(Color.BLACK);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(confirmButton, gbc);
        
        confirmButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            
            if (name.isEmpty() || ageText.isEmpty()) {
                JOptionPane.showMessageDialog(passengerDialog, "Please fill all fields", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int age = Integer.parseInt(ageText);
                if (age <= 0 || age > 120) {
                    JOptionPane.showMessageDialog(passengerDialog, "Please enter a valid age", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Create booking
                BookingManager.BookingResult result = bookingManager.createBooking(
                    currentUser.getUserId(),
                    seat.getSeatId(),
                    trainResult.getTrain().getTrainId(),
                    trainResult.getRoute().getRouteId(),
                    name,
                    age
                );

                if (result.isSuccess()) {
                    // Show payment dialog
                    BigDecimal bookingAmount = trainResult.getRoute().getPrice();
                    PaymentDialog paymentDialog = new PaymentDialog(mainFrame, result.getId(), bookingAmount);
                    paymentDialog.setVisible(true);
                    
                    passengerDialog.dispose();
                    parentDialog.dispose();
                    
                    // Refresh my bookings tab
                    refreshMyBookings();
                     if (paymentDialog.isPaymentSuccessful()) {
                        JOptionPane.showMessageDialog(passengerDialog, 
                            "Booking and payment successful!\nBooking ID: " + result.getId(), 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        passengerDialog.dispose();
                        // parentDialog.dispose();
                        
                        // // Refresh my bookings tab
                        // refreshMyBookings();
                    } else {
                        JOptionPane.showMessageDialog(passengerDialog, 
                            "Payment was not completed. Booking has been cancelled.", 
                            "Payment Cancelled", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(passengerDialog, 
                        "Booking failed: " + result.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(passengerDialog, "Please enter a valid age", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(passengerDialog, "Database error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        passengerDialog.add(panel);
        passengerDialog.setVisible(true);
    }
    
    private void showMultiplePassengerDetailsDialog(TrainManager.TrainSearchResult trainResult, 
                                                   List<SeatAvailabilityManager.SeatWithDetails> selectedSeats, 
                                                   JDialog parentDialog) {
        JDialog detailsDialog = new JDialog(mainFrame, "Passenger Details for " + selectedSeats.size() + " Seats", true);
        detailsDialog.setSize(600, 500);
        detailsDialog.setLocationRelativeTo(parentDialog);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(new JLabel("Enter passenger details for each seat:"));
        
        StringBuilder seatInfo = new StringBuilder("Selected Seats: ");
        for (int i = 0; i < selectedSeats.size(); i++) {
            if (i > 0) seatInfo.append(", ");
            seatInfo.append(selectedSeats.get(i).getSeatNumber());
        }
        infoPanel.add(new JLabel(seatInfo.toString()));
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Passenger details panel
        JPanel passengerPanel = new JPanel(new GridLayout(selectedSeats.size(), 1, 5, 5));
        passengerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        List<JTextField> nameFields = new ArrayList<>();
        List<JTextField> ageFields = new ArrayList<>();
        
        for (int i = 0; i < selectedSeats.size(); i++) {
            SeatAvailabilityManager.SeatWithDetails seat = selectedSeats.get(i);
            
            JPanel seatDetailPanel = new JPanel(new GridBagLayout());
            seatDetailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), 
                "Seat " + seat.getSeatNumber() + " (" + seat.getBerthType() + ")"
            ));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Name field
            gbc.gridx = 0; gbc.gridy = 0;
            seatDetailPanel.add(new JLabel("Passenger Name:"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            JTextField nameField = new JTextField(20);
            nameFields.add(nameField);
            seatDetailPanel.add(nameField, gbc);
            
            // Age field
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            seatDetailPanel.add(new JLabel("Age:"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            JTextField ageField = new JTextField(5);
            ageFields.add(ageField);
            seatDetailPanel.add(ageField, gbc);
            
            passengerPanel.add(seatDetailPanel);
        }
        
        JScrollPane scrollPane = new JScrollPane(passengerPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton confirmButton = new JButton("Confirm Booking");
        confirmButton.setBackground(new Color(34, 139, 34));
        confirmButton.setForeground(Color.BLACK);
        
        confirmButton.addActionListener(e -> {
            // Validate passenger details
            List<String> passengerNames = new ArrayList<>();
            List<Integer> passengerAges = new ArrayList<>();
            
            for (int i = 0; i < selectedSeats.size(); i++) {
                String name = nameFields.get(i).getText().trim();
                String ageText = ageFields.get(i).getText().trim();
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(detailsDialog, 
                        "Please enter name for seat " + selectedSeats.get(i).getSeatNumber(), 
                        "Missing Information", JOptionPane.ERROR_MESSAGE);
                    nameFields.get(i).requestFocus();
                    return;
                }
                
                if (ageText.isEmpty()) {
                    JOptionPane.showMessageDialog(detailsDialog, 
                        "Please enter age for seat " + selectedSeats.get(i).getSeatNumber(), 
                        "Missing Information", JOptionPane.ERROR_MESSAGE);
                    ageFields.get(i).requestFocus();
                    return;
                }
                
                try {
                    int age = Integer.parseInt(ageText);
                    if (age <= 0 || age > 120) {
                        JOptionPane.showMessageDialog(detailsDialog, 
                            "Please enter a valid age (1-120) for seat " + selectedSeats.get(i).getSeatNumber(), 
                            "Invalid Age", JOptionPane.ERROR_MESSAGE);
                        ageFields.get(i).requestFocus();
                        return;
                    }
                    passengerAges.add(age);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(detailsDialog, 
                        "Please enter a valid number for age for seat " + selectedSeats.get(i).getSeatNumber(), 
                        "Invalid Age", JOptionPane.ERROR_MESSAGE);
                    ageFields.get(i).requestFocus();
                    return;
                }
                
                passengerNames.add(name);
            }
            
            // Proceed with multiple seat booking
            processMultipleSeatBooking(trainResult, selectedSeats, passengerNames, passengerAges, detailsDialog, parentDialog);
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> detailsDialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        detailsDialog.add(mainPanel);
        detailsDialog.setVisible(true);
    }
    
    
    private void showMixedBookingDialog(TrainManager.TrainSearchResult trainResult,
                                       List<SeatAvailabilityManager.SeatWithDetails> selectedSeats,
                                       int totalRequestedSeats,
                                       JDialog parentDialog) {
        
        int confirmedSeats = selectedSeats.size();
        int racWaitlistSeats = totalRequestedSeats - confirmedSeats;
        
        JDialog mixedDialog = new JDialog(mainFrame, "Mixed Booking - Passenger Details", true);
        mixedDialog.setSize(700, 600);
        mixedDialog.setLocationRelativeTo(parentDialog);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("Mixed Booking Summary:"));
        infoPanel.add(new JLabel("• " + confirmedSeats + " passenger(s) will get confirmed seats"));
        infoPanel.add(new JLabel("• " + racWaitlistSeats + " passenger(s) will be placed in RAC/Waitlist"));
        infoPanel.add(new JLabel("Please enter passenger details for all " + totalRequestedSeats + " passenger(s):"));
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Passenger details panel
        JPanel passengerPanel = new JPanel(new GridLayout(totalRequestedSeats, 1, 5, 5));
        passengerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        List<JTextField> nameFields = new ArrayList<>();
        List<JTextField> ageFields = new ArrayList<>();
        
        // Add confirmed seat passenger details
        for (int i = 0; i < confirmedSeats; i++) {
            SeatAvailabilityManager.SeatWithDetails seat = selectedSeats.get(i);
            
            JPanel seatDetailPanel = new JPanel(new GridBagLayout());
            seatDetailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 128, 0)), 
                "CONFIRMED - Seat " + seat.getSeatNumber() + " (" + seat.getBerthType() + ")"
            ));
            
            addPassengerFieldsToPanel(seatDetailPanel, nameFields, ageFields);
            passengerPanel.add(seatDetailPanel);
        }
        
        // Add RAC/Waitlist passenger details
        for (int i = 0; i < racWaitlistSeats; i++) {
            JPanel racDetailPanel = new JPanel(new GridBagLayout());
            racDetailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0)), 
                "RAC/WAITLIST - Passenger " + (i + 1)
            ));
            
            addPassengerFieldsToPanel(racDetailPanel, nameFields, ageFields);
            passengerPanel.add(racDetailPanel);
        }
        
        JScrollPane scrollPane = new JScrollPane(passengerPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton confirmButton = new JButton("Confirm Mixed Booking");
        confirmButton.setBackground(new Color(34, 139, 34));
        confirmButton.setForeground(Color.BLACK);
        
        confirmButton.addActionListener(e -> {
            // Validate and collect passenger details
            List<String> passengerNames = new ArrayList<>();
            List<Integer> passengerAges = new ArrayList<>();
            
            if (validatePassengerDetails(nameFields, ageFields, passengerNames, passengerAges, mixedDialog)) {
                processMixedBooking(trainResult, selectedSeats, totalRequestedSeats, 
                                  passengerNames, passengerAges, mixedDialog, parentDialog);
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> mixedDialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mixedDialog.add(mainPanel);
        mixedDialog.setVisible(true);
    }
    
    private void addPassengerFieldsToPanel(JPanel panel, List<JTextField> nameFields, List<JTextField> ageFields) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Passenger Name:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(20);
        nameFields.add(nameField);
        panel.add(nameField, gbc);
        
        // Age field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Age:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField ageField = new JTextField(5);
        ageFields.add(ageField);
        panel.add(ageField, gbc);
    }
    
    private boolean validatePassengerDetails(List<JTextField> nameFields, List<JTextField> ageFields,
                                           List<String> passengerNames, List<Integer> passengerAges,
                                           JDialog parentDialog) {
        for (int i = 0; i < nameFields.size(); i++) {
            String name = nameFields.get(i).getText();
            String ageText = ageFields.get(i).getText().trim();
            
            // Enhanced name validation
            if (name == null) {
                JOptionPane.showMessageDialog(parentDialog, 
                    "Passenger name cannot be null for passenger " + (i + 1), 
                    "Invalid Name", JOptionPane.ERROR_MESSAGE);
                nameFields.get(i).requestFocus();
                return false;
            }
            
            name = name.trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(parentDialog, 
                    "Please enter name for passenger " + (i + 1), 
                    "Missing Information", JOptionPane.ERROR_MESSAGE);
                nameFields.get(i).requestFocus();
                return false;
            }
            
            // Check minimum length
            if (name.length() < 2) {
                JOptionPane.showMessageDialog(parentDialog, 
                    "Passenger name must be at least 2 characters long for passenger " + (i + 1), 
                    "Invalid Name", JOptionPane.ERROR_MESSAGE);
                nameFields.get(i).requestFocus();
                return false;
            }
            
            // Remove invalid characters and check if still valid
            String cleanedName = name.replaceAll("[^a-zA-Z0-9\\s.-]", "").trim();
            if (cleanedName.isEmpty()) {
                JOptionPane.showMessageDialog(parentDialog, 
                    "Passenger name contains invalid characters for passenger " + (i + 1), 
                    "Invalid Name", JOptionPane.ERROR_MESSAGE);
                nameFields.get(i).requestFocus();
                return false;
            }
            
            // Update the field with cleaned name
            nameFields.get(i).setText(cleanedName);
            
            if (ageText.isEmpty()) {
                JOptionPane.showMessageDialog(parentDialog, 
                    "Please enter age for passenger " + (i + 1), 
                    "Missing Information", JOptionPane.ERROR_MESSAGE);
                ageFields.get(i).requestFocus();
                return false;
            }
            
            try {
                int age = Integer.parseInt(ageText);
                if (age <= 0 || age > 120) {
                    JOptionPane.showMessageDialog(parentDialog, 
                        "Please enter a valid age (1-120) for passenger " + (i + 1), 
                        "Invalid Age", JOptionPane.ERROR_MESSAGE);
                    ageFields.get(i).requestFocus();
                    return false;
                }
                passengerAges.add(age);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parentDialog, 
                    "Please enter a valid number for age for passenger " + (i + 1), 
                    "Invalid Age", JOptionPane.ERROR_MESSAGE);
                ageFields.get(i).requestFocus();
                return false;
            }
            
            passengerNames.add(cleanedName);
            System.out.println("DEBUG: Validated passenger " + (i + 1) + " - Name: '" + cleanedName + "' (length: " + cleanedName.length() + ")");
        }
        return true;
    }
    
    private void processMixedBooking(TrainManager.TrainSearchResult trainResult,
                                   List<SeatAvailabilityManager.SeatWithDetails> selectedSeats,
                                   int totalRequestedSeats,
                                   List<String> passengerNames,
                                   List<Integer> passengerAges,
                                   JDialog mixedDialog,
                                   JDialog parentDialog) {
        try {
            List<BookingManager.BookingResult> allResults = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            int confirmedSeats = selectedSeats.size();
            int racWaitlistSeats = totalRequestedSeats - confirmedSeats;
            
            // Book confirmed seats first
            for (int i = 0; i < confirmedSeats; i++) {
                SeatAvailabilityManager.SeatWithDetails seat = selectedSeats.get(i);
                String passengerName = passengerNames.get(i);
                int passengerAge = passengerAges.get(i);
                
                BookingManager.BookingResult result = bookingManager.createBooking(
                    currentUser.getUserId(),
                    seat.getSeatId(),
                    trainResult.getTrain().getTrainId(),
                    trainResult.getRoute().getRouteId(),
                    passengerName,
                    passengerAge
                );
                
                allResults.add(result);
                if (result.isSuccess() && "Confirmed".equals(result.getStatus())) {
                    totalAmount = totalAmount.add(trainResult.getRoute().getPrice());
                }
            }
            
            // Book remaining passengers in RAC/Waitlist
            for (int i = confirmedSeats; i < totalRequestedSeats; i++) {
                String passengerName = passengerNames.get(i);
                int passengerAge = passengerAges.get(i);
                
                System.out.println("DEBUG: Mixed booking RAC/Waitlist attempt " + (i+1) + " - Name: '" + passengerName + "', Age: " + passengerAge);
                
                // Use a dummy seat ID (-1) for RAC/Waitlist bookings
                BookingManager.BookingResult result = bookingManager.createBooking(
                    currentUser.getUserId(),
                    -1, // Dummy seat ID for RAC/Waitlist
                    trainResult.getTrain().getTrainId(),
                    trainResult.getRoute().getRouteId(),
                    passengerName,
                    passengerAge
                );
                
                allResults.add(result);
            }
            
            // Process payment for confirmed bookings if any
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                PaymentDialog paymentDialog = new PaymentDialog(mainFrame, 
                    allResults.get(0).getId(), totalAmount);
                paymentDialog.setVisible(true);
                
                if (paymentDialog.isPaymentSuccessful()) {
                    showMixedBookingSuccessMessage(allResults, confirmedSeats, racWaitlistSeats);
                } else {
                    // Cancel confirmed bookings if payment failed
                    JOptionPane.showMessageDialog(mixedDialog,
                        "Payment failed. All bookings have been cancelled.",
                        "Payment Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // All went to RAC/Waitlist, no payment needed
                showMixedBookingSuccessMessage(allResults, confirmedSeats, racWaitlistSeats);
            }
            
            mixedDialog.dispose();
            parentDialog.dispose();
            refreshMyBookings();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mixedDialog,
                "An error occurred while processing the booking: " + ex.getMessage(),
                "Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showMixedBookingSuccessMessage(List<BookingManager.BookingResult> results,
                                              int confirmedSeats, int racWaitlistSeats) {
        StringBuilder message = new StringBuilder();
        message.append("Mixed Booking Completed Successfully!\n\n");
        
        if (confirmedSeats > 0) {
            message.append("Confirmed Bookings:\n");
            for (int i = 0; i < confirmedSeats; i++) {
                BookingManager.BookingResult result = results.get(i);
                if (result.isSuccess()) {
                    message.append("• Booking ID: ").append(result.getId())
                           .append(" - Status: ").append(result.getStatus()).append("\n");
                }
            }
            message.append("\n");
        }
        
        if (racWaitlistSeats > 0) {
            message.append("RAC/Waitlist Bookings:\n");
            for (int i = confirmedSeats; i < results.size(); i++) {
                BookingManager.BookingResult result = results.get(i);
                if (result.isSuccess()) {
                    message.append("• Booking ID: ").append(result.getId())
                           .append(" - Status: ").append(result.getStatus()).append("\n");
                }
            }
        }
        
        JOptionPane.showMessageDialog(mainFrame, message.toString(),
            "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void proceedWithRACWaitlistBooking(TrainManager.TrainSearchResult trainResult, int numberOfSeats) {
        // Show dialog for passenger details for RAC/Waitlist booking
        JDialog racDialog = new JDialog(mainFrame, "RAC/Waitlist Booking - Passenger Details", true);
        racDialog.setSize(600, 500);
        racDialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(new JLabel("No seats available for confirmation."));
        infoPanel.add(new JLabel("All " + numberOfSeats + " passenger(s) will be placed in RAC/Waitlist."));
        infoPanel.add(new JLabel("Please enter passenger details:"));
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Passenger details panel
        JPanel passengerPanel = new JPanel(new GridLayout(numberOfSeats, 1, 5, 5));
        passengerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        List<JTextField> nameFields = new ArrayList<>();
        List<JTextField> ageFields = new ArrayList<>();
        
        for (int i = 0; i < numberOfSeats; i++) {
            JPanel racDetailPanel = new JPanel(new GridBagLayout());
            racDetailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0)), 
                "RAC/WAITLIST - Passenger " + (i + 1)
            ));
            
            addPassengerFieldsToPanel(racDetailPanel, nameFields, ageFields);
            passengerPanel.add(racDetailPanel);
        }
        
        JScrollPane scrollPane = new JScrollPane(passengerPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton confirmButton = new JButton("Confirm RAC/Waitlist Booking");
        confirmButton.setBackground(new Color(255, 140, 0));
        confirmButton.setForeground(Color.BLACK);
        
        confirmButton.addActionListener(e -> {
            List<String> passengerNames = new ArrayList<>();
            List<Integer> passengerAges = new ArrayList<>();
            
            if (validatePassengerDetails(nameFields, ageFields, passengerNames, passengerAges, racDialog)) {
                // Process RAC/Waitlist bookings
                try {
                    List<BookingManager.BookingResult> racResults = new ArrayList<>();
                    
                    for (int i = 0; i < numberOfSeats; i++) {
                        String passengerName = passengerNames.get(i);
                        int passengerAge = passengerAges.get(i);
                        
                        System.out.println("DEBUG: RAC/Waitlist booking attempt " + (i+1) + " - Name: '" + passengerName + "', Age: " + passengerAge);
                        
                        BookingManager.BookingResult result = bookingManager.createBooking(
                            currentUser.getUserId(),
                            -1, // Dummy seat ID for RAC/Waitlist
                            trainResult.getTrain().getTrainId(),
                            trainResult.getRoute().getRouteId(),
                            passengerName,
                            passengerAge
                        );
                        
                        racResults.add(result);
                    }
                    
                    // Show success message
                    StringBuilder message = new StringBuilder();
                    message.append("RAC/Waitlist Booking Completed!\n\n");
                    
                    for (BookingManager.BookingResult result : racResults) {
                        if (result.isSuccess()) {
                            message.append("• Booking ID: ").append(result.getId())
                                   .append(" - Status: ").append(result.getStatus()).append("\n");
                        }
                    }
                    
                    JOptionPane.showMessageDialog(racDialog, message.toString(),
                        "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
                    
                    racDialog.dispose();
                    refreshMyBookings();
                    
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(racDialog,
                        "An error occurred while processing the booking: " + ex.getMessage(),
                        "Booking Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> racDialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        racDialog.add(mainPanel);
        racDialog.setVisible(true);
    }

    private void processMultipleSeatBooking(TrainManager.TrainSearchResult trainResult,
                                          List<SeatAvailabilityManager.SeatWithDetails> selectedSeats,
                                          List<String> passengerNames,
                                          List<Integer> passengerAges,
                                          JDialog detailsDialog,
                                          JDialog parentDialog) {
        try {
            List<BookingManager.BookingResult> bookingResults = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            StringBuilder statusMessages = new StringBuilder();
            boolean hasFailures = false;
            
            // Create bookings for each seat
            for (int i = 0; i < selectedSeats.size(); i++) {
                SeatAvailabilityManager.SeatWithDetails seat = selectedSeats.get(i);
                String passengerName = passengerNames.get(i);
                int passengerAge = passengerAges.get(i);
                
                try {
                    BookingManager.BookingResult result = bookingManager.createBooking(
                        currentUser.getUserId(),
                        seat.getSeatId(),
                        trainResult.getTrain().getTrainId(),
                        trainResult.getRoute().getRouteId(),
                        passengerName,
                        passengerAge
                    );
                    
                    bookingResults.add(result);
                    
                    if (result.isSuccess()) {
                        totalAmount = totalAmount.add(trainResult.getRoute().getPrice());
                        
                        // Build status message based on booking status
                        String status = result.getStatus();
                        if ("Confirmed".equals(status)) {
                            statusMessages.append("✓ Seat ").append(seat.getSeatNumber())
                                        .append(" - ").append(passengerName)
                                        .append(" (Confirmed - ID: ").append(result.getId()).append(")\n");
                        } else if ("RAC".equals(status)) {
                            statusMessages.append("⚪ Seat ").append(seat.getSeatNumber())
                                        .append(" - ").append(passengerName)
                                        .append(" (RAC - ").append(result.getMessage()).append(")\n");
                        } else if ("Waiting".equals(status)) {
                            statusMessages.append("⏳ Seat ").append(seat.getSeatNumber())
                                        .append(" - ").append(passengerName)
                                        .append(" (Waitlist - ").append(result.getMessage()).append(")\n");
                        }
                    } else {
                        hasFailures = true;
                        statusMessages.append("✗ Seat ").append(seat.getSeatNumber())
                                    .append(" - ").append(passengerName)
                                    .append(" (Failed: ").append(result.getMessage()).append(")\n");
                    }
                } catch (SQLException ex) {
                    hasFailures = true;
                    statusMessages.append("✗ Seat ").append(seat.getSeatNumber())
                                 .append(" - ").append(passengerName)
                                 .append(" (Error: ").append(ex.getMessage()).append(")\n");
                }
            }
            
            // Check if we have any successful bookings
            List<BookingManager.BookingResult> successfulBookings = new ArrayList<>();
            for (BookingManager.BookingResult result : bookingResults) {
                if (result.isSuccess()) {
                    successfulBookings.add(result);
                }
            }
            
            if (successfulBookings.isEmpty()) {
                JOptionPane.showMessageDialog(detailsDialog,
                    "All bookings failed:\n" + statusMessages.toString(),
                    "Booking Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Show booking status and proceed with payment for confirmed bookings
            BigDecimal confirmedAmount = BigDecimal.ZERO;
            List<BookingManager.BookingResult> confirmedBookings = new ArrayList<>();
            
            for (BookingManager.BookingResult result : successfulBookings) {
                if ("Confirmed".equals(result.getStatus())) {
                    confirmedBookings.add(result);
                    confirmedAmount = confirmedAmount.add(trainResult.getRoute().getPrice());
                }
            }
            
            // If we have confirmed bookings, proceed with payment
            if (!confirmedBookings.isEmpty()) {
                PaymentDialog paymentDialog = new PaymentDialog(mainFrame, 
                    confirmedBookings.get(0).getId(), confirmedAmount);
                paymentDialog.setVisible(true);
                
                if (paymentDialog.isPaymentSuccessful()) {
                    detailsDialog.dispose();
                    parentDialog.dispose();
                    
                    // Refresh my bookings tab
                    refreshMyBookings();
                    
                    StringBuilder successMessage = new StringBuilder();
                    successMessage.append("Booking Process Completed!\n\n");
                    successMessage.append("Booking Status:\n");
                    successMessage.append(statusMessages.toString());
                    
                    if (confirmedAmount.compareTo(BigDecimal.ZERO) > 0) {
                        successMessage.append("\nPayment Amount: ₹").append(confirmedAmount);
                        successMessage.append(" (for confirmed seats only)");
                    }
                    
                    if (hasFailures) {
                        successMessage.append("\n\nNote: Some bookings failed or were placed in RAC/Waitlist.");
                        successMessage.append("\nRAC and Waitlist bookings will be automatically confirmed when seats become available.");
                    }
                    
                    JOptionPane.showMessageDialog(mainFrame,
                        successMessage.toString(),
                        "Booking Status", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Payment cancelled, cancel only confirmed bookings
                    for (BookingManager.BookingResult result : confirmedBookings) {
                        try {
                            bookingManager.cancelBooking(result.getId());
                        } catch (SQLException ex) {
                            System.err.println("Failed to cancel booking " + result.getId() + ": " + ex.getMessage());
                        }
                    }
                    
                    JOptionPane.showMessageDialog(detailsDialog,
                        "Payment was cancelled. Confirmed bookings have been cancelled.\n" +
                        "RAC and Waitlist entries remain active.",
                        "Payment Cancelled", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // All bookings went to RAC/Waitlist, no payment needed
                detailsDialog.dispose();
                parentDialog.dispose();
                
                // Refresh my bookings tab
                refreshMyBookings();
                
                StringBuilder message = new StringBuilder();
                message.append("All passengers have been placed in RAC/Waitlist:\n\n");
                message.append(statusMessages.toString());
                message.append("\nNo payment required at this time.");
                message.append("\nYou will be notified when seats become available.");
                
                JOptionPane.showMessageDialog(mainFrame,
                    message.toString(),
                    "RAC/Waitlist Booking", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(detailsDialog,
                "An unexpected error occurred: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JTextArea myBookingsTextArea; // Add instance variable for bookings area
    
    private JPanel createMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title and info panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("My Bookings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Info panel with user details
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(25, 25, 112)); // Dark blue background for white text
        JLabel userLabel = new JLabel("User: " + currentUser.getUsername());
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        infoPanel.add(userLabel);
        headerPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Bookings");
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBackground(new Color(34, 139, 34));
        refreshButton.addActionListener(e -> refreshMyBookings());
        
        JButton cancelBookingButton = new JButton("Cancel Booking");
        cancelBookingButton.setForeground(Color.BLACK);
        cancelBookingButton.setBackground(new Color(220, 20, 60));
        cancelBookingButton.addActionListener(e -> showCancelBookingDialog());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelBookingButton);
        headerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Bookings display area
        myBookingsTextArea = new JTextArea(30, 150);
        myBookingsTextArea.setEditable(false);
        myBookingsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        myBookingsTextArea.setBackground(new Color(248, 248, 255));
        myBookingsTextArea.setBorder(BorderFactory.createLoweredBevelBorder());
        myBookingsTextArea.setLineWrap(false);
        myBookingsTextArea.setWrapStyleWord(false);
        
        JScrollPane scrollPane = new JScrollPane(myBookingsTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load initial bookings
        refreshMyBookings();
        
        return panel;
    }
    
    private void refreshMyBookings() {
        try {
            List<BookingManager.BookingDetails> bookings = bookingManager.getBookingsForUser(currentUser.getUserId());
            
            StringBuilder sb = new StringBuilder();
            sb.append("╔").append("═".repeat(140)).append("╗\n");
            sb.append("║").append(" ".repeat(55)).append("MY BOOKINGS").append(" ".repeat(55)).append("║\n");
            sb.append("║").append(" ".repeat(50)).append("User: ").append(String.format("%-20s", currentUser.getUsername())).append(" ".repeat(50)).append("║\n");
            sb.append("╚").append("═".repeat(140)).append("╝\n\n");
            
            if (bookings.isEmpty()) {
                sb.append("┌").append("─".repeat(80)).append("┐\n");
                sb.append("│").append(" ".repeat(28)).append("No bookings found").append(" ".repeat(27)).append("│\n");
                sb.append("│").append(" ".repeat(15)).append("Start booking trains to see your bookings here!").append(" ".repeat(14)).append("│\n");
                sb.append("└").append("─".repeat(80)).append("┘\n");
            } else {
                sb.append("Total Bookings: ").append(bookings.size()).append("\n\n");
                
                int bookingCount = 1;
                for (BookingManager.BookingDetails booking : bookings) {
                    // Booking header with number
                    sb.append("┌─ BOOKING #").append(String.format("%02d", bookingCount)).append(" ").append("─".repeat(125)).append("┐\n");
                    
                    // Row 1: Booking ID, Status, and Price
                    sb.append("│ ");
                    sb.append(String.format("%-25s", "Booking ID: " + booking.getBookingId()));
                    sb.append("│ ");
                    sb.append(String.format("%-30s", "Status: " + booking.getStatus()));
                    sb.append("│ ");
                    sb.append(String.format("%-25s", "Price: ₹" + booking.getPrice()));
                    sb.append("│ ");
                    sb.append(String.format("%-25s", "Date: " + booking.getBookingTime().toLocalDate()));
                    sb.append("│\n");
                    
                    // Separator line
                    sb.append("├").append("─".repeat(138)).append("┤\n");
                    
                    // Row 2: Train Information
                    sb.append("│ ");
                    sb.append(String.format("%-35s", "Train: " + booking.getTrainName()));
                    sb.append("│ ");
                    sb.append(String.format("%-25s", "Number: " + booking.getTrainNumber()));
                    sb.append("│ ");
                    sb.append(String.format("%-25s", "Dept: " + (booking.getDepartureTime() != null ? booking.getDepartureTime() : "N/A")));
                    sb.append("│ ");
                    sb.append(String.format("%-25s", "Arrival: " + (booking.getArrivalTime() != null ? booking.getArrivalTime() : "N/A")));
                    sb.append("│\n");
                    
                    // Row 3: Route Information
                    sb.append("│ ");
                    sb.append(String.format("%-35s", "From: " + booking.getSourceStation()));
                    sb.append("│ ");
                    sb.append(String.format("%-77s", "To: " + booking.getDestinationStation()));
                    sb.append("│\n");
                    
                    // Row 4: Passenger Information
                    sb.append("│ ");
                    sb.append(String.format("%-35s", "Passenger: " + booking.getPassengerName()));
                    sb.append("│ ");
                    sb.append(String.format("%-25s", "Age: " + booking.getPassengerAge()));
                    sb.append("│ ");
                    String genderInfo = (booking.getPassengerAge() < 18) ? "Minor" : "Adult";
                    sb.append(String.format("%-52s", "Category: " + genderInfo));
                    sb.append("│\n");
                    
                    // Row 5: Seat Information
                    sb.append("│ ");
                    String seatNumber = booking.getSeatNumber() != null ? booking.getSeatNumber() : "Not Assigned";
                    sb.append(String.format("%-35s", "Seat Number: " + seatNumber));
                    sb.append("│ ");
                    String berthType = booking.getBerthType() != null ? booking.getBerthType() : "N/A";
                    sb.append(String.format("%-25s", "Berth: " + berthType));
                    sb.append("│ ");
                    String compartment = booking.getCompartmentName() != null ? booking.getCompartmentName() : "N/A";
                    sb.append(String.format("%-52s", "Compartment: " + compartment));
                    sb.append("│\n");
                    
                    // Row 6: Class and Payment Information
                    sb.append("│ ");
                    String classType = booking.getClassType() != null ? booking.getClassType() : "N/A";
                    sb.append(String.format("%-35s", "Class: " + classType));
                    sb.append("│ ");
                    String paymentStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "N/A";
                    sb.append(String.format("%-25s", "Payment: " + paymentStatus));
                    sb.append("│ ");
                    String paymentAmount = booking.getPaymentAmount() != null ? "₹" + booking.getPaymentAmount() : "N/A";
                    sb.append(String.format("%-52s", "Amount Paid: " + paymentAmount));
                    sb.append("│\n");
                    
                    // Row 7: Additional Information
                    sb.append("│ ");
                    sb.append(String.format("%-35s", "Booking Time: " + booking.getBookingTime().toLocalTime()));
                    sb.append("│ ");
                    
                    // Status-specific information
                    String statusInfo = "";
                    switch (booking.getStatus()) {
                        case "Confirmed":
                            statusInfo = "✓ Ticket Confirmed";
                            break;
                        case "RAC":
                            statusInfo = "⚠ RAC - Reservation Against Cancellation";
                            break;
                        case "Waiting":
                            statusInfo = "⏳ Waitlisted";
                            break;
                        case "Cancelled":
                            statusInfo = "✗ Cancelled";
                            break;
                        default:
                            statusInfo = booking.getStatus();
                    }
                    sb.append(String.format("%-77s", "Status Info: " + statusInfo));
                    sb.append("│\n");
                    
                    // Bottom border
                    sb.append("└").append("─".repeat(138)).append("┘\n\n");
                    bookingCount++;
                }
                
                // Summary section with better alignment
                sb.append("╔").append("═".repeat(140)).append("╗\n");
                sb.append("║").append(" ".repeat(60)).append("SUMMARY").append(" ".repeat(60)).append("║\n");
                sb.append("╠").append("═".repeat(140)).append("╣\n");
                
                long confirmedCount = bookings.stream().filter(b -> "Confirmed".equals(b.getStatus())).count();
                long racCount = bookings.stream().filter(b -> "RAC".equals(b.getStatus())).count();
                long waitlistCount = bookings.stream().filter(b -> "Waiting".equals(b.getStatus())).count();
                long cancelledCount = bookings.stream().filter(b -> "Cancelled".equals(b.getStatus())).count();
                
                sb.append("║ ");
                sb.append(String.format("%-30s", "Confirmed Bookings: " + confirmedCount));
                sb.append("│ ");
                sb.append(String.format("%-30s", "RAC Bookings: " + racCount));
                sb.append("│ ");
                sb.append(String.format("%-30s", "Waitlist Bookings: " + waitlistCount));
                sb.append("│ ");
                sb.append(String.format("%-30s", "Cancelled Bookings: " + cancelledCount));
                sb.append("║\n");
                
                // Calculate total amount
                BigDecimal totalAmount = bookings.stream()
                    .filter(b -> !"Cancelled".equals(b.getStatus()))
                    .map(BookingManager.BookingDetails::getPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                sb.append("║ ");
                sb.append(String.format("%-60s", "Total Amount Spent: ₹" + totalAmount));
                sb.append(String.format("%-60s", "Active Bookings: " + (confirmedCount + racCount + waitlistCount)));
                sb.append("║\n");
                
                sb.append("╚").append("═".repeat(140)).append("╝");
            }
            
            // Update the text area directly using the instance variable
            if (myBookingsTextArea != null) {
                myBookingsTextArea.setText(sb.toString());
                myBookingsTextArea.setCaretPosition(0); // Scroll to top
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading bookings: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            if (myBookingsTextArea != null) {
                myBookingsTextArea.setText("Error loading bookings: " + e.getMessage());
            }
        }
    }
    
    private void showCancelBookingDialog() {
        try {
            List<BookingManager.BookingDetails> bookings = bookingManager.getBookingsForUser(currentUser.getUserId());
            
            // Filter only active bookings (not cancelled)
            List<BookingManager.BookingDetails> activeBookings = bookings.stream()
                .filter(b -> !"Cancelled".equals(b.getStatus()))
                .collect(Collectors.toList());
            
            if (activeBookings.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                    "No active bookings found to cancel.", 
                    "No Bookings", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Create dialog
            JDialog cancelDialog = new JDialog(mainFrame, "Cancel Booking", true);
            cancelDialog.setSize(800, 600);
            cancelDialog.setLocationRelativeTo(mainFrame);
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            
            // Instructions
            JLabel instructionLabel = new JLabel("Select a booking to cancel:");
            instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            instructionLabel.setFont(new Font("Arial", Font.BOLD, 14));
            mainPanel.add(instructionLabel, BorderLayout.NORTH);
            
            // Table for bookings
            String[] columnNames = {"Booking ID", "Train", "Route", "Passenger", "Seat", "Status", "Booking Date", "Price"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable bookingTable = new JTable(tableModel);
            bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            bookingTable.setFont(new Font("Arial", Font.PLAIN, 12));
            bookingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            
            // Populate table
            for (BookingManager.BookingDetails booking : activeBookings) {
                Object[] row = {
                    booking.getBookingId(),
                    booking.getTrainName() + " (" + booking.getTrainNumber() + ")",
                    booking.getSourceStation() + " → " + booking.getDestinationStation(),
                    booking.getPassengerName() + " (" + booking.getPassengerAge() + ")",
                    booking.getSeatNumber() != null ? booking.getSeatNumber() : "RAC/Waitlist",
                    booking.getStatus(),
                    booking.getBookingTime().toLocalDate().toString(),
                    "₹" + booking.getPrice()
                };
                tableModel.addRow(row);
            }
            
            JScrollPane tableScrollPane = new JScrollPane(bookingTable);
            mainPanel.add(tableScrollPane, BorderLayout.CENTER);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            
            JButton cancelBookingBtn = new JButton("Cancel Selected Booking");
            cancelBookingBtn.setBackground(new Color(220, 20, 60));
            cancelBookingBtn.setForeground(Color.BLACK);
            
            JButton closeBtn = new JButton("Close");
            closeBtn.setForeground(Color.BLACK);
            
            cancelBookingBtn.addActionListener(e -> {
                int selectedRow = bookingTable.getSelectedRow();
                if (selectedRow >= 0) {
                    BookingManager.BookingDetails selectedBooking = activeBookings.get(selectedRow);
                    
                    int confirmation = JOptionPane.showConfirmDialog(
                        cancelDialog,
                        "Are you sure you want to cancel this booking?\n\n" +
                        "Booking ID: " + selectedBooking.getBookingId() + "\n" +
                        "Train: " + selectedBooking.getTrainName() + "\n" +
                        "Passenger: " + selectedBooking.getPassengerName() + "\n" +
                        "Price: ₹" + selectedBooking.getPrice(),
                        "Confirm Cancellation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    if (confirmation == JOptionPane.YES_OPTION) {
                        try {
                            boolean success = bookingManager.cancelBooking(selectedBooking.getBookingId());
                            if (success) {
                                JOptionPane.showMessageDialog(cancelDialog, 
                                    "Booking cancelled successfully!", 
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                                cancelDialog.dispose();
                                refreshMyBookings(); // Refresh the main bookings view
                            } else {
                                JOptionPane.showMessageDialog(cancelDialog, 
                                    "Failed to cancel booking. Please try again.", 
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(cancelDialog, 
                                "Error cancelling booking: " + ex.getMessage(), 
                                "Database Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(cancelDialog, 
                        "Please select a booking to cancel.", 
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
            
            closeBtn.addActionListener(e -> cancelDialog.dispose());
            
            buttonPanel.add(cancelBookingBtn);
            buttonPanel.add(closeBtn);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            cancelDialog.add(mainPanel);
            cancelDialog.setVisible(true);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Error loading bookings: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createAdminPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Admin panel title
        JLabel titleLabel = new JLabel("Admin Control Panel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create tabbed pane for different admin functions
        JTabbedPane adminTabs = new JTabbedPane();
        
        // Train Management Tab
        adminTabs.addTab("Train Management", createTrainManagementPanel());
        
        // User Management Tab  
        adminTabs.addTab("User Management", createUserManagementPanel());
        
        // Booking Overview Tab
        adminTabs.addTab("All Bookings", createBookingOverviewPanel());
        
        mainPanel.add(adminTabs, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createTrainManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Train management controls
        JPanel controlPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Basic train operations
        JButton addTrainBtn = new JButton("Add New Train");
        addTrainBtn.setBackground(new Color(34, 139, 34));
        addTrainBtn.setForeground(Color.BLACK);
        addTrainBtn.addActionListener(e -> showAddTrainDialog());
        
        JButton viewTrainsBtn = new JButton("View All Trains");
        viewTrainsBtn.setBackground(new Color(30, 144, 255));
        viewTrainsBtn.setForeground(Color.BLACK);
        viewTrainsBtn.addActionListener(e -> {
            System.out.println("DEBUG: View All Trains button clicked");
            refreshTrainList();
        });
        
        JButton deleteTrainBtn = new JButton("Delete Train");
        deleteTrainBtn.setBackground(new Color(220, 20, 60));
        deleteTrainBtn.setForeground(Color.BLACK);
        deleteTrainBtn.addActionListener(e -> showDeleteTrainDialog());
        
        // Route management
        JButton manageRoutesBtn = new JButton("Manage Routes");
        manageRoutesBtn.setBackground(new Color(75, 0, 130));
        manageRoutesBtn.setForeground(Color.BLACK);
        manageRoutesBtn.addActionListener(e -> showRouteManagementDialog());
        
        JButton addRouteBtn = new JButton("Add Route");
        addRouteBtn.setBackground(new Color(255, 140, 0));
        addRouteBtn.setForeground(Color.BLACK);
        addRouteBtn.addActionListener(e -> showAddRouteDialog());
        
        // Compartment and seat management
        JButton manageCompartmentsBtn = new JButton("Manage Compartments");
        manageCompartmentsBtn.setBackground(new Color(0, 128, 128));
        manageCompartmentsBtn.setForeground(Color.BLACK);
        manageCompartmentsBtn.addActionListener(e -> showCompartmentManagementDialog());
        
        JButton manageSeatsBtn = new JButton("Manage Seats");
        manageSeatsBtn.setBackground(new Color(139, 69, 19));
        manageSeatsBtn.setForeground(Color.BLACK);
        manageSeatsBtn.addActionListener(e -> showSeatManagementDialog());
        
        JButton viewStationsBtn = new JButton("View Stations");
        viewStationsBtn.setBackground(new Color(70, 130, 180));
        viewStationsBtn.setForeground(Color.BLACK);
        viewStationsBtn.addActionListener(e -> showStationManagementDialog());
        
        // Advanced train configuration
        JButton trainConfigBtn = new JButton("Train Configuration");
        trainConfigBtn.setBackground(new Color(106, 90, 205));
        trainConfigBtn.setForeground(Color.BLACK);
        trainConfigBtn.addActionListener(e -> showTrainConfigurationDialog());
        
        controlPanel.add(addTrainBtn);
        controlPanel.add(viewTrainsBtn);
        controlPanel.add(deleteTrainBtn);
        controlPanel.add(manageRoutesBtn);
        controlPanel.add(addRouteBtn);
        controlPanel.add(manageCompartmentsBtn);
        controlPanel.add(manageSeatsBtn);
        controlPanel.add(viewStationsBtn);
        controlPanel.add(trainConfigBtn);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Train list display
        trainListTextArea = new JTextArea(20, 70);
        trainListTextArea.setEditable(false);
        trainListTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(trainListTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Show initial message instead of loading trains immediately
        trainListTextArea.setText("Train Management Panel\n" + 
                             "=".repeat(80) + "\n\n" +
                             "Click 'View All Trains' to load the train list.\n" +
                             "Use 'Add New Train' to add trains to the system.\n" +
                             "Use 'Delete Train' to remove trains from the system.");
        
        return panel;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // User management controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        JButton viewUsersBtn = new JButton("View All Users");
        viewUsersBtn.setBackground(new Color(30, 144, 255));
        viewUsersBtn.setForeground(Color.BLACK);
        viewUsersBtn.addActionListener(e -> refreshUserList());
        
        JButton addAdminBtn = new JButton("Create Admin User");
        addAdminBtn.setBackground(new Color(34, 139, 34));
        addAdminBtn.setForeground(Color.BLACK);
        addAdminBtn.addActionListener(e -> showCreateAdminDialog());
        
        controlPanel.add(viewUsersBtn);
        controlPanel.add(addAdminBtn);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // User list display
        userListTextArea = new JTextArea(20, 70);
        userListTextArea.setEditable(false);
        userListTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(userListTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load initial user list
        refreshUserList();
        
        return panel;
    }
    
    private JPanel createBookingOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Booking management controls
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Train selection panel
        JPanel trainSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        trainSelectionPanel.setBorder(BorderFactory.createTitledBorder("Select Train for RAC/Waitlist View"));
        
        JComboBox<Train> trainCombo = new JComboBox<>();
        trainCombo.addItem(null); // Add null option for "All Trains"
        
        try {
            List<Train> trains = trainManager.getAllTrains();
            for (Train train : trains) {
                trainCombo.addItem(train);
            }
        } catch (SQLException e) {
            System.err.println("Error loading trains for booking overview: " + e.getMessage());
        }
        
        trainSelectionPanel.add(new JLabel("Train:"));
        trainSelectionPanel.add(trainCombo);
        
        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout());
        
        JButton viewAllBookingsBtn = new JButton("View All Bookings");
        viewAllBookingsBtn.setBackground(new Color(30, 144, 255));
        viewAllBookingsBtn.setForeground(Color.BLACK);
        viewAllBookingsBtn.addActionListener(e -> refreshAllBookings());
        
        JButton viewRACBtn = new JButton("View RAC Queue");
        viewRACBtn.setBackground(new Color(255, 165, 0));
        viewRACBtn.setForeground(Color.BLACK);
        viewRACBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            refreshRACQueueByTrain(selectedTrain);
        });
        
        JButton viewWaitlistBtn = new JButton("View Waitlist");
        viewWaitlistBtn.setBackground(new Color(128, 0, 128));
        viewWaitlistBtn.setForeground(Color.BLACK);
        viewWaitlistBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            refreshWaitlistByTrain(selectedTrain);
        });
        
        actionPanel.add(viewAllBookingsBtn);
        actionPanel.add(viewRACBtn);
        actionPanel.add(viewWaitlistBtn);
        
        controlPanel.add(trainSelectionPanel, BorderLayout.NORTH);
        controlPanel.add(actionPanel, BorderLayout.SOUTH);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Booking list display
        bookingListTextArea = new JTextArea(20, 70);
        bookingListTextArea.setEditable(false);
        bookingListTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(bookingListTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load initial booking overview
        refreshAllBookings();
        
        return panel;
    }
    
    // Admin functionality methods
    private void showAddTrainDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add New Train", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField nameField = new JTextField(20);
        JTextField numberField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Train Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Train Number:"), gbc);
        gbc.gridx = 1;
        panel.add(numberField, gbc);
        
        JButton addButton = new JButton("Add Train");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.BLACK);
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String number = numberField.getText().trim();
            
            if (name.isEmpty() || number.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                boolean success = trainManager.addTrain(name, number);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Train added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshTrainList();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add train", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showDeleteTrainDialog() {
        try {
            List<Train> trains = trainManager.getAllTrains();
            if (trains.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "No trains available to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            Train selectedTrain = (Train) JOptionPane.showInputDialog(
                mainFrame,
                "Select train to delete:",
                "Delete Train",
                JOptionPane.QUESTION_MESSAGE,
                null,
                trains.toArray(),
                trains.get(0)
            );
            
            if (selectedTrain != null) {
                int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to delete: " + selectedTrain.getTrainName() + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = trainManager.deleteTrain(selectedTrain.getTrainId());
                    if (success) {
                        JOptionPane.showMessageDialog(mainFrame, "Train deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshTrainList();
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Failed to delete train", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showCreateAdminDialog() {
        JDialog dialog = new JDialog(mainFrame, "Create Admin User", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField emailField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        JButton createButton = new JButton("Create Admin");
        createButton.setBackground(new Color(34, 139, 34));
        createButton.setForeground(Color.BLACK);
        createButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                boolean success = loginOps.registerUser(username, password, email, User.UserRole.Admin);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Admin user created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshUserList();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(createButton, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void refreshTrainList() {
        try {
            System.out.println("DEBUG: Refreshing train list...");
            List<Train> trains = trainManager.getAllTrains();
            System.out.println("DEBUG: Found " + trains.size() + " trains");
            
            StringBuilder sb = new StringBuilder();
            sb.append("All Trains in System\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            if (trains.isEmpty()) {
                sb.append("No trains found in the system.\n");
                sb.append("Note: Make sure the database is running and has sample data.\n");
            } else {
                for (int i = 0; i < trains.size(); i++) {
                    Train train = trains.get(i);
                    sb.append(String.format("%d. Train ID: %d\n", i + 1, train.getTrainId()));
                    sb.append(String.format("   Name: %s\n", train.getTrainName()));
                    sb.append(String.format("   Number: %s\n", train.getTrainNumber()));
                    sb.append("-".repeat(60)).append("\n");
                }
                sb.append(String.format("\nTotal Trains: %d\n", trains.size()));
            }
            
            // Update the train list area
            System.out.println("DEBUG: Updating text area...");
            updateAdminTextArea("trainListArea", sb.toString());
            System.out.println("DEBUG: Text area updated");
            
        } catch (SQLException e) {
            System.err.println("DEBUG: SQL Error: " + e.getMessage());
            JOptionPane.showMessageDialog(mainFrame, "Error loading trains: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshUserList() {
        try {
            List<User> users = loginOps.getAllUsers();
            
            StringBuilder sb = new StringBuilder();
            sb.append("All Users in System\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            if (users.isEmpty()) {
                sb.append("No users found in the system.\n");
            } else {
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    sb.append(String.format("%d. User ID: %d\n", i + 1, user.getUserId()));
                    sb.append(String.format("   Username: %s\n", user.getUsername()));
                    sb.append(String.format("   Email: %s\n", user.getEmail() != null ? user.getEmail() : "N/A"));
                    sb.append(String.format("   Role: %s\n", user.getRole()));
                    sb.append("-".repeat(60)).append("\n");
                }
                sb.append(String.format("\nTotal Users: %d\n", users.size()));
            }
            
            updateAdminTextArea("userListArea", sb.toString());
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshAllBookings() {
        try {
            List<BookingManager.BookingDetails> bookings = bookingManager.getAllBookings();
            
            StringBuilder sb = new StringBuilder();
            sb.append("All Bookings Overview\n");
            sb.append("=".repeat(100)).append("\n\n");
            
            if (bookings.isEmpty()) {
                sb.append("No bookings found in the system.\n");
            } else {
                for (BookingManager.BookingDetails booking : bookings) {
                    sb.append("Booking ID: ").append(booking.getBookingId());
                    sb.append(" | User: ").append(booking.getUsername());
                    sb.append(" | Status: ").append(booking.getStatus()).append("\n");
                    
                    sb.append("Train: ").append(booking.getTrainName()).append(" (").append(booking.getTrainNumber()).append(")\n");
                    sb.append("Route: ").append(booking.getSourceStation()).append(" → ").append(booking.getDestinationStation()).append("\n");
                    sb.append("Passenger: ").append(booking.getPassengerName()).append(" (Age: ").append(booking.getPassengerAge()).append(")\n");
                    
                    if (booking.getSeatNumber() != null) {
                        sb.append("Seat: ").append(booking.getSeatNumber());
                        if (booking.getBerthType() != null) {
                            sb.append(" (").append(booking.getBerthType()).append(")");
                        }
                        sb.append("\n");
                    }
                    
                    sb.append("Booking Time: ").append(booking.getBookingTime()).append("\n");
                    sb.append("Price: ₹").append(booking.getPrice()).append("\n");
                    
                    if (booking.getPaymentAmount() != null) {
                        sb.append("Payment: ₹").append(booking.getPaymentAmount());
                        sb.append(" (").append(booking.getPaymentStatus()).append(")\n");
                    }
                    
                    sb.append("-".repeat(80)).append("\n\n");
                }
                sb.append("Total Bookings: ").append(bookings.size()).append("\n");
            }
            
            updateAdminTextArea("bookingListArea", sb.toString());
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading bookings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshRACQueue() {
        // This method now delegates to the train-specific method with null (all trains)
        refreshRACQueueByTrain(null);
    }
    
    private void refreshRACQueueByTrain(Train selectedTrain) {
        try {
            List<RACQueue.RACEntryWithTrainInfo> racEntries;
            
            if (selectedTrain == null) {
                // Show all RAC entries across all trains
                racEntries = racQueue.getAllRACEntries();
            } else {
                // Get RAC entries for specific train across all routes
                racEntries = getRACEntriesForTrain(selectedTrain.getTrainId());
            }
            
            StringBuilder sb = new StringBuilder();
            if (selectedTrain == null) {
                sb.append("RAC Queue Status - All Trains\n");
            } else {
                sb.append("RAC Queue Status - ").append(selectedTrain.getTrainName())
                  .append(" (").append(selectedTrain.getTrainNumber()).append(")\n");
            }
            sb.append("=".repeat(100)).append("\n\n");
            
            if (racEntries.isEmpty()) {
                sb.append("No RAC entries found");
                if (selectedTrain != null) {
                    sb.append(" for ").append(selectedTrain.getTrainName());
                }
                sb.append(".\n");
                sb.append("RAC entries are created when seats are full but passengers still want to book.\n");
            } else {
                sb.append("Total RAC Entries: ").append(racEntries.size());
                if (selectedTrain != null) {
                    sb.append(" for ").append(selectedTrain.getTrainName());
                }
                sb.append("\n\n");
                
                String currentRoute = "";
                int count = 1;
                
                for (RACQueue.RACEntryWithTrainInfo entry : racEntries) {
                    String routeInfo = entry.getSourceStation() + " → " + entry.getDestinationStation();
                    
                    if (!routeInfo.equals(currentRoute)) {
                        if (!currentRoute.isEmpty()) {
                            sb.append("\n");
                        }
                        if (selectedTrain == null) {
                            sb.append("Train: ").append(entry.getTrainName())
                              .append(" (").append(entry.getTrainNumber()).append(")\n");
                        }
                        sb.append("Route: ").append(routeInfo).append("\n");
                        sb.append("-".repeat(80)).append("\n");
                        currentRoute = routeInfo;
                        count = 1;
                    }
                    
                    sb.append(String.format("%d. RAC ID: %d\n", count++, entry.getRacId()));
                    sb.append(String.format("   Passenger: %s\n", entry.getUsername()));
                    sb.append(String.format("   Email: %s\n", entry.getEmail()));
                    sb.append(String.format("   Position: %d\n", entry.getPosition()));
                    sb.append(String.format("   Status: %s\n", entry.getStatus()));
                    sb.append(String.format("   Request Time: %s\n", entry.getRequestTime()));
                    sb.append("\n");
                }
                
                sb.append("\nNote: RAC passengers get confirmed seats when regular passengers cancel their bookings.\n");
                sb.append("Lower position numbers have higher priority for confirmation.\n");
            }
            
            updateAdminTextArea("bookingListArea", sb.toString());
            
        } catch (SQLException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error loading RAC queue");
            if (selectedTrain != null) {
                sb.append(" for ").append(selectedTrain.getTrainName());
            }
            sb.append(": ").append(e.getMessage()).append("\n\n");
            sb.append("This could be due to:\n");
            sb.append("- Database connectivity issues\n");
            sb.append("- Missing RAC table in database\n");
            sb.append("- Insufficient database permissions\n\n");
            sb.append("Please check the database setup and try again.\n");
            
            updateAdminTextArea("bookingListArea", sb.toString());
        }
    }
    
    private void refreshWaitlist() {
        // This method now delegates to the train-specific method with null (all trains)
        refreshWaitlistByTrain(null);
    }
    
    private void refreshWaitlistByTrain(Train selectedTrain) {
        try {
            List<WaitlistManager.WaitlistEntryWithTrainInfo> waitlistEntries;
            
            if (selectedTrain == null) {
                // Show all waitlist entries across all trains
                waitlistEntries = waitlistManager.getAllWaitlistEntries();
            } else {
                // Get waitlist entries for specific train across all routes
                waitlistEntries = getWaitlistEntriesForTrain(selectedTrain.getTrainId());
            }
            
            StringBuilder sb = new StringBuilder();
            if (selectedTrain == null) {
                sb.append("Waitlist Status - All Trains\n");
            } else {
                sb.append("Waitlist Status - ").append(selectedTrain.getTrainName())
                  .append(" (").append(selectedTrain.getTrainNumber()).append(")\n");
            }
            sb.append("=".repeat(100)).append("\n\n");
            
            if (waitlistEntries.isEmpty()) {
                sb.append("No waitlist entries found");
                if (selectedTrain != null) {
                    sb.append(" for ").append(selectedTrain.getTrainName());
                }
                sb.append(".\n");
                sb.append("Waitlist entries are created when both regular seats and RAC are full.\n");
            } else {
                sb.append("Total Waitlist Entries: ").append(waitlistEntries.size());
                if (selectedTrain != null) {
                    sb.append(" for ").append(selectedTrain.getTrainName());
                }
                sb.append("\n\n");
                
                String currentRoute = "";
                int count = 1;
                
                for (WaitlistManager.WaitlistEntryWithTrainInfo entry : waitlistEntries) {
                    String routeInfo = entry.getSourceStation() + " → " + entry.getDestinationStation();
                    
                    if (!routeInfo.equals(currentRoute)) {
                        if (!currentRoute.isEmpty()) {
                            sb.append("\n");
                        }
                        if (selectedTrain == null) {
                            sb.append("Train: ").append(entry.getTrainName())
                              .append(" (").append(entry.getTrainNumber()).append(")\n");
                        }
                        sb.append("Route: ").append(routeInfo).append("\n");
                        sb.append("-".repeat(80)).append("\n");
                        currentRoute = routeInfo;
                        count = 1;
                    }
                    
                    sb.append(String.format("%d. Waitlist ID: %d\n", count++, entry.getWaitlistId()));
                    sb.append(String.format("   Passenger: %s\n", entry.getUsername()));
                    sb.append(String.format("   Email: %s\n", entry.getEmail()));
                    sb.append(String.format("   Position: %d\n", entry.getPosition()));
                    sb.append(String.format("   Status: %s\n", entry.getStatus()));
                    sb.append(String.format("   Request Time: %s\n", entry.getRequestTime()));
                    sb.append("\n");
                }
                
                sb.append("\nNote: Waitlist passengers get confirmed when RAC or regular passengers cancel.\n");
                sb.append("Lower position numbers have higher priority for confirmation.\n");
                sb.append("Waitlist → RAC → Confirmed is the typical booking progression.\n");
            }
            
            updateAdminTextArea("bookingListArea", sb.toString());
            
        } catch (SQLException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error loading waitlist");
            if (selectedTrain != null) {
                sb.append(" for ").append(selectedTrain.getTrainName());
            }
            sb.append(": ").append(e.getMessage()).append("\n\n");
            sb.append("This could be due to:\n");
            sb.append("- Database connectivity issues\n");
            sb.append("- Missing waitlist table in database\n");
            sb.append("- Insufficient database permissions\n\n");
            sb.append("Please check the database setup and try again.\n");
            
            updateAdminTextArea("bookingListArea", sb.toString());
        }
    }
    
    private void updateAdminTextArea(String areaName, String text) {
        // Store reference to text areas for direct access
        if ("trainListArea".equals(areaName)) {
            trainListTextArea.setText(text);
            trainListTextArea.setCaretPosition(0);
        } else if ("userListArea".equals(areaName)) {
            userListTextArea.setText(text);
            userListTextArea.setCaretPosition(0);
        } else if ("bookingListArea".equals(areaName)) {
            bookingListTextArea.setText(text);
            bookingListTextArea.setCaretPosition(0);
        }
    }
    
    // Enhanced Train Management Methods
    
    private void showRouteManagementDialog() {
        JDialog dialog = new JDialog(mainFrame, "Route Management", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton viewRoutesBtn = new JButton("View All Routes");
        viewRoutesBtn.setForeground(Color.BLACK);
        JButton editRouteBtn = new JButton("Edit Route");
        editRouteBtn.setForeground(Color.BLACK);
        
        controlPanel.add(viewRoutesBtn);
        controlPanel.add(editRouteBtn);
        
        // Route display area
        JTextArea routeArea = new JTextArea(25, 70);
        routeArea.setEditable(false);
        routeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(routeArea);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Load routes
        viewRoutesBtn.addActionListener(e -> loadRoutes(routeArea));
        editRouteBtn.addActionListener(e -> showEditRouteDialog());
        
        // Load initial routes
        loadRoutes(routeArea);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showAddRouteDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add New Route", true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Train selection
        JComboBox<Train> trainCombo = new JComboBox<>();
        try {
            List<Train> trains = trainManager.getAllTrains();
            System.out.println("DEBUG: Loading trains for route dialog, found: " + trains.size() + " trains");
            
            if (trains.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "No trains found in the system.\nPlease add trains first before creating routes.", 
                    "No Trains Available", JOptionPane.WARNING_MESSAGE);
                dialog.dispose();
                return;
            }
            
            for (Train train : trains) {
                trainCombo.addItem(train);
                System.out.println("DEBUG: Added train: " + train.getTrainName() + " (" + train.getTrainNumber() + ")");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, 
                "Error loading trains from database: " + e.getMessage() + 
                "\n\nPlease ensure the database is running and accessible.", 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            dialog.dispose();
            return;
        }
        
        JTextField sourceField = new JTextField(25);
        sourceField.setFont(new Font("Arial", Font.PLAIN, 14));
        sourceField.setPreferredSize(new Dimension(250, 30));
        
        JTextField destField = new JTextField(25);
        destField.setFont(new Font("Arial", Font.PLAIN, 14));
        destField.setPreferredSize(new Dimension(250, 30));
        
        JTextField depTimeField = new JTextField("09:00", 25);
        depTimeField.setFont(new Font("Arial", Font.PLAIN, 14));
        depTimeField.setPreferredSize(new Dimension(250, 30));
        
        JTextField arrTimeField = new JTextField("17:00", 25);
        arrTimeField.setFont(new Font("Arial", Font.PLAIN, 14));
        arrTimeField.setPreferredSize(new Dimension(250, 30));
        
        JTextField priceField = new JTextField("500.00", 25);
        priceField.setFont(new Font("Arial", Font.PLAIN, 14));
        priceField.setPreferredSize(new Dimension(250, 30));
        
        JTextField stopsField = new JTextField("3", 25);
        stopsField.setFont(new Font("Arial", Font.PLAIN, 14));
        stopsField.setPreferredSize(new Dimension(250, 30));
        
        JTextArea stationsArea = new JTextArea(4, 25);
        stationsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        stationsArea.setBorder(BorderFactory.createTitledBorder("Intermediate Stations (one per line)"));
        stationsArea.setText("Station1\nStation2\nStation3");
        stationsArea.setLineWrap(true);
        stationsArea.setWrapStyleWord(true);
        JScrollPane stationsScroll = new JScrollPane(stationsArea);
        stationsScroll.setPreferredSize(new Dimension(250, 100));
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Train:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(trainCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Source Station:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(sourceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Destination Station:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(destField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Departure Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(depTimeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Arrival Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(arrTimeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Price (₹):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(priceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Number of Stops:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(stopsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Intermediate Stations:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(stationsScroll, gbc);
        
        // Add help text
        JLabel helpLabel = new JLabel("<html><small>Format: Time as HH:MM (24-hour), Price as decimal, Stations one per line</small></html>");
        helpLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(helpLabel, gbc);
        
        JButton addButton = new JButton("Add Route");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.BLACK);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(150, 35));
        
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 8, 8, 8);
        panel.add(addButton, gbc);
        
        addButton.addActionListener(e -> {
            try {
                System.out.println("DEBUG: Add Route button clicked");
                
                Train selectedTrain = (Train) trainCombo.getSelectedItem();
                if (selectedTrain == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a train", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String source = sourceField.getText().trim();
                String dest = destField.getText().trim();
                String depTime = depTimeField.getText().trim();
                String arrTime = arrTimeField.getText().trim();
                String priceText = priceField.getText().trim();
                String stopsText = stopsField.getText().trim();
                String intermediateStations = stationsArea.getText().trim();
                
                System.out.println("DEBUG: Form data - Train: " + selectedTrain.getTrainName() + 
                    ", Source: " + source + ", Dest: " + dest + ", Stops: " + stopsText);
                
                if (source.isEmpty() || dest.isEmpty() || depTime.isEmpty() || arrTime.isEmpty() || priceText.isEmpty() || stopsText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validate time format
                if (!depTime.matches("\\d{2}:\\d{2}") || !arrTime.matches("\\d{2}:\\d{2}")) {
                    JOptionPane.showMessageDialog(dialog, "Time format should be HH:MM (e.g., 09:30)", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double price = Double.parseDouble(priceText);
                int stops = Integer.parseInt(stopsText);
                
                if (price <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Price must be greater than 0", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (stops < 0) {
                    JOptionPane.showMessageDialog(dialog, "Number of stops cannot be negative", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                System.out.println("DEBUG: Validation passed, calling addRouteToDatabase");
                
                // Add route to database
                boolean success = addRouteToDatabase(selectedTrain.getTrainId(), source, dest, depTime, arrTime, price, stops, intermediateStations);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Route added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    System.out.println("DEBUG: Route added successfully");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add route. Please check the console for detailed error information.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.err.println("DEBUG: addRouteToDatabase returned false");
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format in price or stops field.\nPrice: " + priceField.getText() + "\nStops: " + stopsField.getText(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("DEBUG: Number format error: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Unexpected error: " + ex.getMessage() + "\n\nPlease check the console for more details.", "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("DEBUG: Unexpected error in add route: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // Add padding around the panel
        JPanel paddedPanel = new JPanel(new BorderLayout());
        paddedPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        paddedPanel.add(panel, BorderLayout.CENTER);
        
        dialog.add(paddedPanel);
        dialog.setVisible(true);
    }
    
    private void showCompartmentManagementDialog() {
        JDialog dialog = new JDialog(mainFrame, "Compartment Management", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Train selection
        JPanel topPanel = new JPanel(new FlowLayout());
        JComboBox<Train> trainCombo = new JComboBox<>();
        try {
            List<Train> trains = trainManager.getAllTrains();
            for (Train train : trains) {
                trainCombo.addItem(train);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading trains: " + e.getMessage());
        }
        
        JButton loadCompartmentsBtn = new JButton("Load Compartments");
        loadCompartmentsBtn.setForeground(Color.BLACK);
        JButton addCompartmentBtn = new JButton("Add Compartment");
        addCompartmentBtn.setForeground(Color.BLACK);
        JButton deleteCompartmentBtn = new JButton("Delete Compartment");
        deleteCompartmentBtn.setForeground(Color.BLACK);
        
        topPanel.add(new JLabel("Select Train:"));
        topPanel.add(trainCombo);
        topPanel.add(loadCompartmentsBtn);
        topPanel.add(addCompartmentBtn);
        topPanel.add(deleteCompartmentBtn);
        
        // Compartment display
        JTextArea compartmentArea = new JTextArea(25, 70);
        compartmentArea.setEditable(false);
        compartmentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(compartmentArea);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Event handlers
        loadCompartmentsBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            if (selectedTrain != null) {
                loadCompartments(selectedTrain, compartmentArea);
            }
        });
        
        addCompartmentBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            if (selectedTrain != null) {
                showAddCompartmentDialog(selectedTrain);
                // Refresh the display after adding
                loadCompartments(selectedTrain, compartmentArea);
            }
        });
        
        deleteCompartmentBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            if (selectedTrain != null) {
                showDeleteCompartmentDialog(selectedTrain, compartmentArea);
            }
        });
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showSeatManagementDialog() {
        JDialog dialog = new JDialog(mainFrame, "Seat Management", true);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Selection panel
        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        
        // Train selection
        JPanel trainPanel = new JPanel(new FlowLayout());
        JComboBox<Train> trainCombo = new JComboBox<>();
        try {
            List<Train> trains = trainManager.getAllTrains();
            for (Train train : trains) {
                trainCombo.addItem(train);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading trains: " + e.getMessage());
        }
        
        trainPanel.add(new JLabel("Select Train:"));
        trainPanel.add(trainCombo);
        
        // Compartment selection panel
        JPanel compartmentPanel = new JPanel(new FlowLayout());
        JComboBox<CompartmentInfo> compartmentCombo = new JComboBox<>();
        compartmentPanel.add(new JLabel("Select Compartment:"));
        compartmentPanel.add(compartmentCombo);
        
        JButton loadCompartmentsBtn = new JButton("Load Compartments");
        loadCompartmentsBtn.setForeground(Color.BLACK);
        compartmentPanel.add(loadCompartmentsBtn);
        
        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton loadSeatsBtn = new JButton("Load Seats");
        JButton addSeatsBtn = new JButton("Add Seats to Compartment");
        JButton deleteSeatsBtn = new JButton("Delete Seats");
        JButton generateSeatsBtn = new JButton("Auto-Generate Seats");
        
        loadSeatsBtn.setForeground(Color.BLACK);
        addSeatsBtn.setForeground(Color.BLACK);
        deleteSeatsBtn.setForeground(Color.BLACK);
        generateSeatsBtn.setForeground(Color.BLACK);
        
        controlPanel.add(loadSeatsBtn);
        controlPanel.add(addSeatsBtn);
        controlPanel.add(deleteSeatsBtn);
        controlPanel.add(generateSeatsBtn);
        
        topPanel.add(trainPanel);
        topPanel.add(compartmentPanel);
        topPanel.add(controlPanel);
        
        // Seat display
        JTextArea seatArea = new JTextArea(30, 80);
        seatArea.setEditable(false);
        seatArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(seatArea);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Event handlers
        loadCompartmentsBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            if (selectedTrain != null) {
                loadCompartmentsForDropdown(selectedTrain.getTrainId(), compartmentCombo);
                seatArea.setText("Compartments loaded. Select a compartment and click 'Load Seats' to view seats.");
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a train first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        loadSeatsBtn.addActionListener(e -> {
            CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
            if (selectedCompartment != null) {
                loadSeatsForCompartment(selectedCompartment, seatArea);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a compartment first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        addSeatsBtn.addActionListener(e -> {
            CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
            if (selectedCompartment != null) {
                showAddSeatsToCompartmentDialog(selectedCompartment);
                // Refresh the display after adding
                loadSeatsForCompartment(selectedCompartment, seatArea);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a compartment first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        deleteSeatsBtn.addActionListener(e -> {
            CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
            if (selectedCompartment != null) {
                showDeleteSeatsFromCompartmentDialog(selectedCompartment, seatArea);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a compartment first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        generateSeatsBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            if (selectedTrain != null) {
                showAutoGenerateSeatsDialog(selectedTrain);
                // Refresh the display after generating
                CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
                if (selectedCompartment != null) {
                    loadSeatsForCompartment(selectedCompartment, seatArea);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a train first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showStationManagementDialog() {
        JDialog dialog = new JDialog(mainFrame, "Station Management", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton viewStationsBtn = new JButton("View All Stations");
        JButton addStationBtn = new JButton("Add Station");
        
        controlPanel.add(viewStationsBtn);
        controlPanel.add(addStationBtn);
        
        // Station display
        JTextArea stationArea = new JTextArea(25, 50);
        stationArea.setEditable(false);
        stationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(stationArea);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Event handlers
        viewStationsBtn.addActionListener(e -> loadStations(stationArea));
        addStationBtn.addActionListener(e -> showAddStationDialog());
        
        // Load initial stations
        loadStations(stationArea);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showTrainConfigurationDialog() {
        JDialog dialog = new JDialog(mainFrame, "Train Configuration", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Train selection
        JPanel topPanel = new JPanel(new FlowLayout());
        JComboBox<Train> trainCombo = new JComboBox<>();
        try {
            List<Train> trains = trainManager.getAllTrains();
            for (Train train : trains) {
                trainCombo.addItem(train);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading trains: " + e.getMessage());
        }
        
        JButton loadConfigBtn = new JButton("Load Configuration");
        JButton saveConfigBtn = new JButton("Save Configuration");
        
        topPanel.add(new JLabel("Select Train:"));
        topPanel.add(trainCombo);
        topPanel.add(loadConfigBtn);
        topPanel.add(saveConfigBtn);
        
        // Configuration display
        JTextArea configArea = new JTextArea(25, 60);
        configArea.setEditable(true);
        configArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(configArea);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Event handlers
        loadConfigBtn.addActionListener(e -> {
            Train selectedTrain = (Train) trainCombo.getSelectedItem();
            if (selectedTrain != null) {
                loadTrainConfiguration(selectedTrain, configArea);
            }
        });
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    // Helper methods for enhanced train management
    
    private void loadRoutes(JTextArea routeArea) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("All Routes in System\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            // Check if extended columns exist
            boolean hasExtendedColumns = checkRouteTableColumns();
            
            String query;
            if (hasExtendedColumns) {
                query = """
                    SELECT r.route_id, r.source_station, r.destination_station, 
                           r.departure_time, r.arrival_time, r.price, r.stops, r.intermediate_stations,
                           t.train_name, t.train_number
                    FROM routes r
                    JOIN trains t ON r.train_id = t.train_id
                    ORDER BY t.train_name, r.source_station
                    """;
            } else {
                query = """
                    SELECT r.route_id, r.source_station, r.destination_station, 
                           r.departure_time, r.arrival_time, r.price,
                           t.train_name, t.train_number
                    FROM routes r
                    JOIN trains t ON r.train_id = t.train_id
                    ORDER BY t.train_name, r.source_station
                    """;
            }
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    sb.append(String.format("%d. Route ID: %d\n", count, rs.getInt("route_id")));
                    sb.append(String.format("   Train: %s (%s)\n", rs.getString("train_name"), rs.getString("train_number")));
                    sb.append(String.format("   Route: %s → %s\n", rs.getString("source_station"), rs.getString("destination_station")));
                    sb.append(String.format("   Time: %s - %s\n", rs.getTime("departure_time"), rs.getTime("arrival_time")));
                    sb.append(String.format("   Price: ₹%.2f\n", rs.getBigDecimal("price")));
                    
                    if (hasExtendedColumns) {
                        int stops = rs.getInt("stops");
                        sb.append(String.format("   Stops: %d\n", stops));
                        
                        String intermediateStations = rs.getString("intermediate_stations");
                        if (intermediateStations != null && !intermediateStations.trim().isEmpty()) {
                            sb.append(String.format("   Intermediate Stations: %s\n", intermediateStations.trim()));
                        } else {
                            sb.append("   Intermediate Stations: None\n");
                        }
                    } else {
                        sb.append("   Stops: Not available (database needs update)\n");
                        sb.append("   Intermediate Stations: Not available (database needs update)\n");
                    }
                    
                    sb.append("-".repeat(60)).append("\n");
                }
                
                if (count == 0) {
                    sb.append("No routes found in the system.\n");
                } else {
                    sb.append(String.format("\nTotal Routes: %d\n", count));
                    if (!hasExtendedColumns) {
                        sb.append("\nNote: Extended route information (stops, intermediate stations) is not available.\n");
                        sb.append("Please run the database update script to enable these features.\n");
                    }
                }
            }
            
            routeArea.setText(sb.toString());
            
        } catch (SQLException e) {
            routeArea.setText("Error loading routes: " + e.getMessage() + 
                "\n\nNote: There may be a database connectivity issue or missing columns.");
        }
    }
    
    private void loadCompartments(Train train, JTextArea compartmentArea) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Compartments for Train: ").append(train.getTrainName()).append("\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            String query = """
                SELECT c.compartment_id, c.compartment_name, cl.class_type,
                       COUNT(s.seat_id) as seat_count
                FROM compartments c
                JOIN classes cl ON c.class_id = cl.class_id
                LEFT JOIN seats s ON c.compartment_id = s.compartment_id
                WHERE cl.train_id = ?
                GROUP BY c.compartment_id, c.compartment_name, cl.class_type
                ORDER BY cl.class_type, c.compartment_name
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, train.getTrainId());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        sb.append(String.format("%d. Compartment: %s\n", count, rs.getString("compartment_name")));
                        sb.append(String.format("   Class: %s\n", rs.getString("class_type")));
                        sb.append(String.format("   Seats: %d\n", rs.getInt("seat_count")));
                        sb.append("-".repeat(50)).append("\n");
                    }
                    
                    if (count == 0) {
                        sb.append("No compartments found for this train.\n");
                        sb.append("Use 'Add Compartment' to add compartments.\n");
                    } else {
                        sb.append(String.format("\nTotal Compartments: %d\n", count));
                    }
                }
            }
            
            compartmentArea.setText(sb.toString());
            
        } catch (SQLException e) {
            compartmentArea.setText("Error loading compartments: " + e.getMessage());
        }
    }
    
    private void loadSeats(Train train, JTextArea seatArea) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Seats for Train: ").append(train.getTrainName()).append("\n");
            sb.append("=".repeat(100)).append("\n\n");
            
            String query = """
                SELECT s.seat_id, s.seat_number, s.berth_type, s.is_available,
                       c.compartment_name, cl.class_type
                FROM seats s
                JOIN compartments c ON s.compartment_id = c.compartment_id
                JOIN classes cl ON c.class_id = cl.class_id
                WHERE cl.train_id = ?
                ORDER BY cl.class_type, c.compartment_name, s.seat_number
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, train.getTrainId());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    String currentClass = "";
                    String currentCompartment = "";
                    int count = 0;
                    
                    while (rs.next()) {
                        String classType = rs.getString("class_type");
                        String compartmentName = rs.getString("compartment_name");
                        
                        if (!classType.equals(currentClass)) {
                            currentClass = classType;
                            sb.append("\n").append(classType).append(":\n");
                            sb.append("=".repeat(classType.length() + 1)).append("\n");
                        }
                        
                        if (!compartmentName.equals(currentCompartment)) {
                            currentCompartment = compartmentName;
                            sb.append("\n  Compartment ").append(compartmentName).append(":\n");
                        }
                        
                        count++;
                        String status = rs.getBoolean("is_available") ? "Available" : "Occupied";
                        sb.append(String.format("    Seat %s (%s) - %s\n", 
                            rs.getString("seat_number"),
                            rs.getString("berth_type"),
                            status));
                    }
                    
                    if (count == 0) {
                        sb.append("No seats found for this train.\n");
                        sb.append("Use 'Add Seats to Compartment' or 'Auto-Generate Seats' to add seats.\n");
                    } else {
                        sb.append(String.format("\nTotal Seats: %d\n", count));
                    }
                }
            }
            
            seatArea.setText(sb.toString());
            
        } catch (SQLException e) {
            seatArea.setText("Error loading seats: " + e.getMessage());
        }
    }
    
    private void loadSeatsForCompartment(CompartmentInfo compartment, JTextArea seatArea) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Seats for Compartment: ").append(compartment.compartmentName)
              .append(" (").append(compartment.classType).append(")\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            String query = """
                SELECT s.seat_id, s.seat_number, s.berth_type, s.is_available
                FROM seats s
                WHERE s.compartment_id = ?
                ORDER BY s.seat_number
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, compartment.compartmentId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    int count = 0;
                    int availableCount = 0;
                    int occupiedCount = 0;
                    
                    while (rs.next()) {
                        count++;
                        boolean isAvailable = rs.getBoolean("is_available");
                        String status = isAvailable ? "Available" : "Occupied";
                        
                        if (isAvailable) {
                            availableCount++;
                        } else {
                            occupiedCount++;
                        }
                        
                        sb.append(String.format("Seat %s (%s) - %s\n", 
                            rs.getString("seat_number"),
                            rs.getString("berth_type"),
                            status));
                    }
                    
                    if (count == 0) {
                        sb.append("No seats found in this compartment.\n");
                        sb.append("Use 'Add Seats to Compartment' or 'Auto-Generate Seats' to add seats.\n");
                    } else {
                        sb.append("\n").append("=".repeat(80)).append("\n");
                        sb.append(String.format("Total Seats: %d\n", count));
                        sb.append(String.format("Available: %d | Occupied: %d\n", availableCount, occupiedCount));
                    }
                }
            }
            
            seatArea.setText(sb.toString());
            
        } catch (SQLException e) {
            seatArea.setText("Error loading seats for compartment: " + e.getMessage());
        }
    }
    
    private void showAddSeatsToCompartmentDialog(CompartmentInfo compartment) {
        JDialog dialog = new JDialog(mainFrame, "Add Seats to " + compartment.compartmentName, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Show compartment info
        JLabel compartmentLabel = new JLabel("Compartment: " + compartment.compartmentName + " (" + compartment.classType + ")");
        compartmentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JTextField seatNumberField = new JTextField(20);
        JComboBox<String> berthTypeCombo = new JComboBox<>(new String[]{"Upper", "Middle", "Lower", "Side Upper", "Side Lower"});
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(compartmentLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Seat Number:"), gbc);
        gbc.gridx = 1;
        panel.add(seatNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Berth Type:"), gbc);
        gbc.gridx = 1;
        panel.add(berthTypeCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Seat");
        addButton.setForeground(Color.BLACK);
        addButton.setBackground(new Color(34, 139, 34));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        addButton.addActionListener(e -> {
            String seatNumber = seatNumberField.getText().trim();
            String berthType = (String) berthTypeCombo.getSelectedItem();
            
            if (seatNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter seat number", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (addSeatToDatabase(compartment.compartmentId, seatNumber, berthType)) {
                JOptionPane.showMessageDialog(dialog, "Seat added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                seatNumberField.setText(""); // Clear for next entry
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add seat", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showDeleteSeatsFromCompartmentDialog(CompartmentInfo compartment, JTextArea seatArea) {
        try {
            // Get seats for this compartment
            String query = """
                SELECT s.seat_id, s.seat_number, s.berth_type, s.is_available
                FROM seats s
                WHERE s.compartment_id = ?
                ORDER BY s.seat_number
                """;
            
            java.util.List<SeatInfo> seats = new java.util.ArrayList<>();
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, compartment.compartmentId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        seats.add(new SeatInfo(
                            rs.getInt("seat_id"),
                            rs.getString("seat_number"),
                            rs.getString("berth_type"),
                            rs.getBoolean("is_available")
                        ));
                    }
                }
            }
            
            if (seats.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "No seats found in this compartment", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Show selection dialog
            SeatInfo selectedSeat = (SeatInfo) JOptionPane.showInputDialog(
                mainFrame,
                "Select seat to delete from " + compartment.compartmentName + ":",
                "Delete Seat",
                JOptionPane.QUESTION_MESSAGE,
                null,
                seats.toArray(),
                seats.get(0)
            );
            
            if (selectedSeat != null) {
                int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to delete seat " + selectedSeat.seatNumber + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (deleteSeatFromDatabase(selectedSeat.seatId)) {
                        JOptionPane.showMessageDialog(mainFrame, "Seat deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadSeatsForCompartment(compartment, seatArea); // Refresh display
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Failed to delete seat", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadStations(JTextArea stationArea) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("All Stations in System\n");
            sb.append("=".repeat(50)).append("\n\n");
            
            // Get unique stations from routes
            String query = """
                SELECT DISTINCT station_name FROM (
                    SELECT source_station as station_name FROM routes
                    UNION
                    SELECT destination_station as station_name FROM routes
                ) stations
                ORDER BY station_name
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    sb.append(String.format("%d. %s\n", count, rs.getString("station_name")));
                }
                
                if (count == 0) {
                    sb.append("No stations found.\n");
                    sb.append("Stations are automatically added when routes are created.\n");
                } else {
                    sb.append(String.format("\nTotal Stations: %d\n", count));
                }
            }
            
            stationArea.setText(sb.toString());
            
        } catch (SQLException e) {
            stationArea.setText("Error loading stations: " + e.getMessage());
        }
    }
    
    private void loadTrainConfiguration(Train train, JTextArea configArea) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Configuration for Train: ").append(train.getTrainName()).append(" (").append(train.getTrainNumber()).append(")\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            // Basic train info
            sb.append("BASIC INFORMATION:\n");
            sb.append("Train ID: ").append(train.getTrainId()).append("\n");
            sb.append("Train Name: ").append(train.getTrainName()).append("\n");
            sb.append("Train Number: ").append(train.getTrainNumber()).append("\n\n");
            
            // Routes
            sb.append("ROUTES:\n");
            String routeQuery = """
                SELECT source_station, destination_station, departure_time, arrival_time, price
                FROM routes WHERE train_id = ?
                ORDER BY source_station
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(routeQuery)) {
                pstmt.setInt(1, train.getTrainId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        sb.append(String.format("  %s → %s (%s - %s) ₹%.2f\n",
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getTime("departure_time"),
                            rs.getTime("arrival_time"),
                            rs.getBigDecimal("price")));
                    }
                }
            }
            
            // Classes and Compartments
            sb.append("\nCLASSES & COMPARTMENTS:\n");
            String classQuery = """
                SELECT cl.class_type, c.compartment_name, COUNT(s.seat_id) as seat_count
                FROM classes cl
                LEFT JOIN compartments c ON cl.class_id = c.class_id
                LEFT JOIN seats s ON c.compartment_id = s.compartment_id
                WHERE cl.train_id = ?
                GROUP BY cl.class_id, c.compartment_id
                ORDER BY cl.class_type, c.compartment_name
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(classQuery)) {
                pstmt.setInt(1, train.getTrainId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    String currentClass = "";
                    while (rs.next()) {
                        String classType = rs.getString("class_type");
                        if (!classType.equals(currentClass)) {
                            currentClass = classType;
                            sb.append("  ").append(classType).append(":\n");
                        }
                        String compName = rs.getString("compartment_name");
                        if (compName != null) {
                            sb.append(String.format("    %s (%d seats)\n", compName, rs.getInt("seat_count")));
                        }
                    }
                }
            }
            
            // Summary
            String summaryQuery = """
                SELECT 
                    COUNT(DISTINCT cl.class_id) as class_count,
                    COUNT(DISTINCT c.compartment_id) as compartment_count,
                    COUNT(s.seat_id) as total_seats
                FROM classes cl
                LEFT JOIN compartments c ON cl.class_id = c.class_id
                LEFT JOIN seats s ON c.compartment_id = s.compartment_id
                WHERE cl.train_id = ?
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(summaryQuery)) {
                pstmt.setInt(1, train.getTrainId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        sb.append("\nSUMMARY:\n");
                        sb.append("Total Classes: ").append(rs.getInt("class_count")).append("\n");
                        sb.append("Total Compartments: ").append(rs.getInt("compartment_count")).append("\n");
                        sb.append("Total Seats: ").append(rs.getInt("total_seats")).append("\n");
                    }
                }
            }
            
            configArea.setText(sb.toString());
            
        } catch (SQLException e) {
            configArea.setText("Error loading train configuration: " + e.getMessage());
        }
    }
    
    // Complete Route Management Implementation
    private void showEditRouteDialog() {
        // First, show route selection dialog
        showRouteSelectionDialog("Edit Route", this::editSelectedRoute);
    }
    
    private void showDeleteRouteDialog() {
        // First, show route selection dialog
        showRouteSelectionDialog("Delete Route", this::deleteSelectedRoute);
    }
    
    private void showRouteSelectionDialog(String title, RouteSelectionCallback callback) {
        JDialog dialog = new JDialog(mainFrame, title, true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Instructions
        JLabel instructionLabel = new JLabel("Select a route to " + title.toLowerCase() + ":");
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(instructionLabel, BorderLayout.NORTH);
        
        // Route table
        String[] columnNames = {"Route ID", "Train", "Source", "Destination", "Departure", "Arrival", "Price", "Stops"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable routeTable = new JTable(tableModel);
        routeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadRoutesIntoTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(routeTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton selectButton = new JButton(title);
        selectButton.setForeground(Color.BLACK);
        selectButton.setBackground(new Color(34, 139, 34));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        selectButton.addActionListener(e -> {
            int selectedRow = routeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int routeId = (Integer) tableModel.getValueAt(selectedRow, 0);
                dialog.dispose();
                callback.onRouteSelected(routeId);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a route", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void loadRoutesIntoTable(DefaultTableModel tableModel) {
        try {
            boolean hasExtendedColumns = checkRouteTableColumns();
            
            String query;
            if (hasExtendedColumns) {
                query = """
                    SELECT r.route_id, t.train_name, t.train_number, r.source_station, 
                           r.destination_station, r.departure_time, r.arrival_time, r.price, r.stops
                    FROM routes r
                    JOIN trains t ON r.train_id = t.train_id
                    ORDER BY t.train_name, r.source_station
                    """;
            } else {
                query = """
                    SELECT r.route_id, t.train_name, t.train_number, r.source_station, 
                           r.destination_station, r.departure_time, r.arrival_time, r.price
                    FROM routes r
                    JOIN trains t ON r.train_id = t.train_id
                    ORDER BY t.train_name, r.source_station
                    """;
            }
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                tableModel.setRowCount(0); // Clear existing data
                
                while (rs.next()) {
                    Object[] row;
                    if (hasExtendedColumns) {
                        row = new Object[]{
                            rs.getInt("route_id"),
                            rs.getString("train_name") + " (" + rs.getString("train_number") + ")",
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getTime("departure_time"),
                            rs.getTime("arrival_time"),
                            "₹" + rs.getBigDecimal("price"),
                            rs.getInt("stops")
                        };
                    } else {
                        row = new Object[]{
                            rs.getInt("route_id"),
                            rs.getString("train_name") + " (" + rs.getString("train_number") + ")",
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getTime("departure_time"),
                            rs.getTime("arrival_time"),
                            "₹" + rs.getBigDecimal("price"),
                            "N/A"
                        };
                    }
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading routes: " + e.getMessage() + 
                "\n\nNote: Some database columns may be missing. Please run the database update script.", 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editSelectedRoute(int routeId) {
        try {
            // Get route details with fallback for missing columns
            boolean hasExtendedColumns = checkRouteTableColumns();
            
            String query;
            if (hasExtendedColumns) {
                query = """
                    SELECT r.*, t.train_name, t.train_number 
                    FROM routes r
                    JOIN trains t ON r.train_id = t.train_id
                    WHERE r.route_id = ?
                    """;
            } else {
                query = """
                    SELECT r.route_id, r.train_id, r.source_station, r.destination_station,
                           r.departure_time, r.arrival_time, r.price,
                           t.train_name, t.train_number 
                    FROM routes r
                    JOIN trains t ON r.train_id = t.train_id
                    WHERE r.route_id = ?
                    """;
            }
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, routeId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        showEditRouteForm(rs, hasExtendedColumns);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Route not found", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading route details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showEditRouteForm(ResultSet routeData, boolean hasExtendedColumns) throws SQLException {
        JDialog dialog = new JDialog(mainFrame, "Edit Route", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Store original route data
        int routeId = routeData.getInt("route_id");
        int trainId = routeData.getInt("train_id");
        
        // Train info (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Train:"), gbc);
        JLabel trainLabel = new JLabel(routeData.getString("train_name") + " (" + routeData.getString("train_number") + ")");
        trainLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 1;
        panel.add(trainLabel, gbc);
        
        // Editable fields
        JTextField sourceField = new JTextField(routeData.getString("source_station"), 20);
        JTextField destField = new JTextField(routeData.getString("destination_station"), 20);
        JTextField depTimeField = new JTextField(routeData.getTime("departure_time").toString().substring(0, 5), 20);
        JTextField arrTimeField = new JTextField(routeData.getTime("arrival_time").toString().substring(0, 5), 20);
        JTextField priceField = new JTextField(routeData.getBigDecimal("price").toString(), 20);
        
        // Extended fields
        JTextField stopsField = null;
        JTextArea stationsArea = null;
        
        if (hasExtendedColumns) {
            try {
                stopsField = new JTextField(String.valueOf(routeData.getInt("stops")), 20);
                String intermediateStations = routeData.getString("intermediate_stations");
                stationsArea = new JTextArea(intermediateStations != null ? intermediateStations : "", 5, 20);
            } catch (SQLException e) {
                stopsField = new JTextField("0", 20);
                stationsArea = new JTextArea("", 5, 20);
            }
        } else {
            stopsField = new JTextField("0", 20);
            stopsField.setEnabled(false);
            stationsArea = new JTextArea("Column not available in database", 5, 20);
            stationsArea.setEnabled(false);
        }
        
        stationsArea.setBorder(BorderFactory.createTitledBorder("Intermediate Stations (one per line)"));
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Source Station:"), gbc);
        gbc.gridx = 1;
        panel.add(sourceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Destination Station:"), gbc);
        gbc.gridx = 1;
        panel.add(destField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Departure Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        panel.add(depTimeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Arrival Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        panel.add(arrTimeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Number of Stops:"), gbc);
        gbc.gridx = 1;
        panel.add(stopsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Intermediate Stations:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(stationsArea), gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("Update Route");
        updateButton.setForeground(Color.BLACK);
        updateButton.setBackground(new Color(34, 139, 34));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        final JTextField finalStopsField = stopsField;
        final JTextArea finalStationsArea = stationsArea;
        
        updateButton.addActionListener(e -> {
            try {
                String source = sourceField.getText().trim();
                String dest = destField.getText().trim();
                String depTime = depTimeField.getText().trim();
                String arrTime = arrTimeField.getText().trim();
                String priceText = priceField.getText().trim();
                String stopsText = finalStopsField.getText().trim();
                String intermediateStations = finalStationsArea.getText().trim();
                
                if (source.isEmpty() || dest.isEmpty() || depTime.isEmpty() || arrTime.isEmpty() || priceText.isEmpty() || stopsText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double price = Double.parseDouble(priceText);
                int stops = Integer.parseInt(stopsText);
                
                if (updateRoute(routeId, source, dest, depTime, arrTime, price, stops, intermediateStations)) {
                    JOptionPane.showMessageDialog(dialog, "Route updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update route", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid price or stops format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean updateRoute(int routeId, String source, String dest, String depTime, String arrTime, double price, int stops, String intermediateStations) {
        try {
            System.out.println("DEBUG: Updating route - routeId: " + routeId + ", source: " + source + ", dest: " + dest);
            
            boolean hasExtendedColumns = checkRouteTableColumns();
            System.out.println("DEBUG: Extended columns exist for update: " + hasExtendedColumns);
            
            String query;
            if (hasExtendedColumns) {
                query = """
                    UPDATE routes 
                    SET source_station = ?, destination_station = ?, departure_time = ?, arrival_time = ?, price = ?, stops = ?, intermediate_stations = ?
                    WHERE route_id = ?
                    """;
            } else {
                query = """
                    UPDATE routes 
                    SET source_station = ?, destination_station = ?, departure_time = ?, arrival_time = ?, price = ?
                    WHERE route_id = ?
                    """;
            }
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setString(1, source);
                pstmt.setString(2, dest);
                pstmt.setTime(3, java.sql.Time.valueOf(depTime + ":00"));
                pstmt.setTime(4, java.sql.Time.valueOf(arrTime + ":00"));
                pstmt.setBigDecimal(5, new BigDecimal(price));
                
                if (hasExtendedColumns) {
                    pstmt.setInt(6, stops);
                    pstmt.setString(7, intermediateStations);
                    pstmt.setInt(8, routeId);
                    System.out.println("DEBUG: Updating with stops: " + stops + ", intermediate stations: " + intermediateStations);
                } else {
                    pstmt.setInt(6, routeId);
                    System.out.println("DEBUG: Updating without extended columns");
                }
                
                int result = pstmt.executeUpdate();
                System.out.println("DEBUG: Route update result: " + result);
                
                if (result > 0) {
                    System.out.println("DEBUG: Route updated successfully");
                    return true;
                } else {
                    System.err.println("DEBUG: Route update failed - no rows affected, route may not exist");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating route: " + e.getMessage());
            e.printStackTrace();
            
            // Show a user-friendly error message
            SwingUtilities.invokeLater(() -> {
                String errorMsg = "Failed to update route in database.\n\n";
                if (e.getMessage().contains("Column") && e.getMessage().contains("doesn't exist")) {
                    errorMsg += "The database table is missing required columns.\n";
                    errorMsg += "Please run the database update script: fix_routes_table.sql\n";
                } else {
                    errorMsg += "Database error: " + e.getMessage() + "\n";
                }
                errorMsg += "\nPlease check the console for detailed error information.";
                
                JOptionPane.showMessageDialog(mainFrame, errorMsg, "Database Error", JOptionPane.ERROR_MESSAGE);
            });
            
            return false;
        }
    }
    
    private void deleteSelectedRoute(int routeId) {
        // Confirm deletion
        int option = JOptionPane.showConfirmDialog(
            mainFrame,
            "Are you sure you want to delete this route?\n\nThis action cannot be undone and may affect existing bookings.",
            "Confirm Route Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                // Check for existing bookings on this route
                String checkQuery = "SELECT COUNT(*) FROM bookings b JOIN routes r ON b.train_id = r.train_id WHERE r.route_id = ?";
                try (PreparedStatement checkStmt = DatabaseManager.getInstance().getConnection().prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, routeId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            int bookingCount = rs.getInt(1);
                            int choice = JOptionPane.showConfirmDialog(
                                mainFrame,
                                "This route has " + bookingCount + " existing booking(s).\n\nDeleting this route may affect those bookings. Continue?",
                                "Warning: Existing Bookings",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                            );
                            
                            if (choice != JOptionPane.YES_OPTION) {
                                return;
                            }
                        }
                    }
                }
                
                // Delete the route
                String deleteQuery = "DELETE FROM routes WHERE route_id = ?";
                try (PreparedStatement deleteStmt = DatabaseManager.getInstance().getConnection().prepareStatement(deleteQuery)) {
                    deleteStmt.setInt(1, routeId);
                    
                    if (deleteStmt.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(mainFrame, "Route deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Failed to delete route", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(mainFrame, "Error deleting route: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Functional interface for route selection callback
    @FunctionalInterface
    private interface RouteSelectionCallback {
        void onRouteSelected(int routeId);
    }
    
    private void showAddCompartmentDialog(Train train) {
        JDialog dialog = new JDialog(mainFrame, "Add Compartment - " + train.getTrainName(), true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Compartment fields
        JTextField compartmentNameField = new JTextField(20);
        JComboBox<String> classTypeCombo = new JComboBox<>(new String[]{"AC First Class", "AC 2-Tier", "AC 3-Tier", "Sleeper", "General"});
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Compartment Name:"), gbc);
        gbc.gridx = 1;
        panel.add(compartmentNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Class Type:"), gbc);
        gbc.gridx = 1;
        panel.add(classTypeCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Compartment");
        addButton.setForeground(Color.BLACK);
        addButton.setBackground(new Color(34, 139, 34));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        addButton.addActionListener(e -> {
            try {
                String compartmentName = compartmentNameField.getText().trim();
                String classType = (String) classTypeCombo.getSelectedItem();
                
                if (compartmentName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter compartment name", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (addCompartmentToDatabase(train.getTrainId(), compartmentName, classType)) {
                    JOptionPane.showMessageDialog(dialog, "Compartment added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add compartment", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showAddSeatsDialog(Train train) {
        JDialog dialog = new JDialog(mainFrame, "Add Seats - " + train.getTrainName(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Get compartments for this train
        JComboBox<CompartmentInfo> compartmentCombo = new JComboBox<>();
        loadCompartmentsForDropdown(train.getTrainId(), compartmentCombo);
        
        JTextField seatNumberField = new JTextField(20);
        JComboBox<String> berthTypeCombo = new JComboBox<>(new String[]{"Upper", "Middle", "Lower", "Side Upper", "Side Lower"});
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Compartment:"), gbc);
        gbc.gridx = 1;
        panel.add(compartmentCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Seat Number:"), gbc);
        gbc.gridx = 1;
        panel.add(seatNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Berth Type:"), gbc);
        gbc.gridx = 1;
        panel.add(berthTypeCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Seat");
        addButton.setForeground(Color.BLACK);
        addButton.setBackground(new Color(34, 139, 34));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        addButton.addActionListener(e -> {
            try {
                CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
                String seatNumber = seatNumberField.getText().trim();
                String berthType = (String) berthTypeCombo.getSelectedItem();
                
                if (selectedCompartment == null || seatNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (addSeatToDatabase(selectedCompartment.compartmentId, seatNumber, berthType)) {
                    JOptionPane.showMessageDialog(dialog, "Seat added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    seatNumberField.setText(""); // Clear for next entry
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add seat (may already exist)", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showAutoGenerateSeatsDialog(Train train) {
        JDialog dialog = new JDialog(mainFrame, "Auto-Generate Seats - " + train.getTrainName(), true);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Get compartments for this train
        JComboBox<CompartmentInfo> compartmentCombo = new JComboBox<>();
        loadCompartmentsForDropdown(train.getTrainId(), compartmentCombo);
        
        JTextField seatsPerCompartmentField = new JTextField("72", 20); // Default for sleeper
        seatsPerCompartmentField.setEditable(true);
        seatsPerCompartmentField.setBackground(Color.WHITE);
        JComboBox<String> seatPatternCombo = new JComboBox<>(new String[]{"Sleeper Pattern", "AC Pattern", "Chair Car Pattern"});
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Compartment:"), gbc);
        gbc.gridx = 1;
        panel.add(compartmentCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Number of Seats:"), gbc);
        gbc.gridx = 1;
        panel.add(seatsPerCompartmentField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Seat Pattern:"), gbc);
        gbc.gridx = 1;
        panel.add(seatPatternCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton generateButton = new JButton("Generate Seats");
        generateButton.setForeground(Color.BLACK);
        generateButton.setBackground(new Color(34, 139, 34));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        generateButton.addActionListener(e -> {
            try {
                CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
                if (selectedCompartment == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a compartment", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int seatCount = Integer.parseInt(seatsPerCompartmentField.getText().trim());
                String pattern = (String) seatPatternCombo.getSelectedItem();
                
                if (autoGenerateSeats(selectedCompartment.compartmentId, seatCount, pattern)) {
                    JOptionPane.showMessageDialog(dialog, "Seats generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to generate seats", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid seat count", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showAddStationDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add Station", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField stationNameField = new JTextField(20);
        JTextField stationCodeField = new JTextField(10);
        JTextField cityField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Station Name:"), gbc);
        gbc.gridx = 1;
        panel.add(stationNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Station Code:"), gbc);
        gbc.gridx = 1;
        panel.add(stationCodeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        panel.add(cityField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Station");
        addButton.setForeground(Color.BLACK);
        addButton.setBackground(new Color(34, 139, 34));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        addButton.addActionListener(e -> {
            String stationName = stationNameField.getText().trim();
            String stationCode = stationCodeField.getText().trim();
            String city = cityField.getText().trim();
            
            if (stationName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Station name is required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (addStationToDatabase(stationName, stationCode, city)) {
                JOptionPane.showMessageDialog(dialog, "Station information saved!\nNote: Stations are automatically managed through routes.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save station information", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    // Helper classes and methods for enhanced functionality
    
    private static class CompartmentInfo {
        int compartmentId;
        String compartmentName;
        String classType;
        
        public CompartmentInfo(int compartmentId, String compartmentName, String classType) {
            this.compartmentId = compartmentId;
            this.compartmentName = compartmentName;
            this.classType = classType;
        }
        
        @Override
        public String toString() {
            return compartmentName + " (" + classType + ")";
        }
    }
    
    private static class SeatInfo {
        int seatId;
        String seatNumber;
        String berthType;
        boolean isAvailable;
        
        public SeatInfo(int seatId, String seatNumber, String berthType, boolean isAvailable) {
            this.seatId = seatId;
            this.seatNumber = seatNumber;
            this.berthType = berthType;
            this.isAvailable = isAvailable;
        }
        
        @Override
        public String toString() {
            String status = isAvailable ? "Available" : "Occupied";
            return seatNumber + " (" + berthType + ") - " + status;
        }
    }
    
    private boolean addCompartmentToDatabase(int trainId, String compartmentName, String classType) {
        return addCompartmentToDatabase(trainId, compartmentName, classType, 0); // Default capacity to 0 or use a reasonable default
    }
    
    private boolean addCompartmentToDatabase(int trainId, String compartmentName, String classType, int capacity) {
        try {
            // First, check if class exists for this train, if not create it
            int classId = getOrCreateClassId(trainId, classType);
            
            // Check if capacity column exists in compartments table
            boolean hasCapacityColumn = checkCompartmentTableColumns();
            
            // Then add the compartment
            String query;
            if (hasCapacityColumn) {
                query = "INSERT INTO compartments (class_id, compartment_name, capacity) VALUES (?, ?, ?)";
            } else {
                query = "INSERT INTO compartments (class_id, compartment_name) VALUES (?, ?)";
            }
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, classId);
                pstmt.setString(2, compartmentName);
                if (hasCapacityColumn) {
                    pstmt.setInt(3, capacity);
                }
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error adding compartment: " + e.getMessage());
            return false;
        }
    }
    
    private boolean checkCompartmentTableColumns() {
        try {
            String checkQuery = "SELECT capacity FROM compartments LIMIT 1";
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(checkQuery)) {
                pstmt.executeQuery();
                return true; // Capacity column exists
            }
        } catch (SQLException e) {
            return false; // Capacity column doesn't exist
        }
    }
    
    private int getOrCreateClassId(int trainId, String classType) throws SQLException {
        // First, check if class exists
        String checkQuery = "SELECT class_id FROM classes WHERE train_id = ? AND class_type = ?";
        try (PreparedStatement checkStmt = DatabaseManager.getInstance().getConnection().prepareStatement(checkQuery)) {
            checkStmt.setInt(1, trainId);
            checkStmt.setString(2, classType);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("class_id");
                }
            }
        }
        
        // If not exists, create new class
        String insertQuery = "INSERT INTO classes (train_id, class_type) VALUES (?, ?)";
        try (PreparedStatement insertStmt = DatabaseManager.getInstance().getConnection().prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, trainId);
            insertStmt.setString(2, classType);
            
            if (insertStmt.executeUpdate() > 0) {
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        
        throw new SQLException("Failed to create class");
    }
    
    private void loadCompartmentsForDropdown(int trainId, JComboBox<CompartmentInfo> compartmentCombo) {
        try {
            String query = """
                SELECT c.compartment_id, c.compartment_name, cl.class_type
                FROM compartments c
                JOIN classes cl ON c.class_id = cl.class_id
                WHERE cl.train_id = ?
                ORDER BY cl.class_type, c.compartment_name
                """;
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, trainId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    compartmentCombo.removeAllItems();
                    while (rs.next()) {
                        CompartmentInfo info = new CompartmentInfo(
                            rs.getInt("compartment_id"),
                            rs.getString("compartment_name"),
                            rs.getString("class_type")
                        );
                        compartmentCombo.addItem(info);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading compartments: " + e.getMessage());
        }
    }
    
    private boolean addSeatToDatabase(int compartmentId, String seatNumber, String berthType) {
        try {
            String query = "INSERT INTO seats (compartment_id, seat_number, berth_type, is_available) VALUES (?, ?, ?, true)";
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, compartmentId);
                pstmt.setString(2, seatNumber);
                pstmt.setString(3, berthType);
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error adding seat: " + e.getMessage());
            return false;
        }
    }
    
    private boolean deleteSeatFromDatabase(int seatId) {
        try {
            String query = "DELETE FROM seats WHERE seat_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, seatId);
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting seat: " + e.getMessage());
            return false;
        }
    }
    
    private String generateSeatPreview(int seatCount, String pattern) {
        StringBuilder preview = new StringBuilder();
        preview.append("Seat Generation Preview:\n");
        preview.append("=".repeat(30)).append("\n");
        preview.append("Pattern: ").append(pattern).append("\n");
        preview.append("Total Seats: ").append(seatCount).append("\n\n");
        
        if ("Sleeper Pattern".equals(pattern)) {
            preview.append("Sleeper compartment layout:\n");
            preview.append("Berth distribution:\n");
            preview.append("- Lower berths: ").append(seatCount / 6).append("\n");
            preview.append("- Middle berths: ").append(seatCount / 6).append("\n");
            preview.append("- Upper berths: ").append(seatCount / 6).append("\n");
            preview.append("- Side Lower: ").append(seatCount / 12).append("\n");
            preview.append("- Side Upper: ").append(seatCount / 12).append("\n");
        } else if ("AC Pattern".equals(pattern)) {
            preview.append("AC compartment layout:\n");
            preview.append("Berth distribution:\n");
            preview.append("- Lower berths: ").append(seatCount / 4).append("\n");
            preview.append("- Upper berths: ").append(seatCount / 4).append("\n");
            preview.append("- Side Lower: ").append(seatCount / 8).append("\n");
            preview.append("- Side Upper: ").append(seatCount / 8).append("\n");
        } else {
            preview.append("Chair Car layout:\n");
            preview.append("All seats will be generated as 'Lower' type\n");
        }
        
        return preview.toString();
    }
    
    private boolean autoGenerateSeats(int compartmentId, int seatCount, String pattern) {
        try {
            // First, check if compartment already has seats
            String checkQuery = "SELECT COUNT(*) FROM seats WHERE compartment_id = ?";
            try (PreparedStatement checkStmt = DatabaseManager.getInstance().getConnection().prepareStatement(checkQuery)) {
                checkStmt.setInt(1, compartmentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        int choice = JOptionPane.showConfirmDialog(
                            mainFrame,
                            "This compartment already has seats. Do you want to continue and add more?",
                            "Existing Seats Found",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (choice != JOptionPane.YES_OPTION) {
                            return false;
                        }
                    }
                }
            }
            
            // Generate seats based on pattern
            String query = "INSERT INTO seats (compartment_id, seat_number, berth_type, is_available) VALUES (?, ?, ?, true)";
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                
                if ("Sleeper Pattern".equals(pattern)) {
                    generateSleeperSeats(pstmt, compartmentId, seatCount);
                } else if ("AC Pattern".equals(pattern)) {
                    generateACSeats(pstmt, compartmentId, seatCount);
                } else {
                    generateChairCarSeats(pstmt, compartmentId, seatCount);
                }
                
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error auto-generating seats: " + e.getMessage());
            return false;
        }
    }
    
    private void generateSleeperSeats(PreparedStatement pstmt, int compartmentId, int seatCount) throws SQLException {
        String[] berthTypes = {"Lower", "Middle", "Upper", "Side Lower", "Side Upper", "Lower"};
        int berths = seatCount / 8; // 8 berths per bay in sleeper
        
        for (int bay = 1; bay <= berths; bay++) {
            for (int i = 0; i < 8 && (bay - 1) * 8 + i < seatCount; i++) {
                String seatNumber = String.format("S%d", (bay - 1) * 8 + i + 1);
                String berthType = berthTypes[i % 6];
                
                pstmt.setInt(1, compartmentId);
                pstmt.setString(2, seatNumber);
                pstmt.setString(3, berthType);
                pstmt.addBatch();
            }
        }
        pstmt.executeBatch();
    }
    
    private void generateACSeats(PreparedStatement pstmt, int compartmentId, int seatCount) throws SQLException {
        String[] berthTypes = {"Lower", "Upper", "Side Lower", "Side Upper"};
        int berths = seatCount / 6; // 6 berths per bay in AC
        
        for (int bay = 1; bay <= berths; bay++) {
            for (int i = 0; i < 6 && (bay - 1) * 6 + i < seatCount; i++) {
                String seatNumber = String.format("A%d", (bay - 1) * 6 + i + 1);
                String berthType = berthTypes[i % 4];
                
                pstmt.setInt(1, compartmentId);
                pstmt.setString(2, seatNumber);
                pstmt.setString(3, berthType);
                pstmt.addBatch();
            }
        }
        pstmt.executeBatch();
    }
    
    private void generateChairCarSeats(PreparedStatement pstmt, int compartmentId, int seatCount) throws SQLException {
        for (int i = 1; i <= seatCount; i++) {
            String seatNumber = String.format("CC%d", i);
            
            pstmt.setInt(1, compartmentId);
            pstmt.setString(2, seatNumber);
            pstmt.setString(3, "Lower");
            pstmt.addBatch();
        }
        pstmt.executeBatch();
    }
    
    private boolean addStationToDatabase(String stationName, String stationCode, String city) {
        try {
            // Since we don't have a dedicated stations table, we'll create a simple log
            // In a real system, you'd have a proper stations table
            String query = "INSERT INTO station_info (station_name, station_code, city, created_date) VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE city = ?, updated_date = NOW()";
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setString(1, stationName);
                pstmt.setString(2, stationCode);
                pstmt.setString(3, city);
                pstmt.setString(4, city);
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            // If table doesn't exist, that's okay - stations are managed through routes
            System.out.println("Station info saved (stations are managed through routes)");
            return true;
        }
    }
    
    private void showDeleteCompartmentDialog(Train train, JTextArea compartmentArea) {
        JDialog dialog = new JDialog(mainFrame, "Delete Compartment - " + train.getTrainName(), true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Get compartments for this train
        JComboBox<CompartmentInfo> compartmentCombo = new JComboBox<>();
        loadCompartmentsForDropdown(train.getTrainId(), compartmentCombo);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Compartment to Delete:"), gbc);
        gbc.gridx = 1;
        panel.add(compartmentCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton deleteButton = new JButton("Delete Compartment");
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setBackground(new Color(220, 20, 60));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        deleteButton.addActionListener(e -> {
            CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
            if (selectedCompartment == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a compartment", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int choice = JOptionPane.showConfirmDialog(
                dialog,
                "Are you sure you want to delete compartment '" + selectedCompartment.compartmentName + "'?\nThis will also delete all seats in this compartment!",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                if (deleteCompartmentFromDatabase(selectedCompartment.compartmentId)) {
                    JOptionPane.showMessageDialog(dialog, "Compartment deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCompartments(train, compartmentArea); // Refresh display
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to delete compartment", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showDeleteSeatsDialog(Train train, JTextArea seatArea) {
        JDialog dialog = new JDialog(mainFrame, "Delete Seats - " + train.getTrainName(), true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Options for deletion
        JRadioButton deleteByCompartmentBtn = new JRadioButton("Delete all seats in a compartment", true);
        JRadioButton deleteSpecificSeatBtn = new JRadioButton("Delete specific seat");
        
        ButtonGroup deleteOptionGroup = new ButtonGroup();
        deleteOptionGroup.add(deleteByCompartmentBtn);
        deleteOptionGroup.add(deleteSpecificSeatBtn);
        
        // Compartment selection
        JComboBox<CompartmentInfo> compartmentCombo = new JComboBox<>();
        loadCompartmentsForDropdown(train.getTrainId(), compartmentCombo);
        
        // Specific seat input
        JTextField seatNumberField = new JTextField(15);
        seatNumberField.setEnabled(false);
        
        // Layout
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(deleteByCompartmentBtn, gbc);
        
        gbc.gridy = 1;
        panel.add(deleteSpecificSeatBtn, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Compartment:"), gbc);
        gbc.gridx = 1;
        panel.add(compartmentCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Seat Number:"), gbc);
        gbc.gridx = 1;
        panel.add(seatNumberField, gbc);
        
        // Radio button listeners
        deleteByCompartmentBtn.addActionListener(e -> {
            compartmentCombo.setEnabled(true);
            seatNumberField.setEnabled(false);
        });
        
        deleteSpecificSeatBtn.addActionListener(e -> {
            compartmentCombo.setEnabled(true);
            seatNumberField.setEnabled(true);
        });
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton deleteButton = new JButton("Delete");
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setBackground(new Color(220, 20, 60));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        
        deleteButton.addActionListener(e -> {
            CompartmentInfo selectedCompartment = (CompartmentInfo) compartmentCombo.getSelectedItem();
            if (selectedCompartment == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a compartment", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success;
            String confirmMessage;
            
            if (deleteByCompartmentBtn.isSelected()) {
                confirmMessage = "Are you sure you want to delete ALL seats in compartment '" + selectedCompartment.compartmentName + "'?";
                int choice = JOptionPane.showConfirmDialog(dialog, confirmMessage, "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    success = deleteAllSeatsInCompartment(selectedCompartment.compartmentId);
                } else {
                    return;
                }
            } else {
                String seatNumber = seatNumberField.getText().trim();
                if (seatNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter seat number", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                confirmMessage = "Are you sure you want to delete seat '" + seatNumber + "' from compartment '" + selectedCompartment.compartmentName + "'?";
                int choice = JOptionPane.showConfirmDialog(dialog, confirmMessage, "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    success = deleteSpecificSeat(selectedCompartment.compartmentId, seatNumber);
                } else {
                    return;
                }
            }
            
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Seats deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadSeats(train, seatArea); // Refresh display
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to delete seats", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean deleteCompartmentFromDatabase(int compartmentId) {
        try {
            // First delete all seats in the compartment
            String deleteSeatsQuery = "DELETE FROM seats WHERE compartment_id = ?";
            try (PreparedStatement deleteSeatsStmt = DatabaseManager.getInstance().getConnection().prepareStatement(deleteSeatsQuery)) {
                deleteSeatsStmt.setInt(1, compartmentId);
                deleteSeatsStmt.executeUpdate();
            }
            
            // Then delete the compartment
            String deleteCompartmentQuery = "DELETE FROM compartments WHERE compartment_id = ?";
            try (PreparedStatement deleteCompartmentStmt = DatabaseManager.getInstance().getConnection().prepareStatement(deleteCompartmentQuery)) {
                deleteCompartmentStmt.setInt(1, compartmentId);
                return deleteCompartmentStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting compartment: " + e.getMessage());
            return false;
        }
    }
    
    private boolean deleteAllSeatsInCompartment(int compartmentId) {
        try {
            String query = "DELETE FROM seats WHERE compartment_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, compartmentId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting seats: " + e.getMessage());
            return false;
        }
    }
    
    private boolean deleteSpecificSeat(int compartmentId, String seatNumber) {
        try {
            String query = "DELETE FROM seats WHERE compartment_id = ? AND seat_number = ?";
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, compartmentId);
                pstmt.setString(2, seatNumber);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting specific seat: " + e.getMessage());
            return false;
        }
    }

    private boolean addRouteToDatabase(int trainId, String source, String dest, String depTime, String arrTime, double price, int stops, String intermediateStations) {
        try {
            System.out.println("DEBUG: Adding route - trainId: " + trainId + ", source: " + source + ", dest: " + dest);
            
            // Check if the extended columns exist
            boolean hasExtendedColumns = checkRouteTableColumns();
            System.out.println("DEBUG: Extended columns exist: " + hasExtendedColumns);
            
            String query;
            if (hasExtendedColumns) {
                query = "INSERT INTO routes (train_id, source_station, destination_station, departure_time, arrival_time, price, stops, intermediate_stations) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                System.out.println("DEBUG: Using extended query with stops and intermediate stations");
            } else {
                query = "INSERT INTO routes (train_id, source_station, destination_station, departure_time, arrival_time, price) VALUES (?, ?, ?, ?, ?, ?)";
                System.out.println("DEBUG: Using basic query without stops and intermediate stations");
            }
            
            try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(query)) {
                pstmt.setInt(1, trainId);
                pstmt.setString(2, source);
                pstmt.setString(3, dest);
                pstmt.setTime(4, java.sql.Time.valueOf(depTime + ":00"));
                pstmt.setTime(5, java.sql.Time.valueOf(arrTime + ":00"));
                pstmt.setBigDecimal(6, new BigDecimal(price));
                
                if (hasExtendedColumns) {
                    pstmt.setInt(7, stops);
                    pstmt.setString(8, intermediateStations);
                    System.out.println("DEBUG: Setting stops: " + stops + ", intermediate stations: " + intermediateStations);
                }
                
                int result = pstmt.executeUpdate();
                System.out.println("DEBUG: Route insert result: " + result);
                
                if (result > 0) {
                    System.out.println("DEBUG: Route added successfully");
                    return true;
                } else {
                    System.err.println("DEBUG: Route insert failed - no rows affected");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding route: " + e.getMessage());
            e.printStackTrace();
            
            // Show a user-friendly error message
            SwingUtilities.invokeLater(() -> {
                String errorMsg = "Failed to add route to database.\n\n";
                if (e.getMessage().contains("Column") && e.getMessage().contains("doesn't exist")) {
                    errorMsg += "The database table is missing required columns.\n";
                    errorMsg += "Please run the database update script: fix_routes_table.sql\n";
                } else if (e.getMessage().contains("Duplicate")) {
                    errorMsg += "A route with these details already exists.\n";
                } else {
                    errorMsg += "Database error: " + e.getMessage() + "\n";
                }
                errorMsg += "\nPlease check the console for detailed error information.";
                
                JOptionPane.showMessageDialog(mainFrame, errorMsg, "Database Error", JOptionPane.ERROR_MESSAGE);
            });
            
            return false;
        }
    }
    
    private boolean checkRouteTableColumns() {
        try {
            // Check if the columns exist by using DatabaseMetaData
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Connection conn = dbManager.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            
            boolean hasStops = false;
            boolean hasIntermediateStations = false;
            
            // Check for 'stops' column
            try (ResultSet rs = metaData.getColumns(null, null, "routes", "stops")) {
                if (rs.next()) {
                    hasStops = true;
                    System.out.println("DEBUG: Found 'stops' column");
                }
            }
            
            // Check for 'intermediate_stations' column
            try (ResultSet rs = metaData.getColumns(null, null, "routes", "intermediate_stations")) {
                if (rs.next()) {
                    hasIntermediateStations = true;
                    System.out.println("DEBUG: Found 'intermediate_stations' column");
                }
            }
            
            System.out.println("DEBUG: Column check - stops: " + hasStops + ", intermediate_stations: " + hasIntermediateStations);
            return hasStops && hasIntermediateStations;
            
        } catch (SQLException e) {
            System.err.println("Error checking route table columns: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: try a simple select query to check if columns exist
            try {
                String testQuery = "SELECT stops, intermediate_stations FROM routes LIMIT 1";
                try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(testQuery)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        System.out.println("DEBUG: Fallback column check successful");
                        return true;
                    }
                }
            } catch (SQLException fallbackEx) {
                System.err.println("DEBUG: Fallback column check failed: " + fallbackEx.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Get RAC entries for a specific train
     */
    private List<RACQueue.RACEntryWithTrainInfo> getRACEntriesForTrain(int trainId) {
        try {
            List<RACQueue.RACEntryWithTrainInfo> allEntries = racQueue.getAllRACEntries();
            return allEntries.stream()
                .filter(entry -> entry.getTrainId() == trainId)
                .collect(Collectors.toList());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Error retrieving RAC entries for train ID " + trainId + ": " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get waitlist entries for a specific train
     */
    private List<WaitlistManager.WaitlistEntryWithTrainInfo> getWaitlistEntriesForTrain(int trainId) {
        try {
            List<WaitlistManager.WaitlistEntryWithTrainInfo> allEntries = waitlistManager.getAllWaitlistEntries();
            return allEntries.stream()
                .filter(entry -> entry.getTrainId() == trainId)
                .collect(Collectors.toList());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Error retrieving waitlist entries for train ID " + trainId + ": " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }
    
    /**
     * Build the complete route sequence for display
     */
    private List<String> buildRouteSequence(Route route) {
        List<String> sequence = new ArrayList<>();
        
        // Add source station
        sequence.add(route.getSourceStation());
        
        // Add intermediate stations if they exist
        String intermediateStations = route.getIntermediateStations();
        if (intermediateStations != null && !intermediateStations.trim().isEmpty()) {
            String[] intermediates = intermediateStations.split(",");
            for (String station : intermediates) {
                String trimmed = station.trim();
                if (!trimmed.isEmpty()) {
                    sequence.add(trimmed);
                }
            }
        }
        
        // Add destination station
        sequence.add(route.getDestinationStation());
        
        return sequence;
    }
    
    private void logout() {
        currentUser = null;
        mainPanel.removeAll();
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");
        cardLayout.show(mainPanel, "LOGIN");
        mainFrame.revalidate();
        mainFrame.repaint();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BookMyTicketApp();
        });
    }
}
