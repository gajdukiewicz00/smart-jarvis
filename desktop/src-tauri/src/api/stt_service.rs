use crate::api::{ApiClient, ApiResponse};
use crate::api::models::{AudioChunk, TranscriptionResult};
use anyhow::Result;

/// Клиент для STT (Speech-to-Text) сервиса
pub struct SttServiceClient {
    client: ApiClient,
}

impl SttServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Отправить аудио для транскрипции
    pub async fn transcribe_audio(&self, chunk: AudioChunk) -> Result<ApiResponse<TranscriptionResult>> {
        self.client.post("/api/v1/stt/transcribe", &chunk).await
    }

    /// Получить результат транскрипции по ID
    pub async fn get_transcription(&self, transcription_id: &str) -> Result<ApiResponse<TranscriptionResult>> {
        self.client.get(&format!("/api/v1/stt/transcription/{}", transcription_id)).await
    }

    /// Начать streaming транскрипцию
    pub async fn start_streaming_transcription(&self, session_id: &str, user_id: &str) -> Result<ApiResponse<String>> {
        let data = serde_json::json!({
            "session_id": session_id,
            "user_id": user_id,
            "streaming": true
        });
        self.client.post("/api/v1/stt/streaming/start", &data).await
    }

    /// Отправить аудио чанк для streaming транскрипции
    pub async fn send_streaming_chunk(&self, session_id: &str, audio_data: Vec<u8>) -> Result<ApiResponse<TranscriptionResult>> {
        let data = serde_json::json!({
            "session_id": session_id,
            "audio_data": audio_data,
            "timestamp": chrono::Utc::now().timestamp_millis()
        });
        self.client.post("/api/v1/stt/streaming/chunk", &data).await
    }

    /// Завершить streaming транскрипцию
    pub async fn end_streaming_transcription(&self, session_id: &str) -> Result<ApiResponse<TranscriptionResult>> {
        self.client.post(&format!("/api/v1/stt/streaming/{}/end", session_id), &serde_json::Value::Null).await
    }

    /// Получить поддерживаемые языки
    pub async fn get_supported_languages(&self) -> Result<ApiResponse<Vec<String>>> {
        self.client.get("/api/v1/stt/languages").await
    }

    /// Установить язык для транскрипции
    pub async fn set_language(&self, session_id: &str, language: &str) -> Result<ApiResponse<String>> {
        let data = serde_json::json!({
            "language": language
        });
        self.client.post(&format!("/api/v1/stt/session/{}/language", session_id), &data).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
