# Development Guide - WebSocket Audio Tester

Руководство для разработчиков по работе с WebSocket Audio Tester.

## 🏗️ Архитектура

### Структура файлов
```
ws-audio-tester/
├── index.html          # Основной HTML файл с UI
├── README.md           # Документация для пользователей
├── DEVELOPMENT.md      # Это руководство для разработчиков
├── config-examples.md  # Примеры конфигурации
├── start.sh           # Скрипт запуска для Linux/Mac
└── start.bat          # Скрипт запуска для Windows
```

### Основные компоненты

#### 1. UI Layer (HTML/CSS)
- **Responsive Design**: Адаптивная сетка и компоненты
- **CSS Variables**: Легко настраиваемые цвета и размеры
- **Animations**: CSS анимации для улучшения UX

#### 2. JavaScript Core
- **WebSocket Manager**: Управление соединением и переподключением
- **Audio Processor**: Обработка аудио потока и PCM конвертация
- **Metrics Collector**: Сбор и отображение метрик
- **Logger**: Система логирования с цветовой кодировкой

#### 3. Audio Visualization
- **Real-time Bars**: 50 вертикальных баров для отображения аудио
- **PCM Processing**: Обработка Int16 PCM данных
- **Smooth Updates**: Плавные анимации обновления

## 🔧 Разработка

### Локальная разработка

1. **Клонируйте репозиторий**
```bash
git clone <repository-url>
cd smart-jarvis/clients/js/ws-audio-tester
```

2. **Запустите локальный сервер**
```bash
# Linux/Mac
./start.sh

# Windows
start.bat

# Или вручную
python3 -m http.server 8080
```

3. **Откройте в браузере**
```
http://localhost:8080
```

### Структура кода

#### Основные функции

```javascript
// WebSocket управление
function connectWebSocket()
function scheduleReconnect()
function handleWebSocketMessage()

// Аудио обработка
function startRecording()
function stopRecording()
function updateVisualizer()

// UI обновления
function updateStatus()
function updateMetric()
function log()
```

#### Переменные состояния

```javascript
let media, ws, chunks = [];           // Аудио и WebSocket
let sessionStartTime = 0;             // Время начала сессии
let chunkCount = 0;                   // Счетчик чанков
let reconnectAttempts = 0;            // Попытки переподключения
let maxRetries = 5;                   // Максимум попыток
let autoReconnect = true;             // Автопереподключение
let isRecording = false;              // Статус записи
let lastMessageTime = 0;              // Время последнего сообщения
let reconnectionCount = 0;            // Счетчик переподключений
```

## 🎨 Кастомизация UI

### CSS переменные

```css
:root {
    --primary-color: #007bff;
    --success-color: #28a745;
    --warning-color: #ffc107;
    --danger-color: #dc3545;
    --info-color: #17a2b8;
    
    --border-radius: 5px;
    --shadow: 0 2px 10px rgba(0,0,0,0.1);
    --transition: all 0.3s ease;
}
```

### Добавление новых метрик

1. **HTML элемент**
```html
<div class="metric">
    <div class="metric-value" id="newMetric">-</div>
    <div class="metric-label">New Metric</div>
</div>
```

2. **JavaScript обновление**
```javascript
function updateNewMetric(value) {
    document.getElementById('newMetric').textContent = value;
}
```

### Добавление новых кнопок

```html
<button id="newButton" class="control-btn btn-info">New Action</button>
```

```javascript
document.getElementById('newButton').onclick = () => {
    // Ваша логика
    log('New action executed', 'info');
};
```

## 🔌 WebSocket протокол

### Формат сообщений

#### Header (первое сообщение)
```json
{
    "sessionId": "uuid-string",
    "correlationId": "uuid-string", 
    "sampleRate": 16000,
    "protocolVersion": 1
}
```

#### Аудио данные
- **Формат**: PCM16 mono 16kHz
- **Размер чанка**: 1024 samples
- **Кодировка**: Int16Array

#### Ответы от сервера
- **Аудио**: WAV формат
- **JSON метаданные**: intent, confidence, etc.

