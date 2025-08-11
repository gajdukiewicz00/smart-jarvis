# TTS Service

Сервис синтеза речи (готов для Piper TTS).

## Возможности

- Mock TTS (готов для Piper интеграции)
- Кэширование коротких фраз
- Поддержка SSML
- DLQ для ошибок обработки

## Endpoints

- `/healthz` - проверка здоровья
- `/readyz` - проверка Kafka + TTS backend
- `/synthesize` - синтез речи (POST)

## Kafka

- **Consumer**: `sj.tts.request.v1`
- **Producer**: `sj.tts.audio.v1`, `sj.tts.dlq.v1`

## Конфигурация

- `TTS_BACKEND=piper` (по умолчанию)
- `TTS_VOICE=en_US-amy-medium`

## Запуск

```bash
pip install -r requirements.txt
python src/main.py
```
