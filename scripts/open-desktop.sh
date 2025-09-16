#!/usr/bin/env bash
set -euo pipefail

# Open SmartJARVIS Desktop (Tauri) in dev mode
# Usage: ./scripts/open-desktop.sh

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DESKTOP_DIR="$PROJECT_ROOT/desktop"

cd "$DESKTOP_DIR"

# Ensure deps
if [ ! -d "node_modules" ]; then
  echo "📦 Installing frontend deps..."
  npm install --silent
fi

# Start Tauri dev (Vite + Rust)
echo "🚀 Launching SmartJARVIS Desktop (dev)..."
npm run tauri dev --silent || npx tauri dev
