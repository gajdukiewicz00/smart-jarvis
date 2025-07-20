-- =====================================================
-- Схема базы данных SmartJARVIS для PostgreSQL
-- Нормализованная структура с индексами и ограничениями
-- =====================================================

-- Создание базы данных
CREATE DATABASE jarvis_db;
\c jarvis_db;

-- Включение расширений
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =====================================================
-- Таблица пользователей
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Индексы
    CONSTRAINT idx_users_username UNIQUE (username),
    CONSTRAINT idx_users_email UNIQUE (email)
);

CREATE INDEX idx_users_active ON users (is_active);
CREATE INDEX idx_users_created_at ON users (created_at);

-- =====================================================
-- Таблица приоритетов (справочник)
-- =====================================================
CREATE TABLE priorities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    level INTEGER NOT NULL CHECK (level BETWEEN 1 AND 4),
    color VARCHAR(7) DEFAULT '#000000',
    description TEXT,
    
    -- Индексы
    CONSTRAINT idx_priorities_level CHECK (level BETWEEN 1 AND 4)
);

CREATE INDEX idx_priorities_level ON priorities (level);
CREATE INDEX idx_priorities_name ON priorities (name);

-- =====================================================
-- Таблица статусов (справочник)
-- =====================================================
CREATE TABLE statuses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(7) DEFAULT '#000000'
);

CREATE INDEX idx_statuses_name ON statuses (name);

-- =====================================================
-- Таблица категорий
-- =====================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#000000',
    user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Внешние ключи
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Ограничения
    CONSTRAINT uk_category_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_categories_user_id ON categories (user_id);
CREATE INDEX idx_categories_name ON categories (name);
CREATE INDEX idx_categories_active ON categories (is_active);

-- =====================================================
-- Таблица тегов
-- =====================================================
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) DEFAULT '#000000',
    user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Внешние ключи
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Ограничения
    CONSTRAINT uk_tag_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_tags_user_id ON tags (user_id);
CREATE INDEX idx_tags_name ON tags (name);

-- =====================================================
-- Таблица задач
-- =====================================================
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    user_id UUID NOT NULL,
    category_id UUID,
    priority_id UUID,
    status_id UUID,
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_hours DECIMAL(5,2),
    actual_hours DECIMAL(5,2),
    
    -- Внешние ключи
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (priority_id) REFERENCES priorities(id) ON DELETE SET NULL,
    FOREIGN KEY (status_id) REFERENCES statuses(id) ON DELETE SET NULL,
    
    -- Ограничения
    CONSTRAINT chk_estimated_hours CHECK (estimated_hours >= 0),
    CONSTRAINT chk_actual_hours CHECK (actual_hours >= 0),
    CONSTRAINT chk_due_date CHECK (due_date IS NULL OR due_date >= created_at),
    CONSTRAINT chk_completed_date CHECK (completed_at IS NULL OR completed_at >= created_at)
);

-- Индексы для задач
CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_category_id ON tasks (category_id);
CREATE INDEX idx_tasks_priority_id ON tasks (priority_id);
CREATE INDEX idx_tasks_status_id ON tasks (status_id);
CREATE INDEX idx_tasks_due_date ON tasks (due_date);
CREATE INDEX idx_tasks_created_at ON tasks (created_at);
CREATE INDEX idx_tasks_completed_at ON tasks (completed_at);
CREATE INDEX idx_tasks_title ON tasks USING gin (to_tsvector('english', title));
CREATE INDEX idx_tasks_user_status ON tasks (user_id, status_id);
CREATE INDEX idx_tasks_user_due ON tasks (user_id, due_date);
CREATE INDEX idx_tasks_overdue ON tasks (due_date, status_id) WHERE due_date < CURRENT_TIMESTAMP;

-- =====================================================
-- Таблица связи задач и тегов (N:M)
-- =====================================================
CREATE TABLE task_tags (
    task_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Первичный ключ
    PRIMARY KEY (task_id, tag_id),
    
    -- Внешние ключи
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_tags_task_id ON task_tags (task_id);
CREATE INDEX idx_task_tags_tag_id ON task_tags (tag_id);

-- =====================================================
-- Таблица напоминаний
-- =====================================================
CREATE TABLE reminders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reminder_date TIMESTAMP NOT NULL,
    message TEXT,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Внешние ключи
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Ограничения
    CONSTRAINT chk_reminder_date CHECK (reminder_date >= created_at),
    CONSTRAINT chk_sent_at CHECK (sent_at IS NULL OR sent_at >= created_at)
);

