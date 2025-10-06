use crate::api::{ApiClient, ApiResponse};
use crate::api::models::{TodoTask, CreateTaskRequest, UpdateTaskRequest};
use anyhow::Result;

/// Клиент для Todo сервиса
pub struct TodoServiceClient {
    client: ApiClient,
}

impl TodoServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Создать новую задачу
    pub async fn create_task(&self, user_id: &str, request: CreateTaskRequest) -> Result<ApiResponse<TodoTask>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "task": request
        });
        self.client.post("/api/v1/todos", &data).await
    }

    /// Получить задачу по ID
    pub async fn get_task(&self, task_id: &str) -> Result<ApiResponse<TodoTask>> {
        self.client.get(&format!("/api/v1/todos/{}", task_id)).await
    }

    /// Получить все задачи пользователя
    pub async fn get_user_tasks(&self, user_id: &str) -> Result<ApiResponse<Vec<TodoTask>>> {
        self.client.get(&format!("/api/v1/todos/user/{}", user_id)).await
    }

    /// Получить задачи по статусу
    pub async fn get_tasks_by_status(&self, user_id: &str, status: &str) -> Result<ApiResponse<Vec<TodoTask>>> {
        self.client.get(&format!("/api/v1/todos/user/{}/status/{}", user_id, status)).await
    }

    /// Получить задачи по приоритету
    pub async fn get_tasks_by_priority(&self, user_id: &str, priority: &str) -> Result<ApiResponse<Vec<TodoTask>>> {
        self.client.get(&format!("/api/v1/todos/user/{}/priority/{}", user_id, priority)).await
    }

    /// Обновить задачу
    pub async fn update_task(&self, task_id: &str, request: UpdateTaskRequest) -> Result<ApiResponse<TodoTask>> {
        self.client.put(&format!("/api/v1/todos/{}", task_id), &request).await
    }

    /// Удалить задачу
    pub async fn delete_task(&self, task_id: &str) -> Result<ApiResponse<String>> {
        self.client.delete(&format!("/api/v1/todos/{}", task_id)).await
    }

    /// Отметить задачу как выполненную
    pub async fn complete_task(&self, task_id: &str) -> Result<ApiResponse<TodoTask>> {
        let request = UpdateTaskRequest {
            title: None,
            description: None,
            status: Some(crate::api::models::TaskStatus::Completed),
            priority: None,
            due_date: None,
        };
        self.update_task(task_id, request).await
    }

    /// Получить задачи на сегодня
    pub async fn get_today_tasks(&self, user_id: &str) -> Result<ApiResponse<Vec<TodoTask>>> {
        self.client.get(&format!("/api/v1/todos/user/{}/today", user_id)).await
    }

    /// Получить просроченные задачи
    pub async fn get_overdue_tasks(&self, user_id: &str) -> Result<ApiResponse<Vec<TodoTask>>> {
        self.client.get(&format!("/api/v1/todos/user/{}/overdue", user_id)).await
    }

    /// Поиск задач
    pub async fn search_tasks(&self, user_id: &str, query: &str) -> Result<ApiResponse<Vec<TodoTask>>> {
        self.client.get(&format!("/api/v1/todos/user/{}/search?q={}", user_id, query)).await
    }

    /// Получить статистику задач
    pub async fn get_task_statistics(&self, user_id: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/todos/user/{}/statistics", user_id)).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
