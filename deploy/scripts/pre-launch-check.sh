#!/usr/bin/env bash
# NL2-161 pre-launch validation (run on ECS before go-live)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="${ROOT_DIR}/deploy/.env"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.prod.yml"
SSL_DIR="${ROOT_DIR}/deploy/nginx/ssl"

fail=0

warn() { echo "[WARN] $*" >&2; }
err() { echo "[FAIL] $*" >&2; fail=1; }
ok() { echo "[OK]   $*"; }

echo "=== Pre-launch check ==="

if [ ! -f "$ENV_FILE" ]; then
  err "missing deploy/.env"
else
  ok "deploy/.env exists"
  if grep -qE '^JWT_SECRET=change-to-random' "$ENV_FILE" 2>/dev/null; then
    err "JWT_SECRET still default placeholder"
  fi
  if grep -qE '^MYSQL_PASSWORD=change_' "$ENV_FILE" 2>/dev/null; then
    err "MYSQL_PASSWORD still default placeholder"
  fi
  if grep -qE '^PAYMENT_MOCK_ENABLED=true' "$ENV_FILE" 2>/dev/null; then
    warn "PAYMENT_MOCK_ENABLED=true (confirm for production)"
  fi
fi

if [ ! -f "${SSL_DIR}/fullchain.pem" ] || [ ! -f "${SSL_DIR}/privkey.pem" ]; then
  err "SSL certs missing in deploy/nginx/ssl/"
else
  ok "SSL certs present"
fi

if docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps --status running -q 2>/dev/null | grep -q .; then
  ok "production stack is running"
  if [ -x "${ROOT_DIR}/deploy/monitoring/check-health.sh" ]; then
    if "${ROOT_DIR}/deploy/monitoring/check-health.sh"; then
      ok "monitoring check passed"
    else
      err "monitoring check failed"
    fi
  fi
else
  warn "production stack not running (skip runtime checks)"
fi

echo "Git commit: $(git -C "$ROOT_DIR" rev-parse --short HEAD 2>/dev/null || echo unknown)"

if [ "$fail" -ne 0 ]; then
  echo "=== Pre-launch check FAILED ===" >&2
  exit 1
fi

echo "=== Pre-launch check PASSED ==="
exit 0
