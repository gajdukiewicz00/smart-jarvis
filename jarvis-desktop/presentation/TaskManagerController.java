package com.smartjarvis.desktop.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import com.smartjarvis.desktop.domain.Task;
import com.smartjarvis.desktop.infrastructure.services.TaskServiceClient;

import java.time.LocalDate;
import java.util.List;

public class TaskManagerController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<Task.TaskPriority> priorityBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private Label statusLabel;

    private final TaskServiceClient taskService = new TaskServiceClient();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(cell -> cell.getValue().titleProperty());
        descriptionColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        priorityColumn.setCellValueFactory(cell -> cell.getValue().priorityProperty().asString());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty().asString());
        dueDateColumn.setCellValueFactory(cell -> cell.getValue().dueDateProperty().asString());

        taskTable.setItems(taskList);

        priorityBox.setItems(FXCollections.observableArrayList(Task.TaskPriority.values()));
        priorityBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Task.TaskPriority object) {
                return object == null ? "" : object.name();
            }
            @Override
            public Task.TaskPriority fromString(String string) {
                return Task.TaskPriority.valueOf(string);
            }
        });

        taskTable.setOnMouseClicked(this::onTableClick);
        refreshTaskList();
    }

    private void onTableClick(MouseEvent event) {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            titleField.setText(selected.getTitle());
            descriptionField.setText(selected.getDescription());
            priorityBox.setValue(selected.getPriority());
            dueDatePicker.setValue(selected.getDueDate() != null ? selected.getDueDate().toLocalDate() : null);
        }
    }

    @FXML
    private void onAddTask() {
        try {
            Task task = new Task();
            task.setTitle(titleField.getText());
            task.setDescription(descriptionField.getText());
            task.setPriority(priorityBox.getValue());
            if (dueDatePicker.getValue() != null) {
                task.setDueDate(dueDatePicker.getValue().atStartOfDay());
            }
            Task created = taskService.createTask(task);
            taskList.add(created);
            clearForm();
            statusLabel.setText("Task added!");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a task to update.");
            return;
        }
        try {
            selected.setTitle(titleField.getText());
            selected.setDescription(descriptionField.getText());
            selected.setPriority(priorityBox.getValue());
            if (dueDatePicker.getValue() != null) {
                selected.setDueDate(dueDatePicker.getValue().atStartOfDay());
            }
            Task updated = taskService.updateTask(selected);
            int idx = taskList.indexOf(selected);
            taskList.set(idx, updated);
            clearForm();
            statusLabel.setText("Task updated!");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a task to delete.");
            return;
        }
        try {
            taskService.deleteTask(selected.getId());
            taskList.remove(selected);
            clearForm();
            statusLabel.setText("Task deleted!");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onCompleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a task to complete.");
            return;
        }
        try {
            Task completed = taskService.completeTask(selected.getId());
            int idx = taskList.indexOf(selected);
            taskList.set(idx, completed);
            clearForm();
            statusLabel.setText("Task completed!");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        refreshTaskList();
    }

    private void refreshTaskList() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            taskList.setAll(tasks);
            statusLabel.setText("Task list refreshed.");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        priorityBox.setValue(null);
        dueDatePicker.setValue(null);
    }
} 