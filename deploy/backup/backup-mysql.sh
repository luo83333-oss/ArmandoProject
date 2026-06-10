#!/usr/bin/env bash
# NL2-157 生产环境 MySQL 逻辑备份（Linux / ECS cron）
# 用法: ./deploy/backup/backup-mysql.sh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
BACKUP_DIR="${BACKUP_DIR:-${ROOT_DIR}/data/backups/mysql}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
CONTAINER="${MYSQL_CONTAINER:-market-mysql}"

DB_USER="${MYSQL_USER:-market}"
DB_PASS="${MYSQL_PASSWORD:-market_pass}"
DB_NAME="${MYSQL_DATABASE:-market}"

mkdir -p "$BACKUP_DIR"
if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  echo "ERROR: container $CONTAINER not running" >&2
  exit 1
fi

STAMP="$(date +%Y%m%d-%H%M%S)"
OUT_FILE="${BACKUP_DIR}/market-${STAMP}.sql.gz"

docker compose -f "$COMPOSE_FILE" exec -T mysql mysqldump \
  -u"$DB_USER" -p"$DB_PASS" \
  --single-transaction \
  --no-tablespaces \
  --routines \
  --triggers \
  --set-gtid-purged=OFF \
  "$DB_NAME" | gzip -c > "$OUT_FILE"

SIZE="$(wc -c < "$OUT_FILE" | tr -d ' ')"
if [ "$SIZE" -lt 100 ]; then
  rm -f "$OUT_FILE"
  echo "ERROR: backup too small ($SIZE bytes)" >&2
  exit 1
fi

find "$BACKUP_DIR" -name 'market-*.sql.gz' -type f -mtime +"$RETENTION_DAYS" -delete
echo "OK: $OUT_FILE ($SIZE bytes)"
