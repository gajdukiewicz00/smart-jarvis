use crate::api::{ApiClient, ApiResponse};
use crate::api::models::AudioChunk;
use anyhow::Result;
use serde_json::Value;

/// Клиент для Voice Gateway сервиса
pub struct VoiceGatewayClient {
    client: ApiClient,
}

impl VoiceGatewayClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Отправить аудио чанк для обработки
    pub async fn send_audio_chunk(&self, chunk: AudioChunk) -> Result<ApiResponse<String>> {
        self.client.post("/api/v1/voice/audio", &chunk).await
    }

    /// Получить статус обработки аудио
    pub async fn get_processing_status(&self, session_id: &str) -> Result<ApiResponse<Value>> {
        self.client.get(&format!("/api/v1/voice/status/{}", session_id)).await
    }

    /// Начать новую сессию
    pub async fn start_session(&self, user_id: &str) -> Result<ApiResponse<String>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "timestamp": chrono::Utc::now().timestamp_millis()
        });
        self.client.post("/api/v1/voice/session/start", &data).await
    }

    /// Завершить сессию
    pub async fn end_session(&self, session_id: &str) -> Result<ApiResponse<String>> {
        self.client.post(&format!("/api/v1/voice/session/{}/end", session_id), &serde_json::Value::Null).await
    }

    /// Получить WebSocket URL для real-time аудио
    pub fn get_websocket_url(&self, session_id: &str) -> String {
        self.client.get_websocket_url(&format!("/ws/voice/{}", session_id))
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<Value>> {
        self.client.get("/health").await
    }
}
