package com.smartjarvis.desktop;

import com.smartjarvis.desktop.infrastructure.services.ServiceFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main JavaFX application class for SmartJARVIS Desktop
 */
public class Main extends Application {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    private static final String APP_TITLE = "SmartJARVIS";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting SmartJARVIS Desktop Application...");
            
            // Initialize services with real HTTP clients
            initializeServices();
            
            // Load the TaskManager FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/TaskManagerView.fxml"));
            Parent root = loader.load();
            
            // Create the scene
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // Configure the stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Set window style (optional - for modern look)
            primaryStage.initStyle(StageStyle.DECORATED);
            
            // Handle window close event
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Application closing...");
                cleanupServices();
                Platform.exit();
                System.exit(0);
            });
            
            // Show the stage
            primaryStage.show();
            
            logger.info("SmartJARVIS Desktop Application started successfully");
            
        } catch (IOException e) {
            logger.severe("Error starting application: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    /**
     * Initialize all services with real HTTP clients
     */
    private void initializeServices() {
        try {
            logger.info("Initializing services with real HTTP clients...");
            
            // Create services using factory
            var taskServiceClient = ServiceFactory.createTaskServiceClient();
            var nlpService = ServiceFactory.createNLPService();
            var notificationService = ServiceFactory.createNotificationService();
            var speechService = ServiceFactory.createSpeechService();
            
            // Test connection to task service
            if (taskServiceClient instanceof com.smartjarvis.desktop.infrastructure.services.impl.HttpTaskServiceClient) {
                com.smartjarvis.desktop.infrastructure.services.impl.HttpTaskServiceClient httpClient = 
                    (com.smartjarvis.desktop.infrastructure.services.impl.HttpTaskServiceClient) taskServiceClient;
                
                if (httpClient.testConnection()) {
                    logger.info("Task service connection successful");
                } else {
                    logger.warning("Task service connection failed - using fallback mode");
                }
            }
            
            logger.info("Services initialized successfully");
            
        } catch (Exception e) {
            logger.severe("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cleanup services on application shutdown
     */
    private void cleanupServices() {
        try {
            logger.info("Cleaning up services...");
            // Add any cleanup logic here if needed
            logger.info("Services cleanup completed");
        } catch (Exception e) {
            logger.warning("Error during services cleanup: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        logger.info("Stopping SmartJARVIS Desktop Application...");
        
        // Cleanup resources
        try {
            cleanupServices();
            logger.info("Application cleanup completed");
        } catch (Exception e) {
            logger.warning("Error during application cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Main method to launch the application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Set system properties for JavaFX
        System.setProperty("javafx.application.platform", "desktop");
        
        // Launch the JavaFX application
        launch(args);
    }
} 