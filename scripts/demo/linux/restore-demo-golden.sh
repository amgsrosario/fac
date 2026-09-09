#!/usr/bin/env bash
set -Eeuo pipefail
# Never trace credentials loaded from the environment.
set +x
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

dry_run=false
case "${1:-}" in
  "") [[ $# == 0 ]] || die "argumentos invalidos";;
  --dry-run) [[ $# == 1 ]] || die "argumentos invalidos"; dry_run=true;;
  *) die "uso: restore-demo-golden.sh [--dry-run]";;
esac
for cmd in flock python3 sha256sum realpath; do require_command "$cmd"; done
[[ -f "$ENV_FILE" && ! -L "$ENV_FILE" ]] || die ".env.demo ausente ou symlink"
load_demo_env
require_docker
readonly golden_root="/home/arosario/tuuli-backups/golden"
golden="$(realpath -e "$golden_root/fac_demo_golden_current.backup")"
metadata="$(realpath -e "$golden_root/fac_demo_golden_current.metadata.json")"
[[ -f "$golden" && -f "$metadata" ]] || die "golden/metadata nao sao ficheiros regulares"

# Lock the existing project directory: no predictable writable lock file.
exec {lock_fd}<"$PROJECT_ROOT"
flock -n "$lock_fd" || die "outra reposicao golden esta em execucao"
log=""
if ! "$dry_run"; then
  umask 077
  mkdir -p "$PROJECT_ROOT/backups/logs"
  log="$(mktemp "$PROJECT_ROOT/backups/logs/golden-restore.XXXXXX.log")"
fi
record() {
  local message="$(date -Iseconds) $*"
  printf '%s\n' "$message"
  [[ -z "$log" ]] || printf '%s\n' "$message" >> "$log"
}
container="" container_tmp="" phase="validacao"
cleanup() {
  local rc=$?
  trap - EXIT
  if [[ -n "$container_tmp" ]]; then
    docker exec "$container" rm -f -- "$container_tmp" || rc=1
  fi
  record "fim resultado=$rc fase=$phase"
  if (( rc != 0 )); then
    printf 'Falha na fase %s. Sem rollback automatico; preserve o safety backup e consulte os logs backend/Flyway.\n' "$phase" >&2
  fi
  exit "$rc"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
record "inicio dry_run=$dry_run commit=$(git -C "$PROJECT_ROOT" rev-parse HEAD) golden=$golden"

validate_archive() {
  python3 - "$1" "$2" <<'PY'
import hashlib,json,sys
from pathlib import Path
p,m=map(Path,sys.argv[1:])
assert p.is_file() and p.stat().st_size>0 and m.is_file(), "arquivo/metadata ausente"
data=json.loads(m.read_text())
assert data["database"]=="fac_demo" and data["environment"]=="demo" and data["project"]=="fac"
assert data["validationResult"]=="OK"
assert data["backupFile"]==p.name and data["backupSizeBytes"]==p.stat().st_size
assert p.open("rb").read(5)==b"PGDMP", "formato nao custom"
digest=hashlib.sha256(p.read_bytes()).hexdigest()
assert digest==data["sha256"], "checksum invalido"
print(digest)
PY
}
checksum="$(validate_archive "$golden" "$metadata")"
record "golden checksum=$checksum metadata=$metadata"
project="$(compose config --format json | python3 -c 'import json,sys; print(json.load(sys.stdin)["name"])')"
[[ "$project" == fac-demo ]] || die "projeto Compose deve ser fac-demo"
container="$(compose ps -q db)"
[[ -n "$container" && "$container" != *$'\n'* ]] || die "servico db ausente ou ambiguo"
identity="$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}/{{index .Config.Labels "com.docker.compose.service"}}/{{.State.Health.Status}}' "$container")"
[[ "$identity" == fac-demo/db/healthy ]] || die "container db inesperado ou nao healthy"
db_exec() { docker exec -i "$container" "$@"; }
[[ "$(db_exec psql -v ON_ERROR_STOP=1 -U "$FAC_DEMO_DB_USER" -d fac_demo -Atc 'select current_database()')" == fac_demo ]] || die "database alvo inesperada"

container_tmp="$(db_exec mktemp /tmp/fac-golden.XXXXXX)"
[[ "$container_tmp" == /tmp/fac-golden.* ]] || die "temporario invalido"
docker cp "$golden" "$container:$container_tmp"
verify_staged() {
  [[ "$(db_exec sha256sum "$container_tmp" | awk '{print $1}')" == "$checksum" ]] || die "checksum do temporario invalido"
}
verify_staged
listing="$(db_exec pg_restore -l "$container_tmp")"
for object in cliente artigo documento_comercial linha_documento_comercial documento_financeiro utilizador auditoria_evento mpagamento serie empresa importacao_dados_mestres flyway_schema_history; do
  grep -Eq " TABLE public ${object} " <<< "$listing" || die "tabela essencial ausente: $object"
done
record "catalogo OK; 12 tabelas; database=fac_demo container=$container"
if "$dry_run"; then
  phase="dry-run concluido"
  exit 0
fi
[[ -t 0 ]] || die "confirmacao requer terminal humano"
phase="safety backup"
# Preserve the official backup flow; capture its exact success path, not the newest file.
backup_output="$(bash "$SCRIPT_DIR/backup-demo.sh")"
printf '%s\n' "$backup_output"
safety="$(sed -n 's/^FAC_BACKUP_OK caminho=\(.*\) tamanho_bytes=.*/\1/p' <<< "$backup_output")"
[[ -n "$safety" && "$safety" != *$'\n'* ]] || die "backup preventivo nao identificado"
safety_checksum="$(validate_archive "$safety" "${safety%.backup}.metadata.json")"
db_exec pg_restore -l < "$safety" >/dev/null
record "safety=$safety checksum=$safety_checksum"
# The official backup must not have replaced the pinned database container.
[[ "$(compose ps -q db)" == "$container" ]] || die "container db mudou; reposicao recusada"
printf '%s\n' 'ATENÇÃO: esta operação vai substituir integralmente a database fac_demo pelo GOLDEN DEMO atual.'
read -r -p 'Escreva exatamente REPOR fac_demo: ' confirmation
[[ "$confirmation" == "REPOR fac_demo" ]] || die "confirmacao recusada"
verify_staged
phase="parar aplicacao"
compose stop backend frontend
phase="drop fac_demo"
db_exec dropdb -U "$FAC_DEMO_DB_USER" --maintenance-db=postgres --force fac_demo
phase="recriar fac_demo"
db_exec createdb -U "$FAC_DEMO_DB_USER" --owner="$FAC_DEMO_DB_USER" fac_demo
phase="restore golden"
db_exec pg_restore -U "$FAC_DEMO_DB_USER" -d fac_demo --no-owner --exit-on-error "$container_tmp"
phase="backend e Flyway"
compose start backend
wait_healthy backend
phase="frontend"
compose start frontend
wait_healthy frontend
phase="checks oficiais"
bash "$SCRIPT_DIR/status-demo.sh"
bash "$SCRIPT_DIR/check-demo.sh"
db_exec psql -v ON_ERROR_STOP=1 -U "$FAC_DEMO_DB_USER" -d fac_demo -At <<'SQL'
BEGIN READ ONLY;
select 'clientes',count(*) from cliente union all select 'artigos',count(*) from artigo union all select 'utilizadores',count(*) from utilizador union all select 'comerciais',count(*) from documento_comercial union all select 'financeiros',count(*) from documento_financeiro;
COMMIT;
SQL
phase="concluido"
record "GOLDEN_RESTORE_OK database=fac_demo safety=$safety"
