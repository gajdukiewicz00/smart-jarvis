package com.smartjarvis.desktop.infrastructure.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartjarvis.desktop.domain.Task;

import java.net.http.*;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class TaskServiceClient {
    private static final String BASE_URL = "http://localhost:8080/api/v1/tasks";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Task> getAllTasks() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] tasks = objectMapper.readValue(response.body(), Task[].class);
        return Arrays.asList(tasks);
    }

    public Task createTask(Task task) throws Exception {
        String json = objectMapper.writeValueAsString(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), Task.class);
    }

    public Task updateTask(Task task) throws Exception {
        String json = objectMapper.writeValueAsString(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + task.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), Task.class);
    }

    public void deleteTask(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    public Task completeTask(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id + "/complete"))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), Task.class);
    }
} 