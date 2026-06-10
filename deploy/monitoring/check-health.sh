#!/usr/bin/env bash
# NL2-160 basic monitoring: disk, containers, API health, nginx 5xx rate
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/docker-compose.prod.yml}"
ENV_FILE="${ENV_FILE:-${ROOT_DIR}/deploy/.env}"
ALERT_ENV="${ALERT_ENV:-${ROOT_DIR}/deploy/monitoring/alert.env}"

DISK_THRESHOLD="${DISK_THRESHOLD:-85}"
HTTP5XX_THRESHOLD="${HTTP5XX_THRESHOLD:-20}"
LOG_WINDOW="${LOG_WINDOW:-1h}"

if [ -f "$ALERT_ENV" ]; then
  set -a
  # shellcheck disable=SC1090
  source "$ALERT_ENV"
  set +a
fi

issues=()

disk_used="$(df -P / | awk 'NR==2 {gsub(/%/,"",$5); print $5}')"
if [ -n "$disk_used" ] && [ "$disk_used" -ge "$DISK_THRESHOLD" ]; then
  issues+=("disk_usage=${disk_used}%")
fi

for svc in mysql redis api nginx; do
  if ! docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps --status running "$svc" -q 2>/dev/null | grep -q .; then
    issues+=("service_down=${svc}")
  fi
done

if ! docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec -T api \
  curl -fsS http://localhost:8082/api/health >/dev/null 2>&1; then
  issues+=("api_health_fail")
fi

five_xx="$(docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs --since "$LOG_WINDOW" nginx 2>&1 \
  | grep -cE 'HTTP/[0-9.]+" 5[0-9]{2}' || true)"
if [ "$five_xx" -ge "$HTTP5XX_THRESHOLD" ]; then
  issues+=("nginx_5xx_${LOG_WINDOW}=${five_xx}")
fi

if [ "${#issues[@]}" -gt 0 ]; then
  msg="[market-monitor] $(date -Iseconds) ALERT: $(IFS=,; echo "${issues[*]}")"
  echo "$msg" >&2
  if [ -n "${ALERT_WEBHOOK_URL:-}" ]; then
    payload="$(printf '{"text":"%s"}' "$msg")"
    curl -fsS -X POST -H "Content-Type: application/json" \
      -d "$payload" "$ALERT_WEBHOOK_URL" >/dev/null || echo "webhook failed" >&2
  fi
  exit 1
fi

echo "[market-monitor] $(date -Iseconds) OK disk=${disk_used:-?}% 5xx_${LOG_WINDOW}=${five_xx}"
exit 0