### Обработка ошибок

```javascript
ws.onerror = (error) => {
    log(`WebSocket error: ${error}`, 'error');
    updateStatus('Connection Error', 'disconnected');
};

ws.onclose = (event) => {
    if (autoReconnect && reconnectAttempts < maxRetries && !event.wasClean) {
        scheduleReconnect();
    }
};
```

## 📊 Метрики и мониторинг

### Встроенные метрики

- **Roundtrip**: Время от записи до ответа
- **Chunks**: Количество отправленных аудио чанков
- **Intent**: Распознанный intent
- **Latency**: Задержка между сообщениями
- **Reconnections**: Количество переподключений

### Добавление новых метрик

```javascript
// В функции startRecording()
let newMetricStart = Date.now();

// В обработчике ответа
const newMetricValue = Date.now() - newMetricStart;
updateMetric('newMetric', newMetricValue);
```

## 🧪 Тестирование

### Unit тесты

Создайте файл `test.js`:

```javascript
// Mock WebSocket
class MockWebSocket {
    constructor(url) {
        this.url = url;
        this.readyState = WebSocket.CONNECTING;
        setTimeout(() => this.open(), 100);
    }
    
    open() {
        this.readyState = WebSocket.OPEN;
        this.onopen && this.onopen();
    }
    
    send(data) {
        // Mock отправки
        console.log('Mock send:', data);
    }
}

// Mock MediaDevices
navigator.mediaDevices.getUserMedia = () => {
    return Promise.resolve({
        getTracks: () => [{
            stop: () => console.log('Mock track stopped')
        }]
    });
};

// Запуск тестов
function runTests() {
    console.log('Running tests...');
    
    // Тест подключения
    connectWebSocket();
    
    // Тест записи
    setTimeout(() => {
        startRecording();
        setTimeout(() => stopRecording(), 1000);
    }, 200);
}
```

### Интеграционные тесты

```javascript
// Тест полного цикла
async function testFullCycle() {
    console.log('Testing full voice cycle...');
    
    // Подключение
    await connectWebSocket();
    
    // Запись
    await startRecording();
    
    // Симуляция речи
    await simulateSpeech();
    
    // Остановка
    await stopRecording();
    
    console.log('Full cycle test completed');
}

// Симуляция речи
function simulateSpeech() {
    return new Promise(resolve => {
        // Создаем тестовый аудио сигнал
        const audioContext = new AudioContext();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.setValueAtTime(440, audioContext.currentTime);
        gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);
        
        oscillator.start();
        setTimeout(() => {
            oscillator.stop();
            resolve();
        }, 2000);
    });
}
```

## 🚀 Производительность

### Оптимизации

1. **Debouncing обновлений UI**
```javascript
let updateTimeout;
function debouncedUpdate(metric, value) {
    clearTimeout(updateTimeout);
    updateTimeout = setTimeout(() => {
        updateMetric(metric, value);
    }, 100);
}
```

2. **Batch логирование**
```javascript
let logBuffer = [];
let logTimeout;

function bufferedLog(message, type = 'info') {
    logBuffer.push({ message, type, timestamp: Date.now() });
    
    if (!logTimeout) {
        logTimeout = setTimeout(() => {
            flushLogs();
        }, 1000);
    }
}

function flushLogs() {
    logBuffer.forEach(entry => {
        log(entry.message, entry.type);
    });
    logBuffer = [];
    logTimeout = null;
}
```

3. **Оптимизация визуализации**
```javascript
let animationFrame;
function optimizedVisualizer(audioData) {
    if (animationFrame) {
        cancelAnimationFrame(animationFrame);
    }
    
    animationFrame = requestAnimationFrame(() => {
        updateVisualizer(audioData);
    });
}
```

## 🔒 Безопасность

### Рекомендации

1. **Валидация входных данных**
```javascript
function validateHost(host) {
    const validHostPattern = /^[a-zA-Z0-9.-]+$/;
    return validHostPattern.test(host);
}

function validatePort(port) {
    const portNum = parseInt(port);
    return portNum >= 1 && portNum <= 65535;
}
```

