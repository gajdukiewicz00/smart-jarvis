use std::sync::OnceLock;
use std::sync::mpsc;
use std::thread;
use cpal::traits::{HostTrait, DeviceTrait, StreamTrait};
use crate::{config, listener, stt};

static AUDIO_THREAD: OnceLock<thread::JoinHandle<()>> = OnceLock::new();
static STOP_TX: OnceLock<mpsc::Sender<()>> = OnceLock::new();

pub fn init_cpal() {
    // Инициализация уже выполнена в модуле recorder
}

pub fn start_recording() -> Result<(), String> {
    if AUDIO_THREAD.get().is_some() {
        return Err("Recording already in progress".into());
    }

    let (stop_tx, stop_rx) = mpsc::channel::<()>();

    let handle = thread::spawn(move || {
        run_audio_loop(stop_rx);
    });

    AUDIO_THREAD.set(handle).unwrap();
    STOP_TX.set(stop_tx).unwrap();

    Ok(())
}

pub fn stop_recording() -> Result<(), String> {
    if let Some(tx) = STOP_TX.get() {
        let _ = tx.send(());
    }

    if let Some(handle) = AUDIO_THREAD.get() {
        let _ = handle.thread().unpark();
        // Не ждем завершения, просто возвращаем управление
    }

    Ok(())
}

