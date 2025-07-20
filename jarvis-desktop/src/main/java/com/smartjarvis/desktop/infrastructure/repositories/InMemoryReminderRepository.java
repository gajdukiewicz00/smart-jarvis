package com.smartjarvis.desktop.infrastructure.repositories;

import com.smartjarvis.desktop.domain.Reminder;
import com.smartjarvis.desktop.domain.repositories.ReminderRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of Reminder Repository
 */
public class InMemoryReminderRepository implements ReminderRepository {
    
    private final Map<UUID, Reminder> reminders = new ConcurrentHashMap<>();
    
    @Override
    public Reminder save(Reminder reminder) {
        reminders.put(reminder.getId(), reminder);
        return reminder;
    }
    
    @Override
    public Optional<Reminder> findById(UUID id) {
        return Optional.ofNullable(reminders.get(id));
    }
    
    @Override
    public List<Reminder> findAll() {
        return new ArrayList<>(reminders.values());
    }
    
    @Override
    public List<Reminder> findByStatus(Reminder.ReminderStatus status) {
        return reminders.values().stream()
                .filter(reminder -> reminder.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reminder> findByType(Reminder.ReminderType type) {
        return reminders.values().stream()
                .filter(reminder -> reminder.getType() == type)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reminder> findDueReminders() {
        return reminders.values().stream()
                .filter(reminder -> reminder.isDue())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reminder> findByScheduledTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return reminders.values().stream()
                .filter(reminder -> {
                    LocalDateTime scheduledTime = reminder.getScheduledTime();
                    return scheduledTime != null && 
                           scheduledTime.isAfter(startTime) && 
                           scheduledTime.isBefore(endTime);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reminder> findRecurringReminders() {
        return reminders.values().stream()
                .filter(reminder -> reminder.isRecurring())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reminder> findByDate(LocalDateTime date) {
        return reminders.values().stream()
                .filter(reminder -> {
                    LocalDateTime scheduledTime = reminder.getScheduledTime();
                    return scheduledTime != null && 
                           scheduledTime.toLocalDate().equals(date.toLocalDate());
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(UUID id) {
        reminders.remove(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return reminders.containsKey(id);
    }
    
    @Override
    public long count() {
        return reminders.size();
    }
    
    @Override
    public long countByStatus(Reminder.ReminderStatus status) {
        return reminders.values().stream()
                .filter(reminder -> reminder.getStatus() == status)
                .count();
    }
    
    @Override
    public int deleteTriggeredRemindersOlderThan(LocalDateTime olderThan) {
        List<UUID> toDelete = reminders.values().stream()
                .filter(reminder -> reminder.getStatus() == Reminder.ReminderStatus.TRIGGERED &&
                                  reminder.getTriggeredAt() != null &&
                                  reminder.getTriggeredAt().isBefore(olderThan))
                .map(Reminder::getId)
                .collect(Collectors.toList());
        
        toDelete.forEach(reminders::remove);
        return toDelete.size();
    }
} 