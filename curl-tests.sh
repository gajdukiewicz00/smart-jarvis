#!/bin/bash

echo "=== SmartJARVIS Local Testing with curl ==="
echo "Timestamp: $(date)"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== 1. Gateway Health Check ===${NC}"
echo "curl -v http://localhost:8080/actuator/health"
curl -v http://localhost:8080/actuator/health
echo ""
echo ""

echo -e "${BLUE}=== 2. Task Service (via Gateway) ===${NC}"
echo "curl -v http://localhost:8080/api/v1/tasks"
curl -v http://localhost:8080/api/v1/tasks
echo ""
echo ""

echo -e "${BLUE}=== 3. Task Service (direct) ===${NC}"
echo "curl -v http://localhost:8081/tasks"
curl -v http://localhost:8081/tasks
echo ""
echo ""

echo -e "${BLUE}=== 4. NLP Engine (via Gateway) ===${NC}"
echo "curl -v http://localhost:8080/api/v1/nlp/health"
curl -v http://localhost:8080/api/v1/nlp/health
echo ""
echo ""

echo -e "${BLUE}=== 5. NLP Engine (direct) ===${NC}"
echo "curl -v http://localhost:8082/health"
curl -v http://localhost:8082/health
echo ""
echo ""

echo -e "${BLUE}=== 6. Speech Service (via Gateway) ===${NC}"
echo "curl -v http://localhost:8080/api/v1/speech/"
curl -v http://localhost:8080/api/v1/speech/
echo ""
echo ""

echo -e "${BLUE}=== 7. Speech Service (direct) ===${NC}"
echo "curl -v http://localhost:8083/"
curl -v http://localhost:8083/
echo ""
echo ""

echo -e "${BLUE}=== 8. Rate Limiter Test (multiple requests) ===${NC}"
echo "Making 3 quick requests to test Rate Limiter..."
for i in {1..3}; do
    echo "Request $i:"
    curl -s -o /dev/null -w "Status: %{http_code}, Time: %{time_total}s\n" http://localhost:8080/api/v1/tasks
    sleep 0.5
done
echo ""
echo ""

echo -e "${BLUE}=== 9. Gateway Info ===${NC}"
echo "curl -v http://localhost:8080/actuator/gateway/routes"
curl -v http://localhost:8080/actuator/gateway/routes
echo ""
echo ""

echo -e "${BLUE}=== 10. Process Status ===${NC}"
echo "Checking if all services are running..."
ps aux | grep -E "(java|node|python)" | grep -v grep
echo ""
echo ""

echo -e "${BLUE}=== 11. Port Status ===${NC}"
echo "Checking which ports are listening..."
ss -tlnp | grep -E "(8080|8081|8082|8083)"
echo ""
echo ""

echo -e "${GREEN}=== Testing Complete ===${NC}"
echo "✅ Gateway: http://localhost:8080"
echo "✅ Task Service: http://localhost:8081"
echo "✅ NLP Engine: http://localhost:8082"
echo "✅ Speech Service: http://localhost:8083"
echo ""
echo "🌐 Browser Access:"
echo "   - Gateway Health: http://localhost:8080/actuator/health"
echo "   - Tasks API: http://localhost:8080/api/v1/tasks"
echo "   - Gateway Routes: http://localhost:8080/actuator/gateway/routes" 