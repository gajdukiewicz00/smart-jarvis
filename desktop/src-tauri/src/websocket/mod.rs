pub mod client;
pub mod events;

pub use client::WebSocketClientImpl;
pub use events::{WebSocketEvent, WebSocketEventHandler, WebSocketEventType};

use serde::{Deserialize, Serialize};
use tokio::sync::mpsc;
use std::sync::Arc;
use tokio::sync::RwLock;

/// Состояние WebSocket соединения
#[derive(Debug, Clone, PartialEq)]
pub enum ConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Reconnecting,
    Error(String),
}

/// WebSocket сообщение
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WebSocketMessage {
    pub message_type: String,
    pub data: serde_json::Value,
    pub timestamp: u64,
    pub correlation_id: Option<String>,
}

impl WebSocketMessage {
    pub fn new(message_type: String, data: serde_json::Value) -> Self {
        Self {
            message_type,
            data,
            timestamp: chrono::Utc::now().timestamp_millis() as u64,
            correlation_id: Some(uuid::Uuid::new_v4().to_string()),
        }
    }
}

/// Конфигурация WebSocket клиента
#[derive(Debug, Clone)]
pub struct WebSocketConfig {
    pub url: String,
    pub reconnect_interval: u64,
    pub max_reconnect_attempts: u32,
    pub heartbeat_interval: u64,
    pub timeout: u64,
}

impl Default for WebSocketConfig {
    fn default() -> Self {
        Self {
            url: "ws://localhost:8080/ws".to_string(),
            reconnect_interval: 5000, // 5 секунд
            max_reconnect_attempts: 10,
            heartbeat_interval: 30000, // 30 секунд
            timeout: 10000, // 10 секунд
        }
    }
}

/// WebSocket клиент (обертка над WebSocketClientImpl)
pub struct WebSocketClient {
    client: WebSocketClientImpl,
    event_handler: WebSocketEventHandler,
}

impl WebSocketClient {
    pub fn new(config: WebSocketConfig) -> Self {
        Self {
            client: WebSocketClientImpl::new(config),
            event_handler: WebSocketEventHandler::new(),
        }
    }

    /// Подключиться к WebSocket серверу
    pub async fn connect(&mut self) -> Result<(), String> {
        self.client.connect().await
    }

    /// Отключиться от WebSocket сервера
    pub async fn disconnect(&mut self) -> Result<(), String> {
        self.client.disconnect().await
    }

    /// Отправить сообщение
    pub async fn send_message(&self, message: WebSocketMessage) -> Result<(), String> {
        // Отправляем событие о том, что сообщение отправлено
        if let Err(e) = self.event_handler.handle_message_sent(&message) {
            log::warn!("Failed to send message sent event: {}", e);
        }
        
        self.client.send_message(message).await
    }

    /// Получить текущее состояние соединения
    pub async fn get_connection_state(&self) -> ConnectionState {
        self.client.get_connection_state().await
    }

    /// Проверить, подключен ли клиент
    pub async fn is_connected(&self) -> bool {
        self.client.is_connected().await
    }

    /// Получить receiver для получения сообщений
    pub fn get_message_receiver(&mut self) -> Option<mpsc::UnboundedReceiver<WebSocketMessage>> {
        self.client.get_message_receiver()
    }

    /// Получить receiver для получения событий
    pub fn get_event_receiver(&mut self) -> Option<mpsc::UnboundedReceiver<WebSocketEvent>> {
        self.event_handler.get_event_receiver()
    }

    /// Получить количество попыток переподключения
    pub async fn get_reconnect_attempts(&self) -> u32 {
        self.client.get_reconnect_attempts().await
    }

    /// Сбросить счетчик попыток переподключения
    pub async fn reset_reconnect_attempts(&self) {
        self.client.reset_reconnect_attempts().await;
    }

    /// Получить время последнего heartbeat
    pub async fn get_last_heartbeat(&self) -> std::time::Instant {
        self.client.get_last_heartbeat().await
    }

}
