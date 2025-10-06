use crate::api::{ApiClient, ApiResponse};
use crate::api::models::{MemoryEntry, CreateMemoryRequest, SearchMemoryRequest, SearchMemoryResponse};
use anyhow::Result;
use std::collections::HashMap;

/// Клиент для Memory сервиса
pub struct MemoryServiceClient {
    client: ApiClient,
}

impl MemoryServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Создать новую запись в памяти
    pub async fn create_memory(&self, user_id: &str, session_id: &str, request: CreateMemoryRequest) -> Result<ApiResponse<MemoryEntry>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "session_id": session_id,
            "memory": request
        });
        self.client.post("/api/v1/memory/entries", &data).await
    }

    /// Получить запись памяти по ID
    pub async fn get_memory(&self, memory_id: &str) -> Result<ApiResponse<MemoryEntry>> {
        self.client.get(&format!("/api/v1/memory/entries/{}", memory_id)).await
    }

    /// Получить все записи пользователя
    pub async fn get_user_memories(&self, user_id: &str) -> Result<ApiResponse<Vec<MemoryEntry>>> {
        self.client.get(&format!("/api/v1/memory/entries/user/{}", user_id)).await
    }

    /// Получить записи сессии
    pub async fn get_session_memories(&self, user_id: &str, session_id: &str) -> Result<ApiResponse<Vec<MemoryEntry>>> {
        self.client.get(&format!("/api/v1/memory/entries/user/{}/session/{}", user_id, session_id)).await
    }

    /// Поиск в памяти
    pub async fn search_memories(&self, user_id: &str, request: SearchMemoryRequest) -> Result<ApiResponse<SearchMemoryResponse>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "search": request
        });
        self.client.post("/api/v1/memory/search", &data).await
    }

    /// Быстрый поиск в памяти
    pub async fn quick_search(&self, user_id: &str, query: &str, limit: Option<u32>) -> Result<ApiResponse<Vec<MemoryEntry>>> {
        let url = if let Some(limit) = limit {
            format!("/api/v1/memory/search/{}/quick?q={}&limit={}", user_id, query, limit)
        } else {
            format!("/api/v1/memory/search/{}/quick?q={}", user_id, query)
        };
        self.client.get(&url).await
    }

    /// Обновить запись памяти
    pub async fn update_memory(&self, memory_id: &str, content: &str, context: HashMap<String, serde_json::Value>) -> Result<ApiResponse<MemoryEntry>> {
        let data = serde_json::json!({
            "content": content,
            "context": context
        });
        self.client.put(&format!("/api/v1/memory/entries/{}", memory_id), &data).await
    }

    /// Удалить запись памяти
    pub async fn delete_memory(&self, memory_id: &str) -> Result<ApiResponse<String>> {
        self.client.delete(&format!("/api/v1/memory/entries/{}", memory_id)).await
    }

    /// Очистить память пользователя
    pub async fn clear_user_memory(&self, user_id: &str) -> Result<ApiResponse<String>> {
        self.client.delete(&format!("/api/v1/memory/entries/user/{}", user_id)).await
    }

    /// Очистить память сессии
    pub async fn clear_session_memory(&self, user_id: &str, session_id: &str) -> Result<ApiResponse<String>> {
        self.client.delete(&format!("/api/v1/memory/entries/user/{}/session/{}", user_id, session_id)).await
    }

    /// Получить контекст пользователя
    pub async fn get_user_context(&self, user_id: &str) -> Result<ApiResponse<HashMap<String, serde_json::Value>>> {
        self.client.get(&format!("/api/v1/memory/context/{}", user_id)).await
    }

    /// Обновить контекст пользователя
    pub async fn update_user_context(&self, user_id: &str, context: HashMap<String, serde_json::Value>) -> Result<ApiResponse<String>> {
        self.client.put(&format!("/api/v1/memory/context/{}", user_id), &context).await
    }

    /// Получить статистику памяти
    pub async fn get_memory_statistics(&self, user_id: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/memory/statistics/{}", user_id)).await
    }

    /// Экспортировать память
    pub async fn export_memory(&self, user_id: &str, format: &str) -> Result<ApiResponse<String>> {
        self.client.get(&format!("/api/v1/memory/export/{}/{}", user_id, format)).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
