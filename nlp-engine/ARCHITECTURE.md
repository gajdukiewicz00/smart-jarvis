# NLP Engine - Clean Architecture

## Обзор архитектуры

NLP Engine построен на принципах Clean Architecture и Domain-Driven Design (DDD), обеспечивая четкое разделение ответственности и высокую тестируемость.

## Слои архитектуры

### 1. Domain Layer (Доменный слой)
**Расположение**: `src/domain/`

Содержит бизнес-логику и доменные модели:
- **Entities**: Доменные сущности (Intent, NLPRequest, NLPResponse)
- **Value Objects**: Объекты-значения (IntentType, ConfidenceLevel, ProcessingStatus)
- **Domain Services**: Доменные сервисы (IntentRecognitionService)
- **Repositories**: Интерфейсы репозиториев (NLPRepository)

### 2. Application Layer (Слой приложения)
**Расположение**: `src/application/`

Содержит сценарии использования и координацию доменных объектов:
- **Use Cases**: Сценарии использования (ProcessIntentUseCase, ExecuteActionUseCase)
- **DTOs**: Объекты передачи данных (NLPRequestDto, NLPResponseDto)
- **Interfaces**: Интерфейсы для внешних сервисов

### 3. Infrastructure Layer (Инфраструктурный слой)
**Расположение**: `src/infrastructure/`

Содержит внешние зависимости и технические детали:
- **Repositories**: Реализации репозиториев (InMemoryNLPRepository)
- **Services**: Внешние сервисы (TaskServiceClient, IntentProcessor)
- **Configuration**: Конфигурация приложения

### 4. Presentation Layer (Слой представления)
**Расположение**: `src/presentation/`

Содержит интерфейсы для внешних систем:
- **Controllers**: REST API контроллеры (NLPController)
- **Routes**: Маршрутизация запросов
- **Middleware**: Промежуточное ПО

## Доменные модели

### Intent (Намерение)
```typescript
interface Intent {
  id: string;
  type: IntentType;
  confidence: number;
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
  execute?: boolean;
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
  status: ProcessingStatus;
  processingTime: number;
  createdAt: Date;
}
```

## Принципы проектирования

### 1. Dependency Inversion Principle
- Доменный слой не зависит от внешних слоев
- Интерфейсы репозиториев определены в доменном слое
- Реализации находятся в инфраструктурном слое

### 2. Single Responsibility Principle
- Каждый класс имеет одну ответственность
- Use Cases содержат только бизнес-логику
- Controllers отвечают только за HTTP взаимодействие

### 3. Open/Closed Principle
- Система открыта для расширения (новые интенты)
- Закрыта для модификации (существующая логика)

### 4. Interface Segregation Principle
- Интерфейсы разделены по функциональности
- Клиенты не зависят от неиспользуемых методов

## Поток данных

1. **HTTP Request** → Presentation Layer (Controller)
2. **Controller** → Application Layer (Use Case)
3. **Use Case** → Domain Layer (Domain Service)
4. **Domain Service** → Infrastructure Layer (Repository/External Service)
5. **Response** → Application Layer → Presentation Layer → HTTP Response

## Тестирование

### Unit Tests
- Доменные сущности и сервисы
- Use Cases
- Репозитории (с мок-данными)

### Integration Tests
- End-to-end тестирование API
- Интеграция с внешними сервисами
- Тестирование полного потока обработки

## Статус реализации

- [x] Domain Layer - Entities, Value Objects, Domain Services
- [x] Application Layer - Use Cases, DTOs
- [x] Infrastructure Layer - Repositories, External Services
- [x] Presentation Layer - Controllers, Routes
- [x] Unit Tests
- [x] Integration Tests
- [x] Documentation

## Структура проекта

```
src/
├── domain/
│   ├── entities/
│   │   ├── Intent.ts
│   │   ├── NLPRequest.ts
│   │   └── NLPResponse.ts
│   ├── value-objects/
│   │   ├── IntentType.ts
│   │   ├── ConfidenceLevel.ts
│   │   └── ProcessingStatus.ts
│   ├── services/
│   │   └── IntentRecognitionService.ts
│   └── repositories/
│       └── NLPRepository.ts
├── application/
│   ├── usecases/
│   │   ├── ProcessIntentUseCase.ts
│   │   └── ExecuteActionUseCase.ts
│   └── dto/
│       ├── NLPRequestDto.ts
│       └── NLPResponseDto.ts
├── infrastructure/
│   ├── repositories/
│   │   └── InMemoryNLPRepository.ts
│   ├── services/
│   │   ├── TaskServiceClient.ts
│   │   └── IntentProcessor.ts
│   └── config/
│       └── AppConfig.ts
├── presentation/
│   ├── controllers/
│   │   └── NLPController.ts
│   └── routes/
│       └── nlpRoutes.ts
└── index.ts
``` 