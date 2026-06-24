#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

require_docker
load_demo_env
compose ps
if command -v curl >/dev/null 2>&1 && curl --fail --silent --show-error "$(local_url)/health" >/dev/null; then
  info "frontend acessivel em $(local_url)"
else
  info "frontend ainda nao responde em $(local_url)"
fi
info "endereco de rede: $(network_url)"
