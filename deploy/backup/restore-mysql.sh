#!/usr/bin/env bash
# NL2-157 MySQL 恢复演练（Linux）
# 用法: ./deploy/backup/restore-mysql.sh /path/to/market-20260609-120000.sql.gz
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: $0 <backup.sql.gz>" >&2
  exit 1
fi

BACKUP_FILE="$1"
ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
CONTAINER="${MYSQL_CONTAINER:-market-mysql}"

DB_USER="${MYSQL_USER:-market}"
DB_PASS="${MYSQL_PASSWORD:-market_pass}"
DB_NAME="${MYSQL_DATABASE:-market}"
ROOT_PASS="${MYSQL_ROOT_PASSWORD:-market_root_pass}"

if [ ! -f "$BACKUP_FILE" ]; then
  echo "ERROR: file not found: $BACKUP_FILE" >&2
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  echo "ERROR: container $CONTAINER not running" >&2
  exit 1
fi

echo "WARNING: will DROP and recreate database $DB_NAME from:"
echo "  $BACKUP_FILE"
read -r -p "Type RESTORE to continue: " CONFIRM
if [ "$CONFIRM" != "RESTORE" ]; then
  echo "Cancelled."
  exit 0
fi

docker compose -f "$COMPOSE_FILE" exec -T mysql mysql -uroot -p"$ROOT_PASS" \
  -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

gunzip -c "$BACKUP_FILE" | docker compose -f "$COMPOSE_FILE" exec -T mysql mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME"

echo "OK: restore finished. Restart API and check /api/health"
