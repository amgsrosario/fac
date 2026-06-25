#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
ENVIRONMENT="demo"; ROOT=""; DRY_RUN="false"
while [[ $# -gt 0 ]]; do
  case "$1" in --environment) ENVIRONMENT="$2"; shift 2;; --backup-root) ROOT="$2"; shift 2;; --dry-run) DRY_RUN="true"; shift;; *) die "argumento desconhecido: $1";; esac
done
assert_environment "$ENVIRONMENT"
dir="$(backup_root "$ENVIRONMENT" "$ROOT")"; allowed="$(cd -- "$PROJECT_ROOT/backups" && pwd)"
[[ "$dir" == "$allowed"* ]] || die "cleanup recusado fora de backups/: $dir"
declare -A keep daily weekly monthly
while IFS= read -r line; do
  ts="${line%% *}"; file="${line#* }"
  day="$(date -d "@$ts" '+%Y-%m-%d')"
  week="$(date -d "@$ts" '+%G-W%V')"
  month="$(date -d "@$ts" '+%Y-%m')"
  if [[ -z "${daily[$day]:-}" ]]; then daily[$day]="$file"; fi
  if [[ -z "${weekly[$week]:-}" ]]; then weekly[$week]="$file"; fi
  if [[ -z "${monthly[$month]:-}" ]]; then monthly[$month]="$file"; fi
done < <(find "$dir" -maxdepth 1 -type f -name '*.backup' ! -name '*.protected.backup' -printf '%T@ %p\n' | sort -rn)

for key in $(printf '%s\n' "${!daily[@]}" | sort -r | head -n 7); do keep["${daily[$key]}"]=1; done
for key in $(printf '%s\n' "${!weekly[@]}" | sort -r | head -n 4); do keep["${weekly[$key]}"]=1; done
for key in $(printf '%s\n' "${!monthly[@]}" | sort -r | head -n 6); do keep["${monthly[$key]}"]=1; done

mapfile -t files < <(find "$dir" -maxdepth 1 -type f -name '*.backup' ! -name '*.protected.backup' -printf '%T@ %p\n' | sort -rn | awk '{sub(/^[^ ]+ /,""); print}' | while IFS= read -r file; do [[ -z "${keep[$file]:-}" ]] && printf '%s\n' "$file"; done)
for file in "${files[@]}"; do
  metadata="${file%.backup}.metadata.json"
  if [[ "$DRY_RUN" == "true" ]]; then printf 'DRY-RUN delete %s\n' "$file"
  else rm -f -- "$file" "$metadata"; printf 'deleted %s\n' "$file"; fi
done
