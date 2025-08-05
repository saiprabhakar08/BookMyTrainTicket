package BookMyTrainTicket;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Manages booking operations
 */
public class BookingManager {
    private DatabaseManager dbManager;
    private SeatAvailabilityManager seatManager;
    private WaitlistManager waitlistManager;
    private RACQueue racQueue;
    
    public BookingManager() throws SQLException {
        this.dbManager = DatabaseManager.getInstance();
        this.seatManager = new SeatAvailabilityManager();
        this.waitlistManager = new WaitlistManager();
        this.racQueue = new RACQueue();
    }
    
    /**
     * Create a new booking
     */
    public BookingResult createBooking(int userId, int seatId, int trainId, int routeId, 
                                     String passengerName, int passengerAge) throws SQLException {
        
        // Enhanced validation for input parameters
        if (passengerName == null) {
            throw new SQLException("Passenger name cannot be null");
        }
        
        // Trim and validate passenger name
        passengerName = passengerName.trim();
        if (passengerName.isEmpty()) {
            throw new SQLException("Passenger name cannot be empty");
        }
        
        // Check for minimum length and valid characters
        if (passengerName.length() < 2) {
            throw new SQLException("Passenger name must be at least 2 characters long");
        }
        
        // Remove any problematic characters and ensure it's valid
        passengerName = passengerName.replaceAll("[^a-zA-Z0-9\\s.-]", "").trim();
        if (passengerName.isEmpty()) {
            throw new SQLException("Passenger name contains invalid characters");
        }
        
        if (passengerAge <= 0 || passengerAge > 120) {
            throw new SQLException("Invalid passenger age: " + passengerAge);
        }
        
        System.out.println("DEBUG: Creating booking - userId: " + userId + ", seatId: " + seatId + 
                          ", trainId: " + trainId + ", routeId: " + routeId + 
                          ", passengerName: '" + passengerName + "' (length: " + passengerName.length() + "), passengerAge: " + passengerAge);
        
        // Check if this is a RAC/Waitlist booking request (seatId = -1)
        if (seatId == -1) {
            return handleRACWaitlistBooking(userId, trainId, routeId, passengerName, passengerAge);
        }
        
        // Check if seat is available
        SeatAvailabilityManager.SeatWithDetails seat = seatManager.getSeatById(seatId);
        if (seat == null || !seat.isAvailable()) {
            // Try to add to RAC or waitlist
            return handleRACWaitlistBooking(userId, trainId, routeId, passengerName, passengerAge);
        }
        
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Create booking
            String bookingQuery = """
                INSERT INTO bookings (user_id, seat_id, train_id, route_id, passenger_name, passenger_age, status)
                VALUES (?, ?, ?, ?, ?, ?, 'Confirmed')
                """;
            
            int bookingId;
            try (PreparedStatement pstmt = conn.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, seatId);
                pstmt.setInt(3, trainId);
                pstmt.setInt(4, routeId);
                
                // Extra safety check before setting passenger name
                if (passengerName == null || passengerName.trim().isEmpty()) {
                    throw new SQLException("CRITICAL: Passenger name is null or empty at SQL execution");
                }
                pstmt.setString(5, passengerName);
                pstmt.setInt(6, passengerAge);
                
                System.out.println("DEBUG: Executing confirmed booking insert with passengerName: '" + passengerName + "' (length: " + passengerName.length() + ")");
                
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookingId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get booking ID");
                    }
                }
            } catch (SQLException sqlEx) {
                // Enhanced error handling for passenger_name issues
                if (sqlEx.getMessage().contains("passenger_name") && 
                    sqlEx.getMessage().toLowerCase().contains("default value")) {
                    System.err.println("CRITICAL ERROR: passenger_name constraint violation");
                    System.err.println("Passenger name value: '" + passengerName + "'");
                    System.err.println("Passenger name length: " + (passengerName != null ? passengerName.length() : "NULL"));
                    throw new SQLException("Invalid passenger name - ensure name is at least 2 characters and contains valid characters", sqlEx);
                } else {
                    throw sqlEx;
                }
            }
            
            // Mark seat as unavailable
            seatManager.updateSeatAvailability(seatId, false);
            
            // Create payment record
            BigDecimal amount = getRoutePrice(routeId);
            createPayment(bookingId, amount);
            
            conn.commit();
            
            return new BookingResult(true, "Booking confirmed successfully", bookingId, "Confirmed");
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Handle booking when train is full (add to RAC or waitlist)
     */
    private BookingResult handleRACWaitlistBooking(int userId, int trainId, int routeId, 
                                                  String passengerName, int passengerAge) throws SQLException {
        
        // Enhanced validation for input parameters
        if (passengerName == null) {
            System.err.println("ERROR: Passenger name is null in handleRACWaitlistBooking");
            throw new SQLException("Passenger name cannot be null");
        }
        
        // Trim and validate passenger name
        passengerName = passengerName.trim();
        if (passengerName.isEmpty()) {
            System.err.println("ERROR: Passenger name is empty after trim in handleRACWaitlistBooking");
            throw new SQLException("Passenger name cannot be empty");
        }
        
        // Check for minimum length and valid characters
        if (passengerName.length() < 2) {
            System.err.println("ERROR: Passenger name too short: '" + passengerName + "' (length: " + passengerName.length() + ")");
            throw new SQLException("Passenger name must be at least 2 characters long");
        }
        
        // Remove any problematic characters and ensure it's valid
        String originalName = passengerName;
        passengerName = passengerName.replaceAll("[^a-zA-Z0-9\\s.-]", "").trim();
        if (passengerName.isEmpty()) {
            System.err.println("ERROR: Passenger name became empty after cleaning. Original: '" + originalName + "'");
            throw new SQLException("Passenger name contains invalid characters");
        }
        
        // Final length check after cleaning
        if (passengerName.length() < 2) {
            System.err.println("ERROR: Passenger name too short after cleaning: '" + passengerName + "' (length: " + passengerName.length() + ")");
            throw new SQLException("Passenger name must be at least 2 characters long after removing invalid characters");
        }
        
        if (passengerAge <= 0 || passengerAge > 120) {
            throw new SQLException("Invalid passenger age: " + passengerAge);
        }
        
        System.out.println("DEBUG: Creating RAC/Waitlist booking - userId: " + userId + 
                          ", trainId: " + trainId + ", routeId: " + routeId + 
                          ", passengerName: '" + passengerName + "' (length: " + passengerName.length() + "), passengerAge: " + passengerAge);
        
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Check RAC availability (maximum 100 RAC positions)
            int racCount = racQueue.getRACCount(trainId, routeId);
            String status;
            String message;
            
            if (racCount < 100) {
                status = "RAC";
                message = "Added to RAC. Position: " + (racCount + 1);
            } else {
                status = "Waiting";
                // Get waitlist position
                int waitlistCount = waitlistManager.getWaitlistCount(trainId, routeId);
                message = "Added to waitlist. Position: " + (waitlistCount + 1);
            }
            
            System.out.println("DEBUG: Booking status determined as: " + status);
            
            // Create booking record with passenger details
            String bookingQuery = """
                INSERT INTO bookings (user_id, seat_id, train_id, route_id, passenger_name, passenger_age, status)
                VALUES (?, NULL, ?, ?, ?, ?, ?)
                """;
            
            int bookingId;
            try (PreparedStatement pstmt = conn.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);        // Position 1: user_id
                pstmt.setInt(2, trainId);       // Position 3: train_id (Position 2 is NULL for seat_id)
                pstmt.setInt(3, routeId);       // Position 4: route_id
                
                // Extra safety check before setting passenger name
                if (passengerName == null || passengerName.trim().isEmpty()) {
                    throw new SQLException("CRITICAL: Passenger name is null or empty at SQL execution for RAC/Waitlist");
                }
                pstmt.setString(4, passengerName);  // Position 5: passenger_name
                pstmt.setInt(5, passengerAge);      // Position 6: passenger_age
                pstmt.setString(6, status);         // Position 7: status
                
                System.out.println("DEBUG: Executing booking insert with parameters: " +
                                 "userId=" + userId + ", trainId=" + trainId + ", routeId=" + routeId + 
                                 ", passengerName='" + passengerName + "' (length: " + passengerName.length() + "), passengerAge=" + passengerAge + 
                                 ", status='" + status + "'");
                
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookingId = rs.getInt(1);
                        System.out.println("DEBUG: Booking created with ID: " + bookingId);
                    } else {
                        throw new SQLException("Failed to get booking ID");
                    }
                }
            } catch (SQLException sqlEx) {
                // Enhanced error handling for passenger_name issues
                if (sqlEx.getMessage().contains("passenger_name") && 
                    sqlEx.getMessage().toLowerCase().contains("default value")) {
                    System.err.println("CRITICAL ERROR: passenger_name constraint violation in RAC/Waitlist");
                    System.err.println("Passenger name value: '" + passengerName + "'");
                    System.err.println("Passenger name length: " + (passengerName != null ? passengerName.length() : "NULL"));
                    throw new SQLException("Invalid passenger name for RAC/Waitlist booking - ensure name is at least 2 characters and contains valid characters", sqlEx);
                } else {
                    throw sqlEx;
                }
            }
            
            // Add to appropriate queue
            if ("RAC".equals(status)) {
                racQueue.addToRAC(userId, trainId, routeId);
            } else {
                waitlistManager.addToWaitlist(userId, trainId, routeId);
            }
            
            conn.commit();
            return new BookingResult(true, message, bookingId, status);
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private BookingResult handleFullBooking(int userId, int trainId, int routeId, 
                                          String passengerName, int passengerAge) throws SQLException {
        // Check RAC availability (maximum 100 RAC positions)
        int racCount = racQueue.getRACCount(trainId, routeId);
        if (racCount < 100) {
            int racId = racQueue.addToRAC(userId, trainId, routeId);
            return new BookingResult(true, "Added to RAC. Position: " + (racCount + 1), racId, "RAC");
        } else {
            // Add to waitlist
            int waitlistId = waitlistManager.addToWaitlist(userId, trainId, routeId);
            int position = waitlistManager.getWaitlistPosition(waitlistId);
            return new BookingResult(true, "Added to waitlist. Position: " + position, waitlistId, "Waiting");
        }
    }
    
    /**
     * Cancel a booking
     */
    public boolean cancelBooking(int bookingId) throws SQLException {
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Get booking details
            BookingDetails booking = getBookingById(bookingId);
            if (booking == null) {
                return false;
            }
            
            // Update booking status
            String updateQuery = "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setInt(1, bookingId);
                pstmt.executeUpdate();
            }
            
            // Make seat available again
            if (booking.getSeatId() > 0) {
                seatManager.updateSeatAvailability(booking.getSeatId(), true);
                
                // Promote from RAC or waitlist
                promoteFromQueue(booking.getTrainId(), booking.getRouteId());
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Promote passengers from RAC or waitlist when seat becomes available
     */
    private void promoteFromQueue(int trainId, int routeId) throws SQLException {
        // First try to promote from RAC
        if (racQueue.promoteFromRAC(trainId, routeId)) {
            return;
        }
        
        // Then try to promote from waitlist
        waitlistManager.promoteFromWaitlist(trainId, routeId);
    }
    
    /**
     * Get bookings for a user
     */
    /**
     * Get all bookings for a specific user
     * Uses a subquery to get only the latest payment for each booking to avoid duplicates
     */
    public List<BookingDetails> getBookingsForUser(int userId) throws SQLException {
        List<BookingDetails> bookings = new ArrayList<>();
        
        String query = """
            SELECT b.booking_id, b.user_id, b.seat_id, b.train_id, b.route_id,
                   b.passenger_name, b.passenger_age, b.booking_time, b.status,
                   t.train_name, t.train_number,
                   r.source_station, r.destination_station, r.departure_time, r.arrival_time, r.price,
                   s.seat_number, s.berth_type,
                   c.compartment_name, cl.class_type,
                   p.amount as payment_amount, p.status as payment_status
            FROM bookings b
            JOIN trains t ON b.train_id = t.train_id
            JOIN routes r ON b.route_id = r.route_id
            LEFT JOIN seats s ON b.seat_id = s.seat_id
            LEFT JOIN compartments c ON s.compartment_id = c.compartment_id
            LEFT JOIN classes cl ON c.class_id = cl.class_id
            LEFT JOIN (
                SELECT p1.booking_id, p1.amount, p1.status, p1.payment_time
                FROM payments p1
                WHERE p1.payment_time = (
                    SELECT MAX(p2.payment_time) 
                    FROM payments p2 
                    WHERE p2.booking_id = p1.booking_id
                )
            ) p ON b.booking_id = p.booking_id
            WHERE b.user_id = ?
            ORDER BY b.booking_time DESC
            """;
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BookingDetails booking = new BookingDetails();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setSeatId(rs.getInt("seat_id"));
                    booking.setTrainId(rs.getInt("train_id"));
                    booking.setRouteId(rs.getInt("route_id"));
                    booking.setPassengerName(rs.getString("passenger_name"));
                    booking.setPassengerAge(rs.getInt("passenger_age"));
                    booking.setBookingTime(rs.getTimestamp("booking_time").toLocalDateTime());
                    booking.setStatus(rs.getString("status"));
                    booking.setTrainName(rs.getString("train_name"));
                    booking.setTrainNumber(rs.getString("train_number"));
                    booking.setSourceStation(rs.getString("source_station"));
                    booking.setDestinationStation(rs.getString("destination_station"));
                    booking.setDepartureTime(rs.getTime("departure_time") != null ? 
                                           rs.getTime("departure_time").toLocalTime() : null);
                    booking.setArrivalTime(rs.getTime("arrival_time") != null ? 
                                         rs.getTime("arrival_time").toLocalTime() : null);
                    booking.setPrice(rs.getBigDecimal("price"));
                    booking.setSeatNumber(rs.getString("seat_number"));
                    booking.setBerthType(rs.getString("berth_type"));
                    booking.setCompartmentName(rs.getString("compartment_name"));
                    booking.setClassType(rs.getString("class_type"));
                    booking.setPaymentAmount(rs.getBigDecimal("payment_amount"));
                    booking.setPaymentStatus(rs.getString("payment_status"));
                    
                    bookings.add(booking);
                }
            }
        }
        
        return bookings;
    }
    
    /**
     * Get booking by ID
     */
    public BookingDetails getBookingById(int bookingId) throws SQLException {
        String query = """
            SELECT b.booking_id, b.user_id, b.seat_id, b.train_id, b.route_id,
                   b.passenger_name, b.passenger_age, b.booking_time, b.status,
                   t.train_name, t.train_number,
                   r.source_station, r.destination_station, r.departure_time, r.arrival_time, r.price
            FROM bookings b
            JOIN trains t ON b.train_id = t.train_id
            JOIN routes r ON b.route_id = r.route_id
            WHERE b.booking_id = ?
            """;
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, bookingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BookingDetails booking = new BookingDetails();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setSeatId(rs.getInt("seat_id"));
                    booking.setTrainId(rs.getInt("train_id"));
                    booking.setRouteId(rs.getInt("route_id"));
                    booking.setPassengerName(rs.getString("passenger_name"));
                    booking.setPassengerAge(rs.getInt("passenger_age"));
                    booking.setBookingTime(rs.getTimestamp("booking_time").toLocalDateTime());
                    booking.setStatus(rs.getString("status"));
                    booking.setTrainName(rs.getString("train_name"));
                    booking.setTrainNumber(rs.getString("train_number"));
                    booking.setSourceStation(rs.getString("source_station"));
                    booking.setDestinationStation(rs.getString("destination_station"));
                    booking.setDepartureTime(rs.getTime("departure_time").toLocalTime());
                    booking.setArrivalTime(rs.getTime("arrival_time").toLocalTime());
                    booking.setPrice(rs.getBigDecimal("price"));
                    
                    return booking;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Create payment record
     */
    private void createPayment(int bookingId, BigDecimal amount) throws SQLException {
        String query = "INSERT INTO payments (booking_id, amount, status) VALUES (?, ?, 'Success')";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, bookingId);
            pstmt.setBigDecimal(2, amount);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get route price
     */
    private BigDecimal getRoutePrice(int routeId) throws SQLException {
        String query = "SELECT price FROM routes WHERE route_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, routeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("price");
                }
            }
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Inner class for booking results
     */
    public static class BookingResult {
        private boolean success;
        private String message;
        private int id;
        private String status;
        
        public BookingResult(boolean success, String message, int id, String status) {
            this.success = success;
            this.message = message;
            this.id = id;
            this.status = status;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getId() { return id; }
        public String getStatus() { return status; }
    }
    
    /**
     * Inner class for booking details
     */
    public static class BookingDetails {
        private int bookingId;
        private int userId;
        private String username;
        private int seatId;
        private int trainId;
        private int routeId;
        private String passengerName;
        private int passengerAge;
        private LocalDateTime bookingTime;
        private String status;
        private String trainName;
        private String trainNumber;
        private String sourceStation;
        private String destinationStation;
        private java.time.LocalTime departureTime;
        private java.time.LocalTime arrivalTime;
        private BigDecimal price;
        private String seatNumber;
        private String berthType;
        private String compartmentName;
        private String classType;
        private BigDecimal paymentAmount;
        private String paymentStatus;
        
        // Getters and setters
        public int getBookingId() { return bookingId; }
        public void setBookingId(int bookingId) { this.bookingId = bookingId; }
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public int getSeatId() { return seatId; }
        public void setSeatId(int seatId) { this.seatId = seatId; }
        
        public int getTrainId() { return trainId; }
        public void setTrainId(int trainId) { this.trainId = trainId; }
        
        public int getRouteId() { return routeId; }
        public void setRouteId(int routeId) { this.routeId = routeId; }
        
        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
        
        public int getPassengerAge() { return passengerAge; }
        public void setPassengerAge(int passengerAge) { this.passengerAge = passengerAge; }
        
        public LocalDateTime getBookingTime() { return bookingTime; }
        public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public String getSourceStation() { return sourceStation; }
        public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }
        
        public String getDestinationStation() { return destinationStation; }
        public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }
        
        public java.time.LocalTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(java.time.LocalTime departureTime) { this.departureTime = departureTime; }
        
        public java.time.LocalTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(java.time.LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
        
        public String getBerthType() { return berthType; }
        public void setBerthType(String berthType) { this.berthType = berthType; }
        
        public String getCompartmentName() { return compartmentName; }
        public void setCompartmentName(String compartmentName) { this.compartmentName = compartmentName; }
        
        public String getClassType() { return classType; }
        public void setClassType(String classType) { this.classType = classType; }
        
        public BigDecimal getPaymentAmount() { return paymentAmount; }
        public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
        
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        
        @Override
        public String toString() {
            return "Booking #" + bookingId + " - " + trainName + " (" + trainNumber + ") - " +
                   sourceStation + " to " + destinationStation + " - " + status;
        }
    }
    
    /**
     * Get all bookings for admin view
     */
    public List<BookingDetails> getAllBookings() throws SQLException {
        List<BookingDetails> bookings = new ArrayList<>();
        
        String query = """
            SELECT b.booking_id, b.user_id, b.passenger_name, b.passenger_age, b.status, b.booking_time,
                   t.train_name, t.train_number,
                   r.source_station, r.destination_station, r.price,
                   s.seat_number, s.berth_type,
                   c.compartment_name, cl.class_type,
                   u.username,
                   p.amount as payment_amount, p.status as payment_status
            FROM bookings b
            JOIN trains t ON b.train_id = t.train_id
            JOIN routes r ON b.route_id = r.route_id
            LEFT JOIN seats s ON b.seat_id = s.seat_id
            LEFT JOIN compartments c ON s.compartment_id = c.compartment_id
            LEFT JOIN classes cl ON c.class_id = cl.class_id
            LEFT JOIN users u ON b.user_id = u.user_id
            LEFT JOIN payments p ON b.booking_id = p.booking_id
            ORDER BY b.booking_time DESC
            """;
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                BookingDetails booking = new BookingDetails();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setUsername(rs.getString("username"));
                booking.setPassengerName(rs.getString("passenger_name"));
                booking.setPassengerAge(rs.getInt("passenger_age"));
                booking.setStatus(rs.getString("status"));
                booking.setBookingTime(rs.getTimestamp("booking_time").toLocalDateTime());
                booking.setTrainName(rs.getString("train_name"));
                booking.setTrainNumber(rs.getString("train_number"));
                booking.setSourceStation(rs.getString("source_station"));
                booking.setDestinationStation(rs.getString("destination_station"));
                booking.setPrice(rs.getBigDecimal("price"));
                booking.setSeatNumber(rs.getString("seat_number"));
                booking.setBerthType(rs.getString("berth_type"));
                booking.setCompartmentName(rs.getString("compartment_name"));
                booking.setClassType(rs.getString("class_type"));
                
                BigDecimal paymentAmount = rs.getBigDecimal("payment_amount");
                if (paymentAmount != null) {
                    booking.setPaymentAmount(paymentAmount);
                }
                
                String paymentStatus = rs.getString("payment_status");
                if (paymentStatus != null) {
                    booking.setPaymentStatus(paymentStatus);
                }
                
                bookings.add(booking);
            }
        }
        
        return bookings;
    }
}
