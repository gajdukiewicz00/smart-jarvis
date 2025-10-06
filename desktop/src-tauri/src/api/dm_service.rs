use crate::api::{ApiClient, ApiResponse};
use crate::api::models::{DialogState, DialogTurn, IntentResult};
use anyhow::Result;
use std::collections::HashMap;

/// Клиент для DM (Dialog Management) сервиса
pub struct DmServiceClient {
    client: ApiClient,
}

impl DmServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Обработать новый диалоговый ход
    pub async fn process_turn(&self, session_id: &str, user_input: &str, intent_result: IntentResult) -> Result<ApiResponse<DialogState>> {
        let data = serde_json::json!({
            "session_id": session_id,
            "user_input": user_input,
            "intent_result": intent_result,
            "timestamp": chrono::Utc::now().timestamp_millis()
        });
        self.client.post("/api/v1/dm/turn", &data).await
    }

    /// Получить текущее состояние диалога
    pub async fn get_dialog_state(&self, session_id: &str) -> Result<ApiResponse<DialogState>> {
        self.client.get(&format!("/api/v1/dm/state/{}", session_id)).await
    }

    /// Обновить состояние диалога
    pub async fn update_dialog_state(&self, session_id: &str, state: DialogState) -> Result<ApiResponse<String>> {
        self.client.put(&format!("/api/v1/dm/state/{}", session_id), &state).await
    }

    /// Получить историю диалога
    pub async fn get_dialog_history(&self, session_id: &str) -> Result<ApiResponse<Vec<DialogTurn>>> {
        self.client.get(&format!("/api/v1/dm/history/{}", session_id)).await
    }

    /// Очистить историю диалога
    pub async fn clear_dialog_history(&self, session_id: &str) -> Result<ApiResponse<String>> {
        self.client.delete(&format!("/api/v1/dm/history/{}", session_id)).await
    }

    /// Получить следующий шаг в диалоге
    pub async fn get_next_action(&self, session_id: &str) -> Result<ApiResponse<String>> {
        self.client.get(&format!("/api/v1/dm/next-action/{}", session_id)).await
    }

    /// Выполнить действие в диалоге
    pub async fn execute_action(&self, session_id: &str, action: &str, parameters: HashMap<String, serde_json::Value>) -> Result<ApiResponse<serde_json::Value>> {
        let data = serde_json::json!({
            "action": action,
            "parameters": parameters,
            "timestamp": chrono::Utc::now().timestamp_millis()
        });
        self.client.post(&format!("/api/v1/dm/execute/{}", session_id), &data).await
    }

    /// Сбросить диалог к начальному состоянию
    pub async fn reset_dialog(&self, session_id: &str) -> Result<ApiResponse<String>> {
        self.client.post(&format!("/api/v1/dm/reset/{}", session_id), &serde_json::Value::Null).await
    }

    /// Получить доступные действия для текущего состояния
    pub async fn get_available_actions(&self, session_id: &str) -> Result<ApiResponse<Vec<String>>> {
        self.client.get(&format!("/api/v1/dm/actions/{}", session_id)).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
