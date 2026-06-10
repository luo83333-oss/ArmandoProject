#!/usr/bin/env bash
# Apply V2/V3 migrations on production MySQL (V1 runs on first container init)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.prod.yml"
ENV_FILE="${ROOT_DIR}/deploy/.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing deploy/.env" >&2
  exit 1
fi

DB_PASS="$(grep '^MYSQL_PASSWORD=' "$ENV_FILE" | cut -d= -f2- | tr -d '\r')"
if [ -z "$DB_PASS" ]; then
  echo "MYSQL_PASSWORD not set in deploy/.env" >&2
  exit 1
fi

echo "Applying V2__add_user_role.sql ..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec -T mysql \
  mysql -umarket -p"$DB_PASS" market < "$ROOT_DIR/backend/sql/V2__add_user_role.sql"

if [ -f "$ROOT_DIR/backend/sql/V3__product_gallery.sql" ]; then
  echo "Applying V3__product_gallery.sql ..."
  docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec -T mysql \
    mysql -umarket -p"$DB_PASS" market < "$ROOT_DIR/backend/sql/V3__product_gallery.sql" || true
fi

echo "DB migrations done."
