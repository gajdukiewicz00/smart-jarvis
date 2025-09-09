package com.smartjarvis.todo.repository;

import com.smartjarvis.todo.domain.Todo;
import com.smartjarvis.todo.domain.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Todo entities
 */
@Repository
public interface TodoRepository extends MongoRepository<Todo, String> {

    /**
     * Find todos by user ID
     */
    List<Todo> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find todos by user ID with pagination
     */
    Page<Todo> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find todos by user ID and status
     */
    List<Todo> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, TodoStatus status);

    /**
     * Find pending todos for user
     */
    @Query("{'userId': ?0, 'status': 'PENDING'}")
    List<Todo> findPendingTodosByUserId(String userId);

    /**
     * Find todos due today
     */
    @Query("{'userId': ?0, 'dueDate': {'$gte': ?1, '$lt': ?2}, 'status': {'$ne': 'COMPLETED'}}")
    List<Todo> findTodosDueToday(String userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Find overdue todos
     */
    @Query("{'userId': ?0, 'dueDate': {'$lt': ?1}, 'status': {'$ne': 'COMPLETED'}}")
    List<Todo> findOverdueTodos(String userId, LocalDateTime now);

    /**
     * Count todos by status for user
     */
    long countByUserIdAndStatus(String userId, TodoStatus status);

    /**
     * Find todo by ID and user ID (security check)
     */
    Optional<Todo> findByIdAndUserId(String id, String userId);

    /**
     * Delete todo by ID and user ID (security check)
     */
    void deleteByIdAndUserId(String id, String userId);

    /**
     * Check if todo exists for user
     */
    boolean existsByIdAndUserId(String id, String userId);
}
