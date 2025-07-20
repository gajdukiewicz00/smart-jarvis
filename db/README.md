# База данных SmartJARVIS

## Быстрый старт

### 1. Автоматическая настройка
```bash
# Запуск полной настройки (установка PostgreSQL + схема + тестовые данные)
./scripts/setup-database.sh

# Только схема без тестовых данных
./scripts/setup-database.sh --schema-only

# Только проверка подключения
./scripts/setup-database.sh --test-only
```

### 2. Ручная настройка
```bash
# Установка PostgreSQL (Ubuntu/Debian)
sudo apt update
sudo apt install postgresql postgresql-contrib

# Запуск PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Создание пользователя и базы данных
sudo -u postgres psql -c "CREATE USER jarvis WITH PASSWORD 'jarvis_password';"
sudo -u postgres psql -c "CREATE DATABASE jarvis_db OWNER jarvis;"

# Применение схемы
PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -f db/schema-postgresql.sql

# Инициализация тестовых данных
PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -f db/init.sql
```

## Структура файлов

```
db/
├── schema.sql              # Схема для MySQL
├── schema-postgresql.sql   # Схема для PostgreSQL
├── init.sql               # Тестовые данные
└── README.md              # Этот файл
```

## Подключение к базе данных

### Параметры подключения:
- **Хост**: localhost
- **Порт**: 5432
- **База данных**: jarvis_db
- **Пользователь**: jarvis
- **Пароль**: jarvis_password

### Подключение через psql:
```bash
PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db
```

### Подключение через приложение:
```properties
# config/database.properties
db.url=jdbc:postgresql://localhost:5432/jarvis_db
db.username=jarvis
db.password=jarvis_password
db.driver=org.postgresql.Driver
```

## Основные таблицы

### 1. users - Пользователи
```sql
SELECT * FROM users;
```

### 2. tasks - Задачи
```sql
SELECT * FROM tasks;
```

### 3. categories - Категории
```sql
SELECT * FROM categories;
```

### 4. priorities - Приоритеты
```sql
SELECT * FROM priorities;
```

### 5. statuses - Статусы
```sql
SELECT * FROM statuses;
```

## Полезные запросы

### Просмотр задач с полной информацией:
```sql
SELECT * FROM task_details;
```

### Просмотр задач с тегами:
```sql
SELECT * FROM task_with_tags;
```

### Статистика пользователя:
```sql
SELECT * FROM user_statistics;
```

### Поиск задач:
```sql
SELECT * FROM search_tasks('документация', 'user-id-here');
```

### Просроченные задачи:
```sql
SELECT * FROM get_overdue_tasks('user-id-here');
```

## Управление базой данных

### Резервное копирование:
```bash
# Создание дампа
pg_dump -h localhost -U jarvis jarvis_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Восстановление
psql -h localhost -U jarvis -d jarvis_db < backup_file.sql
```

### Очистка базы данных:
```sql
-- Удаление всех данных (кроме справочников)
DELETE FROM task_history;
DELETE FROM reminders;
DELETE FROM task_tags;
DELETE FROM tasks;
DELETE FROM categories WHERE user_id IS NOT NULL;
DELETE FROM tags WHERE user_id IS NOT NULL;
DELETE FROM users WHERE username != 'admin';
```

### Сброс к начальному состоянию:
```bash
# Удаление базы данных
sudo -u postgres psql -c "DROP DATABASE IF EXISTS jarvis_db;"

# Пересоздание
sudo -u postgres psql -c "CREATE DATABASE jarvis_db OWNER jarvis;"

# Применение схемы и данных
PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -f db/schema-postgresql.sql
PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -f db/init.sql
```

## Мониторинг

### Проверка состояния:
```sql
-- Количество записей в таблицах
SELECT 
    'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'tasks', COUNT(*) FROM tasks
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'tags', COUNT(*) FROM tags
UNION ALL
SELECT 'reminders', COUNT(*) FROM reminders;
```

### Производительность:
```sql
-- Медленные запросы
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Использование индексов
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

## Устранение неполадок

### Ошибка подключения:
```bash
# Проверка статуса PostgreSQL
sudo systemctl status postgresql

# Перезапуск PostgreSQL
sudo systemctl restart postgresql

# Проверка подключения
PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -c "SELECT 1;"
```

### Ошибка прав доступа:
```sql
-- Предоставление прав пользователю
GRANT ALL PRIVILEGES ON DATABASE jarvis_db TO jarvis;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO jarvis;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO jarvis;
```

### Ошибка расширений:
```sql
-- Создание расширений
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
```

## Разработка

### Добавление новой таблицы:
1. Добавить CREATE TABLE в `schema-postgresql.sql`
2. Добавить тестовые данные в `init.sql`
3. Обновить документацию в `docs/database/README.md`

### Изменение схемы:
1. Создать миграцию
2. Протестировать на тестовой базе
3. Применить на продакшене

### Пример миграции:
```sql
-- Добавление нового поля
ALTER TABLE tasks ADD COLUMN tags TEXT[];

-- Создание индекса
CREATE INDEX idx_tasks_tags ON tasks USING GIN (tags);

-- Обновление данных
UPDATE tasks SET tags = ARRAY['важное'] WHERE priority_id = (SELECT id FROM priorities WHERE name = 'HIGH');
``` 