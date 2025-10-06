use super::{AudioConfig, AudioData};
use cpal::traits::{DeviceTrait, HostTrait};
use cpal::{Device, SampleFormat, SampleRate, Stream, StreamConfig};
use std::sync::Arc;
use anyhow::Result;

/// Аудио воспроизведение
pub struct AudioPlayback {
    device: Option<Device>,
    stream: Option<Stream>,
    config: AudioConfig,
    state: Arc<std::sync::atomic::AtomicBool>,
    audio_queue: Arc<std::sync::Mutex<Vec<AudioData>>>,
}

impl AudioPlayback {
    pub fn new(config: AudioConfig) -> Self {
        Self {
            device: None,
            stream: None,
            config,
            state: Arc::new(std::sync::atomic::AtomicBool::new(false)),
            audio_queue: Arc::new(std::sync::Mutex::new(Vec::new())),
        }
    }

    /// Начать воспроизведение
    pub async fn start_playback(&mut self) -> Result<(), String> {
        if self.state.load(std::sync::atomic::Ordering::Relaxed) {
            return Err("Audio playback is already running".to_string());
        }

        // Получаем устройство вывода
        let host = cpal::default_host();
        let device = if let Some(ref device_name) = self.config.device_name {
            // Ищем устройство по имени
            host.output_devices()
                .map_err(|e| format!("Failed to get output devices: {}", e))?
                .find(|d| d.name().unwrap_or_default() == *device_name)
                .ok_or_else(|| format!("Device '{}' not found", device_name))?
        } else {
            // Используем устройство по умолчанию
            host.default_output_device()
                .ok_or("No default output device available")?
        };

        // Получаем конфигурацию устройства
        let device_config = device.default_output_config()
            .map_err(|e| format!("Failed to get device config: {}", e))?;

        // Создаем конфигурацию потока
        let stream_config = StreamConfig {
            channels: self.config.channels,
            sample_rate: SampleRate(self.config.sample_rate),
            buffer_size: cpal::BufferSize::Fixed(self.config.buffer_size),
        };

        // Создаем аудио поток
        let audio_queue = Arc::clone(&self.audio_queue);
        let stream = match device_config.sample_format() {
            SampleFormat::F32 => {
                device.build_output_stream(
                    &stream_config,
                    move |data: &mut [f32], _: &cpal::OutputCallbackInfo| {
                        Self::fill_audio_buffer(data, &audio_queue);
                    },
                    move |err| {
                        eprintln!("Audio playback error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build output stream: {}", e))?
            }
            SampleFormat::I16 => {
                device.build_output_stream(
                    &stream_config,
                    move |data: &mut [i16], _: &cpal::OutputCallbackInfo| {
                        Self::fill_audio_buffer_i16(data, &audio_queue);
                    },
                    move |err| {
                        eprintln!("Audio playback error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build output stream: {}", e))?
            }
            SampleFormat::U16 => {
                device.build_output_stream(
                    &stream_config,
                    move |data: &mut [u16], _: &cpal::OutputCallbackInfo| {
                        Self::fill_audio_buffer_u16(data, &audio_queue);
                    },
                    move |err| {
                        eprintln!("Audio playback error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build output stream: {}", e))?
            }
            _ => {
                return Err("Unsupported sample format".to_string());
            }
        };

        self.device = Some(device);
        self.stream = Some(stream);
        self.state.store(true, std::sync::atomic::Ordering::Relaxed);

        Ok(())
    }

    /// Остановить воспроизведение
    pub async fn stop_playback(&mut self) -> Result<(), String> {
        if !self.state.load(std::sync::atomic::Ordering::Relaxed) {
            return Ok(());
        }

        // Останавливаем поток
        if let Some(stream) = self.stream.take() {
            drop(stream);
        }

        // Очищаем очередь
        {
            let mut queue = self.audio_queue.lock().unwrap();
            queue.clear();
        }

        self.state.store(false, std::sync::atomic::Ordering::Relaxed);
        Ok(())
    }

    /// Добавить аудио в очередь воспроизведения
    pub fn queue_audio(&self, audio_data: AudioData) -> Result<(), String> {
        let mut queue = self.audio_queue.lock()
            .map_err(|e| format!("Failed to lock audio queue: {}", e))?;
        queue.push(audio_data);
        Ok(())
    }

    /// Проверить, активно ли воспроизведение
    pub fn is_playing(&self) -> bool {
        self.state.load(std::sync::atomic::Ordering::Relaxed)
    }

    /// Получить размер очереди
    pub fn queue_size(&self) -> usize {
        let queue = self.audio_queue.lock().unwrap();
        queue.len()
    }

    /// Очистить очередь
    pub fn clear_queue(&self) {
        let mut queue = self.audio_queue.lock().unwrap();
        queue.clear();
    }

    /// Заполнить аудио буфер (f32)
    fn fill_audio_buffer(
        data: &mut [f32],
        audio_queue: &Arc<std::sync::Mutex<Vec<AudioData>>>,
    ) {
        let mut queue = audio_queue.lock().unwrap();
        let mut sample_index = 0;

        while sample_index < data.len() && !queue.is_empty() {
            if let Some(audio_data) = queue.first_mut() {
                let remaining_samples = audio_data.samples.len();
                let samples_to_copy = std::cmp::min(
                    remaining_samples,
                    data.len() - sample_index,
                );

                if samples_to_copy > 0 {
                    data[sample_index..sample_index + samples_to_copy]
                        .copy_from_slice(&audio_data.samples[..samples_to_copy]);
                    sample_index += samples_to_copy;

                    // Удаляем использованные сэмплы
                    audio_data.samples.drain(..samples_to_copy);
                    if audio_data.samples.is_empty() {
                        queue.remove(0);
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // Заполняем оставшиеся сэмплы нулями
        for i in sample_index..data.len() {
            data[i] = 0.0;
        }
    }

    /// Заполнить аудио буфер (i16)
    fn fill_audio_buffer_i16(
        data: &mut [i16],
        audio_queue: &Arc<std::sync::Mutex<Vec<AudioData>>>,
    ) {
        let mut queue = audio_queue.lock().unwrap();
        let mut sample_index = 0;

        while sample_index < data.len() && !queue.is_empty() {
            if let Some(audio_data) = queue.first_mut() {
                let remaining_samples = audio_data.samples.len();
                let samples_to_copy = std::cmp::min(
                    remaining_samples,
                    data.len() - sample_index,
                );

                if samples_to_copy > 0 {
                    for i in 0..samples_to_copy {
                        data[sample_index + i] = (audio_data.samples[i] * 32767.0) as i16;
                    }
                    sample_index += samples_to_copy;

                    // Удаляем использованные сэмплы
                    audio_data.samples.drain(..samples_to_copy);
                    if audio_data.samples.is_empty() {
                        queue.remove(0);
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // Заполняем оставшиеся сэмплы нулями
        for i in sample_index..data.len() {
            data[i] = 0;
        }
    }

    /// Заполнить аудио буфер (u16)
    fn fill_audio_buffer_u16(
        data: &mut [u16],
        audio_queue: &Arc<std::sync::Mutex<Vec<AudioData>>>,
    ) {
        let mut queue = audio_queue.lock().unwrap();
        let mut sample_index = 0;

        while sample_index < data.len() && !queue.is_empty() {
            if let Some(audio_data) = queue.first_mut() {
                let remaining_samples = audio_data.samples.len();
                let samples_to_copy = std::cmp::min(
                    remaining_samples,
                    data.len() - sample_index,
                );

                if samples_to_copy > 0 {
                    for i in 0..samples_to_copy {
                        data[sample_index + i] = ((audio_data.samples[i] + 1.0) * 32767.5) as u16;
                    }
                    sample_index += samples_to_copy;

                    // Удаляем использованные сэмплы
                    audio_data.samples.drain(..samples_to_copy);
                    if audio_data.samples.is_empty() {
                        queue.remove(0);
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // Заполняем оставшиеся сэмплы нулями
        for i in sample_index..data.len() {
            data[i] = 32768; // Центр для u16
        }
    }
}
