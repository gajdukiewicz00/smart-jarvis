use super::{AuthToken, User, LoginRequest, LoginResponse, RegisterRequest, RegisterResponse};
use crate::api::{ApiClient, ApiConfig};
use anyhow::Result;
use std::sync::Arc;
use tokio::sync::RwLock;

/// Менеджер сессий
pub struct SessionManager {
    api_client: ApiClient,
    current_user: Arc<RwLock<Option<User>>>,
    current_token: Arc<RwLock<Option<AuthToken>>>,
}

impl SessionManager {
    pub fn new(api_config: ApiConfig) -> Result<Self> {
        let api_client = ApiClient::new(api_config)?;
        
        Ok(Self {
            api_client,
            current_user: Arc::new(RwLock::new(None)),
            current_token: Arc::new(RwLock::new(None)),
        })
    }

    /// Войти в систему
    pub async fn login(&self, request: LoginRequest) -> Result<LoginResponse> {
        let response: crate::api::ApiResponse<LoginResponse> = self.api_client
            .post("/api/v1/auth/login", &request)
            .await?;

        if response.success {
            let login_response = response.data.ok_or_else(|| anyhow::anyhow!("No data in response"))?;
            
            // Сохраняем пользователя и токен
            {
                let mut user = self.current_user.write().await;
                *user = Some(login_response.user.clone());
            }
            
            {
                let mut token = self.current_token.write().await;
                *token = Some(login_response.token.clone());
            }
            
            Ok(login_response)
        } else {
            Err(anyhow::anyhow!("Login failed: {}", response.error.unwrap_or("Unknown error".to_string())))
        }
    }

    /// Зарегистрироваться
    pub async fn register(&self, request: RegisterRequest) -> Result<RegisterResponse> {
        let response: crate::api::ApiResponse<RegisterResponse> = self.api_client
            .post("/api/v1/auth/register", &request)
            .await?;

        if response.success {
            let register_response = response.data.ok_or_else(|| anyhow::anyhow!("No data in response"))?;
            
            // Сохраняем пользователя и токен
            {
                let mut user = self.current_user.write().await;
                *user = Some(register_response.user.clone());
            }
            
            {
                let mut token = self.current_token.write().await;
                *token = Some(register_response.token.clone());
            }
            
            Ok(register_response)
        } else {
            Err(anyhow::anyhow!("Registration failed: {}", response.error.unwrap_or("Unknown error".to_string())))
        }
    }

    /// Выйти из системы
    pub async fn logout(&self) -> Result<()> {
        if let Some(token) = self.get_current_token().await {
            // Уведомляем сервер о выходе
            let _: crate::api::ApiResponse<String> = self.api_client
                .post("/api/v1/auth/logout", &serde_json::json!({
                    "token": token.access_token
                }))
                .await?;
        }
        
        // Очищаем локальные данные
        {
            let mut user = self.current_user.write().await;
            *user = None;
        }
        
        {
            let mut token = self.current_token.write().await;
            *token = None;
        }
        
        Ok(())
    }

    /// Получить текущего пользователя
    pub async fn get_current_user(&self) -> Option<User> {
        let user = self.current_user.read().await;
        user.clone()
    }

    /// Получить текущий токен
    pub async fn get_current_token(&self) -> Option<AuthToken> {
        let token = self.current_token.read().await;
        token.clone()
    }

    /// Проверить, авторизован ли пользователь
    pub async fn is_authenticated(&self) -> bool {
        if let Some(token) = self.get_current_token().await {
            token.is_valid()
        } else {
            false
        }
    }

    /// Обновить токен
    pub async fn refresh_token(&self) -> Result<()> {
        if let Some(current_token) = self.get_current_token().await {
            let response: crate::api::ApiResponse<AuthToken> = self.api_client
                .post("/api/v1/auth/refresh", &serde_json::json!({
                    "refresh_token": current_token.refresh_token
                }))
                .await?;

            if response.success {
                if let Some(new_token) = response.data {
                    let mut token = self.current_token.write().await;
                    *token = Some(new_token);
                }
            }
        }
        
        Ok(())
    }

    /// Получить заголовок авторизации для API запросов
    pub async fn get_auth_header(&self) -> Option<String> {
        if let Some(token) = self.get_current_token().await {
            if token.is_valid() {
                return Some(format!("{} {}", token.token_type, token.access_token));
            }
        }
        None
    }
}
