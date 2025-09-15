pub mod capture;
pub mod playback;
pub mod processing;
pub mod visualization;

use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tokio::sync::RwLock;
use cpal::traits::{HostTrait, DeviceTrait};

/// Конфигурация аудио
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AudioConfig {
    pub sample_rate: u32,
    pub channels: u16,
    pub buffer_size: u32,
    pub device_name: Option<String>,
    pub quality: AudioQuality,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum AudioQuality {
    Low,    // 16kHz, 16-bit
    Medium, // 44.1kHz, 16-bit
    High,   // 48kHz, 24-bit
}

impl Default for AudioConfig {
    fn default() -> Self {
        Self {
            sample_rate: 44100,
            channels: 1,
            buffer_size: 1024,
            device_name: None,
            quality: AudioQuality::Medium,
        }
    }
}

/// Аудио устройство
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AudioDevice {
    pub name: String,
    pub is_default: bool,
    pub is_input: bool,
    pub is_output: bool,
    pub sample_rates: Vec<u32>,
    pub channels: u16,
}

/// Аудио данные
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AudioData {
    pub samples: Vec<f32>,
    pub sample_rate: u32,
    pub channels: u16,
    pub duration_ms: u64,
}

impl AudioData {
    pub fn new(samples: Vec<f32>, sample_rate: u32, channels: u16) -> Self {
        let duration_ms = (samples.len() as u64 * 1000) / (sample_rate as u64 * channels as u64);
        Self {
            samples,
            sample_rate,
            channels,
            duration_ms,
        }
    }

    pub fn to_bytes(&self) -> Vec<u8> {
        // Конвертируем f32 в bytes для передачи
        let mut bytes = Vec::new();
        for sample in &self.samples {
            let sample_bytes = sample.to_le_bytes();
            bytes.extend_from_slice(&sample_bytes);
        }
        bytes
    }

    pub fn from_bytes(bytes: &[u8], sample_rate: u32, channels: u16) -> Self {
        let mut samples = Vec::new();
        for chunk in bytes.chunks(4) {
            if chunk.len() == 4 {
                let sample = f32::from_le_bytes([chunk[0], chunk[1], chunk[2], chunk[3]]);
                samples.push(sample);
            }
        }
        Self::new(samples, sample_rate, channels)
    }
}

/// Состояние аудио системы
#[derive(Debug, Clone)]
pub enum AudioState {
    Stopped,
    Recording,
    Playing,
    Processing,
    Error(String),
}

/// Менеджер аудио системы
pub struct AudioManager {
    config: Arc<RwLock<AudioConfig>>,
    state: Arc<RwLock<AudioState>>,
    devices: Arc<RwLock<Vec<AudioDevice>>>,
    is_initialized: bool,
}

impl AudioManager {
    pub fn new() -> Self {
        Self {
            config: Arc::new(RwLock::new(AudioConfig::default())),
            state: Arc::new(RwLock::new(AudioState::Stopped)),
            devices: Arc::new(RwLock::new(Vec::new())),
            is_initialized: false,
        }
    }

    /// Инициализировать аудио систему
    pub async fn initialize(&mut self) -> Result<(), String> {
        if self.is_initialized {
            return Ok(());
        }

        // Обновляем состояние
        {
            let mut state = self.state.write().await;
            *state = AudioState::Processing;
        }

        // Сканируем доступные устройства
        match self.scan_devices().await {
            Ok(_) => {
                self.is_initialized = true;
                let mut state = self.state.write().await;
                *state = AudioState::Stopped;
                Ok(())
            }
            Err(e) => {
                let mut state = self.state.write().await;
                *state = AudioState::Error(e.clone());
                Err(e)
            }
        }
    }

    /// Сканировать доступные аудио устройства
    async fn scan_devices(&self) -> Result<(), String> {
        let mut devices = Vec::new();
        
        // Получаем список устройств через cpal
        let host = cpal::default_host();
        
        // Входные устройства
        if let Ok(input_devices) = host.input_devices() {
            for device in input_devices {
                if let Ok(name) = device.name() {
                    let is_default = device.name().unwrap_or_default() == 
                        host.default_input_device().map(|d| d.name().unwrap_or_default()).unwrap_or_default();
                    
                    let device_info = AudioDevice {
                        name: name.clone(),
                        is_default,
                        is_input: true,
                        is_output: false,
                        sample_rates: vec![44100, 48000], // Заглушка
                        channels: 1,
                    };
                    devices.push(device_info);
                }
            }
        }

        // Выходные устройства
        if let Ok(output_devices) = host.output_devices() {
            for device in output_devices {
                if let Ok(name) = device.name() {
                    let is_default = device.name().unwrap_or_default() == 
                        host.default_output_device().map(|d| d.name().unwrap_or_default()).unwrap_or_default();
                    
                    let device_info = AudioDevice {
                        name: name.clone(),
                        is_default,
                        is_input: false,
                        is_output: true,
                        sample_rates: vec![44100, 48000], // Заглушка
                        channels: 2,
                    };
                    devices.push(device_info);
                }
            }
        }

        {
            let mut device_list = self.devices.write().await;
            *device_list = devices;
        }

        Ok(())
    }

    /// Получить список устройств
    pub async fn get_devices(&self) -> Vec<AudioDevice> {
        let devices = self.devices.read().await;
        devices.clone()
    }

    /// Получить текущую конфигурацию
    pub async fn get_config(&self) -> AudioConfig {
        let config = self.config.read().await;
        config.clone()
    }

    /// Обновить конфигурацию
    pub async fn update_config(&self, new_config: AudioConfig) -> Result<(), String> {
        let mut config = self.config.write().await;
        *config = new_config;
        Ok(())
    }

    /// Получить текущее состояние
    pub async fn get_state(&self) -> AudioState {
        let state = self.state.read().await;
        state.clone()
    }

    /// Проверить, инициализирована ли система
    pub fn is_initialized(&self) -> bool {
        self.is_initialized
    }
}
