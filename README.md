# SmartJARVIS - Intelligent Desktop Assistant

[![CI](https://github.com/gajdukiewicz00/smart-jarvis/workflows/CI/badge.svg)](https://github.com/gajdukiewicz00/smart-jarvis/actions)
[![CodeQL](https://github.com/gajdukiewicz00/smart-jarvis/workflows/CodeQL%20Analysis/badge.svg)](https://github.com/gajdukiewicz00/smart-jarvis/actions)
[![Security Scan](https://github.com/gajdukiewicz00/smart-jarvis/workflows/Security%20Scan/badge.svg)](https://github.com/gajdukiewicz00/smart-jarvis/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

SmartJARVIS - это интеллектуальный десктопный ассистент, построенный на микросервисной архитектуре с использованием Clean Architecture принципов.

## 🏗️ Архитектура

Проект следует принципам Clean Architecture и состоит из следующих микросервисов:

- **jarvis-desktop** - JavaFX десктопное приложение
- **task-service** - Spring Boot REST API для управления задачами
- **nlp-engine** - TypeScript/Node.js сервис для обработки естественного языка
- **speech-service** - Python FastAPI сервис для распознавания и синтеза речи
- **gateway** - API Gateway для маршрутизации запросов

## 🚀 Быстрый старт

### Предварительные требования

- **Java**: JDK 17+ (Temurin recommended)
- **Node.js**: 18+ (LTS recommended)  
- **Python**: 3.10+ (3.11 recommended)
- **Maven**: 3.8+
- **Docker**: Latest stable (опционально)

### Локальный запуск

#### 1. Клонирование и установка зависимостей
```bash
git clone https://github.com/gajdukiewicz00/smart-jarvis.git
cd smart-jarvis

# Java модули
mvn clean install -DskipTests

# Node.js сервисы  
cd nlp-engine && npm ci && cd ..

# Python сервисы
cd speech-service && pip install -r requirements.txt && cd ..
```

#### 2. Запуск тестов
```bash
# Все тесты
bash ./ci/run_all_tests.sh

# Индивидуальные сервисы
mvn test                    # Java
npm test                    # Node.js  
pytest                      # Python
```

#### 3. Сборка и запуск сервисов
```bash
# Java сервисы
mvn spring-boot:run -pl task-service
mvn javafx:run -pl jarvis-desktop

# Node.js сервисы
cd nlp-engine && npm start

# Python сервисы  
cd speech-service && python main.py
```

### Установка и запуск

1. **Клонирование репозитория**
```bash
git clone <repository-url>
cd smart-jarvis
```

2. **Сборка всех компонентов**
```bash
chmod +x scripts/build-all.sh
./scripts/build-all.sh
```

3. **Запуск всех сервисов**
```bash
chmod +x scripts/start.sh
./scripts/start.sh
```

### Альтернативный запуск через Docker

```bash
cd docker
docker-compose up -d
```

## 📁 Структура проекта

```
smart-jarvis/
├── jarvis-desktop/          # JavaFX десктопное приложение
│   ├── src/main/java/
│   │   ├── domain/          # Domain модели и интерфейсы
│   │   ├── application/     # Use Cases и сервисы
│   │   ├── infrastructure/  # Внешние сервисы и репозитории
│   │   └── presentation/    # JavaFX UI контроллеры
│   └── pom.xml
├── task-service/            # Spring Boot REST API
│   ├── src/main/java/
│   │   ├── domain/          # Domain модели
│   │   ├── application/     # Use Cases
│   │   ├── infrastructure/  # JPA репозитории
│   │   └── presentation/    # REST контроллеры
│   └── pom.xml
├── nlp-engine/              # TypeScript NLP сервис
│   ├── src/
│   │   ├── services/        # NLP сервисы
│   │   ├── intents/         # Обработчики интентов
│   │   └── models/          # Модели данных
│   ├── package.json
│   └── tsconfig.json
├── speech-service/          # Python speech сервис
│   ├── main.py
│   └── requirements.txt
├── docker/                  # Docker конфигурация
│   ├── docker-compose.yml
│   ├── Dockerfile.task
│   └── Dockerfile.nlp
├── scripts/                 # Скрипты сборки и запуска
│   ├── build-all.sh
│   └── start.sh
└── docs/                    # Документация
```

## 🔧 Конфигурация

### Порт-маппинг сервисов

| Сервис | Порт | Описание |
|--------|------|----------|
| Task Service | 8081 | REST API для задач |
| NLP Engine | 8082 | NLP обработка |
| Speech Service | 8083 | STT/TTS |
| Gateway | 8080 | API Gateway |
| Grafana | 3000 | Мониторинг |
| PostgreSQL | 5432 | База данных |

### Переменные окружения

Создайте файл `.env` в корне проекта:

```env
# Database
POSTGRES_DB=jarvis_db
POSTGRES_USER=jarvis_user
POSTGRES_PASSWORD=jarvis_password

# Services
TASK_SERVICE_PORT=8081
NLP_ENGINE_PORT=8082
SPEECH_SERVICE_PORT=8083

# NLP Engine
NODE_ENV=development
LOG_LEVEL=info

# Speech Service
WHISPER_MODEL=base
```

## 🧪 Тестирование

### Java модули
```bash
# Тестирование jarvis-desktop
cd jarvis-desktop
mvn test

# Тестирование task-service
cd task-service
mvn test
```

### TypeScript модули
```bash
# Тестирование nlp-engine
cd nlp-engine
npm test
```

### Python модули
```bash
# Тестирование speech-service
cd speech-service
source venv/bin/activate
pytest
```

## 📊 Мониторинг

### Grafana Dashboard
- URL: http://localhost:3000
- Логин: admin
- Пароль: admin

### Health Checks
- Task Service: http://localhost:8081/actuator/health
- NLP Engine: http://localhost:8082/health
- Speech Service: http://localhost:8083/health

## 🔍 Логирование

Логи сохраняются в директории `logs/`:
- `task-service.log` - логи Task Service
- `nlp-engine.log` - логи NLP Engine
- `speech-service.log` - логи Speech Service

## 🛠️ Разработка

### Добавление нового интента

1. Создайте новый обработчик в `nlp-engine/src/intents/`
2. Зарегистрируйте его в `IntentProcessor`
3. Добавьте соответствующий Use Case в `jarvis-desktop`

### Добавление новой команды

1. Создайте новый тип в `Command.CommandType`
2. Добавьте обработку в `JarvisCore.executeCommand()`
3. Создайте соответствующий Use Case

### Добавление нового сервиса

1. Создайте новый модуль
2. Добавьте Dockerfile
3. Обновите `docker-compose.yml`
4. Добавьте в скрипты сборки и запуска

## 📝 API Документация

### Task Service API

```bash
# Создать задачу
POST /api/v1/tasks
{
  "title": "Новая задача",
  "description": "Описание задачи",
  "priority": "HIGH",
  "dueDate": "2024-01-15T10:00:00"
}

# Получить все задачи
GET /api/v1/tasks

# Обновить задачу
PUT /api/v1/tasks/{id}

# Удалить задачу
DELETE /api/v1/tasks/{id}
```

### NLP Engine API

```bash
# Обработать интент
POST /api/process
{
  "text": "Создай задачу на завтра",
  "context": {}
}
```

### Speech Service API

```bash
# Преобразовать речь в текст
POST /api/speech-to-text
# (multipart/form-data с аудио файлом)

# Преобразовать текст в речь
POST /api/text-to-speech
{
  "text": "Привет, JARVIS!",
  "voice": "default",
  "rate": 150
}
```

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте feature ветку
3. Внесите изменения
4. Добавьте тесты
5. Создайте Pull Request

## 📄 Лицензия


## 🆘 Поддержка

- Создайте Issue для багов
- Используйте Discussions для вопросов
- Обращайтесь к документации в папке `docs/`

## 🚀 Roadmap

- [ ] Интеграция с календарем
- [ ] Поддержка голосовых команд
- [ ] Машинное обучение для улучшения NLP
- [ ] Мобильное приложение
- [ ] Интеграция с внешними API
- [ ] Расширенная аналитика 