# База данных SmartJARVIS

## Обзор

База данных SmartJARVIS представляет собой нормализованную реляционную структуру, предназначенную для управления задачами, пользователями, категориями и интеграции с NLP-системой.

## Архитектура

### Основные принципы:
- **Нормализация**: Полная нормализация до 3NF
- **Индексация**: Оптимизированные индексы для быстрого поиска
- **Целостность**: Строгие ограничения и внешние ключи
- **Масштабируемость**: Поддержка множественных пользователей
- **Аудит**: История изменений и логирование

## Структура таблиц

### 1. Пользователи (users)
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN
);
```

### 2. Задачи (tasks)
```sql
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    user_id UUID NOT NULL,
    category_id UUID,
    priority_id UUID,
    status_id UUID,
    due_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_hours DECIMAL(5,2),
    actual_hours DECIMAL(5,2)
);
```

### 3. Категории (categories)
```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    user_id UUID,
    created_at TIMESTAMP,
    is_active BOOLEAN
);
```

### 4. Приоритеты (priorities)
```sql
CREATE TABLE priorities (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE,
    level INTEGER CHECK (1-4),
    color VARCHAR(7),
    description TEXT
);
```

### 5. Статусы (statuses)
```sql
CREATE TABLE statuses (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE,
    description TEXT,
    color VARCHAR(7)
);
```

### 6. Теги (tags)
```sql
CREATE TABLE tags (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7),
    user_id UUID,
    created_at TIMESTAMP
);
```

### 7. Связь задач и тегов (task_tags)
```sql
CREATE TABLE task_tags (
    task_id UUID,
    tag_id UUID,
    created_at TIMESTAMP,
    PRIMARY KEY (task_id, tag_id)
);
```

### 8. Напоминания (reminders)
```sql
CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reminder_date TIMESTAMP NOT NULL,
    message TEXT,
    is_sent BOOLEAN,
    sent_at TIMESTAMP,
    created_at TIMESTAMP
);
```

### 9. История изменений (task_history)
```sql
CREATE TABLE task_history (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    field_name VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    changed_at TIMESTAMP
);
```

### 10. NLP команды (nlp_commands)
```sql
CREATE TABLE nlp_commands (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    command_text TEXT NOT NULL,
    intent VARCHAR(100),
    entities JSONB,
    processed_at TIMESTAMP,
    created_at TIMESTAMP
);
```

## Нормализация

### Первая нормальная форма (1NF)
- ✅ Все атрибуты атомарны
- ✅ Нет повторяющихся групп
- ✅ Каждая запись уникальна

### Вторая нормальная форма (2NF)
- ✅ Все неключевые атрибуты полностью зависят от первичного ключа
- ✅ Вынесены справочники (Priorities, Statuses, Categories)

### Третья нормальная форма (3NF)
- ✅ Нет транзитивных зависимостей
- ✅ Справочники вынесены в отдельные таблицы
- ✅ Связи реализованы через внешние ключи

## Индексы

### Основные индексы:
```sql
-- Пользователи
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_active ON users (is_active);

-- Задачи
CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_category_id ON tasks (category_id);
CREATE INDEX idx_tasks_priority_id ON tasks (priority_id);
CREATE INDEX idx_tasks_status_id ON tasks (status_id);
CREATE INDEX idx_tasks_due_date ON tasks (due_date);
CREATE INDEX idx_tasks_created_at ON tasks (created_at);
CREATE INDEX idx_tasks_title ON tasks USING gin (to_tsvector('english', title));

-- Составные индексы
CREATE INDEX idx_tasks_user_status ON tasks (user_id, status_id);
CREATE INDEX idx_tasks_user_due ON tasks (user_id, due_date);
CREATE INDEX idx_tasks_overdue ON tasks (due_date, status_id) WHERE due_date < CURRENT_TIMESTAMP;

-- Напоминания
CREATE INDEX idx_reminders_pending ON reminders (reminder_date, is_sent) WHERE is_sent = FALSE;

