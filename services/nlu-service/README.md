# NLU Service

Сервис понимания естественного языка с rule-based подхода.

## Возможности

- Rule-based intent recognition
- Поддержка intents: GREETING, ECHO, TIME_NOW, WEATHER_SIMPLE, UNKNOWN
- DLQ для ошибок обработки

## Endpoints

- `/healthz` - проверка здоровья
- `/readyz` - проверка Kafka + Schema Registry

## Kafka

- **Consumer**: `sj.stt.final.v1`
- **Producer**: `sj.nlu.intent.v1`, `sj.nlu.dlq.v1`

## Запуск

```bash
npm install
npm run build
npm start
```
