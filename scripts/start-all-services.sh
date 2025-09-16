#!/bin/bash

# SmartJARVIS All Services Startup Script

PROJECT_ROOT="/home/kwaqa/IdeaProjects/smart-jarvis"
SCRIPTS_DIR="$PROJECT_ROOT/scripts"

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Функция для проверки зависимостей
check_dependencies() {
    echo -e "${BLUE}🔍 Проверка зависимостей...${NC}"
    
    # Проверяем Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java не установлен${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Java: $(java -version 2>&1 | head -n 1)${NC}"
    
    # Проверяем Python
    if ! command -v python3 &> /dev/null; then
        echo -e "${RED}❌ Python3 не установлен${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Python: $(python3 --version)${NC}"
    
    # Проверяем Docker
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker не установлен${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Docker: $(docker --version)${NC}"
    
    # Проверяем Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}❌ Docker Compose не установлен${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Docker Compose: $(docker-compose --version)${NC}"
    
    return 0
}

# Функция для проверки инфраструктуры
check_infrastructure() {
    echo -e "${BLUE}🏗️ Проверка инфраструктуры...${NC}"
    
    # Проверяем Kafka (используем netcat вместо curl)
    if ! nc -z localhost 9092 2>/dev/null; then
        echo -e "${RED}❌ Kafka недоступен на localhost:9092${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Kafka доступен${NC}"
    
    # Проверяем Schema Registry
    if ! curl -s http://localhost:8081 > /dev/null 2>&1; then
        echo -e "${RED}❌ Schema Registry недоступен на localhost:8081${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Schema Registry доступен${NC}"
    
    # Проверяем PostgreSQL
    if ! nc -z localhost 5432 2>/dev/null; then
        echo -e "${RED}❌ PostgreSQL недоступен на localhost:5432${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ PostgreSQL доступен${NC}"
    
    # Проверяем MongoDB
    if ! nc -z localhost 27017 2>/dev/null; then
        echo -e "${RED}❌ MongoDB недоступен на localhost:27017${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ MongoDB доступен${NC}"
    
    # Проверяем Redis
    if ! nc -z localhost 6379 2>/dev/null; then
        echo -e "${RED}❌ Redis недоступен на localhost:6379${NC}"
        return 1
    fi
    echo -e "${GREEN}✅ Redis доступен${NC}"
    
    return 0
}

# Основная логика
case "$1" in
    "start")
        echo -e "${PURPLE}🚀 Запуск всех сервисов SmartJARVIS...${NC}"
        echo ""
        
        # Проверяем зависимости
        if ! check_dependencies; then
            echo -e "${RED}❌ Не все зависимости установлены${NC}"
            exit 1
        fi
        echo ""
        
        # Проверяем инфраструктуру
        if ! check_infrastructure; then
            echo -e "${YELLOW}⚠️ Инфраструктура не готова. Запускаем инфраструктуру...${NC}"
            make infra-up
            sleep 10
            echo ""
        fi
        
        # Запускаем Java сервисы
        echo -e "${BLUE}☕ Запуск Java сервисов...${NC}"
        "$SCRIPTS_DIR/start-java-services.sh" start
        echo ""
        
        # Запускаем Python сервисы
        echo -e "${BLUE}🐍 Запуск Python сервисов...${NC}"
        "$SCRIPTS_DIR/start-python-services.sh" start
        echo ""
        
        echo -e "${GREEN}🎉 Все сервисы SmartJARVIS запущены!${NC}"
        echo ""
        echo -e "${BLUE}📊 Статус сервисов:${NC}"
        "$SCRIPTS_DIR/start-java-services.sh" status
        "$SCRIPTS_DIR/start-python-services.sh" status
        ;;
        
    "stop")
        echo -e "${YELLOW}🛑 Остановка всех сервисов SmartJARVIS...${NC}"
        
        # Останавливаем Python сервисы
        echo -e "${BLUE}🐍 Остановка Python сервисов...${NC}"
        "$SCRIPTS_DIR/start-python-services.sh" stop
        
        # Останавливаем Java сервисы
        echo -e "${BLUE}☕ Остановка Java сервисов...${NC}"
        "$SCRIPTS_DIR/start-java-services.sh" stop
        
        echo -e "${GREEN}✅ Все сервисы остановлены!${NC}"
        ;;
        
    "restart")
        echo -e "${BLUE}🔄 Перезапуск всех сервисов SmartJARVIS...${NC}"
        $0 stop
        sleep 5
        $0 start
        ;;
        
    "status")
        echo -e "${BLUE}📊 Статус всех сервисов SmartJARVIS:${NC}"
        echo ""
        
        echo -e "${PURPLE}☕ Java сервисы:${NC}"
        "$SCRIPTS_DIR/start-java-services.sh" status
        echo ""
        
        echo -e "${PURPLE}🐍 Python сервисы:${NC}"
        "$SCRIPTS_DIR/start-python-services.sh" status
        echo ""
        
        echo -e "${PURPLE}🏗️ Инфраструктура:${NC}"
        check_infrastructure
        ;;
        
    "logs")
        local service_type="$2"
        local service_name="$3"
        
        if [ -z "$service_type" ] || [ -z "$service_name" ]; then
            echo -e "${YELLOW}📋 Доступные типы сервисов: java, python${NC}"
            echo -e "${BLUE}💡 Использование: $0 logs <java|python> <service-name>${NC}"
            echo ""
            echo -e "${YELLOW}Java сервисы: voice-gateway, todo-service, calendar-service, money-service, nlu-service, dm-service, device-agent, home-bridge${NC}"
            echo -e "${YELLOW}Python сервисы: stt-service, tts-service${NC}"
            exit 1
        fi
        
        case "$service_type" in
            "java")
                "$SCRIPTS_DIR/start-java-services.sh" logs "$service_name"
                ;;
            "python")
                "$SCRIPTS_DIR/start-python-services.sh" logs "$service_name"
                ;;
            *)
                echo -e "${RED}❌ Неизвестный тип сервиса: $service_type${NC}"
                echo -e "${YELLOW}Доступные типы: java, python${NC}"
                exit 1
                ;;
        esac
        ;;
        
    "health")
        echo -e "${BLUE}🏥 Проверка здоровья всех сервисов...${NC}"
        echo ""
        
        # Проверяем инфраструктуру
        echo -e "${PURPLE}🏗️ Инфраструктура:${NC}"
        check_infrastructure
        echo ""
        
        # Проверяем Java сервисы
        echo -e "${PURPLE}☕ Java сервисы:${NC}"
        "$SCRIPTS_DIR/start-java-services.sh" status
        echo ""
        
        # Проверяем Python сервисы
        echo -e "${PURPLE}🐍 Python сервисы:${NC}"
        "$SCRIPTS_DIR/start-python-services.sh" status
        ;;
        
    *)
        echo -e "${PURPLE}SmartJARVIS All Services Manager${NC}"
        echo ""
        echo -e "${GREEN}Использование:${NC}"
        echo "  $0 start                    - Запустить все сервисы"
        echo "  $0 stop                     - Остановить все сервисы"
        echo "  $0 restart                  - Перезапустить все сервисы"
        echo "  $0 status                   - Показать статус всех сервисов"
        echo "  $0 health                   - Проверить здоровье всех сервисов"
        echo "  $0 logs <java|python> <service> - Показать логи сервиса"
        echo ""
        echo -e "${YELLOW}Сервисы SmartJARVIS:${NC}"
        echo ""
        echo -e "${BLUE}☕ Java сервисы:${NC}"
        echo "  voice-gateway    (порт 8080) - WebSocket gateway для голоса"
        echo "  todo-service     (порт 8081) - Управление задачами"
        echo "  calendar-service (порт 8082) - Календарные события"
        echo "  money-service    (порт 8083) - Финансовые транзакции"
        echo "  nlu-service      (порт 8084) - Natural Language Understanding"
        echo "  dm-service       (порт 8085) - Dialog Management"
        echo "  device-agent     (порт 8086) - Управление устройствами"
        echo "  home-bridge      (порт 8087) - Интеграция с умным домом"
        echo ""
        echo -e "${BLUE}🐍 Python сервисы:${NC}"
        echo "  stt-service      (порт 8088) - Speech-to-Text"
        echo "  tts-service      (порт 8089) - Text-to-Speech"
        echo ""
        echo -e "${BLUE}🏗️ Инфраструктура:${NC}"
        echo "  Kafka            (порт 9092) - Event streaming"
        echo "  Schema Registry  (порт 8081) - Avro schemas"
        echo "  PostgreSQL       (порт 5432) - Structured data"
        echo "  MongoDB          (порт 27017) - Document data"
        echo "  Redis            (порт 6379) - Caching"
        echo ""
        echo -e "${BLUE}📋 Логи сохраняются в: $PROJECT_ROOT/logs${NC}"
        echo -e "${BLUE}📋 PID файлы в: $PROJECT_ROOT/pids${NC}"
        ;;
esac
