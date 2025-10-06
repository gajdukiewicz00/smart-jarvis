use serde::{Deserialize, Serialize};
use uuid::Uuid;
use chrono::{DateTime, Utc};

/// Базовый ответ от API
#[derive(Debug, Serialize, Deserialize)]
pub struct ApiResponse<T> {
    pub success: bool,
    pub message: String,
    pub data: Option<T>,
    pub error: Option<String>,
    pub correlation_id: Option<String>,
}

impl<T> ApiResponse<T> {
    pub fn success(data: T) -> Self {
        Self {
            success: true,
            message: "Success".to_string(),
            data: Some(data),
            error: None,
            correlation_id: Some(Uuid::new_v4().to_string()),
        }
    }

    pub fn error(message: String) -> Self {
        Self {
            success: false,
            message: "Error".to_string(),
            data: None,
            error: Some(message),
            correlation_id: Some(Uuid::new_v4().to_string()),
        }
    }
}

/// Базовое событие
#[derive(Debug, Serialize, Deserialize)]
pub struct BaseEvent {
    pub event_id: String,
    pub event_type: String,
    pub user_id: String,
    pub session_id: String,
    pub timestamp: DateTime<Utc>,
    pub correlation_id: Option<String>,
}

impl BaseEvent {
    pub fn new(event_type: String, user_id: String, session_id: String) -> Self {
        Self {
            event_id: Uuid::new_v4().to_string(),
            event_type,
            user_id,
            session_id,
            timestamp: Utc::now(),
            correlation_id: Some(Uuid::new_v4().to_string()),
        }
    }
}

/// Конфигурация API клиента
#[derive(Debug, Clone)]
pub struct ApiConfig {
    pub base_url: String,
    pub timeout_seconds: u64,
    pub retry_attempts: u32,
    pub api_key: Option<String>,
}

impl Default for ApiConfig {
    fn default() -> Self {
        Self {
            base_url: "http://localhost:8080".to_string(),
            timeout_seconds: 30,
            retry_attempts: 3,
            api_key: None,
        }
    }
}
