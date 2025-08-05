package BookMyTrainTicket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple runner class to compile and execute the BookMyTicket application
 * This handles compilation of all Java files and runs the main application
 */
public class RunApp {

    public static void main(String[] args) {
        try {
            System.out.println("=== BookMyTicket Train Booking System ===");
            System.out.println("Compiling Java files...");

            // Compile all Java files
            boolean compiled = compileJavaFiles();

            if (compiled) {
                System.out.println("Compilation successful!");
                System.out.println("Starting BookMyTicket application...");
                System.out.println("Note: Make sure MySQL is running and database credentials are correct in DatabaseManager.java");
                System.out.println("Default database: train_booking");
                System.out.println("Default user: root, password: (empty)");
                System.out.println("=".repeat(50));

                // Run the main application
                runApplication();
            } else {
                System.err.println("Compilation failed. Please check the errors above.");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean compileJavaFiles() {
        try {
            // Get all Java files recursively from BookMyTrainTicket folder
            File sourceDir = new File("BookMyTrainTicket");
            List<String> javaFiles = new ArrayList<>();
            listJavaFiles(sourceDir, javaFiles);

            if (javaFiles.isEmpty()) {
                System.err.println("No Java files found in BookMyTrainTicket folder!");
                return false;
            }

            // Build javac command
            List<String> command = new ArrayList<>();
            command.add("javac");
            command.add("-cp");
            command.add(getClasspath());

            for (String file : javaFiles) {
                command.add(file);
                System.out.println("  - " + file);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();

            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            System.err.println("Error during compilation: " + e.getMessage());
            return false;
        }
    }

    private static void listJavaFiles(File dir, List<String> javaFiles) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                listJavaFiles(file, javaFiles);
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file.getPath());
            }
        }
    }

    private static void runApplication() {
        try {
            // Build java command to run the main application
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-cp");

            // Include current dir and MySQL connector
            command.add(getClasspath() + File.pathSeparator + ".");
            command.add("BookMyTrainTicket.BookMyTicketApp"); // main class with full package name

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();

            Process process = pb.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error running application: " + e.getMessage());
        }
    }

    private static String getClasspath() {
        // Try to find MySQL connector JAR
        String[] possiblePaths = {
            "mysql-connector-java-8.0.33.jar",
            "mysql-connector-j-8.0.33.jar",
            "lib/mysql-connector-java-8.0.33.jar",
            "lib/mysql-connector-j-8.0.33.jar",
            "/usr/share/java/mysql-connector-java.jar",
            System.getProperty("user.home") + "/mysql-connector-java.jar"
        };

        for (String path : possiblePaths) {
            File jarFile = new File(path);
            if (jarFile.exists()) {
                System.out.println("Found MySQL connector: " + path);
                return path;
            }
        }

        // If no MySQL JAR found, try to download or provide instructions
        System.out.println("Warning: MySQL connector JAR not found.");
        System.out.println("Please download mysql-connector-java JAR and place it in the current directory.");
        System.out.println("Download from: https://dev.mysql.com/downloads/connector/j/");

        return "."; // fallback
    }
}
