#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

RESET="false"
SKIP_BACKUP="false"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --reset) RESET="true"; shift;;
    --skip-backup) SKIP_BACKUP="true"; shift;;
    *) die "argumento desconhecido: $1";;
  esac
done

require_docker
load_demo_env
compose config --quiet
compose up -d db
wait_healthy db
sync_db_password

if [[ "$RESET" == "true" ]]; then
  info "reset controlado da base fac_demo"
  "$SCRIPT_DIR/reset-demo.sh"
fi

info "check funcional"
"$SCRIPT_DIR/check-demo.sh"

info "check comercial"
compose run --rm --no-deps \
  -e SPRING_FLYWAY_ENABLED=false \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
  -e FAC_DEMO_RESET_AUTHORIZED=false \
  -e FAC_DEMO_SEED_ON_STARTUP=false \
  -e FAC_COMMERCIAL_DEMO_CHECK_ON_STARTUP=true \
  -e FAC_COMMERCIAL_DEMO_EXIT_AFTER_CHECK=true \
  backend --server.port=0

if [[ "$SKIP_BACKUP" != "true" ]]; then
  info "backup de referencia"
  "$SCRIPT_DIR/backup-demo.sh"
fi

info "a iniciar aplicacao"
compose up -d backend frontend
wait_healthy backend
wait_healthy frontend

info "FAC_COMMERCIAL_DEMO_READY url=$(local_url) base=fac_demo perfil=demo"
