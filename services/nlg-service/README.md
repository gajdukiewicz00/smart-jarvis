# NLG Service

Сервис генерации естественного языка и SSML.

## Возможности

- Response templates с вариативностью
- SSML генерация для TTS
- Поддержка placeholder замены
- DLQ для ошибок обработки

## Endpoints

- `/healthz` - проверка здоровья
- `/readyz` - проверка Kafka + Schema Registry

## Kafka

- **Consumer**: `sj.nlu.intent.v1`
- **Producer**: `sj.nlg.reply.v1`, `sj.nlg.dlq.v1`

## Запуск

```bash
npm install
npm run build
npm start
```
