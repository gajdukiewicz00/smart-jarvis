# NLP Engine - SmartJARVIS

NLP Engine для обработки голосовых команд и управления задачами в системе SmartJARVIS.

## 🚀 Возможности

- **Обработка естественного языка** - распознавание намерений из голосовых команд
- **Интеграция с Task Service** - создание, управление и отслеживание задач
- **Расширенное распознавание интентов** - поддержка различных паттернов речи
- **REST API** - HTTP endpoints для интеграции с другими сервисами
- **Логирование** - детальное логирование всех операций

## 📋 Поддерживаемые команды

### Управление задачами
- `"Create a task called 'Buy groceries'"`
- `"Add a high priority task 'Finish project report'"`
- `"Show my tasks"`
- `"List pending tasks"`
- `"Complete task 'Buy groceries'"`
- `"Update task priority to urgent"`
- `"Delete task 'Old task'"`
- `"What are my task statistics?"`
- `"How many tasks do I have?"`
- `"Show completed tasks"`

### Естественный язык
- `"I need to buy groceries"`
- `"Remind me to call the doctor"`
- `"What tasks are due today?"`
- `"Mark the project task as done"`
- `"Set priority to high for the meeting task"`
- `"Show me all my pending work"`

## 🛠 Установка и запуск

### Предварительные требования
- Node.js >= 18.0.0
- npm или yarn

### Установка зависимостей
```bash
npm install
```

### Конфигурация
Скопируйте файл конфигурации:
```bash
cp env.example .env
```

Отредактируйте `.env` файл:
```env
NLP_ENGINE_PORT=8082
TASK_SERVICE_URL=http://localhost:8080
NODE_ENV=development
LOG_LEVEL=info
```

### Сборка и запуск
```bash
# Сборка TypeScript
npm run build

# Запуск в production
npm start

# Запуск в development режиме
npm run dev
```

## 📡 API Endpoints

### Health Check
```
GET /health
```
Проверка состояния сервиса и подключения к Task Service.

### Обработка интентов
```
POST /api/process
```
Обработка текстового ввода и распознавание намерений.

**Тело запроса:**
```json
{
  "text": "Create a task called 'Buy groceries'",
  "context": {},
  "execute": false
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "intent": "task_create",
    "confidence": 0.95,
    "entities": {
      "action": "create",
      "type": "task",
      "extractedDetails": {
        "title": "Buy groceries",
        "priority": "MEDIUM",
        "description": "Created via voice command"
      }
    },
    "response": "I will create a new task for you."
  }
}
```

### Выполнение действий
```
POST /api/execute
```
Выполнение действий на основе распознанных интентов.

**Тело запроса:**
```json
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

### Статус Task Service
```
GET /api/task-service/status
```
Проверка доступности Task Service.

### Примеры команд
```
GET /api/examples
```
Получение примеров поддерживаемых голосовых команд.

### Поддерживаемые интенты
```
GET /api/intents
```
Список всех поддерживаемых типов интентов.

## 🔧 Архитектура

### Сервисы
- **IntentProcessor** - обработка и маршрутизация интентов
- **NLPService** - основной сервис обработки естественного языка
- **TaskServiceClient** - клиент для взаимодействия с Task Service

### Обработчики интентов
- **TaskIntentHandler** - обработка команд управления задачами
- **ReminderIntentHandler** - обработка напоминаний
- **SystemIntentHandler** - системные команды

### Распознавание паттернов
Система использует регулярные выражения для распознавания различных паттернов речи:
- Создание задач: `/create.*task/i`, `/add.*task/i`, `/new.*task/i`
- Просмотр задач: `/list.*task/i`, `/show.*task/i`, `/get.*task/i`
- Завершение задач: `/complete.*task/i`, `/finish.*task/i`, `/done.*task/i`
- Статистика: `/statistics/i`, `/stats/i`, `/summary/i`

## 🔗 Интеграция с Task Service

NLP Engine автоматически интегрируется с Task Service для выполнения операций:

### Создание задач
```typescript
const task = await taskServiceClient.createTask({
  title: "Buy groceries",
  priority: "MEDIUM",
  status: "PENDING",
  description: "Created via voice command"
});
```

### Получение списка задач
```typescript
const tasks = await taskServiceClient.getAllTasks();
const statistics = await taskServiceClient.getTaskStatistics();
```

### Обновление задач
```typescript
const updatedTask = await taskServiceClient.updateTask(taskId, {
  status: "COMPLETED",
  completedAt: new Date().toISOString()
});
```

## 📊 Логирование

Логи сохраняются в директории `logs/`:
- `error.log` - ошибки
- `combined.log` - все логи

Уровни логирования:
- `error` - ошибки
- `warn` - предупреждения
- `info` - информационные сообщения
- `debug` - отладочная информация

## 🧪 Тестирование

```bash
# Запуск тестов
npm test

# Запуск тестов в watch режиме
npm run test:watch
```

## 🔒 Безопасность

- **Helmet** - защита HTTP заголовков
- **CORS** - настройка Cross-Origin Resource Sharing
- **Compression** - сжатие ответов
- **Rate Limiting** - ограничение частоты запросов (планируется)

## 🚀 Развертывание

### Docker
```bash
# Сборка образа
docker build -t nlp-engine .

# Запуск контейнера
docker run -p 8082:8082 nlp-engine
```

### Docker Compose
```yaml
nlp-engine:
  build: ./nlp-engine
  ports:
    - "8082:8082"
  environment:
    - TASK_SERVICE_URL=http://task-service:8080
  depends_on:
    - task-service
```

## 📈 Мониторинг

### Health Check
```
GET /health
```

### Метрики (планируется)
- Количество обработанных запросов
- Время ответа
- Точность распознавания интентов
- Статистика ошибок

## 🔮 Планы развития

- [ ] Интеграция с Speech Service для обработки аудио
- [ ] Машинное обучение для улучшения распознавания
- [ ] Поддержка многоязычности
- [ ] Расширенная аналитика и метрики
- [ ] WebSocket для real-time коммуникации
- [ ] Кэширование результатов распознавания

## 📝 Лицензия

MIT License 