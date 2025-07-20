# Визуальная ER-диаграмма SmartJARVIS

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│     USERS       │         │     TASKS       │         │   CATEGORIES    │
├─────────────────┤         ├─────────────────┤         ├─────────────────┤
│ PK id (UUID)    │◄────────┤ FK user_id      │◄────────┤ PK id (UUID)    │
│ username (V50)  │         │ PK id (UUID)    │         │ name (V100)     │
│ email (V100)    │         │ title (V255)    │         │ description     │
│ password_hash   │         │ description     │         │ color (V7)      │
│ first_name      │         │ FK category_id  │         │ FK user_id      │
│ last_name       │         │ FK priority_id  │         │ created_at      │
│ created_at      │         │ FK status_id    │         │ is_active       │
│ updated_at      │         │ due_date        │         └─────────────────┘
│ is_active       │         │ created_at      │         ┌─────────────────┐
└─────────────────┘         │ updated_at      │         │   PRIORITIES    │
                            │ completed_at    │         ├─────────────────┤
                            │ estimated_hours │         │ PK id (UUID)    │
                            │ actual_hours    │         │ name (V50)      │
                            └─────────────────┘         │ level (INT)     │
                                   │                   │ color (V7)      │
                                   │                   │ description     │
                                   │                   └─────────────────┘
                                   │
                                   │                   ┌─────────────────┐
                                   │                   │    STATUSES     │
                                   │                   ├─────────────────┤
                                   │                   │ PK id (UUID)    │
                                   │                   │ name (V50)      │
                                   │                   │ description     │
                                   │                   │ color (V7)      │
                                   │                   └─────────────────┘
                                   │
                                   │                   ┌─────────────────┐
                                   │                   │      TAGS       │
                                   │                   ├─────────────────┤
                                   │                   │ PK id (UUID)    │
                                   │                   │ name (V50)      │
                                   │                   │ color (V7)      │
                                   │                   │ FK user_id      │
                                   │                   │ created_at      │
                                   │                   └─────────────────┘
                                   │                           │
                                   │                           │
                                   │                   ┌─────────────────┐
                                   │                   │   TASK_TAGS     │
                                   │                   ├─────────────────┤
                                   │                   │ FK task_id      │
                                   │                   │ FK tag_id       │
                                   │                   │ created_at      │
                                   │                   └─────────────────┘
                                   │
                                   │                   ┌─────────────────┐
                                   │                   │   REMINDERS     │
                                   │                   ├─────────────────┤
                                   │                   │ PK id (UUID)    │
                                   │                   │ FK task_id      │
                                   │                   │ FK user_id      │
                                   │                   │ reminder_date   │
                                   │                   │ message         │
                                   │                   │ is_sent         │
                                   │                   │ sent_at         │
                                   │                   │ created_at      │
                                   │                   └─────────────────┘
                                   │
                                   │                   ┌─────────────────┐
                                   │                   │ TASK_HISTORY    │
                                   │                   ├─────────────────┤
                                   │                   │ PK id (UUID)    │
                                   │                   │ FK task_id      │
                                   │                   │ FK user_id      │
                                   │                   │ field_name      │
                                   │                   │ old_value       │
                                   │                   │ new_value       │
                                   │                   │ changed_at      │
                                   │                   └─────────────────┘
                                   │
                                   │                   ┌─────────────────┐
                                   │                   │ NLP_COMMANDS    │
                                   │                   ├─────────────────┤
                                   │                   │ PK id (UUID)    │
                                   │                   │ FK user_id      │
                                   │                   │ command_text    │
                                   │                   │ intent          │
                                   │                   │ entities (JSONB)│
                                   │                   │ processed_at    │
                                   │                   │ created_at      │
                                   │                   └─────────────────┘
                                   │
                                   └───────────────────┘
```

## Связи между таблицами:

### 1. USERS → TASKS (1:N)
- Пользователь может иметь множество задач
- Каждая задача принадлежит одному пользователю

### 2. USERS → CATEGORIES (1:N)
- Пользователь может создавать множество категорий
- Категория может принадлежать одному пользователю (или быть системной)

### 3. USERS → TAGS (1:N)
- Пользователь может создавать множество тегов
- Тег может принадлежать одному пользователю (или быть системным)

### 4. CATEGORIES → TASKS (1:N)
- Категория может содержать множество задач
- Задача может принадлежать одной категории

### 5. PRIORITIES → TASKS (1:N)
- Приоритет может быть у множества задач
- Задача может иметь один приоритет

### 6. STATUSES → TASKS (1:N)
- Статус может быть у множества задач
- Задача может иметь один статус

### 7. TASKS ↔ TAGS (N:M через TASK_TAGS)
- Задача может иметь множество тегов
- Тег может быть у множества задач

### 8. TASKS → REMINDERS (1:N)
- Задача может иметь множество напоминаний
- Напоминание принадлежит одной задаче

### 9. TASKS → TASK_HISTORY (1:N)
- Задача может иметь множество записей истории
- Запись истории принадлежит одной задаче

### 10. USERS → NLP_COMMANDS (1:N)
- Пользователь может иметь множество NLP команд
- NLP команда принадлежит одному пользователю

## Индексы:

### Основные индексы:
- `users`: username, email, is_active
- `tasks`: user_id, category_id, priority_id, status_id, due_date, created_at
- `categories`: user_id, name, is_active
- `tags`: user_id, name
- `reminders`: task_id, user_id, reminder_date, is_sent
- `task_history`: task_id, user_id, changed_at
- `nlp_commands`: user_id, intent, processed_at

### Составные индексы:
- `tasks`: (user_id, status_id), (user_id, due_date)
- `reminders`: (reminder_date, is_sent) WHERE is_sent = FALSE

### Специальные индексы:
- `tasks`: GIN индекс для полнотекстового поиска по title
- `nlp_commands`: GIN индекс для JSONB поля entities 