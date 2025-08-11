# Voice Gateway Service

WebSocket сервер для голосового взаимодействия с SmartJARVIS.

## Протокол

- **Первый кадр**: JSON header с `{sessionId, correlationId, sampleRate, protocolVersion}`
- **Последующие кадры**: Raw PCM16 mono 16kHz, размер 20-40ms
- **Heartbeat**: ping каждые 5 секунд
- **Backpressure**: bounded queue (50 кадров), drop oldest при переполнении

## Endpoints

- `/healthz` - быстрая проверка здоровья
- `/readyz` - проверка Kafka + Schema Registry
- `ws://:7090/ws/audio` - WebSocket для аудио

## Kafka

- **Producer**: `sj.audio.raw.v1` (ключ: sessionId)
- **Consumer**: `sj.tts.audio.v1` (фильтр по sessionId)

## Запуск

```bash
npm install
npm run build
npm start
```
