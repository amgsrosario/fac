#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 /caminho/PT.zip [--import]" >&2
  exit 2
fi

ZIP_PATH=$1
MODE="dry-run"
if [[ ${2:-} == "--import" ]]; then MODE="import"; fi

ARGS="--fac.postal-import.enabled=true --fac.postal-import.file=\"$ZIP_PATH\" --fac.postal-import.dry-run=true"
if [[ "$MODE" == "import" ]]; then
  [[ ${FAC_POSTAL_IMPORT_CONFIRM:-} == "IMPORTAR CATALOGO POSTAL" ]] || { echo "Para importar, defina FAC_POSTAL_IMPORT_CONFIRM='IMPORTAR CATALOGO POSTAL'." >&2; exit 2; }
  ARGS="--spring.profiles.active=demo --fac.postal-import.enabled=true --fac.postal-import.file=\"$ZIP_PATH\" --fac.postal-import.dry-run=false --fac.postal-import.mode=import --fac.postal-import.confirm=true"
fi

./mvnw spring-boot:run -Dspring-boot.run.arguments="$ARGS"
