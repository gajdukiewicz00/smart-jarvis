package com.smartjarvis.desktop;

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
    
    @Override
    public void stop() {
        logger.info("Stopping SmartJARVIS Desktop Application...");
        
        // Cleanup resources
        try {
            // Stop any background services
            // Close database connections
            // Shutdown thread pools
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