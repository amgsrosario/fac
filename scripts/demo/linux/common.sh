#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/../../.." && pwd)"
ENV_FILE="${FAC_DEMO_ENV_FILE:-$PROJECT_ROOT/.env.demo}"
COMPOSE_FILE="$PROJECT_ROOT/compose.demo.yaml"

die() {
  printf 'ERRO: %s\n' "$*" >&2
  exit 1
}

info() {
  printf 'FAC Demo: %s\n' "$*"
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || die "comando obrigatorio nao encontrado: $1"
}

ensure_env_file() {
  if [[ ! -f "$ENV_FILE" ]]; then
    cp "$PROJECT_ROOT/.env.demo.example" "$ENV_FILE"
    chmod 600 "$ENV_FILE"
    die "foi criado $ENV_FILE; preencha os valores locais e repita o comando"
  fi
  [[ ! -L "$ENV_FILE" ]] || die ".env.demo nao pode ser um link simbolico"
}

require_value() {
  local name="$1" value="${!1:-}"
  [[ -n "$value" ]] || die "$name e obrigatorio em .env.demo"
  [[ "$value" != *definir* ]] || die "$name ainda contem um marcador do ficheiro exemplo"
}

load_demo_env() {
  ensure_env_file
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a

  [[ "${SPRING_PROFILES_ACTIVE:-}" == "demo" ]] || die "SPRING_PROFILES_ACTIVE deve ser exatamente demo"
  [[ "${FAC_DEMO_DATABASE:-}" == "fac_demo" ]] || die "FAC_DEMO_DATABASE deve ser exatamente fac_demo"
  [[ "${FAC_DEMO_DATABASE:-}" != *fac_test* && "${FAC_DEMO_DATABASE:-}" != *prod* ]] || die "base perigosa recusada"

  require_value FAC_DEMO_DB_USER
  require_value FAC_DEMO_DB_PASSWORD
  require_value FAC_JWT_SECRET
  [[ "$FAC_DEMO_DB_USER" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || die "FAC_DEMO_DB_USER tem formato invalido"
  [[ ${#FAC_DEMO_DB_PASSWORD} -ge 12 ]] || die "FAC_DEMO_DB_PASSWORD deve ter pelo menos 12 caracteres"
  [[ ${#FAC_JWT_SECRET} -ge 32 ]] || die "FAC_JWT_SECRET deve ter pelo menos 32 caracteres"

  FAC_DEMO_HTTP_PORT="${FAC_DEMO_HTTP_PORT:-8088}"
  FAC_DEMO_BIND_ADDRESS="${FAC_DEMO_BIND_ADDRESS:-0.0.0.0}"
  [[ "$FAC_DEMO_HTTP_PORT" =~ ^[0-9]+$ ]] && (( FAC_DEMO_HTTP_PORT >= 1 && FAC_DEMO_HTTP_PORT <= 65535 )) || die "FAC_DEMO_HTTP_PORT invalida"
  [[ "$FAC_DEMO_BIND_ADDRESS" =~ ^([0-9]{1,3}\.){3}[0-9]{1,3}$ ]] || die "FAC_DEMO_BIND_ADDRESS deve ser um endereco IPv4 explicito"
  local octet
  IFS='.' read -r -a address_octets <<< "$FAC_DEMO_BIND_ADDRESS"
  for octet in "${address_octets[@]}"; do
    ((10#$octet <= 255)) || die "FAC_DEMO_BIND_ADDRESS contem um octeto IPv4 invalido"
  done
  export FAC_DEMO_HTTP_PORT FAC_DEMO_BIND_ADDRESS
}

require_demo_passwords() {
  local name value
  for name in FAC_DEMO_PASSWORD_ADMIN FAC_DEMO_PASSWORD_OPERADOR FAC_DEMO_PASSWORD_CONSULTA; do
    require_value "$name"
    value="${!name}"
    [[ ${#value} -ge 8 ]] || die "$name deve ter pelo menos 8 caracteres"
  done
}

require_reset_authorization() {
  [[ "${FAC_DEMO_RESET_AUTHORIZED:-}" == "true" ]] || die "FAC_DEMO_RESET_AUTHORIZED deve ser true para executar reset"
}

require_docker() {
  require_command docker
  docker compose version >/dev/null 2>&1 || die "Docker Compose v2 nao esta disponivel"
  docker info >/dev/null 2>&1 || die "Docker nao esta em execucao ou o utilizador nao tem permissao"
}

compose() {
  docker compose --project-directory "$PROJECT_ROOT" --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

wait_healthy() {
  local service="$1" attempts="${2:-60}" container health
  for ((i=1; i<=attempts; i++)); do
    container="$(compose ps -q "$service")"
    if [[ -n "$container" ]]; then
      health="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container" 2>/dev/null || true)"
      [[ "$health" == "healthy" ]] && return 0
      [[ "$health" == "unhealthy" ]] && die "servico $service ficou unhealthy"
    fi
    sleep 2
  done
  die "tempo esgotado a aguardar pelo servico $service"
}

sync_db_password() {
  local escaped
  escaped="${FAC_DEMO_DB_PASSWORD//\'/\'\'}"
  printf 'ALTER ROLE "%s" WITH PASSWORD '\''%s'\'';\n' "$FAC_DEMO_DB_USER" "$escaped" \
    | compose exec -T db psql -v ON_ERROR_STOP=1 -U "$FAC_DEMO_DB_USER" -d postgres >/dev/null
}

local_url() {
  printf 'http://127.0.0.1:%s' "$FAC_DEMO_HTTP_PORT"
}

network_url() {
  local ip
  ip="$(hostname -I 2>/dev/null | awk '{print $1}')"
  [[ -n "$ip" ]] && printf 'http://%s:%s' "$ip" "$FAC_DEMO_HTTP_PORT" || printf 'indisponivel'
}
