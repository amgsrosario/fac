#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

require_docker
load_demo_env
if (($# > 0)); then
  compose logs --tail=200 -f "$@"
else
  compose logs --tail=200 -f
fi
