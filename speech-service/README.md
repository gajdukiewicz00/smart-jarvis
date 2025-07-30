# SmartJARVIS Speech Service

Сервис обработки речи для системы SmartJARVIS, реализованный с использованием Clean Architecture.

## Архитектура

Проект следует принципам Clean Architecture и разделен на следующие слои:

### 🏗️ Domain Layer (Доменный слой)
- **Entities**: Доменные сущности (`SpeechRequest`, `SpeechResponse`)
- **Repositories**: Интерфейсы репозиториев (`SpeechRepository`)
- **Services**: Доменные сервисы (`SpeechService`)

### 📋 Application Layer (Слой приложения)
- **DTOs**: Объекты передачи данных (`SpeechToTextRequest`, `TextToSpeechRequest`, `VoiceCommandRequest`)
- **Use Cases**: Сценарии использования (`SpeechToTextUseCase`, `TextToSpeechUseCase`, `VoiceCommandUseCase`)

### 🏛️ Infrastructure Layer (Инфраструктурный слой)
- **Repositories**: Реализации репозиториев (`InMemorySpeechRepository`)
- **Services**: Реализации сервисов (`SpeechServiceImpl`)

### 🎯 Presentation Layer (Слой представления)
- **Controllers**: REST контроллеры (`SpeechController`)
- **Exceptions**: Обработка исключений

## Возможности

### 🎤 Speech-to-Text (STT)
- Преобразование речи в текст
- Поддержка различных языков
- Настройка порога уверенности
- Обработка различных аудио форматов

### 🔊 Text-to-Speech (TTS)
- Преобразование текста в речь
- Настройка голоса (скорость, громкость, тон)
- Поддержка различных языков
- Выбор голосов

### 🎯 Voice Commands
- Обработка голосовых команд
- Интеграция с NLP движком
- Комбинированная обработка (STT + NLP + TTS)

## API Endpoints

### Speech-to-Text
```
POST /api/speech-to-text
Content-Type: multipart/form-data

Parameters:
- audio_file: Audio file (required)
- language: Language code (default: "en")
- audio_format: Audio format (optional)
- confidence_threshold: Confidence threshold (default: 0.5)
```

### Text-to-Speech
```
POST /api/text-to-speech
Content-Type: application/json

{
  "text": "Hello world",
  "language": "en",
  "voice": "default",
  "rate": 150,
  "volume": 1.0,
  "pitch": 1.0
}
```

### Voice Commands
```
POST /api/voice-command
Content-Type: multipart/form-data

Parameters:
- audio_file: Audio file (required)
- language: Language code (default: "en")
- audio_format: Audio format (optional)
- nlp_engine_url: NLP engine URL (optional)
```

### Health Check
```
GET /api/health
```

### Available Voices
```
GET /api/voices
```

### Statistics
```
GET /api/statistics
GET /api/errors
GET /api/performance
```

## Установка и запуск

### Требования
- Python 3.8+
- pip

### Установка зависимостей
```bash
pip install -r requirements.txt
```

### Запуск сервиса
```bash
python main.py
```

### Запуск тестов
```bash
pytest tests/
```

## Структура проекта

```
speech-service/
├── src/
│   ├── domain/
│   │   ├── entities/
│   │   │   ├── speech_request.py
│   │   │   └── speech_response.py
│   │   ├── repositories/
│   │   │   └── speech_repository.py
│   │   └── services/
│   │       └── speech_service.py
│   ├── application/
│   │   ├── dto/
│   │   │   └── speech_dto.py
│   │   └── usecases/
│   │       ├── speech_to_text_usecase.py
│   │       ├── text_to_speech_usecase.py
│   │       └── voice_command_usecase.py
│   ├── infrastructure/
│   │   ├── repositories/
│   │   │   └── in_memory_speech_repository.py
│   │   └── services/
│   │       └── speech_service_impl.py
│   └── presentation/
│       └── controllers/
│           └── speech_controller.py
├── tests/
│   ├── unit/
│   │   └── test_speech_entities.py
│   └── integration/
│       └── test_speech_service_integration.py
├── main.py
├── requirements.txt
└── README.md
```

## Доменные сущности

### SpeechRequest
```python
@dataclass
class SpeechRequest:
    id: str
    speech_type: SpeechType
    content: str  # Text for TTS, audio data for STT
    language: str = "en"
    audio_format: Optional[AudioFormat] = None
    voice_settings: Optional[Dict[str, Any]] = None
    created_at: Optional[datetime] = None
    processed_at: Optional[datetime] = None
```

### SpeechResponse
```python
@dataclass
class SpeechResponse:
    id: str
    request_id: str
    status: ResponseStatus
    content: Union[str, bytes]
    confidence: Optional[float] = None
    language: str = "en"
    processing_time: Optional[float] = None
    error_message: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None
    created_at: Optional[datetime] = None
```

## Бизнес-логика

### Валидация
- Проверка аудио форматов
- Валидация текстового содержимого
- Проверка настроек голоса

### Обработка ошибок
- Graceful handling ошибок
- Детальная информация об ошибках
- Статистика ошибок

### Производительность
- Измерение времени обработки
- Метрики производительности
- Мониторинг состояния сервиса

## Тестирование

### Unit тесты
```bash
pytest tests/unit/
```

### Интеграционные тесты
```bash
pytest tests/integration/
```

### Покрытие кода
```bash
pytest --cov=src tests/
```

## Конфигурация

### Переменные окружения
- `SPEECH_SERVICE_PORT`: Порт сервиса (по умолчанию: 8083)
- `NLP_ENGINE_URL`: URL NLP движка
- `LOG_LEVEL`: Уровень логирования

### Настройки логирования
```python
import logging
logging.basicConfig(level=logging.INFO)
```

## Мониторинг

### Health Check
```bash
curl http://localhost:8083/api/health
```

### Статистика
```bash
curl http://localhost:8083/api/statistics
curl http://localhost:8083/api/errors
curl http://localhost:8083/api/performance
```

## Разработка

### Добавление новых функций
1. Создайте доменную сущность в `src/domain/entities/`
2. Добавьте методы в репозиторий в `src/domain/repositories/`
3. Реализуйте бизнес-логику в `src/domain/services/`
4. Создайте DTO в `src/application/dto/`
5. Реализуйте use case в `src/application/usecases/`
6. Добавьте контроллер в `src/presentation/controllers/`
7. Напишите тесты в `tests/`

### Принципы разработки
- **Single Responsibility Principle**: Каждый класс имеет одну ответственность
- **Dependency Inversion Principle**: Зависимости от абстракций, а не от конкретных реализаций
- **Clean Architecture**: Четкое разделение слоев
- **Domain-Driven Design**: Бизнес-логика в доменном слое

## Лицензия

MIT License 