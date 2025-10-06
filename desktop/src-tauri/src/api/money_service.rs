use crate::api::{ApiClient, ApiResponse};
use crate::api::models::{MoneyTransaction, CreateTransactionRequest};
use anyhow::Result;

/// Клиент для Money сервиса
pub struct MoneyServiceClient {
    client: ApiClient,
}

impl MoneyServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Создать новую транзакцию
    pub async fn create_transaction(&self, user_id: &str, request: CreateTransactionRequest) -> Result<ApiResponse<MoneyTransaction>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "transaction": request
        });
        self.client.post("/api/v1/money/transactions", &data).await
    }

    /// Получить транзакцию по ID
    pub async fn get_transaction(&self, transaction_id: &str) -> Result<ApiResponse<MoneyTransaction>> {
        self.client.get(&format!("/api/v1/money/transactions/{}", transaction_id)).await
    }

    /// Получить все транзакции пользователя
    pub async fn get_user_transactions(&self, user_id: &str) -> Result<ApiResponse<Vec<MoneyTransaction>>> {
        self.client.get(&format!("/api/v1/money/transactions/user/{}", user_id)).await
    }

    /// Получить транзакции по типу
    pub async fn get_transactions_by_type(&self, user_id: &str, transaction_type: &str) -> Result<ApiResponse<Vec<MoneyTransaction>>> {
        self.client.get(&format!("/api/v1/money/transactions/user/{}/type/{}", user_id, transaction_type)).await
    }

    /// Получить транзакции по категории
    pub async fn get_transactions_by_category(&self, user_id: &str, category: &str) -> Result<ApiResponse<Vec<MoneyTransaction>>> {
        self.client.get(&format!("/api/v1/money/transactions/user/{}/category/{}", user_id, category)).await
    }

    /// Получить транзакции за период
    pub async fn get_transactions_by_period(&self, user_id: &str, start_date: u64, end_date: u64) -> Result<ApiResponse<Vec<MoneyTransaction>>> {
        self.client.get(&format!("/api/v1/money/transactions/user/{}/period?start={}&end={}", user_id, start_date, end_date)).await
    }

    /// Получить баланс пользователя
    pub async fn get_balance(&self, user_id: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/money/balance/{}", user_id)).await
    }

    /// Получить статистику расходов
    pub async fn get_expense_statistics(&self, user_id: &str, period: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/money/statistics/{}/expenses?period={}", user_id, period)).await
    }

    /// Получить статистику доходов
    pub async fn get_income_statistics(&self, user_id: &str, period: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/money/statistics/{}/income?period={}", user_id, period)).await
    }

    /// Получить категории транзакций
    pub async fn get_categories(&self, user_id: &str) -> Result<ApiResponse<Vec<String>>> {
        self.client.get(&format!("/api/v1/money/categories/{}", user_id)).await
    }

    /// Добавить новую категорию
    pub async fn add_category(&self, user_id: &str, category: &str) -> Result<ApiResponse<String>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "category": category
        });
        self.client.post("/api/v1/money/categories", &data).await
    }

    /// Поиск транзакций
    pub async fn search_transactions(&self, user_id: &str, query: &str) -> Result<ApiResponse<Vec<MoneyTransaction>>> {
        self.client.get(&format!("/api/v1/money/transactions/user/{}/search?q={}", user_id, query)).await
    }

    /// Экспортировать транзакции
    pub async fn export_transactions(&self, user_id: &str, format: &str) -> Result<ApiResponse<String>> {
        self.client.get(&format!("/api/v1/money/transactions/user/{}/export?format={}", user_id, format)).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
