#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[start]${NC} $1"; }
warn() { echo -e "${YELLOW}[start]${NC} $1"; }
err()  { echo -e "${RED}[start]${NC} $1"; }

cleanup() {
    log "Shutting down..."
    kill $API_PID $ADMIN_PID 2>/dev/null || true
    wait $API_PID $ADMIN_PID 2>/dev/null || true
    log "Done."
}
trap cleanup EXIT INT TERM

# --- 1. Database ---
log "Starting PostgreSQL..."
if docker ps --format '{{.Names}}' | grep -q '^privacyalert-db$'; then
    log "PostgreSQL already running."
else
    docker compose -f "$ROOT_DIR/api/docker-compose.yml" up -d
    log "Waiting for PostgreSQL to accept connections..."
    for i in $(seq 1 30); do
        if docker exec privacyalert-db pg_isready -U dev -d privacyalert >/dev/null 2>&1; then
            break
        fi
        sleep 1
    done
    if ! docker exec privacyalert-db pg_isready -U dev -d privacyalert >/dev/null 2>&1; then
        err "PostgreSQL failed to start within 30s"
        exit 1
    fi
    log "PostgreSQL ready."
fi

# --- 2. API ---
log "Starting API (Spring Boot)..."
cd "$ROOT_DIR/api"
./gradlew bootRun &
API_PID=$!

# Wait for API health endpoint
log "Waiting for API to be ready..."
for i in $(seq 1 60); do
    if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
        break
    fi
    sleep 2
done
if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
    log "API ready at http://localhost:8080"
else
    warn "API not responding yet (may still be starting). Continuing..."
fi

# --- 3. Admin UI ---
log "Starting Admin UI (Vue.js)..."
cd "$ROOT_DIR/admin"
if [ ! -d node_modules ]; then
    log "Installing npm dependencies..."
    npm install
fi
npm run dev &
ADMIN_PID=$!
log "Admin UI starting at http://localhost:5173"

echo ""
log "========================================="
log "  All services starting!"
log "  Database:  postgresql://localhost:5432"
log "  API:       http://localhost:8080"
log "  Admin UI:  http://localhost:5173"
log "  Admin token: changeme (default)"
log "========================================="
echo ""
log "Press Ctrl+C to stop all services."

wait
