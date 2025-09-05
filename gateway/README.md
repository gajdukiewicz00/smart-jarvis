# SmartJARVIS Gateway Service

Spring Cloud Gateway для микросервисной архитектуры SmartJARVIS.

## Описание

Gateway сервис предоставляет единую точку входа для всех микросервисов SmartJARVIS с дополнительными возможностями:

- **Маршрутизация запросов** к микросервисам
- **Rate Limiting** для защиты от перегрузки
- **Circuit Breaker** для обработки отказов сервисов
- **CORS** поддержка для веб-приложений
- **Мониторинг и метрики** через Actuator
- **Логирование** всех запросов и ответов
- **Fallback** обработка при недоступности сервисов

## Архитектура

```
┌─────────────────┐
│   Client Apps   │
└─────────┬───────┘
          │
┌─────────▼───────┐
│   Gateway       │
│   (Port 8080)   │
└─────────┬───────┘
          │
    ┌─────┴─────┐
    │           │
┌───▼───┐   ┌───▼───┐
│ Task  │   │ NLP   │
│Service│   │Engine │
│(8081) │   │(8082) │
└───────┘   └───────┘
    │           │
    └─────┬─────┘
          │
    ┌─────▼─────┐
    │ Speech    │
    │ Service   │
    │ (8083)    │
    └───────────┘
```

## Маршруты

### Task Service
- **GET** `/api/v1/tasks` - Получить все задачи
- **GET** `/api/v1/tasks/{id}` - Получить задачу по ID
- **POST** `/api/v1/tasks` - Создать новую задачу
- **PUT** `/api/v1/tasks/{id}` - Обновить задачу
- **DELETE** `/api/v1/tasks/{id}` - Удалить задачу

### NLP Engine
- **POST** `/api/v1/nlp/process` - Обработать естественный язык
- **GET** `/api/v1/nlp/intents` - Получить доступные интенты

### Speech Service
- **POST** `/api/v1/speech/recognize` - Распознавание речи
- **POST** `/api/v1/speech/synthesize` - Синтез речи

### Gateway Endpoints
- **GET** `/api/v1/gateway/info` - Информация о Gateway
- **GET** `/api/v1/gateway/health` - Проверка здоровья
- **GET** `/api/v1/gateway/docs` - Документация API

### Management Endpoints
- **GET** `/actuator/health` - Состояние сервиса
- **GET** `/actuator/metrics` - Метрики
- **GET** `/actuator/prometheus` - Prometheus метрики
- **GET** `/actuator/gateway/routes` - Список маршрутов

## Конфигурация

### Локальная разработка

```yaml
spring:
  profiles:
    active: local
  
  cloud:
    gateway:
      routes:
        - id: task-service-routes
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/tasks/**
```

### Docker окружение

```yaml
spring:
  profiles:
    active: docker
  
  cloud:
    gateway:
      routes:
        - id: task-service-routes-docker
          uri: http://task-service:8081
          predicates:
            - Path=/api/v1/tasks/**
```

## Запуск

### Локально

```bash
# Сборка
mvn clean package

# Запуск
java -jar target/gateway-1.0.0.jar

# Или через Maven
mvn spring-boot:run
```

### Docker

```bash
# Сборка образа
docker build -t smartjarvis-gateway .

# Запуск контейнера
docker run -p 8080:8080 smartjarvis-gateway
```

### Docker Compose

```bash
# Запуск всех сервисов
docker-compose up -d
```

## Мониторинг

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Метрики
```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus
```bash
curl http://localhost:8080/actuator/prometheus
```

## Circuit Breaker

Gateway использует Resilience4j для реализации паттерна Circuit Breaker:

- **Sliding Window Size**: 10 запросов
- **Failure Rate Threshold**: 50%
- **Wait Duration**: 5 секунд
- **Minimum Number of Calls**: 5

## Rate Limiting

Ограничения запросов на основе Redis:

- **Task Service**: 10 запросов/сек, burst: 20
- **NLP Engine**: 15 запросов/сек, burst: 30
- **Speech Service**: 5 запросов/сек, burst: 10

## Безопасность

По умолчанию безопасность отключена для разработки. Для продакшена:

```yaml
gateway:
  security:
    enabled: true
    jwt:
      secret: your-secret-key
      expiration: 86400000
```

## Логирование

Все запросы и ответы логируются с дополнительной информацией:

- HTTP метод и путь
- IP адрес клиента
- User-Agent
- Время выполнения
- Статус код ответа
- Request ID для трассировки

## Fallback

При недоступности микросервисов Gateway возвращает структурированные ответы:

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": "SERVICE_UNAVAILABLE",
  "message": "Service is currently unavailable",
  "service": "task-service",
  "fallback": true
}
```

## Разработка

### Структура проекта

```
gateway/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/smartjarvis/gateway/
│   │   │       ├── GatewayApplication.java
│   │   │       ├── config/
│   │   │       │   └── GatewayConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── GatewayController.java
│   │   │       │   └── FallbackController.java
│   │   │       └── filter/
│   │   │           ├── LoggingFilter.java
│   │   │           └── RequestIdFilter.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/
│       │   └── com/smartjarvis/gateway/
│       │       └── GatewayApplicationTests.java
│       └── resources/
│           └── application-test.yml
├── pom.xml
└── README.md
```

### Добавление новых маршрутов

1. Добавить маршрут в `application.yml`
2. Настроить предикаты и фильтры
3. Добавить fallback обработчик
4. Протестировать через Gateway

### Тестирование

```bash
# Запуск тестов
mvn test

# Интеграционные тесты
mvn verify
```