#!/bin/bash

# Gateway Test Script
# Tests the Gateway API endpoints

set -e

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

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Gateway base URL
GATEWAY_URL="http://localhost:8080"

# Function to test endpoint
test_endpoint() {
    local endpoint=$1
    local expected_status=${2:-200}
    local description=${3:-"Testing $endpoint"}
    
    print_status "$description"
    
    response=$(curl -s -w "%{http_code}" "$GATEWAY_URL$endpoint" -o /tmp/gateway_response.json)
    status_code=${response: -3}
    
    if [ "$status_code" -eq "$expected_status" ]; then
        print_success "✓ $endpoint (Status: $status_code)"
        if [ -f /tmp/gateway_response.json ]; then
            echo "Response:"
            cat /tmp/gateway_response.json | jq '.' 2>/dev/null || cat /tmp/gateway_response.json
            echo ""
        fi
    else
        print_error "✗ $endpoint (Expected: $expected_status, Got: $status_code)"
        if [ -f /tmp/gateway_response.json ]; then
            echo "Response:"
            cat /tmp/gateway_response.json
            echo ""
        fi
    fi
}

# Check if Gateway is running
check_gateway() {
    print_status "Checking if Gateway is running..."
    
    if curl -s "$GATEWAY_URL/actuator/health" > /dev/null 2>&1; then
        print_success "Gateway is running"
        return 0
    else
        print_error "Gateway is not running. Please start it first."
        return 1
    fi
}

# Main test function
main() {
    print_status "Starting Gateway API tests..."
    
    # Check if Gateway is running
    if ! check_gateway; then
        exit 1
    fi
    
    echo ""
    print_status "Testing Gateway endpoints..."
    echo ""
    
    # Test Gateway info endpoint
    test_endpoint "/api/v1/gateway/info" 200 "Testing Gateway Info"
    
    # Test Gateway health endpoint
    test_endpoint "/api/v1/gateway/health" 200 "Testing Gateway Health"
    
    # Test Gateway docs endpoint
    test_endpoint "/api/v1/gateway/docs" 200 "Testing Gateway Documentation"
    
    # Test Actuator endpoints
    test_endpoint "/actuator/health" 200 "Testing Actuator Health"
    test_endpoint "/actuator/info" 200 "Testing Actuator Info"
    test_endpoint "/actuator/metrics" 200 "Testing Actuator Metrics"
    
    # Test routing to Task Service (if available)
    print_status "Testing Task Service routing..."
    if curl -s "http://localhost:8081/actuator/health" > /dev/null 2>&1; then
        test_endpoint "/api/v1/tasks" 200 "Testing Task Service routing"
    else
        print_error "Task Service not available, skipping routing test"
    fi
    
    # Test routing to NLP Engine (if available)
    print_status "Testing NLP Engine routing..."
    if curl -s "http://localhost:8082/health" > /dev/null 2>&1; then
        test_endpoint "/api/v1/nlp/intents" 200 "Testing NLP Engine routing"
    else
        print_error "NLP Engine not available, skipping routing test"
    fi
    
    # Test fallback endpoints
    print_status "Testing fallback endpoints..."
    test_endpoint "/fallback/task-service" 200 "Testing Task Service fallback"
    test_endpoint "/fallback/nlp-engine" 200 "Testing NLP Engine fallback"
    test_endpoint "/fallback/speech-service" 200 "Testing Speech Service fallback"
    test_endpoint "/fallback/health" 200 "Testing Health fallback"
    
    echo ""
    print_success "🎉 Gateway API tests completed!"
    
    # Cleanup
    rm -f /tmp/gateway_response.json
}

# Run main function
main "$@"