# SmartJARVIS Task Service - Архитектура

## Обзор

Task Service реализован с использованием **Clean Architecture** и **SOLID принципов**. Проект разделен на слои с четким разделением ответственности.

## Структура проекта

```
src/main/java/com/smartjarvis/taskservice/
├── domain/                    # Доменный слой
│   ├── entities/             # Доменные сущности
│   ├── repositories/         # Интерфейсы репозиториев
│   └── services/            # Доменные сервисы
├── application/              # Прикладной слой
│   ├── dto/                 # Data Transfer Objects
│   ├── services/            # Прикладные сервисы
│   └── usecases/           # Use Cases (сценарии использования)
├── infrastructure/           # Инфраструктурный слой
│   ├── config/             # Конфигурации
│   ├── repositories/       # Реализации репозиториев
│   ├── services/          # Реализации сервисов
│   └── filters/           # Фильтры
└── presentation/           # Презентационный слой
    ├── controllers/        # Контроллеры
    └── exceptions/        # Обработчики исключений
```

## Слои архитектуры

### 1. Domain Layer (Доменный слой)
- **Entities**: Доменные сущности с бизнес-логикой (Task)
- **Services**: Доменные сервисы с бизнес-правилами (TaskService)
- **Repositories**: Интерфейсы для доступа к данным (TaskRepository)

### 2. Application Layer (Прикладной слой)
- **DTOs**: Объекты для передачи данных между слоями (TaskDto)
- **Use Cases**: Сценарии использования приложения (CreateTaskUseCase, GetTaskUseCase)
- **Services**: Координация между доменом и инфраструктурой

### 3. Infrastructure Layer (Инфраструктурный слой)
- **Config**: Конфигурации Spring Boot
- **Repositories**: Реализации доступа к данным
- **Services**: Реализации доменных сервисов (TaskServiceImpl)

### 4. Presentation Layer (Презентационный слой)
- **Controllers**: HTTP контроллеры (TaskController)
- **Exceptions**: Обработчики исключений

## Принципы

### SOLID принципы
- **S** - Single Responsibility: Каждый класс имеет одну ответственность
- **O** - Open/Closed: Классы открыты для расширения, закрыты для изменения
- **L** - Liskov Substitution: Интерфейсы и абстракции
- **I** - Interface Segregation: Специфичные интерфейсы
- **D** - Dependency Inversion: Зависимости от абстракций

### Clean Architecture
- **Independence of Frameworks**: Архитектура не зависит от фреймворков
- **Testability**: Легко тестировать каждый слой
- **Independence of UI**: UI можно легко заменить
- **Independence of Database**: База данных - деталь реализации
- **Independence of any external agency**: Внешние агентства - детали

## Конфигурация

### Environment Variables
- `SERVER_PORT`: Порт сервера (по умолчанию: 8081)
- `SPRING_PROFILES_ACTIVE`: Активный профиль (по умолчанию: dev)
- `DATABASE_URL`: URL базы данных
- `DATABASE_USERNAME`: Имя пользователя БД
- `DATABASE_PASSWORD`: Пароль БД
- `EUREKA_ENABLED`: Включение Eureka (по умолчанию: true)

### Profiles
- **dev**: Разработка
- **prod**: Продакшн
- **test**: Тестирование

## Тестирование

### Структура тестов
```
src/test/java/com/smartjarvis/taskservice/
├── domain/           # Тесты доменного слоя
├── application/      # Тесты прикладного слоя
├── infrastructure/   # Тесты инфраструктурного слоя
└── presentation/     # Тесты презентационного слоя
```

### Запуск тестов
```bash
# Все тесты
mvn test

# Только unit тесты
mvn test -Dtest=*Test

# С проверкой стиля кода
mvn checkstyle:check
```

## Линтинг и форматирование

### Checkstyle
- Конфигурация: `checkstyle.xml`
- Проверка: `mvn checkstyle:check`

## Сборка и запуск

```bash
# Сборка
mvn clean compile

# Запуск тестов
mvn test

# Сборка JAR
mvn clean package

# Запуск
java -jar target/task-service-1.0.0.jar
```

## API Endpoints

### Tasks
- `POST /api/v1/tasks` - Создать задачу
- `GET /api/v1/tasks/{id}` - Получить задачу по ID
- `GET /api/v1/tasks` - Получить все задачи
- `GET /api/v1/tasks/health` - Проверка здоровья сервиса

### Мониторинг
- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`

### Логирование
- Уровень: `INFO` (настраивается через `LOG_LEVEL`)
- Формат: `%d{yyyy-MM-dd HH:mm:ss} - %msg%n`

## Бизнес-логика

### Task Entity
- Создание задач с приоритетами
- Управление статусами (PENDING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED)
- Проверка просроченных задач
- Проверка задач, которые скоро должны быть выполнены
- Валидация данных

### Task Service
- CRUD операции для задач
- Бизнес-логика управления задачами
- Статистика по задачам
- Фильтрация по различным критериям

## Статус реализации

### ✅ Завершено
- [x] Доменный слой (Domain Layer) - Task entity, TaskRepository, TaskService
- [x] Слой приложения (Application Layer) - TaskDto, CreateTaskUseCase, GetTaskUseCase
- [x] Инфраструктурный слой (Infrastructure Layer) - InMemoryTaskRepository, TaskServiceImpl
- [x] Слой представления (Presentation Layer) - TaskController
- [x] Базовые CRUD операции (создание, получение)
- [x] Бизнес-логика (проверка сроков, валидация)
- [x] Unit тесты (TaskTest.java)
- [x] Интеграционные тесты (TaskServiceIntegrationTest.java)
- [x] Документация (README.md, ARCHITECTURE.md)

### 🔄 В процессе
- [ ] Интеграция с базой данных PostgreSQL
- [ ] Дополнительные операции (обновление, удаление задач)
- [ ] Фильтрация и поиск по различным критериям
- [ ] Пагинация для больших списков

### 📋 Планируется
- [ ] Аутентификация и авторизация
- [ ] Структурированное логирование
- [ ] Мониторинг и метрики (Actuator)
- [ ] Docker контейнеризация
- [ ] CI/CD pipeline
- [ ] API документация (Swagger/OpenAPI)

## Тестирование

### Unit Tests
- `TaskTest.java` - тесты доменной сущности Task
- Проверка бизнес-логики (isOverdue, isDueSoon)
- Проверка валидации данных
- Проверка создания и обновления задач

### Integration Tests
- `TaskServiceIntegrationTest.java` - интеграционные тесты
- Проверка взаимодействия всех слоев архитектуры
- Проверка полного цикла создания и получения задач
- Проверка бизнес-логики в контексте всей системы

### Результаты тестирования
```bash
# Запуск тестов
mvn test

# Результат: ✅ 17 тестов, 0 ошибок
# - TaskTest: 13 тестов
# - TaskServiceIntegrationTest: 4 теста
```

## Доменная модель

### Task Entity
```java
public class Task {
    public enum TaskPriority { LOW, MEDIUM, HIGH, URGENT }
    public enum TaskStatus { PENDING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED }
    
    private UUID id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private String category;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
```

### Бизнес-правила
- Задача должна иметь заголовок (title не может быть null или пустым)
- Приоритет может быть: LOW, MEDIUM, HIGH, URGENT
- Статус может быть: PENDING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED
- Задача считается просроченной, если dueDate < текущее время
- Задача считается срочной, если dueDate в пределах 24 часов
- При создании задачи статус автоматически устанавливается в PENDING
- Время создания (createdAt) устанавливается автоматически
- Время обновления (updatedAt) обновляется при каждом изменении 