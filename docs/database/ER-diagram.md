# ER-диаграмма базы данных SmartJARVIS

## Сущности

### 1. Users (Пользователи)
- **id** (UUID, PK) - уникальный идентификатор пользователя
- **username** (VARCHAR(50), UNIQUE) - имя пользователя
- **email** (VARCHAR(100), UNIQUE) - email пользователя
- **password_hash** (VARCHAR(255)) - хеш пароля
- **first_name** (VARCHAR(50)) - имя
- **last_name** (VARCHAR(50)) - фамилия
- **created_at** (TIMESTAMP) - дата создания
- **updated_at** (TIMESTAMP) - дата обновления
- **is_active** (BOOLEAN) - активен ли пользователь

### 2. Tasks (Задачи)
- **id** (UUID, PK) - уникальный идентификатор задачи
- **title** (VARCHAR(255), NOT NULL) - название задачи
- **description** (TEXT) - описание задачи
- **user_id** (UUID, FK) - владелец задачи
- **category_id** (UUID, FK) - категория задачи
- **priority_id** (UUID, FK) - приоритет задачи
- **status_id** (UUID, FK) - статус задачи
- **due_date** (TIMESTAMP) - срок выполнения
- **created_at** (TIMESTAMP) - дата создания
- **updated_at** (TIMESTAMP) - дата обновления
- **completed_at** (TIMESTAMP) - дата завершения
- **estimated_hours** (DECIMAL(5,2)) - оценка времени в часах
- **actual_hours** (DECIMAL(5,2)) - фактическое время в часах

### 3. Categories (Категории)
- **id** (UUID, PK) - уникальный идентификатор категории
- **name** (VARCHAR(100), NOT NULL) - название категории
- **description** (TEXT) - описание категории
- **color** (VARCHAR(7)) - цвет категории (hex)
- **user_id** (UUID, FK) - владелец категории (NULL для системных)
- **created_at** (TIMESTAMP) - дата создания
- **is_active** (BOOLEAN) - активна ли категория

### 4. Priorities (Приоритеты)
- **id** (UUID, PK) - уникальный идентификатор приоритета
- **name** (VARCHAR(50), NOT NULL) - название приоритета
- **level** (INTEGER, NOT NULL) - уровень приоритета (1-4)
- **color** (VARCHAR(7)) - цвет приоритета (hex)
- **description** (TEXT) - описание приоритета

### 5. Statuses (Статусы)
- **id** (UUID, PK) - уникальный идентификатор статуса
- **name** (VARCHAR(50), NOT NULL) - название статуса
- **description** (TEXT) - описание статуса
- **color** (VARCHAR(7)) - цвет статуса (hex)

### 6. Reminders (Напоминания)
- **id** (UUID, PK) - уникальный идентификатор напоминания
- **task_id** (UUID, FK) - связанная задача
- **user_id** (UUID, FK) - пользователь
- **reminder_date** (TIMESTAMP, NOT NULL) - дата напоминания
- **message** (TEXT) - сообщение напоминания
- **is_sent** (BOOLEAN) - отправлено ли напоминание
- **sent_at** (TIMESTAMP) - дата отправки
- **created_at** (TIMESTAMP) - дата создания

### 7. Tags (Теги)
- **id** (UUID, PK) - уникальный идентификатор тега
- **name** (VARCHAR(50), NOT NULL) - название тега
- **color** (VARCHAR(7)) - цвет тега (hex)
- **user_id** (UUID, FK) - владелец тега (NULL для системных)
- **created_at** (TIMESTAMP) - дата создания

### 8. Task_Tags (Связь задач и тегов)
- **task_id** (UUID, FK) - идентификатор задачи
- **tag_id** (UUID, FK) - идентификатор тега
- **created_at** (TIMESTAMP) - дата создания связи

### 9. Task_History (История изменений задач)
- **id** (UUID, PK) - уникальный идентификатор записи
- **task_id** (UUID, FK) - идентификатор задачи
- **user_id** (UUID, FK) - пользователь, внесший изменения
- **field_name** (VARCHAR(50)) - название измененного поля
- **old_value** (TEXT) - старое значение
- **new_value** (TEXT) - новое значение
- **changed_at** (TIMESTAMP) - дата изменения

### 10. NLP_Commands (Команды NLP)
- **id** (UUID, PK) - уникальный идентификатор команды
- **user_id** (UUID, FK) - пользователь
- **command_text** (TEXT, NOT NULL) - текст команды
- **intent** (VARCHAR(100)) - намерение команды
- **entities** (JSONB) - извлеченные сущности
- **processed_at** (TIMESTAMP) - дата обработки
- **created_at** (TIMESTAMP) - дата создания

## Связи

### Основные связи:
1. **Users** → **Tasks** (1:N) - пользователь может иметь много задач
2. **Users** → **Categories** (1:N) - пользователь может создавать категории
3. **Users** → **Tags** (1:N) - пользователь может создавать теги
4. **Categories** → **Tasks** (1:N) - категория может содержать много задач
5. **Priorities** → **Tasks** (1:N) - приоритет может быть у многих задач
6. **Statuses** → **Tasks** (1:N) - статус может быть у многих задач
7. **Tasks** → **Reminders** (1:N) - задача может иметь много напоминаний
8. **Tasks** → **Task_History** (1:N) - задача может иметь много записей истории
9. **Tasks** ↔ **Tags** (N:M через Task_Tags) - задачи и теги связаны многие-ко-многим

### Индексы:
- **Users**: username, email
- **Tasks**: user_id, category_id, priority_id, status_id, due_date, created_at
- **Categories**: user_id, name
- **Tags**: user_id, name
- **Reminders**: task_id, user_id, reminder_date, is_sent
- **Task_History**: task_id, changed_at
- **NLP_Commands**: user_id, processed_at

## Нормализация

### Первая нормальная форма (1NF):
- Все атрибуты атомарны
- Нет повторяющихся групп
- Каждая запись уникальна

### Вторая нормальная форма (2NF):
- Все неключевые атрибуты полностью зависят от первичного ключа
- Вынесены справочники (Priorities, Statuses, Categories)

### Третья нормальная форма (3NF):
- Нет транзитивных зависимостей
- Справочники вынесены в отдельные таблицы
- Связи реализованы через внешние ключи 