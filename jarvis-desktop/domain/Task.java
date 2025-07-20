package com.smartjarvis.desktop.domain;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Task {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<TaskPriority> priority = new SimpleObjectProperty<>();
    private final ObjectProperty<TaskStatus> status = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dueDate = new SimpleObjectProperty<>();

    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set(id); }
    public StringProperty idProperty() { return id; }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public TaskPriority getPriority() { return priority.get(); }
    public void setPriority(TaskPriority priority) { this.priority.set(priority); }
    public ObjectProperty<TaskPriority> priorityProperty() { return priority; }

    public TaskStatus getStatus() { return status.get(); }
    public void setStatus(TaskStatus status) { this.status.set(status); }
    public ObjectProperty<TaskStatus> statusProperty() { return status; }

    public LocalDateTime getDueDate() { return dueDate.get(); }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate.set(dueDate); }
    public ObjectProperty<LocalDateTime> dueDateProperty() { return dueDate; }

    public enum TaskPriority { LOW, MEDIUM, HIGH, URGENT }
    public enum TaskStatus { PENDING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED }
} 