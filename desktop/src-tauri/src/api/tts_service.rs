use crate::api::{ApiClient, ApiResponse};
use crate::api::models::{TtsRequest, TtsResponse};
use anyhow::Result;

/// Клиент для TTS (Text-to-Speech) сервиса
pub struct TtsServiceClient {
    client: ApiClient,
}

impl TtsServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Синтезировать речь из текста
    pub async fn synthesize_speech(&self, request: TtsRequest) -> Result<ApiResponse<TtsResponse>> {
        self.client.post("/api/v1/tts/synthesize", &request).await
    }

    /// Получить быстрый синтез речи (упрощенный запрос)
    pub async fn quick_synthesize(&self, text: &str, voice: &str) -> Result<ApiResponse<TtsResponse>> {
        let request = TtsRequest {
            text: text.to_string(),
            voice: voice.to_string(),
            speed: 1.0,
            pitch: 1.0,
            volume: 1.0,
            format: "wav".to_string(),
        };
        self.synthesize_speech(request).await
    }

    /// Начать streaming синтез речи
    pub async fn start_streaming_synthesis(&self, session_id: &str, voice: &str) -> Result<ApiResponse<String>> {
        let data = serde_json::json!({
            "session_id": session_id,
            "voice": voice,
            "streaming": true
        });
        self.client.post("/api/v1/tts/streaming/start", &data).await
    }

    /// Отправить текст для streaming синтеза
    pub async fn send_streaming_text(&self, session_id: &str, text: &str) -> Result<ApiResponse<TtsResponse>> {
        let data = serde_json::json!({
            "session_id": session_id,
            "text": text,
            "timestamp": chrono::Utc::now().timestamp_millis()
        });
        self.client.post("/api/v1/tts/streaming/text", &data).await
    }

    /// Завершить streaming синтез
    pub async fn end_streaming_synthesis(&self, session_id: &str) -> Result<ApiResponse<TtsResponse>> {
        self.client.post(&format!("/api/v1/tts/streaming/{}/end", session_id), &serde_json::Value::Null).await
    }

    /// Получить список доступных голосов
    pub async fn get_available_voices(&self) -> Result<ApiResponse<Vec<serde_json::Value>>> {
        self.client.get("/api/v1/tts/voices").await
    }

    /// Получить информацию о голосе
    pub async fn get_voice_info(&self, voice_name: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/tts/voices/{}", voice_name)).await
    }

    /// Получить поддерживаемые форматы аудио
    pub async fn get_supported_formats(&self) -> Result<ApiResponse<Vec<String>>> {
        self.client.get("/api/v1/tts/formats").await
    }

    /// Предварительно загрузить голос
    pub async fn preload_voice(&self, voice_name: &str) -> Result<ApiResponse<String>> {
        let data = serde_json::json!({
            "voice": voice_name
        });
        self.client.post("/api/v1/tts/preload", &data).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
