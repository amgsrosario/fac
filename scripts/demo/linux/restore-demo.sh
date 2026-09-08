#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
backup="${1:-}"; target="${2:-fac_restore_test}"
[[ -n "$backup" && -f "$backup" ]] || die "backup inexistente ou nao indicado"
[[ "$target" == "fac_restore_test" ]] || die "apenas fac_restore_test e permitido"
load_demo_env
bash "$PROJECT_ROOT/scripts/backup/linux/restore-database.sh" --backup "$backup" --target-database "$target" --environment demo --user "$FAC_DEMO_DB_USER" --confirm-database "$target" --docker-demo --drop-after-validation
