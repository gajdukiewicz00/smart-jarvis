//! Управление системными настройками
//! 
//! Этот модуль предоставляет функции для работы с системными настройками:
//! - Информация об операционной системе
//! - Системные переменные окружения
//! - Информация о пользователе
//! - Системные ресурсы

use super::{PcCommand, PcCommandResult};
use serde::{Deserialize, Serialize};
use sysinfo::System;
use std::env;
use std::collections::HashMap;

#[derive(Debug, Serialize, Deserialize)]
pub struct OsInfo {
    pub name: String,
    pub version: String,
    pub kernel_version: String,
    pub hostname: String,
    pub architecture: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct UserInfo {
    pub username: String,
    pub home_dir: String,
    pub current_dir: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct EnvironmentInfo {
    pub variables: HashMap<String, String>,
}

/// Выполнить команду системных настроек
pub async fn execute_system_command(command: PcCommand) -> PcCommandResult {
    match command.action.as_str() {
        "get_os_info" => get_os_info(command),
        "get_user_info" => get_user_info(command),
        "get_environment" => get_environment(command),
        "get_environment_variable" => get_environment_variable(command),
        "set_environment_variable" => set_environment_variable(command),
        "get_system_resources" => get_system_resources(command),
        _ => PcCommandResult::error(format!("Unknown system action: {}", command.action)),
    }
}

/// Получить информацию об операционной системе
fn get_os_info(command: PcCommand) -> PcCommandResult {
    let system = System::new_all();
    
    let os_info = OsInfo {
        name: System::name().unwrap_or_else(|| "Unknown".to_string()),
        version: System::os_version().unwrap_or_else(|| "Unknown".to_string()),
        kernel_version: System::kernel_version().unwrap_or_else(|| "Unknown".to_string()),
        hostname: System::host_name().unwrap_or_else(|| "Unknown".to_string()),
        architecture: std::env::consts::ARCH.to_string(),
    };

    PcCommandResult::success_with_data(
        "OS info retrieved".to_string(),
        serde_json::to_value(os_info).unwrap_or_default()
    )
}

/// Получить информацию о пользователе
fn get_user_info(command: PcCommand) -> PcCommandResult {
    let user_info = UserInfo {
        username: env::var("USER").unwrap_or_else(|_| "Unknown".to_string()),
        home_dir: env::var("HOME").unwrap_or_else(|_| "Unknown".to_string()),
        current_dir: env::current_dir()
            .unwrap_or_default()
            .to_string_lossy()
            .to_string(),
    };

    PcCommandResult::success_with_data(
        "User info retrieved".to_string(),
        serde_json::to_value(user_info).unwrap_or_default()
    )
}

/// Получить все переменные окружения
fn get_environment(command: PcCommand) -> PcCommandResult {
    let mut variables = HashMap::new();
    
    for (key, value) in env::vars() {
        variables.insert(key, value);
    }

    let env_info = EnvironmentInfo { variables };

    PcCommandResult::success_with_data(
        "Environment variables retrieved".to_string(),
        serde_json::to_value(env_info).unwrap_or_default()
    )
}

/// Получить конкретную переменную окружения
fn get_environment_variable(command: PcCommand) -> PcCommandResult {
    let var_name = command.parameters.get("name")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if var_name.is_empty() {
        return PcCommandResult::error("Variable name is required".to_string());
    }

    match env::var(var_name) {
        Ok(value) => PcCommandResult::success_with_data(
            format!("Environment variable retrieved: {}", var_name),
            serde_json::Value::String(value)
        ),
        Err(_) => PcCommandResult::error(format!("Environment variable not found: {}", var_name)),
    }
}

/// Установить переменную окружения
fn set_environment_variable(command: PcCommand) -> PcCommandResult {
    let var_name = command.parameters.get("name")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    
    let var_value = command.parameters.get("value")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if var_name.is_empty() {
        return PcCommandResult::error("Variable name is required".to_string());
    }

    env::set_var(var_name, var_value);

    PcCommandResult::success(format!("Environment variable set: {}={}", var_name, var_value))
}

/// Получить информацию о системных ресурсах
fn get_system_resources(command: PcCommand) -> PcCommandResult {
    let mut system = System::new_all();
    system.refresh_all();

    let resources = serde_json::json!({
        "total_memory": system.total_memory(),
        "used_memory": system.used_memory(),
        "free_memory": system.free_memory(),
        "total_swap": system.total_swap(),
        "used_swap": system.used_swap(),
        "free_swap": system.free_swap(),
        "cpu_count": system.cpus().len(),
        "cpu_usage": system.global_cpu_usage(),
        "uptime": System::uptime(),
        "boot_time": System::boot_time(),
    });

    PcCommandResult::success_with_data(
        "System resources retrieved".to_string(),
        resources
    )
}
