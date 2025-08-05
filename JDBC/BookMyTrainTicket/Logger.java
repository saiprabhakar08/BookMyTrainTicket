package BookMyTrainTicket;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom Logger class for the BookMyTrainTicket application
 * Provides thread-safe logging capabilities with different log levels
 */
public class Logger {
    private static final String LOG_FILE_PATH = "logs/login_operations.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ReentrantLock lock = new ReentrantLock();
    private static Logger instance;
    
    // Log levels
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    private Logger() {
        // Create logs directory if it doesn't exist
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }
    
    /**
     * Get singleton instance of Logger
     */
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }
    
    /**
     * Log a message with specified level
     */
    public void log(LogLevel level, String className, String methodName, String message) {
        lock.lock();
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String logEntry = String.format("[%s] [%s] [%s.%s] %s%n", 
                timestamp, level, className, methodName, message);
            
            // Write to file
            try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
                writer.write(logEntry);
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
            
            // Also print to console for immediate feedback
            System.out.print(logEntry);
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Log DEBUG level message
     */
    public void debug(String className, String methodName, String message) {
        log(LogLevel.DEBUG, className, methodName, message);
    }
    
    /**
     * Log INFO level message
     */
    public void info(String className, String methodName, String message) {
        log(LogLevel.INFO, className, methodName, message);
    }
    
    /**
     * Log WARN level message
     */
    public void warn(String className, String methodName, String message) {
        log(LogLevel.WARN, className, methodName, message);
    }
    
    /**
     * Log ERROR level message
     */
    public void error(String className, String methodName, String message) {
        log(LogLevel.ERROR, className, methodName, message);
    }
    
    /**
     * Log ERROR level message with exception details
     */
    public void error(String className, String methodName, String message, Exception e) {
        String errorMessage = message + " - Exception: " + e.getClass().getSimpleName() + 
                            " - Message: " + e.getMessage();
        log(LogLevel.ERROR, className, methodName, errorMessage);
    }
    
    /**
     * Log method entry
     */
    public void logMethodEntry(String className, String methodName, String... params) {
        StringBuilder sb = new StringBuilder("Method entry");
        if (params.length > 0) {
            sb.append(" - Parameters: ");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append("param").append(i + 1).append("=").append(params[i]);
            }
        }
        debug(className, methodName, sb.toString());
    }
    
    /**
     * Log method exit
     */
    public void logMethodExit(String className, String methodName, String result) {
        debug(className, methodName, "Method exit - Result: " + result);
    }
    
    /**
     * Log method exit without result
     */
    public void logMethodExit(String className, String methodName) {
        debug(className, methodName, "Method exit");
    }
}
