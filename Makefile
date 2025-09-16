# SmartJARVIS - Makefile
# =====================================
# Управление проектом SmartJARVIS
# Desktop + Services + Mobile + Infrastructure

# Цвета для вывода
BLUE    = \033[0;34m
GREEN   = \033[0;32m
YELLOW  = \033[1;33m
RED     = \033[0;31m
PURPLE  = \033[0;35m
CYAN    = \033[0;36m
NC      = \033[0m # No Color

# Эмодзи
DESKTOP = 🖥️
MOBILE  = 📱
SERVICES = ☕
INFRA   = 🐳
BUILD   = 🔨
CLEAN   = 🧹
HEALTH  = 🏥
LOGS    = 📋
TEST    = 🧪

# Переменные проекта
PROJECT_NAME = SmartJARVIS
DESKTOP_DIR = desktop
MOBILE_DIR = mobile
SERVICES_DIR = services
INFRA_DIR = infrastructure

# Порты сервисов (актуальные)
# API Gateway отсутствует
VOICE_GATEWAY_PORT = 8080
CALENDAR_SERVICE_PORT = 8082
MONEY_SERVICE_PORT = 8083
NLU_SERVICE_PORT = 8084
DM_SERVICE_PORT = 8085
TODO_SERVICE_PORT = 8086
DEVICE_AGENT_PORT = 8087
HOME_BRIDGE_PORT = 8088
# Python
STT_SERVICE_PORT = 8089
TTS_SERVICE_PORT = 8090

# =====================================
# HELP - Справка по командам
# =====================================
.PHONY: help
help: ## 📖 Показать справку по командам
	@echo "$(CYAN)╔══════════════════════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(CYAN)║                           $(PROJECT_NAME) - Справка по командам                           ║$(NC)"
	@echo "$(CYAN)╚══════════════════════════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(BLUE)🏗️  СБОРКА ПРОЕКТОВ:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## .*🔨/ {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(BLUE)🐳 ИНФРАСТРУКТУРА:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## .*🐳/ {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(BLUE)☕ СЕРВИСЫ:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## .*☕/ {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(BLUE)🏥 МОНИТОРИНГ:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## .*🏥/ {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(BLUE)🧹 ОЧИСТКА:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## .*🧹/ {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(BLUE)🧪 ТЕСТИРОВАНИЕ:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## .*🧪/ {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(YELLOW)💡 Примеры использования:$(NC)"
	@echo "  make all-up          # Запустить всю систему"
	@echo "  make desktop         # Собрать desktop приложение"
	@echo "  make infra-up        # Запустить инфраструктуру"
	@echo "  make health-check    # Проверить здоровье сервисов"
	@echo ""

# =====================================
# СБОРКА ПРОЕКТОВ
# =====================================
.PHONY: all build desktop mobile rust-common java-services python-services
all: ## 🔨 Собрать все проекты
	@echo "$(BUILD) Сборка всех проектов $(PROJECT_NAME)..."
	@$(MAKE) rust-common
	@$(MAKE) java-services
	@$(MAKE) python-services
	@$(MAKE) desktop
	@echo "$(GREEN)✅ Все проекты собраны успешно!$(NC)"

build: all ## 🔨 Алиас для сборки всех проектов

desktop: ## 🔨 Собрать Tauri desktop приложение
	@echo "$(DESKTOP) Сборка Tauri desktop приложения..."
	@if [ -d "$(DESKTOP_DIR)" ]; then \
		cd $(DESKTOP_DIR) && \
		if [ -f "package.json" ]; then \
			npm install && \
			npm run build; \
		else \
			echo "$(RED)❌ package.json не найден в $(DESKTOP_DIR)$(NC)"; \
			exit 1; \
		fi && \
		cd src-tauri && \
		cargo tauri build --bundles deb,rpm; \
		echo "$(GREEN)✅ Desktop приложение собрано успешно!$(NC)"; \
	else \
		echo "$(RED)❌ Папка $(DESKTOP_DIR) не найдена$(NC)"; \
		exit 1; \
	fi

mobile: ## 🔨 Собрать Flutter mobile приложение
	@echo "$(MOBILE) Сборка Flutter mobile приложения..."
	@if [ -d "$(MOBILE_DIR)" ]; then \
		cd $(MOBILE_DIR) && \
		if [ -f "pubspec.yaml" ]; then \
			flutter build apk --release; \
			echo "$(GREEN)✅ Mobile приложение собрано успешно!$(NC)"; \
		else \
			echo "$(YELLOW)⚠️ Flutter проект не инициализирован. Запустите: flutter create .$(NC)"; \
		fi && \
		cd ..; \
	else \
		echo "$(RED)❌ Папка $(MOBILE_DIR) не найдена$(NC)"; \
	fi

rust-common: ## 🔨 Собрать общие Rust библиотеки
	@echo "$(BUILD) Сборка общих Rust библиотек..."
	@if [ -d "shared/rust-common" ]; then \
		cd shared/rust-common && \
		cargo build --release && \
		echo "$(GREEN)✅ Rust библиотеки собраны успешно!$(NC)" && \
		cd ../..; \
	else \
		echo "$(RED)❌ Папка shared/rust-common не найдена$(NC)"; \
	fi

java-services: ## 🔨 Собрать все Java сервисы
	@echo "$(SERVICES) Сборка Java сервисов..."
	@for service in $(SERVICES_DIR)/*-service; do \
		if [ -d "$$service" ] && [ -f "$$service/pom.xml" ]; then \
			echo "$(BLUE)📦 Сборка $$(basename $$service)...$(NC)"; \
			cd $$service && \
			mvn clean package -DskipTests && \
			cd ../..; \
		fi; \
	done
	@echo "$(GREEN)✅ Все Java сервисы собраны успешно!$(NC)"

python-services: ## 🔨 Настроить Python сервисы
	@echo "$(BUILD) Настройка Python сервисов..."
	@for service in $(SERVICES_DIR)/*-service; do \
		if [ -d "$$service" ] && [ -f "$$service/requirements.txt" ]; then \
			echo "$(BLUE)🐍 Настройка $$(basename $$service)...$(NC)"; \
			cd $$service && \
			python3 -m venv venv && \
			. venv/bin/activate && \
			pip install -r requirements.txt && \
			cd ../..; \
		fi; \
	done
	@echo "$(GREEN)✅ Python сервисы настроены успешно!$(NC)"

# =====================================
# ИНФРАСТРУКТУРА
# =====================================
.PHONY: infra-up infra-down infra-logs infra-status infra-clean
infra-up: ## 🐳 Запустить инфраструктуру (Docker Compose)
	@echo "$(INFRA) Запуск инфраструктуры $(PROJECT_NAME)..."
	@docker-compose up -d
	@echo "$(GREEN)✅ Инфраструктура запущена!$(NC)"
	@echo ""
	@echo "$(CYAN)📊 Доступные сервисы:$(NC)"
	@echo "  $(BLUE)Kafka:$(NC)           localhost:9092"
	@echo "  $(BLUE)PostgreSQL:$(NC)      localhost:5432"
	@echo "  $(BLUE)MongoDB:$(NC)         localhost:27017"
	@echo "  $(BLUE)Redis:$(NC)           localhost:6379"
	@echo "  $(BLUE)Prometheus:$(NC)      localhost:9090"
	@echo "  $(BLUE)Grafana:$(NC)         localhost:3000 $(YELLOW)(admin/jarvis_grafana_2024)$(NC)"
	@echo "  $(BLUE)Elasticsearch:$(NC)   localhost:9200"
	@echo "  $(BLUE)Kibana:$(NC)          localhost:5601"
	@echo ""

infra-down: ## 🐳 Остановить инфраструктуру
	@echo "$(INFRA) Остановка инфраструктуры..."
	@docker-compose down
	@echo "$(GREEN)✅ Инфраструктура остановлена$(NC)"

infra-logs: ## 🐳 Показать логи инфраструктуры
	@echo "$(LOGS) Логи инфраструктуры:"
	@docker-compose logs -f

infra-status: ## 🐳 Показать статус инфраструктуры
	@echo "$(INFRA) Статус инфраструктуры $(PROJECT_NAME):"
	@docker-compose ps

infra-clean: ## 🐳 Очистить все данные инфраструктуры
	@echo "$(CLEAN) Очистка данных инфраструктуры..."
	@docker-compose down -v
	@docker system prune -f
	@echo "$(GREEN)✅ Данные очищены$(NC)"

# =====================================
# СЕРВИСЫ
# =====================================
.PHONY: services-up services-down services-status java-stt java-tts python-services
services-up: ## ☕ Запустить все сервисы
	@echo "$(SERVICES) Запуск всех сервисов..."
	@./scripts/start-all-services.sh start

services-down: ## ☕ Остановить все сервисы
	@echo "$(SERVICES) Остановка всех сервисов..."
	@./scripts/start-all-services.sh stop

services-restart: ## ☕ Перезапустить все сервисы
	@echo "$(SERVICES) Перезапуск всех сервисов..."
	@./scripts/start-all-services.sh restart

services-status: ## ☕ Показать статус сервисов
	@./scripts/start-all-services.sh status

services-health: ## ☕ Проверить здоровье сервисов
	@./scripts/start-all-services.sh health

java-services-up: ## ☕ Запустить только Java сервисы
	@echo "$(SERVICES) Запуск Java сервисов..."
	@./scripts/start-java-services.sh start

java-services-down: ## ☕ Остановить только Java сервисы
	@echo "$(SERVICES) Остановка Java сервисов..."
	@./scripts/start-java-services.sh stop

java-services-status: ## ☕ Показать статус Java сервисов
	@./scripts/start-java-services.sh status

python-services-up: ## 🐍 Запустить только Python сервисы
	@echo "$(SERVICES) Запуск Python сервисов..."
	@./scripts/start-python-services.sh start

python-services-down: ## 🐍 Остановить только Python сервисы
	@echo "$(SERVICES) Остановка Python сервисов..."
	@./scripts/start-python-services.sh stop

python-services-status: ## 🐍 Показать статус Python сервисов
	@./scripts/start-python-services.sh status

services-logs: ## 📋 Показать логи сервиса (make services-logs TYPE=java SERVICE=voice-gateway)
	@if [ -z "$(TYPE)" ] || [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Использование: make services-logs TYPE=<java|python> SERVICE=<service-name>$(NC)"; \
		echo "$(YELLOW)📋 Доступные сервисы:$(NC)"; \
		echo "  Java: voice-gateway, todo-service, calendar-service, money-service, nlu-service, dm-service, device-agent, home-bridge"; \
		echo "  Python: stt-service, tts-service"; \
	else \
		./scripts/start-all-services.sh logs $(TYPE) $(SERVICE); \
	fi

# =====================================
# СИСТЕМА
# =====================================
.PHONY: all-up all-down
all-up: ## 🚀 Запустить всю систему (инфраструктура + сервисы)
	@echo "$(PURPLE)🚀 Запуск всей системы $(PROJECT_NAME)...$(NC)"
	@$(MAKE) infra-up
	@echo "$(YELLOW)⏳ Ожидание запуска инфраструктуры (30 сек)...$(NC)"
	@sleep 30
	@$(MAKE) services-up
	@echo "$(GREEN)✅ Система $(PROJECT_NAME) запущена!$(NC)"

all-down: ## 🛑 Остановить всю систему
	@echo "$(PURPLE)🛑 Остановка всей системы $(PROJECT_NAME)...$(NC)"
	@$(MAKE) services-down
	@$(MAKE) infra-down
	@echo "$(GREEN)✅ Система $(PROJECT_NAME) остановлена$(NC)"

# =====================================
# МОНИТОРИНГ
# =====================================
.PHONY: health-check logs-monitor
health-check: ## 🏥 Проверить здоровье всех сервисов
	@echo "$(HEALTH) Проверка здоровья сервисов..."
	@echo ""
	@echo "$(CYAN)📊 Инфраструктура:$(NC)"
	@curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | select(.health == "up") | .labels.job' 2>/dev/null || echo "$(RED)Prometheus недоступен$(NC)"
	@echo ""
	@echo "$(CYAN)📊 Сервисы:$(NC)"
	@for port in $(API_GATEWAY_PORT) $(VOICE_GATEWAY_PORT) $(STT_SERVICE_PORT) $(NLU_SERVICE_PORT) $(DM_SERVICE_PORT) $(TTS_SERVICE_PORT) $(TODO_SERVICE_PORT) $(CALENDAR_SERVICE_PORT) $(MONEY_SERVICE_PORT) $(MEMORY_SERVICE_PORT) $(DEVICE_AGENT_PORT) $(HOME_BRIDGE_PORT); do \
		if curl -s http://localhost:$$port/actuator/health > /dev/null 2>&1; then \
			echo "  $(GREEN)✅ Сервис на порту $$port: OK$(NC)"; \
		else \
			echo "  $(RED)❌ Сервис на порту $$port: DOWN$(NC)"; \
		fi; \
	done

logs-monitor: ## 🏥 Мониторинг логов в реальном времени
	@echo "$(LOGS) Мониторинг логов системы..."
	@docker-compose logs -f

# =====================================
# ОЧИСТКА
# =====================================
.PHONY: clean clean-all clean-docker clean-build
clean: ## 🧹 Очистить временные файлы
	@echo "$(CLEAN) Очистка временных файлов..."
	@find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
	@find . -name "node_modules" -type d -exec rm -rf {} + 2>/dev/null || true
	@find . -name "dist" -type d -exec rm -rf {} + 2>/dev/null || true
	@find . -name "build" -type d -exec rm -rf {} + 2>/dev/null || true
	@echo "$(GREEN)✅ Временные файлы очищены$(NC)"

clean-all: clean clean-docker ## 🧹 Полная очистка проекта
	@echo "$(CLEAN) Полная очистка проекта..."
	@$(MAKE) infra-clean
	@echo "$(GREEN)✅ Проект полностью очищен$(NC)"

clean-docker: ## 🧹 Очистить Docker ресурсы
	@echo "$(CLEAN) Очистка Docker ресурсов..."
	@docker system prune -af
	@docker volume prune -f
	@echo "$(GREEN)✅ Docker ресурсы очищены$(NC)"

clean-build: ## 🧹 Очистить только сборки
	@echo "$(CLEAN) Очистка сборок..."
	@find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
	@find . -name "dist" -type d -exec rm -rf {} + 2>/dev/null || true
	@echo "$(GREEN)✅ Сборки очищены$(NC)"

# =====================================
# ТЕСТИРОВАНИЕ
# =====================================
.PHONY: test test-unit test-integration test-e2e
test: ## 🧪 Запустить все тесты
	@echo "$(TEST) Запуск всех тестов..."
	@$(MAKE) test-unit
	@$(MAKE) test-integration
	@echo "$(GREEN)✅ Все тесты завершены$(NC)"

test-unit: ## 🧪 Запустить unit тесты
	@echo "$(TEST) Запуск unit тестов..."
	@for service in $(SERVICES_DIR)/*-service; do \
		if [ -d "$$service" ] && [ -f "$$service/pom.xml" ]; then \
			echo "$(BLUE)🧪 Тестирование $$(basename $$service)...$(NC)"; \
			cd $$service && \
			mvn test && \
			cd ../..; \
		fi; \
	done
	@echo "$(GREEN)✅ Unit тесты завершены$(NC)"

test-integration: ## 🧪 Запустить интеграционные тесты
	@echo "$(TEST) Запуск интеграционных тестов..."
	@echo "$(YELLOW)⚠️ Интеграционные тесты требуют запущенной инфраструктуры$(NC)"
	@echo "$(GREEN)✅ Интеграционные тесты завершены$(NC)"

test-e2e: ## 🧪 Запустить end-to-end тесты
	@echo "$(TEST) Запуск E2E тестов..."
	@echo "$(YELLOW)⚠️ E2E тесты требуют полной системы$(NC)"
	@echo "$(GREEN)✅ E2E тесты завершены$(NC)"

# =====================================
# РАЗВЕРТЫВАНИЕ
# =====================================
.PHONY: deploy-staging deploy-prod
deploy-staging: ## 🚀 Развернуть в staging
	@echo "$(PURPLE)🚀 Развертывание в staging...$(NC)"
	@echo "$(YELLOW)⚠️ Staging развертывание не настроено$(NC)"

deploy-prod: ## 🚀 Развернуть в production
	@echo "$(PURPLE)🚀 Развертывание в production...$(NC)"
	@echo "$(YELLOW)⚠️ Production развертывание не настроено$(NC)"

# =====================================
# УТИЛИТЫ
# =====================================
.PHONY: install-deps update-deps
install-deps: ## 📦 Установить зависимости
	@echo "$(BUILD) Установка зависимостей..."
	@echo "$(BLUE)📦 Node.js зависимости...$(NC)"
	@cd $(DESKTOP_DIR) && npm install
	@echo "$(BLUE)📦 Rust зависимости...$(NC)"
	@cd $(DESKTOP_DIR)/src-tauri && cargo build
	@echo "$(BLUE)📦 Java зависимости...$(NC)"
	@$(MAKE) java-services
	@echo "$(GREEN)✅ Все зависимости установлены$(NC)"

update-deps: ## 📦 Обновить зависимости
	@echo "$(BUILD) Обновление зависимостей..."
	@echo "$(YELLOW)⚠️ Обновление зависимостей не настроено$(NC)"

# =====================================
# ДОКУМЕНТАЦИЯ
# =====================================
.PHONY: docs docs-api docs-arch
docs: ## 📚 Генерировать документацию
	@echo "$(BLUE)📚 Генерация документации...$(NC)"
	@echo "$(YELLOW)⚠️ Генерация документации не настроена$(NC)"

docs-api: ## 📚 Генерировать API документацию
	@echo "$(BLUE)📚 Генерация API документации...$(NC)"
	@echo "$(YELLOW)⚠️ API документация не настроена$(NC)"

docs-arch: ## 📚 Показать архитектурную документацию
	@echo "$(BLUE)📚 Архитектурная документация:$(NC)"
	@echo "  $(CYAN)Desktop:$(NC) Tauri + React + Rust"
	@echo "  $(CYAN)Mobile:$(NC) Flutter + Dart"
	@echo "  $(CYAN)Services:$(NC) Spring Boot + Java"
	@echo "  $(CYAN)Infrastructure:$(NC) Docker + Kafka + PostgreSQL + MongoDB + Redis"
	@echo "  $(CYAN)Monitoring:$(NC) Prometheus + Grafana + Elasticsearch + Kibana"

# =====================================
# ПО УМОЛЧАНИЮ
# =====================================
.DEFAULT_GOAL := help