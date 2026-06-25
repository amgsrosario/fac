#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
backup="${1:-}"; target="${2:-fac_restore_test}"
require_value BackupFile "$backup"; load_demo_env
"$PROJECT_ROOT/scripts/backup/linux/restore-database.sh" --backup "$backup" --target-database "$target" --environment demo --user "$FAC_DEMO_DB_USER" --confirm-database "$target" --docker-demo --drop-after-validation
