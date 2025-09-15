use crate::api::{ApiConfig, ApiResponse};
use reqwest::Client;
use std::time::Duration;
use anyhow::Result;
use uuid::Uuid;

/// Базовый HTTP клиент для коммуникации с Java сервисами
pub struct ApiClient {
    client: Client,
    config: ApiConfig,
}

impl ApiClient {
    pub fn new(config: ApiConfig) -> Result<Self> {
        let client = Client::builder()
            .timeout(Duration::from_secs(config.timeout_seconds))
            .build()?;

        Ok(Self { client, config })
    }

    /// Выполнить GET запрос
    pub async fn get<T>(&self, endpoint: &str) -> Result<ApiResponse<T>>
    where
        T: serde::de::DeserializeOwned,
    {
        let url = format!("{}/{}", self.config.base_url, endpoint.trim_start_matches('/'));
        
        let response = self.client
            .get(&url)
            .send()
            .await?;

        let status = response.status();
        let body = response.text().await?;

        if status.is_success() {
            let data: T = serde_json::from_str(&body)?;
            Ok(ApiResponse::success(data))
        } else {
            Ok(ApiResponse::error(format!("HTTP {}: {}", status, body)))
        }
    }

    /// Выполнить POST запрос
    pub async fn post<T, U>(&self, endpoint: &str, data: &T) -> Result<ApiResponse<U>>
    where
        T: serde::Serialize,
        U: serde::de::DeserializeOwned,
    {
        let url = format!("{}/{}", self.config.base_url, endpoint.trim_start_matches('/'));
        
        let response = self.client
            .post(&url)
            .json(data)
            .send()
            .await?;

        let status = response.status();
        let body = response.text().await?;

        if status.is_success() {
            let result: U = serde_json::from_str(&body)?;
            Ok(ApiResponse::success(result))
        } else {
            Ok(ApiResponse::error(format!("HTTP {}: {}", status, body)))
        }
    }

    /// Выполнить PUT запрос
    pub async fn put<T, U>(&self, endpoint: &str, data: &T) -> Result<ApiResponse<U>>
    where
        T: serde::Serialize,
        U: serde::de::DeserializeOwned,
    {
        let url = format!("{}/{}", self.config.base_url, endpoint.trim_start_matches('/'));
        
        let response = self.client
            .put(&url)
            .json(data)
            .send()
            .await?;

        let status = response.status();
        let body = response.text().await?;

        if status.is_success() {
            let result: U = serde_json::from_str(&body)?;
            Ok(ApiResponse::success(result))
        } else {
            Ok(ApiResponse::error(format!("HTTP {}: {}", status, body)))
        }
    }

    /// Выполнить DELETE запрос
    pub async fn delete<T>(&self, endpoint: &str) -> Result<ApiResponse<T>>
    where
        T: serde::de::DeserializeOwned,
    {
        let url = format!("{}/{}", self.config.base_url, endpoint.trim_start_matches('/'));
        
        let response = self.client
            .delete(&url)
            .send()
            .await?;

        let status = response.status();
        let body = response.text().await?;

        if status.is_success() {
            let result: T = serde_json::from_str(&body)?;
            Ok(ApiResponse::success(result))
        } else {
            Ok(ApiResponse::error(format!("HTTP {}: {}", status, body)))
        }
    }

    /// Отправить аудио поток
    pub async fn send_audio_stream(&self, endpoint: &str, audio_data: Vec<u8>) -> Result<ApiResponse<String>> {
        let url = format!("{}/{}", self.config.base_url, endpoint.trim_start_matches('/'));
        
        let response = self.client
            .post(&url)
            .header("Content-Type", "application/octet-stream")
            .body(audio_data)
            .send()
            .await?;

        let status = response.status();
        let body = response.text().await?;

        if status.is_success() {
            Ok(ApiResponse::success(body))
        } else {
            Ok(ApiResponse::error(format!("HTTP {}: {}", status, body)))
        }
    }

    /// Получить WebSocket URL для real-time коммуникации
    pub fn get_websocket_url(&self, endpoint: &str) -> String {
        let base_ws = self.config.base_url
            .replace("http://", "ws://")
            .replace("https://", "wss://");
        format!("{}/{}", base_ws, endpoint.trim_start_matches('/'))
    }

    /// Создать correlation ID для трассировки
    pub fn create_correlation_id() -> String {
        Uuid::new_v4().to_string()
    }

    /// Получить базовый URL
    pub fn get_base_url(&self) -> &str {
        &self.config.base_url
    }
}

impl Default for ApiClient {
    fn default() -> Self {
        Self::new(ApiConfig::default()).expect("Failed to create default API client")
    }
}
