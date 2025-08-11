# 🚀 Быстрый старт - WebSocket Audio Tester

Минимальные инструкции для запуска WebSocket Audio Tester за 5 минут.

## ⚡ Экспресс запуск

### 1. Запуск сервера
```bash
# Linux/Mac
./start.sh

# Windows
start.bat

# Или вручную
python3 -m http.server 8080
```

### 2. Открытие в браузере
```
http://localhost:8080
```

### 3. Настройка соединения
- **Host**: `localhost` (или IP вашего voice-gateway)
- **Port**: `7090` (стандартный порт voice-gateway)
- **Protocol**: `ws` (для локального тестирования)

### 4. Тестирование
1. Нажмите **🔌 Connect**
2. Нажмите **🎤 Start Recording**
3. Скажите: "hello jarvis"
4. Дождитесь ответа
5. Нажмите **⏹️ Stop Recording**

## 🎯 Минимальная конфигурация

### Для локального тестирования
```json
{
  "host": "localhost",
  "port": "7090",
  "protocol": "ws"
}
```

### Для Docker
```json
{
  "host": "localhost", 
  "port": "7090",
  "protocol": "ws"
}
```

## ⚠️ Частые проблемы

### "Failed to access microphone"
- Разрешите доступ к микрофону в браузере
- Проверьте, что микрофон подключен и работает

### "WebSocket connection failed"
- Убедитесь, что voice-gateway запущен
- Проверьте порт 7090
- Проверьте firewall настройки

### "Recording stopped unexpectedly"
- Проверьте логи на ошибки
- Убедитесь, что WebSocket соединение активно

## 🔧 Проверка voice-gateway

```bash
# Проверка порта
netstat -an | grep 7090

# Или
ss -tuln | grep 7090

# Проверка процесса
ps aux | grep voice-gateway
```

## 📱 Тестовые команды

- **"hello jarvis"** → GREETING
- **"echo hello world"** → ECHO
- **"what time is it"** → TIME_NOW
- **"how is the weather"** → WEATHER_SIMPLE

## 🎵 Проверка аудио

1. Нажмите **🔊 Test Audio** - должен прозвучать тон 440Hz
2. Проверьте, что динамики работают
3. Убедитесь, что громкость достаточная

## 📊 Что смотреть

### Статус соединения
- 🟢 **Connected** - все работает
- 🟡 **Connecting** - подключение...
- 🔴 **Disconnected** - нет соединения

### Метрики
- **Roundtrip**: время от записи до ответа
- **Chunks**: количество аудио чанков
- **Intent**: распознанная команда
- **Latency**: задержка между сообщениями

### Логи
- 🟢 **Success** - успешные операции
- 🟡 **Warning** - предупреждения
- 🔴 **Error** - ошибки
- 🔵 **Info** - информация

## 🚀 Следующие шаги

После успешного тестирования:

1. **Изучите README.md** - полная документация
2. **Проверьте config-examples.md** - примеры для разных сценариев
3. **Изучите DEVELOPMENT.md** - для разработчиков
4. **Настройте для продакшна** - используйте WSS и SSL

## 🆘 Нужна помощь?

1. Проверьте логи в интерфейсе
2. Убедитесь, что voice-gateway запущен
3. Проверьте настройки сети
4. Изучите полную документацию в README.md

---

**Удачи с тестированием SmartJARVIS! 🎙️✨** 