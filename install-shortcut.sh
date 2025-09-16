#!/usr/bin/env bash
set -euo pipefail

echo "📎 Установка ярлыка SmartJARVIS..."

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
LAUNCHER="$PROJECT_ROOT/scripts/open-desktop.sh"
ICON_PATH="$PROJECT_ROOT/desktop/src-tauri/icons/icon.png"
APP_NAME="SmartJARVIS"
DESKTOP_ID="smartjarvis.desktop"
APPS_DIR="$HOME/.local/share/applications"
ICONS_DIR="$HOME/.local/share/icons/hicolor/128x128/apps"
DESKTOP_FILE="$APPS_DIR/$DESKTOP_ID"
DESKTOP_SHORTCUT="$HOME/Desktop/$APP_NAME.desktop"

# Проверки
if [ ! -x "$LAUNCHER" ]; then
  echo "⚠️  Делает исполняемым $LAUNCHER"
  chmod +x "$LAUNCHER" || true
fi

if [ ! -f "$ICON_PATH" ]; then
  echo "⚠️  Иконка не найдена по пути $ICON_PATH. Будет использован стандартный значок.";
  ICON_PATH=""
fi

# Директории
mkdir -p "$APPS_DIR" "$ICONS_DIR"

# Копируем иконку (если есть)
if [ -n "$ICON_PATH" ]; then
  cp "$ICON_PATH" "$ICONS_DIR/smartjarvis.png"
  ICON_LINE="Icon=$ICONS_DIR/smartjarvis.png"
else
  ICON_LINE="Icon=utilities-terminal"
fi

# Генерация .desktop файла
cat > "$DESKTOP_FILE" <<EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=$APP_NAME
Comment=Персональный ассистент SmartJARVIS (Desktop)
Exec=$LAUNCHER
$ICON_LINE
Terminal=false
StartupNotify=true
Categories=Utility;AudioVideo;Network;
Keywords=assistant;voice;ai;jarvis;smart;desktop;
EOF

chmod +x "$DESKTOP_FILE"

# Обновляем базы
update-desktop-database "$APPS_DIR" >/dev/null 2>&1 || true
gtk-update-icon-cache -f -t "$HOME/.local/share/icons/hicolor/" >/dev/null 2>&1 || true

# Ярлык на рабочем столе
cp "$DESKTOP_FILE" "$DESKTOP_SHORTCUT"
chmod +x "$DESKTOP_SHORTCUT"

# Отметить ярлык как доверенный (Ubuntu/GNOME)
gio set "$DESKTOP_SHORTCUT" metadata::trusted true >/dev/null 2>&1 || true
xdg-desktop-menu forceupdate >/dev/null 2>&1 || true

echo "✅ Ярлык установлен:"
echo "   • Меню приложений: $DESKTOP_FILE"
echo "   • Рабочий стол:    $DESKTOP_SHORTCUT"
