use serde::{Deserialize, Serialize};
use std::path::PathBuf;
use once_cell::sync::Lazy;
use platform_dirs::AppDirs;
// rustpotter временно отключен из-за проблем с версиями
// use rustpotter::{RustpotterConfig, WavFmt, DetectorConfig, FiltersConfig, ScoreMode, GainNormalizationConfig, BandPassConfig};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum WakeWordEngine {
    #[serde(rename = "rustpotter")]
    Rustpotter,
    #[serde(rename = "porcupine")]
    Porcupine,
    #[serde(rename = "vosk")]
    Vosk,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum SpeechToTextEngine {
    #[serde(rename = "vosk")]
    Vosk,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum RecorderType {
    #[serde(rename = "cpal")]
    Cpal,
    #[serde(rename = "pvrecorder")]
    PvRecorder,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum AudioType {
    #[serde(rename = "kira")]
    Kira,
    #[serde(rename = "rodio")]
    Rodio,
}

pub static APP_DIRS: Lazy<AppDirs> = Lazy::new(|| {
    AppDirs::new(Some("com.smartjarvis.desktop"), false).unwrap()
});

pub static APP_CONFIG_DIR: Lazy<PathBuf> = Lazy::new(|| {
    let mut config_dir = PathBuf::from(&APP_DIRS.config_dir);
    if !config_dir.exists() {
        std::fs::create_dir_all(&config_dir).expect("Cannot create config directory");
    }
    config_dir
});

pub static APP_LOG_DIR: Lazy<PathBuf> = Lazy::new(|| {
    let mut log_dir = PathBuf::from(&APP_DIRS.config_dir);
    if !log_dir.exists() {
        std::fs::create_dir_all(&log_dir).expect("Cannot create log directory");
    }
    log_dir
});

pub static APP_DATA_DIR: Lazy<PathBuf> = Lazy::new(|| {
    let mut data_dir = PathBuf::from(&APP_DIRS.data_dir);
    if !data_dir.exists() {
        std::fs::create_dir_all(&data_dir).expect("Cannot create data directory");
    }
    data_dir
});

// Константы по умолчанию
pub const DEFAULT_AUDIO_TYPE: AudioType = AudioType::Kira;
pub const DEFAULT_RECORDER_TYPE: RecorderType = RecorderType::Cpal;
pub const DEFAULT_WAKE_WORD_ENGINE: WakeWordEngine = WakeWordEngine::Rustpotter;
pub const DEFAULT_SPEECH_TO_TEXT_ENGINE: SpeechToTextEngine = SpeechToTextEngine::Vosk;

pub const DEFAULT_VOICE: &str = "jarvis-default";

pub const BUNDLE_IDENTIFIER: &str = "com.smartjarvis.desktop";
pub const DB_FILE_NAME: &str = "app.db";
pub const LOG_FILE_NAME: &str = "log.txt";
pub const APP_VERSION: Option<&str> = option_env!("CARGO_PKG_VERSION");

// Пути к ресурсам
pub const COMMANDS_PATH: &str = "commands/";
pub const KEYWORDS_PATH: &str = "keywords/";
pub const DEFAULT_KEYWORD: &str = "jarvis-default.ppn";
pub const DEFAULT_SENSITIVITY: f32 = 1.0;

// Vosk настройки
pub const VOSK_MODEL_PATH: &str = "models/vosk/model";
pub const VOSK_FETCH_PHRASE: &str = "jarvis";
pub const VOSK_MIN_RATIO: f64 = 70.0;

// Rustpotter настройки - временно отключены
pub const RUSPOTTER_MIN_SCORE: f32 = 0.62;
// pub const RUSTPOTTER_DEFAULT_CONFIG: Lazy<RustpotterConfig> = Lazy::new(|| {
//     RustpotterConfig {
//         fmt: WavFmt::default(),
//         detector: DetectorConfig {
//             avg_threshold: 0.,
//             threshold: 0.5,
//             min_scores: 15,
//             score_mode: ScoreMode::Average,
//             comparator_band_size: 5,
//             comparator_ref: 0.22
//         },
//         filters: FiltersConfig {
//             gain_normalizer: GainNormalizationConfig {
//                 enabled: true,
//                 gain_ref: None,
//                 min_gain: 0.7,
//                 max_gain: 1.0,
//             },
//             band_pass: BandPassConfig {
//                 enabled: true,
//                 low_cutoff: 80.,
//                 high_cutoff: 400.,
//             }
//         }
//     }
// });

// Командная система
pub const CMD_RATIO_THRESHOLD: f64 = 65.0;
pub const CMD_WAIT_DELAY: std::time::Duration = std::time::Duration::from_secs(15);

// Фразы ассистента
pub const ASSISTANT_GREET_PHRASES: [&str; 3] = ["greet1", "greet2", "greet3"];
pub const ASSISTANT_PHRASES_TBR: [&str; 17] = [
    "jarvis",
    "слушаю",
    "всегда к услугам",
    "произнеси",
    "ответь",
    "покажи",
    "скажи",
    "давай",
    "да",
    "к вашим услугам",
    "всегда к вашим услугам",
    "запрос выполнен",
    "выполнен",
    "есть",
    "загружаю",
    "очень тонкое замечание",
    "джарвис",
];

pub fn init_dirs() -> Result<(), String> {
    // Директории уже инициализированы в lazy static
    Ok(())
}
