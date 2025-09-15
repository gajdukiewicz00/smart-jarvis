# Ярлык SmartJARVIS

## Описание

Ярлык (desktop file) для приложения SmartJARVIS - персонального голосового ассистента с поддержкой PC интеграции.

## Файлы

- `SmartJARVIS.desktop` - ярлык для разработки (с абсолютными путями)
- `SmartJARVIS-installed.desktop` - ярлык для установленного приложения
- `install-shortcut.sh` - скрипт установки ярлыка
- `uninstall-shortcut.sh` - скрипт удаления ярлыка

## Установка ярлыка

### Автоматическая установка

```bash
./install-shortcut.sh
```

### Ручная установка

1. Скопируйте иконки в системные папки:
```bash
mkdir -p ~/.local/share/icons/hicolor/{16x16,32x32,48x48,64x64,128x128,256x256,scalable}/apps/
cp icons/32x32.png ~/.local/share/icons/hicolor/32x32/apps/smartjarvis-desktop.png
cp icons/128x128.png ~/.local/share/icons/hicolor/128x128/apps/smartjarvis-desktop.png
cp icons/icon.png ~/.local/share/icons/hicolor/scalable/apps/smartjarvis-desktop.png
```

2. Скопируйте desktop файл:
```bash
cp SmartJARVIS-installed.desktop ~/.local/share/applications/SmartJARVIS.desktop
```

3. Обновите базы данных:
```bash
update-desktop-database ~/.local/share/applications/
gtk-update-icon-cache -f -t ~/.local/share/icons/hicolor/
```

4. Создайте ярлык на рабочем столе:
```bash
cp SmartJARVIS-installed.desktop ~/Desktop/SmartJARVIS.desktop
chmod +x ~/Desktop/SmartJARVIS.desktop
```

## Удаление ярлыка

```bash
./uninstall-shortcut.sh
```

## Особенности ярлыка

### Основные свойства

- **Название**: SmartJARVIS
- **Описание**: Персональный голосовой ассистент с поддержкой PC интеграции
- **Категории**: Utility;AudioVideo;Office;Network
- **Ключевые слова**: assistant;voice;ai;jarvis;smart;desktop

### Действия (Actions)

- **Start**: Запустить SmartJARVIS
- **Stop**: Остановить SmartJARVIS  
- **Settings**: Настройки SmartJARVIS

### Иконки

Поддерживаются все стандартные размеры иконок:
- 16x16, 32x32, 48x48, 64x64, 128x128, 256x256
- Масштабируемая иконка (SVG)

## Проверка установки

После установки ярлык должен появиться:

1. **В меню приложений** - поиск "SmartJARVIS"
2. **На рабочем столе** - файл SmartJARVIS.desktop
3. **В списке приложений** - категория "Утилиты" или "Мультимедиа"

## Устранение проблем

### Ярлык не отображается

1. Проверьте права доступа:
```bash
chmod +x ~/Desktop/SmartJARVIS.desktop
```

2. Обновите базы данных:
```bash
update-desktop-database ~/.local/share/applications/
gtk-update-icon-cache -f -t ~/.local/share/icons/hicolor/
```

3. Перезапустите сессию или перелогиньтесь

### Иконка не отображается

1. Проверьте наличие иконок:
```bash
ls -la ~/.local/share/icons/hicolor/*/apps/smartjarvis-desktop.png
```

2. Обновите кэш иконок:
```bash
gtk-update-icon-cache -f -t ~/.local/share/icons/hicolor/
```

### Приложение не запускается

1. Проверьте путь к исполняемому файлу в desktop файле
2. Убедитесь что приложение установлено и доступно в PATH
3. Проверьте права доступа к исполняемому файлу

## Интеграция с системой

Ярлык интегрируется с:

- **Меню приложений** - автоматическое появление в категориях
- **Поиск** - поиск по названию и ключевым словам
- **Рабочий стол** - двойной клик для запуска
- **Контекстное меню** - дополнительные действия
- **Автозапуск** - возможность добавления в автозагрузку

## Поддержка

При возникновении проблем с ярлыком:

1. Проверьте логи системы
2. Убедитесь в корректности путей в desktop файле
3. Проверьте наличие всех необходимых иконок
4. Обновите базы данных приложений и иконок
