use super::{WebSocketConfig, WebSocketMessage, ConnectionState};
use tokio_tungstenite::{connect_async, tungstenite::Message};
use futures_util::{SinkExt, StreamExt};
use tokio::sync::{mpsc, RwLock};
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};
use tokio::time::{sleep, Duration, timeout};
use serde_json;
use log::{info, error, warn, debug};
use base64::Engine;

/// Полноценный WebSocket клиент с поддержкой переподключения и heartbeat
pub struct WebSocketClientImpl {
    config: WebSocketConfig,
    state: Arc<RwLock<ConnectionState>>,
    message_sender: Option<mpsc::UnboundedSender<WebSocketMessage>>,
    message_receiver: Option<mpsc::UnboundedReceiver<WebSocketMessage>>,
    is_running: Arc<AtomicBool>,
    reconnect_attempts: Arc<RwLock<u32>>,
    last_heartbeat: Arc<RwLock<std::time::Instant>>,
}

impl WebSocketClientImpl {
    pub fn new(config: WebSocketConfig) -> Self {
        let (tx, rx) = mpsc::unbounded_channel();
        
        Self {
            config,
            state: Arc::new(RwLock::new(ConnectionState::Disconnected)),
            message_sender: Some(tx),
            message_receiver: Some(rx),
            is_running: Arc::new(AtomicBool::new(false)),
            reconnect_attempts: Arc::new(RwLock::new(0)),
            last_heartbeat: Arc::new(RwLock::new(std::time::Instant::now())),
        }
    }

    /// Подключиться к WebSocket серверу
    pub async fn connect(&mut self) -> Result<(), String> {
        if self.is_running.load(Ordering::Relaxed) {
            return Err("WebSocket client is already running".to_string());
        }

        info!("Connecting to WebSocket server: {}", self.config.url);
        
        // Обновляем состояние
        {
            let mut state = self.state.write().await;
            *state = ConnectionState::Connecting;
        }

        // Запускаем WebSocket соединение в отдельном потоке
        let config = self.config.clone();
        let state = Arc::clone(&self.state);
        let is_running = Arc::clone(&self.is_running);
        let reconnect_attempts = Arc::clone(&self.reconnect_attempts);
        let last_heartbeat = Arc::clone(&self.last_heartbeat);
        let message_sender = self.message_sender.clone();
        
        tokio::spawn(async move {
            Self::run_websocket_connection(
                config, 
                state, 
                is_running, 
                reconnect_attempts,
                last_heartbeat,
                message_sender
            ).await;
        });

        self.is_running.store(true, Ordering::Relaxed);
        Ok(())
    }

    /// Отключиться от WebSocket сервера
    pub async fn disconnect(&mut self) -> Result<(), String> {
        if !self.is_running.load(Ordering::Relaxed) {
            return Ok(());
        }

        info!("Disconnecting from WebSocket server");
        self.is_running.store(false, Ordering::Relaxed);
        
        // Обновляем состояние
        {
            let mut state = self.state.write().await;
            *state = ConnectionState::Disconnected;
        }

        Ok(())
    }

    /// Отправить сообщение
    pub async fn send_message(&self, message: WebSocketMessage) -> Result<(), String> {
        if let Some(ref sender) = self.message_sender {
            sender.send(message).map_err(|e| format!("Failed to send message: {}", e))?;
            Ok(())
        } else {
            Err("WebSocket client is not initialized".to_string())
        }
    }

    /// Получить текущее состояние соединения
    pub async fn get_connection_state(&self) -> ConnectionState {
        let state = self.state.read().await;
        state.clone()
    }

    /// Проверить, подключен ли клиент
    pub async fn is_connected(&self) -> bool {
        matches!(*self.state.read().await, ConnectionState::Connected)
    }

    /// Получить receiver для получения сообщений
    pub fn get_message_receiver(&mut self) -> Option<mpsc::UnboundedReceiver<WebSocketMessage>> {
        self.message_receiver.take()
    }

    /// Получить количество попыток переподключения
    pub async fn get_reconnect_attempts(&self) -> u32 {
        let attempts = self.reconnect_attempts.read().await;
        *attempts
    }

    /// Сбросить счетчик попыток переподключения
    pub async fn reset_reconnect_attempts(&self) {
        let mut attempts = self.reconnect_attempts.write().await;
        *attempts = 0;
    }

    /// Получить время последнего heartbeat
    pub async fn get_last_heartbeat(&self) -> std::time::Instant {
        let heartbeat = self.last_heartbeat.read().await;
        *heartbeat
    }

