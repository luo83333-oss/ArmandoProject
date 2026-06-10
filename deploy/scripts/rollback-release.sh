#!/usr/bin/env bash
# NL2-161 rollback to a previous git ref and rebuild production stack
# Usage: ./deploy/scripts/rollback-release.sh --git-ref abc1234 [--db-backup path.sql.gz]
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.prod.yml"
ENV_FILE="${ROOT_DIR}/deploy/.env"
GIT_REF=""
DB_BACKUP=""

while [ $# -gt 0 ]; do
  case "$1" in
    --git-ref) GIT_REF="$2"; shift 2 ;;
    --db-backup) DB_BACKUP="$2"; shift 2 ;;
    *) echo "Unknown arg: $1" >&2; exit 1 ;;
  esac
done

if [ -z "$GIT_REF" ]; then
  echo "Usage: $0 --git-ref <commit|tag> [--db-backup <file.sql.gz>]" >&2
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing deploy/.env" >&2
  exit 1
fi

current="$(git -C "$ROOT_DIR" rev-parse --short HEAD)"
echo "Current: $current"
echo "Rollback target: $GIT_REF"
read -r -p "Type ROLLBACK to continue: " CONFIRM
if [ "$CONFIRM" != "ROLLBACK" ]; then
  echo "Cancelled."
  exit 0
fi

if [ -n "$DB_BACKUP" ]; then
  echo "Restoring database from $DB_BACKUP ..."
  COMPOSE_FILE=docker-compose.prod.yml bash "${ROOT_DIR}/deploy/backup/restore-mysql.sh" "$DB_BACKUP"
fi

cd "$ROOT_DIR"
git fetch --all --tags 2>/dev/null || true
git checkout "$GIT_REF"

docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --build

echo "Waiting for API health..."
for i in $(seq 1 30); do
  if docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec -T api \
    curl -fsS http://localhost:8082/api/health >/dev/null 2>&1; then
    echo "Rollback complete at $(git rev-parse --short HEAD)"
    exit 0
  fi
  sleep 3
done

echo "API health not ready — check logs: docker compose ... logs api" >&2
exit 1