fn run_audio_loop(stop_rx: mpsc::Receiver<()>) {
    log::info!("Starting audio capture loop");

    let host = cpal::default_host();

    // Предпочитаем устройство с микрофоном
    let mut picked: Option<cpal::Device> = None;
    if let Ok(mut iter) = host.input_devices() {
        for dev in iter.by_ref() {
            let name = dev.name().unwrap_or_default().to_lowercase();
            if name.contains("mic") || name.contains("microphone") || name.contains("usb") {
                picked = Some(dev);
                break;
            }
        }
    }

    let device = if let Some(d) = picked {
        d
    } else {
        match host.default_input_device() {
            Some(d) => d,
            None => {
                log::error!("No input device available");
                return;
            }
        }
    };

    log::info!("Using input device: {}", device.name().unwrap_or("unknown".into()));

    // Предпочитаем 16k моно если поддерживается
    let supported = match device.supported_input_configs() {
        Ok(iter) => iter.collect::<Vec<_>>(),
        Err(e) => {
            log::error!("supported_input_configs error: {}", e);
            return;
        }
    };

    let mut chosen: Option<cpal::SupportedStreamConfig> = None;
    for cfg in supported {
        let min = cfg.min_sample_rate().0;
        let max = cfg.max_sample_rate().0;
        if min <= 16000 && 16000 <= max {
            chosen = Some(cfg.with_sample_rate(cpal::SampleRate(16000)));
            break;
        }
    }

    let config_any = if let Some(c) = chosen {
        c
    } else {
        match device.default_input_config() {
            Ok(c) => c,
            Err(e) => {
                log::error!("Failed to get input config: {}", e);
                return;
            }
        }
    };

    let channels = config_any.channels() as usize;
    let sample_rate_hz: u32 = config_any.sample_rate().0;

    log::info!("Audio config: {}Hz, {} channels, format: {:?}", sample_rate_hz, channels, config_any.sample_format());

    // Параметры VAD
    let vad_threshold: f32 = std::env::var("SJ_VAD_THRESHOLD")
        .ok()
        .and_then(|v| v.parse::<f32>().ok())
        .unwrap_or(0.005);

    let mut silence_ms: u64 = 0;
    let max_silence_ms: u64 = std::env::var("SJ_VAD_SILENCE_MS")
        .ok()
        .and_then(|v| v.parse::<u64>().ok())
        .unwrap_or(600);

    // Создаем канал для передачи аудио данных
    let (audio_tx, audio_rx) = std::sync::mpsc::channel::<Vec<i16>>();

    // Создаем канал для передачи уровня громкости
    let (level_tx, level_rx) = std::sync::mpsc::channel::<f32>();

    // Создаем поток для обработки аудио данных
    let audio_processor = thread::spawn(move || {
        while let Ok(audio_data) = audio_rx.recv() {
            // Отправляем данные в wake word detection
            if let Some(_) = listener::data_callback(&audio_data) {
                log::info!("Wake word detected!");
                // Здесь можно отправить событие в Tauri приложение
            }

            // Отправляем данные в STT для распознавания
            if let Some(text) = stt::recognize(&audio_data, false) {
                log::info!("STT result: {}", text);
                // Здесь можно отправить результат в Tauri приложение
            }
        }
    });

    // Создаем поток для мониторинга уровня громкости
    let level_monitor = thread::spawn(move || {
        while let Ok(level) = level_rx.recv() {
            // Здесь можно отправить уровень в Tauri приложение для визуализации
            log::debug!("Audio level: {:.5}", level);
        }
    });

    // Создаем аудио поток
    let result: Result<cpal::Stream, String> = match config_any.sample_format() {
        cpal::SampleFormat::F32 => {
            let tx = audio_tx.clone();
            let level_tx_inner = level_tx.clone();

            device.build_input_stream(&config_any.clone().into(), move |data: &[f32], _| {
                let mut audio_data = Vec::with_capacity(data.len());

                if channels == 1 {
                    for &sample in data {
                        audio_data.push((sample * i16::MAX as f32) as i16);
                    }
                } else {
                    for frame in data.chunks(channels) {
                        let avg = frame.iter().copied().sum::<f32>() / channels as f32;
                        audio_data.push((avg * i16::MAX as f32) as i16);
                    }
                }

                // Вычисляем RMS для VAD
                let rms = if data.is_empty() {
                    0.0
                } else {
                    (data.iter().map(|s| s * s).sum::<f32>() / data.len() as f32).sqrt()
                };

                let _ = level_tx_inner.send(rms);

                let frame_ms = if sample_rate_hz > 0 {
                    ((data.len() / channels) as u64 * 1000u64) / sample_rate_hz as u64
                } else {
                    10
                };

                if rms < vad_threshold {
                    silence_ms = silence_ms.saturating_add(frame_ms.max(1));
                } else {
                    silence_ms = 0;
                }

                if silence_ms >= max_silence_ms {
                    log::info!("Silence detected, stopping recording");
                    return;
                }

                let _ = tx.send(audio_data);
            }, move |err| {
                log::error!("cpal error: {}", err);
            }, None).map_err(|e| format!("stream: {}", e))
        }
        cpal::SampleFormat::I16 => {
            let tx = audio_tx.clone();
            let level_tx_inner = level_tx.clone();

            device.build_input_stream(&config_any.clone().into(), move |data: &[i16], _| {
                let mut audio_data = Vec::with_capacity(data.len());

                if channels == 1 {
                    audio_data.extend_from_slice(data);
                } else {
                    for frame in data.chunks(channels) {
                        let avg = frame.iter().copied().map(|x| x as i32).sum::<i32>() / channels as i32;
                        audio_data.push(avg as i16);
                    }
                }

                // Вычисляем RMS для VAD
                let rms = if data.is_empty() {
                    0.0
                } else {
                    let sum: i64 = data.iter().map(|&s| (s as i32).pow(2) as i64).sum();
                    ((sum as f32 / data.len() as f32).sqrt()) / i16::MAX as f32
                };

                let _ = level_tx_inner.send(rms);

                let frame_ms = if sample_rate_hz > 0 {
                    ((data.len() / channels) as u64 * 1000u64) / sample_rate_hz as u64
                } else {
                    10
                };

                if rms < vad_threshold {
                    silence_ms = silence_ms.saturating_add(frame_ms.max(1));
                } else {
                    silence_ms = 0;
                }

                if silence_ms >= max_silence_ms {
                    log::info!("Silence detected, stopping recording");
                    return;
                }

                let _ = tx.send(audio_data);
            }, move |err| {
                log::error!("cpal error: {}", err);
            }, None).map_err(|e| format!("stream: {}", e))
        }
        _ => Err("Unsupported sample format".into()),
    };

    match result {
        Ok(stream) => {
            if let Err(e) = stream.play() {
                log::error!("stream play error: {}", e);
                return;
            }

            log::info!("Audio stream started");

            // Ждем сигнала остановки
            let _ = stop_rx.recv();

            drop(stream);
            log::info!("Audio stream stopped");
        }
        Err(e) => log::error!("Audio stream build error: {}", e),
    }

    // Ждем завершения обработчиков
    drop(audio_tx);
    drop(level_tx);
    let _ = audio_processor.join();
    let _ = level_monitor.join();
}
