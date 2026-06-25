#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
ENVIRONMENT="${1:-demo}"; ROOT="${2:-}"
assert_environment "$ENVIRONMENT"
dir="$(backup_root "$ENVIRONMENT" "$ROOT")"
find "$dir" -maxdepth 1 -type f -name '*.backup' -printf '%TY-%Tm-%Td %TH:%TM %s %p\n' | sort -r
