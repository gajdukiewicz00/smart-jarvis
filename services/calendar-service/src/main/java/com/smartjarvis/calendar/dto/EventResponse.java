package com.smartjarvis.calendar.dto;

import com.smartjarvis.calendar.domain.EventPriority;
import com.smartjarvis.calendar.domain.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for calendar events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    
    private String id;
    private String userId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private EventStatus status;
    private EventPriority priority;
    private String category;
    private boolean isAllDay;
    private boolean isToday;
    private boolean isUpcoming;
    private long durationMinutes;
    private LocalDateTime createdAt;
}
