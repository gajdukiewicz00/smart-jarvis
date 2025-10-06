use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Модели для голосовых сервисов

#[derive(Debug, Serialize, Deserialize)]
pub struct AudioChunk {
    pub session_id: String,
    pub user_id: String,
    pub audio_data: Vec<u8>,
    pub sample_rate: u32,
    pub channels: u16,
    pub format: String,
    pub timestamp: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct TranscriptionResult {
    pub text: String,
    pub confidence: f32,
    pub language: String,
    pub duration_ms: u64,
    pub is_final: bool,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct IntentResult {
    pub intent: String,
    pub confidence: f32,
    pub entities: HashMap<String, String>,
    pub slots: HashMap<String, serde_json::Value>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct DialogState {
    pub state_id: String,
    pub context: HashMap<String, serde_json::Value>,
    pub history: Vec<DialogTurn>,
    pub current_intent: Option<String>,
    pub pending_actions: Vec<String>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct DialogTurn {
    pub turn_id: String,
    pub user_input: String,
    pub intent: Option<String>,
    pub entities: HashMap<String, String>,
    pub system_response: String,
    pub timestamp: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct TtsRequest {
    pub text: String,
    pub voice: String,
    pub speed: f32,
    pub pitch: f32,
    pub volume: f32,
    pub format: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct TtsResponse {
    pub audio_data: Vec<u8>,
    pub duration_ms: u64,
    pub format: String,
    pub sample_rate: u32,
}

/// Модели для доменных сервисов

#[derive(Debug, Serialize, Deserialize)]
pub struct TodoTask {
    pub id: String,
    pub title: String,
    pub description: Option<String>,
    pub status: TaskStatus,
    pub priority: TaskPriority,
    pub due_date: Option<u64>,
    pub created_at: u64,
    pub updated_at: u64,
    pub user_id: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum TaskStatus {
    Pending,
    InProgress,
    Completed,
    Cancelled,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum TaskPriority {
    Low,
    Medium,
    High,
    Critical,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateTaskRequest {
    pub title: String,
    pub description: Option<String>,
    pub priority: TaskPriority,
    pub due_date: Option<u64>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct UpdateTaskRequest {
    pub title: Option<String>,
    pub description: Option<String>,
    pub status: Option<TaskStatus>,
    pub priority: Option<TaskPriority>,
    pub due_date: Option<u64>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MoneyTransaction {
    pub id: String,
    pub amount: f64,
    pub currency: String,
    pub category: String,
    pub description: String,
    pub transaction_type: TransactionType,
    pub date: u64,
    pub user_id: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum TransactionType {
    Income,
    Expense,
    Transfer,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateTransactionRequest {
    pub amount: f64,
    pub currency: String,
    pub category: String,
    pub description: String,
    pub transaction_type: TransactionType,
    pub date: Option<u64>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CalendarEvent {
    pub id: String,
    pub title: String,
    pub description: Option<String>,
    pub start_time: u64,
    pub end_time: u64,
    pub location: Option<String>,
    pub attendees: Vec<String>,
    pub reminders: Vec<Reminder>,
    pub user_id: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Reminder {
    pub minutes_before: u32,
    pub message: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateEventRequest {
    pub title: String,
    pub description: Option<String>,
    pub start_time: u64,
    pub end_time: u64,
    pub location: Option<String>,
    pub attendees: Vec<String>,
    pub reminders: Vec<Reminder>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MemoryEntry {
    pub id: String,
    pub user_id: String,
    pub session_id: String,
    pub content: String,
    pub context: HashMap<String, serde_json::Value>,
    pub embeddings: Vec<f32>,
    pub timestamp: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateMemoryRequest {
    pub content: String,
    pub context: HashMap<String, serde_json::Value>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SearchMemoryRequest {
    pub query: String,
    pub limit: Option<u32>,
    pub similarity_threshold: Option<f32>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SearchMemoryResponse {
    pub memories: Vec<MemoryEntry>,
    pub similarity_scores: Vec<f32>,
}
