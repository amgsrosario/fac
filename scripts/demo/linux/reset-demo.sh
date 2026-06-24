#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

require_docker
load_demo_env
require_demo_passwords
require_reset_authorization

compose config --quiet
compose up -d db
wait_healthy db
sync_db_password
compose stop frontend backend >/dev/null 2>&1 || true

info "a repor exclusivamente a base fac_demo"
compose exec -T db dropdb -U "$FAC_DEMO_DB_USER" --maintenance-db=postgres --if-exists --force fac_demo
compose exec -T db createdb -U "$FAC_DEMO_DB_USER" fac_demo

compose run --rm --no-deps \
  -e FAC_DEMO_RESET_AUTHORIZED=true \
  -e FAC_DEMO_SEED_ON_STARTUP=true \
  -e FAC_DEMO_EXIT_AFTER_SEED=true \
  -e FAC_DEMO_PASSWORD_ADMIN \
  -e FAC_DEMO_PASSWORD_OPERADOR \
  -e FAC_DEMO_PASSWORD_CONSULTA \
  backend --server.port=0

compose up -d backend frontend
wait_healthy backend
wait_healthy frontend
info "reset concluido e aplicacao disponivel em $(local_url)"
