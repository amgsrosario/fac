#!/usr/bin/env bash
set -euo pipefail

database="${FAC_DEV_DATABASE:-fac}"
host="${PGHOST:-localhost}"
port="${PGPORT:-25432}"
user="${PGUSER:-postgres}"
output_directory="${1:-backups/database}"

if [[ "$database" != "fac" || "$database" =~ [Pp][Rr][Oo][Dd] ]]; then
  echo "Backup recusado: a base de desenvolvimento esperada é 'fac'." >&2
  exit 1
fi
command -v pg_dump >/dev/null || { echo "pg_dump não foi encontrado no PATH." >&2; exit 1; }

mkdir -p "$output_directory"
output="$output_directory/fac-dev-$(date +%Y%m%d-%H%M%S).dump"
pg_dump --host="$host" --port="$port" --username="$user" --format=custom --no-owner --no-privileges --file="$output" "$database"
echo "Backup criado: $output"

