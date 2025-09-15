#!/bin/bash

# Скрипт для установки ярлыка SmartJARVIS

echo "Установка ярлыка SmartJARVIS..."

# Создаем директории если их нет
mkdir -p ~/.local/share/applications
mkdir -p ~/.local/share/icons/hicolor/scalable/apps
mkdir -p ~/.local/share/icons/hicolor/256x256/apps
mkdir -p ~/.local/share/icons/hicolor/128x128/apps
mkdir -p ~/.local/share/icons/hicolor/64x64/apps
mkdir -p ~/.local/share/icons/hicolor/48x48/apps
mkdir -p ~/.local/share/icons/hicolor/32x32/apps
mkdir -p ~/.local/share/icons/hicolor/16x16/apps

# Копируем иконки
echo "Копирование иконок..."
cp icons/icon.png ~/.local/share/icons/hicolor/scalable/apps/smartjarvis-desktop.png
cp icons/icon.png ~/.local/share/icons/hicolor/256x256/apps/smartjarvis-desktop.png
cp icons/128x128.png ~/.local/share/icons/hicolor/128x128/apps/smartjarvis-desktop.png
cp icons/128x128.png ~/.local/share/icons/hicolor/64x64/apps/smartjarvis-desktop.png
cp icons/128x128.png ~/.local/share/icons/hicolor/48x48/apps/smartjarvis-desktop.png
cp icons/32x32.png ~/.local/share/icons/hicolor/32x32/apps/smartjarvis-desktop.png
cp icons/32x32.png ~/.local/share/icons/hicolor/16x16/apps/smartjarvis-desktop.png

# Копируем desktop файл
echo "Копирование desktop файла..."
cp ../../SmartJARVIS-installed.desktop ~/.local/share/applications/SmartJARVIS.desktop

# Обновляем базу данных приложений
echo "Обновление базы данных приложений..."
update-desktop-database ~/.local/share/applications/

# Обновляем кэш иконок
echo "Обновление кэша иконок..."
gtk-update-icon-cache -f -t ~/.local/share/icons/hicolor/

# Создаем ярлык на рабочем столе
echo "Создание ярлыка на рабочем столе..."
cp ../../SmartJARVIS-installed.desktop ~/Desktop/SmartJARVIS.desktop
chmod +x ~/Desktop/SmartJARVIS.desktop

echo "Ярлык SmartJARVIS успешно установлен!"
echo "Приложение должно появиться в меню приложений и на рабочем столе."
