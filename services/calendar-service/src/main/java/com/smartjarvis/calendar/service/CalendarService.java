package com.smartjarvis.calendar.service;

import com.smartjarvis.calendar.domain.CalendarEvent;
import com.smartjarvis.calendar.domain.EventStatus;
import com.smartjarvis.calendar.dto.CreateEventRequest;
import com.smartjarvis.calendar.dto.EventResponse;
import com.smartjarvis.calendar.exception.EventNotFoundException;
import com.smartjarvis.calendar.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calendar service with event management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final CalendarEventRepository eventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Create new event
     */
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: title='{}', userId='{}'", request.getTitle(), request.getUserId());

        // Check for conflicts
        List<CalendarEvent> conflicts = eventRepository.findConflictingEvents(
                request.getUserId(), request.getStartTime(), request.getEndTime());
        
        if (!conflicts.isEmpty()) {
            log.warn("Event conflicts detected: {} conflicts", conflicts.size());
            // For MVP, allow conflicts but log them
        }

        CalendarEvent event = CalendarEvent.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .category(request.getCategory())
                .attendees(request.getAttendees())
                .isAllDay(request.isAllDay())
                .build();

        CalendarEvent savedEvent = eventRepository.save(event);

        // Publish event created
        publishEventCreatedEvent(savedEvent);

        log.info("Event created successfully: id={}", savedEvent.getId());
        return mapToResponse(savedEvent);
    }

    /**
     * Get today's events
     */
    public List<EventResponse> getTodayEvents(String userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        List<CalendarEvent> events = eventRepository.findTodayEvents(userId, startOfDay, endOfDay);
        return events.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get upcoming events
     */
    public List<EventResponse> getUpcomingEvents(String userId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);
        
        List<CalendarEvent> events = eventRepository.findUpcomingEvents(userId, now, future);
        return events.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Map to response DTO
     */
    private EventResponse mapToResponse(CalendarEvent event) {
        return EventResponse.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .status(event.getStatus())
                .priority(event.getPriority())
                .category(event.getCategory())
                .isAllDay(event.isAllDay())
                .isToday(event.isToday())
                .isUpcoming(event.isUpcoming())
                .durationMinutes(event.getDurationMinutes())
                .createdAt(event.getCreatedAt())
                .build();
    }

    /**
     * Publish event created event
     */
    private void publishEventCreatedEvent(CalendarEvent event) {
        try {
            kafkaTemplate.send("calendar.event.created", event.getId(), event);
            log.debug("Event created event published: id={}", event.getId());
        } catch (Exception e) {
            log.error("Failed to publish event created event: id={}", event.getId(), e);
        }
    }
}
