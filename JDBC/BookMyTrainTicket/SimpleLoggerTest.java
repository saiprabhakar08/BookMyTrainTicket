package BookMyTrainTicket;

/**
 * Simple test to verify Logger functionality without database dependency
 */
public class SimpleLoggerTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Logger functionality...");
            
            // Test the Logger directly
            Logger logger = Logger.getInstance();
            
            logger.info("SimpleLoggerTest", "main", "Logger test started");
            logger.debug("SimpleLoggerTest", "main", "This is a debug message");
            logger.warn("SimpleLoggerTest", "main", "This is a warning message");
            logger.error("SimpleLoggerTest", "main", "This is an error message");
            
            logger.logMethodEntry("SimpleLoggerTest", "testMethod", "param1=test", "param2=123");
            logger.logMethodExit("SimpleLoggerTest", "testMethod", "success");
            
            logger.info("SimpleLoggerTest", "main", "Logger test completed successfully");
            
            System.out.println("Logger test completed! Check logs/login_operations.log for output.");
            
        } catch (Exception e) {
            System.err.println("Error during logger test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