-- NLP команды
CREATE INDEX idx_nlp_commands_entities ON nlp_commands USING GIN (entities);
```

## Ограничения

### Проверочные ограничения:
```sql
-- Приоритеты
CONSTRAINT chk_priority_level CHECK (level BETWEEN 1 AND 4)

-- Задачи
CONSTRAINT chk_estimated_hours CHECK (estimated_hours >= 0)
CONSTRAINT chk_actual_hours CHECK (actual_hours >= 0)
CONSTRAINT chk_due_date CHECK (due_date IS NULL OR due_date >= created_at)
CONSTRAINT chk_completed_date CHECK (completed_at IS NULL OR completed_at >= created_at)

-- Напоминания
CONSTRAINT chk_reminder_date CHECK (reminder_date >= created_at)
CONSTRAINT chk_sent_at CHECK (sent_at IS NULL OR sent_at >= created_at)
```

### Уникальные ограничения:
```sql
-- Пользователи
CONSTRAINT uk_users_username UNIQUE (username)
CONSTRAINT uk_users_email UNIQUE (email)

-- Категории
CONSTRAINT uk_category_user_name UNIQUE (user_id, name)

-- Теги
CONSTRAINT uk_tag_user_name UNIQUE (user_id, name)
```

## Представления (Views)

### 1. task_details
Полная информация о задачах с пользователями, категориями, приоритетами и статусами.

### 2. task_with_tags
Задачи с объединенными тегами.

### 3. user_statistics
Статистика пользователей по задачам.

## Функции

### 1. search_tasks(search_term TEXT, user_id UUID)
Поиск задач по тексту с использованием полнотекстового поиска.

### 2. get_overdue_tasks(user_id UUID)
Получение просроченных задач пользователя.

### 3. update_updated_at_column()
Автоматическое обновление поля updated_at.

### 4. record_task_history()
Запись истории изменений задач.

## Триггеры

### 1. tasks_update_trigger
Автоматически обновляет updated_at при изменении задачи.

### 2. tasks_history_trigger
Записывает изменения в историю при обновлении задачи.

## Начальные данные

### Приоритеты:
- LOW (1) - Низкий приоритет
- MEDIUM (2) - Средний приоритет  
- HIGH (3) - Высокий приоритет
- URGENT (4) - Срочный приоритет

### Статусы:
- PENDING - Ожидает выполнения
- IN_PROGRESS - В работе
- PAUSED - Приостановлено
- COMPLETED - Завершено
- CANCELLED - Отменено

### Системные категории:
- Работа
- Личное
- Учеба
- Здоровье
- Финансы

### Системные теги:
- важное
- срочно
- проект
- встреча
- звонок

## Производительность

### Оптимизация запросов:
1. **Индексы** на часто используемых полях
2. **Составные индексы** для сложных запросов
3. **Частичные индексы** для фильтрации
4. **GIN индексы** для JSON и полнотекстового поиска

### Мониторинг:
```sql
-- Анализ производительности индексов
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Медленные запросы
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

## Безопасность

### Шифрование:
- Пароли хешируются с использованием bcrypt
- Чувствительные данные не хранятся в открытом виде

### Права доступа:
- Пользователи видят только свои данные
- Системные справочники доступны всем
- Аудит изменений через task_history

## Резервное копирование

### Автоматическое резервное копирование:
```bash
# Создание дампа
pg_dump -h localhost -U postgres jarvis_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Восстановление
psql -h localhost -U postgres jarvis_db < backup_file.sql
```

## Миграции

### Создание миграции:
```sql
-- Пример миграции
CREATE TABLE IF NOT EXISTS new_feature (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Откат миграции
DROP TABLE IF EXISTS new_feature;
```

## Мониторинг

### Ключевые метрики:
- Количество активных пользователей
- Количество задач по статусам
- Среднее время выполнения задач
- Количество просроченных задач
- Производительность запросов

### Алерты:
- Высокое количество просроченных задач
- Медленные запросы (> 1 секунды)
- Недостаток места на диске
- Ошибки подключения к БД 