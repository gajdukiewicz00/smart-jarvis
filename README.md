# SmartJARVIS

> Персональный «киношный» ассистент, который слышит, понимает и действует локально и приватно.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)

## ✨ Особенности

- **🎭 Киношный UX**: barge-in, wake-word, «личность» (persona), 3D-HUD
- **🤖 Agentic Actions**: управляет ПК и умным домом, а не просто отвечает
- **🏠 Локально и быстро**: Whisper CT2 + Piper/Edge-TTS, всё работает офлайн
- **🔧 Открытая архитектура**: микросервисы, плагины/skills, стабильные Avro-контракты
- **🔒 Приватность**: ваши данные не покидают ваш компьютер

## 🏗️ Архитектура

SmartJARVIS построен на принципах **полной микросервисности**:

```
[User] → [Web/Mobile] → [Voice Gateway] → [Kafka] → [Services] → [Databases]
                ↓
        [3D HUD] ← [WebSocket] ← [Event Stream]
```

### Микросервисы

- **voice-gateway** - WebSocket соединения и аудио поток
- **stt-service** - Speech-to-Text (Whisper CT2)
- **nlu-service** - Natural Language Understanding
- **dm-service** - Dialog Management
- **tts-service** - Text-to-Speech (Piper)
- **todo-service** - Управление задачами
- **money-service** - Финансовый трекинг
- **calendar-service** - Управление календарем
- **memory-service** - Контекстная память
- **device-agent** - Управление ПК
- **home-bridge** - Интеграция с Home Assistant

## 🚀 Быстрый старт

### Требования

- **Java 21+**
- **Docker & Docker Compose**
- **Python 3.11+**
- **Node.js 18+**

### Установка

```bash
# Клонировать репозиторий
git clone https://github.com/your-username/smart-jarvis.git
cd smart-jarvis

# Поднять инфраструктуру
make up

# Проверить статус
make status

# Посмотреть логи
make logs
```

### Первые команды

```bash
# Через веб-интерфейс
open http://localhost:3000

# Голосовые команды
"Джарвис, добавь задачу позвонить маме завтра в 10"
"Сделай музыку тише до 15% в гостиной"
"Открой VS Code и github.com, затем сделай скриншот"
```

## 📁 Структура проекта

```
smart-jarvis/
├── services/              # Микросервисы
├── web/                   # React frontend
├── mobile/                # React Native app
├── shared/                # Общие библиотеки
│   ├── avro-schemas/      # Avro схемы
│   ├── java-common/       # Java утилиты
│   └── python-common/     # Python утилиты
├── infrastructure/        # Docker, K8s, мониторинг
├── docs/                  # Документация
└── scripts/               # Автоматизация
```

## 🛠️ Разработка

### Команды

```bash
# Разработка
make up          # Поднять все сервисы
make down        # Остановить все сервисы
make logs        # Посмотреть логи
make test        # Запустить тесты
make build       # Собрать все сервисы

# Мониторинг
make grafana     # Открыть Grafana
make prometheus  # Открыть Prometheus
make jaeger      # Открыть Jaeger
```

### Документация

- [📋 План разработки](docs/tasklist.md)
- [🔄 Процесс работы](docs/workflow.md)
- [🎯 Техническое видение](.cursor/rules/vision.mdc)
- [📏 Соглашения по разработке](.cursor/rules/conventions.mdc)

## 🤝 Участие в разработке

1. Форкните репозиторий
2. Создайте ветку для фичи (`git checkout -b feature/amazing-feature`)
3. Следуйте [соглашениям по разработке](.cursor/rules/conventions.mdc)
4. Сделайте коммит (`git commit -m 'feat: add amazing feature'`)
5. Отправьте в ветку (`git push origin feature/amazing-feature`)
6. Откройте Pull Request

## 📊 Мониторинг

- **Grafana**: http://localhost:3001
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686

## 🎯 Roadmap

- [x] **Итерация 0**: Подготовка проекта
- [ ] **Итерация 1**: Инфраструктура + Gateway
- [ ] **Итерация 2**: STT + NLU + DM + TTS
- [ ] **Итерация 3**: Todo Service
- [ ] **Итерация 4**: Device Agent
- [ ] **Итерация 5**: Home Bridge
- [ ] **Итерация 6**: Web UI + HUD
- [ ] **Итерация 7**: Barge-in
- [ ] **Итерация 8**: Демо-скрипт

## 📄 Лицензия

Этот проект лицензирован под MIT License - смотрите файл [LICENSE](LICENSE) для деталей.

## 🙏 Благодарности

- [Whisper](https://github.com/openai/whisper) - за отличный STT
- [Piper](https://github.com/rhasspy/piper) - за качественный TTS
- [Home Assistant](https://www.home-assistant.io/) - за платформу умного дома
- [Spring Boot](https://spring.io/projects/spring-boot) - за отличный фреймворк

---

**SmartJARVIS** - это не просто «ещё один ассистент», это **операционная система твоей повседневности** с UX уровня кино и реальными действиями в мире софта и «железа».