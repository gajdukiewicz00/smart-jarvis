@echo off
chcp 65001 >nul

echo 🎙️  SmartJARVIS WebSocket Audio Tester
echo ========================================

REM Check if Python is available
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Python not found. Please install Python 3.x
    pause
    exit /b 1
)

REM Check if we're in the right directory
if not exist "index.html" (
    echo ❌ index.html not found. Please run this script from the ws-audio-tester directory
    pause
    exit /b 1
)

REM Default port
set PORT=%1
if "%PORT%"=="" set PORT=8080

echo 🚀 Starting HTTP server on port %PORT%...
echo 📱 Open your browser and navigate to: http://localhost:%PORT%
echo 🔧 Make sure voice-gateway is running on port 7090
echo.
echo Press Ctrl+C to stop the server
echo.

REM Start HTTP server
python -m http.server %PORT%

echo.
echo 👋 Server stopped. Goodbye!
pause 