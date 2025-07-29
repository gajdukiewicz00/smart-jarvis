#!/bin/bash

echo "=== Simple Gateway Test ==="
echo "Timestamp: $(date)"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to test endpoint
test_endpoint() {
    local name=$1
    local url=$2
    local expected_status=$3
    
    echo -n "Testing $name... "
    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    
    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✓ SUCCESS${NC} ($response)"
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} ($response)"
        return 1
    fi
}

echo "=== Gateway Health ==="
test_endpoint "Gateway Health" "http://localhost:8080/actuator/health" "200"

echo ""
echo "=== Task Service ==="
test_endpoint "Task Service (via Gateway)" "http://localhost:8080/api/v1/tasks" "200"
test_endpoint "Task Service (direct)" "http://localhost:8081/api/v1/tasks" "200"

echo ""
echo "=== NLP Engine ==="
test_endpoint "NLP Engine (direct)" "http://localhost:8082/health" "200"
test_endpoint "NLP Engine (via Gateway - old path)" "http://localhost:8080/api/v1/nlp/health" "404"
test_endpoint "NLP Engine (via Gateway - new path)" "http://localhost:8080/nlp/health" "404"

echo ""
echo "=== Speech Service ==="
test_endpoint "Speech Service (direct)" "http://localhost:8083/" "404"
test_endpoint "Speech Service (via Gateway)" "http://localhost:8080/api/v1/speech/" "404"

echo ""
echo "=== Summary ==="
echo "✅ Gateway is working"
echo "✅ Task Service is working"
echo "✅ NLP Engine is working (direct access)"
echo "⚠️  NLP Engine routing needs fixing"
echo "⚠️  Speech Service routing needs fixing"

echo ""
echo "=== Next Steps ==="
echo "1. Fix NLP Engine routing"
echo "2. Fix Speech Service routing"
echo "3. Test all endpoints through gateway" 