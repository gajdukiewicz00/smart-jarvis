# SmartJARVIS - Makefile
# Управление микросервисами

.PHONY: help up down status logs build test clean grafana prometheus jaeger desktop mobile rust-common flutter-common

help: ## Показать справку
	@echo "SmartJARVIS - Команды управления"
	@echo ""
	@echo "Основные команды:"
	@echo "  up          - Поднять все микросервисы"
	@echo "  down        - Остановить все микросервисы"
	@echo "  status      - Проверить статус сервисов"
	@echo "  logs        - Показать логи всех сервисов"
	@echo ""
	@echo "Разработка:"
	@echo "  build       - Собрать все микросервисы"
	@echo "  test        - Запустить тесты"
	@echo "  clean       - Очистить артефакты сборки"
	@echo ""
	@echo "Multi-Platform:"
	@echo "  desktop     - Собрать Tauri desktop приложение"
	@echo "  mobile      - Собрать Flutter mobile приложение"
	@echo "  rust-common - Собрать общие Rust библиотеки"
	@echo "  flutter-common - Собрать общие Flutter библиотеки"
	@echo ""
	@echo "Мониторинг:"
	@echo "  grafana     - Открыть Grafana (http://localhost:3001)"
	@echo "  prometheus  - Открыть Prometheus (http://localhost:9090)"
	@echo "  jaeger      - Открыть Jaeger (http://localhost:16686)"

up: ## Поднять все микросервисы
	@echo "🚀 Запускаем SmartJARVIS микросервисы..."
	docker-compose -f infrastructure/docker/docker-compose.yml up -d
	@echo "✅ Все сервисы запущены!"
	@echo "🌐 Web UI: http://localhost:3000"
	@echo "📊 Grafana: http://localhost:3001"

down: ## Остановить все микросервисы
	@echo "🛑 Останавливаем SmartJARVIS микросервисы..."
	docker-compose -f infrastructure/docker/docker-compose.yml down
	@echo "✅ Все сервисы остановлены!"

status: ## Проверить статус сервисов
	@echo "📊 Статус SmartJARVIS микросервисов:"
	docker-compose -f infrastructure/docker/docker-compose.yml ps

logs: ## Показать логи всех сервисов
	@echo "📋 Логи SmartJARVIS микросервисов:"
	docker-compose -f infrastructure/docker/docker-compose.yml logs -f --tail=100

build: ## Собрать все микросервисы
	@echo "🔨 Сборка всех микросервисов..."
	@for service in services/*/; do \
		if [ -f "$$service/pom.xml" ]; then \
			echo "Building Java service: $$service"; \
			cd "$$service" && mvn clean package -DskipTests && cd ../..; \
		elif [ -f "$$service/requirements.txt" ]; then \
			echo "Building Python service: $$service"; \
			cd "$$service" && pip install -r requirements.txt && cd ../..; \
		elif [ -f "$$service/package.json" ]; then \
			echo "Building Node.js service: $$service"; \
			cd "$$service" && npm install && npm run build && cd ../..; \
		fi; \
	done
	@echo "✅ Все микросервисы собраны!"

test: ## Запустить тесты
	@echo "🧪 Запуск тестов всех микросервисов..."
	@for service in services/*/; do \
		if [ -f "$$service/pom.xml" ]; then \
			echo "Testing Java service: $$service"; \
			cd "$$service" && mvn test && cd ../..; \
		elif [ -f "$$service/requirements.txt" ]; then \
			echo "Testing Python service: $$service"; \
			cd "$$service" && python -m pytest && cd ../..; \
		elif [ -f "$$service/package.json" ]; then \
			echo "Testing Node.js service: $$service"; \
			cd "$$service" && npm test && cd ../..; \
		fi; \
	done
	@echo "✅ Все тесты пройдены!"

clean: ## Очистить артефакты сборки
	@echo "🧹 Очистка артефактов сборки..."
	find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
	find . -name "node_modules" -type d -exec rm -rf {} + 2>/dev/null || true
	find . -name "dist" -type d -exec rm -rf {} + 2>/dev/null || true
	find . -name "*.log" -delete 2>/dev/null || true
	find . -name "__pycache__" -type d -exec rm -rf {} + 2>/dev/null || true
	@echo "✅ Очистка завершена!"

grafana: ## Открыть Grafana
	@echo "📊 Открываем Grafana..."
	@command -v xdg-open >/dev/null 2>&1 && xdg-open http://localhost:3001 || \
	command -v open >/dev/null 2>&1 && open http://localhost:3001 || \
	echo "Откройте http://localhost:3001 в браузере"

prometheus: ## Открыть Prometheus
	@echo "📈 Открываем Prometheus..."
	@command -v xdg-open >/dev/null 2>&1 && xdg-open http://localhost:9090 || \
	command -v open >/dev/null 2>&1 && open http://localhost:9090 || \
	echo "Откройте http://localhost:9090 в браузере"

jaeger: ## Открыть Jaeger
	@echo "🔍 Открываем Jaeger..."
	@command -v xdg-open >/dev/null 2>&1 && xdg-open http://localhost:16686 || \
	command -v open >/dev/null 2>&1 && open http://localhost:16686 || \
	echo "Откройте http://localhost:16686 в браузере"

desktop: ## Собрать Tauri desktop приложение
	@echo "🖥️ Сборка Tauri desktop приложения..."
	@if [ -d "desktop" ]; then \
		cd desktop && \
		if [ -f "src-tauri/Cargo.toml" ]; then \
			cargo tauri build; \
		else \
			echo "⚠️ Tauri проект не инициализирован. Запустите: cargo tauri init"; \
		fi && \
		cd ..; \
	else \
		echo "❌ Папка desktop не найдена"; \
	fi

mobile: ## Собрать Flutter mobile приложение
	@echo "📱 Сборка Flutter mobile приложения..."
	@if [ -d "mobile" ]; then \
		cd mobile && \
		if [ -f "pubspec.yaml" ]; then \
			flutter build apk --release; \
		else \
			echo "⚠️ Flutter проект не инициализирован. Запустите: flutter create mobile"; \
		fi && \
		cd ..; \
	else \
		echo "❌ Папка mobile не найдена"; \
	fi

rust-common: ## Собрать общие Rust библиотеки
	@echo "🦀 Сборка общих Rust библиотек..."
	@if [ -d "shared/rust-common" ]; then \
		cd shared/rust-common && \
		if [ -f "Cargo.toml" ]; then \
			cargo build --release; \
		else \
			echo "⚠️ Rust библиотека не инициализирована"; \
		fi && \
		cd ../..; \
	else \
		echo "❌ Папка shared/rust-common не найдена"; \
	fi

flutter-common: ## Собрать общие Flutter библиотеки
	@echo "📦 Сборка общих Flutter библиотек..."
	@if [ -d "shared/flutter-common" ]; then \
		cd shared/flutter-common && \
		if [ -f "pubspec.yaml" ]; then \
			flutter packages get && flutter analyze; \
		else \
			echo "⚠️ Flutter библиотека не инициализирована"; \
		fi && \
		cd ../..; \
	else \
		echo "❌ Папка shared/flutter-common не найдена"; \
	fi