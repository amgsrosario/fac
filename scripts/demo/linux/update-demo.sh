#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$PROJECT_ROOT"

echo "=================================================="
echo " FAC Demo Ubuntu — reconstrução e validação"
echo " Projeto: $PROJECT_ROOT"
echo "=================================================="
echo
echo "ATENÇÃO:"
echo "- não faz git pull"
echo "- não faz git fetch"
echo "- não faz commit"
echo "- não faz push"
echo "- recria exclusivamente a base fac_demo"
echo

if [[ ! -f "compose.demo.yaml" ]]; then
  echo "ERRO: compose.demo.yaml não encontrado na raiz do projeto." >&2
  exit 1
fi

if [[ ! -f ".env.demo" ]]; then
  echo "ERRO: .env.demo não encontrado." >&2
  echo "Execute primeiro: ./scripts/demo/linux/prepare-demo.sh" >&2
  exit 1
fi

if [[ ! -d "scripts/demo/linux" ]]; then
  echo "ERRO: scripts/demo/linux não encontrado." >&2
  exit 1
fi

read -r -p "Confirmar reconstrução e reset de fac_demo? [s/N] " CONFIRMAR

case "${CONFIRMAR,,}" in
  s|sim)
    ;;
  *)
    echo "Operação cancelada."
    exit 0
    ;;
esac

echo
echo "[1/7] A corrigir permissões dos scripts Linux..."
chmod +x scripts/demo/linux/*.sh

echo
echo "[2/7] A validar configuração local não secreta..."
grep -E '^(SPRING_PROFILES_ACTIVE|FAC_DEMO_DATABASE|FAC_DEMO_RESET_AUTHORIZED|FAC_DEMO_HTTP_PORT|FAC_DEMO_BIND_ADDRESS)=' .env.demo || true

echo
echo "[3/7] A preparar e reconstruir a demonstração..."
./scripts/demo/linux/prepare-demo.sh

echo
echo "[4/7] A forçar rebuild do frontend/Nginx para evitar cache antiga..."
docker compose \
  --project-directory "$PROJECT_ROOT" \
  --env-file "$PROJECT_ROOT/.env.demo" \
  -f "$PROJECT_ROOT/compose.demo.yaml" \
  build --no-cache frontend

echo
echo "[5/7] A repor exclusivamente fac_demo..."
./scripts/demo/linux/reset-demo.sh

echo
echo "[6/7] A executar verificação funcional..."
./scripts/demo/linux/check-demo.sh

echo
echo "[7/7] A apresentar estado final..."
./scripts/demo/linux/status-demo.sh

echo
echo "A confirmar proxy_pass dentro do container frontend..."
docker compose \
  --project-directory "$PROJECT_ROOT" \
  --env-file "$PROJECT_ROOT/.env.demo" \
  -f "$PROJECT_ROOT/compose.demo.yaml" \
  exec -T frontend \
  grep -n "proxy_pass" /etc/nginx/conf.d/default.conf

echo
echo "=================================================="
echo " FAC Demo preparada e validada."
echo " Acesso local: http://127.0.0.1:8088"
echo "=================================================="
