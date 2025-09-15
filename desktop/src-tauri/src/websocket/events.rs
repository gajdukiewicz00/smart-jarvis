use super::{WebSocketMessage, ConnectionState};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Типы событий WebSocket
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum WebSocketEventType {
    ConnectionEstablished,
    ConnectionLost,
    ConnectionError,
    MessageReceived,
    MessageSent,
    HeartbeatReceived,
    HeartbeatTimeout,
    ReconnectionAttempt,
    ReconnectionFailed,
    ReconnectionSuccess,
}

/// WebSocket событие
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WebSocketEvent {
    pub event_type: WebSocketEventType,
    pub timestamp: u64,
    pub data: HashMap<String, serde_json::Value>,
    pub correlation_id: Option<String>,
}

impl WebSocketEvent {
    pub fn new(event_type: WebSocketEventType, data: HashMap<String, serde_json::Value>) -> Self {
        Self {
            event_type,
            timestamp: chrono::Utc::now().timestamp_millis() as u64,
            data,
            correlation_id: Some(uuid::Uuid::new_v4().to_string()),
        }
    }

    /// Создать событие установки соединения
    pub fn connection_established(url: &str) -> Self {
        let mut data = HashMap::new();
        data.insert("url".to_string(), serde_json::Value::String(url.to_string()));
        Self::new(WebSocketEventType::ConnectionEstablished, data)
    }

    /// Создать событие потери соединения
    pub fn connection_lost(reason: &str) -> Self {
        let mut data = HashMap::new();
        data.insert("reason".to_string(), serde_json::Value::String(reason.to_string()));
        Self::new(WebSocketEventType::ConnectionLost, data)
    }

    /// Создать событие ошибки соединения
    pub fn connection_error(error: &str) -> Self {
        let mut data = HashMap::new();
        data.insert("error".to_string(), serde_json::Value::String(error.to_string()));
        Self::new(WebSocketEventType::ConnectionError, data)
    }

    /// Создать событие получения сообщения
    pub fn message_received(message: &WebSocketMessage) -> Self {
        let mut data = HashMap::new();
        data.insert("message_type".to_string(), serde_json::Value::String(message.message_type.clone()));
        data.insert("data".to_string(), message.data.clone());
        data.insert("message_timestamp".to_string(), serde_json::Value::Number(message.timestamp.into()));
        Self::new(WebSocketEventType::MessageReceived, data)
    }

    /// Создать событие отправки сообщения
    pub fn message_sent(message: &WebSocketMessage) -> Self {
        let mut data = HashMap::new();
        data.insert("message_type".to_string(), serde_json::Value::String(message.message_type.clone()));
        data.insert("data".to_string(), message.data.clone());
        data.insert("message_timestamp".to_string(), serde_json::Value::Number(message.timestamp.into()));
        Self::new(WebSocketEventType::MessageSent, data)
    }

    /// Создать событие получения heartbeat
    pub fn heartbeat_received() -> Self {
        let data = HashMap::new();
        Self::new(WebSocketEventType::HeartbeatReceived, data)
    }

    /// Создать событие timeout heartbeat
    pub fn heartbeat_timeout(timeout_ms: u64) -> Self {
        let mut data = HashMap::new();
        data.insert("timeout_ms".to_string(), serde_json::Value::Number(timeout_ms.into()));
        Self::new(WebSocketEventType::HeartbeatTimeout, data)
    }

    /// Создать событие попытки переподключения
    pub fn reconnection_attempt(attempt: u32, url: &str) -> Self {
        let mut data = HashMap::new();
        data.insert("attempt".to_string(), serde_json::Value::Number(attempt.into()));
        data.insert("url".to_string(), serde_json::Value::String(url.to_string()));
        Self::new(WebSocketEventType::ReconnectionAttempt, data)
    }

    /// Создать событие неудачного переподключения
    pub fn reconnection_failed(attempt: u32, error: &str) -> Self {
        let mut data = HashMap::new();
        data.insert("attempt".to_string(), serde_json::Value::Number(attempt.into()));
        data.insert("error".to_string(), serde_json::Value::String(error.to_string()));
        Self::new(WebSocketEventType::ReconnectionFailed, data)
    }

