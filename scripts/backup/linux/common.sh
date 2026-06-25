#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/../../.." && pwd)"

die() { printf 'ERRO: %s\n' "$*" >&2; exit 1; }
info() { printf 'FAC Backup: %s\n' "$*"; }
require_command() { command -v "$1" >/dev/null 2>&1 || die "comando obrigatorio nao encontrado: $1"; }
require_value() { local n="$1" v="${2:-}"; [[ -n "$v" ]] || die "$n e obrigatorio"; }

assert_database_name() {
  local db="${1:-}"
  require_value "Database" "$db"
  [[ "$db" != *"*"* && "$db" != *"?"* && "$db" =~ ^[A-Za-z_][A-Za-z0-9_]{0,62}$ ]] || die "nome de base invalido: $db"
  case "${db,,}" in fac|postgres|template0|template1) die "base recusada: $db";; esac
}

assert_restore_target() {
  local db="${1:-}"
  assert_database_name "$db"
  case "${db,,}" in fac_restore_test|fac_demo|fac_test) ;; *) die "destino nao autorizado: $db";; esac
}

assert_environment() {
  local env="${1:-}"
  case "$env" in demo|test|production) ;; *) die "ambiente nao reconhecido: $env";; esac
}

backup_root() {
  local env="$1" root="${2:-}"
  [[ -n "$root" ]] || root="$PROJECT_ROOT/backups/$env"
  mkdir -p -- "$root"
  (cd -- "$root" && pwd)
}

audit_log() {
  mkdir -p -- "$PROJECT_ROOT/backups/logs"
  printf '%s\n' "$PROJECT_ROOT/backups/logs/backup-restore-audit.log"
}

audit() {
  local op="$1" env="$2" db="$3" result="$4" details="${5:-}"
  printf '{"timestamp":"%s","operation":"%s","environment":"%s","database":"%s","result":"%s","user":"%s","details":"%s"}\n' \
    "$(date -Iseconds)" "$op" "$env" "$db" "$result" "${USER:-unknown}" "${details//\"/\'}" >>"$(audit_log)"
}

git_commit() {
  local commit dirty
  commit="$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || true)"
  [[ -n "$commit" ]] || { printf 'unknown'; return; }
  dirty="$(git -C "$PROJECT_ROOT" status --short 2>/dev/null || true)"
  [[ -n "$dirty" ]] && printf '%s-dirty' "$commit" || printf '%s' "$commit"
}

backup_base_name() {
  local env="$1" db="$2" commit
  commit="$(git_commit | sed 's/[^A-Za-z0-9_.-]/-/g')"
  printf 'fac_%s_%s_%s_%s' "$env" "$db" "$(date '+%Y-%m-%d_%H%M%S')" "$commit"
}

sha256_file() {
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | awk '{print $1}'
  else shasum -a 256 "$1" | awk '{print $1}'; fi
}

compose() {
  local env_file="${FAC_DEMO_ENV_FILE:-$PROJECT_ROOT/.env.demo}"
  docker compose --project-directory "$PROJECT_ROOT" --env-file "$env_file" -f "$PROJECT_ROOT/compose.demo.yaml" "$@"
}

load_demo_env() {
  local env_file="${FAC_DEMO_ENV_FILE:-$PROJECT_ROOT/.env.demo}"
  [[ -f "$env_file" ]] || die ".env.demo inexistente; copie .env.demo.example e preencha valores locais"
  set -a
  # shellcheck disable=SC1090
  source "$env_file"
  set +a
  [[ "${SPRING_PROFILES_ACTIVE:-}" == "demo" ]] || die "SPRING_PROFILES_ACTIVE deve ser demo"
  [[ "${FAC_DEMO_DATABASE:-}" == "fac_demo" ]] || die "FAC_DEMO_DATABASE deve ser fac_demo"
  require_value FAC_DEMO_DB_USER "${FAC_DEMO_DB_USER:-}"
  require_value FAC_DEMO_DB_PASSWORD "${FAC_DEMO_DB_PASSWORD:-}"
}

verify_backup_file() {
  local backup="$1"
  [[ -f "$backup" ]] || die "ficheiro inexistente: $backup"
  [[ -s "$backup" ]] || die "ficheiro vazio: $backup"
  require_command pg_restore
  pg_restore --list "$backup" >/tmp/fac_backup_list.$$ || die "pg_restore --list falhou"
  local text
  text="$(cat /tmp/fac_backup_list.$$)"
  rm -f /tmp/fac_backup_list.$$
  local object
  for object in cliente artigo documento_comercial linha_documento_comercial documento_financeiro utilizador auditoria_evento mpagamento serie empresa importacao_dados_mestres flyway_schema_history; do
    grep -q "TABLE public $object" <<<"$text" || die "objeto essencial ausente: $object"
  done
}

read_metadata_checksum() {
  local backup="$1" metadata="${backup%.backup}.metadata.json"
  [[ -f "$metadata" ]] || die "metadados inexistentes: $metadata"
  sed -n 's/.*"sha256"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$metadata" | head -n1
}
