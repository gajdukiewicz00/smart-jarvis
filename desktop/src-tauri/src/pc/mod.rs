//! PC Integration модули для SmartJARVIS Desktop
//! 
//! Этот модуль содержит все функции для интеграции с операционной системой:
//! - Управление файловой системой
//! - Управление процессами
//! - Системные настройки
//! - Сетевые функции

pub mod file_system;
pub mod process_manager;
pub mod system_settings;
pub mod network;

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Результат выполнения PC команды
#[derive(Debug, Serialize, Deserialize)]
pub struct PcCommandResult {
    pub success: bool,
    pub message: String,
    pub data: Option<serde_json::Value>,
}

/// Типы PC команд
#[derive(Debug, Serialize, Deserialize)]
pub enum PcCommandType {
    FileSystem,
    Process,
    System,
    Network,
}

/// PC команда
#[derive(Debug, Serialize, Deserialize)]
pub struct PcCommand {
    pub command_type: PcCommandType,
    pub action: String,
    pub parameters: HashMap<String, serde_json::Value>,
}

impl PcCommandResult {
    pub fn success(message: String) -> Self {
        Self {
            success: true,
            message,
            data: None,
        }
    }

    pub fn success_with_data(message: String, data: serde_json::Value) -> Self {
        Self {
            success: true,
            message,
            data: Some(data),
        }
    }

    pub fn error(message: String) -> Self {
        Self {
            success: false,
            message,
            data: None,
        }
    }
}
