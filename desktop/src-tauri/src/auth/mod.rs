pub mod jwt;
pub mod session;

use serde::{Deserialize, Serialize};
use chrono::{DateTime, Utc};

/// Токен аутентификации
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuthToken {
    pub access_token: String,
    pub refresh_token: Option<String>,
    pub expires_at: DateTime<Utc>,
    pub token_type: String,
}

impl AuthToken {
    pub fn new(access_token: String, expires_at: DateTime<Utc>) -> Self {
        Self {
            access_token,
            refresh_token: None,
            expires_at,
            token_type: "Bearer".to_string(),
        }
    }

    pub fn is_expired(&self) -> bool {
        Utc::now() > self.expires_at
    }

    pub fn is_valid(&self) -> bool {
        !self.access_token.is_empty() && !self.is_expired()
    }
}

/// Пользователь
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct User {
    pub id: String,
    pub username: String,
    pub email: String,
    pub roles: Vec<String>,
    pub created_at: DateTime<Utc>,
    pub last_login: Option<DateTime<Utc>>,
}

/// Запрос на аутентификацию
#[derive(Debug, Serialize, Deserialize)]
pub struct LoginRequest {
    pub username: String,
    pub password: String,
    pub remember_me: bool,
}

/// Ответ на аутентификацию
#[derive(Debug, Serialize, Deserialize)]
pub struct LoginResponse {
    pub user: User,
    pub token: AuthToken,
    pub message: String,
}

/// Запрос на регистрацию
#[derive(Debug, Serialize, Deserialize)]
pub struct RegisterRequest {
    pub username: String,
    pub email: String,
    pub password: String,
    pub confirm_password: String,
}

/// Ответ на регистрацию
#[derive(Debug, Serialize, Deserialize)]
pub struct RegisterResponse {
    pub user: User,
    pub token: AuthToken,
    pub message: String,
}
