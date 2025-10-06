#!/bin/bash

# Скрипт для удаления ярлыка SmartJARVIS

echo "Удаление ярлыка SmartJARVIS..."

# Удаляем desktop файл из меню приложений
echo "Удаление из меню приложений..."
rm -f ~/.local/share/applications/SmartJARVIS.desktop

# Удаляем ярлык с рабочего стола
echo "Удаление с рабочего стола..."
rm -f ~/Desktop/SmartJARVIS.desktop

# Удаляем иконки
echo "Удаление иконок..."
rm -f ~/.local/share/icons/hicolor/scalable/apps/smartjarvis-desktop.png
rm -f ~/.local/share/icons/hicolor/256x256/apps/smartjarvis-desktop.png
rm -f ~/.local/share/icons/hicolor/128x128/apps/smartjarvis-desktop.png
rm -f ~/.local/share/icons/hicolor/64x64/apps/smartjarvis-desktop.png
rm -f ~/.local/share/icons/hicolor/48x48/apps/smartjarvis-desktop.png
rm -f ~/.local/share/icons/hicolor/32x32/apps/smartjarvis-desktop.png
rm -f ~/.local/share/icons/hicolor/16x16/apps/smartjarvis-desktop.png

# Обновляем базу данных приложений
echo "Обновление базы данных приложений..."
update-desktop-database ~/.local/share/applications/

# Обновляем кэш иконок
echo "Обновление кэша иконок..."
gtk-update-icon-cache -f -t ~/.local/share/icons/hicolor/

echo "Ярлык SmartJARVIS успешно удален!"
