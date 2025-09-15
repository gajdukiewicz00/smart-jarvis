pub mod client;
pub mod models;
pub mod voice_gateway;
pub mod stt_service;
pub mod nlu_service;
pub mod dm_service;
pub mod tts_service;
pub mod todo_service;
pub mod money_service;
pub mod calendar_service;
pub mod memory_service;

// Re-export для удобства
pub use client::ApiClient;
pub use models::*;

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Базовый ответ от API
#[derive(Debug, Serialize, Deserialize)]
pub struct ApiResponse<T> {
    pub success: bool,
    pub message: String,
    pub data: Option<T>,
    pub error: Option<String>,
}

impl<T> ApiResponse<T> {
    pub fn success(data: T) -> Self {
        Self {
            success: true,
            message: "Success".to_string(),
            data: Some(data),
            error: None,
        }
    }

    pub fn error(message: String) -> Self {
        Self {
            success: false,
            message: "Error".to_string(),
            data: None,
            error: Some(message),
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

/// Типы событий для Kafka
#[derive(Debug, Serialize, Deserialize)]
pub enum EventType {
    AudioIncoming,
    AudioProcessed,
    IntentRecognized,
    DialogStateChanged,
    TaskCreated,
    TaskUpdated,
    TaskCompleted,
    CalendarEventCreated,
    MoneyTransactionAdded,
    SystemCommandExecuted,
}

/// Базовое событие
#[derive(Debug, Serialize, Deserialize)]
pub struct BaseEvent {
    pub event_id: String,
    pub event_type: EventType,
    pub user_id: String,
    pub session_id: String,
    pub timestamp: u64,
    pub correlation_id: Option<String>,
}

/// Результат выполнения команды
#[derive(Debug, Serialize, Deserialize)]
pub struct CommandResult {
    pub command_id: String,
    pub success: bool,
    pub message: String,
    pub data: Option<serde_json::Value>,
    pub execution_time_ms: u64,
}
