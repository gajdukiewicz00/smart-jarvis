package com.smartjarvis.desktop.application.services;

import com.smartjarvis.desktop.domain.Command;
import com.smartjarvis.desktop.domain.Task;
import com.smartjarvis.desktop.domain.Reminder;
import com.smartjarvis.desktop.application.usecases.CommandUseCase;
import com.smartjarvis.desktop.application.usecases.TaskUseCase;
import com.smartjarvis.desktop.application.usecases.ReminderUseCase;
import com.smartjarvis.desktop.infrastructure.services.NLPService;
import com.smartjarvis.desktop.infrastructure.services.SpeechService;
import com.smartjarvis.desktop.infrastructure.services.NotificationService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Core service that coordinates all JARVIS functionality
 */
public class JarvisCore {
    private static final Logger logger = Logger.getLogger(JarvisCore.class.getName());
    
    private final CommandUseCase commandUseCase;
    private final TaskUseCase taskUseCase;
    private final ReminderUseCase reminderUseCase;
    private final NLPService nlpService;
    private final SpeechService speechService;
    private final NotificationService notificationService;
    
    private final ExecutorService executorService;
    private volatile boolean isRunning = false;
    
    public JarvisCore(CommandUseCase commandUseCase,
                     TaskUseCase taskUseCase,
                     ReminderUseCase reminderUseCase,
                     NLPService nlpService,
                     SpeechService speechService,
                     NotificationService notificationService) {
        this.commandUseCase = commandUseCase;
        this.taskUseCase = taskUseCase;
        this.reminderUseCase = reminderUseCase;
        this.nlpService = nlpService;
        this.speechService = speechService;
        this.notificationService = notificationService;
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Start JARVIS core services
     */
    public void start() {
        if (isRunning) {
            logger.warning("JARVIS Core is already running");
            return;
        }
        
        logger.info("Starting JARVIS Core...");
        isRunning = true;
        
        // Start background services
        startReminderScheduler();
        startTaskMonitor();
        startSpeechRecognition();
        
        logger.info("JARVIS Core started successfully");
    }
    
    /**
     * Stop JARVIS core services
     */
    public void stop() {
        if (!isRunning) {
            logger.warning("JARVIS Core is not running");
            return;
        }
        
        logger.info("Stopping JARVIS Core...");
        isRunning = false;
        
        // Shutdown executor service
        executorService.shutdown();
        
        logger.info("JARVIS Core stopped");
    }
    
    /**
     * Process voice command
     * @param voiceInput the voice input to process
     * @return CompletableFuture with the result
     */
    public CompletableFuture<String> processVoiceCommand(String voiceInput) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Convert speech to text
                String text = speechService.speechToText(voiceInput);
                
                // Process with NLP
                Command command = nlpService.processIntent(text);
                
                // Execute command
                return executeCommand(command);
                
            } catch (Exception e) {
                logger.severe("Error processing voice command: " + e.getMessage());
                return "Sorry, I couldn't process that command. Please try again.";
            }
        }, executorService);
    }
    
    /**
     * Process text command
     * @param textInput the text input to process
     * @return CompletableFuture with the result
     */
    public CompletableFuture<String> processTextCommand(String textInput) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Process with NLP
                Command command = nlpService.processIntent(textInput);
                
                // Execute command
                return executeCommand(command);
                
            } catch (Exception e) {
                logger.severe("Error processing text command: " + e.getMessage());
                return "Sorry, I couldn't process that command. Please try again.";
            }
        }, executorService);
    }
    
    /**
     * Execute a command
     * @param command the command to execute
     * @return result of command execution
     */
    private String executeCommand(Command command) {
        try {
            command.execute();
            
            String result = "";
            switch (command.getType()) {
                case TASK_CREATE -> result = taskUseCase.createTask(command.getPayload());
                case TASK_UPDATE -> result = taskUseCase.updateTask(command.getPayload());
                case TASK_DELETE -> result = taskUseCase.deleteTask(command.getPayload());
                case TASK_LIST -> result = taskUseCase.listTasks();
                case REMINDER_SET -> result = reminderUseCase.setReminder(command.getPayload());
                case REMINDER_CANCEL -> result = reminderUseCase.cancelReminder(command.getPayload());
                case SYSTEM_INFO -> result = getSystemInfo();
                case VOICE_COMMAND -> result = processVoiceCommand(command.getPayload()).join();
                case CUSTOM_ACTION -> result = commandUseCase.executeCustomAction(command.getPayload());
            }
            
            command.complete(result);
            return result;
            
        } catch (Exception e) {
            String error = "Error executing command: " + e.getMessage();
            command.fail(error);
            logger.severe(error);
            return error;
        }
    }
    
    /**
     * Get system information
     * @return system info string
     */
    private String getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return String.format("System Status: Memory used: %d MB, Free: %d MB, Total: %d MB",
                usedMemory / 1024 / 1024,
                freeMemory / 1024 / 1024,
                totalMemory / 1024 / 1024);
    }
    
    /**
     * Start reminder scheduler
     */
    private void startReminderScheduler() {
        CompletableFuture.runAsync(() -> {
            while (isRunning) {
                try {
                    List<Reminder> dueReminders = reminderUseCase.getDueReminders();
                    
                    for (Reminder reminder : dueReminders) {
                        reminder.trigger();
                        reminderUseCase.updateReminder(reminder);
                        
                        // Show notification
                        notificationService.showNotification(
                            reminder.getTitle(),
                            reminder.getMessage()
                        );
                        
                        // Speak reminder if enabled
                        speechService.textToSpeech(reminder.getMessage());
                    }
                    
                    Thread.sleep(30000); // Check every 30 seconds
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.severe("Error in reminder scheduler: " + e.getMessage());
                }
            }
        }, executorService);
    }
    
    /**
     * Start task monitor
     */
    private void startTaskMonitor() {
        CompletableFuture.runAsync(() -> {
            while (isRunning) {
                try {
                    List<Task> overdueTasks = taskUseCase.getOverdueTasks();
                    
                    for (Task task : overdueTasks) {
                        notificationService.showNotification(
                            "Overdue Task",
                            "Task '" + task.getTitle() + "' is overdue!"
                        );
                    }
                    
                    Thread.sleep(60000); // Check every minute
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.severe("Error in task monitor: " + e.getMessage());
                }
            }
        }, executorService);
    }
    
    /**
     * Start speech recognition
     */
    private void startSpeechRecognition() {
        CompletableFuture.runAsync(() -> {
            while (isRunning) {
                try {
                    // This would integrate with the speech service
                    // to continuously listen for voice commands
                    Thread.sleep(1000);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.severe("Error in speech recognition: " + e.getMessage());
                }
            }
        }, executorService);
    }
    
    /**
     * Check if JARVIS is running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
} 