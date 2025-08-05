package BookMyTrainTicket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages train-related operations
 */
public class TrainManager {
    private DatabaseManager dbManager;
    
    public TrainManager() throws SQLException {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Get all trains
     */
    public List<Train> getAllTrains() throws SQLException {
        List<Train> trains = new ArrayList<>();
        String query = "SELECT train_id, train_name, train_number FROM trains ORDER BY train_name";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Train train = new Train(
                    rs.getInt("train_id"),
                    rs.getString("train_name"),
                    rs.getString("train_number")
                );
                trains.add(train);
            }
        }
        
        return trains;
    }
    
    /**
     * Add a new train
     */
    public boolean addTrain(String trainName, String trainNumber) throws SQLException {
        String query = "INSERT INTO trains (train_name, train_number) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, trainName);
            pstmt.setString(2, trainNumber);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Update train information
     */
    public boolean updateTrain(int trainId, String trainName, String trainNumber) throws SQLException {
        String query = "UPDATE trains SET train_name = ?, train_number = ? WHERE train_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, trainName);
            pstmt.setString(2, trainNumber);
            pstmt.setInt(3, trainId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Delete a train
     */
    public boolean deleteTrain(int trainId) throws SQLException {
        String query = "DELETE FROM trains WHERE train_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, trainId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Search trains by source and destination
     */
    public List<TrainSearchResult> searchTrains(String source, String destination) throws SQLException {
        List<TrainSearchResult> results = new ArrayList<>();
        
        // First, check if intermediate_stations column exists
        boolean hasIntermediateStations = checkIntermediateStationsColumn();
        
        String query;
        if (hasIntermediateStations) {
            // Enhanced query that searches in intermediate stations as well
            query = """
                SELECT DISTINCT t.train_id, t.train_name, t.train_number,
                       r.route_id, r.source_station, r.destination_station,
                       r.departure_time, r.arrival_time, r.price, r.intermediate_stations
                FROM trains t
                JOIN routes r ON t.train_id = r.train_id
                WHERE (
                    -- Direct source-destination match
                    (LOWER(r.source_station) LIKE LOWER(?) AND LOWER(r.destination_station) LIKE LOWER(?))
                    OR
                    -- Source in intermediate stations, destination as actual destination
                    (LOWER(r.intermediate_stations) LIKE LOWER(?) AND LOWER(r.destination_station) LIKE LOWER(?))
                    OR
                    -- Source as actual source, destination in intermediate stations
                    (LOWER(r.source_station) LIKE LOWER(?) AND LOWER(r.intermediate_stations) LIKE LOWER(?))
                    OR
                    -- Both source and destination in intermediate stations (if train passes through both)
                    (LOWER(r.intermediate_stations) LIKE LOWER(?) AND LOWER(r.intermediate_stations) LIKE LOWER(?))
                )
                ORDER BY t.train_name
                """;
        } else {
            // Fallback to original query if intermediate_stations column doesn't exist
            query = """
                SELECT DISTINCT t.train_id, t.train_name, t.train_number,
                       r.route_id, r.source_station, r.destination_station,
                       r.departure_time, r.arrival_time, r.price
                FROM trains t
                JOIN routes r ON t.train_id = r.train_id
                WHERE LOWER(r.source_station) LIKE LOWER(?) 
                AND LOWER(r.destination_station) LIKE LOWER(?)
                ORDER BY t.train_name
                """;
        }
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            if (hasIntermediateStations) {
                // Set parameters for enhanced search
                String sourcePattern = "%" + source + "%";
                String destPattern = "%" + destination + "%";
                
                pstmt.setString(1, sourcePattern);  // Direct source match
                pstmt.setString(2, destPattern);    // Direct destination match
                pstmt.setString(3, sourcePattern);  // Source in intermediate stations
                pstmt.setString(4, destPattern);    // Destination as actual destination
                pstmt.setString(5, sourcePattern);  // Source as actual source
                pstmt.setString(6, destPattern);    // Destination in intermediate stations
                pstmt.setString(7, sourcePattern);  // Source in intermediate stations
                pstmt.setString(8, destPattern);    // Destination in intermediate stations
            } else {
                // Set parameters for basic search
                pstmt.setString(1, "%" + source + "%");
                pstmt.setString(2, "%" + destination + "%");
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Train train = new Train(
                        rs.getInt("train_id"),
                        rs.getString("train_name"),
                        rs.getString("train_number")
                    );
                    
                    Route route;
                    if (hasIntermediateStations) {
                        // Use the enhanced constructor with intermediate stations
                        route = new Route(
                            rs.getInt("route_id"),
                            rs.getInt("train_id"),
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getTime("departure_time").toLocalTime(),
                            rs.getTime("arrival_time").toLocalTime(),
                            rs.getBigDecimal("price"),
                            rs.getString("intermediate_stations")
                        );
                    } else {
                        // Use the original constructor without intermediate stations
                        route = new Route(
                            rs.getInt("route_id"),
                            rs.getInt("train_id"),
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getTime("departure_time").toLocalTime(),
                            rs.getTime("arrival_time").toLocalTime(),
                            rs.getBigDecimal("price")
                        );
                    }
                    
                    // Validate station order - only include if source comes before destination in the route
                    if (isValidStationOrder(route, source, destination)) {
                        int availableSeats = getAvailableSeatsCount(train.getTrainId(), route.getRouteId());
                        TrainSearchResult result = new TrainSearchResult(train, route, availableSeats);
                        results.add(result);
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Check if the routes table has the intermediate_stations column
     */
    private boolean checkIntermediateStationsColumn() {
        try {
            String testQuery = "SELECT intermediate_stations FROM routes LIMIT 1";
            try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(testQuery)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    return true; // Column exists
                }
            }
        } catch (SQLException e) {
            // Column doesn't exist
            return false;
        }
    }
    
    /**
     * Validate that the source station comes before the destination station in the route order
     */
    private boolean isValidStationOrder(Route route, String searchSource, String searchDestination) {
        String sourceStation = route.getSourceStation();
        String destinationStation = route.getDestinationStation();
        String intermediateStations = route.getIntermediateStations();
        
        // Convert search terms to lowercase for comparison
        String searchSourceLower = searchSource.toLowerCase();
        String searchDestLower = searchDestination.toLowerCase();
        
        // Build the complete station sequence: source -> intermediate stations -> destination
        List<String> stationSequence = new ArrayList<>();
        stationSequence.add(sourceStation.toLowerCase());
        
        // Add intermediate stations if they exist
        if (intermediateStations != null && !intermediateStations.trim().isEmpty()) {
            String[] intermediates = intermediateStations.split(",");
            for (String station : intermediates) {
                stationSequence.add(station.trim().toLowerCase());
            }
        }
        
        stationSequence.add(destinationStation.toLowerCase());
        
        // Find positions of search source and destination in the sequence
        int sourcePos = -1;
        int destPos = -1;
        
        for (int i = 0; i < stationSequence.size(); i++) {
            String station = stationSequence.get(i);
            
            // Check if this station matches the search source
            if (sourcePos == -1 && station.contains(searchSourceLower)) {
                sourcePos = i;
            }
            
            // Check if this station matches the search destination
            if (destPos == -1 && station.contains(searchDestLower)) {
                destPos = i;
            }
        }
        
        // Valid only if both stations found and source comes before destination
        if (sourcePos != -1 && destPos != -1) {
            boolean isValidOrder = sourcePos < destPos;
            
            // Debug logging
            System.out.println("DEBUG: Station order validation for " + route.getRouteId());
            System.out.println("  Route: " + sourceStation + " -> " + destinationStation);
            System.out.println("  Intermediates: " + (intermediateStations != null ? intermediateStations : "None"));
            System.out.println("  Search: " + searchSource + " -> " + searchDestination);
            System.out.println("  Source position: " + sourcePos + ", Dest position: " + destPos);
            System.out.println("  Valid order: " + isValidOrder);
            
            return isValidOrder;
        }
        
        // If either station not found, this route doesn't match the search criteria
        return false;
    }
    
    /**
     * Get train by ID
     */
    public Train getTrainById(int trainId) throws SQLException {
        String query = "SELECT train_id, train_name, train_number FROM trains WHERE train_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, trainId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Train(
                        rs.getInt("train_id"),
                        rs.getString("train_name"),
                        rs.getString("train_number")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if train number already exists
     */
    public boolean trainNumberExists(String trainNumber) throws SQLException {
        String query = "SELECT COUNT(*) FROM trains WHERE train_number = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, trainNumber);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get available seats count for a train and route
     */
    private int getAvailableSeatsCount(int trainId, int routeId) throws SQLException {
        String query = """
            SELECT COUNT(*) FROM seats s
            JOIN compartments c ON s.compartment_id = c.compartment_id
            JOIN classes cl ON c.class_id = cl.class_id
            WHERE cl.train_id = ? AND s.is_available = TRUE
            """;
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, trainId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Inner class to represent search results
     */
    public static class TrainSearchResult {
        private Train train;
        private Route route;
        private int availableSeats;
        
        public TrainSearchResult(Train train, Route route, int availableSeats) {
            this.train = train;
            this.route = route;
            this.availableSeats = availableSeats;
        }
        
        public Train getTrain() { return train; }
        public Route getRoute() { return route; }
        public int getAvailableSeats() { return availableSeats; }
        
        @Override
        public String toString() {
            return train.getTrainName() + " - " + route.getSourceStation() + 
                   " to " + route.getDestinationStation() + " (" + availableSeats + " seats available)";
        }
    }
}
