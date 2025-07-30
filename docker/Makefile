# SmartJARVIS Docker Management Makefile

.PHONY: help start stop restart rebuild logs status clean force-clean health check

# Default target
help:
	@echo "SmartJARVIS Docker Management"
	@echo ""
	@echo "Available commands:"
	@echo "  make start        - Start all services"
	@echo "  make stop         - Stop all services"
	@echo "  make restart      - Restart all services"
	@echo "  make rebuild      - Rebuild all services"
	@echo "  make logs         - Show logs for all services"
	@echo "  make status       - Show status of all services"
	@echo "  make clean        - Stop and remove all containers and volumes"
	@echo "  make force-clean  - Force clean all containers, networks and volumes"
	@echo "  make health       - Check health of all services"
	@echo "  make check        - Check if all services are running"
	@echo ""

# Start all services
start:
	@echo "🚀 Starting SmartJARVIS..."
	@./scripts/start.sh

# Stop all services
stop:
	@echo "🛑 Stopping SmartJARVIS..."
	@./scripts/stop.sh

# Restart all services
restart: stop
	@echo "🔄 Restarting SmartJARVIS..."
	@./scripts/start.sh

# Rebuild all services
rebuild:
	@echo "🔨 Rebuilding SmartJARVIS..."
	@./scripts/rebuild.sh

# Show logs
logs:
	@echo "📝 Showing logs..."
	@docker compose logs -f

# Show status
status:
	@echo "📊 Service Status:"
	@docker compose ps

# Clean everything
clean:
	@echo "🧹 Cleaning up everything..."
	@docker compose down -v --rmi all --remove-orphans 2>/dev/null || true
	@docker system prune -f
	@echo "✅ Cleanup completed"

# Force clean everything
force-clean:
	@echo "🧹 Force cleaning everything..."
	@./scripts/force-clean.sh

# Check health
health:
	@echo "🏥 Health Check:"
	@echo "Task Service:"
	@curl -s http://localhost:8081/actuator/health || echo "❌ Task Service not responding"
	@echo ""
	@echo "NLP Engine:"
	@curl -s http://localhost:8082/api/health || echo "❌ NLP Engine not responding"
	@echo ""
	@echo "Speech Service:"
	@curl -s http://localhost:8083/health || echo "❌ Speech Service not responding"
	@echo ""
	@echo "Gateway:"
	@curl -s http://localhost:8080/actuator/health || echo "❌ Gateway not responding"
	@echo ""

# Check if services are running
check:
	@echo "🔍 Checking service status..."
	@docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

# Individual service commands
task-service:
	@docker compose up -d task-service

nlp-engine:
	@docker compose up -d nlp-engine

speech-service:
	@docker compose up -d speech-service

gateway:
	@docker compose up -d gateway

# Infrastructure services
infrastructure:
	@docker compose up -d redis postgres

monitoring:
	@docker compose up -d prometheus grafana

# Development commands
dev:
	@echo "🔧 Starting development environment..."
	@docker compose up -d redis postgres
	@echo "✅ Infrastructure services started"
	@echo "🌐 Connect to:"
	@echo "   PostgreSQL: localhost:5432"
	@echo "   Redis: localhost:6379"

# Production commands
prod:
	@echo "🚀 Starting production environment..."
	@docker compose --profile production up -d

# Backup commands
backup:
	@echo "💾 Creating backup..."
	@docker compose exec postgres pg_dump -U jarvis_user jarvis_db > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "✅ Backup created"

# Restore commands
restore:
	@echo "📥 Restoring from backup..."
	@if [ -z "$(FILE)" ]; then \
		echo "❌ Please specify backup file: make restore FILE=backup.sql"; \
		exit 1; \
	fi
	@docker compose exec -T postgres psql -U jarvis_user jarvis_db < $(FILE)
	@echo "✅ Restore completed"

# Logs for specific services
logs-task:
	@docker compose logs -f task-service

logs-nlp:
	@docker compose logs -f nlp-engine

logs-speech:
	@docker compose logs -f speech-service

logs-gateway:
	@docker compose logs -f gateway

# Shell access
shell-task:
	@docker compose exec task-service bash

shell-nlp:
	@docker compose exec nlp-engine sh

shell-speech:
	@docker compose exec speech-service bash

shell-gateway:
	@docker compose exec gateway bash 