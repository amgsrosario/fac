#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
backup="${1:-}"; require_value BackupFile "$backup"
verify_backup_file "$backup"
expected="$(read_metadata_checksum "$backup")"; actual="$(sha256_file "$backup")"
[[ "$expected" == "$actual" ]] || die "checksum invalido: esperado $expected, obtido $actual"
printf 'FAC_BACKUP_VERIFY_OK %s %s\n' "$backup" "$actual"