2. **Ограничение размера сообщений**
```javascript
const MAX_MESSAGE_SIZE = 1024 * 1024; // 1MB

ws.onmessage = (e) => {
    if (e.data.byteLength > MAX_MESSAGE_SIZE) {
        log('Message too large, ignoring', 'warning');
        return;
    }
    // Обработка сообщения
};
```

3. **Rate limiting**
```javascript
let messageCount = 0;
let lastReset = Date.now();

function checkRateLimit() {
    const now = Date.now();
    if (now - lastReset > 60000) { // 1 минута
        messageCount = 0;
        lastReset = now;
    }
    
    if (messageCount > 1000) { // 1000 сообщений в минуту
        log('Rate limit exceeded', 'warning');
        return false;
    }
    
    messageCount++;
    return true;
}
```

## 📝 Логирование

### Уровни логирования

```javascript
const LOG_LEVELS = {
    DEBUG: 0,
    INFO: 1,
    WARNING: 2,
    ERROR: 3
};

let currentLogLevel = LOG_LEVELS.INFO;

function log(message, type = 'info', level = LOG_LEVELS.INFO) {
    if (level >= currentLogLevel) {
        const entry = document.createElement('div');
        entry.className = `log-entry log-${type}`;
        entry.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
        logs.appendChild(entry);
        logs.scrollTop = logs.scrollHeight;
    }
}
```

### Экспорт логов

```javascript
function exportLogs(format = 'txt') {
    const logEntries = Array.from(logs.children);
    
    if (format === 'json') {
        const logData = logEntries.map(entry => ({
            timestamp: entry.textContent.match(/\[(.*?)\]/)?.[1],
            message: entry.textContent.replace(/\[.*?\]\s*/, ''),
            type: entry.className.replace('log-entry log-', '')
        }));
        
        const blob = new Blob([JSON.stringify(logData, null, 2)], 
                             { type: 'application/json' });
        downloadBlob(blob, 'smartjarvis-logs.json');
    } else {
        const logText = logEntries.map(entry => entry.textContent).join('\n');
        const blob = new Blob([logText], { type: 'text/plain' });
        downloadBlob(blob, 'smartjarvis-logs.txt');
    }
}

function downloadBlob(blob, filename) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
}
```

## 🔄 Версионирование

### Semantic Versioning

- **MAJOR**: Несовместимые изменения API
- **MINOR**: Новые функции, обратная совместимость
- **PATCH**: Исправления багов

### Changelog

```markdown
# Changelog

## [2.0.0] - 2024-01-XX
### Added
- Настройки соединения
- Автоматическое переподключение
- Визуализация аудио
- Расширенные метрики

### Changed
- Разделение подключения и записи
- Улучшенный UI/UX

### Removed
- Автоматическое подключение при записи

## [1.0.0] - 2024-01-XX
### Added
- Базовый WebSocket клиент
- Запись и воспроизведение аудио
- Базовые метрики
```

## 🤝 Вклад в разработку

### Guidelines

1. **Код стиль**
   - Используйте ES6+ синтаксис
   - Добавляйте JSDoc комментарии
   - Следуйте принципам DRY и SOLID

2. **Тестирование**
   - Покрывайте новые функции тестами
   - Тестируйте edge cases
   - Проверяйте кроссбраузерную совместимость

3. **Документация**
   - Обновляйте README.md при изменениях
   - Добавляйте примеры использования
   - Документируйте API изменения

### Pull Request процесс

1. Создайте feature branch
2. Добавьте тесты для новых функций
3. Обновите документацию
4. Проверьте линтер и тесты
5. Создайте PR с описанием изменений

## 📚 Ресурсы

### WebSocket
- [MDN WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
- [WebSocket Protocol](https://tools.ietf.org/html/rfc6455)

### Web Audio API
- [MDN Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API)
- [Audio Processing](https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Basic_concepts_behind_Web_Audio_API)

### MediaDevices
- [MDN MediaDevices](https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices)
- [getUserMedia](https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia) 