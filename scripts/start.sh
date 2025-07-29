#!/bin/bash

# SmartJARVIS Start Script
# Starts all services in the correct order

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
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a port is available
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
    local port=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for $service_name to be ready on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if check_port $port; then
            print_success "$service_name is ready!"
            return 0
        fi
        
        print_status "Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to start within expected time"
    return 1
}

# Start Redis (if not running)
start_redis() {
    print_status "Starting Redis..."
    
    if ! command -v redis-server &> /dev/null; then
        print_warning "Redis not installed locally. Please install Redis or use Docker."
        return
    fi
    
    if ! check_port 6379; then
        redis-server --daemonize yes
        print_success "Redis started"
    else
        print_status "Redis is already running"
    fi
}

# Start PostgreSQL (if not running)
start_postgres() {
    print_status "Starting PostgreSQL..."
    
    if ! command -v pg_ctl &> /dev/null; then
        print_warning "PostgreSQL not installed locally. Please install PostgreSQL or use Docker."
        return
    fi
    
    if ! check_port 5432; then
        # This is a simplified example - you might need to adjust based on your PostgreSQL installation
        print_warning "Please start PostgreSQL manually or use Docker"
    else
        print_status "PostgreSQL is already running"
    fi
}

# Start Task Service
start_task_service() {
    print_status "Starting Task Service..."
    
    if [ ! -f "task-service/target/task-service-1.0.0.jar" ]; then
        print_error "Task Service JAR not found. Please run './scripts/build-all.sh' first."
        exit 1
    fi
    
    cd task-service
    nohup java -jar target/task-service-1.0.0.jar > ../logs/task-service.log 2>&1 &
    TASK_SERVICE_PID=$!
    echo $TASK_SERVICE_PID > ../logs/task-service.pid
    cd ..
    
    wait_for_service "Task Service" 8081
}

# Start NLP Engine
start_nlp_engine() {
    print_status "Starting NLP Engine..."
    
    if [ ! -d "nlp-engine/node_modules" ]; then
        print_error "NLP Engine dependencies not installed. Please run './scripts/build-all.sh' first."
        exit 1
    fi
    
    cd nlp-engine
    nohup npm start > ../logs/nlp-engine.log 2>&1 &
    NLP_ENGINE_PID=$!
    echo $NLP_ENGINE_PID > ../logs/nlp-engine.pid
    cd ..
    
    wait_for_service "NLP Engine" 8082
}

# Start Speech Service
start_speech_service() {
    print_status "Starting Speech Service..."
    
    cd speech-service
    nohup python3 main.py > ../logs/speech-service.log 2>&1 &
    SPEECH_SERVICE_PID=$!
    echo $SPEECH_SERVICE_PID > ../logs/speech-service.pid
    cd ..
    
    wait_for_service "Speech Service" 8083
}

# Start Gateway Service
start_gateway_service() {
    print_status "Starting Gateway Service..."
    
    if [ ! -f "gateway/target/gateway-1.0.0.jar" ]; then
        print_error "Gateway Service JAR not found. Please run './scripts/build-all.sh' first."
        exit 1
    fi
    
    cd gateway
    nohup java -jar target/gateway-1.0.0.jar > ../logs/gateway.log 2>&1 &
    GATEWAY_PID=$!
    echo $GATEWAY_PID > ../logs/gateway.pid
    cd ..
    
    wait_for_service "Gateway Service" 8080
}

# Create logs directory
create_logs_directory() {
    if [ ! -d "logs" ]; then
        mkdir -p logs
        print_status "Created logs directory"
    fi
}

# Stop all services
stop_services() {
    print_status "Stopping all services..."
    
    # Stop Gateway
    if [ -f "logs/gateway.pid" ]; then
        GATEWAY_PID=$(cat logs/gateway.pid)
        if kill -0 $GATEWAY_PID 2>/dev/null; then
            kill $GATEWAY_PID
            print_status "Stopped Gateway Service"
        fi
        rm -f logs/gateway.pid
    fi
    
    # Stop Speech Service
    if [ -f "logs/speech-service.pid" ]; then
        SPEECH_SERVICE_PID=$(cat logs/speech-service.pid)
        if kill -0 $SPEECH_SERVICE_PID 2>/dev/null; then
            kill $SPEECH_SERVICE_PID
            print_status "Stopped Speech Service"
        fi
        rm -f logs/speech-service.pid
    fi
    
    # Stop NLP Engine
    if [ -f "logs/nlp-engine.pid" ]; then
        NLP_ENGINE_PID=$(cat logs/nlp-engine.pid)
        if kill -0 $NLP_ENGINE_PID 2>/dev/null; then
            kill $NLP_ENGINE_PID
            print_status "Stopped NLP Engine"
        fi
        rm -f logs/nlp-engine.pid
    fi
    
    # Stop Task Service
    if [ -f "logs/task-service.pid" ]; then
        TASK_SERVICE_PID=$(cat logs/task-service.pid)
        if kill -0 $TASK_SERVICE_PID 2>/dev/null; then
            kill $TASK_SERVICE_PID
            print_status "Stopped Task Service"
        fi
        rm -f logs/task-service.pid
    fi
    
    print_success "All services stopped"
}

# Show service status
show_status() {
    print_status "Service Status:"
    echo "  Gateway Service: $(check_port 8080 && echo "RUNNING" || echo "STOPPED")"
    echo "  Task Service: $(check_port 8081 && echo "RUNNING" || echo "STOPPED")"
    echo "  NLP Engine: $(check_port 8082 && echo "RUNNING" || echo "STOPPED")"
    echo "  Speech Service: $(check_port 8083 && echo "RUNNING" || echo "STOPPED")"
    echo "  Redis: $(check_port 6379 && echo "RUNNING" || echo "STOPPED")"
    echo "  PostgreSQL: $(check_port 5432 && echo "RUNNING" || echo "STOPPED")"
}

# Main function
main() {
    case "${1:-start}" in
        "start")
            print_status "Starting SmartJARVIS services..."
            
            # Create logs directory
            create_logs_directory
            
            # Start infrastructure services
            start_redis
            start_postgres
            
            # Start application services
            start_task_service
            start_nlp_engine
            start_speech_service
            start_gateway_service
            
            print_success "🎉 All services started successfully!"
            print_status "Gateway is available at: http://localhost:8080"
            print_status "Task Service: http://localhost:8081"
            print_status "NLP Engine: http://localhost:8082"
            print_status "Speech Service: http://localhost:8083"
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

# Handle Ctrl+C
trap 'print_status "Received interrupt signal. Stopping services..."; stop_services; exit 0' INT

# Run main function
main "$@"
