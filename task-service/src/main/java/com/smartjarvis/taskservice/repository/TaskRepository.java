package com.smartjarvis.taskservice.repository;

import com.smartjarvis.taskservice.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    // Find by status
    List<Task> findByStatus(Task.TaskStatus status);
    
    // Find by priority
    List<Task> findByPriority(Task.TaskPriority priority);
    
    // Find by category
    List<Task> findByCategory(String category);
    
    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);
    
    // Find tasks due soon (within next 24 hours)
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :now AND :soon AND t.status != 'COMPLETED'")
    List<Task> findTasksDueSoon(@Param("now") LocalDateTime now, @Param("soon") LocalDateTime soon);
    
    // Find tasks by date range
    @Query("SELECT t FROM Task t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Task> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    // Find tasks by title containing
    List<Task> findByTitleContainingIgnoreCase(String title);
    
    // Find tasks by description containing
    List<Task> findByDescriptionContainingIgnoreCase(String description);
    
    // Count by status
    long countByStatus(Task.TaskStatus status);
    
    // Count by priority
    long countByPriority(Task.TaskPriority priority);
    
    // Find completed tasks
    List<Task> findByStatusOrderByCompletedAtDesc(Task.TaskStatus status);
    
    // Find tasks by multiple statuses
    List<Task> findByStatusIn(List<Task.TaskStatus> statuses);
    
    // Find tasks with no due date
    List<Task> findByDueDateIsNull();
    
    // Find tasks with due date
    List<Task> findByDueDateIsNotNull();
} 