    /// Создать событие успешного переподключения
    pub fn reconnection_success(attempt: u32, url: &str) -> Self {
        let mut data = HashMap::new();
        data.insert("attempt".to_string(), serde_json::Value::Number(attempt.into()));
        data.insert("url".to_string(), serde_json::Value::String(url.to_string()));
        Self::new(WebSocketEventType::ReconnectionSuccess, data)
    }
}

/// Обработчик событий WebSocket
pub struct WebSocketEventHandler {
    event_sender: Option<tokio::sync::mpsc::UnboundedSender<WebSocketEvent>>,
    event_receiver: Option<tokio::sync::mpsc::UnboundedReceiver<WebSocketEvent>>,
}

impl WebSocketEventHandler {
    pub fn new() -> Self {
        let (tx, rx) = tokio::sync::mpsc::unbounded_channel();
        
        Self {
            event_sender: Some(tx),
            event_receiver: Some(rx),
        }
    }

    /// Отправить событие
    pub fn send_event(&self, event: WebSocketEvent) -> Result<(), String> {
        if let Some(ref sender) = self.event_sender {
            sender.send(event).map_err(|e| format!("Failed to send event: {}", e))?;
            Ok(())
        } else {
            Err("Event handler is not initialized".to_string())
        }
    }

    /// Получить receiver для событий
    pub fn get_event_receiver(&mut self) -> Option<tokio::sync::mpsc::UnboundedReceiver<WebSocketEvent>> {
        self.event_receiver.take()
    }

    /// Обработать изменение состояния соединения
    pub fn handle_connection_state_change(&self, old_state: &ConnectionState, new_state: &ConnectionState) -> Result<(), String> {
        match (old_state, new_state) {
            (_, ConnectionState::Connected) => {
                if let ConnectionState::Error(error) = old_state {
                    self.send_event(WebSocketEvent::connection_error(error))?;
                } else {
                    self.send_event(WebSocketEvent::connection_established("ws://localhost:8080/ws"))?;
                }
            }
            (ConnectionState::Connected, ConnectionState::Disconnected) => {
                self.send_event(WebSocketEvent::connection_lost("Normal disconnect"))?;
            }
            (ConnectionState::Connected, ConnectionState::Error(error)) => {
                self.send_event(WebSocketEvent::connection_lost(error))?;
            }
            (_, ConnectionState::Reconnecting) => {
                self.send_event(WebSocketEvent::reconnection_attempt(1, "ws://localhost:8080/ws"))?;
            }
            _ => {}
        }
        Ok(())
    }

    /// Обработать получение сообщения
    pub fn handle_message_received(&self, message: &WebSocketMessage) -> Result<(), String> {
        self.send_event(WebSocketEvent::message_received(message))
    }

    /// Обработать отправку сообщения
    pub fn handle_message_sent(&self, message: &WebSocketMessage) -> Result<(), String> {
        self.send_event(WebSocketEvent::message_sent(message))
    }

    /// Обработать получение heartbeat
    pub fn handle_heartbeat_received(&self) -> Result<(), String> {
        self.send_event(WebSocketEvent::heartbeat_received())
    }

    /// Обработать timeout heartbeat
    pub fn handle_heartbeat_timeout(&self, timeout_ms: u64) -> Result<(), String> {
        self.send_event(WebSocketEvent::heartbeat_timeout(timeout_ms))
    }

    /// Обработать попытку переподключения
    pub fn handle_reconnection_attempt(&self, attempt: u32, url: &str) -> Result<(), String> {
        self.send_event(WebSocketEvent::reconnection_attempt(attempt, url))
    }

    /// Обработать неудачное переподключение
    pub fn handle_reconnection_failed(&self, attempt: u32, error: &str) -> Result<(), String> {
        self.send_event(WebSocketEvent::reconnection_failed(attempt, error))
    }

    /// Обработать успешное переподключение
    pub fn handle_reconnection_success(&self, attempt: u32, url: &str) -> Result<(), String> {
        self.send_event(WebSocketEvent::reconnection_success(attempt, url))
    }
}

impl Default for WebSocketEventHandler {
    fn default() -> Self {
        Self::new()
    }
}
