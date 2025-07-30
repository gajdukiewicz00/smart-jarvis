# NLP Engine - Clean Architecture Implementation

## Обзор

NLP Engine реализован с использованием принципов Clean Architecture и Domain-Driven Design (DDD), обеспечивая четкое разделение ответственности, высокую тестируемость и легкость поддержки.

## Архитектура

### Слои архитектуры

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

### Принципы проектирования

1. **Dependency Inversion Principle**: Доменный слой не зависит от внешних слоев
2. **Single Responsibility Principle**: Каждый класс имеет одну ответственность
3. **Open/Closed Principle**: Система открыта для расширения, закрыта для модификации
4. **Interface Segregation Principle**: Интерфейсы разделены по функциональности

## Доменные модели

### Intent (Намерение)
```typescript
interface Intent {
  id: string;
  type: IntentTypeValue;
  confidence: ConfidenceLevelValue;
  entities: Record<string, any>;
  text: string;
  context?: Record<string, any>;
  createdAt: Date;
}
```

### NLPRequest (Запрос NLP)
```typescript
interface NLPRequest {
  id: string;
  text: string;
  context?: Record<string, any>;
  execute: boolean;
  createdAt: Date;
}
```

### NLPResponse (Ответ NLP)
```typescript
interface NLPResponse {
  id: string;
  requestId: string;
  intent: Intent;
  response: string;
  status: ProcessingStatusValue;
  processingTime: number;
  createdAt: Date;
}
```

## Value Objects

### IntentType
```typescript
enum IntentType {
  TASK_CREATE = 'task_create',
  TASK_LIST = 'task_list',
  TASK_COMPLETE = 'task_complete',
  TASK_UPDATE = 'task_update',
  TASK_DELETE = 'task_delete',
  TASK_STATISTICS = 'task_statistics',
  UNKNOWN = 'unknown'
}
```

### ConfidenceLevel
```typescript
enum ConfidenceLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  VERY_HIGH = 'very_high'
}
```

### ProcessingStatus
```typescript
enum ProcessingStatus {
  PENDING = 'pending',
  PROCESSING = 'processing',
  COMPLETED = 'completed',
  FAILED = 'failed',
  CANCELLED = 'cancelled'
}
```

## Use Cases

### ProcessIntentUseCase
Обрабатывает запросы на распознавание намерений:

```typescript
async execute(requestDto: ProcessIntentRequestDto): Promise<ProcessIntentResponseDto>
```

### ExecuteActionUseCase
Выполняет действия на основе распознанных намерений:

```typescript
async execute(requestDto: ExecuteActionRequestDto): Promise<ExecuteActionResponseDto>
```

## API Endpoints

### POST /api/process
Обработка текстового ввода и распознавание намерений.

**Запрос:**
```json
{
  "text": "Create a task called 'Buy groceries'",
  "context": { "source": "voice" },
  "execute": false
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "intent": {
      "id": "intent_1234567890_abc123",
      "type": "task_create",
      "confidence": 0.9,
      "entities": {
        "action": "create",
        "taskDetails": {
          "title": "Buy groceries",
          "priority": "MEDIUM"
        }
      },
      "text": "Create a task called 'Buy groceries'",
      "context": { "source": "voice" },
      "createdAt": "2024-01-01T12:00:00.000Z"
    },
    "response": "I'll create a task for you: \"Buy groceries\"",
    "processingTime": 45
  }
}
```

### POST /api/execute
Выполнение действий на основе намерений.

**Запрос:**
```json
{
  "intent": "task_create",
  "entities": {
    "action": "create",
    "taskDetails": {
      "title": "Test task",
      "priority": "MEDIUM"
    }
  }
}
```

### GET /api/health
Проверка состояния сервиса.

### GET /api/statistics
Получение статистики обработки.

### GET /api/intents
Список поддерживаемых намерений.

### GET /api/examples
Примеры команд.

## Поддерживаемые команды

### Управление задачами
- `"Create a task called 'Buy groceries'"`
- `"Show my tasks"`
- `"List pending tasks"`
- `"Complete task 'Buy groceries'"`
- `"Delete task 'Old task'"`
- `"What are my task statistics?"`

### Естественный язык
- `"I need to buy groceries"`
- `"Remind me to call the doctor"`
- `"What tasks are due today?"`

## Установка и запуск

### Предварительные требования
- Node.js >= 18.0.0
- npm или yarn

### Установка зависимостей
```bash
npm install
```

### Сборка
```bash
npm run build
```

### Запуск в development режиме
```bash
npm run dev
```

### Запуск в production
```bash
npm start
```

### Тестирование
```bash
npm test
```

## Тестирование

### Unit Tests
Тесты для доменных сущностей и бизнес-логики:
```bash
npm test -- --testPathPattern=domain
```

### Integration Tests
Тесты для интеграции слоев:
```bash
npm test -- --testPathPattern=integration
```

### Coverage
```bash
npm test -- --coverage
```

## Конфигурация

### Переменные окружения
```env
NLP_ENGINE_PORT=8082
NODE_ENV=development
LOG_LEVEL=info
```

## Мониторинг

### Health Check
```
GET /api/health
```

### Метрики
- Количество обработанных запросов
- Время ответа
- Точность распознавания намерений
- Статистика ошибок

## Расширение функциональности

### Добавление новых намерений
1. Добавить новый тип в `IntentType` enum
2. Добавить паттерн в `IntentRecognitionServiceImpl`
3. Реализовать обработку в `generateResponse`
4. Добавить тесты

### Добавление новых репозиториев
1. Реализовать интерфейс `NLPRepository`
2. Добавить в `setupDependencies` в `app.ts`
3. Добавить тесты

## Преимущества Clean Architecture

1. **Тестируемость**: Каждый слой можно тестировать независимо
2. **Независимость от фреймворков**: Бизнес-логика не зависит от Express
3. **Независимость от UI**: Можно легко заменить REST API на GraphQL
4. **Независимость от базы данных**: Можно заменить in-memory на PostgreSQL
5. **Независимость от внешних агентов**: Бизнес-правила не зависят от внешних сервисов

## Статус реализации

- [x] Domain Layer - Entities, Value Objects, Domain Services
- [x] Application Layer - Use Cases, DTOs
- [x] Infrastructure Layer - Repositories, External Services
- [x] Presentation Layer - Controllers, Routes
- [x] Unit Tests
- [x] Integration Tests
- [x] Documentation

## Планы развития

- [ ] Интеграция с реальными NLP библиотеками
- [ ] Машинное обучение для улучшения распознавания
- [ ] Поддержка многоязычности
- [ ] Кэширование результатов
- [ ] Метрики и мониторинг
- [ ] WebSocket для real-time коммуникации 