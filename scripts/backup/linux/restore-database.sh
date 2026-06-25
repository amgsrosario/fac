#!/usr/bin/env bash
set -Eeuo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

BACKUP=""; TARGET=""; ENVIRONMENT=""; HOST="localhost"; PORT="5432"; USER_NAME=""; CONFIRM=""; DROP_AFTER="false"; USE_DOCKER_DEMO="false"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --backup) BACKUP="$2"; shift 2;;
    --target-database) TARGET="$2"; shift 2;;
    --environment) ENVIRONMENT="$2"; shift 2;;
    --host) HOST="$2"; shift 2;;
    --port) PORT="$2"; shift 2;;
    --user) USER_NAME="$2"; shift 2;;
    --confirm-database) CONFIRM="$2"; shift 2;;
    --drop-after-validation) DROP_AFTER="true"; shift;;
    --docker-demo) USE_DOCKER_DEMO="true"; shift;;
    *) die "argumento desconhecido: $1";;
  esac
done

assert_restore_target "$TARGET"; assert_environment "$ENVIRONMENT"; require_value User "$USER_NAME"
[[ "$CONFIRM" == "$TARGET" ]] || die "confirmacao recusada: escreva exatamente '$TARGET'"
"$SCRIPT_DIR/verify-backup.sh" "$BACKUP"

if [[ "$USE_DOCKER_DEMO" == "true" ]]; then
  require_command docker; load_demo_env
  compose up -d db
  compose cp "$BACKUP" "db:/tmp/restore.backup"
  compose exec -T db dropdb -U "$USER_NAME" --maintenance-db=postgres --if-exists --force "$TARGET"
  compose exec -T db createdb -U "$USER_NAME" "$TARGET"
  compose exec -T db pg_restore -U "$USER_NAME" -d "$TARGET" --clean --if-exists --no-owner --exit-on-error /tmp/restore.backup
  psql_cmd=(compose exec -T db psql -v ON_ERROR_STOP=1 -U "$USER_NAME" -d "$TARGET" -At -c)
else
  require_command dropdb; require_command createdb; require_command pg_restore; require_command psql
  PGPASSWORD="${PGPASSWORD:-}" dropdb -h "$HOST" -p "$PORT" -U "$USER_NAME" --if-exists --force "$TARGET"
  PGPASSWORD="${PGPASSWORD:-}" createdb -h "$HOST" -p "$PORT" -U "$USER_NAME" "$TARGET"
  PGPASSWORD="${PGPASSWORD:-}" pg_restore -h "$HOST" -p "$PORT" -U "$USER_NAME" -d "$TARGET" --clean --if-exists --no-owner --exit-on-error "$BACKUP"
  psql_cmd=(psql -h "$HOST" -p "$PORT" -U "$USER_NAME" -d "$TARGET" -At -c)
fi

for sql in \
  "select count(*) from flyway_schema_history where success" \
  "select count(*) from empresa where id=1" \
  "select count(*) from cliente" \
  "select count(*) from artigo" \
  "select count(*) from documento_comercial" \
  "select count(*) from linha_documento_comercial" \
  "select count(*) from documento_financeiro" \
  "select count(*) from auditoria_evento" \
  "select count(*) from serie" \
  "select count(*) from importacao_dados_mestres"; do
  value="$("${psql_cmd[@]}" "$sql")"
  [[ "$value" =~ ^[0-9]+$ ]] || die "check pos-restauro falhou: $sql"
done
fk="$("${psql_cmd[@]}" "select count(*) from pg_constraint where contype='f' and not convalidated")"
[[ "$fk" == "0" ]] || die "existem foreign keys nao validadas"
audit restore "$ENVIRONMENT" "$TARGET" OK "backup=$BACKUP"
printf 'FAC_BACKUP_RESTORE_OK origem=%s destino=%s\n' "$BACKUP" "$TARGET"

if [[ "$DROP_AFTER" == "true" ]]; then
  if [[ "$USE_DOCKER_DEMO" == "true" ]]; then compose exec -T db dropdb -U "$USER_NAME" --maintenance-db=postgres --if-exists --force "$TARGET"
  else PGPASSWORD="${PGPASSWORD:-}" dropdb -h "$HOST" -p "$PORT" -U "$USER_NAME" --if-exists --force "$TARGET"; fi
  printf 'FAC_RESTORE_TEST_DROPPED %s\n' "$TARGET"
fi
