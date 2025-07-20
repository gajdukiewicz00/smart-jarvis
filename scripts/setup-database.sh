#!/bin/bash

# =====================================================
# Скрипт настройки базы данных SmartJARVIS
# Автоматическая установка и инициализация PostgreSQL
# =====================================================

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функции для вывода
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверка наличия PostgreSQL
check_postgresql() {
    print_info "Проверка установки PostgreSQL..."
    
    if command -v psql &> /dev/null; then
        print_success "PostgreSQL найден"
        psql --version
    else
        print_error "PostgreSQL не установлен"
        print_info "Установка PostgreSQL..."
        
        # Определение дистрибутива
        if [ -f /etc/debian_version ]; then
            # Ubuntu/Debian
            sudo apt update
            sudo apt install -y postgresql postgresql-contrib
        elif [ -f /etc/redhat-release ]; then
            # CentOS/RHEL
            sudo yum install -y postgresql postgresql-server postgresql-contrib
            sudo postgresql-setup initdb
            sudo systemctl enable postgresql
            sudo systemctl start postgresql
        elif command -v pacman &> /dev/null; then
            # Arch Linux
            sudo pacman -S postgresql
            sudo -u postgres initdb -D /var/lib/postgres/data
            sudo systemctl enable postgresql
            sudo systemctl start postgresql
        else
            print_error "Неизвестный дистрибутив Linux"
            exit 1
        fi
    fi
}

# Запуск PostgreSQL
start_postgresql() {
    print_info "Запуск PostgreSQL..."
    
    if sudo systemctl is-active --quiet postgresql; then
        print_success "PostgreSQL уже запущен"
    else
        sudo systemctl start postgresql
        print_success "PostgreSQL запущен"
    fi
}

# Создание пользователя и базы данных
setup_database() {
    print_info "Настройка базы данных..."
    
    # Создание пользователя jarvis
    sudo -u postgres psql -c "SELECT 1 FROM pg_roles WHERE rolname='jarvis'" | grep -q 1 || {
        print_info "Создание пользователя jarvis..."
        sudo -u postgres psql -c "CREATE USER jarvis WITH PASSWORD 'jarvis_password';"
    }
    
    # Создание базы данных
    sudo -u postgres psql -c "SELECT 1 FROM pg_database WHERE datname='jarvis_db'" | grep -q 1 || {
        print_info "Создание базы данных jarvis_db..."
        sudo -u postgres psql -c "CREATE DATABASE jarvis_db OWNER jarvis;"
    }
    
    # Предоставление прав
    sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE jarvis_db TO jarvis;"
    sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO jarvis;"
    sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO jarvis;"
    
    print_success "База данных настроена"
}

# Применение схемы
apply_schema() {
    print_info "Применение схемы базы данных..."
    
    # Проверка наличия файлов схемы
    if [ ! -f "db/schema-postgresql.sql" ]; then
        print_error "Файл схемы db/schema-postgresql.sql не найден"
        exit 1
    fi
    
    # Применение схемы
    PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -f db/schema-postgresql.sql
    
    print_success "Схема применена"
}

# Инициализация тестовых данных
init_test_data() {
    print_info "Инициализация тестовых данных..."
    
    # Проверка наличия файла инициализации
    if [ ! -f "db/init.sql" ]; then
        print_error "Файл инициализации db/init.sql не найден"
        exit 1
    fi
    
    # Применение тестовых данных
    PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -f db/init.sql
    
    print_success "Тестовые данные инициализированы"
}

# Проверка подключения
test_connection() {
    print_info "Проверка подключения к базе данных..."
    
    # Проверка подключения
    if PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -c "SELECT 1;" &> /dev/null; then
        print_success "Подключение к базе данных успешно"
    else
        print_error "Ошибка подключения к базе данных"
        exit 1
    fi
    
    # Проверка таблиц
    print_info "Проверка созданных таблиц..."
    PGPASSWORD=jarvis_password psql -h localhost -U jarvis -d jarvis_db -c "
    SELECT 
        schemaname,
        tablename,
        tableowner
    FROM pg_tables 
    WHERE schemaname = 'public'
    ORDER BY tablename;
    "
}

# Создание конфигурационного файла
create_config() {
    print_info "Создание конфигурационного файла..."
    
    cat > config/database.properties << EOF
# Конфигурация базы данных SmartJARVIS
db.url=jdbc:postgresql://localhost:5432/jarvis_db
db.username=jarvis
db.password=jarvis_password
db.driver=org.postgresql.Driver

# Настройки пула соединений
db.pool.initialSize=5
db.pool.maxSize=20
db.pool.minIdle=5
db.pool.maxIdle=10

# Настройки производительности
db.pool.timeout=30000
db.pool.validationQuery=SELECT 1
db.pool.testOnBorrow=true
db.pool.testOnReturn=false
db.pool.testWhileIdle=true
EOF

    print_success "Конфигурационный файл создан: config/database.properties"
}

# Основная функция
main() {
    print_info "Настройка базы данных SmartJARVIS"
    print_info "=================================="
    
    # Проверка и установка PostgreSQL
    check_postgresql
    
    # Запуск PostgreSQL
    start_postgresql
    
    # Настройка базы данных
    setup_database
    
    # Применение схемы
    apply_schema
    
    # Инициализация тестовых данных
    init_test_data
    
    # Проверка подключения
    test_connection
    
    # Создание конфигурации
    create_config
    
    print_success "Настройка базы данных завершена успешно!"
    print_info "Данные для подключения:"
    print_info "  Хост: localhost"
    print_info "  Порт: 5432"
    print_info "  База данных: jarvis_db"
    print_info "  Пользователь: jarvis"
    print_info "  Пароль: jarvis_password"
}

# Обработка аргументов командной строки
case "${1:-}" in
    --help|-h)
        echo "Использование: $0 [опции]"
        echo "Опции:"
        echo "  --help, -h     Показать эту справку"
        echo "  --schema-only  Применить только схему без тестовых данных"
        echo "  --test-only    Только проверить подключение"
        exit 0
        ;;
    --schema-only)
        check_postgresql
        start_postgresql
        setup_database
        apply_schema
        test_connection
        print_success "Схема применена без тестовых данных"
        exit 0
        ;;
    --test-only)
        test_connection
        exit 0
        ;;
    "")
        main
        ;;
    *)
        print_error "Неизвестная опция: $1"
        echo "Используйте --help для получения справки"
        exit 1
        ;;
esac 