CREATE INDEX idx_reminders_task_id ON reminders (task_id);
CREATE INDEX idx_reminders_user_id ON reminders (user_id);
CREATE INDEX idx_reminders_date ON reminders (reminder_date);
CREATE INDEX idx_reminders_sent ON reminders (is_sent);
CREATE INDEX idx_reminders_pending ON reminders (reminder_date, is_sent) WHERE is_sent = FALSE;

-- =====================================================
-- Таблица истории изменений задач
-- =====================================================
CREATE TABLE task_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Внешние ключи
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_history_task_id ON task_history (task_id);
CREATE INDEX idx_task_history_user_id ON task_history (user_id);
CREATE INDEX idx_task_history_changed_at ON task_history (changed_at);
CREATE INDEX idx_task_history_field ON task_history (field_name);

-- =====================================================
-- Таблица команд NLP
-- =====================================================
CREATE TABLE nlp_commands (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    command_text TEXT NOT NULL,
    intent VARCHAR(100),
    entities JSONB,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Внешние ключи
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_nlp_commands_user_id ON nlp_commands (user_id);
CREATE INDEX idx_nlp_commands_intent ON nlp_commands (intent);
CREATE INDEX idx_nlp_commands_processed_at ON nlp_commands (processed_at);
CREATE INDEX idx_nlp_commands_created_at ON nlp_commands (created_at);
CREATE INDEX idx_nlp_commands_entities ON nlp_commands USING GIN (entities);

-- =====================================================
-- Вставка начальных данных
-- =====================================================

-- Приоритеты
INSERT INTO priorities (name, level, color, description) VALUES
('LOW', 1, '#28a745', 'Низкий приоритет'),
('MEDIUM', 2, '#ffc107', 'Средний приоритет'),
('HIGH', 3, '#fd7e14', 'Высокий приоритет'),
('URGENT', 4, '#dc3545', 'Срочный приоритет');

-- Статусы
INSERT INTO statuses (name, color, description) VALUES
('PENDING', '#6c757d', 'Ожидает выполнения'),
('IN_PROGRESS', '#007bff', 'В работе'),
('PAUSED', '#ffc107', 'Приостановлено'),
('COMPLETED', '#28a745', 'Завершено'),
('CANCELLED', '#dc3545', 'Отменено');

-- Системные категории
INSERT INTO categories (name, description, color, user_id, is_active) VALUES
('Работа', 'Задачи, связанные с работой', '#007bff', NULL, TRUE),
('Личное', 'Личные задачи', '#28a745', NULL, TRUE),
('Учеба', 'Задачи, связанные с обучением', '#fd7e14', NULL, TRUE),
('Здоровье', 'Задачи, связанные со здоровьем', '#e83e8c', NULL, TRUE),
('Финансы', 'Финансовые задачи', '#20c997', NULL, TRUE);

-- Системные теги
INSERT INTO tags (name, color, user_id) VALUES
('важное', '#dc3545', NULL),
('срочно', '#fd7e14', NULL),
('проект', '#007bff', NULL),
('встреча', '#6f42c1', NULL),
('звонок', '#20c997', NULL);

-- =====================================================
-- Представления для удобства
-- =====================================================

-- Представление для просмотра задач с полной информацией
CREATE VIEW task_details AS
SELECT 
    t.id,
    t.title,
    t.description,
    t.due_date,
    t.created_at,
    t.updated_at,
    t.completed_at,
    t.estimated_hours,
    t.actual_hours,
    u.username as user_username,
    u.email as user_email,
    c.name as category_name,
    c.color as category_color,
    p.name as priority_name,
    p.level as priority_level,
    p.color as priority_color,
    s.name as status_name,
    s.color as status_color,
    CASE 
        WHEN t.due_date IS NULL THEN NULL
        WHEN t.due_date < CURRENT_TIMESTAMP AND s.name != 'COMPLETED' THEN 'OVERDUE'
        WHEN t.due_date <= CURRENT_TIMESTAMP + INTERVAL '1 day' AND s.name != 'COMPLETED' THEN 'DUE_SOON'
        ELSE 'ON_TIME'
    END as urgency_status
FROM tasks t
LEFT JOIN users u ON t.user_id = u.id
LEFT JOIN categories c ON t.category_id = c.id
LEFT JOIN priorities p ON t.priority_id = p.id
LEFT JOIN statuses s ON t.status_id = s.id;

-- Представление для просмотра задач с тегами
CREATE VIEW task_with_tags AS
SELECT 
    t.id,
    t.title,
    t.description,
    t.due_date,
    t.status_id,
    s.name as status_name,
    string_agg(tag.name, ', ') as tags
FROM tasks t
LEFT JOIN statuses s ON t.status_id = s.id
LEFT JOIN task_tags tt ON t.id = tt.task_id
LEFT JOIN tags tag ON tt.tag_id = tag.id
GROUP BY t.id, t.title, t.description, t.due_date, t.status_id, s.name;

-- Представление для просмотра статистики пользователя
CREATE VIEW user_statistics AS
SELECT 
    u.id as user_id,
    u.username,
    COUNT(t.id) as total_tasks,
    COUNT(CASE WHEN s.name = 'PENDING' THEN 1 END) as pending_tasks,
    COUNT(CASE WHEN s.name = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN s.name = 'COMPLETED' THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN t.due_date < CURRENT_TIMESTAMP AND s.name != 'COMPLETED' THEN 1 END) as overdue_tasks,
    AVG(t.estimated_hours) as avg_estimated_hours,
    AVG(t.actual_hours) as avg_actual_hours
FROM users u
LEFT JOIN tasks t ON u.id = t.user_id
LEFT JOIN statuses s ON t.status_id = s.id
GROUP BY u.id, u.username;

-- =====================================================
-- Функции для автоматического обновления
-- =====================================================

-- Функция для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггеры для автоматического обновления
CREATE TRIGGER tasks_update_trigger
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER categories_update_trigger
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Функция для записи истории изменений задач
CREATE OR REPLACE FUNCTION record_task_history()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.title != NEW.title THEN
        INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value)
        VALUES (NEW.id, NEW.user_id, 'title', OLD.title, NEW.title);
    END IF;
    
    IF OLD.description IS DISTINCT FROM NEW.description THEN
        INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value)
        VALUES (NEW.id, NEW.user_id, 'description', OLD.description, NEW.description);
    END IF;
    
    IF OLD.category_id IS DISTINCT FROM NEW.category_id THEN
        INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value)
        VALUES (NEW.id, NEW.user_id, 'category_id', OLD.category_id::text, NEW.category_id::text);
    END IF;
    
    IF OLD.priority_id IS DISTINCT FROM NEW.priority_id THEN
        INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value)
        VALUES (NEW.id, NEW.user_id, 'priority_id', OLD.priority_id::text, NEW.priority_id::text);
    END IF;
    
    IF OLD.status_id IS DISTINCT FROM NEW.status_id THEN
        INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value)
        VALUES (NEW.id, NEW.user_id, 'status_id', OLD.status_id::text, NEW.status_id::text);
    END IF;
    
    IF OLD.due_date IS DISTINCT FROM NEW.due_date THEN
        INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value)
        VALUES (NEW.id, NEW.user_id, 'due_date', OLD.due_date::text, NEW.due_date::text);
    END IF;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггер для записи истории изменений
