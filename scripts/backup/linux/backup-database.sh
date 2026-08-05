#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

DATABASE=""; ENVIRONMENT=""; HOST="localhost"; PORT="5432"; USER_NAME=""; BACKUP_ROOT=""; USE_DOCKER_DEMO="false"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --database) DATABASE="$2"; shift 2;;
    --environment) ENVIRONMENT="$2"; shift 2;;
    --host) HOST="$2"; shift 2;;
    --port) PORT="$2"; shift 2;;
    --user) USER_NAME="$2"; shift 2;;
    --backup-root) BACKUP_ROOT="$2"; shift 2;;
    --docker-demo) USE_DOCKER_DEMO="true"; shift;;
    *) die "argumento desconhecido: $1";;
  esac
done

assert_database_name "$DATABASE"; assert_environment "$ENVIRONMENT"; require_value User "$USER_NAME"; require_value Host "$HOST"
dir="$(backup_root "$ENVIRONMENT" "$BACKUP_ROOT")"; base="$(backup_base_name "$ENVIRONMENT" "$DATABASE")"
backup="$dir/$base.backup"; metadata="$dir/$base.metadata.json"; started="$(date -Iseconds)"
container_tmp=""; host_tmp=""

cleanup() {
  if [[ -n "$container_tmp" ]]; then
    compose exec -T db rm -f -- "$container_tmp" >/dev/null 2>&1 || true
  fi
  if [[ -n "$host_tmp" ]]; then
    rm -f -- "$host_tmp"
  fi
}
trap cleanup EXIT

if [[ "$USE_DOCKER_DEMO" == "true" ]]; then
  [[ "$ENVIRONMENT" == "demo" && "$DATABASE" == "fac_demo" ]] || die "--docker-demo apenas permite demo/fac_demo"
  require_command docker; load_demo_env
  compose up -d db
  container_tmp="$(compose exec -T db mktemp '/tmp/fac-backup.backup.XXXXXX' | tr -d '\r')"
  [[ "$container_tmp" == /tmp/fac-backup.backup.* ]] || die "nao foi possivel criar temporario seguro no container"
  compose exec -T db pg_dump -U "$USER_NAME" -d "$DATABASE" -Fc -f "$container_tmp"
  listing="$(compose exec -T db pg_restore -l "$container_tmp")" || die "pg_restore -l falhou no container db"
  [[ -n "$listing" ]] || die "pg_restore -l devolveu uma listagem vazia"
  for object in cliente artigo documento_comercial linha_documento_comercial documento_financeiro utilizador auditoria_evento mpagamento serie empresa importacao_dados_mestres flyway_schema_history; do
    grep -q "TABLE public $object" <<<"$listing" || die "objeto essencial ausente: $object"
  done
  info "pg_restore -l validou o arquivo no container db"
  host_tmp="$(mktemp "$dir/.${base}.XXXXXX.backup")"
  compose cp "db:$container_tmp" "$host_tmp"
  [[ -s "$host_tmp" ]] || die "backup copiado esta vazio"
  mv -- "$host_tmp" "$backup"
  host_tmp=""
  pg_version="$(compose exec -T db pg_dump --version | tr -d '\r')"
else
  require_command pg_dump; require_command psql
  PGPASSWORD="${PGPASSWORD:-}" pg_dump -h "$HOST" -p "$PORT" -U "$USER_NAME" -d "$DATABASE" -Fc -f "$backup"
  pg_version="$(PGPASSWORD="${PGPASSWORD:-}" psql -h "$HOST" -p "$PORT" -U "$USER_NAME" -d "$DATABASE" -At -c 'show server_version')"
fi

if [[ "$USE_DOCKER_DEMO" != "true" ]]; then
  verify_backup_file "$backup"
fi
checksum="$(sha256_file "$backup")"
size="$(wc -c <"$backup" | tr -d ' ')"
cat >"$metadata" <<JSON
{
  "project": "fac",
  "environment": "$ENVIRONMENT",
  "database": "$DATABASE",
  "createdAt": "$started",
  "finishedAt": "$(date -Iseconds)",
  "postgresVersion": "$pg_version",
  "applicationVersion": "0.0.1-SNAPSHOT",
  "gitCommit": "$(git_commit)",
  "flywayVersion": "schema_history",
  "backupFile": "$(basename "$backup")",
  "backupSizeBytes": $size,
  "sha256": "$checksum",
  "executedBy": "${USER:-unknown}",
  "validationResult": "OK",
  "encrypted": false,
  "notes": "PostgreSQL custom format (-Fc). Passwords omitted."
}
JSON
audit backup "$ENVIRONMENT" "$DATABASE" OK "$backup $checksum"
printf 'FAC_BACKUP_OK caminho=%s tamanho_bytes=%s commit=%s sha256=%s\n' "$backup" "$size" "$(git_commit)" "$checksum"
