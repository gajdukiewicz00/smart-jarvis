# SmartJARVIS - Intelligent Desktop Assistant

SmartJARVIS - это интеллектуальный десктопный ассистент, построенный на микросервисной архитектуре с использованием Clean Architecture принципов.

## 🏗️ Архитектура

Проект следует принципам Clean Architecture и состоит из следующих микросервисов:

- **jarvis-desktop** - JavaFX десктопное приложение
- **task-service** - Spring Boot REST API для управления задачами
- **nlp-engine** - TypeScript/Node.js сервис для обработки естественного языка
- **speech-service** - Python FastAPI сервис для распознавания и синтеза речи
- **gateway** - Spring Cloud Gateway для маршрутизации запросов и API управления

## 🚀 Быстрый старт

### Предварительные требования

- Java 21+
- Maven 3.8+
- Node.js 18+
- Python 3.11+
- Redis (для rate limiting)
- Docker & Docker Compose (опционально)

### Установка и запуск

1. **Клонирование репозитория**
```bash
git clone <repository-url>
cd smart-jarvis
```

2. **Сборка всех компонентов**
```bash
chmod +x scripts/build-all.sh
./scripts/build-all.sh
```

3. **Запуск всех сервисов**
```bash
chmod +x scripts/start.sh
./scripts/start.sh
```

### Альтернативный запуск через Docker

```bash
cd docker
docker-compose up -d
```

## 📁 Структура проекта

```
smart-jarvis/
├── jarvis-desktop/          # JavaFX десктопное приложение
│   ├── src/main/java/
│   │   ├── domain/          # Domain модели и интерфейсы
│   │   ├── application/     # Use Cases и сервисы
│   │   ├── infrastructure/  # Внешние сервисы и репозитории
│   │   └── presentation/    # JavaFX UI контроллеры
│   └── pom.xml
├── task-service/            # Spring Boot REST API
│   ├── src/main/java/
│   │   ├── domain/          # Domain модели
│   │   ├── application/     # Use Cases
│   │   ├── infrastructure/  # JPA репозитории
│   │   └── presentation/    # REST контроллеры
│   └── pom.xml
├── gateway/                 # Spring Cloud Gateway
│   ├── src/main/java/
│   │   ├── config/          # Gateway конфигурация
│   │   ├── controller/      # Gateway контроллеры
│   │   └── filter/          # Gateway фильтры
│   ├── pom.xml
│   └── README.md
├── nlp-engine/              # TypeScript NLP сервис
│   ├── src/
│   │   ├── services/        # NLP сервисы
│   │   ├── intents/         # Обработчики интентов
│   │   └── models/          # Модели данных
│   ├── package.json
│   └── tsconfig.json
├── speech-service/          # Python speech сервис
│   ├── main.py
│   └── requirements.txt
├── docker/                  # Docker конфигурация
│   ├── docker-compose.yml
│   ├── Dockerfile.task
│   ├── Dockerfile.gateway
│   └── Dockerfile.nlp
├── scripts/                 # Скрипты сборки и запуска
│   ├── build-all.sh
│   └── start.sh
└── docs/                    # Документация
```

## 🔧 Конфигурация

### Порт-маппинг сервисов

| Сервис | Порт | Описание |
|--------|------|----------|
| Gateway | 8080 | API Gateway (единая точка входа) |
| Task Service | 8081 | REST API для задач |
| NLP Engine | 8082 | NLP обработка |
| Speech Service | 8083 | STT/TTS |
| Redis | 6379 | Rate limiting и кэширование |
| Grafana | 3000 | Мониторинг |
| PostgreSQL | 5432 | База данных |

### Переменные окружения

Создайте файл `.env` в корне проекта:

```env
# Database
POSTGRES_DB=jarvis_db
POSTGRES_USER=jarvis_user
POSTGRES_PASSWORD=jarvis_password

# Services
GATEWAY_PORT=8080
TASK_SERVICE_PORT=8081
NLP_ENGINE_PORT=8082
SPEECH_SERVICE_PORT=8083

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# NLP Engine
NODE_ENV=development
LOG_LEVEL=info

# Speech Service
WHISPER_MODEL=base
```

## 🧪 Тестирование

### Java модули
```bash
# Тестирование jarvis-desktop
cd jarvis-desktop
mvn test

# Тестирование task-service
cd task-service
mvn test

# Тестирование gateway
cd gateway
mvn test
```

