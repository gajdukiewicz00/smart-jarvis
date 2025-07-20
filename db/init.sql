-- =====================================================
-- Скрипт инициализации базы данных SmartJARVIS
-- Создание тестовых пользователей и данных
-- =====================================================

-- Создание тестового пользователя
INSERT INTO users (username, email, password_hash, first_name, last_name) VALUES
('admin', 'admin@jarvis.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Администратор', 'Системы'),
('user1', 'user1@jarvis.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Иван', 'Петров'),
('user2', 'user2@jarvis.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Мария', 'Сидорова');

-- Создание пользовательских категорий
INSERT INTO categories (name, description, color, user_id, is_active) VALUES
('Проект А', 'Задачи по проекту А', '#ff6b6b', (SELECT id FROM users WHERE username = 'user1'), TRUE),
('Встречи', 'Задачи, связанные с встречами', '#4ecdc4', (SELECT id FROM users WHERE username = 'user1'), TRUE),
('Домашние дела', 'Домашние задачи', '#45b7d1', (SELECT id FROM users WHERE username = 'user1'), TRUE),
('Разработка', 'Задачи по разработке', '#96ceb4', (SELECT id FROM users WHERE username = 'user2'), TRUE),
('Тестирование', 'Задачи по тестированию', '#feca57', (SELECT id FROM users WHERE username = 'user2'), TRUE);

-- Создание пользовательских тегов
INSERT INTO tags (name, color, user_id) VALUES
('frontend', '#ff6b6b', (SELECT id FROM users WHERE username = 'user1')),
('backend', '#4ecdc4', (SELECT id FROM users WHERE username = 'user1')),
('bug', '#ff9ff3', (SELECT id FROM users WHERE username = 'user1')),
('feature', '#54a0ff', (SELECT id FROM users WHERE username = 'user2')),
('documentation', '#5f27cd', (SELECT id FROM users WHERE username = 'user2'));

-- Создание тестовых задач для user1
INSERT INTO tasks (title, description, user_id, category_id, priority_id, status_id, due_date, estimated_hours) VALUES
('Создать макет главной страницы', 'Разработать дизайн главной страницы сайта', 
 (SELECT id FROM users WHERE username = 'user1'),
 (SELECT id FROM categories WHERE name = 'Проект А' AND user_id = (SELECT id FROM users WHERE username = 'user1')),
 (SELECT id FROM priorities WHERE name = 'HIGH'),
 (SELECT id FROM statuses WHERE name = 'IN_PROGRESS'),
 CURRENT_TIMESTAMP + INTERVAL '3 days', 8.0),

('Подготовить презентацию для клиента', 'Создать презентацию по результатам работы',
 (SELECT id FROM users WHERE username = 'user1'),
 (SELECT id FROM categories WHERE name = 'Встречи' AND user_id = (SELECT id FROM users WHERE username = 'user1')),
 (SELECT id FROM priorities WHERE name = 'URGENT'),
 (SELECT id FROM statuses WHERE name = 'PENDING'),
 CURRENT_TIMESTAMP + INTERVAL '1 day', 4.0),

('Исправить баг в форме регистрации', 'Найти и исправить ошибку в валидации формы',
 (SELECT id FROM users WHERE username = 'user1'),
 (SELECT id FROM categories WHERE name = 'Проект А' AND user_id = (SELECT id FROM users WHERE username = 'user1')),
 (SELECT id FROM priorities WHERE name = 'MEDIUM'),
 (SELECT id FROM statuses WHERE name = 'PENDING'),
 CURRENT_TIMESTAMP + INTERVAL '5 days', 3.0),

('Купить продукты', 'Молоко, хлеб, яйца, овощи',
 (SELECT id FROM users WHERE username = 'user1'),
 (SELECT id FROM categories WHERE name = 'Домашние дела' AND user_id = (SELECT id FROM users WHERE username = 'user1')),
 (SELECT id FROM priorities WHERE name = 'LOW'),
 (SELECT id FROM statuses WHERE name = 'PENDING'),
 CURRENT_TIMESTAMP + INTERVAL '2 days', 1.0),

