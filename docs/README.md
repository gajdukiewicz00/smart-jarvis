# SmartJARVIS - Документация

Добро пожаловать в документацию SmartJARVIS!

## 📋 Планирование и процессы

- [📋 План разработки](tasklist.md) - итерационный план разработки MVP
- [🔄 Процесс работы](workflow.md) - правила выполнения работ по тасклисту

## 🏗️ Архитектура и принципы

- [🎯 Техническое видение](../.cursor/rules/vision.mdc) - архитектура, технологии, принципы
- [📏 Соглашения по разработке](../.cursor/rules/conventions.mdc) - правила кодирования
- [💡 Идея проекта](../idea.md) - бизнес-видение и концепция

## 🛠️ Разработка

- [🚀 Быстрый старт](../README.md#-быстрый-старт) - установка и запуск
- [🏗️ Структура проекта](../README.md#-структура-проекта) - организация кода
- [🤝 Участие в разработке](../README.md#-участие-в-разработке) - как контрибьютить

## 🧪 Тестирование

Веб-инструменты тестирования удалены; используйте Desktop (Tauri) и сервисные интеграционные тесты.

## 📊 Мониторинг

- **Grafana**: http://localhost:3001 - дашборды и метрики
- **Prometheus**: http://localhost:9090 - сбор метрик
- **Jaeger**: http://localhost:16686 - распределенная трассировка

## 🔧 Команды

```bash
# Основные команды
make help      # Показать все доступные команды
make up        # Поднять все микросервисы
make down      # Остановить все микросервисы
make status    # Проверить статус сервисов
make logs      # Показать логи

# Разработка
make build     # Собрать все микросервисы
make test      # Запустить тесты
make clean     # Очистить артефакты

# Мониторинг
make grafana   # Открыть Grafana
make prometheus # Открыть Prometheus
make jaeger    # Открыть Jaeger
```

## 📚 Дополнительные ресурсы

- [Whisper](https://github.com/openai/whisper) - STT модель
- [Piper](https://github.com/rhasspy/piper) - TTS модель
- [Home Assistant](https://www.home-assistant.io/) - платформа умного дома
- [Spring Boot](https://spring.io/projects/spring-boot) - Java фреймворк
- [Apache Kafka](https://kafka.apache.org/) - брокер сообщений
