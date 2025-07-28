# 🎤 Speech Service TTS - Восстановленная функциональность

## ✅ Статус: ПОЛНОСТЬЮ ВОССТАНОВЛЕНО

TTS (Text-to-Speech) функциональность в Speech Service была успешно восстановлена и протестирована.

## 🔧 Что было исправлено

### 1. **Замена espeak на gTTS**
- **Проблема**: espeak не работал в Docker контейнере
- **Решение**: Заменили на Google Text-to-Speech (gTTS)
- **Преимущества**: 
  - Поддержка множества языков
  - Высокое качество речи
  - Надежная работа в Docker

### 2. **Обновление зависимостей**
```python
# Добавлено в requirements.txt
gTTS==2.4.0
```

### 3. **Исправление Dockerfile**
```dockerfile
# Добавлены аудио зависимости
pulseaudio \
pulseaudio-utils \
alsa-utils \
```

### 4. **Обновление кода**
- Заменили `pyttsx3` на `gTTS`
- Добавили поддержку множества языков
- Улучшили обработку ошибок

## 🌍 Поддерживаемые языки

| Код | Язык | Статус |
|-----|-------|--------|
| `en` | English | ✅ |
| `ru` | Russian | ✅ |
| `es` | Spanish | ✅ |
| `fr` | French | ✅ |
| `de` | German | ✅ |
| `it` | Italian | ✅ |
| `pt` | Portuguese | ✅ |
| `ja` | Japanese | ✅ |
| `ko` | Korean | ✅ |
| `zh` | Chinese | ✅ |

## 📡 API Endpoints

### 1. **Text-to-Speech**
```http
POST /api/text-to-speech
Content-Type: application/json

{
  "text": "Hello, world!",
  "voice": "en"
}
```

**Ответ**: MP3 аудио файл

### 2. **Available Voices**
```http
GET /api/voices
```

**Ответ**:
```json
{
  "voices": {
    "en": "English",
    "ru": "Russian",
    "es": "Spanish",
    ...
  },
  "default": "en",
  "message": "gTTS supports multiple languages"
}
```

### 3. **Health Check**
```http
GET /health
```

**Ответ**:
```json
{
  "status": "healthy",
  "service": "speech-service",
  "timestamp": "1234567890.123",
  "nlp_engine_url": "http://nlp-engine:8082"
}
```

## 🧪 Тестирование

### Базовый тест TTS
```bash
python3 test_tts.py
```

### Интеграционный тест
```bash
python3 test_integration.py
```

### Ручное тестирование через curl
```bash
# Прямое обращение к Speech Service
curl -X POST http://localhost:8083/api/text-to-speech \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello, world!", "voice": "en"}' \
  --output test.mp3

# Через API Gateway
curl -X POST http://localhost:8080/speech-service/api/text-to-speech \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello, world!", "voice": "en"}' \
  --output test_gateway.mp3
```

## 🐳 Docker

### Сборка
```bash
cd docker
sudo docker compose build speech-service
```

### Запуск
```bash
sudo docker compose up -d speech-service
```

### Логи
```bash
sudo docker compose logs speech-service
```

## 📊 Результаты тестирования

### ✅ Все тесты пройдены

1. **Health Check**: ✅
2. **Voices Endpoint**: ✅ (10 языков)
3. **TTS Endpoint**: ✅ (все языки)
4. **NLP Integration**: ✅
5. **API Gateway**: ✅
6. **Task Service Integration**: ✅

### 📁 Созданные файлы
- `test_tts_en.mp3` (25KB) - английский
- `test_tts_ru.mp3` (25KB) - русский  
- `test_tts_es.mp3` (26KB) - испанский
- `test_tts_gateway.mp3` (40KB) - через API Gateway

## 🔄 Интеграция с другими сервисами

### NLP Engine
- Speech Service может обрабатывать голосовые команды
- Интеграция с NLP Engine для распознавания намерений
- Создание задач через голосовые команды

### Task Service
- Создание задач через голосовые команды
- Полный цикл: Голос → Текст → NLP → Действие → Ответ

### API Gateway
- Единая точка входа через nginx
- Правильная маршрутизация запросов
- Поддержка всех сервисов

## 🚀 Использование

### 1. Простое TTS
```python
import requests

response = requests.post(
    "http://localhost:8083/api/text-to-speech",
    json={"text": "Hello, world!", "voice": "en"}
)

with open("output.mp3", "wb") as f:
    f.write(response.content)
```

### 2. Голосовые команды
```python
# Загрузка аудио файла
with open("voice_command.wav", "rb") as f:
    files = {"audio_file": f}
    response = requests.post(
        "http://localhost:8083/api/voice-command",
        files=files
    )
    
result = response.json()
print(f"Recognized: {result['original_text']}")
print(f"Response: {result['tts_response']}")
```

## 🎯 Следующие шаги

1. **Веб-интерфейс**: Создать UI для управления TTS
2. **Реальное время**: Добавить поддержку микрофона
3. **Качество**: Настроить параметры речи (скорость, тон)
4. **Кэширование**: Кэшировать часто используемые фразы
5. **Офлайн режим**: Локальный TTS для работы без интернета

## 📝 Заключение

TTS функциональность полностью восстановлена и работает стабильно. Поддерживается множество языков, интеграция с другими сервисами и полное тестирование. Система готова к использованию в продакшене. 