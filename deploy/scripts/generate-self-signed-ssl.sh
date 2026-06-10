#!/usr/bin/env bash
# Local / staging TLS certs (NOT for public production)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
SSL_DIR="${ROOT_DIR}/deploy/nginx/ssl"
mkdir -p "$SSL_DIR"

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "${SSL_DIR}/privkey.pem" \
  -out "${SSL_DIR}/fullchain.pem" \
  -subj "/CN=localhost/O=ArmandoProject/C=CN"

echo "Self-signed certs written to deploy/nginx/ssl/"
