#!/bin/bash

# SmartJARVIS Python Services Startup Script

PROJECT_ROOT="/home/kwaqa/IdeaProjects/smart-jarvis"
SERVICES_DIR="$PROJECT_ROOT/services"
LOG_DIR="$PROJECT_ROOT/logs"
PID_DIR="$PROJECT_ROOT/pids"

# Создаем директории для логов и PID файлов
mkdir -p "$LOG_DIR" "$PID_DIR"

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для запуска Python сервиса
start_python_service() {
    local service_name="$1"
    local service_dir="$2"
    local port="$3"
    
    echo -e "${BLUE}🐍 Запуск Python сервиса: $service_name${NC}"
    
    # Проверяем что сервис не запущен
    if [ -f "$PID_DIR/$service_name.pid" ]; then
        local pid=$(cat "$PID_DIR/$service_name.pid")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${YELLOW}⚠️ Сервис $service_name уже запущен (PID: $pid)${NC}"
            return 0
        else
            echo -e "${YELLOW}🧹 Удаляем устаревший PID файл для $service_name${NC}"
            rm -f "$PID_DIR/$service_name.pid"
        fi
    fi
    
    # Проверяем что порт свободен
    if lsof -i ":$port" > /dev/null 2>&1; then
        echo -e "${RED}❌ Порт $port уже занят для сервиса $service_name${NC}"
        return 1
    fi
    
    # Проверяем что виртуальное окружение существует
    if [ ! -d "$service_dir/venv" ]; then
        echo -e "${RED}❌ Виртуальное окружение не найдено: $service_dir/venv${NC}"
        return 1
    fi
    
    # Проверяем что main.py существует
    if [ ! -f "$service_dir/main.py" ]; then
        echo -e "${RED}❌ main.py не найден: $service_dir/main.py${NC}"
        return 1
    fi
    
    # Запускаем сервис
    echo -e "${GREEN}📦 Запуск $service_name на порту $port...${NC}"
    
    cd "$service_dir"
    nohup ./venv/bin/python main.py \
        > "$LOG_DIR/$service_name.out" 2>&1 &
    
    local pid=$!
    echo "$pid" > "$PID_DIR/$service_name.pid"
    
    # Ждем немного и проверяем что сервис запустился
    sleep 3
    if ps -p "$pid" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Сервис $service_name запущен успешно (PID: $pid)${NC}"
        echo -e "${BLUE}📋 Логи: $LOG_DIR/$service_name.out${NC}"
        echo -e "${BLUE}🌐 URL: http://localhost:$port${NC}"
        return 0
    else
        echo -e "${RED}❌ Ошибка запуска сервиса $service_name${NC}"
        echo -e "${RED}📋 Проверьте логи: $LOG_DIR/$service_name.out${NC}"
        rm -f "$PID_DIR/$service_name.pid"
        return 1
    fi
}

# Функция для остановки Python сервиса
stop_python_service() {
    local service_name="$1"
    
    if [ -f "$PID_DIR/$service_name.pid" ]; then
        local pid=$(cat "$PID_DIR/$service_name.pid")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${YELLOW}🛑 Остановка сервиса: $service_name (PID: $pid)${NC}"
            kill "$pid"
            sleep 2
            if ps -p "$pid" > /dev/null 2>&1; then
                echo -e "${RED}⚠️ Принудительная остановка сервиса $service_name${NC}"
                kill -9 "$pid"
            fi
            echo -e "${GREEN}✅ Сервис $service_name остановлен${NC}"
        else
            echo -e "${YELLOW}⚠️ Сервис $service_name не запущен${NC}"
        fi
        rm -f "$PID_DIR/$service_name.pid"
    else
        echo -e "${YELLOW}⚠️ PID файл не найден для сервиса $service_name${NC}"
    fi
}

# Функция для проверки статуса Python сервиса
check_python_service() {
    local service_name="$1"
    local port="$2"
    
    if [ -f "$PID_DIR/$service_name.pid" ]; then
        local pid=$(cat "$PID_DIR/$service_name.pid")
        if ps -p "$pid" > /dev/null 2>&1; then
            if curl -s "http://localhost:$port/health" > /dev/null 2>&1; then
                echo -e "${GREEN}✅ $service_name: Запущен и здоров (PID: $pid, Port: $port)${NC}"
            else
                echo -e "${YELLOW}⚠️ $service_name: Запущен но не отвечает (PID: $pid, Port: $port)${NC}"
            fi
        else
            echo -e "${RED}❌ $service_name: Не запущен (PID файл есть, но процесс нет)${NC}"
        fi
    else
        echo -e "${RED}❌ $service_name: Не запущен${NC}"
    fi
}

# Основная логика
case "$1" in
    "start")
        echo -e "${BLUE}🐍 Запуск всех Python сервисов SmartJARVIS...${NC}"
        
        # Запускаем Python сервисы
        start_python_service "stt-service" "$SERVICES_DIR/stt-service" 8089
        start_python_service "tts-service" "$SERVICES_DIR/tts-service" 8090
        
        echo -e "${GREEN}🎉 Все Python сервисы запущены!${NC}"
        ;;
        
    "stop")
        echo -e "${YELLOW}🛑 Остановка всех Python сервисов...${NC}"
        
        stop_python_service "tts-service"
        stop_python_service "stt-service"
        
        echo -e "${GREEN}✅ Все Python сервисы остановлены!${NC}"
        ;;
        
    "restart")
        echo -e "${BLUE}🔄 Перезапуск всех Python сервисов...${NC}"
        $0 stop
        sleep 3
        $0 start
        ;;
        
    "status")
        echo -e "${BLUE}📊 Статус Python сервисов:${NC}"
        
        check_python_service "stt-service" 8089
        check_python_service "tts-service" 8090
        ;;
        
    "logs")
        local service_name="$2"
        if [ -z "$service_name" ]; then
            echo -e "${YELLOW}📋 Доступные сервисы: stt-service, tts-service${NC}"
            echo -e "${BLUE}💡 Использование: $0 logs <service-name>${NC}"
            exit 1
        fi
        
        if [ -f "$LOG_DIR/$service_name.out" ]; then
            echo -e "${BLUE}📋 Логи сервиса $service_name:${NC}"
            tail -f "$LOG_DIR/$service_name.out"
        else
            echo -e "${RED}❌ Лог файл не найден: $LOG_DIR/$service_name.out${NC}"
        fi
        ;;
        
    *)
        echo -e "${BLUE}SmartJARVIS Python Services Manager${NC}"
        echo ""
        echo -e "${GREEN}Использование:${NC}"
        echo "  $0 start     - Запустить все Python сервисы"
        echo "  $0 stop      - Остановить все Python сервисы"
        echo "  $0 restart   - Перезапустить все Python сервисы"
        echo "  $0 status    - Показать статус всех сервисов"
        echo "  $0 logs <service> - Показать логи сервиса"
        echo ""
        echo -e "${YELLOW}Доступные сервисы:${NC}"
        echo "  stt-service  (порт 8088) - Speech-to-Text"
        echo "  tts-service  (порт 8089) - Text-to-Speech"
        echo ""
        echo -e "${BLUE}Логи сохраняются в: $LOG_DIR${NC}"
        echo -e "${BLUE}PID файлы в: $PID_DIR${NC}"
        ;;
esac
