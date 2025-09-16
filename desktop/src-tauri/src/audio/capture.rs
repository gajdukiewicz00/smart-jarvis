use super::{AudioConfig, AudioData};
use cpal::traits::{DeviceTrait, HostTrait};
use cpal::{Device, SampleFormat, SampleRate, Stream, StreamConfig};
use std::sync::Arc;
use tokio::sync::mpsc;
use anyhow::Result;

/// Аудио захват
pub struct AudioCapture {
    device: Option<Device>,
    stream: Option<Stream>,
    config: AudioConfig,
    state: Arc<std::sync::atomic::AtomicBool>,
    audio_sender: Option<mpsc::UnboundedSender<AudioData>>,
}

impl AudioCapture {
    pub fn new(config: AudioConfig) -> Self {
        Self {
            device: None,
            stream: None,
            config,
            state: Arc::new(std::sync::atomic::AtomicBool::new(false)),
            audio_sender: None,
        }
    }

    /// Начать захват аудио
    pub async fn start_capture(&mut self) -> Result<(), String> {
        if self.state.load(std::sync::atomic::Ordering::Relaxed) {
            return Err("Audio capture is already running".to_string());
        }

        // Получаем устройство ввода
        let host = cpal::default_host();
        let device = if let Some(ref device_name) = self.config.device_name {
            // Ищем устройство по имени
            host.input_devices()
                .map_err(|e| format!("Failed to get input devices: {}", e))?
                .find(|d| d.name().unwrap_or_default() == *device_name)
                .ok_or_else(|| format!("Device '{}' not found", device_name))?
        } else {
            // Используем устройство по умолчанию
            host.default_input_device()
                .ok_or("No default input device available")?
        };

        // Получаем конфигурацию устройства
        let device_config = device.default_input_config()
            .map_err(|e| format!("Failed to get device config: {}", e))?;

        // Создаем конфигурацию потока
        let stream_config = StreamConfig {
            channels: self.config.channels,
            sample_rate: SampleRate(self.config.sample_rate),
            buffer_size: cpal::BufferSize::Fixed(self.config.buffer_size),
        };

        // Создаем канал для передачи аудио данных
        let (tx, mut rx) = mpsc::unbounded_channel();
        self.audio_sender = Some(tx);

        // Создаем аудио поток
        let stream = match device_config.sample_format() {
            SampleFormat::F32 => {
                device.build_input_stream(
                    &stream_config,
                    move |data: &[f32], _: &cpal::InputCallbackInfo| {
                        // Обрабатываем аудио данные
                        Self::process_audio_data(data, &mut rx);
                    },
                    move |err| {
                        eprintln!("Audio stream error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build input stream: {}", e))?
            }
            SampleFormat::I16 => {
                device.build_input_stream(
                    &stream_config,
                    move |data: &[i16], _: &cpal::InputCallbackInfo| {
                        // Конвертируем i16 в f32
                        let f32_data: Vec<f32> = data.iter()
                            .map(|&sample| sample as f32 / 32768.0)
                            .collect();
                        Self::process_audio_data(&f32_data, &mut rx);
                    },
                    move |err| {
                        eprintln!("Audio stream error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build input stream: {}", e))?
            }
            SampleFormat::U16 => {
                device.build_input_stream(
                    &stream_config,
                    move |data: &[u16], _: &cpal::InputCallbackInfo| {
                        // Конвертируем u16 в f32
                        let f32_data: Vec<f32> = data.iter()
                            .map(|&sample| (sample as f32 - 32768.0) / 32768.0)
                            .collect();
                        Self::process_audio_data(&f32_data, &mut rx);
                    },
                    move |err| {
                        eprintln!("Audio stream error: {}", err);
                    },
                    None,
                ).map_err(|e| format!("Failed to build input stream: {}", e))?
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

    /// Остановить захват аудио
    pub async fn stop_capture(&mut self) -> Result<(), String> {
        if !self.state.load(std::sync::atomic::Ordering::Relaxed) {
            return Ok(());
        }

        // Останавливаем поток
        if let Some(stream) = self.stream.take() {
            drop(stream);
        }

        // Закрываем канал
        if let Some(sender) = self.audio_sender.take() {
            drop(sender);
        }

        self.state.store(false, std::sync::atomic::Ordering::Relaxed);
        Ok(())
    }

    /// Проверить, активен ли захват
    pub fn is_capturing(&self) -> bool {
        self.state.load(std::sync::atomic::Ordering::Relaxed)
    }

    /// Получить receiver для аудио данных
    pub fn get_audio_receiver(&mut self) -> Option<mpsc::UnboundedReceiver<AudioData>> {
        // В реальной реализации здесь должен быть доступ к receiver
        None
    }

    /// Обработать аудио данные
    fn process_audio_data(
        data: &[f32],
        _rx: &mut mpsc::UnboundedReceiver<AudioData>,
    ) {
        // Создаем AudioData из samples
        let _audio_data = AudioData::new(
            data.to_vec(),
            44100, // Заглушка
            1,     // Заглушка
        );

        // Отправляем данные (в реальности через sender)
        println!("Captured {} samples", data.len());
    }

    /// Записать аудио в файл
    pub async fn save_to_file(&self, audio_data: &AudioData, filename: &str) -> Result<(), String> {
        use hound::{WavWriter, WavSpec};

        let spec = WavSpec {
            channels: audio_data.channels,
            sample_rate: audio_data.sample_rate,
            bits_per_sample: 16,
            sample_format: hound::SampleFormat::Int,
        };

        let mut writer = WavWriter::create(filename, spec)
            .map_err(|e| format!("Failed to create WAV file: {}", e))?;

        for sample in &audio_data.samples {
            let sample_i16 = (sample * 32767.0) as i16;
            writer.write_sample(sample_i16)
                .map_err(|e| format!("Failed to write sample: {}", e))?;
        }

        writer.finalize()
            .map_err(|e| format!("Failed to finalize WAV file: {}", e))?;

        Ok(())
    }

    /// Загрузить аудио из файла
    pub async fn load_from_file(filename: &str) -> Result<AudioData, String> {
        use hound::WavReader;

        let mut reader = WavReader::open(filename)
            .map_err(|e| format!("Failed to open WAV file: {}", e))?;

        let spec = reader.spec();
        let samples: Result<Vec<i16>, _> = reader.samples().collect();
        let samples = samples.map_err(|e| format!("Failed to read samples: {}", e))?;

        // Конвертируем i16 в f32
        let f32_samples: Vec<f32> = samples.iter()
            .map(|&sample| sample as f32 / 32768.0)
            .collect();

        Ok(AudioData::new(
            f32_samples,
            spec.sample_rate,
            spec.channels,
        ))
    }
}
