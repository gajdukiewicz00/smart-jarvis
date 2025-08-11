# Dialog Manager Service

Сервис управления диалогом с FSM.

## Возможности

- FSM: idle → listening → understanding → speaking
- Orchestration NLG → TTS
- Управление сессиями
- DLQ для ошибок обработки

## Endpoints

- `/healthz` - проверка здоровья
- `/readyz` - проверка Kafka + Schema Registry

## Kafka

- **Consumer**: `sj.nlu.intent.v1`
- **Producer**: `sj.nlg.reply.v1`, `sj.dm.dlq.v1`

## FSM States

- **idle**: ожидание команды
- **listening**: прослушивание аудио
- **understanding**: обработка intent
- **speaking**: генерация ответа

## Запуск

```bash
npm install
npm run build
npm start
```
