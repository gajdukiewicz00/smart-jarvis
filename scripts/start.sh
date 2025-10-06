#!/usr/bin/env bash
set -euo pipefail

# Simple launcher for SmartJARVIS using Makefile targets
# Usage: ./scripts/start.sh [start|stop|status|restart|logs]

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

BLUE='\033[0;34m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info(){ echo -e "${GREEN}[$(date +%H:%M:%S)]${NC} $*"; }
warn(){ echo -e "${YELLOW}[$(date +%H:%M:%S)]${NC} $*"; }
err(){  echo -e "${RED}[$(date +%H:%M:%S)]${NC} $*"; }

start_all(){
  info "🏗️  Infrastructure up"
  make infra-up
  info "☕ Services up"
  make services-up
  info "🏥 Health check"
  make services-health || true
  info "📊 Grafana: http://localhost:3000  |  Prometheus: http://localhost:9090"
  info "🗄️  Schema Registry: http://localhost:8081  |  Kafka: localhost:9092"
}

stop_all(){
  warn "🛑 Stopping services"
  make services-down || true
  warn "🛑 Stopping infrastructure"
  make infra-down || true
}

status_all(){
  info "🏥 Health overview"
  make services-health || true
}

logs_follow(){
  local svc_type=${1:-}
  local svc_name=${2:-}
  if [[ -z "$svc_type" || -z "$svc_name" ]]; then
    err "Usage: $0 logs <java|python> <service-name>"; exit 1; fi
  make services-logs TYPE="$svc_type" SERVICE="$svc_name"
}

case "${1:-start}" in
  start)
    start_all
    ;;
  stop)
    stop_all
    ;;
  status)
    status_all
    ;;
  restart)
    stop_all
    sleep 2
    start_all
    ;;
  logs)
    shift || true
    logs_follow "$@"
    ;;
  *)
    echo "Usage: $0 {start|stop|status|restart|logs}"; exit 1;
    ;;
esac
