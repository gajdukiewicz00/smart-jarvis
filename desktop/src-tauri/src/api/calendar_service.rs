use crate::api::{ApiClient, ApiResponse};
use crate::api::models::{CalendarEvent, CreateEventRequest};
use anyhow::Result;

/// Клиент для Calendar сервиса
pub struct CalendarServiceClient {
    client: ApiClient,
}

impl CalendarServiceClient {
    pub fn new(client: ApiClient) -> Self {
        Self { client }
    }

    /// Создать новое событие
    pub async fn create_event(&self, user_id: &str, request: CreateEventRequest) -> Result<ApiResponse<CalendarEvent>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "event": request
        });
        self.client.post("/api/v1/calendar/events", &data).await
    }

    /// Получить событие по ID
    pub async fn get_event(&self, event_id: &str) -> Result<ApiResponse<CalendarEvent>> {
        self.client.get(&format!("/api/v1/calendar/events/{}", event_id)).await
    }

    /// Получить все события пользователя
    pub async fn get_user_events(&self, user_id: &str) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        self.client.get(&format!("/api/v1/calendar/events/user/{}", user_id)).await
    }

    /// Получить события за день
    pub async fn get_events_by_date(&self, user_id: &str, date: u64) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        self.client.get(&format!("/api/v1/calendar/events/user/{}/date/{}", user_id, date)).await
    }

    /// Получить события за неделю
    pub async fn get_events_by_week(&self, user_id: &str, week_start: u64) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        self.client.get(&format!("/api/v1/calendar/events/user/{}/week/{}", user_id, week_start)).await
    }

    /// Получить события за месяц
    pub async fn get_events_by_month(&self, user_id: &str, month: u32, year: u32) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        self.client.get(&format!("/api/v1/calendar/events/user/{}/month/{}/{}", user_id, year, month)).await
    }

    /// Получить предстоящие события
    pub async fn get_upcoming_events(&self, user_id: &str, limit: Option<u32>) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        let url = if let Some(limit) = limit {
            format!("/api/v1/calendar/events/user/{}/upcoming?limit={}", user_id, limit)
        } else {
            format!("/api/v1/calendar/events/user/{}/upcoming", user_id)
        };
        self.client.get(&url).await
    }

    /// Обновить событие
    pub async fn update_event(&self, event_id: &str, request: CreateEventRequest) -> Result<ApiResponse<CalendarEvent>> {
        self.client.put(&format!("/api/v1/calendar/events/{}", event_id), &request).await
    }

    /// Удалить событие
    pub async fn delete_event(&self, event_id: &str) -> Result<ApiResponse<String>> {
        self.client.delete(&format!("/api/v1/calendar/events/{}", event_id)).await
    }

    /// Поиск событий
    pub async fn search_events(&self, user_id: &str, query: &str) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        self.client.get(&format!("/api/v1/calendar/events/user/{}/search?q={}", user_id, query)).await
    }

    /// Получить события по местоположению
    pub async fn get_events_by_location(&self, user_id: &str, location: &str) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        self.client.get(&format!("/api/v1/calendar/events/user/{}/location/{}", user_id, location)).await
    }

    /// Получить статистику событий
    pub async fn get_event_statistics(&self, user_id: &str, period: &str) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get(&format!("/api/v1/calendar/statistics/{}/{}", user_id, period)).await
    }

    /// Экспортировать календарь
    pub async fn export_calendar(&self, user_id: &str, format: &str) -> Result<ApiResponse<String>> {
        self.client.get(&format!("/api/v1/calendar/export/{}/{}", user_id, format)).await
    }

    /// Импортировать календарь
    pub async fn import_calendar(&self, user_id: &str, calendar_data: &str, format: &str) -> Result<ApiResponse<Vec<CalendarEvent>>> {
        let data = serde_json::json!({
            "user_id": user_id,
            "calendar_data": calendar_data,
            "format": format
        });
        self.client.post("/api/v1/calendar/import", &data).await
    }

    /// Проверить доступность сервиса
    pub async fn health_check(&self) -> Result<ApiResponse<serde_json::Value>> {
        self.client.get("/health").await
    }
}
