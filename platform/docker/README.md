# Platform Docker Profiles

Profiles:
- core: kafka, schema-registry, redis, postgres, otel-collector, prometheus, grafana, loki
- voice: core + voice-gateway, stt-service, nlu-service, nlg-service, tts-service, dm-service
- full: voice + future adapters/tools

## Usage

```
cp env.sample .env

# Start core
docker compose -f compose.yml --profile core up -d

# Start voice
docker compose -f compose.yml --profile voice up -d

# Stop
docker compose -f compose.yml down
```

Only voice-gateway publishes port 7090 by default for browser testing. Other services are internal.
