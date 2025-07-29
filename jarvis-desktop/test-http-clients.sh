#!/bin/bash

echo "Testing HTTP clients for SmartJARVIS Desktop..."

# Check if task service is running
echo "Checking Task Service..."
if curl -s http://localhost:8082/api/v1/tasks/ping > /dev/null; then
    echo "✅ Task Service is running"
else
    echo "❌ Task Service is not running on http://localhost:8082"
fi

# Check if NLP service is running
echo "Checking NLP Service..."
if curl -s http://localhost:3000/health > /dev/null; then
    echo "✅ NLP Service is running"
else
    echo "❌ NLP Service is not running on http://localhost:3000"
fi

# Check if Speech service is running
echo "Checking Speech Service..."
if curl -s http://localhost:5000/health > /dev/null; then
    echo "✅ Speech Service is running"
else
    echo "❌ Speech Service is not running on http://localhost:5000"
fi

echo ""
echo "To start the services, run:"
echo "  docker-compose up -d"
echo ""
echo "To test the desktop application:"
echo "  mvn clean compile exec:java -pl jarvis-desktop"