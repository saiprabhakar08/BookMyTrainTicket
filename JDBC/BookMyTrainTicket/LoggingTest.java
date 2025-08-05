package BookMyTrainTicket;

import java.sql.SQLException;

/**
 * Test class to demonstrate logging functionality in LoginOperations
 */
public class LoggingTest {
    public static void main(String[] args) {
        try {
            // Create LoginOperations instance (this will create the first log entry)
            LoginOperations loginOps = new LoginOperations();
            
            // Test user authentication (this will log the process)
            System.out.println("Testing authentication...");
            User user = loginOps.authenticateUser("testuser", "testpass");
            
            if (user != null) {
                System.out.println("Authentication successful for user: " + user.getUsername());
            } else {
                System.out.println("Authentication failed");
            }
            
            // Test user registration
            System.out.println("\nTesting registration...");
            boolean registered = loginOps.registerUser("newuser", "newpass", "new@email.com", User.UserRole.Regular);
            System.out.println("Registration result: " + registered);
            
            // Test getting user by ID
            System.out.println("\nTesting get user by ID...");
            User retrievedUser = loginOps.getUserById(1);
            if (retrievedUser != null) {
                System.out.println("Retrieved user: " + retrievedUser.getUsername());
            } else {
                System.out.println("User not found");
            }
            
            System.out.println("\nCheck the logs/login_operations.log file for detailed logging information!");
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
