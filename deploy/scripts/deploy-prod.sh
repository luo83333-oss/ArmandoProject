#!/usr/bin/env bash
# NL2-158 one-shot production deploy on Linux ECS
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.prod.yml"
ENV_FILE="${ROOT_DIR}/deploy/.env"

cd "$ROOT_DIR"

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing deploy/.env — copy from deploy/.env.production.example" >&2
  exit 1
fi

if [ ! -f "${ROOT_DIR}/deploy/nginx/ssl/fullchain.pem" ] || [ ! -f "${ROOT_DIR}/deploy/nginx/ssl/privkey.pem" ]; then
  echo "Missing SSL certs in deploy/nginx/ssl/ — see docs/deploy/PRODUCTION_DEPLOY.md" >&2
  exit 1
fi

ROOT_PASS="$(grep '^MYSQL_ROOT_PASSWORD=' "$ENV_FILE" | cut -d= -f2- | tr -d '\r')"
USER_DOMAIN="$(grep '^USER_DOMAIN=' "$ENV_FILE" | cut -d= -f2- | tr -d '\r')"
MERCHANT_DOMAIN="$(grep '^MERCHANT_DOMAIN=' "$ENV_FILE" | cut -d= -f2- | tr -d '\r')"
ADMIN_DOMAIN="$(grep '^ADMIN_DOMAIN=' "$ENV_FILE" | cut -d= -f2- | tr -d '\r')"

docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --build

echo "Waiting for MySQL..."
for i in $(seq 1 30); do
  if docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec -T mysql mysqladmin ping -h localhost -uroot -p"${ROOT_PASS}" &>/dev/null; then
    break
  fi
  sleep 3
done

bash "${ROOT_DIR}/deploy/scripts/init-prod-db.sh"

echo "Deploy complete."
echo "  User:     https://${USER_DOMAIN}"
echo "  Merchant: https://${MERCHANT_DOMAIN}"
echo "  Admin:    https://${ADMIN_DOMAIN}"