('Позвонить в техподдержку', 'Уточнить детали по API',
 (SELECT id FROM users WHERE username = 'user1'),
 (SELECT id FROM categories WHERE name = 'Встречи' AND user_id = (SELECT id FROM users WHERE username = 'user1')),
 (SELECT id FROM priorities WHERE name = 'HIGH'),
 (SELECT id FROM statuses WHERE name = 'IN_PROGRESS'),
 CURRENT_TIMESTAMP + INTERVAL '6 hours', 0.5);

-- Создание тестовых задач для user2
INSERT INTO tasks (title, description, user_id, category_id, priority_id, status_id, due_date, estimated_hours) VALUES
('Написать документацию API', 'Создать полную документацию для REST API',
 (SELECT id FROM users WHERE username = 'user2'),
 (SELECT id FROM categories WHERE name = 'Разработка' AND user_id = (SELECT id FROM users WHERE username = 'user2')),
 (SELECT id FROM priorities WHERE name = 'HIGH'),
 (SELECT id FROM statuses WHERE name = 'IN_PROGRESS'),
 CURRENT_TIMESTAMP + INTERVAL '4 days', 12.0),

('Провести unit-тестирование', 'Написать тесты для модуля аутентификации',
 (SELECT id FROM users WHERE username = 'user2'),
 (SELECT id FROM categories WHERE name = 'Тестирование' AND user_id = (SELECT id FROM users WHERE username = 'user2')),
 (SELECT id FROM priorities WHERE name = 'MEDIUM'),
 (SELECT id FROM statuses WHERE name = 'PENDING'),
 CURRENT_TIMESTAMP + INTERVAL '7 days', 6.0),

('Оптимизировать запросы к БД', 'Улучшить производительность запросов',
 (SELECT id FROM users WHERE username = 'user2'),
 (SELECT id FROM categories WHERE name = 'Разработка' AND user_id = (SELECT id FROM users WHERE username = 'user2')),
 (SELECT id FROM priorities WHERE name = 'LOW'),
 (SELECT id FROM statuses WHERE name = 'PENDING'),
 CURRENT_TIMESTAMP + INTERVAL '10 days', 8.0),

('Создать отчет о тестировании', 'Подготовить отчет по результатам тестирования',
 (SELECT id FROM users WHERE username = 'user2'),
 (SELECT id FROM categories WHERE name = 'Тестирование' AND user_id = (SELECT id FROM users WHERE username = 'user2')),
 (SELECT id FROM priorities WHERE name = 'HIGH'),
 (SELECT id FROM statuses WHERE name = 'COMPLETED'),
 CURRENT_TIMESTAMP - INTERVAL '1 day', 3.0);

-- Связывание задач с тегами
INSERT INTO task_tags (task_id, tag_id) VALUES
-- Задачи user1
((SELECT id FROM tasks WHERE title = 'Создать макет главной страницы'), (SELECT id FROM tags WHERE name = 'frontend' AND user_id = (SELECT id FROM users WHERE username = 'user1'))),
((SELECT id FROM tasks WHERE title = 'Исправить баг в форме регистрации'), (SELECT id FROM tags WHERE name = 'bug' AND user_id = (SELECT id FROM users WHERE username = 'user1'))),
((SELECT id FROM tasks WHERE title = 'Исправить баг в форме регистрации'), (SELECT id FROM tags WHERE name = 'frontend' AND user_id = (SELECT id FROM users WHERE username = 'user1'))),

-- Задачи user2
((SELECT id FROM tasks WHERE title = 'Написать документацию API'), (SELECT id FROM tags WHERE name = 'documentation' AND user_id = (SELECT id FROM users WHERE username = 'user2'))),
((SELECT id FROM tasks WHERE title = 'Провести unit-тестирование'), (SELECT id FROM tags WHERE name = 'feature' AND user_id = (SELECT id FROM users WHERE username = 'user2'))),
((SELECT id FROM tasks WHERE title = 'Оптимизировать запросы к БД'), (SELECT id FROM tags WHERE name = 'backend' AND user_id = (SELECT id FROM users WHERE username = 'user1')));

-- Создание напоминаний
INSERT INTO reminders (task_id, user_id, reminder_date, message) VALUES
((SELECT id FROM tasks WHERE title = 'Подготовить презентацию для клиента'), 
 (SELECT id FROM users WHERE username = 'user1'),
 CURRENT_TIMESTAMP + INTERVAL '12 hours',
 'Не забудьте подготовить презентацию для клиента завтра!'),

