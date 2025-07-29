package com.smartjarvis.desktop.infrastructure.services;

import com.smartjarvis.desktop.infrastructure.services.impl.HttpTaskServiceClient;
import com.smartjarvis.desktop.infrastructure.services.impl.SimpleNLPService;
import com.smartjarvis.desktop.infrastructure.services.impl.SimpleNotificationService;
import com.smartjarvis.desktop.infrastructure.services.impl.SimpleSpeechService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Factory for creating service implementations
 */
public class ServiceFactory {
    
    private static final Logger logger = Logger.getLogger(ServiceFactory.class.getName());
    private static final Properties config = new Properties();
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        try (InputStream input = ServiceFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                config.load(input);
                logger.info("Configuration loaded successfully");
            } else {
                logger.warning("Configuration file not found, using defaults");
            }
        } catch (IOException e) {
            logger.warning("Error loading configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get configuration value with default fallback
     */
    private static String getConfig(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    /**
     * Create TaskServiceClient implementation
     * @return HTTP-based TaskServiceClient
     */
    public static TaskServiceClient createTaskServiceClient() {
        String baseUrl = getConfig("task.service.url", "http://localhost:8082");
        logger.info("Creating TaskServiceClient with base URL: " + baseUrl);
        return new HttpTaskServiceClient(baseUrl);
    }
    
    /**
     * Create NLPService implementation
     * @return Simple NLPService implementation
     */
    public static NLPService createNLPService() {
        String baseUrl = getConfig("nlp.service.url", "http://localhost:3000");
        logger.info("Creating NLPService with base URL: " + baseUrl);
        return new SimpleNLPService();
    }
    
    /**
     * Create NotificationService implementation
     * @return Simple NotificationService implementation
     */
    public static NotificationService createNotificationService() {
        logger.info("Creating NotificationService");
        return new SimpleNotificationService();
    }
    
    /**
     * Create SpeechService implementation
     * @return Simple SpeechService implementation
     */
    public static SpeechService createSpeechService() {
        String baseUrl = getConfig("speech.service.url", "http://localhost:5000");
        logger.info("Creating SpeechService with base URL: " + baseUrl);
        return new SimpleSpeechService();
    }
}