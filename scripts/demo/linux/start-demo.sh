#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

require_docker
load_demo_env
compose config --quiet
compose up -d
wait_healthy db
wait_healthy backend
wait_healthy frontend
info "aplicacao iniciada"
info "acesso local: $(local_url)"
info "acesso na rede: $(network_url)"
