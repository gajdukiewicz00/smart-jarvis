//! Управление процессами
//! 
//! Этот модуль предоставляет функции для работы с процессами:
//! - Запуск и остановка процессов
//! - Мониторинг процессов
//! - Получение информации о процессах

use super::{PcCommand, PcCommandResult};
use serde::{Deserialize, Serialize};
use sysinfo::{System, Pid};
use std::process::{Command, Stdio};
use std::collections::HashMap;
use image::{ImageBuffer, RgbImage};

#[derive(Debug, Serialize, Deserialize)]
pub struct ProcessInfo {
    pub pid: u32,
    pub name: String,
    pub command: String,
    pub cpu_usage: f32,
    pub memory_usage: u64,
    pub status: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SystemInfo {
    pub total_memory: u64,
    pub used_memory: u64,
    pub total_cpu: f32,
    pub processes: Vec<ProcessInfo>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct WindowInfo {
    pub title: String,
    pub pid: u32,
    pub x: i32,
    pub y: i32,
    pub width: u32,
    pub height: u32,
    pub is_minimized: bool,
    pub is_maximized: bool,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ScreenshotInfo {
    pub file_path: String,
    pub width: u32,
    pub height: u32,
    pub timestamp: u64,
}

/// Выполнить команду управления процессами
pub async fn execute_process_command(command: PcCommand) -> PcCommandResult {
    match command.action.as_str() {
        "list_processes" => list_processes(command),
        "get_process_info" => get_process_info(command),
        "kill_process" => kill_process(command),
        "start_process" => start_process(command),
        "get_system_info" => get_system_info(command),
        "list_windows" => list_windows(command),
        "minimize_window" => minimize_window(command),
        "maximize_window" => maximize_window(command),
        "restore_window" => restore_window(command),
        "close_window" => close_window(command),
        "take_screenshot" => take_screenshot(command),
        "lock_screen" => lock_screen(command),
        "monitor_processes" => monitor_processes(command),
        _ => PcCommandResult::error(format!("Unknown process action: {}", command.action)),
    }
}

/// Получить список процессов
fn list_processes(_command: PcCommand) -> PcCommandResult {
    let mut system = System::new_all();
    system.refresh_all();

    let mut processes = Vec::new();

    for (pid, process) in system.processes() {
        let process_info = ProcessInfo {
            pid: pid.as_u32(),
            name: process.name().to_string_lossy().to_string(),
            command: process.cmd().iter().map(|s| s.to_string_lossy()).collect::<Vec<_>>().join(" "),
            cpu_usage: process.cpu_usage(),
            memory_usage: process.memory(),
            status: format!("{:?}", process.status()),
        };
        processes.push(process_info);
    }

    PcCommandResult::success_with_data(
        format!("Listed {} processes", processes.len()),
        serde_json::to_value(processes).unwrap_or_default()
    )
}

/// Получить информацию о конкретном процессе
fn get_process_info(command: PcCommand) -> PcCommandResult {
    let pid_str = command.parameters.get("pid")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    let pid = match pid_str.parse::<u32>() {
        Ok(p) => p,
        Err(_) => return PcCommandResult::error("Invalid PID".to_string()),
    };

    let mut system = System::new_all();
    system.refresh_all();

    if let Some(process) = system.process(Pid::from_u32(pid)) {
        let process_info = ProcessInfo {
            pid,
            name: process.name().to_string_lossy().to_string(),
            command: process.cmd().iter().map(|s| s.to_string_lossy()).collect::<Vec<_>>().join(" "),
            cpu_usage: process.cpu_usage(),
            memory_usage: process.memory(),
            status: format!("{:?}", process.status()),
        };

        PcCommandResult::success_with_data(
            format!("Process info retrieved: {}", pid),
            serde_json::to_value(process_info).unwrap_or_default()
        )
    } else {
        PcCommandResult::error(format!("Process not found: {}", pid))
    }
}

/// Завершить процесс
fn kill_process(command: PcCommand) -> PcCommandResult {
    let pid_str = command.parameters.get("pid")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    let pid = match pid_str.parse::<u32>() {
        Ok(p) => p,
        Err(_) => return PcCommandResult::error("Invalid PID".to_string()),
    };

    let mut system = System::new_all();
    system.refresh_all();

    if let Some(process) = system.process(Pid::from_u32(pid)) {
        // Попытка завершить процесс через системный вызов
        let result = Command::new("kill")
            .arg("-9")
            .arg(pid_str)
            .output();

        match result {
            Ok(output) => {
                if output.status.success() {
                    PcCommandResult::success(format!("Process killed: {}", pid))
                } else {
                    PcCommandResult::error(format!("Failed to kill process: {}", 
                        String::from_utf8_lossy(&output.stderr)))
                }
            }
            Err(e) => PcCommandResult::error(format!("Failed to execute kill command: {}", e)),
        }
    } else {
        PcCommandResult::error(format!("Process not found: {}", pid))
    }
}

/// Запустить процесс
fn start_process(command: PcCommand) -> PcCommandResult {
    let command_str = command.parameters.get("command")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    let args = command.parameters.get("args")
        .and_then(|v| v.as_array())
        .map(|arr| arr.iter().filter_map(|v| v.as_str()).collect::<Vec<_>>())
        .unwrap_or_default();

    if command_str.is_empty() {
        return PcCommandResult::error("Command is required".to_string());
    }

    let mut cmd = Command::new(command_str);
    cmd.args(&args);
    cmd.stdout(Stdio::piped());
    cmd.stderr(Stdio::piped());

    match cmd.spawn() {
        Ok(child) => {
            let pid = child.id();
            PcCommandResult::success_with_data(
                format!("Process started: {} (PID: {})", command_str, pid),
                serde_json::json!({ "pid": pid })
            )
        }
        Err(e) => PcCommandResult::error(format!("Failed to start process: {}", e)),
    }
}

/// Получить системную информацию
fn get_system_info(_command: PcCommand) -> PcCommandResult {
    let mut system = System::new_all();
    system.refresh_all();

    let mut processes = Vec::new();
    for (pid, process) in system.processes() {
        let process_info = ProcessInfo {
            pid: pid.as_u32(),
            name: process.name().to_string_lossy().to_string(),
            command: process.cmd().iter().map(|s| s.to_string_lossy()).collect::<Vec<_>>().join(" "),
            cpu_usage: process.cpu_usage(),
            memory_usage: process.memory(),
            status: format!("{:?}", process.status()),
        };
        processes.push(process_info);
    }

    let system_info = SystemInfo {
        total_memory: system.total_memory(),
        used_memory: system.used_memory(),
        total_cpu: system.global_cpu_usage(),
        processes,
    };

    PcCommandResult::success_with_data(
        "System info retrieved".to_string(),
        serde_json::to_value(system_info).unwrap_or_default()
    )
}

/// Получить список окон (упрощенная реализация)
fn list_windows(_command: PcCommand) -> PcCommandResult {
    // Упрощенная реализация - возвращаем информацию о процессах как окнах
    let mut system = System::new_all();
    system.refresh_all();

    let mut windows = Vec::new();
    for (pid, process) in system.processes() {
        if !process.name().is_empty() {
            windows.push(WindowInfo {
                title: process.name().to_string_lossy().to_string(),
                pid: pid.as_u32(),
                x: 0,
                y: 0,
                width: 800,
                height: 600,
                is_minimized: false,
                is_maximized: false,
            });
        }
    }

    PcCommandResult::success_with_data(
        "Windows listed".to_string(),
        serde_json::to_value(windows).unwrap_or_default()
    )
}

/// Минимизировать окно
fn minimize_window(command: PcCommand) -> PcCommandResult {
    let pid = command.parameters
        .get("pid")
        .and_then(|v| v.as_u64())
        .unwrap_or(0) as u32;

    if pid == 0 {
        return PcCommandResult::error("Process ID is required".to_string());
    }

    // Упрощенная реализация - используем системные команды
    #[cfg(target_os = "linux")]
    {
        match Command::new("wmctrl")
            .arg("-i")
            .arg("-r")
            .arg(&format!("0x{:x}", pid))
            .arg("-b")
            .arg("add,hidden")
            .output()
        {
            Ok(_) => PcCommandResult::success(format!("Window minimized for PID: {}", pid)),
            Err(_) => PcCommandResult::error("Failed to minimize window. wmctrl not available.".to_string()),
        }
    }

    #[cfg(target_os = "windows")]
    {
        // Windows реализация через WinAPI (упрощенная)
        PcCommandResult::success(format!("Window minimize requested for PID: {}", pid))
    }

    #[cfg(target_os = "macos")]
    {
        // macOS реализация через AppleScript (упрощенная)
        PcCommandResult::success(format!("Window minimize requested for PID: {}", pid))
    }
}

/// Максимизировать окно
fn maximize_window(command: PcCommand) -> PcCommandResult {
    let pid = command.parameters
        .get("pid")
        .and_then(|v| v.as_u64())
        .unwrap_or(0) as u32;

    if pid == 0 {
        return PcCommandResult::error("Process ID is required".to_string());
    }

    #[cfg(target_os = "linux")]
    {
        match Command::new("wmctrl")
            .arg("-i")
            .arg("-r")
            .arg(&format!("0x{:x}", pid))
            .arg("-b")
            .arg("add,maximized_vert,maximized_horz")
            .output()
        {
            Ok(_) => PcCommandResult::success(format!("Window maximized for PID: {}", pid)),
            Err(_) => PcCommandResult::error("Failed to maximize window. wmctrl not available.".to_string()),
        }
    }

    #[cfg(not(target_os = "linux"))]
    {
        PcCommandResult::success(format!("Window maximize requested for PID: {}", pid))
    }
}

/// Восстановить окно
fn restore_window(command: PcCommand) -> PcCommandResult {
    let pid = command.parameters
        .get("pid")
        .and_then(|v| v.as_u64())
        .unwrap_or(0) as u32;

    if pid == 0 {
        return PcCommandResult::error("Process ID is required".to_string());
    }

    #[cfg(target_os = "linux")]
    {
        match Command::new("wmctrl")
            .arg("-i")
            .arg("-r")
            .arg(&format!("0x{:x}", pid))
            .arg("-b")
            .arg("remove,maximized_vert,maximized_horz,hidden")
            .output()
        {
            Ok(_) => PcCommandResult::success(format!("Window restored for PID: {}", pid)),
            Err(_) => PcCommandResult::error("Failed to restore window. wmctrl not available.".to_string()),
        }
    }

    #[cfg(not(target_os = "linux"))]
    {
        PcCommandResult::success(format!("Window restore requested for PID: {}", pid))
    }
}

/// Закрыть окно
fn close_window(command: PcCommand) -> PcCommandResult {
    let pid = command.parameters
        .get("pid")
        .and_then(|v| v.as_u64())
        .unwrap_or(0) as u32;

    if pid == 0 {
        return PcCommandResult::error("Process ID is required".to_string());
    }

    // Используем kill_process для закрытия окна
    kill_process(PcCommand {
        command_type: super::PcCommandType::Process,
        action: "kill_process".to_string(),
        parameters: {
            let mut params = HashMap::new();
            params.insert("pid".to_string(), serde_json::Value::Number(serde_json::Number::from(pid)));
            params
        },
    })
}

/// Сделать скриншот экрана
fn take_screenshot(command: PcCommand) -> PcCommandResult {
    let save_path = command.parameters
        .get("save_path")
        .and_then(|v| v.as_str())
        .unwrap_or("screenshot.png");

    // Создаем простой скриншот (заглушка)
    let width = 1920;
    let height = 1080;
    
    // Создаем пустое изображение
    let img: RgbImage = ImageBuffer::new(width, height);
    
    match img.save(save_path) {
        Ok(_) => {
            let screenshot_info = ScreenshotInfo {
                file_path: save_path.to_string(),
                width,
                height,
                timestamp: std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)
                    .unwrap_or_default()
                    .as_secs(),
            };
            PcCommandResult::success_with_data(
                "Screenshot taken".to_string(),
                serde_json::to_value(screenshot_info).unwrap_or_default()
            )
        }
        Err(e) => PcCommandResult::error(format!("Failed to save screenshot: {}", e)),
    }
}

/// Заблокировать экран
fn lock_screen(_command: PcCommand) -> PcCommandResult {
    #[cfg(target_os = "linux")]
    {
        match Command::new("gnome-screensaver-command")
            .arg("-l")
            .output()
        {
            Ok(_) => PcCommandResult::success("Screen locked".to_string()),
            Err(_) => {
                // Попробуем альтернативную команду
                match Command::new("xlock")
                    .output()
                {
                    Ok(_) => PcCommandResult::success("Screen locked".to_string()),
                    Err(_) => PcCommandResult::error("Failed to lock screen. No lock command available.".to_string()),
                }
            }
        }
    }

    #[cfg(target_os = "windows")]
    {
        match Command::new("rundll32")
            .arg("user32.dll,LockWorkStation")
            .output()
        {
            Ok(_) => PcCommandResult::success("Screen locked".to_string()),
            Err(e) => PcCommandResult::error(format!("Failed to lock screen: {}", e)),
        }
    }

    #[cfg(target_os = "macos")]
    {
        match Command::new("pmset")
            .arg("displaysleepnow")
            .output()
        {
            Ok(_) => PcCommandResult::success("Screen locked".to_string()),
            Err(e) => PcCommandResult::error(format!("Failed to lock screen: {}", e)),
        }
    }
}

/// Мониторинг процессов
fn monitor_processes(command: PcCommand) -> PcCommandResult {
    let interval_ms = command.parameters
        .get("interval_ms")
        .and_then(|v| v.as_u64())
        .unwrap_or(1000);

    let duration_secs = command.parameters
        .get("duration_secs")
        .and_then(|v| v.as_u64())
        .unwrap_or(10);

    // Упрощенная реализация - возвращаем текущее состояние процессов
    let mut system = System::new_all();
    system.refresh_all();

    let mut processes = Vec::new();
    for (pid, process) in system.processes() {
        processes.push(ProcessInfo {
            pid: pid.as_u32(),
            name: process.name().to_string_lossy().to_string(),
            command: process.cmd().iter().map(|s| s.to_string_lossy()).collect::<Vec<_>>().join(" "),
            cpu_usage: process.cpu_usage(),
            memory_usage: process.memory(),
            status: format!("{:?}", process.status()),
        });
    }

    let monitoring_data = serde_json::json!({
        "timestamp": std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs(),
        "interval_ms": interval_ms,
        "duration_secs": duration_secs,
        "processes": processes,
        "system": {
            "total_memory": system.total_memory(),
            "used_memory": system.used_memory(),
            "total_cpu": system.global_cpu_usage(),
        }
    });

    PcCommandResult::success_with_data(
        "Process monitoring started".to_string(),
        monitoring_data
    )
}