((SELECT id FROM tasks WHERE title = 'Позвонить в техподдержку'), 
 (SELECT id FROM users WHERE username = 'user1'),
 CURRENT_TIMESTAMP + INTERVAL '2 hours',
 'Напоминание: позвонить в техподдержку'),

((SELECT id FROM tasks WHERE title = 'Написать документацию API'), 
 (SELECT id FROM users WHERE username = 'user2'),
 CURRENT_TIMESTAMP + INTERVAL '1 day',
 'Документация API должна быть готова через день'),

((SELECT id FROM tasks WHERE title = 'Провести unit-тестирование'), 
 (SELECT id FROM users WHERE username = 'user2'),
 CURRENT_TIMESTAMP + INTERVAL '3 days',
 'Начать написание unit-тестов');

-- Создание записей истории изменений
INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value) VALUES
((SELECT id FROM tasks WHERE title = 'Создать макет главной страницы'),
 (SELECT id FROM users WHERE username = 'user1'),
 'status_id',
 (SELECT id FROM statuses WHERE name = 'PENDING')::text,
 (SELECT id FROM statuses WHERE name = 'IN_PROGRESS')::text),

((SELECT id FROM tasks WHERE title = 'Создать отчет о тестировании'),
 (SELECT id FROM users WHERE username = 'user2'),
 'status_id',
 (SELECT id FROM statuses WHERE name = 'IN_PROGRESS')::text,
 (SELECT id FROM statuses WHERE name = 'COMPLETED')::text),

((SELECT id FROM tasks WHERE title = 'Исправить баг в форме регистрации'),
 (SELECT id FROM users WHERE username = 'user1'),
 'priority_id',
 (SELECT id FROM priorities WHERE name = 'LOW')::text,
 (SELECT id FROM priorities WHERE name = 'MEDIUM')::text);

-- Создание тестовых NLP команд
INSERT INTO nlp_commands (user_id, command_text, intent, entities, processed_at) VALUES
((SELECT id FROM users WHERE username = 'user1'),
 'Создай задачу купить продукты на завтра',
 'CREATE_TASK',
 '{"task": "купить продукты", "due_date": "tomorrow"}',
 CURRENT_TIMESTAMP),

((SELECT id FROM users WHERE username = 'user1'),
 'Покажи все просроченные задачи',
 'SHOW_OVERDUE_TASKS',
 '{}',
 CURRENT_TIMESTAMP),

((SELECT id FROM users WHERE username = 'user2'),
 'Заверши задачу написать документацию',
 'COMPLETE_TASK',
 '{"task": "написать документацию"}',
 CURRENT_TIMESTAMP),

((SELECT id FROM users WHERE username = 'user2'),
 'Измени приоритет задачи оптимизировать запросы на высокий',
 'CHANGE_PRIORITY',
 '{"task": "оптимизировать запросы", "priority": "high"}',
 CURRENT_TIMESTAMP);

-- Обновление некоторых задач как просроченные
UPDATE tasks 
SET due_date = CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE title IN ('Купить продукты', 'Оптимизировать запросы к БД');

-- Обновление некоторых задач как выполненные
UPDATE tasks 
SET status_id = (SELECT id FROM statuses WHERE name = 'COMPLETED'),
    completed_at = CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE title = 'Создать отчет о тестировании';

-- Обновление некоторых задач как в работе
UPDATE tasks 
SET status_id = (SELECT id FROM statuses WHERE name = 'IN_PROGRESS')
WHERE title IN ('Создать макет главной страницы', 'Написать документацию API');

-- Обновление некоторых задач как приостановленные
UPDATE tasks 
SET status_id = (SELECT id FROM statuses WHERE name = 'PAUSED')
WHERE title = 'Оптимизировать запросы к БД';

-- Вывод статистики
SELECT 
    'База данных успешно инициализирована!' as status,
    COUNT(DISTINCT u.id) as total_users,
    COUNT(DISTINCT t.id) as total_tasks,
    COUNT(DISTINCT c.id) as total_categories,
    COUNT(DISTINCT tag.id) as total_tags,
    COUNT(DISTINCT r.id) as total_reminders
FROM users u
LEFT JOIN tasks t ON u.id = t.user_id
LEFT JOIN categories c ON u.id = c.user_id
LEFT JOIN tags tag ON u.id = tag.user_id
LEFT JOIN reminders r ON u.id = r.user_id;
