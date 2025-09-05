#!/bin/bash

# SmartJARVIS Force Clean Script
# This script forcefully removes all containers and networks

set -e

echo "🧹 Force cleaning SmartJARVIS..."

cd "$(dirname "$0")/.."

# Stop and remove all containers
echo "🛑 Stopping all containers..."
docker compose down --remove-orphans --volumes --rmi all 2>/dev/null || true

# Force remove any remaining containers
echo "🗑️ Force removing containers..."
docker ps -aq --filter "name=jarvis" | xargs -r docker rm -f 2>/dev/null || true

# Remove networks
echo "🌐 Removing networks..."
docker network ls --filter "name=jarvis" --format "{{.ID}}" | xargs -r docker network rm 2>/dev/null || true

# Remove volumes
echo "💾 Removing volumes..."
docker volume ls --filter "name=jarvis" --format "{{.Name}}" | xargs -r docker volume rm 2>/dev/null || true

# Clean up system
echo "🧽 Cleaning up system..."
docker system prune -f

echo "✅ Force clean completed successfully!" 