//! Управление файловой системой
//! 
//! Этот модуль предоставляет функции для работы с файлами и директориями:
//! - CRUD операции с файлами
//! - Навигация по директориям
//! - Поиск файлов
//! - Операции с путями

use super::{PcCommand, PcCommandResult};
use serde::{Deserialize, Serialize};
use std::fs;
use std::path::{Path, PathBuf};
use std::collections::HashMap;
use walkdir::WalkDir;
use zip::ZipWriter;
use zip::write::FileOptions;
use std::io::Write;
use std::fs::File;
use tar::Builder as TarBuilder;
use flate2::write::GzEncoder;
use flate2::Compression;
use glob::Pattern;
use ignore::Walk;

#[derive(Debug, Serialize, Deserialize)]
pub struct FileInfo {
    pub name: String,
    pub path: String,
    pub size: u64,
    pub is_dir: bool,
    pub modified: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct DirectoryInfo {
    pub path: String,
    pub files: Vec<FileInfo>,
    pub subdirs: Vec<String>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ArchiveInfo {
    pub name: String,
    pub path: String,
    pub size: u64,
    pub file_count: usize,
    pub compression_ratio: f32,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SearchResult {
    pub file_path: String,
    pub file_name: String,
    pub file_size: u64,
    pub modified: String,
    pub match_type: String, // "name", "content", "both"
    pub match_context: Option<String>, // Контекст для поиска по содержимому
}

#[derive(Debug, Serialize, Deserialize)]
pub struct FilePermissions {
    pub readable: bool,
    pub writable: bool,
    pub executable: bool,
    pub owner_read: bool,
    pub owner_write: bool,
    pub owner_execute: bool,
    pub group_read: bool,
    pub group_write: bool,
    pub group_execute: bool,
    pub other_read: bool,
    pub other_write: bool,
    pub other_execute: bool,
}

/// Выполнить команду файловой системы
pub async fn execute_file_system_command(command: PcCommand) -> PcCommandResult {
    match command.action.as_str() {
        "list_directory" => list_directory(command),
        "create_file" => create_file(command),
        "read_file" => read_file(command),
        "write_file" => write_file(command),
        "delete_file" => delete_file(command),
        "create_directory" => create_directory(command),
        "delete_directory" => delete_directory(command),
        "search_files" => search_files(command),
        "get_file_info" => get_file_info(command),
        "copy_file" => copy_file(command),
        "move_file" => move_file(command),
        "create_zip_archive" => create_zip_archive(command),
        "extract_zip_archive" => extract_zip_archive(command),
        "create_tar_archive" => create_tar_archive(command),
        "extract_tar_archive" => extract_tar_archive(command),
        "search_files_by_name" => search_files_by_name(command),
        "search_files_by_content" => search_files_by_content(command),
        "get_file_permissions" => get_file_permissions(command),
        "set_file_permissions" => set_file_permissions(command),
        "get_archive_info" => get_archive_info(command),
        "navigate_directory" => navigate_directory(command),
        _ => PcCommandResult::error(format!("Unknown file system action: {}", command.action)),
    }
}

/// Получить список файлов в директории
fn list_directory(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or(".");

    let path_buf = PathBuf::from(path);
    
    if !path_buf.exists() {
        return PcCommandResult::error(format!("Path does not exist: {}", path));
    }

    if !path_buf.is_dir() {
        return PcCommandResult::error(format!("Path is not a directory: {}", path));
    }

    match fs::read_dir(&path_buf) {
        Ok(entries) => {
            let mut files = Vec::new();
            let mut subdirs = Vec::new();

            for entry in entries {
                if let Ok(entry) = entry {
                    let path = entry.path();
                    let metadata = entry.metadata().unwrap_or_else(|_| std::fs::metadata(".").unwrap());
                    
                    let file_info = FileInfo {
                        name: path.file_name()
                            .unwrap_or_default()
                            .to_string_lossy()
                            .to_string(),
                        path: path.to_string_lossy().to_string(),
                        size: metadata.len(),
                        is_dir: metadata.is_dir(),
                        modified: metadata.modified()
                            .unwrap_or_else(|_| std::time::SystemTime::now())
                            .duration_since(std::time::UNIX_EPOCH)
                            .unwrap_or_default()
                            .as_secs()
                            .to_string(),
                    };

                    if file_info.is_dir {
                        subdirs.push(file_info.name.clone());
                    }
                    files.push(file_info);
                }
            }

            let dir_info = DirectoryInfo {
                path: path.to_string(),
                files,
                subdirs,
            };

            PcCommandResult::success_with_data(
                format!("Listed directory: {}", path),
                serde_json::to_value(dir_info).unwrap_or_default()
            )
        }
        Err(e) => PcCommandResult::error(format!("Failed to read directory: {}", e)),
    }
}

/// Создать файл
fn create_file(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    
    let content = command.parameters.get("content")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if path.is_empty() {
        return PcCommandResult::error("Path is required".to_string());
    }

    match fs::write(path, content) {
        Ok(_) => PcCommandResult::success(format!("File created: {}", path)),
        Err(e) => PcCommandResult::error(format!("Failed to create file: {}", e)),
    }
}

/// Прочитать файл
fn read_file(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if path.is_empty() {
        return PcCommandResult::error("Path is required".to_string());
    }

    match fs::read_to_string(path) {
        Ok(content) => PcCommandResult::success_with_data(
            format!("File read: {}", path),
            serde_json::Value::String(content)
        ),
        Err(e) => PcCommandResult::error(format!("Failed to read file: {}", e)),
    }
}

/// Записать в файл
fn write_file(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    
    let content = command.parameters.get("content")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if path.is_empty() {
        return PcCommandResult::error("Path is required".to_string());
    }

    match fs::write(path, content) {
        Ok(_) => PcCommandResult::success(format!("File written: {}", path)),
        Err(e) => PcCommandResult::error(format!("Failed to write file: {}", e)),
    }
}

/// Удалить файл
fn delete_file(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if path.is_empty() {
        return PcCommandResult::error("Path is required".to_string());
    }

    match fs::remove_file(path) {
        Ok(_) => PcCommandResult::success(format!("File deleted: {}", path)),
        Err(e) => PcCommandResult::error(format!("Failed to delete file: {}", e)),
    }
}

/// Создать директорию
fn create_directory(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if path.is_empty() {
        return PcCommandResult::error("Path is required".to_string());
    }

    match fs::create_dir_all(path) {
        Ok(_) => PcCommandResult::success(format!("Directory created: {}", path)),
        Err(e) => PcCommandResult::error(format!("Failed to create directory: {}", e)),
    }
}

/// Удалить директорию
fn delete_directory(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if path.is_empty() {
        return PcCommandResult::error("Path is required".to_string());
    }

    match fs::remove_dir_all(path) {
        Ok(_) => PcCommandResult::success(format!("Directory deleted: {}", path)),
        Err(e) => PcCommandResult::error(format!("Failed to delete directory: {}", e)),
    }
}

/// Поиск файлов
fn search_files(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or(".");
    
    let pattern = command.parameters.get("pattern")
        .and_then(|v| v.as_str())
        .unwrap_or("*");

    let mut results = Vec::new();

    for entry in WalkDir::new(path).into_iter().filter_map(|e| e.ok()) {
        let path = entry.path();
        let file_name = path.file_name()
            .unwrap_or_default()
            .to_string_lossy();

        if file_name.contains(pattern) {
            let metadata = entry.metadata().unwrap_or_else(|_| std::fs::metadata(".").unwrap());
            let file_info = FileInfo {
                name: file_name.to_string(),
                path: path.to_string_lossy().to_string(),
                size: metadata.len(),
                is_dir: metadata.is_dir(),
                modified: metadata.modified()
                    .unwrap_or_else(|_| std::time::SystemTime::now())
                    .duration_since(std::time::UNIX_EPOCH)
                    .unwrap_or_default()
                    .as_secs()
                    .to_string(),
            };
            results.push(file_info);
        }
    }

    PcCommandResult::success_with_data(
        format!("Found {} files matching '{}'", results.len(), pattern),
        serde_json::to_value(results).unwrap_or_default()
    )
}

/// Получить информацию о файле
fn get_file_info(command: PcCommand) -> PcCommandResult {
    let path = command.parameters.get("path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if path.is_empty() {
        return PcCommandResult::error("Path is required".to_string());
    }

    match fs::metadata(path) {
        Ok(metadata) => {
            let file_info = FileInfo {
                name: Path::new(path).file_name()
                    .unwrap_or_default()
                    .to_string_lossy()
                    .to_string(),
                path: path.to_string(),
                size: metadata.len(),
                is_dir: metadata.is_dir(),
                modified: metadata.modified()
                    .unwrap_or_else(|_| std::time::SystemTime::now())
                    .duration_since(std::time::UNIX_EPOCH)
                    .unwrap_or_default()
                    .as_secs()
                    .to_string(),
            };

            PcCommandResult::success_with_data(
                format!("File info retrieved: {}", path),
                serde_json::to_value(file_info).unwrap_or_default()
            )
        }
        Err(e) => PcCommandResult::error(format!("Failed to get file info: {}", e)),
    }
}

/// Копировать файл
fn copy_file(command: PcCommand) -> PcCommandResult {
    let source = command.parameters.get("source")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    
    let destination = command.parameters.get("destination")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if source.is_empty() || destination.is_empty() {
        return PcCommandResult::error("Source and destination are required".to_string());
    }

    match fs::copy(source, destination) {
        Ok(_) => PcCommandResult::success(format!("File copied: {} -> {}", source, destination)),
        Err(e) => PcCommandResult::error(format!("Failed to copy file: {}", e)),
    }
}

/// Переместить файл
fn move_file(command: PcCommand) -> PcCommandResult {
    let source = command.parameters.get("source")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    
    let destination = command.parameters.get("destination")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if source.is_empty() || destination.is_empty() {
        return PcCommandResult::error("Source and destination are required".to_string());
    }

    match fs::rename(source, destination) {
        Ok(_) => PcCommandResult::success(format!("File moved: {} -> {}", source, destination)),
        Err(e) => PcCommandResult::error(format!("Failed to move file: {}", e)),
    }
}

/// Создать ZIP архив
fn create_zip_archive(command: PcCommand) -> PcCommandResult {
    let source_dir = command.parameters
        .get("source_dir")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let archive_path = command.parameters
        .get("archive_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if source_dir.is_empty() || archive_path.is_empty() {
        return PcCommandResult::error("Source directory and archive path are required".to_string());
    }

    match File::create(archive_path) {
        Ok(file) => {
            let mut zip = ZipWriter::new(file);
            let options = FileOptions::default()
                .compression_method(zip::CompressionMethod::Deflated)
                .unix_permissions(0o755);

            match add_directory_to_zip(&mut zip, source_dir, "", &options) {
                Ok(_) => {
                    match zip.finish() {
                        Ok(_) => PcCommandResult::success(format!("ZIP archive created: {}", archive_path)),
                        Err(e) => PcCommandResult::error(format!("Failed to finish ZIP archive: {}", e)),
                    }
                }
                Err(e) => PcCommandResult::error(format!("Failed to add files to ZIP: {}", e)),
            }
        }
        Err(e) => PcCommandResult::error(format!("Failed to create ZIP file: {}", e)),
    }
}

/// Добавить директорию в ZIP архив
fn add_directory_to_zip(
    zip: &mut ZipWriter<File>,
    source_dir: &str,
    archive_path: &str,
    options: &FileOptions,
) -> Result<(), Box<dyn std::error::Error>> {
    for entry in WalkDir::new(source_dir) {
        let entry = entry?;
        let path = entry.path();
        let name = path.strip_prefix(source_dir)?.to_string_lossy();

        if path.is_file() {
            zip.start_file(&*name, *options)?;
            let mut f = File::open(path)?;
            std::io::copy(&mut f, zip)?;
        } else if path.is_dir() && !name.is_empty() {
            zip.add_directory(&*name, *options)?;
        }
    }
    Ok(())
}

/// Извлечь ZIP архив
fn extract_zip_archive(command: PcCommand) -> PcCommandResult {
    let archive_path = command.parameters
        .get("archive_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let extract_to = command.parameters
        .get("extract_to")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if archive_path.is_empty() || extract_to.is_empty() {
        return PcCommandResult::error("Archive path and extract destination are required".to_string());
    }

    match fs::create_dir_all(extract_to) {
        Ok(_) => {
            match zip_extract_archive(archive_path, extract_to) {
                Ok(_) => PcCommandResult::success(format!("ZIP archive extracted to: {}", extract_to)),
                Err(e) => PcCommandResult::error(format!("Failed to extract ZIP: {}", e)),
            }
        }
        Err(e) => PcCommandResult::error(format!("Failed to create extract directory: {}", e)),
    }
}

/// Извлечь ZIP архив (внутренняя функция)
fn zip_extract_archive(archive_path: &str, extract_to: &str) -> Result<(), Box<dyn std::error::Error>> {
    let file = File::open(archive_path)?;
    let mut archive = zip::ZipArchive::new(file)?;

    for i in 0..archive.len() {
        let mut file = archive.by_index(i)?;
        let outpath = Path::new(extract_to).join(file.name());

        if file.name().ends_with('/') {
            fs::create_dir_all(&outpath)?;
        } else {
            if let Some(p) = outpath.parent() {
                if !p.exists() {
                    fs::create_dir_all(p)?;
                }
            }
            let mut outfile = File::create(&outpath)?;
            std::io::copy(&mut file, &mut outfile)?;
        }
    }
    Ok(())
}

/// Создать TAR архив
fn create_tar_archive(command: PcCommand) -> PcCommandResult {
    let source_dir = command.parameters
        .get("source_dir")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let archive_path = command.parameters
        .get("archive_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if source_dir.is_empty() || archive_path.is_empty() {
        return PcCommandResult::error("Source directory and archive path are required".to_string());
    }

    match File::create(archive_path) {
        Ok(file) => {
            let mut tar = TarBuilder::new(file);
            match add_directory_to_tar(&mut tar, source_dir) {
                Ok(_) => {
                    match tar.finish() {
                        Ok(_) => PcCommandResult::success(format!("TAR archive created: {}", archive_path)),
                        Err(e) => PcCommandResult::error(format!("Failed to finish TAR archive: {}", e)),
                    }
                }
                Err(e) => PcCommandResult::error(format!("Failed to add files to TAR: {}", e)),
            }
        }
        Err(e) => PcCommandResult::error(format!("Failed to create TAR file: {}", e)),
    }
}

/// Добавить директорию в TAR архив
fn add_directory_to_tar(tar: &mut TarBuilder<File>, source_dir: &str) -> Result<(), Box<dyn std::error::Error>> {
    for entry in WalkDir::new(source_dir) {
        let entry = entry?;
        let path = entry.path();
        let name = path.strip_prefix(source_dir)?.to_string_lossy();

        if path.is_file() {
            tar.append_path_with_name(path, &*name)?;
        } else if path.is_dir() && !name.is_empty() {
            tar.append_dir(&*name, path)?;
        }
    }
    Ok(())
}

/// Извлечь TAR архив
fn extract_tar_archive(command: PcCommand) -> PcCommandResult {
    let archive_path = command.parameters
        .get("archive_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let extract_to = command.parameters
        .get("extract_to")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if archive_path.is_empty() || extract_to.is_empty() {
        return PcCommandResult::error("Archive path and extract destination are required".to_string());
    }

    match fs::create_dir_all(extract_to) {
        Ok(_) => {
            match tar_extract_archive(archive_path, extract_to) {
                Ok(_) => PcCommandResult::success(format!("TAR archive extracted to: {}", extract_to)),
                Err(e) => PcCommandResult::error(format!("Failed to extract TAR: {}", e)),
            }
        }
        Err(e) => PcCommandResult::error(format!("Failed to create extract directory: {}", e)),
    }
}

/// Извлечь TAR архив (внутренняя функция)
fn tar_extract_archive(archive_path: &str, extract_to: &str) -> Result<(), Box<dyn std::error::Error>> {
    let file = File::open(archive_path)?;
    let mut archive = tar::Archive::new(file);

    archive.unpack(extract_to)?;
    Ok(())
}

/// Поиск файлов по имени
fn search_files_by_name(command: PcCommand) -> PcCommandResult {
    let search_dir = command.parameters
        .get("search_dir")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let pattern = command.parameters
        .get("pattern")
        .and_then(|v| v.as_str())
        .unwrap_or("*");

    if search_dir.is_empty() {
        return PcCommandResult::error("Search directory is required".to_string());
    }

    let mut results = Vec::new();
    let glob_pattern = format!("{}/**/{}", search_dir, pattern);

    match Pattern::new(&glob_pattern) {
        Ok(pat) => {
            for entry in WalkDir::new(search_dir) {
                if let Ok(entry) = entry {
                    let path = entry.path();
                    if path.is_file() && pat.matches_path(path) {
                        if let Ok(metadata) = path.metadata() {
                            results.push(SearchResult {
                                file_path: path.to_string_lossy().to_string(),
                                file_name: path.file_name().unwrap_or_default().to_string_lossy().to_string(),
                                file_size: metadata.len(),
                                modified: metadata.modified()
                                    .unwrap_or_else(|_| std::time::SystemTime::now())
                                    .duration_since(std::time::UNIX_EPOCH)
                                    .unwrap_or_default()
                                    .as_secs()
                                    .to_string(),
                                match_type: "name".to_string(),
                                match_context: None,
                            });
                        }
                    }
                }
            }
        }
        Err(e) => return PcCommandResult::error(format!("Invalid pattern: {}", e)),
    }

    PcCommandResult::success_with_data(
        "Search by name completed".to_string(),
        serde_json::to_value(results).unwrap_or_default()
    )
}

/// Поиск файлов по содержимому
fn search_files_by_content(command: PcCommand) -> PcCommandResult {
    let search_dir = command.parameters
        .get("search_dir")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let search_text = command.parameters
        .get("search_text")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let file_extensions = command.parameters
        .get("file_extensions")
        .and_then(|v| v.as_array())
        .map(|arr| arr.iter().filter_map(|v| v.as_str()).collect::<Vec<_>>())
        .unwrap_or_else(|| vec!["txt", "md", "rs", "js", "ts", "py", "java", "cpp", "c", "h"]);

    if search_dir.is_empty() || search_text.is_empty() {
        return PcCommandResult::error("Search directory and search text are required".to_string());
    }

    let mut results = Vec::new();

    for entry in WalkDir::new(search_dir) {
        if let Ok(entry) = entry {
            let path = entry.path();
            if path.is_file() {
                // Проверяем расширение файла
                if let Some(ext) = path.extension() {
                    if let Some(ext_str) = ext.to_str() {
                        if file_extensions.contains(&ext_str) {
                            // Читаем содержимое файла
                            if let Ok(content) = fs::read_to_string(path) {
                                if content.contains(search_text) {
                                    if let Ok(metadata) = path.metadata() {
                                        // Находим контекст вокруг найденного текста
                                        let context = find_context(&content, search_text);
                                        
                                        results.push(SearchResult {
                                            file_path: path.to_string_lossy().to_string(),
                                            file_name: path.file_name().unwrap_or_default().to_string_lossy().to_string(),
                                            file_size: metadata.len(),
                                            modified: metadata.modified()
                                                .unwrap_or_else(|_| std::time::SystemTime::now())
                                                .duration_since(std::time::UNIX_EPOCH)
                                                .unwrap_or_default()
                                                .as_secs()
                                                .to_string(),
                                            match_type: "content".to_string(),
                                            match_context: Some(context),
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    PcCommandResult::success_with_data(
        "Search by content completed".to_string(),
        serde_json::to_value(results).unwrap_or_default()
    )
}

/// Найти контекст вокруг найденного текста
fn find_context(content: &str, search_text: &str) -> String {
    if let Some(pos) = content.find(search_text) {
        let start = pos.saturating_sub(50);
        let end = (pos + search_text.len() + 50).min(content.len());
        format!("...{}...", &content[start..end])
    } else {
        String::new()
    }
}

/// Получить права доступа к файлу
fn get_file_permissions(command: PcCommand) -> PcCommandResult {
    let file_path = command.parameters
        .get("file_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if file_path.is_empty() {
        return PcCommandResult::error("File path is required".to_string());
    }

    match fs::metadata(file_path) {
        Ok(metadata) => {
            let permissions = metadata.permissions();
            let perms = FilePermissions {
                readable: permissions.readonly() == false,
                writable: permissions.readonly() == false,
                executable: false, // Упрощенная реализация
                owner_read: true,
                owner_write: permissions.readonly() == false,
                owner_execute: false,
                group_read: true,
                group_write: false,
                group_execute: false,
                other_read: true,
                other_write: false,
                other_execute: false,
            };
            PcCommandResult::success_with_data(
                "File permissions retrieved".to_string(),
                serde_json::to_value(perms).unwrap_or_default()
            )
        }
        Err(e) => PcCommandResult::error(format!("Failed to get file permissions: {}", e)),
    }
}

/// Установить права доступа к файлу
fn set_file_permissions(command: PcCommand) -> PcCommandResult {
    let file_path = command.parameters
        .get("file_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let readonly = command.parameters
        .get("readonly")
        .and_then(|v| v.as_bool())
        .unwrap_or(false);

    if file_path.is_empty() {
        return PcCommandResult::error("File path is required".to_string());
    }

    match fs::metadata(file_path) {
        Ok(metadata) => {
            let mut permissions = metadata.permissions();
            permissions.set_readonly(readonly);
            match fs::set_permissions(file_path, permissions) {
                Ok(_) => PcCommandResult::success(format!("File permissions updated: {}", file_path)),
                Err(e) => PcCommandResult::error(format!("Failed to set file permissions: {}", e)),
            }
        }
        Err(e) => PcCommandResult::error(format!("Failed to get file metadata: {}", e)),
    }
}

/// Получить информацию об архиве
fn get_archive_info(command: PcCommand) -> PcCommandResult {
    let archive_path = command.parameters
        .get("archive_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    if archive_path.is_empty() {
        return PcCommandResult::error("Archive path is required".to_string());
    }

    match fs::metadata(archive_path) {
        Ok(metadata) => {
            let archive_info = ArchiveInfo {
                name: Path::new(archive_path).file_name()
                    .unwrap_or_default()
                    .to_string_lossy()
                    .to_string(),
                path: archive_path.to_string(),
                size: metadata.len(),
                file_count: 0, // Упрощенная реализация
                compression_ratio: 0.0, // Упрощенная реализация
            };
            PcCommandResult::success_with_data(
                "Archive info retrieved".to_string(),
                serde_json::to_value(archive_info).unwrap_or_default()
            )
        }
        Err(e) => PcCommandResult::error(format!("Failed to get archive info: {}", e)),
    }
}

/// Навигация по директории
fn navigate_directory(command: PcCommand) -> PcCommandResult {
    let current_path = command.parameters
        .get("current_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let target_path = command.parameters
        .get("target_path")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    let base_path = if current_path.is_empty() {
        dirs::home_dir().unwrap_or_else(|| PathBuf::from("/"))
    } else {
        PathBuf::from(current_path)
    };

    let target = if target_path.is_empty() {
        base_path.clone()
    } else if target_path == ".." {
        base_path.parent().unwrap_or(&base_path).to_path_buf()
    } else if target_path == "." {
        base_path.clone()
    } else if target_path.starts_with('/') {
        PathBuf::from(target_path)
    } else {
        base_path.join(target_path)
    };

    match fs::metadata(&target) {
        Ok(metadata) => {
            if metadata.is_dir() {
                list_directory(PcCommand {
                    command_type: super::PcCommandType::FileSystem,
                    action: "list_directory".to_string(),
                    parameters: {
                        let mut params = HashMap::new();
                        params.insert("path".to_string(), serde_json::Value::String(target.to_string_lossy().to_string()));
                        params
                    },
                })
            } else {
                PcCommandResult::error("Target is not a directory".to_string())
            }
        }
        Err(e) => PcCommandResult::error(format!("Failed to navigate to directory: {}", e)),
    }
}
