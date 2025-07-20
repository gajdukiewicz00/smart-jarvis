package com.smartjarvis.desktop.domain.repositories;

import com.smartjarvis.desktop.domain.Reminder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Reminder domain entity
 */
public interface ReminderRepository {
    
    /**
     * Save a reminder
     * @param reminder the reminder to save
     * @return the saved reminder
     */
    Reminder save(Reminder reminder);
    
    /**
     * Find a reminder by its ID
     * @param id the reminder ID
     * @return Optional containing the reminder if found
     */
    Optional<Reminder> findById(UUID id);
    
    /**
     * Find all reminders
     * @return list of all reminders
     */
    List<Reminder> findAll();
    
    /**
     * Find reminders by status
     * @param status the reminder status
     * @return list of reminders with the specified status
     */
    List<Reminder> findByStatus(Reminder.ReminderStatus status);
    
    /**
     * Find reminders by type
     * @param type the reminder type
     * @return list of reminders with the specified type
     */
    List<Reminder> findByType(Reminder.ReminderType type);
    
    /**
     * Find due reminders (scheduled time has passed)
     * @return list of due reminders
     */
    List<Reminder> findDueReminders();
    
    /**
     * Find reminders scheduled between two times
     * @param startTime the start time
     * @param endTime the end time
     * @return list of reminders in the time range
     */
    List<Reminder> findByScheduledTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find recurring reminders
     * @return list of recurring reminders
     */
    List<Reminder> findRecurringReminders();
    
    /**
     * Find reminders for a specific date
     * @param date the date to search for
     * @return list of reminders for the specified date
     */
    List<Reminder> findByDate(LocalDateTime date);
    
    /**
     * Delete a reminder by its ID
     * @param id the reminder ID to delete
     */
    void deleteById(UUID id);
    
    /**
     * Check if a reminder exists by ID
     * @param id the reminder ID
     * @return true if reminder exists, false otherwise
     */
    boolean existsById(UUID id);
    
    /**
     * Count total number of reminders
     * @return total reminder count
     */
    long count();
    
    /**
     * Count reminders by status
     * @param status the reminder status
     * @return count of reminders with the specified status
     */
    long countByStatus(Reminder.ReminderStatus status);
    
    /**
     * Delete all triggered reminders older than specified time
     * @param olderThan the cutoff time
     * @return number of deleted reminders
     */
    int deleteTriggeredRemindersOlderThan(LocalDateTime olderThan);
} 