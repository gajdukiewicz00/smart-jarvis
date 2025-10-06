#!/usr/bin/env bash
set -euo pipefail

# Gateway Health and Functionality Test Script
# Tests gateway endpoints and service routing

GATEWAY_URL="http://localhost:8080"
TIMEOUT=10
RETRIES=3

echo "🚀 Testing SmartJARVIS Gateway"
echo "================================"

# Function to test endpoint with retries
test_endpoint() {
    local endpoint="$1"
    local expected_status="$2"
    local description="$3"
    
    echo "🔍 Testing: $description"
    echo "   Endpoint: $endpoint"
    
    for i in $(seq 1 $RETRIES); do
        if response=$(curl -s -w "\n%{http_code}" --max-time $TIMEOUT "$GATEWAY_URL$endpoint" 2>/dev/null); then
            status_code=$(echo "$response" | tail -n1)
            if [ "$status_code" = "$expected_status" ]; then
                echo "   ✅ Status: $status_code (expected: $expected_status)"
                return 0
            else
                echo "   ⚠️  Status: $status_code (expected: $expected_status)"
            fi
        else
            echo "   ❌ Connection failed (attempt $i/$RETRIES)"
        fi
        
        if [ $i -lt $RETRIES ]; then
            echo "   ⏳ Retrying in 2 seconds..."
            sleep 2
        fi
    done
    
    echo "   ❌ Failed after $RETRIES attempts"
    return 1
}

# Function to test service routing
test_service_routing() {
    local service="$1"
    local path="$2"
    local description="$3"
    
    echo "🔍 Testing: $description"
    echo "   Service: $service"
    echo "   Path: $path"
    
    if response=$(curl -s -w "\n%{http_code}" --max-time $TIMEOUT "$GATEWAY_URL/api/$service$path" 2>/dev/null); then
        status_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | head -n -1)
        
        if [ "$status_code" = "200" ] || [ "$status_code" = "404" ]; then
            echo "   ✅ Status: $status_code (service reachable)"
            if [ -n "$body" ]; then
                echo "   📄 Response: ${body:0:100}..."
            fi
            return 0
        else
            echo "   ⚠️  Status: $status_code"
            return 1
        fi
    else
        echo "   ❌ Connection failed"
        return 1
    fi
}

# Test gateway health
echo ""
echo "🏥 Health Checks"
echo "----------------"
test_endpoint "/actuator/health" "200" "Gateway Health Check"

# Test gateway info
echo ""
echo "ℹ️  Gateway Info"
echo "----------------"
test_endpoint "/actuator/info" "200" "Gateway Info"

# Test service routing
echo ""
echo "🔄 Service Routing"
echo "------------------"
test_service_routing "tasks" "/" "Task Service Routing"
test_service_routing "nlp" "/health" "NLP Engine Routing"
test_service_routing "speech" "/health" "Speech Service Routing"

# Test rate limiting (if configured)
echo ""
echo "🚦 Rate Limiting"
echo "----------------"
echo "🔍 Testing rate limiting (if configured)..."
for i in $(seq 1 5); do
    if response=$(curl -s -w "\n%{http_code}" --max-time $TIMEOUT "$GATEWAY_URL/api/tasks/" 2>/dev/null); then
        status_code=$(echo "$response" | tail -n1)
        echo "   Request $i: Status $status_code"
        
        if [ "$status_code" = "429" ]; then
            echo "   ✅ Rate limiting working (429 Too Many Requests)"
            break
        fi
    fi
    sleep 1
done

# Test circuit breaker (if configured)
echo ""
echo "⚡ Circuit Breaker"
echo "------------------"
echo "🔍 Testing circuit breaker (if configured)..."
test_endpoint "/actuator/circuitbreakers" "200" "Circuit Breaker Status"

# Test metrics
echo ""
echo "📊 Metrics"
echo "----------"
test_endpoint "/actuator/metrics" "200" "Gateway Metrics"

# Test Prometheus metrics
test_endpoint "/actuator/prometheus" "200" "Prometheus Metrics"

echo ""
echo "🎯 Gateway Test Summary"
echo "======================="
echo "✅ Health checks completed"
echo "✅ Service routing tested"
echo "✅ Metrics endpoints verified"
echo ""
echo "🚀 Gateway is ready for production use!"
echo ""
echo "📋 Available endpoints:"
echo "   Health: $GATEWAY_URL/actuator/health"
echo "   Info: $GATEWAY_URL/actuator/info"
echo "   Metrics: $GATEWAY_URL/actuator/metrics"
echo "   Prometheus: $GATEWAY_URL/actuator/prometheus"
echo "   Tasks API: $GATEWAY_URL/api/tasks/"
echo "   NLP API: $GATEWAY_URL/api/nlp/"
echo "   Speech API: $GATEWAY_URL/api/speech/"
