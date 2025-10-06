#!/bin/bash

# Test script for SmartJARVIS Nginx Proxy
# This script tests all the proxy routes

BASE_URL="http://localhost:8080"

echo "🧪 Testing SmartJARVIS Nginx Proxy Configuration"
echo "================================================"

# Test health endpoint
echo -e "\n1. Testing health endpoint..."
curl -s "$BASE_URL/health" | jq .

# Test default endpoint
echo -e "\n2. Testing default endpoint..."
curl -s "$BASE_URL/" | jq .

# Test NLP service endpoint
echo -e "\n3. Testing NLP service endpoint..."
curl -s "$BASE_URL/nlp/health" | jq . 2>/dev/null || echo "NLP service not responding"

# Test Speech service endpoint
echo -e "\n4. Testing Speech service endpoint..."
curl -s "$BASE_URL/speech/health" | jq . 2>/dev/null || echo "Speech service not responding"

# Test Task service endpoint
echo -e "\n5. Testing Task service endpoint..."
curl -s "$BASE_URL/task/tasks" | jq . 2>/dev/null || echo "Task service not responding"

# Test Prometheus endpoint
echo -e "\n6. Testing Prometheus endpoint..."
curl -s "$BASE_URL/prometheus/-/healthy" | jq . 2>/dev/null || echo "Prometheus not responding"

# Test CORS headers
echo -e "\n7. Testing CORS headers..."
curl -s -I -H "Origin: http://localhost:3000" "$BASE_URL/health" | grep -i "access-control"

# Test 404 endpoint
echo -e "\n8. Testing 404 endpoint..."
curl -s "$BASE_URL/nonexistent" | jq .

echo -e "\n✅ Testing completed!" 