package com.smartjarvis.desktop.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a command that can be executed by JARVIS
 */
public class Command {
    private final UUID id;
    private final String name;
    private final String description;
    private final CommandType type;
    private final String payload;
    private final LocalDateTime createdAt;
    private CommandStatus status;
    private String result;
    private LocalDateTime executedAt;

    public Command(String name, String description, CommandType type, String payload) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.status = CommandStatus.PENDING;
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CommandType getType() { return type; }
    public String getPayload() { return payload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public CommandStatus getStatus() { return status; }
    public String getResult() { return result; }
    public LocalDateTime getExecutedAt() { return executedAt; }

    // Business methods
    public void execute() {
        this.status = CommandStatus.EXECUTING;
    }

    public void complete(String result) {
        this.status = CommandStatus.COMPLETED;
        this.result = result;
        this.executedAt = LocalDateTime.now();
    }

    public void fail(String error) {
        this.status = CommandStatus.FAILED;
        this.result = error;
        this.executedAt = LocalDateTime.now();
    }

    public boolean isExecutable() {
        return status == CommandStatus.PENDING;
    }

    @Override
    public String toString() {
        return "Command{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                '}';
    }

    public enum CommandType {
        TASK_CREATE,
        TASK_UPDATE,
        TASK_DELETE,
        TASK_LIST,
        REMINDER_SET,
        REMINDER_CANCEL,
        SYSTEM_INFO,
        VOICE_COMMAND,
        CUSTOM_ACTION
    }

    public enum CommandStatus {
        PENDING,
        EXECUTING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
} 