package BookMyTrainTicket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles user authentication and login operations
 */
public class LoginOperations {
    private Logger logger;
    
    public LoginOperations() throws SQLException {
        // Initialize database manager to ensure connection is available
        DatabaseManager.getInstance();
        this.logger = Logger.getInstance();
        logger.info("LoginOperations", "constructor", "LoginOperations instance created successfully");
    }
    
    /**
     * Authenticate user with username and password
     */
    public User authenticateUser(String username, String password) throws SQLException {
        logger.logMethodEntry("LoginOperations", "authenticateUser", "username=" + username);
        
        String query = "SELECT user_id, username, password, email, role FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            logger.debug("LoginOperations", "authenticateUser", "Executing authentication query for user: " + username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    
                    // In a real application, you would hash passwords
                    if (password.equals(storedPassword)) {
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(rs.getString("password"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(User.UserRole.valueOf(rs.getString("role")));
                        
                        logger.info("LoginOperations", "authenticateUser", "User authentication successful for: " + username + " (ID: " + user.getUserId() + ")");
                        logger.logMethodExit("LoginOperations", "authenticateUser", "Authentication successful");
                        return user;
                    } else {
                        logger.warn("LoginOperations", "authenticateUser", "Password mismatch for user: " + username);
                    }
                } else {
                    logger.warn("LoginOperations", "authenticateUser", "User not found: " + username);
                }
            }
        } catch (SQLException e) {
            logger.error("LoginOperations", "authenticateUser", "Database error during authentication for user: " + username, e);
            throw e;
        }
        
        logger.info("LoginOperations", "authenticateUser", "Authentication failed for user: " + username);
        logger.logMethodExit("LoginOperations", "authenticateUser", "Authentication failed");
        return null; // Authentication failed
    }
    
    /**
     * Register a new user
     */
    public boolean registerUser(String username, String password, String email, User.UserRole role) throws SQLException {
        logger.logMethodEntry("LoginOperations", "registerUser", "username=" + username, "email=" + email, "role=" + role);
        
        // Check if username already exists
        if (userExists(username)) {
            logger.warn("LoginOperations", "registerUser", "Registration failed - Username already exists: " + username);
            logger.logMethodExit("LoginOperations", "registerUser", "false - username exists");
            return false;
        }
        
        String query = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In real app, hash the password
            pstmt.setString(3, email);
            pstmt.setString(4, role.name());
            
            logger.debug("LoginOperations", "registerUser", "Executing user registration query for: " + username);
            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                logger.info("LoginOperations", "registerUser", "User registration successful for: " + username + " with role: " + role);
            } else {
                logger.error("LoginOperations", "registerUser", "User registration failed - No rows affected for: " + username);
            }
            
            logger.logMethodExit("LoginOperations", "registerUser", String.valueOf(success));
            return success;
        } catch (SQLException e) {
            logger.error("LoginOperations", "registerUser", "Database error during registration for user: " + username, e);
            throw e;
        }
    }
    
    /**
     * Check if username already exists
     */
    private boolean userExists(String username) throws SQLException {
        logger.logMethodEntry("LoginOperations", "userExists", "username=" + username);
        
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    logger.debug("LoginOperations", "userExists", "Username '" + username + "' exists: " + exists);
                    logger.logMethodExit("LoginOperations", "userExists", String.valueOf(exists));
                    return exists;
                }
            }
        } catch (SQLException e) {
            logger.error("LoginOperations", "userExists", "Database error while checking if user exists: " + username, e);
            throw e;
        }
        
        logger.logMethodExit("LoginOperations", "userExists", "false");
        return false;
    }
    
    /**
     * Update user password
     */
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        logger.logMethodEntry("LoginOperations", "updatePassword", "userId=" + userId);
        
        String query = "UPDATE users SET password = ? WHERE user_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, newPassword); // In real app, hash the password
            pstmt.setInt(2, userId);
            
            logger.debug("LoginOperations", "updatePassword", "Executing password update for user ID: " + userId);
            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                logger.info("LoginOperations", "updatePassword", "Password update successful for user ID: " + userId);
            } else {
                logger.warn("LoginOperations", "updatePassword", "Password update failed - No rows affected for user ID: " + userId);
            }
            
            logger.logMethodExit("LoginOperations", "updatePassword", String.valueOf(success));
            return success;
        } catch (SQLException e) {
            logger.error("LoginOperations", "updatePassword", "Database error during password update for user ID: " + userId, e);
            throw e;
        }
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) throws SQLException {
        logger.logMethodEntry("LoginOperations", "getUserById", "userId=" + userId);
        
        String query = "SELECT user_id, username, password, email, role FROM users WHERE user_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, userId);
            
            logger.debug("LoginOperations", "getUserById", "Executing query to get user by ID: " + userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(User.UserRole.valueOf(rs.getString("role")));
                    
                    logger.info("LoginOperations", "getUserById", "User retrieved successfully: " + user.getUsername() + " (ID: " + userId + ")");
                    logger.logMethodExit("LoginOperations", "getUserById", "User found");
                    return user;
                } else {
                    logger.warn("LoginOperations", "getUserById", "User not found with ID: " + userId);
                    logger.logMethodExit("LoginOperations", "getUserById", "User not found");
                }
            }
        } catch (SQLException e) {
            logger.error("LoginOperations", "getUserById", "Database error while retrieving user by ID: " + userId, e);
            throw e;
        }
        
        return null;
    }
    
    /**
     * Get all users (for admin purposes)
     */
    public List<User> getAllUsers() throws SQLException {
        logger.logMethodEntry("LoginOperations", "getAllUsers");
        
        List<User> users = new ArrayList<>();
        String query = "SELECT user_id, username, email, role FROM users ORDER BY user_id";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            logger.debug("LoginOperations", "getAllUsers", "Executing query to retrieve all users");
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(User.UserRole.valueOf(rs.getString("role")));
                users.add(user);
            }
            
            logger.info("LoginOperations", "getAllUsers", "Retrieved " + users.size() + " users from database");
            logger.logMethodExit("LoginOperations", "getAllUsers", "Retrieved " + users.size() + " users");
        } catch (SQLException e) {
            logger.error("LoginOperations", "getAllUsers", "Database error while retrieving all users", e);
            throw e;
        }
        
        return users;
    }
}
