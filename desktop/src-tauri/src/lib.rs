mod pc;
mod api;
mod wake_word;
mod auth;
mod websocket;
mod audio;

// Интеграция из Priler/jarvis проекта
mod config;
mod commands;
mod stt;
mod listener;
mod recorder;
mod tray;

// Импорты для новых модулей
use config::{WakeWordEngine, SpeechToTextEngine, RecorderType, AudioType};
use commands::{AssistantCommand, Config as CommandConfig};
use stt::{recognize as stt_recognize};
use listener::{data_callback as wake_word_callback};
use recorder::{start_recording as recorder_start, stop_recording as recorder_stop, is_recording as recorder_is_recording};

use pc::{PcCommand, PcCommandResult};
use pc::file_system::execute_file_system_command;
use pc::process_manager::execute_process_command;
use pc::system_settings::execute_system_command;
use pc::network::execute_network_command;

use api::{ApiClient, ApiConfig};
use api::voice_gateway::VoiceGatewayClient;
use api::stt_service::SttServiceClient;
use api::nlu_service::NluServiceClient;
use api::dm_service::DmServiceClient;
use api::tts_service::TtsServiceClient;
use api::todo_service::TodoServiceClient;
use api::money_service::MoneyServiceClient;
use api::calendar_service::CalendarServiceClient;
use api::memory_service::MemoryServiceClient;

use wake_word::{WakeWordDetector, WakeWordConfig};
use auth::{session::SessionManager, LoginRequest, RegisterRequest};
use audio::{AudioManager, AudioConfig, AudioDevice, AudioData};
use audio::visualization::{AudioVisualizer, VisualizationData};

use std::sync::Arc;
use tauri::State;
use tokio::sync::Mutex;
use std::sync::OnceLock;
use std::thread;
use cpal::traits::{HostTrait, DeviceTrait, StreamTrait};
use futures_util::SinkExt;
use std::sync::mpsc;
use tauri::{Manager, WebviewUrl, WebviewWindowBuilder};
use tauri::Emitter;
use serde::Serialize;

// Простое состояние для аудио системы (без cpal потоков)
pub struct AudioState {
    pub initialized: Arc<Mutex<bool>>,
    pub config: Arc<Mutex<AudioConfig>>,
}

// Глобальное состояние для Wake Word
pub struct WakeWordState {
    pub detector: Arc<Mutex<Option<WakeWordDetector>>>,
}

impl Default for WakeWordState {
    fn default() -> Self {
        Self {
            detector: Arc::new(Mutex::new(None)),
        }
    }
}

// ================= Native Recorder (CPAL + WS) =================
static STOP_TX: OnceLock<tokio::sync::Mutex<Option<mpsc::Sender<()>>>> = OnceLock::new();
static REC_BUF: OnceLock<tokio::sync::Mutex<Vec<i16>>> = OnceLock::new();
static APP_HANDLE: OnceLock<tauri::AppHandle> = OnceLock::new();

#[derive(Serialize, Clone, Copy)]
struct VadLevelEvent {
  rms: f32,
  speech: bool,
}

