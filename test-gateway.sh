#!/bin/bash

echo "=== Testing SmartJARVIS Gateway ==="
echo "Gateway URL: http://localhost:8080"
echo ""

echo "1. Testing Gateway Health..."
curl -s http://localhost:8080/actuator/health | jq '.status' 2>/dev/null || echo "Gateway health check failed"

echo ""
echo "2. Testing Task Service..."
curl -s http://localhost:8080/api/v1/tasks | jq '.' 2>/dev/null || echo "Task service test failed"

echo ""
echo "3. Testing NLP Engine..."
curl -s http://localhost:8080/api/v1/nlp/ | jq '.' 2>/dev/null || echo "NLP engine test failed"

echo ""
echo "4. Testing Speech Service..."
curl -s http://localhost:8080/api/v1/speech/ | jq '.' 2>/dev/null || echo "Speech service test failed"

echo ""
echo "5. Testing Direct Service Access..."
echo "Task Service (direct):"
curl -s http://localhost:8081/api/v1/tasks | jq '.' 2>/dev/null || echo "Direct task service test failed"

echo ""
echo "NLP Engine (direct):"
curl -s http://localhost:8082/health | jq '.' 2>/dev/null || echo "Direct NLP engine test failed"

echo ""
echo "Speech Service (direct):"
curl -s http://localhost:8083/ | jq '.' 2>/dev/null || echo "Direct speech service test failed"

echo ""
echo "=== Gateway Test Complete ===" 