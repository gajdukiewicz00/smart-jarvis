# HTTP Clients Implementation

## Обзор

В desktop приложении SmartJARVIS теперь реализованы реальные HTTP клиенты вместо заглушек. Это позволяет приложению взаимодействовать с микросервисами через REST API.

## Реализованные HTTP клиенты

### 1. TaskServiceClient (HttpTaskServiceClient)

**Файл:** `src/main/java/com/smartjarvis/desktop/infrastructure/services/impl/HttpTaskServiceClient.java`

**Функциональность:**
- Создание задач (`POST /api/v1/tasks`)
- Обновление задач (`PUT /api/v1/tasks/{id}`)
- Удаление задач (`DELETE /api/v1/tasks/{id}`)
- Получение задачи (`GET /api/v1/tasks/{id}`)
- Получение всех задач (`GET /api/v1/tasks`)
- Тестирование соединения (`GET /api/v1/tasks/ping`)

**Особенности:**
- Использует Spring WebClient для HTTP запросов
- Таймаут 10 секунд для всех запросов
- Подробное логирование операций
- Обработка ошибок HTTP (404, 500, etc.)
- Автоматическое преобразование между DTO и Domain объектами

### 2. DTO классы

**Файл:** `src/main/java/com/smartjarvis/desktop/infrastructure/dto/TaskDto.java`

**Функциональность:**
- Преобразование между Domain Task и DTO объектами
- Совместимость с API task-service
- Поддержка всех полей задачи (id, title, description, priority, status, etc.)

## Конфигурация

### Файл конфигурации
`src/main/resources/application.properties`

```properties
# Task Service Configuration
task.service.url=http://localhost:8082
task.service.timeout=10

# NLP Service Configuration
nlp.service.url=http://localhost:3000

# Speech Service Configuration
speech.service.url=http://localhost:5000
```

### ServiceFactory
**Файл:** `src/main/java/com/smartjarvis/desktop/infrastructure/services/ServiceFactory.java`

Фабрика для создания сервисов с автоматической загрузкой конфигурации.

## Использование

### 1. Запуск сервисов

```bash
# Запуск всех микросервисов
docker-compose up -d

# Проверка статуса сервисов
./jarvis-desktop/test-http-clients.sh
```

### 2. Запуск desktop приложения

```bash
# Компиляция и запуск
mvn clean compile exec:java -pl jarvis-desktop

# Или через Maven plugin
mvn javafx:run -pl jarvis-desktop
```

### 3. Тестирование HTTP клиентов

```java
// Создание клиента
TaskServiceClient client = ServiceFactory.createTaskServiceClient();

// Тестирование соединения
if (client instanceof HttpTaskServiceClient) {
    HttpTaskServiceClient httpClient = (HttpTaskServiceClient) client;
    boolean connected = httpClient.testConnection();
    System.out.println("Connection: " + connected);
}

// Создание задачи
Task task = new Task("Test Task", "Test Description", Task.TaskPriority.MEDIUM, LocalDateTime.now());
Task createdTask = client.createTask(task);

// Получение всех задач
List<Task> tasks = client.getAllTasks();
```

## Логирование

HTTP клиенты используют SLF4J для логирования:

- **INFO:** Успешные операции
- **WARNING:** Проблемы с соединением
- **ERROR:** Ошибки HTTP запросов

Пример логов:
```
INFO: Task created successfully: Test Task
INFO: Retrieved 5 tasks successfully
ERROR: HTTP error creating task: 500 - Internal Server Error
```

## Обработка ошибок

### Типы ошибок:
1. **WebClientResponseException.NotFound (404)** - ресурс не найден
2. **WebClientResponseException (4xx, 5xx)** - HTTP ошибки
3. **TimeoutException** - превышение времени ожидания
4. **ConnectionException** - проблемы с сетью

### Стратегия обработки:
- Автоматический fallback на локальные заглушки при недоступности сервисов
- Подробное логирование ошибок
- Пользовательские сообщения об ошибках

## Архитектура

```
Desktop App
    ↓
ServiceFactory
    ↓
HttpTaskServiceClient
    ↓
WebClient → Task Service API
```

## Совместимость

- **Task Service API:** Полная совместимость с REST API
- **DTO Mapping:** Автоматическое преобразование JSON ↔ Domain Objects
- **Error Handling:** Graceful degradation при недоступности сервисов

## Дальнейшее развитие

1. **Добавить HTTP клиенты для:**
   - NLP Service
   - Speech Service
   - Notification Service

2. **Улучшения:**
   - Retry механизм для неудачных запросов
   - Circuit Breaker паттерн
   - Кэширование ответов
   - Асинхронные операции

3. **Мониторинг:**
   - Метрики производительности
   - Health checks
   - Distributed tracing