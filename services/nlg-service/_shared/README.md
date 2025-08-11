# Shared Utilities

Общие утилиты для всех сервисов SmartJARVIS.

## Kafka

- `producer.ts` - Kafka producer с Schema Registry
- `consumer.ts` - Kafka consumer с error handling

## OpenTelemetry

- `tracer.ts` - инициализация трейсера
- `metrics.ts` - инициализация метрик

## Использование

```typescript
import { KafkaProducer } from "../_shared/kafka/producer";
import { initTracer } from "../_shared/otel/tracer";
```
