#!/bin/bash

# STT Service Runner Script
# Activates virtual environment and runs the service

set -e

echo "Starting STT Service..."

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
else
    echo "Activating existing virtual environment..."
    source venv/bin/activate
fi

# Set environment variables
export PYTHONPATH="${PYTHONPATH}:$(pwd)"
export SERVICE_NAME="stt-service"
export SERVICE_PORT="${STT_SERVICE_PORT:-8082}"
export PORT="${SERVICE_PORT}"

echo "STT Service starting on port ${SERVICE_PORT}..."
echo "Virtual environment: $(which python)"
echo "Python version: $(python --version)"

# Run the service
python main.py