CREATE TRIGGER tasks_history_trigger
    AFTER UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION record_task_history();

-- =====================================================
-- Дополнительные функции для работы с задачами
-- =====================================================

-- Функция для поиска задач по тексту
CREATE OR REPLACE FUNCTION search_tasks(search_term TEXT, user_id UUID)
RETURNS TABLE(
    id UUID,
    title VARCHAR(255),
    description TEXT,
    due_date TIMESTAMP,
    status_name VARCHAR(50),
    priority_name VARCHAR(50),
    category_name VARCHAR(100)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id,
        t.title,
        t.description,
        t.due_date,
        s.name as status_name,
        p.name as priority_name,
        c.name as category_name
    FROM tasks t
    LEFT JOIN statuses s ON t.status_id = s.id
    LEFT JOIN priorities p ON t.priority_id = p.id
    LEFT JOIN categories c ON t.category_id = c.id
    WHERE t.user_id = search_tasks.user_id
    AND (
        to_tsvector('english', t.title) @@ plainto_tsquery('english', search_term)
        OR to_tsvector('english', t.description) @@ plainto_tsquery('english', search_term)
    );
END;
$$ LANGUAGE plpgsql;

-- Функция для получения просроченных задач
CREATE OR REPLACE FUNCTION get_overdue_tasks(user_id UUID)
RETURNS TABLE(
    id UUID,
    title VARCHAR(255),
    due_date TIMESTAMP,
    days_overdue INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id,
        t.title,
        t.due_date,
        EXTRACT(DAY FROM (CURRENT_TIMESTAMP - t.due_date))::INTEGER as days_overdue
    FROM tasks t
    LEFT JOIN statuses s ON t.status_id = s.id
    WHERE t.user_id = get_overdue_tasks.user_id
    AND t.due_date < CURRENT_TIMESTAMP
    AND s.name != 'COMPLETED';
END;
$$ LANGUAGE plpgsql; 