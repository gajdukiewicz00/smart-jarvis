# SmartJARVIS - Интеллектуальная система управления задачами

## 🚀 Обзор проекта

SmartJARVIS - это интеллектуальная система управления задачами с голосовым управлением, построенная на принципах **Clean Architecture** и микросервисной архитектуры.

## 🏗️ Архитектура

### Микросервисы с Clean Architecture

| Сервис | Технология | Архитектура | Порт |
|--------|------------|-------------|------|
| **Task Service** | Java/Spring Boot | Clean Architecture | 8081 |
| **NLP Engine** | TypeScript/Node.js | Clean Architecture | 8082 |
| **Speech Service** | Python/FastAPI | Clean Architecture | 8083 |
| **Gateway** | Java/Spring Cloud | API Gateway | 8080 |

### Инфраструктура

- **PostgreSQL** - Основная база данных
- **Redis** - Кэширование и rate limiting
- **Prometheus** - Сбор метрик
- **Grafana** - Визуализация метрик

## 🐳 Быстрый старт с Docker

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

### Доступные сервисы

| Сервис | URL | Описание |
|--------|-----|----------|
| **Gateway** | http://localhost:8080 | API Gateway |
| **Task Service** | http://localhost:8081 | Управление задачами |
| **NLP Engine** | http://localhost:8082 | Обработка естественного языка |
| **Speech Service** | http://localhost:8083 | Обработка речи |
| **Grafana** | http://localhost:3000 | Мониторинг (admin/admin) |
| **Prometheus** | http://localhost:9090 | Метрики |

## 🛠️ Управление

```bash
# Основные команды
make start      # Запуск всех сервисов
make stop       # Остановка всех сервисов
make restart    # Перезапуск
make rebuild    # Пересборка
make logs       # Просмотр логов
make health     # Проверка здоровья
make clean      # Очистка всего
```

## 🏛️ Clean Architecture

### Принципы
- **Dependency Inversion**: Доменный слой не зависит от внешних слоев
- **Single Responsibility**: Каждый класс имеет одну ответственность
- **Open/Closed**: Система открыта для расширения, закрыта для модификации
- **Interface Segregation**: Интерфейсы разделены по функциональности

### Структура каждого сервиса

```
src/
├── domain/           # Доменный слой (бизнес-логика)
│   ├── entities/     # Доменные сущности
│   ├── value-objects/ # Объекты-значения
│   ├── services/     # Доменные сервисы
│   └── repositories/ # Интерфейсы репозиториев
├── application/      # Слой приложения (сценарии использования)
│   ├── usecases/     # Use Cases
│   └── dto/          # Data Transfer Objects
├── infrastructure/   # Инфраструктурный слой
│   ├── repositories/ # Реализации репозиториев
│   └── services/     # Внешние сервисы
└── presentation/     # Слой представления
    ├── controllers/  # REST контроллеры
    └── routes/       # Маршрутизация
```

## 📚 Документация

### Архитектура сервисов
- [Task Service Architecture](task-service/ARCHITECTURE.md)
- [NLP Engine Architecture](nlp-engine/ARCHITECTURE.md)
- [Speech Service Architecture](speech-service/ARCHITECTURE.md)

### Docker и развертывание
- [Docker Setup](docker/README.md)
- [Полная документация Docker](README_DOCKER.md)

## 🧪 Тестирование

### API Тесты

```bash
# Health checks
curl http://localhost:8081/actuator/health  # Task Service
curl http://localhost:8082/api/health       # NLP Engine
curl http://localhost:8083/health           # Speech Service
curl http://localhost:8080/actuator/health  # Gateway
```

## 🎙️ Event Bus demo (INCREMENT A)

Минимальный голосовой цикл на Kafka + Avro + Schema Registry.

```bash
# 1) Подготовка окружения
cd platform/docker && cp env.sample .env

# 2) Запуск профиля voice (включает core)
make -C ../.. up-voice

# 3) Проверка
curl -f http://localhost:7090/healthz
```

- Веб-сокет: `ws://localhost:7090/ws/audio` — первый бинарный кадр JSON header `{sessionId, correlationId, sampleRate}`, далее PCM16 16kHz 20–40ms
- Ответ — PCM/WAV по тому же `sessionId`
- Дашборды: Grafana `http://localhost:3000`

Legacy Docker Compose сохранён в `_archive/docker-compose.legacy.yml` и продолжает работать (порты 8080–8083 неизменны).

### Создание задачи

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test task",
    "description": "Test description",
    "priority": "MEDIUM",
    "status": "PENDING"
  }'
```

### Обработка голосовой команды

```bash
curl -X POST http://localhost:8082/api/process \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Create a task called Buy groceries",
    "context": {"source": "voice"},
    "execute": false
  }'
```

## 🔍 Разработка

### Локальная разработка

```bash
# Запустить только инфраструктуру
cd docker
make dev

# Разрабатывать сервисы локально
# Подключение к БД: localhost:5432
# Подключение к Redis: localhost:6379
```

### Структура проекта

```
smart-jarvis/
├── task-service/          # Java/Spring Boot
│   ├── src/
│   ├── ARCHITECTURE.md
│   └── README.md
├── nlp-engine/           # TypeScript/Node.js
│   ├── src/
│   ├── ARCHITECTURE.md
│   └── README.md
├── speech-service/        # Python/FastAPI
│   ├── src/
│   ├── ARCHITECTURE.md
│   └── README.md
├── gateway/              # Java/Spring Cloud
├── docker/               # Docker конфигурация
│   ├── docker-compose.yml
│   ├── scripts/
│   └── monitoring/
└── docs/                 # Документация
```

## 📊 Мониторинг

### Health Checks
Все сервисы имеют настроенные health checks:
- **Task Service**: `/actuator/health`
- **NLP Engine**: `/api/health`
- **Speech Service**: `/health`
- **Gateway**: `/actuator/health`

### Метрики
- **Prometheus**: Сбор метрик со всех сервисов
- **Grafana**: Дашборды для визуализации

## 🔄 Обновление

```bash
# Остановить сервисы
make stop

# Обновить код
git pull

# Пересобрать и запустить
make rebuild
```

## 🤝 Поддержка

### Логи
```bash
# Просмотр логов всех сервисов
docker compose logs

# Просмотр логов конкретного сервиса
docker compose logs task-service
```

### Мониторинг
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

---

**SmartJARVIS** - Интеллектуальная система управления задачами с голосовым управлением, построенная на принципах Clean Architecture и микросервисной архитектуры.

## 📄 Лицензия

Этот проект является открытым исходным кодом. 