### TypeScript модули
```bash
# Тестирование nlp-engine
cd nlp-engine
npm test
```

### Python модули
```bash
# Тестирование speech-service
cd speech-service
source venv/bin/activate
pytest
```

### Gateway API тестирование
```bash
# Запуск тестов Gateway
cd gateway
./test-gateway.sh
```

## 📊 Мониторинг

### Gateway Endpoints
- **Gateway Info**: http://localhost:8080/api/v1/gateway/info
- **Gateway Health**: http://localhost:8080/api/v1/gateway/health
- **API Documentation**: http://localhost:8080/api/v1/gateway/docs

### Actuator Endpoints
- **Gateway Health**: http://localhost:8080/actuator/health
- **Gateway Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

### Service Health Checks
- Task Service: http://localhost:8081/actuator/health
- NLP Engine: http://localhost:8082/health
- Speech Service: http://localhost:8083/health

### Grafana Dashboard
- URL: http://localhost:3000
- Логин: admin
- Пароль: admin

## 🔍 Логирование

Логи сохраняются в директории `logs/`:
- `gateway.log` - логи Gateway Service
- `task-service.log` - логи Task Service
- `nlp-engine.log` - логи NLP Engine
- `speech-service.log` - логи Speech Service

## 🛠️ Разработка

### Gateway Features

Gateway предоставляет следующие возможности:

- **Маршрутизация запросов** к микросервисам
- **Rate Limiting** для защиты от перегрузки
- **Circuit Breaker** для обработки отказов сервисов
- **CORS** поддержка для веб-приложений
- **Мониторинг и метрики** через Actuator
- **Логирование** всех запросов и ответов
- **Fallback** обработка при недоступности сервисов

### Добавление нового интента

1. Создайте новый обработчик в `nlp-engine/src/intents/`
2. Зарегистрируйте его в `IntentProcessor`
3. Добавьте соответствующий Use Case в `jarvis-desktop`

### Добавление новой команды

1. Создайте новый тип в `Command.CommandType`
2. Добавьте обработку в `JarvisCore.executeCommand()`
3. Создайте соответствующий Use Case

### Добавление нового сервиса

1. Создайте новый модуль
2. Добавьте маршрут в Gateway `application.yml`
3. Добавьте Dockerfile
4. Обновите `docker-compose.yml`
5. Добавьте в скрипты сборки и запуска

## 📝 API Документация

### Gateway API

Все запросы к сервисам проходят через Gateway на порту 8080:

```bash
# Task Service через Gateway
GET /api/v1/tasks
POST /api/v1/tasks
PUT /api/v1/tasks/{id}
DELETE /api/v1/tasks/{id}

# NLP Engine через Gateway
POST /api/v1/nlp/process
GET /api/v1/nlp/intents

# Speech Service через Gateway
POST /api/v1/speech/recognize
POST /api/v1/speech/synthesize
```

### Task Service API

```bash
# Создать задачу
POST /api/v1/tasks
{
  "title": "Новая задача",
  "description": "Описание задачи",
  "priority": "HIGH",
  "dueDate": "2024-01-15T10:00:00"
}

# Получить все задачи
GET /api/v1/tasks

# Обновить задачу
PUT /api/v1/tasks/{id}

# Удалить задачу
DELETE /api/v1/tasks/{id}
```

### NLP Engine API

```bash
# Обработать интент
POST /api/v1/nlp/process
{
  "text": "Создай задачу на завтра",
  "context": {}
}
```

### Speech Service API

```bash
# Преобразовать речь в текст
POST /api/v1/speech/recognize
# (multipart/form-data с аудио файлом)

# Преобразовать текст в речь
POST /api/v1/speech/synthesize
{
  "text": "Привет, JARVIS!",
  "voice": "default",
  "rate": 150
}
```

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте feature ветку
3. Внесите изменения
4. Добавьте тесты
5. Создайте Pull Request

## 📄 Лицензия


## 🆘 Поддержка

- Создайте Issue для багов
- Используйте Discussions для вопросов
- Обращайтесь к документации в папке `docs/`

## 🚀 Roadmap

- [ ] Интеграция с календарем
- [ ] Поддержка голосовых команд
- [ ] Машинное обучение для улучшения NLP
- [ ] Мобильное приложение
- [ ] Интеграция с внешними API
- [ ] Расширенная аналитика
- [ ] JWT аутентификация в Gateway
- [ ] GraphQL поддержка
- [ ] WebSocket поддержка для real-time обновлений 