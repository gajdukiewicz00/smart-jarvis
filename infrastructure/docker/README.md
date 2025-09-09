# SmartJARVIS Docker Setup

## Обзор

Этот Docker setup обеспечивает полное развертывание SmartJARVIS с использованием Clean Architecture для всех сервисов.

## Архитектура

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Gateway       │    │   Task Service  │    │   NLP Engine    │
│   (8080)        │    │   (8081)        │    │   (8082)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Speech Service│
                    │   (8083)        │
                    └─────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │   Redis         │    │   Monitoring    │
│   (5432)        │    │   (6379)        │    │   (3000, 9090)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Сервисы

### Основные сервисы
- **Gateway** (8080) - Spring Cloud Gateway для маршрутизации
- **Task Service** (8081) - Spring Boot с Clean Architecture
- **NLP Engine** (8082) - Node.js/TypeScript с Clean Architecture
- **Speech Service** (8083) - Python/FastAPI с Clean Architecture

### Инфраструктурные сервисы
- **PostgreSQL** (5432) - База данных
- **Redis** (6379) - Кэширование и rate limiting

### Мониторинг
- **Prometheus** (9090) - Сбор метрик
- **Grafana** (3000) - Визуализация метрик

## Быстрый старт

### Предварительные требования
- Docker 20.10+
- Docker Compose v2
- 4GB+ RAM
- 10GB+ свободного места

### Запуск

```bash
# Клонировать репозиторий
git clone <repository-url>
cd smart-jarvis

# Запустить все сервисы
cd docker
make start
```

### Проверка статуса

```bash
# Проверить статус всех сервисов
make status
# или
docker compose ps
```

## Управление

### Скрипты

```bash
# Запуск
./scripts/start.sh

# Остановка
./scripts/stop.sh

# Остановка с очисткой данных
./scripts/stop.sh --clean

# Пересборка
./scripts/rebuild.sh
```

### Ручное управление

```bash
# Запуск всех сервисов
docker compose up -d

# Запуск конкретного сервиса
docker compose up -d task-service

# Остановка всех сервисов
docker compose down

# Пересборка
docker compose build --no-cache

# Просмотр логов
docker compose logs -f
```

## API Endpoints

### Gateway (Основной API)
- `http://localhost:8080` - Главный endpoint

### Прямой доступ к сервисам
- **Task Service**: `http://localhost:8081`
- **NLP Engine**: `http://localhost:8082`
- **Speech Service**: `http://localhost:8083`

### Мониторинг
- **Grafana**: `http://localhost:3000` (admin/admin)
- **Prometheus**: `http://localhost:9090`

## Конфигурация

### Переменные окружения

#### Task Service
```env
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/jarvis_db
SPRING_DATASOURCE_USERNAME=jarvis_user
SPRING_DATASOURCE_PASSWORD=jarvis_password
```

#### NLP Engine
```env
NODE_ENV=production
NLP_ENGINE_PORT=8082
TASK_SERVICE_URL=http://task-service:8081/api/v1
```

#### Speech Service
```env
SPEECH_SERVICE_PORT=8083
NLP_ENGINE_URL=http://nlp-engine:8082
```

#### Gateway
```env
SPRING_PROFILES_ACTIVE=docker
GATEWAY_PORT=8080
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
```

## Мониторинг

### Health Checks
Все сервисы имеют настроенные health checks:
- **Task Service**: `/actuator/health`
- **NLP Engine**: `/api/health`
- **Speech Service**: `/health`
- **Gateway**: `/actuator/health`

### Метрики
- **Prometheus**: Сбор метрик со всех сервисов
- **Grafana**: Дашборды для визуализации

## Разработка

### Локальная разработка
```bash
# Запуск только инфраструктуры
docker compose up -d redis postgres

# Разработка сервисов локально
# Подключение к БД: localhost:5432
# Подключение к Redis: localhost:6379
```

### Отладка
```bash
# Просмотр логов конкретного сервиса
docker compose logs -f task-service

# Вход в контейнер
docker compose exec task-service bash

# Проверка health check
curl http://localhost:8081/actuator/health
```

## Troubleshooting

### Проблемы с портами
```bash
# Проверить занятые порты
netstat -tulpn | grep :808

# Остановить сервис на порту
sudo lsof -ti:8080 | xargs kill -9
```

### Проблемы с памятью
```bash
# Проверить использование памяти
docker stats

# Очистить неиспользуемые ресурсы
docker system prune -a
```

### Проблемы с базой данных
```bash
# Проверить подключение к БД
docker compose exec postgres psql -U jarvis_user -d jarvis_db

# Сбросить БД
docker compose down -v
docker compose up -d postgres
```

## Production

### Безопасность
- Все сервисы запускаются от непривилегированных пользователей
- Используются health checks
- Настроен rate limiting через Redis

### Масштабирование
```bash
# Масштабирование сервисов
docker compose up -d --scale task-service=3
docker compose up -d --scale nlp-engine=2
```

### Backup
```bash
# Backup базы данных
docker compose exec postgres pg_dump -U jarvis_user jarvis_db > backup.sql

# Restore базы данных
docker compose exec -T postgres psql -U jarvis_user jarvis_db < backup.sql
```

## Логирование

### Структура логов
```
logs/
├── task-service/
├── nlp-engine/
├── speech-service/
└── gateway/
```

### Просмотр логов
```bash
# Все логи
docker compose logs

# Логи конкретного сервиса
docker compose logs task-service

# Логи в реальном времени
docker compose logs -f task-service
```

## Обновление

### Обновление сервисов
```bash
# Остановить сервисы
make stop

# Обновить код
git pull

# Пересобрать и запустить
make rebuild
```

### Обновление Docker образов
```bash
# Обновить базовые образы
docker compose pull

# Пересобрать с новыми образами
docker compose build --no-cache
```

## Docker Compose v2 Особенности

### Основные изменения
- Используется `docker compose` вместо `docker-compose`
- Улучшенная производительность
- Лучшая интеграция с Docker Engine
- Поддержка новых функций

### Команды
```bash
# Старая версия
docker-compose up -d

# Новая версия
docker compose up -d
```

### Проверка версии
```bash
docker compose version
```

### Совместимость
- Все команды совместимы с v1
- Автоматическое определение версии
- Обратная совместимость 