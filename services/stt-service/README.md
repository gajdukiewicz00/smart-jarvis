# STT Service

Сервис распознавания речи на базе Vosk.

## Возможности

- Streaming STT с Vosk (CPU-оптимизированный)
- Partial результаты каждые ~300ms
- Final результаты при завершении utterance
- DLQ для ошибок обработки

## Endpoints

- `/healthz` - проверка здоровья
- `/readyz` - проверка Kafka + Vosk модель
- `/process_audio` - обработка аудио (POST)

## Kafka

- **Consumer**: `sj.audio.raw.v1`
- **Producer**: 
  - `sj.stt.partial.v1` (partial results)
  - `sj.stt.final.v1` (final results)
  - `sj.stt.dlq.v1` (dead letter queue)

## Конфигурация

- `STT_ENGINE=vosk` (по умолчанию)
- `STT_MODEL=vosk-model-small-en-us-0.15`
- `AUDIO_SAMPLE_RATE=16000`

## Запуск

```bash
pip install -r requirements.txt
python src/main.py
```
