package com.smartjarvis.taskservice.domain.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Unit tests for Task domain entity
 */
class TaskTest {
    
    private Task task;
    private LocalDateTime futureDate;
    private LocalDateTime pastDate;
    
    @BeforeEach
    void setUp() {
        futureDate = LocalDateTime.now().plusDays(1);
        pastDate = LocalDateTime.now().minusDays(1);
        
        task = new Task(
            "Test Task",
            "Test Description",
            Task.TaskPriority.MEDIUM,
            "Test Category",
            futureDate
        );
    }
    
    @Test
    void testTaskCreation() {
        assertNotNull(task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(Task.TaskPriority.MEDIUM, task.getPriority());
        assertEquals(Task.TaskStatus.PENDING, task.getStatus());
        assertEquals("Test Category", task.getCategory());
        assertEquals(futureDate, task.getDueDate());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        assertNull(task.getCompletedAt());
    }
    
    @Test
    void testStartTask() {
        task.start();
        assertEquals(Task.TaskStatus.IN_PROGRESS, task.getStatus());
    }
    
    @Test
    void testPauseTask() {
        task.start();
        task.pause();
        assertEquals(Task.TaskStatus.PAUSED, task.getStatus());
    }
    
    @Test
    void testCompleteTask() {
        task.complete();
        assertEquals(Task.TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }
    
    @Test
    void testCancelTask() {
        task.cancel();
        assertEquals(Task.TaskStatus.CANCELLED, task.getStatus());
    }
    
    @Test
    void testCannotStartCompletedTask() {
        task.complete();
        assertThrows(IllegalStateException.class, () -> task.start());
    }
    
    @Test
    void testCannotCompleteCancelledTask() {
        task.cancel();
        assertThrows(IllegalStateException.class, () -> task.complete());
    }
    
    @Test
    void testIsOverdue() {
        Task overdueTask = new Task(
            "Overdue Task",
            "Description",
            Task.TaskPriority.HIGH,
            "Category",
            pastDate
        );
        
        assertTrue(overdueTask.isOverdue());
        assertFalse(task.isOverdue());
    }
    
    @Test
    void testIsDueSoon() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Task dueSoonTask = new Task(
            "Due Soon Task",
            "Description",
            Task.TaskPriority.HIGH,
            "Category",
            tomorrow
        );
        
        assertTrue(dueSoonTask.isDueSoon());
        
        // Create a task with a date further in the future (not due soon)
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);
        Task futureTask = new Task(
            "Future Task",
            "Description",
            Task.TaskPriority.HIGH,
            "Category",
            futureDate
        );
        
        assertFalse(futureTask.isDueSoon());
    }
    
    @Test
    void testIsUrgent() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Task urgentTask = new Task(
            "Urgent Task",
            "Description",
            Task.TaskPriority.HIGH,
            "Category",
            tomorrow
        );
        
        assertTrue(urgentTask.isUrgent());
        assertFalse(task.isUrgent()); // medium priority
    }
    
    @Test
    void testUpdateTask() {
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(2);
        task.update("Updated Title", "Updated Description", Task.TaskPriority.HIGH, "Updated Category", newDueDate);
        
        assertEquals("Updated Title", task.getTitle());
        assertEquals("Updated Description", task.getDescription());
        assertEquals(Task.TaskPriority.HIGH, task.getPriority());
        assertEquals("Updated Category", task.getCategory());
        assertEquals(newDueDate, task.getDueDate());
    }
    
    @Test
    void testIsValid() {
        assertTrue(task.isValid());
        
        Task invalidTask = new Task();
        invalidTask.setTitle("");
        assertFalse(invalidTask.isValid());
    }
    
    @Test
    void testEqualsAndHashCode() {
        Task task1 = new Task("Task 1", "Description", Task.TaskPriority.MEDIUM, "Category", futureDate);
        Task task2 = new Task("Task 2", "Description", Task.TaskPriority.MEDIUM, "Category", futureDate);
        
        // Different IDs should make them not equal
        assertNotEquals(task1, task2);
        
        // Same ID should make them equal
        task2.setId(task1.getId());
        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }
} 