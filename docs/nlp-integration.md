# NLP Engine Integration - SmartJARVIS

Документация по интеграции NLP Engine с другими сервисами системы SmartJARVIS.

## 🧠 Обзор NLP Engine

NLP Engine является центральным компонентом для обработки естественного языка в системе SmartJARVIS. Он обеспечивает:

- **Распознавание намерений** из голосовых и текстовых команд
- **Извлечение сущностей** из пользовательского ввода
- **Интеграцию с Task Service** для управления задачами
- **REST API** для взаимодействия с другими сервисами

## 🔗 Архитектура интеграции

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Speech Service │    │   NLP Engine    │    │  Task Service   │
│                 │    │                 │    │                 │
│ • Speech-to-    │───▶│ • Intent        │───▶│ • Task CRUD     │
│   Text          │    │   Recognition   │    │ • Statistics    │
│ • Text-to-      │    │ • Entity        │    │ • Management    │
│   Speech        │    │   Extraction    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📡 API Endpoints

### NLP Engine (Port 8082)

#### Health Check
```http
GET /health
```

#### Process Intent
```http
POST /api/process
Content-Type: application/json

{
  "text": "Create a task called 'Buy groceries'",
  "context": {},
  "execute": false
}
```

#### Execute Action
```http
POST /api/execute
Content-Type: application/json

{
  "intent": "task_create",
  "entities": {
    "taskDetails": {
      "title": "Buy groceries",
      "priority": "MEDIUM"
    }
  }
}
```

#### Get Examples
```http
GET /api/examples
```

#### Task Service Status
```http
GET /api/task-service/status
```

### Speech Service Integration (Port 8083)

#### Voice Command Processing
```http
POST /api/voice-command
Content-Type: multipart/form-data

audio_file: [audio file]
```

#### NLP Status Check
```http
GET /api/nlp-status
```

## 🎯 Поддерживаемые команды

### Управление задачами
- `"Create a task called 'Buy groceries'"`
- `"Add a high priority task 'Finish project report'"`
- `"Show my tasks"`
- `"List pending tasks"`
- `"Complete task 'Buy groceries'"`
- `"Update task priority to urgent"`
- `"Delete task 'Old task'"`
- `"What are my task statistics?"`

### Естественный язык
- `"I need to buy groceries"`
- `"Remind me to call the doctor"`
- `"What tasks are due today?"`
- `"Mark the project task as done"`
- `"Set priority to high for the meeting task"`

## 🔧 Конфигурация

### Переменные окружения

#### NLP Engine
```env
NLP_ENGINE_PORT=8082
TASK_SERVICE_URL=http://localhost:8080
NODE_ENV=development
LOG_LEVEL=info
```

#### Speech Service
```env
SPEECH_SERVICE_PORT=8083
NLP_ENGINE_URL=http://localhost:8082
```

### Docker Compose
```yaml
nlp-engine:
  build:
    context: ../nlp-engine
    dockerfile: ../docker/Dockerfile.nlp
  environment:
    NODE_ENV: production
    NLP_ENGINE_PORT: 8082
    TASK_SERVICE_URL: http://task-service:8081
  ports:
    - "8082:8082"

speech-service:
  build:
    context: ../speech-service
    dockerfile: Dockerfile
  environment:
    SPEECH_SERVICE_PORT: 8083
    NLP_ENGINE_URL: http://nlp-engine:8082
  ports:
    - "8083:8083"
```

## 🚀 Запуск и тестирование

### 1. Запуск сервисов
```bash
# Запуск всех сервисов
docker-compose up -d

# Или запуск отдельных сервисов
cd nlp-engine && npm run dev
cd speech-service && python main.py
```

### 2. Тестирование NLP Engine
```bash
cd nlp-engine
./test-api.sh
```

### 3. Тестирование интеграции
```bash
cd speech-service
python test-integration.py
```

### 4. Проверка здоровья сервисов
```bash
# NLP Engine
curl http://localhost:8082/health

# Speech Service
curl http://localhost:8083/health

# Task Service
curl http://localhost:8080/tasks/ping
```

## 📊 Примеры использования

### 1. Создание задачи через голос
```bash
# Запись аудио команды
echo "Create a task called 'Buy groceries'" | text2wave -o command.wav

# Отправка в Speech Service
curl -X POST http://localhost:8083/api/voice-command \
  -F "audio_file=@command.wav"
```

### 2. Обработка текстовой команды
```bash
curl -X POST http://localhost:8082/api/process \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Show my tasks",
    "context": {},
    "execute": false
  }'
```

### 3. Выполнение действия
```bash
curl -X POST http://localhost:8082/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "intent": "task_create",
    "entities": {
      "taskDetails": {
        "title": "Test Task",
        "priority": "MEDIUM"
      }
    }
  }'
```

## 🔍 Отладка

### Логи NLP Engine
```bash
# Просмотр логов
docker logs jarvis-nlp-engine

# Или локально
tail -f nlp-engine/logs/combined.log
```

### Логи Speech Service
```bash
# Просмотр логов
docker logs jarvis-speech-service

# Или локально
tail -f speech-service/logs/speech_service.log
```

### Проверка соединений
```bash
# Проверка доступности NLP Engine
curl http://localhost:8082/health

# Проверка статуса Task Service из NLP Engine
curl http://localhost:8082/api/task-service/status

# Проверка статуса NLP Engine из Speech Service
curl http://localhost:8083/api/nlp-status
```

## 🛠 Разработка

### Добавление новых интентов

1. Создайте новый обработчик в `nlp-engine/src/intents/`
2. Зарегистрируйте обработчик в `IntentProcessor`
3. Добавьте тесты в `__tests__/`
4. Обновите документацию

### Расширение функциональности

1. Добавьте новые методы в `TaskServiceClient`
2. Обновите `NLPService` для обработки новых действий
3. Добавьте новые эндпоинты в `index.ts`
4. Обновите тесты и документацию

## 📈 Мониторинг

### Метрики для отслеживания
- Количество обработанных запросов
- Время ответа NLP Engine
- Точность распознавания интентов
- Статус интеграции с Task Service
- Ошибки обработки команд

### Health Checks
```bash
# Проверка всех сервисов
curl http://localhost:8082/health  # NLP Engine
curl http://localhost:8083/health  # Speech Service
curl http://localhost:8080/tasks/ping  # Task Service
```

## 🔮 Планы развития

- [ ] Интеграция с машинным обучением для улучшения распознавания
- [ ] Поддержка многоязычности
- [ ] Расширенная аналитика и метрики
- [ ] WebSocket для real-time коммуникации
- [ ] Кэширование результатов распознавания
- [ ] Интеграция с другими сервисами (календарь, почта, etc.)

## 📝 Лицензия

MIT License 