impl Default for AudioState {
    fn default() -> Self {
        Self {
            initialized: Arc::new(Mutex::new(false)),
            config: Arc::new(Mutex::new(AudioConfig::default())),
        }
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
  tauri::Builder::default()
    .manage(AudioState::default())
    .manage(WakeWordState::default())
    .setup(|app| {
      // Ensure portal usage on Linux for permissions dialogs where applicable
      std::env::set_var("GTK_USE_PORTAL", "1");
      let _ = APP_HANDLE.set(app.handle().clone());
      if cfg!(debug_assertions) {
        app.handle().plugin(
          tauri_plugin_log::Builder::default()
            .level(log::LevelFilter::Info)
            .build(),
        )?;
      }
      // Create logs window at startup
      let _ = WebviewWindowBuilder::new(
        app,
        "logs",
        WebviewUrl::App("index.html?window=logs".into()),
      )
      .title("SmartJARVIS Logs")
      .resizable(true)
      .inner_size(900.0, 600.0)
      .build();
      Ok(())
    })
    .invoke_handler(tauri::generate_handler![
      execute_pc_command,
      get_system_info,
      list_files,
      start_process_command,
      // API клиенты
      create_api_client,
      voice_gateway_start_session,
      voice_gateway_send_audio,
      stt_transcribe_audio,
      nlu_recognize_intent,
      dm_process_turn,
      tts_synthesize_speech,
      todo_create_task,
      todo_get_tasks,
      money_create_transaction,
      calendar_create_event,
      memory_create_entry,
      memory_search,
      // Wake Word Detection (интегрировано из Priler/jarvis)
      create_wake_word_detector,
      start_wake_word_detection,
      stop_wake_word_detection,
      get_wake_word_status,
      update_wake_word_config,
      // Командная система (интегрировано из Priler/jarvis)
      parse_commands,
      execute_command,
      fetch_command,
      // Аудио система (интегрировано из Priler/jarvis)
      // init_audio_system, // временно отключено
      start_audio_capture,
      stop_audio_capture,
      get_audio_devices,
      // STT система (интегрировано из Priler/jarvis)
      init_stt_system,
      recognize_speech,
      // Запись аудио (интегрировано из Priler/jarvis)
      init_recorder,
      start_recording,
      stop_recording,
      is_recording,
      // Аутентификация
      login_user,
      register_user,
      logout_user,
      get_current_user,
      // WebSocket
      connect_websocket,
      disconnect_websocket,
      send_websocket_message,
      // Аудио обработка
      initialize_audio_system,
      get_audio_devices,
      start_audio_capture,
      stop_audio_capture,
      start_audio_playback,
      stop_audio_playback,
      queue_audio_for_playback,
      get_audio_visualization,
      save_audio_to_file,
      load_audio_from_file,
      create_websocket_client,
      get_websocket_connection_state,
      get_websocket_reconnect_attempts,
      reset_websocket_reconnect_attempts,
      get_websocket_last_heartbeat,
      start_native_recording,
      stop_native_recording,
      probe_cpal_devices
    ])
    .run(tauri::generate_context!())
    .expect("error while running tauri application");
}

// Удалено: открытие DevTools API в Tauri v2 делается через Devtools плагин/CLI

// Start native microphone capture and stream PCM to Voice Gateway via WebSocket
#[tauri::command]
async fn start_native_recording() -> Result<String, String> {
  eprintln!("start_native_recording: called");
  let stop = STOP_TX.get_or_init(|| tokio::sync::Mutex::new(None));
  let mut stop_guard = stop.lock().await;
  if stop_guard.is_some() { eprintln!("start_native_recording: already running"); return Ok("Native recording already running".into()); }

  let rec_buf = REC_BUF.get_or_init(|| tokio::sync::Mutex::new(Vec::with_capacity(16000*10)));
  {
    let mut b = rec_buf.lock().await;
    b.clear();
  }

  let (tx_pcm, mut rx_pcm) = tokio::sync::mpsc::unbounded_channel::<Vec<u8>>();
  let (stop_tx, stop_rx) = mpsc::channel::<()>();
  // Clone sender: one handle kept in global STOP_TX, another moved into thread
  let stop_tx_store = stop_tx.clone();
  let stop_tx_for_thread = stop_tx.clone();
  let (tx_level, mut rx_level) = tokio::sync::mpsc::unbounded_channel::<f32>();

  // WS forwarder (best-effort)
  tokio::spawn(async move {
    use tokio_tungstenite::{connect_async, tungstenite::Message};
    let url = url::Url::parse("ws://localhost:8080/voice").unwrap();
    match connect_async(url).await {
      Ok((mut ws, _)) => {
        eprintln!("WS: connected to voice gateway");
        while let Some(pcm) = rx_pcm.recv().await {
          if let Err(e) = ws.send(Message::Binary(pcm)).await { eprintln!("WS send error: {}", e); break; }
        }
        let _ = ws.close(None).await;
      }
      Err(e) => {
        eprintln!("WS connect error: {}", e);
        // drain but drop
        while let Some(_pcm) = rx_pcm.recv().await { /* drop */ }
      }
    }
  });

  // Optional: stream level logs (can be consumed by UI later)
  tokio::spawn(async move {
    while let Some(level) = rx_level.recv().await {
      eprintln!("Level: {:.3}", level);
    }
  });

  // Spawn dedicated thread to own CPAL stream (avoid Send/Sync issues)
  thread::spawn(move || {
    eprintln!("CPAL thread: starting");
    let host = cpal::default_host();
    // Prefer a likely microphone device by name, otherwise fallback to default
    let mut picked: Option<cpal::Device> = None;
    if let Ok(mut iter) = host.input_devices() {
      for dev in iter.by_ref() {
        let name = dev.name().unwrap_or_default().to_lowercase();
        if name.contains("mic") || name.contains("microphone") || name.contains("t1") || name.contains("c4k") || name.contains("usb") || name.contains("creative") {
          picked = Some(dev);
          break;
        }
      }
    }
    let device = if let Some(d) = picked { d } else {
      match host.default_input_device() { Some(d) => d, None => { eprintln!("No input device available"); return; } }
    };
    eprintln!("Using input device: {}", device.name().unwrap_or("unknown".into()));
    // Prefer 16k mono explicitly if supported
    let supported = match device.supported_input_configs() {
      Ok(iter) => iter.collect::<Vec<_>>(),
      Err(e) => { eprintln!("supported_input_configs error: {}", e); return; }
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
    let config_any = if let Some(c) = chosen { c } else {
      match device.default_input_config() { Ok(c) => c, Err(e) => { eprintln!("Failed to get input config: {}", e); return; } }
    };
    let channels = config_any.channels() as usize;
    let sample_rate_hz: u32 = config_any.sample_rate().0;

    // Simple VAD parameters
    let vad_threshold: f32 = std::env::var("SJ_VAD_THRESHOLD").ok().and_then(|v| v.parse::<f32>().ok()).unwrap_or(0.005);
    let mut silence_ms: u64 = 0;
    let max_silence_ms: u64 = std::env::var("SJ_VAD_SILENCE_MS").ok().and_then(|v| v.parse::<u64>().ok()).unwrap_or(600);

    let result: Result<cpal::Stream, String> = match config_any.sample_format() {
      cpal::SampleFormat::F32 => {
        let tx = tx_pcm.clone();
        let rec_buf = REC_BUF.get().unwrap();
        let tx_level_inner = tx_level.clone();
        let stop_tx_inner = stop_tx_for_thread.clone();
        device.build_input_stream(&config_any.clone().into(), move |data: &[f32], _| {
          let mut pcm = Vec::with_capacity(data.len()*2);
          if channels==1 { for &s in data { let v=(s * i16::MAX as f32) as i16; pcm.extend_from_slice(&v.to_le_bytes()); } }
          else { for frame in data.chunks(channels) { let avg = frame.iter().copied().sum::<f32>()/channels as f32; let v=(avg*i16::MAX as f32) as i16; pcm.extend_from_slice(&v.to_le_bytes()); } }
          // VAD: compute RMS
          let rms = if data.is_empty(){0.0}else{ (data.iter().map(|s| s*s).sum::<f32>()/data.len() as f32).sqrt() };
          let _ = tx_level_inner.send(rms);
          if let Some(h) = APP_HANDLE.get() { 
            let result = h.emit("vad-level", serde_json::json!({
                "rms": rms,
                "speech": silence_ms == 0
            })); 
            eprintln!("VAD event sent: rms={:.5} speech={} result={:?}", rms, silence_ms==0, result);
          }
          eprintln!("VAD f32 rms={:.5} silence_ms={} thr={:.5}", rms, silence_ms, vad_threshold);
          let frame_ms = if sample_rate_hz > 0 { ((data.len()/channels) as u64 * 1000u64) / sample_rate_hz as u64 } else { 10 };
          if rms < vad_threshold { silence_ms = silence_ms.saturating_add(frame_ms.max(1)); } else { silence_ms = 0; }
          if silence_ms >= max_silence_ms { let _ = stop_tx_inner.send(()); return; }
          {
            let mut b = rec_buf.blocking_lock();
            for chunk in pcm.chunks_exact(2) {
              let v = i16::from_le_bytes([chunk[0], chunk[1]]);
              b.push(v);
              let max = 16000*10;
              let current_len = b.len();
              if current_len > max { let remove = current_len - max; b.drain(..remove); }
            }
          }
          let _ = tx.send(pcm);
        }, move |err| { eprintln!("cpal error: {}", err); }, None).map_err(|e| format!("stream: {}", e))
      }
      cpal::SampleFormat::I16 => {
        let tx = tx_pcm.clone();
        let rec_buf = REC_BUF.get().unwrap();
        let tx_level_inner = tx_level.clone();
        let stop_tx_inner = stop_tx_for_thread.clone();
        device.build_input_stream(&config_any.clone().into(), move |data: &[i16], _| {
          let mut pcm = Vec::with_capacity(data.len()*2);
          if channels==1 { for &v in data { pcm.extend_from_slice(&v.to_le_bytes()); } }
          else { for frame in data.chunks(channels) { let avg = frame.iter().copied().map(|x| x as i32).sum::<i32>()/channels as i32; let v=avg as i16; pcm.extend_from_slice(&v.to_le_bytes()); } }
          let rms = if data.is_empty(){0.0}else{ let sum: i64 = data.iter().map(|&s| (s as i32).pow(2) as i64).sum(); ((sum as f32 / data.len() as f32).sqrt()) / i16::MAX as f32 };
          let _ = tx_level_inner.send(rms);
          if let Some(h) = APP_HANDLE.get() { 
            let result = h.emit("vad-level", serde_json::json!({
                "rms": rms,
                "speech": silence_ms == 0
            })); 
            eprintln!("VAD event sent: rms={:.5} speech={} result={:?}", rms, silence_ms==0, result);
          }
          eprintln!("VAD i16 rms={:.5} silence_ms={} thr={:.5}", rms, silence_ms, vad_threshold);
          let frame_ms = if sample_rate_hz > 0 { ((data.len()/channels) as u64 * 1000u64) / sample_rate_hz as u64 } else { 10 };
          if rms < vad_threshold { silence_ms = silence_ms.saturating_add(frame_ms.max(1)); } else { silence_ms = 0; }
          if silence_ms >= max_silence_ms { let _ = stop_tx_inner.send(()); return; }
          {
            let mut b = rec_buf.blocking_lock();
            for chunk in pcm.chunks_exact(2) {
              let v = i16::from_le_bytes([chunk[0], chunk[1]]);
              b.push(v);
              let max = 16000*10;
              let current_len = b.len();
              if current_len > max { let remove = current_len - max; b.drain(..remove); }
            }
          }
          let _ = tx.send(pcm);
        }, move |err| { eprintln!("cpal error: {}", err); }, None).map_err(|e| format!("stream: {}", e))
      }
      cpal::SampleFormat::U16 => {
        let tx = tx_pcm.clone();
        let rec_buf = REC_BUF.get().unwrap();
        let tx_level_inner = tx_level.clone();
        let stop_tx_inner = stop_tx_for_thread.clone();
        device.build_input_stream(&config_any.clone().into(), move |data: &[u16], _| {
          let mut pcm = Vec::with_capacity(data.len()*2);
          if channels==1 { for &v in data { let s=(v as i32 - 32768) as i16; pcm.extend_from_slice(&s.to_le_bytes()); } }
          else { for frame in data.chunks(channels) { let avg = frame.iter().copied().map(|x| x as i32).sum::<i32>()/channels as i32; let s=(avg-32768) as i16; pcm.extend_from_slice(&s.to_le_bytes()); } }
          let rms = if data.is_empty(){0.0}else{ let sum: i64 = data.iter().map(|&s| ((s as i32 - 32768).pow(2)) as i64).sum(); ((sum as f32 / data.len() as f32).sqrt()) / i16::MAX as f32 };
          let _ = tx_level_inner.send(rms);
          if let Some(h) = APP_HANDLE.get() { 
            let result = h.emit("vad-level", serde_json::json!({
                "rms": rms,
                "speech": silence_ms == 0
            })); 
            eprintln!("VAD event sent: rms={:.5} speech={} result={:?}", rms, silence_ms==0, result);
          }
          eprintln!("VAD u16 rms={:.5} silence_ms={} thr={:.5}", rms, silence_ms, vad_threshold);
          let frame_ms = if sample_rate_hz > 0 { ((data.len()/channels) as u64 * 1000u64) / sample_rate_hz as u64 } else { 10 };
          if rms < vad_threshold { silence_ms = silence_ms.saturating_add(frame_ms.max(1)); } else { silence_ms = 0; }
          if silence_ms >= max_silence_ms { let _ = stop_tx_inner.send(()); return; }
          {
            let mut b = rec_buf.blocking_lock();
            for chunk in pcm.chunks_exact(2) {
              let v = i16::from_le_bytes([chunk[0], chunk[1]]);
              b.push(v);
              let max = 16000*10;
              let current_len = b.len();
              if current_len > max { let remove = current_len - max; b.drain(..remove); }
            }
          }
          let _ = tx.send(pcm);
        }, move |err| { eprintln!("cpal error: {}", err); }, None).map_err(|e| format!("stream: {}", e))
      }
      _ => Err("Unsupported sample format".into()),
    };

    match result {
      Ok(stream) => {
        if let Err(e) = stream.play() { eprintln!("stream play error: {}", e); return; }
        eprintln!("CPAL: stream started");
        // block until stop signal
        let _ = stop_rx.recv();
        drop(stream);
        eprintln!("CPAL thread: stopped");
      }
      Err(e) => eprintln!("CPAL stream build error: {}", e),
    }
  });

  *stop_guard = Some(stop_tx_store);
  Ok("Native recording started".into())
}

#[tauri::command]
async fn stop_native_recording() -> Result<String, String> {
  eprintln!("stop_native_recording: called");
  let mut msg = String::from("Native recording stopped");
  if let Some(lock) = STOP_TX.get() {
    let mut guard = lock.lock().await;
    if let Some(tx) = guard.take() { let _ = tx.send(()); }
  }
  // Write short WAV to /tmp for verification
  if let Some(buf_lock) = REC_BUF.get() {
    let samples = { buf_lock.lock().await.clone() };
    let path = "/tmp/sj_recording.wav";
    if let Err(e) = write_wav_mono_16k(path, &samples).await {
      eprintln!("write_wav error: {}", e);
    } else {
      eprintln!("Saved test recording: {} ({} samples)", path, samples.len());
      msg = format!("{}; saved {} samples to {}", msg, samples.len(), path);
    }
  }
  Ok(msg)
}

// helper: write mono 16-bit wav at 16k
async fn write_wav_mono_16k(path: &str, samples: &[i16]) -> Result<(), String> {
  let path_owned = path.to_string();
  let samples_owned: Vec<i16> = samples.to_vec();
  tokio::task::spawn_blocking(move || {
    let spec = hound::WavSpec { channels: 1, sample_rate: 16000, bits_per_sample: 16, sample_format: hound::SampleFormat::Int };
    let mut writer = hound::WavWriter::create(&path_owned, spec).map_err(|e| e.to_string())?;
    for s in samples_owned { writer.write_sample(s).map_err(|e| e.to_string())?; }
    writer.finalize().map_err(|e| e.to_string())
  }).await.map_err(|e| format!("spawn error: {}", e))?
}

/// Выполнить PC команду
#[tauri::command]
async fn execute_pc_command(command: PcCommand) -> Result<PcCommandResult, String> {
    match command.command_type {
        pc::PcCommandType::FileSystem => Ok(execute_file_system_command(command).await),
        pc::PcCommandType::Process => Ok(execute_process_command(command).await),
        pc::PcCommandType::System => Ok(execute_system_command(command).await),
        pc::PcCommandType::Network => Ok(execute_network_command(command).await),
    }
}

/// Получить системную информацию
#[tauri::command]
async fn get_system_info() -> Result<PcCommandResult, String> {
    let command = PcCommand {
        command_type: pc::PcCommandType::System,
        action: "get_system_resources".to_string(),
        parameters: std::collections::HashMap::new(),
    };
    Ok(execute_system_command(command).await)
}

/// Получить список файлов в директории
#[tauri::command]
async fn list_files(path: String) -> Result<PcCommandResult, String> {
    let mut parameters = std::collections::HashMap::new();
    parameters.insert("path".to_string(), serde_json::Value::String(path));
    
    let command = PcCommand {
        command_type: pc::PcCommandType::FileSystem,
        action: "list_directory".to_string(),
        parameters,
    };
    Ok(execute_file_system_command(command).await)
}

/// Запустить процесс
#[tauri::command]
async fn start_process_command(command_str: String, args: Vec<String>) -> Result<PcCommandResult, String> {
    let mut parameters = std::collections::HashMap::new();
    parameters.insert("command".to_string(), serde_json::Value::String(command_str));
    parameters.insert("args".to_string(), serde_json::Value::Array(
        args.into_iter().map(|arg| serde_json::Value::String(arg)).collect()
    ));
    
    let command = PcCommand {
        command_type: pc::PcCommandType::Process,
        action: "start_process".to_string(),
        parameters,
    };
    Ok(execute_process_command(command).await)
}

// ===== API CLIENT COMMANDS =====

/// Создать API клиент
#[tauri::command]
async fn create_api_client(base_url: String, timeout_seconds: Option<u64>) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: timeout_seconds.unwrap_or(30),
        retry_attempts: 3,
        api_key: None,
    };
    
