use crate::api::{ApiClient, ApiResponse};
use crate::api::models::IntentResult;
use anyhow::Result;
use std::collections::HashMap;

/// Клиент для NLU (Natural Language Understanding) сервиса
pub struct NluServiceClient {
    client: ApiClient,
}

impl NluServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Распознать интент из текста
    pub async fn recognize_intent(&self, text: &str, session_id: &str, user_id: &str) -> Result<ApiResponse<IntentResult>> {
        let data = serde_json::json!({
            "text": text,
            "session_id": session_id,
            "user_id": user_id,
            "timestamp": chrono::Utc::now().timestamp_millis()
        });
        self.client.post("/api/v1/nlu/intent", &data).await
    }

    /// Извлечь сущности из текста
    pub async fn extract_entities(&self, text: &str, session_id: &str) -> Result<ApiResponse<HashMap<String, String>>> {
        let data = serde_json::json!({
            "text": text,
            "session_id": session_id
        });
        self.client.post("/api/v1/nlu/entities", &data).await
    }

    /// Заполнить слоты в диалоге
    pub async fn fill_slots(&self, session_id: &str, current_slots: HashMap<String, serde_json::Value>) -> Result<ApiResponse<HashMap<String, serde_json::Value>>> {
        let data = serde_json::json!({
            "session_id": session_id,
            "current_slots": current_slots
        });
        self.client.post("/api/v1/nlu/slots", &data).await
    }

    /// Получить список поддерживаемых интентов
    pub async fn get_supported_intents(&self) -> Result<ApiResponse<Vec<String>>> {
        self.client.get("/api/v1/nlu/intents").await
    }

    /// Получить детали интента
    pub async fn get_intent_details(&self, intent_name: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/nlu/intents/{}", intent_name)).await
    }

    /// Обновить контекст пользователя
    pub async fn update_user_context(&self, user_id: &str, context: HashMap<String, serde_json::Value>) -> Result<ApiResponse<String>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "context": context,
            "timestamp": chrono::Utc::now().timestamp_millis()
        });
        self.client.post("/api/v1/nlu/context", &data).await
    }

    /// Получить контекст пользователя
    pub async fn get_user_context(&self, user_id: &str) -> Result<ApiResponse<HashMap<String, serde_json::Value>>> {
        self.client.get(&format!("/api/v1/nlu/context/{}", user_id)).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
