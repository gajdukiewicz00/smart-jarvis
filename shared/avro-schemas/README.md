# SmartJARVIS Avro Schemas

Этот каталог содержит Avro схемы для всех событий системы SmartJARVIS.

## Структура каталогов

```
shared/avro-schemas/
├── common/           # Общие типы и базовые схемы
│   ├── BaseEvent.avsc
│   └── AudioData.avsc
├── voice/            # Голосовые события
│   ├── AudioIncomingEvent.avsc
│   ├── TranscriptionCompletedEvent.avsc
│   ├── IntentRecognizedEvent.avsc
│   └── ResponseGeneratedEvent.avsc
├── todo/             # События задач
│   ├── TodoCreatedEvent.avsc
│   ├── TodoUpdatedEvent.avsc
│   └── TodoCompletedEvent.avsc
├── calendar/         # Календарные события
│   └── EventCreatedEvent.avsc
├── money/            # Финансовые события
│   └── TransactionCreatedEvent.avsc
├── memory/           # События памяти
│   └── ContextUpdatedEvent.avsc
├── device/           # События устройств
│   └── DeviceCommandExecutedEvent.avsc
├── home/             # События умного дома
│   └── DeviceControlledEvent.avsc
└── system/           # Системные события
    └── UserSessionStartedEvent.avsc
```

## Базовые типы

### BaseEvent
Базовое событие, от которого наследуются все остальные события:
- `eventId`: Уникальный идентификатор события
- `correlationId`: Идентификатор корреляции для трассировки
- `userId`: Идентификатор пользователя
- `sessionId`: Идентификатор сессии (опционально)
- `timestamp`: Временная метка события
- `eventType`: Тип события
- `version`: Версия схемы
- `metadata`: Дополнительные метаданные

### AudioData
Структура для аудио данных:
- `audioBytes`: Аудио данные в байтах
- `sampleRate`: Частота дискретизации
- `channels`: Количество каналов
- `bitDepth`: Разрядность аудио
- `duration`: Длительность в секундах
- `format`: Формат аудио данных

## Голосовые события

### AudioIncomingEvent
Событие входящего аудио:
- Содержит аудио данные и метаданные
- Поддерживает потоковую передачу через `chunkIndex`
- Указывает источник аудио (микрофон, файл, поток)

### TranscriptionCompletedEvent
Событие завершения транскрипции:
- Содержит транскрибированный текст
- Включает уверенность в результате
- Указывает использованную модель STT

### IntentRecognizedEvent
Событие распознавания намерения:
- Содержит распознанное намерение
- Включает извлеченные сущности
- Указывает уверенность в распознавании

### ResponseGeneratedEvent
Событие генерации ответа:
- Содержит текст ответа
- Включает список действий для выполнения
- Указывает тип ответа

## Доменные события

### TodoCreatedEvent
Событие создания задачи:
- Содержит все данные задачи
- Поддерживает приоритеты и категории
- Включает поддержку повторяющихся задач

### EventCreatedEvent
Событие создания календарного события:
- Содержит время начала и окончания
- Включает участников и напоминания
- Поддерживает целодневные события

### TransactionCreatedEvent
Событие создания финансовой транзакции:
- Содержит сумму и валюту
- Включает категорию и описание
- Поддерживает повторяющиеся транзакции

## Системные события

### UserSessionStartedEvent
Событие начала пользовательской сессии:
- Содержит информацию о клиенте
- Включает доступные функции
- Указывает тип сессии

### DeviceCommandExecutedEvent
Событие выполнения команды устройства:
- Содержит тип команды и данные
- Включает статус выполнения
- Указывает время выполнения

## Регистрация схем

Для регистрации схем в Schema Registry используйте:

```bash
./infrastructure/kafka/register-schemas.sh
```

## Версионирование

Все схемы поддерживают версионирование через поле `version` в `BaseEvent`. При изменении схем:

1. Обновите версию схемы
2. Обеспечьте обратную совместимость
3. Зарегистрируйте новую версию в Schema Registry

## Использование в коде

### Java
```java
// Producer
KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props);
GenericRecord event = new GenericData.Record(schema);
producer.send(new ProducerRecord<>("topic", event));

// Consumer
KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("topic"));
```

### Python
```python
from confluent_kafka import Producer, Consumer
from confluent_kafka.schema_registry import SchemaRegistryClient

# Producer
producer = Producer({'bootstrap.servers': 'localhost:9092'})
producer.produce('topic', value=event_data)

# Consumer
consumer = Consumer({'bootstrap.servers': 'localhost:9092'})
consumer.subscribe(['topic'])
```

## Мониторинг

Схемы можно мониторить через:
- Schema Registry UI: http://localhost:8081
- Kafka UI: http://localhost:8080 (если настроен)
- Prometheus метрики Schema Registry
