#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

readonly DEPLOY_BRANCH="feature/visual-redesign"
readonly BACKUP_ROOT="/home/arosario/tuuli-backups/predeploy"
dry_run=false
run_demo_check=false

usage() {
  printf 'Uso: %s [--dry-run] [--check-demo]\n' "$(basename "$0")"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) dry_run=true ;;
    --check-demo) run_demo_check=true ;;
    -h|--help) usage; exit 0 ;;
    *) usage >&2; die "argumento desconhecido: $1" ;;
  esac
  shift
done

cd "$PROJECT_ROOT"
require_command git

branch="$(git branch --show-current)"
[[ "$branch" == "$DEPLOY_BRANCH" ]] || die "branch atual deve ser $DEPLOY_BRANCH (atual: ${branch:-detached})"

if ! git diff --quiet || ! git diff --cached --quiet; then
  die "existem alteracoes versionadas locais; deploy recusado"
fi

current_head="$(git rev-parse HEAD)"
untracked="$(git ls-files --others --exclude-standard)"
if [[ -n "$untracked" ]]; then
  info "ficheiros untracked preservados:"
  printf '%s\n' "$untracked"
else
  info "sem ficheiros untracked"
fi

print_plan() {
  local target="indisponivel ate git fetch"
  if git show-ref --verify --quiet "refs/remotes/origin/$DEPLOY_BRANCH"; then
    target="$(git rev-parse "origin/$DEPLOY_BRANCH")"
  fi
  printf 'Branch: %s\n' "$branch"
  printf 'Estado tracked: limpo\n'
  printf 'HEAD atual: %s\n' "$current_head"
  printf 'Origin alvo conhecido: %s\n' "$target"
  printf 'Commits pendentes (referencia local de origin):\n'
  if [[ "$target" != "indisponivel ate git fetch" ]]; then
    git log --oneline "$current_head..$target" || true
  else
    printf '  referencia origin/%s ainda nao existe localmente\n' "$DEPLOY_BRANCH"
  fi
  printf 'Servicos a reconstruir: backend, frontend (db preservada e validada)\n'
  printf 'Backup pre-deploy: %s/fac_demo_fac_demo_<timestamp>_<commit>.backup\n' "$BACKUP_ROOT"
  printf 'Check funcional seeded: %s\n' "$([[ "$run_demo_check" == true ]] && printf 'sim' || printf 'nao')"
}

if "$dry_run"; then
  print_plan
  info "dry-run concluido; nenhum pull, backup ou comando Docker foi executado"
  exit 0
fi

require_docker
load_demo_env
compose config --quiet

info "a atualizar referencias remotas"
git fetch origin
target_head="$(git rev-parse "origin/$DEPLOY_BRANCH")"
git merge-base --is-ancestor "$current_head" "$target_head" \
  || die "origin/$DEPLOY_BRANCH nao e avancado direto do HEAD atual; intervencao manual necessaria"

if [[ "$current_head" == "$target_head" ]]; then
  info "checkout ja esta no HEAD publicado; o deploy prossegue com backup e rebuild controlado"
else
  info "commits a aplicar:"
  git log --oneline "$current_head..$target_head"
fi

compose up -d db
wait_healthy db

snapshot_counts() {
  compose exec -T db psql -v ON_ERROR_STOP=1 -U "$FAC_DEMO_DB_USER" -d fac_demo -At -F= <<'SQL'
select 'cliente', count(*) from cliente
union all select 'artigo', count(*) from artigo
union all select 'documento_comercial', count(*) from documento_comercial
union all select 'documento_financeiro', count(*) from documento_financeiro
union all select 'utilizador', count(*) from utilizador
order by 1;
SQL
}

counts_before="$(snapshot_counts)"
info "contagens antes do deploy (zero e valido):"
printf '%s\n' "$counts_before"

info "a criar backup pre-deploy obrigatorio"
backup_output="$(bash "$PROJECT_ROOT/scripts/backup/linux/backup-database.sh" \
  --database fac_demo \
  --environment demo \
  --user "$FAC_DEMO_DB_USER" \
  --backup-root "$BACKUP_ROOT" \
  --docker-demo)"
printf '%s\n' "$backup_output"
backup_path="$(sed -n 's/^FAC_BACKUP_OK caminho=\(.*\) tamanho_bytes=.*/\1/p' <<< "$backup_output")"
[[ -n "$backup_path" && "$backup_path" != *$'\n'* && -s "$backup_path" ]] \
  || die "backup pre-deploy nao foi identificado ou esta vazio"

info "a aplicar apenas avancos fast-forward"
git pull --ff-only origin "$DEPLOY_BRANCH"
[[ "$(git rev-parse HEAD)" == "$target_head" ]] || die "HEAD apos pull difere do origin validado"

info "a reconstruir aplicacao sem remover volumes"
compose up -d --build

wait_services() {
  local attempts=90 service container state all_healthy
  for ((attempt=1; attempt<=attempts; attempt++)); do
    all_healthy=true
    for service in db backend frontend; do
      container="$(compose ps -q "$service")"
      if [[ -z "$container" ]]; then
        all_healthy=false
        continue
      fi
      state="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container" 2>/dev/null || true)"
      if [[ "$state" == "unhealthy" || "$state" == "exited" || "$state" == "dead" ]]; then
        all_healthy=false
        break 2
      fi
      [[ "$state" == "healthy" ]] || all_healthy=false
    done
    "$all_healthy" && return 0
    sleep 2
  done
  compose ps >&2 || true
  compose logs --tail=100 db backend frontend >&2 || true
  die "servicos nao ficaram healthy dentro do tempo limite"
}

wait_services
compose ps

counts_after="$(snapshot_counts)"
info "contagens depois do deploy (zero e valido):"
printf '%s\n' "$counts_after"

declare -A before=()
while IFS='=' read -r table count; do before["$table"]="$count"; done <<< "$counts_before"
while IFS='=' read -r table count; do
  [[ -n "${before[$table]+x}" ]] || die "contagem anterior ausente para $table"
  (( count >= before[$table] )) || die "contagem de $table diminuiu: ${before[$table]} -> $count"
done <<< "$counts_after"

if "$run_demo_check"; then
  info "a executar check-demo.sh por pedido explicito"
  bash "$SCRIPT_DIR/check-demo.sh"
fi

info "DEPLOY_OK branch=$DEPLOY_BRANCH head=$(git rev-parse --short HEAD) backup=$backup_path"
info "acesso local: $(local_url)"