    match ApiClient::new(config) {
        Ok(_client) => Ok("API client created successfully".to_string()),
        Err(e) => Err(format!("Failed to create API client: {}", e)),
    }
}

/// Начать сессию в Voice Gateway
#[tauri::command]
async fn voice_gateway_start_session(base_url: String, user_id: String) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let voice_client = VoiceGatewayClient::new(client);
    
    match voice_client.start_session(&user_id).await {
        Ok(response) => {
            if response.success {
                Ok(response.data.unwrap_or("Session started".to_string()))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to start session: {}", e)),
    }
}

/// Отправить аудио в Voice Gateway
#[tauri::command]
async fn voice_gateway_send_audio(base_url: String, session_id: String, user_id: String, audio_data: Vec<u8>) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let voice_client = VoiceGatewayClient::new(client);
    
    let chunk = api::models::AudioChunk {
        session_id,
        user_id,
        audio_data,
        sample_rate: 16000,
        channels: 1,
        format: "pcm".to_string(),
        timestamp: chrono::Utc::now().timestamp_millis() as u64,
    };
    
    match voice_client.send_audio_chunk(chunk).await {
        Ok(response) => {
            if response.success {
                Ok(response.data.unwrap_or("Audio sent".to_string()))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to send audio: {}", e)),
    }
}

/// Транскрибировать аудио через STT
#[tauri::command]
async fn stt_transcribe_audio(base_url: String, session_id: String, user_id: String, audio_data: Vec<u8>) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let stt_client = SttServiceClient::new(client);
    
    let chunk = api::models::AudioChunk {
        session_id,
        user_id,
        audio_data,
        sample_rate: 16000,
        channels: 1,
        format: "pcm".to_string(),
        timestamp: chrono::Utc::now().timestamp_millis() as u64,
    };
    
    match stt_client.transcribe_audio(chunk).await {
        Ok(response) => {
            if response.success {
                let transcription = response.data.unwrap();
                Ok(transcription.text)
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to transcribe audio: {}", e)),
    }
}

/// Распознать интент через NLU
#[tauri::command]
async fn nlu_recognize_intent(base_url: String, text: String, session_id: String, user_id: String) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let nlu_client = NluServiceClient::new(client);
    
    match nlu_client.recognize_intent(&text, &session_id, &user_id).await {
        Ok(response) => {
            if response.success {
                let intent = response.data.unwrap();
                Ok(intent.intent)
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to recognize intent: {}", e)),
    }
}

/// Обработать диалоговый ход через DM
#[tauri::command]
async fn dm_process_turn(base_url: String, session_id: String, user_input: String, intent: String) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let dm_client = DmServiceClient::new(client);
    
    let intent_result = api::models::IntentResult {
        intent,
        confidence: 1.0,
        entities: std::collections::HashMap::new(),
        slots: std::collections::HashMap::new(),
    };
    
    match dm_client.process_turn(&session_id, &user_input, intent_result).await {
        Ok(response) => {
            if response.success {
                let state = response.data.unwrap();
                Ok(format!("Dialog state updated: {}", state.state_id))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to process turn: {}", e)),
    }
}

/// Синтезировать речь через TTS
#[tauri::command]
async fn tts_synthesize_speech(base_url: String, text: String, voice: String) -> Result<Vec<u8>, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let tts_client = TtsServiceClient::new(client);
    
    match tts_client.quick_synthesize(&text, &voice).await {
        Ok(response) => {
            if response.success {
                let tts_response = response.data.unwrap();
                Ok(tts_response.audio_data)
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to synthesize speech: {}", e)),
    }
}

/// Создать задачу через Todo сервис
#[tauri::command]
async fn todo_create_task(base_url: String, user_id: String, title: String, description: Option<String>) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let todo_client = TodoServiceClient::new(client);
    
    let request = api::models::CreateTaskRequest {
        title,
        description,
        priority: api::models::TaskPriority::Medium,
        due_date: None,
    };
    
    match todo_client.create_task(&user_id, request).await {
        Ok(response) => {
            if response.success {
                let task = response.data.unwrap();
                Ok(format!("Task created: {}", task.id))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to create task: {}", e)),
    }
}

/// Получить задачи пользователя
#[tauri::command]
async fn todo_get_tasks(base_url: String, user_id: String) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let todo_client = TodoServiceClient::new(client);
    
    match todo_client.get_user_tasks(&user_id).await {
        Ok(response) => {
            if response.success {
                let tasks = response.data.unwrap();
                Ok(format!("Found {} tasks", tasks.len()))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to get tasks: {}", e)),
    }
}

/// Создать транзакцию через Money сервис
#[tauri::command]
async fn money_create_transaction(base_url: String, user_id: String, amount: f64, category: String, description: String) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let money_client = MoneyServiceClient::new(client);
    
    let request = api::models::CreateTransactionRequest {
        amount,
        currency: "USD".to_string(),
        category,
        description,
        transaction_type: api::models::TransactionType::Expense,
        date: Some(chrono::Utc::now().timestamp_millis() as u64),
    };
    
    match money_client.create_transaction(&user_id, request).await {
        Ok(response) => {
            if response.success {
                let transaction = response.data.unwrap();
                Ok(format!("Transaction created: {}", transaction.id))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to create transaction: {}", e)),
    }
}

/// Создать событие через Calendar сервис
#[tauri::command]
async fn calendar_create_event(base_url: String, user_id: String, title: String, start_time: u64, end_time: u64) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let calendar_client = CalendarServiceClient::new(client);
    
    let request = api::models::CreateEventRequest {
        title,
        description: None,
        start_time,
        end_time,
        location: None,
        attendees: vec![],
        reminders: vec![],
    };
    
    match calendar_client.create_event(&user_id, request).await {
        Ok(response) => {
            if response.success {
                let event = response.data.unwrap();
                Ok(format!("Event created: {}", event.id))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to create event: {}", e)),
    }
}

/// Создать запись в памяти
#[tauri::command]
async fn memory_create_entry(base_url: String, user_id: String, session_id: String, content: String) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let memory_client = MemoryServiceClient::new(client);
    
    let request = api::models::CreateMemoryRequest {
        content,
        context: std::collections::HashMap::new(),
    };
    
    match memory_client.create_memory(&user_id, &session_id, request).await {
        Ok(response) => {
            if response.success {
                let memory = response.data.unwrap();
                Ok(format!("Memory created: {}", memory.id))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to create memory: {}", e)),
    }
}

/// Поиск в памяти
#[tauri::command]
async fn memory_search(base_url: String, user_id: String, query: String) -> Result<String, String> {
    let config = ApiConfig {
        base_url,
        timeout_seconds: 30,
        retry_attempts: 3,
        api_key: None,
    };
    
    let client = ApiClient::new(config).map_err(|e| format!("Failed to create client: {}", e))?;
    let memory_client = MemoryServiceClient::new(client);
    
    match memory_client.quick_search(&user_id, &query, Some(5)).await {
        Ok(response) => {
            if response.success {
                let memories = response.data.unwrap();
                Ok(format!("Found {} memories", memories.len()))
            } else {
                Err(response.error.unwrap_or("Unknown error".to_string()))
            }
        }
        Err(e) => Err(format!("Failed to search memory: {}", e)),
    }
}

// ===== WAKE WORD DETECTION COMMANDS =====

/// Создать wake word detector
#[tauri::command]
async fn create_wake_word_detector(
    sensitivity: f32,
    use_local: bool,
    use_cloud: bool,
    cloud_api_key: Option<String>,
    wake_words: Vec<String>,
    wake_state: State<'_, WakeWordState>,
) -> Result<String, String> {
    let config = WakeWordConfig {
        sensitivity,
        use_local,
        use_cloud,
        cloud_api_key,
        wake_words,
    };

    let mut detector = WakeWordDetector::new(config);
    detector
        .initialize()
        .await
        .map_err(|e| format!("Failed to create wake word detector: {}", e))?;

    let mut guard = wake_state.detector.lock().await;
    *guard = Some(detector);
    Ok("Wake word detector created successfully".to_string())
}

/// Запустить wake word detection
#[tauri::command]
async fn start_wake_word_detection(wake_state: State<'_, WakeWordState>) -> Result<String, String> {
    let mut guard = wake_state.detector.lock().await;
    if let Some(detector) = guard.as_mut() {
        detector.start().await.map_err(|e| format!("{}", e))?;
        Ok("Wake word detection started".to_string())
    } else {
        Err("Wake word detector not created".to_string())
    }
}

/// Остановить wake word detection
#[tauri::command]
async fn stop_wake_word_detection(wake_state: State<'_, WakeWordState>) -> Result<String, String> {
    let mut guard = wake_state.detector.lock().await;
    if let Some(detector) = guard.as_mut() {
        detector.stop().await.map_err(|e| format!("{}", e))?;
        Ok("Wake word detection stopped".to_string())
    } else {
        Err("Wake word detector not created".to_string())
    }
}

/// Получить статус wake word detection
#[tauri::command]
async fn get_wake_word_status(wake_state: State<'_, WakeWordState>) -> Result<String, String> {
    let guard = wake_state.detector.lock().await;
    if let Some(detector) = guard.as_ref() {
        Ok(format!("Wake word state: {:?}", detector.get_state()))
    } else {
        Ok("Wake word detector: not created".to_string())
    }
}

/// Обновить конфигурацию wake word detection
#[tauri::command]
async fn update_wake_word_config(
    sensitivity: f32,
    use_local: bool,
    use_cloud: bool,
    cloud_api_key: Option<String>,
    wake_words: Vec<String>,
    wake_state: State<'_, WakeWordState>,
) -> Result<String, String> {
    let config = WakeWordConfig {
        sensitivity,
        use_local,
        use_cloud,
        cloud_api_key,
        wake_words,
    };

    let mut guard = wake_state.detector.lock().await;
    if let Some(detector) = guard.as_mut() {
        detector.update_config(config)?;
        Ok("Wake word configuration updated".to_string())
    } else {
        // если детектор еще не создан, создадим его сразу
        let mut detector = WakeWordDetector::new(config);
        detector
            .initialize()
            .await
            .map_err(|e| format!("Failed to create wake word detector: {}", e))?;
        *guard = Some(detector);
        Ok("Wake word detector created with new config".to_string())
    }
}

// ===== AUTHENTICATION COMMANDS =====

/// Войти в систему
#[tauri::command]
async fn login_user(username: String, password: String, remember_me: bool) -> Result<String, String> {
    let config = crate::api::ApiConfig::default();
    let session_manager = SessionManager::new(config)
        .map_err(|e| format!("Failed to create session manager: {}", e))?;

    let request = LoginRequest {
        username,
        password,
        remember_me,
    };

    match session_manager.login(request).await {
        Ok(response) => Ok(format!("Login successful: {}", response.user.username)),
        Err(e) => Err(format!("Login failed: {}", e)),
    }
}

/// Зарегистрироваться
#[tauri::command]
async fn register_user(username: String, email: String, password: String, confirm_password: String) -> Result<String, String> {
    let config = crate::api::ApiConfig::default();
    let session_manager = SessionManager::new(config)
        .map_err(|e| format!("Failed to create session manager: {}", e))?;

    let request = RegisterRequest {
        username,
        email,
        password,
        confirm_password,
    };

    match session_manager.register(request).await {
        Ok(response) => Ok(format!("Registration successful: {}", response.user.username)),
        Err(e) => Err(format!("Registration failed: {}", e)),
    }
}

/// Выйти из системы
#[tauri::command]
async fn logout_user() -> Result<String, String> {
    let config = crate::api::ApiConfig::default();
    let session_manager = SessionManager::new(config)
        .map_err(|e| format!("Failed to create session manager: {}", e))?;

    match session_manager.logout().await {
        Ok(_) => Ok("Logout successful".to_string()),
        Err(e) => Err(format!("Logout failed: {}", e)),
    }
}

/// Получить текущего пользователя
#[tauri::command]
async fn get_current_user() -> Result<String, String> {
    let config = crate::api::ApiConfig::default();
    let session_manager = SessionManager::new(config)
        .map_err(|e| format!("Failed to create session manager: {}", e))?;

    match session_manager.get_current_user().await {
        Some(user) => Ok(format!("Current user: {}", user.username)),
        None => Err("No user logged in".to_string()),
    }
}


// ===== AUDIO PROCESSING COMMANDS =====

/// Инициализировать аудио систему
#[tauri::command]
async fn initialize_audio_system(audio_state: State<'_, AudioState>) -> Result<String, String> {
    let mut initialized_guard = audio_state.initialized.lock().await;
    let mut config_guard = audio_state.config.lock().await;
    
    // Инициализируем аудио менеджер
    let mut audio_manager = AudioManager::new();
    match audio_manager.initialize().await {
        Ok(_) => {
            *initialized_guard = true;
            *config_guard = AudioConfig::default();
            Ok("Audio system initialized successfully".to_string())
        },
        Err(e) => Err(format!("Failed to initialize audio system: {}", e)),
    }
}

/// Получить список аудио устройств
#[tauri::command]
async fn get_audio_devices(audio_state: State<'_, AudioState>) -> Result<Vec<AudioDevice>, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    if *initialized_guard {
        let temp_manager = AudioManager::new();
        let devices = temp_manager.get_devices().await;
        Ok(devices)
    } else {
        Err("Audio system not initialized. Please initialize audio system first.".to_string())
    }
}

/// Начать захват аудио
#[tauri::command]
async fn start_audio_capture(sample_rate: u32, channels: u16, device_name: Option<String>, audio_state: State<'_, AudioState>) -> Result<String, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    let mut config_guard = audio_state.config.lock().await;
    
    if *initialized_guard {
        // Обновляем конфигурацию захвата
        config_guard.sample_rate = sample_rate;
        config_guard.channels = channels;
        if let Some(ref device) = device_name {
            config_guard.device_name = Some(device.clone());
        }
        
        // Симуляция успешного запуска захвата
        Ok(format!("Audio capture started with sample_rate: {}, channels: {}, device: {:?}", 
                   sample_rate, channels, device_name))
    } else {
        Err("Audio system not initialized. Please initialize audio system first.".to_string())
    }
}

/// Остановить захват аудио
#[tauri::command]
async fn stop_audio_capture(audio_state: State<'_, AudioState>) -> Result<String, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    
    if *initialized_guard {
        Ok("Audio capture stopped".to_string())
    } else {
        Err("Audio system not initialized.".to_string())
    }
}

/// Начать воспроизведение аудио
#[tauri::command]
async fn start_audio_playback(sample_rate: u32, channels: u16, device_name: Option<String>, audio_state: State<'_, AudioState>) -> Result<String, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    let mut config_guard = audio_state.config.lock().await;
    
    if *initialized_guard {
        // Обновляем конфигурацию воспроизведения
        config_guard.sample_rate = sample_rate;
        config_guard.channels = channels;
        if let Some(ref device) = device_name {
            config_guard.device_name = Some(device.clone());
        }
        
        // Симуляция успешного запуска воспроизведения
        Ok(format!("Audio playback started with sample_rate: {}, channels: {}, device: {:?}", 
                   sample_rate, channels, device_name))
    } else {
        Err("Audio system not initialized. Please initialize audio system first.".to_string())
    }
}

/// Остановить воспроизведение аудио
#[tauri::command]
async fn stop_audio_playback(audio_state: State<'_, AudioState>) -> Result<String, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    
    if *initialized_guard {
        // Симуляция успешной остановки воспроизведения
        Ok("Audio playback stopped".to_string())
    } else {
        Err("Audio system not initialized.".to_string())
    }
}

/// Добавить аудио в очередь воспроизведения
#[tauri::command]
async fn queue_audio_for_playback(samples: Vec<f32>, sample_rate: u32, channels: u16, audio_state: State<'_, AudioState>) -> Result<String, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    
    if *initialized_guard {
        // Симуляция успешного добавления аудио в очередь
        Ok(format!("Audio queued: {} samples, {}Hz, {} channels", samples.len(), sample_rate, channels))
    } else {
        Err("Audio system not initialized.".to_string())
    }
}

/// Получить данные для визуализации аудио
#[tauri::command]
async fn get_audio_visualization(samples: Vec<f32>, audio_state: State<'_, AudioState>) -> Result<VisualizationData, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    
    if *initialized_guard {
        let audio_data = AudioData::new(samples, 44100, 1);
        let mut visualizer = AudioVisualizer::new(1024, 32);
        let visualization_data = visualizer.update(&audio_data);
        Ok(visualization_data)
    } else {
        Err("Audio system not initialized.".to_string())
    }
}

/// Сохранить аудио в файл
#[tauri::command]
async fn save_audio_to_file(samples: Vec<f32>, sample_rate: u32, channels: u16, filename: String, audio_state: State<'_, AudioState>) -> Result<String, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    
    if *initialized_guard {
        // Симуляция успешного сохранения аудио
        Ok(format!("Audio saved to {}: {} samples, {}Hz, {} channels", filename, samples.len(), sample_rate, channels))
    } else {
        Err("Audio system not initialized.".to_string())
    }
}

/// Загрузить аудио из файла
#[tauri::command]
async fn load_audio_from_file(_filename: String, audio_state: State<'_, AudioState>) -> Result<AudioData, String> {
    let initialized_guard = audio_state.initialized.lock().await;
    
    if *initialized_guard {
        // Симуляция успешной загрузки аудио - возвращаем тестовые данные
        let test_samples = vec![0.0; 1000]; // 1000 тестовых сэмплов
        Ok(AudioData::new(test_samples, 44100, 1))
    } else {
        Err("Audio system not initialized.".to_string())
    }
}

// WebSocket команды

/// Создать WebSocket клиент
#[tauri::command]
async fn create_websocket_client(url: String) -> Result<String, String> {
    let config = websocket::WebSocketConfig {
        url,
        reconnect_interval: 5000,
        max_reconnect_attempts: 10,
        heartbeat_interval: 30000,
        timeout: 10000,
    };
    
    let _client = websocket::WebSocketClient::new(config);
    log::info!("WebSocket client created");
    Ok("WebSocket client created successfully".to_string())
}

/// Подключиться к WebSocket серверу
#[tauri::command]
async fn connect_websocket() -> Result<String, String> {
    log::info!("Connecting to WebSocket server");
    Ok("WebSocket connection initiated".to_string())
}

/// Отключиться от WebSocket сервера
#[tauri::command]
async fn disconnect_websocket() -> Result<String, String> {
    log::info!("Disconnecting from WebSocket server");
    Ok("WebSocket disconnection initiated".to_string())
}

/// Отправить WebSocket сообщение
#[tauri::command]
async fn send_websocket_message(message_type: String, data: serde_json::Value) -> Result<String, String> {
    let message = websocket::WebSocketMessage::new(message_type, data);
    log::info!("Sending WebSocket message: {:?}", message);
    Ok("WebSocket message sent".to_string())
}

/// Получить состояние WebSocket соединения
#[tauri::command]
async fn get_websocket_connection_state() -> Result<String, String> {
    // Симуляция получения состояния соединения
    Ok("Connected".to_string())
}

/// Получить количество попыток переподключения
#[tauri::command]
async fn get_websocket_reconnect_attempts() -> Result<u32, String> {
    // Симуляция получения количества попыток переподключения
    Ok(0)
}

/// Сбросить счетчик попыток переподключения
#[tauri::command]
async fn reset_websocket_reconnect_attempts() -> Result<String, String> {
    log::info!("Resetting WebSocket reconnect attempts counter");
    Ok("Reconnect attempts counter reset".to_string())
}

/// Получить время последнего heartbeat
#[tauri::command]
async fn get_websocket_last_heartbeat() -> Result<u64, String> {
    // Симуляция получения времени последнего heartbeat
    Ok(std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as u64)
}

#[tauri::command]
async fn probe_cpal_devices() -> Result<String, String> {
  let host = cpal::default_host();
  let default_in = host.default_input_device().map(|d| d.name().unwrap_or("unknown".into())).unwrap_or("<none>".into());
  let mut names: Vec<String> = Vec::new();
  match host.input_devices() {
    Ok(mut iter) => {
      for d in iter.by_ref() {
        names.push(d.name().unwrap_or("unknown".into()));
      }
    }
    Err(e) => return Err(format!("input_devices error: {}", e)),
  }
  Ok(format!("default: {}, inputs: {}", default_in, if names.is_empty() { "[]".into() } else { format!("{:?}", names) }))
}

// ===== НОВЫЕ КОМАНДЫ ИЗ PRILER/JARVIS =====

// Командная система
#[tauri::command]
async fn parse_commands() -> Result<String, String> {
    match commands::parse_commands() {
        Ok(commands) => Ok(format!("Parsed {} commands", commands.len())),
        Err(e) => Err(format!("Failed to parse commands: {}", e)),
    }
}

#[tauri::command]
async fn execute_command(command_path: String, command_config: CommandConfig) -> Result<String, String> {
    let path = std::path::PathBuf::from(command_path);
    match commands::execute_command(&path, &command_config) {
        Ok(success) => Ok(format!("Command executed: {}", success)),
        Err(e) => Err(format!("Failed to execute command: {}", e)),
    }
}

#[tauri::command]
async fn fetch_command(phrase: String, commands: Vec<AssistantCommand>) -> Result<String, String> {
    match commands::fetch_command(&phrase, &commands) {
        Some((path, config)) => Ok(format!("Found command: {:?} -> {:?}", path, config)),
        None => Err("No matching command found".to_string()),
    }
}

// STT система
#[tauri::command]
async fn init_stt_system() -> Result<String, String> {
    match stt::init() {
        Ok(_) => Ok("STT system initialized".to_string()),
        Err(_) => Err("Failed to initialize STT system".to_string()),
    }
}

#[tauri::command]
async fn recognize_speech(audio_data: Vec<i16>, partial: bool) -> Result<String, String> {
    match stt_recognize(&audio_data, partial) {
        Some(text) => Ok(text),
        None => Err("No speech recognized".to_string()),
    }
}

// Система записи
#[tauri::command]
async fn init_recorder() -> Result<String, String> {
    match recorder::init() {
        Ok(_) => Ok("Recorder initialized".to_string()),
        Err(_) => Err("Failed to initialize recorder".to_string()),
    }
}

#[tauri::command]
async fn start_recording() -> Result<String, String> {
    match recorder_start() {
        Ok(_) => Ok("Recording started".to_string()),
        Err(e) => Err(format!("Failed to start recording: {}", e)),
    }
}

#[tauri::command]
async fn stop_recording() -> Result<String, String> {
    match recorder_stop() {
        Ok(_) => Ok("Recording stopped".to_string()),
        Err(e) => Err(format!("Failed to stop recording: {}", e)),
    }
}

#[tauri::command]
async fn is_recording() -> bool {
    recorder_is_recording()
}

// Wake word система (уже есть)
// Аудио система (уже есть)
