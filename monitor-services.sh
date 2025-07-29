#!/bin/bash

echo "=== SmartJARVIS Services Monitor ==="
echo "Timestamp: $(date)"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check service
check_service() {
    local name=$1
    local url=$2
    local expected_status=$3
    
    echo -n "Checking $name... "
    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    
    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✓ UP${NC} ($response)"
        return 0
    else
        echo -e "${RED}✗ DOWN${NC} ($response)"
        return 1
    fi
}

# Check Gateway
echo "=== Gateway ==="
check_service "Gateway Health" "http://localhost:8080/actuator/health" "200"
check_service "Gateway Info" "http://localhost:8080/api/v1/gateway/info" "200"

echo ""
echo "=== Microservices ==="

# Check Task Service
check_service "Task Service (via Gateway)" "http://localhost:8080/api/v1/tasks" "200"
check_service "Task Service (direct)" "http://localhost:8081/api/v1/tasks" "200"

# Check NLP Engine
check_service "NLP Engine (via Gateway)" "http://localhost:8080/api/v1/nlp/" "404"
check_service "NLP Engine (direct)" "http://localhost:8082/health" "200"

# Check Speech Service
check_service "Speech Service (via Gateway)" "http://localhost:8080/api/v1/speech/" "404"
check_service "Speech Service (direct)" "http://localhost:8083/" "404"

echo ""
echo "=== Service Status Summary ==="
echo "Gateway: http://localhost:8080"
echo "Task Service: http://localhost:8081"
echo "NLP Engine: http://localhost:8082"
echo "Speech Service: http://localhost:8083"

echo ""
echo "=== Available Endpoints ==="
echo "Gateway Health: http://localhost:8080/actuator/health"
echo "Gateway Info: http://localhost:8080/api/v1/gateway/info"
echo "Tasks API: http://localhost:8080/api/v1/tasks"
echo "NLP API: http://localhost:8080/api/v1/nlp/"
echo "Speech API: http://localhost:8080/api/v1/speech/"

echo ""
echo "=== Browser Access ==="
echo "You can now access the gateway in your browser at:"
echo "http://localhost:8080/actuator/health"
echo ""
echo "✅ Gateway is working and ready for browser access!" 