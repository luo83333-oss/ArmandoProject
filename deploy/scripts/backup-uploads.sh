#!/usr/bin/env bash
# Backup uploaded product images from API volume (complement NL2-157 DB backup)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.prod.yml"
ENV_FILE="${ROOT_DIR}/deploy/.env"
BACKUP_DIR="${BACKUP_DIR:-${ROOT_DIR}/data/backups/uploads}"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="${BACKUP_DIR}/uploads-${STAMP}.tar.gz"

mkdir -p "$BACKUP_DIR"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec -T api \
  tar czf - -C /app/uploads . > "$OUT"

echo "OK: $OUT"
