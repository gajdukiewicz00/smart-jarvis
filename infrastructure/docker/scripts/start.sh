#!/bin/bash

# SmartJARVIS Docker Startup Script
# This script starts all services with proper health checks

set -e

echo "🚀 Starting SmartJARVIS with Clean Architecture..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! docker compose version > /dev/null 2>&1; then
    echo "❌ Docker Compose v2 is not installed. Please install it first."
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p logs
mkdir -p ssl

# Build and start services
echo "🔨 Building and starting services..."
cd "$(dirname "$0")/.."

# Clean up any existing containers and networks
echo "🧹 Cleaning up existing containers and networks..."
docker compose down --remove-orphans 2>/dev/null || true

# Start core services first
echo "🔄 Starting core services (Redis, PostgreSQL)..."
docker compose up -d redis postgres

# Wait for database to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 10

# Start application services
echo "🔄 Starting application services..."
docker compose up -d task-service nlp-engine speech-service

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
sleep 30

# Start gateway
echo "🔄 Starting gateway..."
docker compose up -d gateway

# Start monitoring
echo "🔄 Starting monitoring services..."
docker compose up -d prometheus grafana

# Show status
echo "📊 Service Status:"
docker compose ps

echo ""
echo "✅ SmartJARVIS is starting up!"
echo ""
echo "🌐 Services:"
echo "   Gateway:      http://localhost:8080"
echo "   Task Service: http://localhost:8081"
echo "   NLP Engine:   http://localhost:8082"
echo "   Speech Service: http://localhost:8083"
echo ""
echo "📈 Monitoring:"
echo "   Grafana:      http://localhost:3000 (admin/admin)"
echo "   Prometheus:   http://localhost:9090"
echo ""
echo "📝 Logs:"
echo "   docker compose logs -f [service-name]"
echo ""
echo "🛑 To stop: docker compose down" 