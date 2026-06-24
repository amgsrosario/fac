#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

require_command git
require_docker
load_demo_env
chmod 600 "$ENV_FILE"
chmod +x "$SCRIPT_DIR"/*.sh

info "a validar a configuracao Compose"
compose config --quiet
info "a construir backend e frontend"
compose build
info "a preparar PostgreSQL isolado"
compose up -d db
wait_healthy db
sync_db_password
info "preparacao concluida; execute reset-demo.sh na primeira instalacao"
