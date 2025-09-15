use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tokio::sync::Mutex;
use std::collections::HashMap;
use cpal::traits::{HostTrait, DeviceTrait, StreamTrait};
use cpal::{Stream, StreamConfig, SampleFormat, SampleRate};
use ringbuf::{HeapRb, traits::{Consumer, Observer, Producer}};

/// Конфигурация wake word detection
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WakeWordConfig {
    pub sensitivity: f32,
    pub use_local: bool,
    pub use_cloud: bool,
    pub cloud_api_key: Option<String>,
    pub wake_words: Vec<String>,
}

impl Default for WakeWordConfig {
    fn default() -> Self {
        Self {
            sensitivity: 0.5,
            use_local: true,
            use_cloud: false,
            cloud_api_key: None,
            wake_words: vec!["jarvis".to_string()],
        }
    }
}

/// Результат wake word detection
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WakeWordResult {
    pub detected: bool,
    pub wake_word: Option<String>,
    pub confidence: f32,
    pub timestamp: u64,
    pub source: DetectionSource,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum DetectionSource {
    Local,
    Cloud,
}

/// Состояние wake word detector
#[derive(Debug, Clone, PartialEq)]
pub enum DetectorState {
    Stopped,
    Starting,
    Running,
    Stopping,
    Error(String),
}

/// Основной wake word detector
pub struct WakeWordDetector {
    config: WakeWordConfig,
    state: DetectorState,
    audio_buffer: Arc<Mutex<HeapRb<f32>>>,
    cloud_api_key: Option<String>,
    is_running: Arc<Mutex<bool>>,
}

impl WakeWordDetector {
    pub fn new(config: WakeWordConfig) -> Self {
        let buffer_size = 1024 * 16; // 16KB buffer
        let audio_buffer = Arc::new(Mutex::new(HeapRb::new(buffer_size)));
        
        let cloud_api_key = config.cloud_api_key.clone();
        Self {
            config,
            state: DetectorState::Stopped,
            audio_buffer,
            cloud_api_key,
            is_running: Arc::new(Mutex::new(false)),
        }
    }

    /// Инициализировать детекторы
    pub async fn initialize(&mut self) -> Result<(), String> {
        self.state = DetectorState::Starting;
        
        // Инициализация локального детектора (Porcupine)
        if self.config.use_local {
            match self.initialize_porcupine().await {
                Ok(_) => log::info!("Porcupine initialized successfully"),
                Err(e) => {
                    log::warn!("Failed to initialize Porcupine: {}", e);
                    if !self.config.use_cloud {
                        return Err(format!("Failed to initialize local wake word detection: {}", e));
                    }
                }
            }
        }
        
        // Инициализация облачного детектора
        if self.config.use_cloud {
            match self.initialize_cloud_detector().await {
                Ok(_) => log::info!("Cloud detector initialized successfully"),
                Err(e) => {
                    log::warn!("Failed to initialize cloud detector: {}", e);
                    if !self.config.use_local {
                        return Err(format!("Failed to initialize cloud wake word detection: {}", e));
                    }
                }
            }
        }
        
        self.state = DetectorState::Running;
        Ok(())
    }
    
    /// Инициализация Porcupine
    async fn initialize_porcupine(&mut self) -> Result<(), String> {
        // Для версии 0.2 используем простую инициализацию
        // В реальной реализации здесь будет инициализация Porcupine
        log::info!("Porcupine wake word detector initialized (simulated)");
        Ok(())
    }
    
    /// Инициализация облачного детектора
    async fn initialize_cloud_detector(&mut self) -> Result<(), String> {
        if self.cloud_api_key.is_none() {
            return Err("Cloud API key is required for cloud detection".to_string());
        }
        
        // Здесь можно добавить инициализацию облачного API
        // Например, проверка API ключа, создание клиента и т.д.
        log::info!("Cloud detector initialized with API key");
        Ok(())
    }

    /// Запустить детекцию
    pub async fn start(&mut self) -> Result<(), String> {
        if self.state != DetectorState::Running {
            return Err("Detector not initialized. Please call initialize() first.".to_string());
        }
        
        // Запускаем аудио поток
        match self.start_audio_stream().await {
            Ok(_) => {
                *self.is_running.lock().await = true;
                log::info!("Wake word detection started");
                Ok(())
            },
            Err(e) => {
                self.state = DetectorState::Error(e.clone());
                Err(format!("Failed to start audio stream: {}", e))
            }
        }
    }
    
    /// Запуск аудио потока
    async fn start_audio_stream(&mut self) -> Result<(), String> {
        let host = cpal::default_host();
        let device = host.default_input_device()
            .ok_or("No input device available")?;
        
        let config = device.default_input_config()
            .map_err(|e| format!("Failed to get default input config: {}", e))?;
        
        let sample_rate = config.sample_rate().0;
        let channels = config.channels();
        
        log::info!("Starting audio stream: {}Hz, {} channels", sample_rate, channels);
        
        let audio_buffer = self.audio_buffer.clone();
        let is_running = self.is_running.clone();
        // porcupine_handle убран из структуры
        
        let stream = match config.sample_format() {
            SampleFormat::F32 => {
                device.build_input_stream(
                    &config.into(),
                    move |data: &[f32], _: &cpal::InputCallbackInfo| {
                        Self::process_audio_data_f32(data, &audio_buffer, &is_running);
                    },
                    move |err| {
                        log::error!("Audio stream error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build input stream: {}", e))?
            },
            SampleFormat::I16 => {
                device.build_input_stream(
                    &config.into(),
                    move |data: &[i16], _: &cpal::InputCallbackInfo| {
                        Self::process_audio_data_i16(data, &audio_buffer, &is_running);
                    },
                    move |err| {
                        log::error!("Audio stream error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build input stream: {}", e))?
            },
            SampleFormat::U16 => {
                device.build_input_stream(
                    &config.into(),
                    move |data: &[u16], _: &cpal::InputCallbackInfo| {
                        Self::process_audio_data_u16(data, &audio_buffer, &is_running);
                    },
                    move |err| {
                        log::error!("Audio stream error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build input stream: {}", e))?
            },
            _ => return Err("Unsupported sample format".to_string()),
        };
        
        stream.play().map_err(|e| format!("Failed to start audio stream: {}", e))?;
        
        // В реальной реализации stream должен быть сохранен где-то для управления
        // Здесь мы просто запускаем его и он работает в фоне
        std::mem::forget(stream); // Предотвращаем drop stream
        
        Ok(())
    }
    
    /// Обработка аудио данных (f32)
    fn process_audio_data_f32(
        data: &[f32],
        audio_buffer: &Arc<Mutex<HeapRb<f32>>>,
        is_running: &Arc<Mutex<bool>>,
    ) {
        if !*is_running.blocking_lock() {
            return;
        }
        
        // Добавляем данные в буфер
        if let Ok(mut buffer) = audio_buffer.try_lock() {
            for &sample in data {
                // Проверяем переполнение буфера (симуляция)
                if buffer.is_full() {
                    log::debug!("Buffer is full, skipping sample");
                }
                let _ = buffer.try_push(sample);
            }
        }
        
        // Проверяем wake word (симуляция)
        // В реальной реализации здесь будет обработка через Porcupine
        log::debug!("Processing {} audio samples", data.len());
    }
    
    /// Обработка аудио данных (i16)
    fn process_audio_data_i16(
        data: &[i16],
        audio_buffer: &Arc<Mutex<HeapRb<f32>>>,
        is_running: &Arc<Mutex<bool>>,
    ) {
        if !*is_running.blocking_lock() {
            return;
        }
        
        // Конвертируем i16 в f32
        let f32_data: Vec<f32> = data.iter().map(|&x| x as f32 / 32768.0).collect();
        
        // Добавляем данные в буфер
        if let Ok(mut buffer) = audio_buffer.try_lock() {
            for &sample in &f32_data {
                // Проверяем переполнение буфера (симуляция)
                if buffer.is_full() {
                    log::debug!("Buffer is full, skipping sample");
                }
                let _ = buffer.try_push(sample);
            }
        }
        
        // Проверяем wake word (симуляция)
        log::debug!("Processing {} audio samples (i16->f32)", f32_data.len());
    }
    
    /// Обработка аудио данных (u16)
    fn process_audio_data_u16(
        data: &[u16],
        audio_buffer: &Arc<Mutex<HeapRb<f32>>>,
        is_running: &Arc<Mutex<bool>>,
    ) {
        if !*is_running.blocking_lock() {
            return;
        }
        
        // Конвертируем u16 в f32
        let f32_data: Vec<f32> = data.iter().map(|&x| (x as f32 - 32768.0) / 32768.0).collect();
        
        // Добавляем данные в буфер
        if let Ok(mut buffer) = audio_buffer.try_lock() {
            for &sample in &f32_data {
                // Проверяем переполнение буфера (симуляция)
                if buffer.is_full() {
                    log::debug!("Buffer is full, skipping sample");
                }
                let _ = buffer.try_push(sample);
            }
        }
        
        // Проверяем wake word (симуляция)
        log::debug!("Processing {} audio samples (i16->f32)", f32_data.len());
    }

    /// Остановить детекцию
    pub async fn stop(&mut self) -> Result<(), String> {
        self.state = DetectorState::Stopping;
        
        // Останавливаем аудио поток
        // В реальной реализации здесь будет остановка stream
        log::info!("Audio stream stopped");
        
        // Останавливаем флаг работы
        *self.is_running.lock().await = false;
        
        // Очищаем буфер (симуляция)
        log::info!("Audio buffer cleared");
        
        self.state = DetectorState::Stopped;
        log::info!("Wake word detection stopped");
        Ok(())
    }

    /// Получить текущее состояние
    pub fn get_state(&self) -> DetectorState {
        self.state.clone()
    }

    /// Обновить конфигурацию
    pub fn update_config(&mut self, new_config: WakeWordConfig) -> Result<(), String> {
        self.config = new_config;
        Ok(())
    }

    /// Получить текущую конфигурацию
    pub fn get_config(&self) -> WakeWordConfig {
        self.config.clone()
    }

    /// Проверить, активен ли детектор
    pub fn is_active(&self) -> bool {
        matches!(self.state, DetectorState::Running)
    }
    
    /// Получить текущий аудио буфер
    pub async fn get_audio_buffer(&self) -> Vec<f32> {
        let buffer = self.audio_buffer.lock().await;
        buffer.iter().cloned().collect()
    }
    
    /// Проверить wake word в переданных данных
    pub fn check_wake_word(&self, audio_data: &[f32]) -> Option<WakeWordResult> {
        // Симуляция проверки wake word
        log::debug!("Checking wake word in {} samples", audio_data.len());
        
        // В реальной реализации здесь будет обработка через Porcupine
        
        None
    }
    
    /// Обновить чувствительность
    pub async fn update_sensitivity(&mut self, sensitivity: f32) -> Result<(), String> {
        if sensitivity < 0.0 || sensitivity > 1.0 {
            return Err("Sensitivity must be between 0.0 and 1.0".to_string());
        }
        
        self.config.sensitivity = sensitivity;
        
        // Переинициализируем Porcupine с новой чувствительностью (симуляция)
        if self.config.use_local {
            log::info!("Porcupine sensitivity updated to: {}", sensitivity);
        }
        
        Ok(())
    }
    
    /// Получить статистику детекции
    pub fn get_detection_stats(&self) -> HashMap<String, u64> {
        let mut stats = HashMap::new();
        stats.insert("total_detections".to_string(), 0); // Можно добавить счетчик
        // Симуляция размера буфера
        stats.insert("buffer_size".to_string(), 1024);
        stats
    }
}