use jsonwebtoken::{decode, encode, Algorithm, DecodingKey, EncodingKey, Header, Validation};
use serde::{Deserialize, Serialize};
use chrono::{DateTime, Utc, Duration};
use anyhow::Result;

/// JWT Claims
#[derive(Debug, Serialize, Deserialize)]
pub struct Claims {
    pub sub: String, // user_id
    pub username: String,
    pub email: String,
    pub roles: Vec<String>,
    pub exp: i64, // expiration time
    pub iat: i64, // issued at
    pub iss: String, // issuer
}

impl Claims {
    pub fn new(
        user_id: String,
        username: String,
        email: String,
        roles: Vec<String>,
        issuer: String,
    ) -> Self {
        let now = Utc::now();
        Self {
            sub: user_id,
            username,
            email,
            roles,
            exp: (now + Duration::hours(24)).timestamp(),
            iat: now.timestamp(),
            iss: issuer,
        }
    }

    pub fn is_expired(&self) -> bool {
        Utc::now().timestamp() > self.exp
    }
}

/// JWT Manager
pub struct JwtManager {
    secret_key: String,
    issuer: String,
}

impl JwtManager {
    pub fn new(secret_key: String, issuer: String) -> Self {
        Self {
            secret_key,
            issuer,
        }
    }

    /// Создать JWT токен
    pub fn create_token(&self, claims: Claims) -> Result<String> {
        let header = Header::new(Algorithm::HS256);
        let encoding_key = EncodingKey::from_secret(self.secret_key.as_ref());
        
        encode(&header, &claims, &encoding_key)
            .map_err(|e| anyhow::anyhow!("Failed to encode JWT: {}", e))
    }

    /// Валидировать JWT токен
    pub fn validate_token(&self, token: &str) -> Result<Claims> {
        let decoding_key = DecodingKey::from_secret(self.secret_key.as_ref());
        let mut validation = Validation::new(Algorithm::HS256);
        validation.iss = Some(std::collections::HashSet::from([self.issuer.clone()]));
        
        let token_data = decode::<Claims>(token, &decoding_key, &validation)
            .map_err(|e| anyhow::anyhow!("Failed to decode JWT: {}", e))?;
        
        if token_data.claims.is_expired() {
            return Err(anyhow::anyhow!("Token has expired"));
        }
        
        Ok(token_data.claims)
    }

    /// Извлечь пользователя из токена
    pub fn extract_user_from_token(&self, token: &str) -> Result<(String, Vec<String>)> {
        let claims = self.validate_token(token)?;
        Ok((claims.sub, claims.roles))
    }

    /// Обновить токен
    pub fn refresh_token(&self, old_token: &str) -> Result<String> {
        let mut claims = self.validate_token(old_token)?;
        
        // Обновляем время истечения
        let now = Utc::now();
        claims.exp = (now + Duration::hours(24)).timestamp();
        claims.iat = now.timestamp();
        
        self.create_token(claims)
    }
}

impl Default for JwtManager {
    fn default() -> Self {
        Self::new(
            "your-secret-key-change-in-production".to_string(),
            "smartjarvis".to_string(),
        )
    }
}
