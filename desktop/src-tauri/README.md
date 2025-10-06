# SmartJARVIS Desktop Application

## Обзор

SmartJARVIS Desktop - это полнофункциональное десктопное приложение голосового ассистента, построенное на базе Tauri с Rust бэкендом. Приложение интегрирует локальную обработку речи с облачными микросервисами для создания мощного голосового интерфейса.

## Архитектура

### Локальные компоненты (интегрировано из Priler/jarvis)
- **Wake Word Detection**: Автоматическое распознавание фраз активации ("Джарвис")
- **Speech-to-Text (STT)**: Локальное распознавание речи с использованием Vosk
- **Командная система**: YAML-конфигурируемая система команд
- **Аудио система**: Захват, обработка и воспроизведение аудио
- **Трей меню**: Системный трей для управления приложением

### Облачные микросервисы
- **Voice Gateway**: WebSocket шлюз для аудио потоков
- **NLU Service**: Распознавание намерений пользователя
- **Dialog Manager**: Управление диалоговыми состояниями
- **TTS Service**: Синтез речи
- **Domain Services**: Специализированные сервисы (Todo, Money, Calendar, etc.)

## Возможности

### Голосовое управление
- **Wake Word Detection**: Автоматическая активация по фразам
- **Локальное STT**: Распознавание речи без интернета (Vosk)
- **Командная система**: YAML-конфигурируемые команды
- **Barge-in support**: Прерывание речи ассистента

### Аудио обработка
- **Многоканальная запись**: Поддержка различных аудио устройств
- **VAD (Voice Activity Detection)**: Автоматическое обнаружение речи
- **Аудио визуализация**: Реал-тайм отображение уровня громкости
- **Форматы аудио**: Поддержка PCM 16kHz, моно/стерео

### Системная интеграция
- **PC Control**: Управление файлами, процессами, системными настройками
- **Трей меню**: Минималистичный интерфейс в системном трее
- **Горячие клавиши**: Глобальные сочетания клавиш
- **Автозапуск**: Автоматический запуск при старте системы

## Установка и настройка

### Предварительные требования
```bash
# Rust toolchain
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Node.js (для Tauri)
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs

# Системные зависимости (Linux)
sudo apt-get install libgtk-3-dev libwebkit2gtk-4.1-dev libappindicator3-dev librsvg2-dev patchelf
```

### Сборка и запуск
```bash
# Клонирование проекта
git clone https://github.com/smartjarvis/smart-jarvis.git
cd smart-jarvis/desktop

# Установка зависимостей
npm install

# Разработка
npm run tauri dev

# Сборка для продакшена
npm run tauri build
```

## Конфигурация

### Модели и ресурсы
Приложение использует локальные модели для оффлайн работы:

```
desktop/src-tauri/
├── models/
│   ├── vosk/           # Модели Vosk STT
│   └── rustpotter/     # Модели wake word detection
├── commands/           # YAML конфигурации команд
├── sound/              # Звуковые файлы ассистента
└── icons/              # Иконки приложения
```

### Команды
Команды настраиваются через YAML файлы в директории `commands/`:

```yaml
list:
- command:
    action: cli
    cli_cmd: xdg-open
    cli_args:
    - http://google.com
  voice:
    sounds:
    - ok1
    - ok2
  phrases:
  - открой гугл
  - запусти браузер
```

## API команды Tauri

### Wake Word Detection
```rust
// Инициализация wake word системы
init_stt_system() -> Result<String, String>

// Запуск/остановка прослушки
start_wake_word_detection() -> Result<String, String>
stop_wake_word_detection() -> Result<String, String>

// Получение статуса
get_wake_word_status() -> Result<String, String>
```

### Командная система
```rust
// Парсинг команд из YAML файлов
parse_commands() -> Result<String, String>

// Поиск подходящей команды
fetch_command(phrase: String, commands: Vec<AssistantCommand>) -> Result<String, String>

// Выполнение команды
execute_command(command_path: String, config: CommandConfig) -> Result<String, String>
```

### Аудио система
```rust
// Захват аудио
start_audio_capture(sample_rate: u32, channels: u16, device_name: Option<String>) -> Result<String, String>
stop_audio_capture() -> Result<String, String>

// Воспроизведение аудио
start_audio_playback(sample_rate: u32, channels: u16, device_name: Option<String>) -> Result<String, String>
stop_audio_playback() -> Result<String, String>
```

### STT система
```rust
// Распознавание речи
recognize_speech(audio_data: Vec<i16>, partial: bool) -> Result<String, String>
```

## Интеграция с микросервисами

### Архитектура взаимодействия
```
┌─────────────────┐    ┌─────────────────┐
│   Tauri App     │    │  Flutter App    │
│   (Desktop)     │    │   (Mobile)      │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          │ HTTP/gRPC            │ HTTP/gRPC
          │                      │
┌─────────▼───────┐    ┌─────────▼───────┐
│  Java Services  │    │  Java Services  │
│  (Local Server)│    │  (Local Server)│
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          │ Kafka                 │ Kafka
          │                      │
┌─────────▼───────┐    ┌─────────▼───────┐
│  PC Integration │    │  PC Integration │
│  (Rust in Tauri)│   │  (Java wrapper) │
└─────────────────┘    └─────────────────┘
```

### Поток обработки команды
1. **Wake Word Detection** → Активация прослушки
2. **Audio Capture** → Захват аудио данных
3. **STT Processing** → Локальное распознавание речи
4. **Command Matching** → Поиск подходящей команды
5. **Intent Recognition** → Отправка в NLU сервис
6. **Action Execution** → Выполнение через микросервисы
7. **TTS Response** → Синтез ответа
8. **Audio Playback** → Воспроизведение ответа

## Расширение функциональности

### Добавление новых команд
1. Создайте YAML файл в `commands/[название]/command.yaml`
2. Определите действия (cli, exe, voice)
3. Укажите фразы активации
4. Настройте звуковые ответы

### Добавление моделей
- **Vosk модели**: Разместите в `models/vosk/`
- **Wake word модели**: Разместите в `models/rustpotter/`
- **Звуковые файлы**: Разместите в `sound/[голос]/`

## Отладка и логирование

### Логи приложения
```bash
# Просмотр логов в реальном времени
tail -f ~/.config/com.smartjarvis.desktop/log.txt

# Логи Tauri разработки
npm run tauri dev -- --no-bundle
```

### Диагностика аудио
```rust
// Проверка аудио устройств
probe_cpal_devices() -> Result<String, String>

// Тестирование записи
start_recording() -> Result<String, String>
stop_recording() -> Result<String, String>
```

## Безопасность

- **Локальная обработка**: Критичные данные не покидают устройство
- **Шифрование**: Все коммуникации с сервисами зашифрованы
- **Аутентификация**: JWT токены для доступа к сервисам
- **PII фильтрация**: Автоматическое удаление персональных данных из логов

## Производительность

- **Оптимизация памяти**: Эффективное управление аудио буферами
- **Потоковая обработка**: Минимальная задержка при обработке аудио
- **Кэширование**: Локальное кэширование моделей и ресурсов
- **Ресурсо-ограничения**: Контроль использования CPU/памяти

## Лицензия

Этот проект является проприетарным. Все права принадлежат команде SmartJARVIS.
