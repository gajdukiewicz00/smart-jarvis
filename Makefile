# SmartJARVIS Development Makefile
.PHONY: help setup lint test build docker-build docker-up docker-down clean

# Default target
help:
	@echo "SmartJARVIS Development Commands"
	@echo "================================"
	@echo ""
	@echo "Setup & Dependencies:"
	@echo "  setup          Install all dependencies (Java/Node/Python)"
	@echo "  setup-java     Install Java dependencies (Maven)"
	@echo "  setup-node     Install Node.js dependencies"
	@echo "  setup-python   Install Python dependencies"
	@echo ""
	@echo "Code Quality:"
	@echo "  lint           Run all linters (Java/Node/Python)"
	@echo "  lint-java      Run Java linters (checkstyle)"
	@echo "  lint-node      Run Node.js linters (eslint)"
	@echo "  lint-python    Run Python linters (ruff, flake8)"
	@echo ""
	@echo "Testing:"
	@echo "  test           Run all tests"
	@echo "  test-java      Run Java tests (Maven)"
	@echo "  test-node      Run Node.js tests (npm)"
	@echo "  test-python    Run Python tests (pytest)"
	@echo ""
	@echo "Building:"
	@echo "  build          Build all services"
	@echo "  build-java     Build Java services"
	@echo "  build-node     Build Node.js services"
	@echo "  build-python   Build Python services"
	@echo ""
	@echo "Docker:"
	@echo "  docker-build   Build all Docker images"
	@echo "  docker-up      Start all services with Docker Compose"
	@echo "  docker-down    Stop all Docker services"
	@echo "  docker-logs    Show logs for all services"
	@echo ""
	@echo "Utilities:"
	@echo "  clean          Clean build artifacts"
	@echo "  autodev        Run AutoDev with docs scope"
	@echo "  autodev-all    Run AutoDev with all scope"

# Setup targets
setup: setup-java setup-node setup-python
	@echo "✅ All dependencies installed"

setup-java:
	@echo "📦 Installing Java dependencies..."
	mvn clean install -DskipTests

setup-node:
	@echo "📦 Installing Node.js dependencies..."
	cd nlp-engine && npm ci

setup-python:
	@echo "📦 Installing Python dependencies..."
	cd speech-service && pip install -r requirements.txt

# Linting targets
lint: lint-java lint-node lint-python
	@echo "✅ All linters passed"

lint-java:
	@echo "🔍 Running Java linters..."
	mvn checkstyle:check

lint-node:
	@echo "🔍 Running Node.js linters..."
	cd nlp-engine && npm run lint

lint-python:
	@echo "🔍 Running Python linters..."
	cd speech-service && ruff check . && flake8 .

# Testing targets
test: test-java test-node test-python
	@echo "✅ All tests passed"

test-java:
	@echo "🧪 Running Java tests..."
	mvn test

test-node:
	@echo "🧪 Running Node.js tests..."
	cd nlp-engine && npm test

test-python:
	@echo "🧪 Running Python tests..."
	cd speech-service && pytest -q

# Building targets
build: build-java build-node build-python
	@echo "✅ All services built"

build-java:
	@echo "🔨 Building Java services..."
	mvn package -DskipTests

build-node:
	@echo "🔨 Building Node.js services..."
	cd nlp-engine && npm run build

build-python:
	@echo "🔨 Building Python services..."
	cd speech-service && python -m py_compile main.py

# Docker targets
docker-build:
	@echo "🐳 Building Docker images..."
	docker build -t speech-service:dev speech-service/
	docker build -t nlp-engine:dev nlp-engine/
	docker build -t jarvis-desktop:dev jarvis-desktop/
	docker build -t task-service:dev task-service/

docker-up:
	@echo "🐳 Starting Docker services..."
	docker compose up -d

docker-down:
	@echo "🐳 Stopping Docker services..."
	docker compose down

docker-logs:
	@echo "📋 Showing Docker logs..."
	docker compose logs -f

# Utility targets
clean:
	@echo "🧹 Cleaning build artifacts..."
	mvn clean
	cd nlp-engine && rm -rf dist/ node_modules/.cache/
	cd speech-service && find . -type d -name __pycache__ -exec rm -rf {} + 2>/dev/null || true
	find . -name "*.pyc" -delete 2>/dev/null || true

# AutoDev targets
autodev:
	@echo "🤖 Running AutoDev with docs scope..."
	GH_TOKEN=${GH_TOKEN} ./scripts/gh-dispatch-autodev.sh docs

autodev-all:
	@echo "🤖 Running AutoDev with all scope..."
	GH_TOKEN=${GH_TOKEN} ./scripts/gh-dispatch-autodev.sh all

# Health check targets
health:
	@echo "🏥 Checking service health..."
	@curl -s http://localhost:8081/actuator/health || echo "❌ Task Service not responding"
	@curl -s http://localhost:8082/health || echo "❌ NLP Engine not responding"
	@curl -s http://localhost:8083/health || echo "❌ Speech Service not responding"

# Development workflow
dev: setup lint test build
	@echo "🚀 Development workflow completed successfully!"
