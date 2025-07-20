#!/bin/bash

# NLP Engine API Test Script
# Usage: ./test-api.sh [base_url]

BASE_URL=${1:-"http://localhost:8082"}

echo "🧠 Testing NLP Engine API at $BASE_URL"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "\n${YELLOW}Testing: $description${NC}"
    echo "Endpoint: $method $BASE_URL$endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ Success (HTTP $http_code)${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo -e "${RED}❌ Failed (HTTP $http_code)${NC}"
        echo "$body"
    fi
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "⚠️  jq is not installed. Installing JSON formatting..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y jq
    elif command -v yum &> /dev/null; then
        sudo yum install -y jq
    else
        echo "Please install jq manually for better output formatting"
    fi
fi

# Test health check
test_endpoint "GET" "/health" "" "Health Check"

# Test task service status
test_endpoint "GET" "/api/task-service/status" "" "Task Service Status"

# Test get examples
test_endpoint "GET" "/api/examples" "" "Get Voice Command Examples"

# Test get intents
test_endpoint "GET" "/api/intents" "" "Get Supported Intents"

# Test process intent - Create task
test_endpoint "POST" "/api/process" '{
    "text": "Create a task called \"Buy groceries\"",
    "context": {},
    "execute": false
}' "Process Intent - Create Task"

# Test process intent - List tasks
test_endpoint "POST" "/api/process" '{
    "text": "Show my tasks",
    "context": {},
    "execute": false
}' "Process Intent - List Tasks"

# Test process intent - Statistics
test_endpoint "POST" "/api/process" '{
    "text": "What are my task statistics?",
    "context": {},
    "execute": false
}' "Process Intent - Statistics"

# Test process intent - Complete task
test_endpoint "POST" "/api/process" '{
    "text": "Complete task \"Buy groceries\"",
    "context": {},
    "execute": false
}' "Process Intent - Complete Task"

# Test process intent - Priority
test_endpoint "POST" "/api/process" '{
    "text": "Set priority to high for task \"Important meeting\"",
    "context": {},
    "execute": false
}' "Process Intent - Set Priority"

# Test process intent - Unknown
test_endpoint "POST" "/api/process" '{
    "text": "Hello world",
    "context": {},
    "execute": false
}' "Process Intent - Unknown Command"

# Test execute action (if task service is available)
echo -e "\n${YELLOW}Testing Execute Action (requires Task Service)${NC}"
test_endpoint "POST" "/api/execute" '{
    "intent": "task_create",
    "entities": {
        "taskDetails": {
            "title": "Test Task from NLP Engine",
            "priority": "MEDIUM",
            "description": "Created via API test"
        }
    }
}' "Execute Action - Create Task"

echo -e "\n${GREEN}🎉 NLP Engine API testing completed!${NC}"
echo -e "${YELLOW}Note: Some tests may fail if Task Service is not running${NC}" 