    /// Основной цикл WebSocket соединения
    async fn run_websocket_connection(
        config: WebSocketConfig,
        state: Arc<RwLock<ConnectionState>>,
        is_running: Arc<AtomicBool>,
        reconnect_attempts: Arc<RwLock<u32>>,
        last_heartbeat: Arc<RwLock<std::time::Instant>>,
        message_sender: Option<mpsc::UnboundedSender<WebSocketMessage>>,
    ) {
        let mut heartbeat_task = None;
        
        while is_running.load(Ordering::Relaxed) {
            match Self::establish_connection(&config).await {
                Ok((ws_stream, ws_sink)) => {
                    info!("WebSocket connection established successfully");
                    
                    // Соединение установлено
                    {
                        let mut state = state.write().await;
                        *state = ConnectionState::Connected;
                    }
                    
                    // Сбрасываем счетчик попыток переподключения
                    {
                        let mut attempts = reconnect_attempts.write().await;
                        *attempts = 0;
                    }
                    
                    // Обновляем время последнего heartbeat
                    {
                        let mut heartbeat = last_heartbeat.write().await;
                        *heartbeat = std::time::Instant::now();
                    }
                    
                    // Запускаем heartbeat task
                    let heartbeat_config = config.clone();
                    let heartbeat_state = Arc::clone(&state);
                    let heartbeat_last = Arc::clone(&last_heartbeat);
                    let heartbeat_is_running = Arc::clone(&is_running);
                    
                    heartbeat_task = Some(tokio::spawn(async move {
                        Self::heartbeat_task(
                            heartbeat_config,
                            heartbeat_state,
                            heartbeat_last,
                            heartbeat_is_running,
                        ).await;
                    }));
                    
                    // Обрабатываем сообщения
                    let result = Self::handle_websocket_messages(
                        ws_stream,
                        ws_sink,
                        state.clone(),
                        is_running.clone(),
                        message_sender.clone(),
                        last_heartbeat.clone(),
                    ).await;
                    
                    // Останавливаем heartbeat task
                    if let Some(task) = heartbeat_task {
                        task.abort();
                    }
                    
                    match result {
                        Ok(_) => {
                            info!("WebSocket connection closed normally");
                        }
                        Err(e) => {
                            error!("WebSocket connection error: {}", e);
                        }
                    }
                }
                Err(e) => {
                    error!("Failed to establish WebSocket connection: {}", e);
                    
                    // Ошибка соединения
                    {
                        let mut state = state.write().await;
                        *state = ConnectionState::Error(e.clone());
                    }
                    
                    // Увеличиваем счетчик попыток переподключения
                    {
                        let mut attempts = reconnect_attempts.write().await;
                        *attempts += 1;
                        
                        if *attempts >= config.max_reconnect_attempts {
                            error!("Max reconnection attempts reached, giving up");
                            break;
                        }
                    }
                    
                    // Обновляем состояние на переподключение
                    {
                        let mut state = state.write().await;
                        *state = ConnectionState::Reconnecting;
                    }
                    
                    // Ждем перед повторной попыткой
                    warn!("Waiting {}ms before reconnection attempt", config.reconnect_interval);
                    sleep(Duration::from_millis(config.reconnect_interval)).await;
                }
            }
        }
        
        // Обновляем состояние на отключение
        {
            let mut state = state.write().await;
            *state = ConnectionState::Disconnected;
        }
        
        info!("WebSocket connection task finished");
    }

    /// Установка WebSocket соединения
    async fn establish_connection(config: &WebSocketConfig) -> Result<
        (
            futures_util::stream::SplitStream<tokio_tungstenite::WebSocketStream<tokio_tungstenite::MaybeTlsStream<tokio::net::TcpStream>>>,
            futures_util::stream::SplitSink<tokio_tungstenite::WebSocketStream<tokio_tungstenite::MaybeTlsStream<tokio::net::TcpStream>>, Message>
        ),
        String
    > {
        debug!("Attempting to connect to: {}", config.url);
        
        // Устанавливаем таймаут для подключения
        let connection_result = timeout(
            Duration::from_millis(config.timeout),
            connect_async(&config.url)
        ).await;
        
        match connection_result {
            Ok(Ok((ws_stream, _))) => {
                debug!("WebSocket connection established");
                let (ws_sink, ws_stream) = ws_stream.split();
                Ok((ws_stream, ws_sink))
            }
            Ok(Err(e)) => {
                Err(format!("WebSocket connection failed: {}", e))
            }
            Err(_) => {
                Err(format!("WebSocket connection timeout after {}ms", config.timeout))
            }
        }
    }

