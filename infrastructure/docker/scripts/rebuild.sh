#!/bin/bash

# SmartJARVIS Docker Rebuild Script

set -e

echo "🔨 Rebuilding SmartJARVIS with Clean Architecture..."

cd "$(dirname "$0")/.."

# Stop existing services
echo "🛑 Stopping existing services..."
docker compose down

# Clean up images
echo "🧹 Cleaning up old images..."
docker compose down --rmi all

# Rebuild all services
echo "🔨 Rebuilding all services..."
docker compose build --no-cache

# Start services
echo "🚀 Starting services..."
./scripts/start.sh

echo "✅ Rebuild completed successfully!" 