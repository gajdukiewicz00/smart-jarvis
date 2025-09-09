#!/bin/bash

# WebSocket Audio Tester Startup Script
# SmartJARVIS Voice Testing Tool

set -e

echo "🎙️  SmartJARVIS WebSocket Audio Tester"
echo "========================================"

# Check if Python 3 is available
if command -v python3 &> /dev/null; then
    PYTHON_CMD="python3"
elif command -v python &> /dev/null; then
    PYTHON_CMD="python"
else
    echo "❌ Python not found. Please install Python 3.x"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "index.html" ]; then
    echo "❌ index.html not found. Please run this script from the ws-audio-tester directory"
    exit 1
fi

# Default port
PORT=${1:-8080}

echo "🚀 Starting HTTP server on port $PORT..."
echo "📱 Open your browser and navigate to: http://localhost:$PORT"
echo "🔧 Make sure voice-gateway is running on port 7090"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Start HTTP server
$PYTHON_CMD -m http.server $PORT

echo ""
echo "👋 Server stopped. Goodbye!" 