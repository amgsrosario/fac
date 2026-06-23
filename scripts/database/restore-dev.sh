#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Uso: CONFIRM_DATABASE=fac $0 <backup.dump>" >&2
  exit 1
fi

database="${FAC_DEV_DATABASE:-fac}"
host="${PGHOST:-localhost}"
port="${PGPORT:-25432}"
user="${PGUSER:-postgres}"
backup_file="$1"

if [[ "$database" != "fac" || "$database" =~ [Pp][Rr][Oo][Dd] ]]; then
  echo "Reposição recusada: a base de desenvolvimento esperada é 'fac'." >&2
  exit 1
fi
if [[ "${CONFIRM_DATABASE:-}" != "$database" ]]; then
  echo "Reposição recusada: execute com CONFIRM_DATABASE=$database." >&2
  exit 1
fi
[[ -f "$backup_file" ]] || { echo "Ficheiro não encontrado: $backup_file" >&2; exit 1; }
command -v pg_restore >/dev/null || { echo "pg_restore não foi encontrado no PATH." >&2; exit 1; }

pg_restore --host="$host" --port="$port" --username="$user" --dbname="$database" --clean --if-exists --no-owner --no-privileges "$backup_file"
echo "Base '$database' reposta a partir de: $backup_file"

