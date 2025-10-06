package com.smartjarvis.calendar.repository;

import com.smartjarvis.calendar.domain.CalendarEvent;
import com.smartjarvis.calendar.domain.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for CalendarEvent entities
 */
@Repository
public interface CalendarEventRepository extends MongoRepository<CalendarEvent, String> {

    /**
     * Find events by user ID with pagination
     */
    Page<CalendarEvent> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);

    /**
     * Find events by user and date range
     */
    @Query("{'userId': ?0, 'startTime': {'$gte': ?1, '$lte': ?2}}")
    List<CalendarEvent> findByUserIdAndDateRange(String userId, LocalDateTime from, LocalDateTime to);

    /**
     * Find events for today
     */
    @Query("{'userId': ?0, 'startTime': {'$gte': ?1, '$lt': ?2}, 'status': {'$ne': 'CANCELLED'}}")
    List<CalendarEvent> findTodayEvents(String userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Find upcoming events (next 7 days)
     */
    @Query("{'userId': ?0, 'startTime': {'$gte': ?1, '$lt': ?2}, 'status': {'$ne': 'CANCELLED'}}")
    List<CalendarEvent> findUpcomingEvents(String userId, LocalDateTime from, LocalDateTime to);

    /**
     * Find events happening now
     */
    @Query("{'userId': ?0, 'startTime': {'$lte': ?1}, 'endTime': {'$gte': ?1}, 'status': 'CONFIRMED'}")
    List<CalendarEvent> findCurrentEvents(String userId, LocalDateTime now);

    /**
     * Find events by status
     */
    List<CalendarEvent> findByUserIdAndStatusOrderByStartTimeAsc(String userId, EventStatus status);

    /**
     * Find events by category
     */
    List<CalendarEvent> findByUserIdAndCategoryOrderByStartTimeAsc(String userId, String category);

    /**
     * Find event by ID and user ID (security check)
     */
    Optional<CalendarEvent> findByIdAndUserId(String id, String userId);

    /**
     * Delete event by ID and user ID (security check)
     */
    void deleteByIdAndUserId(String id, String userId);

    /**
     * Check if event exists for user
     */
    boolean existsByIdAndUserId(String id, String userId);

    /**
     * Find conflicting events
     */
    @Query("{'userId': ?0, " +
           "'$or': [" +
           "  {'startTime': {'$gte': ?1, '$lt': ?2}}, " +
           "  {'endTime': {'$gt': ?1, '$lte': ?2}}, " +
           "  {'startTime': {'$lte': ?1}, 'endTime': {'$gte': ?2}}" +
           "], " +
           "'status': {'$ne': 'CANCELLED'}}")
    List<CalendarEvent> findConflictingEvents(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find events with reminders due
     */
    @Query("{'reminders': {'$elemMatch': {'isActive': true, 'sentAt': null}}, " +
           "'startTime': {'$gte': ?0, '$lte': ?1}}")
    List<CalendarEvent> findEventsWithDueReminders(LocalDateTime from, LocalDateTime to);

    /**
     * Find recurring events
     */
    @Query("{'userId': ?0, 'recurringPattern.isActive': true}")
    List<CalendarEvent> findRecurringEvents(String userId);

    /**
     * Count events by status for user
     */
    long countByUserIdAndStatus(String userId, EventStatus status);

    /**
     * Find events by location
     */
    List<CalendarEvent> findByUserIdAndLocationContainingIgnoreCaseOrderByStartTimeAsc(String userId, String location);
}
