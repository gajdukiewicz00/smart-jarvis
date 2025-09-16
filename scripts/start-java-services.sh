#!/bin/bash

# SmartJARVIS Java Services Startup Script

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

# Функция для запуска сервиса
start_service() {
    local service_name="$1"
    local service_dir="$2"
    local jar_file="$3"
    local port="$4"
    
    echo -e "${BLUE}🚀 Запуск сервиса: $service_name${NC}"
    
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
    
    # Проверяем что JAR файл существует
    if [ ! -f "$jar_file" ]; then
        echo -e "${RED}❌ JAR файл не найден: $jar_file${NC}"
        return 1
    fi
    
    # Запускаем сервис
    echo -e "${GREEN}📦 Запуск $service_name на порту $port...${NC}"
    
    nohup java -jar \
        -Dspring.profiles.active=dev \
        -Dserver.port=$port \
        -Dlogging.file.name="$LOG_DIR/$service_name.log" \
        -Dlogging.level.root=INFO \
        -Dlogging.level.com.smartjarvis=DEBUG \
        "$jar_file" \
        > "$LOG_DIR/$service_name.out" 2>&1 &
    
    local pid=$!
    echo "$pid" > "$PID_DIR/$service_name.pid"
    
    # Ждем немного и проверяем что сервис запустился
    sleep 3
    if ps -p "$pid" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Сервис $service_name запущен успешно (PID: $pid)${NC}"
        echo -e "${BLUE}📋 Логи: $LOG_DIR/$service_name.log${NC}"
        echo -e "${BLUE}🌐 URL: http://localhost:$port${NC}"
        return 0
    else
        echo -e "${RED}❌ Ошибка запуска сервиса $service_name${NC}"
        echo -e "${RED}📋 Проверьте логи: $LOG_DIR/$service_name.log${NC}"
        rm -f "$PID_DIR/$service_name.pid"
        return 1
    fi
}

# Функция для остановки сервиса
stop_service() {
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

# Функция для проверки статуса сервиса
check_service() {
    local service_name="$1"
    local port="$2"
    
    if [ -f "$PID_DIR/$service_name.pid" ]; then
        local pid=$(cat "$PID_DIR/$service_name.pid")
        if ps -p "$pid" > /dev/null 2>&1; then
            if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
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
        echo -e "${BLUE}🚀 Запуск всех Java сервисов SmartJARVIS...${NC}"
        
        # Запускаем сервисы в правильном порядке
        start_service "voice-gateway" "$SERVICES_DIR/voice-gateway" "$SERVICES_DIR/voice-gateway/target/voice-gateway-1.0.0-SNAPSHOT.jar" 8080
        start_service "todo-service" "$SERVICES_DIR/todo-service" "$SERVICES_DIR/todo-service/target/todo-service-1.0.0-SNAPSHOT.jar" 8086
        start_service "calendar-service" "$SERVICES_DIR/calendar-service" "$SERVICES_DIR/calendar-service/target/calendar-service-1.0.0-SNAPSHOT.jar" 8082
        start_service "money-service" "$SERVICES_DIR/money-service" "$SERVICES_DIR/money-service/target/money-service-1.0.0-SNAPSHOT.jar" 8083
        start_service "nlu-service" "$SERVICES_DIR/nlu-service" "$SERVICES_DIR/nlu-service/target/nlu-service-1.0.0-SNAPSHOT.jar" 8084
        start_service "dm-service" "$SERVICES_DIR/dm-service" "$SERVICES_DIR/dm-service/target/dm-service-1.0.0-SNAPSHOT.jar" 8085
        start_service "device-agent" "$SERVICES_DIR/device-agent" "$SERVICES_DIR/device-agent/target/device-agent-1.0.0-SNAPSHOT.jar" 8087
        start_service "home-bridge" "$SERVICES_DIR/home-bridge" "$SERVICES_DIR/home-bridge/target/home-bridge-1.0.0-SNAPSHOT.jar" 8088
        
        echo -e "${GREEN}🎉 Все Java сервисы запущены!${NC}"
        ;;
        
    "stop")
        echo -e "${YELLOW}🛑 Остановка всех Java сервисов...${NC}"
        
        stop_service "home-bridge"
        stop_service "device-agent"
        stop_service "dm-service"
        stop_service "nlu-service"
        stop_service "money-service"
        stop_service "calendar-service"
        stop_service "todo-service"
        stop_service "voice-gateway"
        
        echo -e "${GREEN}✅ Все Java сервисы остановлены!${NC}"
        ;;
        
    "restart")
        echo -e "${BLUE}🔄 Перезапуск всех Java сервисов...${NC}"
        $0 stop
        sleep 3
        $0 start
        ;;
        
    "status")
        echo -e "${BLUE}📊 Статус Java сервисов:${NC}"
        
        check_service "voice-gateway" 8080
        check_service "todo-service" 8086
        check_service "calendar-service" 8082
        check_service "money-service" 8083
        check_service "nlu-service" 8084
        check_service "dm-service" 8085
        check_service "device-agent" 8087
        check_service "home-bridge" 8088
        ;;
        
    "logs")
        local service_name="$2"
        if [ -z "$service_name" ]; then
            echo -e "${YELLOW}📋 Доступные сервисы: voice-gateway, todo-service, calendar-service, money-service, nlu-service, dm-service, device-agent, home-bridge${NC}"
            echo -e "${BLUE}💡 Использование: $0 logs <service-name>${NC}"
            exit 1
        fi
        
        if [ -f "$LOG_DIR/$service_name.log" ]; then
            echo -e "${BLUE}📋 Логи сервиса $service_name:${NC}"
            tail -f "$LOG_DIR/$service_name.log"
        else
            echo -e "${RED}❌ Лог файл не найден: $LOG_DIR/$service_name.log${NC}"
        fi
        ;;
        
    *)
        echo -e "${BLUE}SmartJARVIS Java Services Manager${NC}"
        echo ""
        echo -e "${GREEN}Использование:${NC}"
        echo "  $0 start     - Запустить все Java сервисы"
        echo "  $0 stop      - Остановить все Java сервисы"
        echo "  $0 restart   - Перезапустить все Java сервисы"
        echo "  $0 status    - Показать статус всех сервисов"
        echo "  $0 logs <service> - Показать логи сервиса"
        echo ""
        echo -e "${YELLOW}Доступные сервисы:${NC}"
        echo "  voice-gateway    (порт 8080) - WebSocket gateway для голоса"
        echo "  todo-service     (порт 8081) - Управление задачами"
        echo "  calendar-service (порт 8082) - Календарные события"
        echo "  money-service    (порт 8083) - Финансовые транзакции"
        echo "  nlu-service      (порт 8084) - Natural Language Understanding"
        echo "  dm-service       (порт 8085) - Dialog Management"
        echo "  device-agent     (порт 8086) - Управление устройствами"
        echo "  home-bridge      (порт 8087) - Интеграция с умным домом"
        echo ""
        echo -e "${BLUE}Логи сохраняются в: $LOG_DIR${NC}"
        echo -e "${BLUE}PID файлы в: $PID_DIR${NC}"
        ;;
esac
