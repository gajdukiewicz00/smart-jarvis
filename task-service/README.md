# SmartJARVIS Task Service

Сервис управления задачами для системы SmartJARVIS, реализованный с использованием Clean Architecture.

## Архитектура

Проект следует принципам Clean Architecture и разделен на следующие слои:

### 🏗️ Domain Layer (Доменный слой)
- **Entities**: Доменные сущности (`Task.java`)
- **Repositories**: Интерфейсы репозиториев (`TaskRepository.java`)
- **Services**: Доменные сервисы (`TaskService.java`)

### 📋 Application Layer (Слой приложения)
- **DTOs**: Объекты передачи данных (`TaskDto.java`)
- **Use Cases**: Сценарии использования (`CreateTaskUseCase.java`, `GetTaskUseCase.java`)

### 🏛️ Infrastructure Layer (Инфраструктурный слой)
- **Repositories**: Реализации репозиториев (`InMemoryTaskRepository.java`)
- **Services**: Реализации сервисов (`TaskServiceImpl.java`)

### 🎯 Presentation Layer (Слой представления)
- **Controllers**: REST контроллеры (`TaskController.java`)

## Функциональность

### Основные возможности:
- ✅ Создание задач
- ✅ Получение задач по ID
- ✅ Получение всех задач
- ✅ Бизнес-логика (проверка сроков, приоритетов)
- ✅ Валидация данных

### Доменная модель Task:
- **Приоритеты**: LOW, MEDIUM, HIGH, URGENT
- **Статусы**: PENDING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED
- **Бизнес-логика**: 
  - Проверка просроченных задач
  - Проверка задач с приближающимся сроком
  - Валидация данных

## Запуск проекта

### Требования
- Java 17+
- Maven 3.6+

### Компиляция и тестирование
```bash
# Компиляция
mvn clean compile

# Запуск тестов
mvn test

# Полная сборка
mvn clean package
```

### Запуск приложения
```bash
# Запуск Spring Boot приложения
mvn spring-boot:run
```

## API Endpoints

### Создание задачи
```http
POST /api/tasks
Content-Type: application/json

{
  "title": "Название задачи",
  "description": "Описание задачи",
  "priority": "HIGH",
  "category": "Работа",
  "dueDate": "2024-01-15T10:00:00"
}
```

### Получение задачи по ID
```http
GET /api/tasks/{id}
```

### Получение всех задач
```http
GET /api/tasks
```

## Тестирование

Проект включает два типа тестов:

### Unit Tests
- `TaskTest.java` - тесты доменной сущности Task
- Проверка бизнес-логики
- Проверка валидации данных

### Integration Tests
- `TaskServiceIntegrationTest.java` - интеграционные тесты
- Проверка взаимодействия всех слоев
- Проверка полного цикла создания и получения задач

## Архитектурные принципы

### ✅ Clean Architecture
- Разделение на слои с четкими границами
- Зависимости направлены внутрь (к домену)
- Независимость от внешних фреймворков

### ✅ SOLID принципы
- **Single Responsibility**: Каждый класс имеет одну ответственность
- **Open/Closed**: Расширяемость без изменения существующего кода
- **Liskov Substitution**: Возможность замены реализаций
- **Interface Segregation**: Тонкие интерфейсы
- **Dependency Inversion**: Зависимость от абстракций

### ✅ Domain-Driven Design
- Богатая доменная модель
- Бизнес-логика в доменном слое
- Ubiquitous Language

## Структура проекта

```
src/main/java/com/smartjarvis/taskservice/
├── application/
│   ├── dto/
│   │   └── TaskDto.java
│   └── usecases/
│       ├── CreateTaskUseCase.java
│       └── GetTaskUseCase.java
├── domain/
│   ├── entities/
│   │   └── Task.java
│   ├── repositories/
│   │   └── TaskRepository.java
│   └── services/
│       └── TaskService.java
├── infrastructure/
│   ├── repositories/
│   │   └── InMemoryTaskRepository.java
│   └── services/
│       └── TaskServiceImpl.java
└── presentation/
    └── controllers/
        └── TaskController.java
```

## Следующие шаги

1. **База данных**: Интеграция с PostgreSQL
2. **Дополнительные операции**: Обновление, удаление задач
3. **Фильтрация и поиск**: По статусу, приоритету, категории
4. **Пагинация**: Для больших списков задач
5. **Аутентификация**: Защита API
6. **Логирование**: Структурированное логирование
7. **Мониторинг**: Метрики и health checks

## Лицензия

