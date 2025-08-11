# Конфигурация WebSocket Audio Tester

Примеры настроек для различных сценариев развертывания SmartJARVIS.

## 🏠 Локальная разработка

```json
{
  "host": "localhost",
  "port": "7090",
  "protocol": "ws",
  "autoReconnect": true,
  "maxRetries": 5
}
```

**Использование**: Для тестирования на локальной машине с voice-gateway.

## 🐳 Docker Compose

```json
{
  "host": "localhost",
  "port": "7090",
  "protocol": "ws",
  "autoReconnect": true,
  "maxRetries": 10
}
```

**Использование**: При запуске через `docker-compose up` с пробросом портов.

## ☁️ Продакшн сервер

```json
{
  "host": "voice.smartjarvis.com",
  "port": "443",
  "protocol": "wss",
  "autoReconnect": true,
  "maxRetries": 20
}
```

**Использование**: Для внешнего доступа с SSL/TLS шифрованием.

## 🔒 Корпоративная сеть

```json
{
  "host": "192.168.1.100",
  "port": "7090",
  "protocol": "ws",
  "autoReconnect": true,
  "maxRetries": 15
}
```

**Использование**: Внутри корпоративной сети с фиксированным IP.

## 🧪 Тестовое окружение

```json
{
  "host": "test-voice.smartjarvis.com",
  "port": "7090",
  "protocol": "wss",
  "autoReconnect": false,
  "maxRetries": 3
}
```

**Использование**: Для тестирования без автоматического переподключения.

## 🚀 Высоконагруженная система

```json
{
  "host": "voice-cluster.smartjarvis.com",
  "port": "443",
  "protocol": "wss",
  "autoReconnect": true,
  "maxRetries": 50
}
```

**Использование**: Для систем с высокой нагрузкой и множественными переподключениями.

## 🔧 Настройка через URL параметры

Можно передавать конфигурацию через URL:

```
http://localhost:8080/index.html?host=voice.example.com&port=443&protocol=wss&autoReconnect=true&maxRetries=10
```

## 📱 Мобильные устройства

```json
{
  "host": "voice.smartjarvis.com",
  "port": "443",
  "protocol": "wss",
  "autoReconnect": true,
  "maxRetries": 8
}
```

**Особенности**: 
- Используйте WSS для HTTPS соединений
- Ограничьте количество попыток переподключения
- Учитывайте нестабильность мобильных сетей

## 🌐 Прокси и балансировщики

```json
{
  "host": "proxy.company.com",
  "port": "443",
  "protocol": "wss",
  "autoReconnect": true,
  "maxRetries": 12
}
```

**Особенности**:
- Настройте WebSocket upgrade в прокси
- Увеличьте timeout для прокси
- Проверьте поддержку WSS в прокси

## 🔍 Диагностика соединения

### Проверка доступности порта

```bash
# Linux/Mac
nc -zv voice.example.com 7090

# Windows
Test-NetConnection -ComputerName voice.example.com -Port 7090
```

### Проверка WebSocket

```bash
# Используйте wscat для тестирования
npm install -g wscat
wscat -c ws://voice.example.com:7090/ws
```

### Проверка SSL сертификата

```bash
# Проверка SSL сертификата
openssl s_client -connect voice.example.com:443 -servername voice.example.com
```

## ⚠️ Частые проблемы

### 1. CORS ошибки
```
Access to WebSocket at 'ws://voice.example.com:7090/ws' from origin 'http://localhost:8080' has been blocked by CORS policy
```
**Решение**: Настройте CORS на voice-gateway или используйте прокси.

### 2. SSL ошибки
```
WebSocket connection to 'wss://voice.example.com:443/ws' failed
```
**Решение**: Проверьте SSL сертификат и настройки WSS.

### 3. Таймауты соединения
```
WebSocket connection timed out
```
**Решение**: Увеличьте timeout на прокси/балансировщике.

### 4. Проблемы с прокси
```
WebSocket upgrade failed
```
**Решение**: Настройте WebSocket upgrade в прокси конфигурации.

## 🎯 Рекомендации по производительности

### Для разработки
- Используйте `ws://` для локального тестирования
- Отключите auto-reconnect для отладки
- Минимальное количество retries

### Для продакшна
- Всегда используйте `wss://` для безопасности
- Включите auto-reconnect с разумными лимитами
- Мониторьте метрики latency и reconnections

### Для тестирования
- Используйте отдельные тестовые серверы
- Логируйте все соединения для анализа
- Тестируйте различные сетевые условия 