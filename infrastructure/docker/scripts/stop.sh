#!/bin/bash

# SmartJARVIS Docker Stop Script

set -e

echo "🛑 Stopping SmartJARVIS..."

cd "$(dirname "$0")/.."

# Stop all services and remove orphaned containers
echo "🔄 Stopping all services..."
docker compose down --remove-orphans

# Remove volumes if requested
if [[ "$1" == "--clean" ]]; then
    echo "🧹 Cleaning up volumes..."
    docker compose down -v --remove-orphans
    echo "✅ Volumes cleaned up"
fi

echo "✅ SmartJARVIS stopped successfully!" 