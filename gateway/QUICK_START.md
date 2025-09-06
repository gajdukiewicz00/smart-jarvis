# Quick Start Guide - SmartJARVIS Gateway

## Быстрый запуск

### 1. Сборка Gateway

```bash
cd gateway
mvn clean package
```

### 2. Запуск Gateway

```bash
# Локально
java -jar target/gateway-1.0.0.jar

# Или через Maven
mvn spring-boot:run
```

### 3. Проверка работы

```bash
# Проверка здоровья Gateway
curl http://localhost:8080/actuator/health

# Информация о Gateway
curl http://localhost:8080/api/v1/gateway/info

# Документация API
curl http://localhost:8080/api/v1/gateway/docs
```

## Тестирование

### Запуск тестов

```bash
# Unit тесты
mvn test

# Интеграционные тесты
mvn verify
```

### Тестирование API

```bash
# Запуск тестового скрипта
./test-gateway.sh
```

## Конфигурация

### Локальная разработка

```yaml
spring:
  profiles:
    active: local
```

### Docker окружение

```yaml
spring:
  profiles:
    active: docker
```

## Основные endpoints

### Gateway Endpoints
- `GET /api/v1/gateway/info` - Информация о Gateway
- `GET /api/v1/gateway/health` - Проверка здоровья
- `GET /api/v1/gateway/docs` - Документация API

### Management Endpoints
- `GET /actuator/health` - Состояние сервиса
- `GET /actuator/metrics` - Метрики
- `GET /actuator/prometheus` - Prometheus метрики

### Fallback Endpoints
- `GET /fallback/task-service` - Fallback для Task Service
- `GET /fallback/nlp-engine` - Fallback для NLP Engine
- `GET /fallback/speech-service` - Fallback для Speech Service

## Маршрутизация

Gateway автоматически маршрутизирует запросы к соответствующим сервисам:

- `/api/v1/tasks/**` → Task Service (8081)
- `/api/v1/nlp/**` → NLP Engine (8082)
- `/api/v1/speech/**` → Speech Service (8083)

## Мониторинг

### Метрики
```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus
```bash
curl http://localhost:8080/actuator/prometheus
```

### Логи
```bash
tail -f logs/gateway.log
```

## Troubleshooting

### Gateway не запускается
1. Проверьте, что порт 8080 свободен
2. Убедитесь, что Redis запущен (для rate limiting)
3. Проверьте логи: `tail -f logs/gateway.log`

### Ошибки маршрутизации
1. Убедитесь, что целевые сервисы запущены
2. Проверьте конфигурацию в `application.yml`
3. Проверьте fallback endpoints

### Проблемы с rate limiting
1. Убедитесь, что Redis запущен
2. Проверьте конфигурацию Redis в `application.yml`
3. Проверьте логи Gateway

## Docker

### Сборка образа
```bash
docker build -f docker/Dockerfile.gateway -t smartjarvis-gateway .
```

### Запуск контейнера
```bash
docker run -p 8080:8080 smartjarvis-gateway
```

### Docker Compose
```bash
cd docker
docker-compose up -d gateway
```