    /// Обработка WebSocket сообщений
    async fn handle_websocket_messages(
        mut ws_stream: futures_util::stream::SplitStream<tokio_tungstenite::WebSocketStream<tokio_tungstenite::MaybeTlsStream<tokio::net::TcpStream>>>,
        mut ws_sink: futures_util::stream::SplitSink<tokio_tungstenite::WebSocketStream<tokio_tungstenite::MaybeTlsStream<tokio::net::TcpStream>>, Message>,
        _state: Arc<RwLock<ConnectionState>>,
        is_running: Arc<AtomicBool>,
        message_sender: Option<mpsc::UnboundedSender<WebSocketMessage>>,
        last_heartbeat: Arc<RwLock<std::time::Instant>>,
    ) -> Result<(), String> {
        while is_running.load(Ordering::Relaxed) {
            match ws_stream.next().await {
                Some(Ok(Message::Text(text))) => {
                    debug!("Received text message: {}", text);
                    
                    // Обновляем время последнего heartbeat
                    {
                        let mut heartbeat = last_heartbeat.write().await;
                        *heartbeat = std::time::Instant::now();
                    }
                    
                    // Парсим сообщение
                    match serde_json::from_str::<WebSocketMessage>(&text) {
                        Ok(message) => {
                            // Отправляем сообщение через канал
                            if let Some(ref sender) = message_sender {
                                if let Err(e) = sender.send(message) {
                                    error!("Failed to send message to channel: {}", e);
                                }
                            }
                        }
                        Err(e) => {
                            warn!("Failed to parse WebSocket message: {}", e);
                        }
                    }
                }
                Some(Ok(Message::Binary(data))) => {
                    debug!("Received binary message: {} bytes", data.len());
                    
                    // Обновляем время последнего heartbeat
                    {
                        let mut heartbeat = last_heartbeat.write().await;
                        *heartbeat = std::time::Instant::now();
                    }
                    
                    // Обрабатываем бинарные данные
                    if let Some(ref sender) = message_sender {
                        let message = WebSocketMessage::new(
                            "binary".to_string(),
                            serde_json::Value::String(base64::engine::general_purpose::STANDARD.encode(&data))
                        );
                        if let Err(e) = sender.send(message) {
                            error!("Failed to send binary message to channel: {}", e);
                        }
                    }
                }
                Some(Ok(Message::Ping(data))) => {
                    debug!("Received ping, sending pong");
                    
                    // Обновляем время последнего heartbeat
                    {
                        let mut heartbeat = last_heartbeat.write().await;
                        *heartbeat = std::time::Instant::now();
                    }
                    
                    // Отправляем pong
                    if let Err(e) = ws_sink.send(Message::Pong(data)).await {
                        error!("Failed to send pong: {}", e);
                        return Err(format!("Failed to send pong: {}", e));
                    }
                }
                Some(Ok(Message::Pong(_))) => {
                    debug!("Received pong");
                    
                    // Обновляем время последнего heartbeat
                    {
                        let mut heartbeat = last_heartbeat.write().await;
                        *heartbeat = std::time::Instant::now();
                    }
                }
                Some(Ok(Message::Close(_))) => {
                    info!("WebSocket connection closed by server");
                    return Ok(());
                }
                Some(Ok(Message::Frame(_))) => {
                    debug!("Received raw frame message");
                }
                Some(Err(e)) => {
                    error!("WebSocket error: {}", e);
                    return Err(format!("WebSocket error: {}", e));
                }
                None => {
                    info!("WebSocket stream ended");
                    return Ok(());
                }
            }
        }
        
        Ok(())
    }

    /// Heartbeat задача для поддержания соединения
    async fn heartbeat_task(
        config: WebSocketConfig,
        state: Arc<RwLock<ConnectionState>>,
        last_heartbeat: Arc<RwLock<std::time::Instant>>,
        is_running: Arc<AtomicBool>,
    ) {
        let mut heartbeat_interval = tokio::time::interval(Duration::from_millis(config.heartbeat_interval));
        
        while is_running.load(Ordering::Relaxed) {
            heartbeat_interval.tick().await;
            
            // Проверяем, что соединение активно
            let current_state = {
                let state = state.read().await;
                state.clone()
            };
            
            if matches!(current_state, ConnectionState::Connected) {
                // Проверяем время последнего heartbeat
                let time_since_last_heartbeat = {
                    let heartbeat = last_heartbeat.read().await;
                    heartbeat.elapsed()
                };
                
                if time_since_last_heartbeat > Duration::from_millis(config.heartbeat_interval * 2) {
                    warn!("No heartbeat received for {}ms, connection may be dead", 
                          time_since_last_heartbeat.as_millis());
                    
                    // Обновляем состояние на ошибку
                    {
                        let mut state = state.write().await;
                        *state = ConnectionState::Error("Heartbeat timeout".to_string());
                    }
                } else {
                    debug!("Heartbeat check passed, last heartbeat: {}ms ago", 
                           time_since_last_heartbeat.as_millis());
                }
            }
        }
        
        debug!("Heartbeat task finished");
    }
}
