#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
load_demo_env
"$PROJECT_ROOT/scripts/backup/linux/backup-database.sh" --database fac_demo --environment demo --user "$FAC_DEMO_DB_USER" --docker-demo
