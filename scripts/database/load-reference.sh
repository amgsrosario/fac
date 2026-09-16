#!/usr/bin/env bash
set -Eeuo pipefail

if [[ $# -lt 1 || $# -gt 3 ]]; then
  echo "Uso: $0 DATABASE [CONTAINER] [DB_USER]" >&2
  exit 2
fi

database="$1"
container="${2:-facdb}"
db_user="${3:-postgres}"
root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"

for file in "$root/data/reference/codigos_postais_portugal.csv" \
            "$root/data/reference/freguesias_portugal.csv" \
            "$root/scripts/database/load-reference.sql"; do
  test -f "$file" || { echo "Ficheiro ausente: $file" >&2; exit 1; }
done

docker cp "$root/data/reference/codigos_postais_portugal.csv" "$container:/tmp/tuuli-codigos-postais.csv"
docker cp "$root/data/reference/freguesias_portugal.csv" "$container:/tmp/tuuli-freguesias.csv"
docker cp "$root/scripts/database/load-reference.sql" "$container:/tmp/tuuli-load-reference.sql"
docker exec "$container" psql -X -v ON_ERROR_STOP=1 -U "$db_user" -d "$database" -f /tmp/tuuli-load-reference.sql
