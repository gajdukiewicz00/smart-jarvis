# SmartJARVIS Gateway - Архитектура

## Обзор

Gateway сервис реализован с использованием **Clean Architecture** и **SOLID принципов**. Проект разделен на слои с четким разделением ответственности.

## Структура проекта

```
src/main/java/com/smartjarvis/gateway/
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
- **Entities**: Доменные сущности с бизнес-логикой
- **Services**: Доменные сервисы с бизнес-правилами
- **Repositories**: Интерфейсы для доступа к данным

### 2. Application Layer (Прикладной слой)
- **DTOs**: Объекты для передачи данных между слоями
- **Use Cases**: Сценарии использования приложения
- **Services**: Координация между доменом и инфраструктурой

### 3. Infrastructure Layer (Инфраструктурный слой)
- **Config**: Конфигурации Spring Boot
- **Repositories**: Реализации доступа к данным
- **Services**: Реализации доменных сервисов
- **Filters**: Фильтры для обработки запросов

### 4. Presentation Layer (Презентационный слой)
- **Controllers**: HTTP контроллеры
- **Exceptions**: Обработчики исключений

## Принципы

### SOLID принципы
- **S** - Single Responsibility: Каждый класс имеет одну ответственность
- **O** - Open/Closed: Классы открыты для расширения, закрыты для изменения
- **L** - Liskov Substitution: Подтипы заменяют базовые типы
- **I** - Interface Segregation: Интерфейсы специфичны для клиентов
- **D** - Dependency Inversion: Зависимости от абстракций, не от конкретных классов

### Clean Architecture
- **Independence of Frameworks**: Архитектура не зависит от фреймворков
- **Testability**: Легко тестировать каждый слой
- **Independence of UI**: UI можно легко заменить
- **Independence of Database**: База данных - деталь реализации
- **Independence of any external agency**: Внешние агентства - детали

## Конфигурация

### Environment Variables
- `SERVER_PORT`: Порт сервера (по умолчанию: 8080)
- `SPRING_PROFILES_ACTIVE`: Активный профиль (по умолчанию: dev)
- `GATEWAY_SECURITY_ENABLED`: Включение безопасности (по умолчанию: false)
- `REDIS_HOST`: Хост Redis для rate limiting
- `REDIS_PORT`: Порт Redis (по умолчанию: 6379)

### Profiles
- **dev**: Разработка
- **prod**: Продакшн
- **test**: Тестирование

## Тестирование

### Структура тестов
```
src/test/java/com/smartjarvis/gateway/
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

# Форматирование кода
mvn spotless:apply
```

## Линтинг и форматирование

### Checkstyle
- Конфигурация: `checkstyle.xml`
- Проверка: `mvn checkstyle:check`

### Spotless
- Форматирование: `mvn spotless:apply`
- Проверка: `mvn spotless:check`

## Сборка и запуск

```bash
# Сборка
mvn clean compile

# Запуск тестов
mvn test

# Сборка JAR
mvn clean package

# Запуск
java -jar target/gateway-1.0.0.jar
```

## Мониторинг

### Endpoints
- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`
- Gateway Routes: `/actuator/gateway/routes`

### Логирование
- Уровень: `INFO` (настраивается через `LOG_LEVEL`)
- Формат: `%d{yyyy-MM-dd HH:mm:ss} - %msg%n` 