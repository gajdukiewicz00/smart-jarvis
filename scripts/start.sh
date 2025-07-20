#!/bin/bash

set -e

echo "🚀 Starting SmartJARVIS services..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_service() {
    echo -e "${BLUE}[SERVICE]${NC} $1"
}

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            print_status "$service_name is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to start within expected time"
    return 1
}

# Start services with Docker Compose
start_with_docker() {
    print_status "Starting services with Docker Compose..."
    
    cd "$(dirname "$0")/../docker"
    
    # Start all services
    docker-compose up -d
    
    # Wait for services to be ready
    wait_for_service "PostgreSQL" "http://localhost:5432"
    wait_for_service "Task Service" "http://localhost:8081/actuator/health"
    wait_for_service "NLP Engine" "http://localhost:8082/health"
    wait_for_service "Speech Service" "http://localhost:8083/health"
    
    print_status "All Docker services started successfully!"
}

# Start services locally
start_locally() {
    print_status "Starting services locally..."
    
    cd "$(dirname "$0")/.."
    
    # Start Task Service
    print_service "Starting Task Service..."
    cd task-service
    if check_port 8081; then
        print_warning "Port 8081 is already in use. Skipping Task Service."
    else
        mvn spring-boot:run -Dspring-boot.run.profiles=local &
        TASK_PID=$!
        echo $TASK_PID > ../.task-service.pid
    fi
    cd ..
    
    # Start NLP Engine
    print_service "Starting NLP Engine..."
    cd nlp-engine
    if check_port 8082; then
        print_warning "Port 8082 is already in use. Skipping NLP Engine."
    else
        npm start &
        NLP_PID=$!
        echo $NLP_PID > ../.nlp-engine.pid
    fi
    cd ..
    
    # Start Speech Service
    print_service "Starting Speech Service..."
    cd speech-service
    if check_port 8083; then
        print_warning "Port 8083 is already in use. Skipping Speech Service."
    else
        source venv/bin/activate
        python -m uvicorn main:app --host 0.0.0.0 --port 8083 &
        SPEECH_PID=$!
        echo $SPEECH_PID > ../.speech-service.pid
    fi
    cd ..
    
    # Start Desktop Application
    print_service "Starting Desktop Application..."
    cd jarvis-desktop
    mvn javafx:run &
    DESKTOP_PID=$!
    echo $DESKTOP_PID > ../.desktop-app.pid
    cd ..
    
    print_status "All local services started!"
    print_status "PIDs saved in .*.pid files"
}

# Stop all services
stop_services() {
    print_status "Stopping all services..."
    
    cd "$(dirname "$0")/.."
    
    # Stop local processes
    for pid_file in .*.pid; do
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if ps -p $pid > /dev/null 2>&1; then
                print_status "Stopping process $pid"
                kill $pid
            fi
            rm "$pid_file"
        fi
    done
    
    # Stop Docker services
    if command -v docker-compose &> /dev/null; then
        cd docker
        docker-compose down
        cd ..
    fi
    
    print_status "All services stopped!"
}

# Show service status
show_status() {
    print_status "Service Status:"
    
    echo "Task Service (8081): $(check_port 8081 && echo "RUNNING" || echo "STOPPED")"
    echo "NLP Engine (8082): $(check_port 8082 && echo "RUNNING" || echo "STOPPED")"
    echo "Speech Service (8083): $(check_port 8083 && echo "RUNNING" || echo "STOPPED")"
    echo "Grafana (3000): $(check_port 3000 && echo "RUNNING" || echo "STOPPED")"
}

# Main function
main() {
    case "${1:-start}" in
        "start")
            if command -v docker &> /dev/null && command -v docker-compose &> /dev/null; then
                start_with_docker
            else
                print_warning "Docker not available, starting services locally"
                start_locally
            fi
            ;;
        "stop")
            stop_services
            ;;
        "status")
            show_status
            ;;
        "restart")
            stop_services
            sleep 2
            main start
            ;;
        *)
            echo "Usage: $0 {start|stop|status|restart}"
            echo "  start   - Start all services"
            echo "  stop    - Stop all services"
            echo "  status  - Show service status"
            echo "  restart - Restart all services"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
