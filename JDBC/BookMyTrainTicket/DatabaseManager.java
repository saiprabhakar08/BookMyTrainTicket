package BookMyTrainTicket;

import java.sql.*;
import java.util.Properties;

/**
 * Database connection and management class
 * Handles all database operations for the train booking system
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/train_booking";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Sai123"; // Set your MySQL password
    
    private static DatabaseManager instance;
    private static Connection connection;
    
    private DatabaseManager() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("serverTimezone", "UTC");
            
            DatabaseManager.connection = DriverManager.getConnection(DB_URL, props);
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public static Connection getConnection() {
        return connection;
    }
    
    private void initializeDatabase() throws SQLException {
        // Create database and tables if they don't exist
        String createDB = "CREATE DATABASE IF NOT EXISTS train_booking";
        String useDB = "USE train_booking";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createDB);
            stmt.executeUpdate(useDB);
            
            // Create all tables from the schema
            createTables();
            insertSampleData();
        }
    }
    
    private void createTables() throws SQLException {
        String[] createTableQueries = {
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                password VARCHAR(100) NOT NULL,
                email VARCHAR(100),
                role ENUM('Admin', 'Regular', 'Senior', 'DifferentlyAbled') NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS trains (
                train_id INT AUTO_INCREMENT PRIMARY KEY,
                train_name VARCHAR(100),
                train_number VARCHAR(20) UNIQUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS routes (
                route_id INT AUTO_INCREMENT PRIMARY KEY,
                train_id INT,
                source_station VARCHAR(100),
                destination_station VARCHAR(100),
                departure_time TIME,
                arrival_time TIME,
                price DECIMAL(10,2),
                FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS classes (
                class_id INT AUTO_INCREMENT PRIMARY KEY,
                train_id INT,
                class_type VARCHAR(50),
                FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS compartments (
                compartment_id INT AUTO_INCREMENT PRIMARY KEY,
                class_id INT,
                compartment_name VARCHAR(50),
                FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS seats (
                seat_id INT AUTO_INCREMENT PRIMARY KEY,
                compartment_id INT,
                berth_type ENUM('Lower', 'Middle', 'Upper', 'Side Lower', 'Side Upper'),
                seat_number VARCHAR(20),
                is_available BOOLEAN DEFAULT TRUE,
                FOREIGN KEY (compartment_id) REFERENCES compartments(compartment_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS bookings (
                booking_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                seat_id INT,
                train_id INT,
                route_id INT,
                passenger_name VARCHAR(100),
                passenger_age INT,
                booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                status ENUM('Confirmed', 'Cancelled', 'RAC', 'Waiting') DEFAULT 'Confirmed',
                FOREIGN KEY (user_id) REFERENCES users(user_id),
                FOREIGN KEY (seat_id) REFERENCES seats(seat_id),
                FOREIGN KEY (train_id) REFERENCES trains(train_id),
                FOREIGN KEY (route_id) REFERENCES routes(route_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS payments (
                payment_id INT AUTO_INCREMENT PRIMARY KEY,
                booking_id INT,
                amount DECIMAL(10,2),
                status ENUM('Success', 'Failed', 'Pending'),
                payment_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS waitlist (
                waitlist_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                train_id INT,
                route_id INT,
                request_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                status ENUM('Waiting', 'Promoted') DEFAULT 'Waiting',
                position INT,
                FOREIGN KEY (user_id) REFERENCES users(user_id),
                FOREIGN KEY (train_id) REFERENCES trains(train_id),
                FOREIGN KEY (route_id) REFERENCES routes(route_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS rac (
                rac_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                train_id INT,
                route_id INT,
                request_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                status ENUM('RAC', 'Promoted') DEFAULT 'RAC',
                position INT,
                FOREIGN KEY (user_id) REFERENCES users(user_id),
                FOREIGN KEY (train_id) REFERENCES trains(train_id),
                FOREIGN KEY (route_id) REFERENCES routes(route_id)
            )
            """
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String query : createTableQueries) {
                stmt.executeUpdate(query);
            }
        }
    }
    
    private void insertSampleData() throws SQLException {
        // Check if data already exists
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        try (PreparedStatement pstmt = connection.prepareStatement(checkQuery);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Data already exists
            }
        }
        
        // Insert sample data
        String[] sampleDataQueries = {
            "INSERT INTO users (username, password, email, role) VALUES ('admin', 'admin123', 'admin@train.com', 'Admin')",
            "INSERT INTO users (username, password, email, role) VALUES ('john_doe', 'password123', 'john@email.com', 'Regular')",
            "INSERT INTO users (username, password, email, role) VALUES ('senior_user', 'senior123', 'senior@email.com', 'Senior')",
            
            "INSERT INTO trains (train_name, train_number) VALUES ('Rajdhani Express', '12301')",
            "INSERT INTO trains (train_name, train_number) VALUES ('Shatabdi Express', '12002')",
            "INSERT INTO trains (train_name, train_number) VALUES ('Duronto Express', '12259')",
            
            "INSERT INTO routes (train_id, source_station, destination_station, departure_time, arrival_time, price) VALUES (1, 'New Delhi', 'Mumbai Central', '16:55:00', '08:35:00', 1500.00)",
            "INSERT INTO routes (train_id, source_station, destination_station, departure_time, arrival_time, price) VALUES (2, 'New Delhi', 'Chandigarh', '17:20:00', '21:00:00', 800.00)",
            "INSERT INTO routes (train_id, source_station, destination_station, departure_time, arrival_time, price) VALUES (3, 'Mumbai Central', 'Pune', '06:00:00', '09:30:00', 600.00)",
            
            "INSERT INTO classes (train_id, class_type) VALUES (1, 'AC 2 Tier')",
            "INSERT INTO classes (train_id, class_type) VALUES (1, 'AC 3 Tier')",
            "INSERT INTO classes (train_id, class_type) VALUES (2, 'AC Chair Car')",
            
            "INSERT INTO compartments (class_id, compartment_name) VALUES (1, 'A1')",
            "INSERT INTO compartments (class_id, compartment_name) VALUES (1, 'A2')",
            "INSERT INTO compartments (class_id, compartment_name) VALUES (2, 'B1')",
            "INSERT INTO compartments (class_id, compartment_name) VALUES (3, 'CC1')"
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String query : sampleDataQueries) {
                stmt.executeUpdate(query);
            }
        }
        
        // Generate seats for compartments
        generateSeats();
    }
    
    private void generateSeats() throws SQLException {
        String[] berthTypes = {"Lower", "Middle", "Upper", "Side Lower", "Side Upper"};
        
        // Generate seats for each compartment
        String getCompartmentsQuery = "SELECT compartment_id, compartment_name FROM compartments";
        try (PreparedStatement pstmt = connection.prepareStatement(getCompartmentsQuery);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int compartmentId = rs.getInt("compartment_id");
                String compartmentName = rs.getString("compartment_name");
                
                // Create 24 seats per compartment
                for (int i = 1; i <= 24; i++) {
                    String seatNumber = compartmentName + "-" + i;
                    String berthType = berthTypes[(i - 1) % berthTypes.length];
                    
                    String insertSeatQuery = "INSERT INTO seats (compartment_id, berth_type, seat_number, is_available) VALUES (?, ?, ?, TRUE)";
                    try (PreparedStatement seatStmt = connection.prepareStatement(insertSeatQuery)) {
                        seatStmt.setInt(1, compartmentId);
                        seatStmt.setString(2, berthType);
                        seatStmt.setString(3, seatNumber);
                        seatStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}