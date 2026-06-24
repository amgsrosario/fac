#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

require_docker
load_demo_env
compose config --quiet
compose up -d db
wait_healthy db

compose run --rm --no-deps \
  -e SPRING_FLYWAY_ENABLED=false \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
  -e FAC_DEMO_RESET_AUTHORIZED=false \
  -e FAC_DEMO_SEED_ON_STARTUP=false \
  -e FAC_DEMO_CHECK_ON_STARTUP=true \
  -e FAC_DEMO_EXIT_AFTER_CHECK=true \
  backend --server.port=0

info "verificacao so de leitura concluida"
