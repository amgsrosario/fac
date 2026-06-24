# Manual operacional — FAC Demo Partner Edition em Ubuntu

## 1. Requisitos

- Ubuntu com acesso à rede apenas para instalação/atualização de dependências e imagens.
- Git, Docker Engine e Docker Compose v2.
- Pelo menos 4 GB de RAM livre e 8 GB de disco disponível para builds e imagens.
- Repositório em `/home/arosario/Projetos desenvolvimento/fac`, branch `main`.

Instalação típica com pacotes Ubuntu:

```bash
sudo apt update
sudo apt install -y git curl ca-certificates docker.io docker-compose-v2
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

Terminar a sessão e voltar a entrar depois de adicionar o utilizador ao grupo `docker`. Confirmar:

```bash
docker --version
docker compose version
docker info
```

Se a versão instalada do Ubuntu não disponibilizar `docker-compose-v2`, instalar Docker Engine e o plugin Compose pelo repositório oficial Docker para essa versão do Ubuntu; não instalar o antigo binário `docker-compose` v1.

## 2. Primeira instalação

Confirmar que não existem alterações locais antes de sincronizar:

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
git status --short
git switch main
git pull --ff-only
chmod +x scripts/demo/linux/*.sh
./scripts/demo/linux/prepare-demo.sh
```

Na primeira execução, o script cria `.env.demo`, aplica permissões restritas e termina com uma mensagem para preencher a configuração.

## 3. Configurar passwords e segredos

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
nano .env.demo
```

Manter obrigatoriamente:

```text
SPRING_PROFILES_ACTIVE=demo
FAC_DEMO_DATABASE=fac_demo
FAC_DEMO_RESET_AUTHORIZED=true
```

Substituir todos os valores que começam por `definir-`. A password da base deve ter pelo menos 12 caracteres, as três passwords de utilizador pelo menos 8 e `FAC_JWT_SECRET` pelo menos 32. Usar valores distintos e não os partilhar por Git, mensagens ou capturas de ecrã.

Não usar espaços, `#`, aspas ou `$` nos valores do ficheiro; preferir passwords longas compostas por letras, números, hífen e underscore. Confirmar que o ficheiro não é versionado:

```bash
chmod 600 .env.demo
git check-ignore -v .env.demo
./scripts/demo/linux/prepare-demo.sh
```

## 4. Reset inicial ou reposição da demonstração

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/reset-demo.sh
```

Este comando elimina e recria apenas `fac_demo`, executa migrações e seed, e arranca a aplicação. Não altera a base normal `fac`. Pode ser repetido para restaurar o estado comercial conhecido.

## 5. Verificação estritamente de leitura

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/check-demo.sh
```

O resultado esperado inclui `FAC_DEMO_CHECK OK`. Uma falha deve ser investigada antes da demonstração.

## 6. Operação diária

Arrancar:

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/start-demo.sh
```

Consultar estado:

```bash
./scripts/demo/linux/status-demo.sh
```

Consultar logs de todos os serviços (terminar com `Ctrl+C`):

```bash
./scripts/demo/linux/logs-demo.sh
```

Consultar apenas um serviço:

```bash
./scripts/demo/linux/logs-demo.sh backend
./scripts/demo/linux/logs-demo.sh frontend
./scripts/demo/linux/logs-demo.sh db
```

Parar sem apagar dados:

```bash
./scripts/demo/linux/stop-demo.sh
```

Reiniciar:

```bash
./scripts/demo/linux/restart-demo.sh
```

## 7. Acesso local e pela rede interna

No Ubuntu, abrir:

```text
http://127.0.0.1:8088
```

Obter o primeiro IP local e confirmar a escuta:

```bash
hostname -I
ss -lnt | grep ':8088'
curl --fail http://127.0.0.1:8088/health
```

Noutro computador ou telemóvel ligado à mesma LAN, abrir:

```text
http://IP_DO_UBUNTU:8088
```

Se UFW estiver ativo, determinar a sub-rede local correta (exemplo `192.168.1.0/24`) e permitir apenas essa origem:

```bash
sudo ufw status
sudo ufw allow from 192.168.1.0/24 to any port 8088 proto tcp
sudo ufw status numbered
```

Não abrir 5432 ou 8080, não criar encaminhamento no router e não expor 8088 à Internet.

## 8. Atualizar o projeto

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/stop-demo.sh
git status --short
git switch main
git pull --ff-only
chmod +x scripts/demo/linux/*.sh
./scripts/demo/linux/prepare-demo.sh
./scripts/demo/linux/reset-demo.sh
./scripts/demo/linux/check-demo.sh
```

`.env.demo` e o volume persistem. Se o contrato do exemplo tiver novas variáveis, compará-lo sem copiar por cima dos segredos:

```bash
diff -u .env.demo.example .env.demo
```

## 9. Alterar passwords

Editar `.env.demo` e executar:

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/prepare-demo.sh
./scripts/demo/linux/reset-demo.sh
./scripts/demo/linux/check-demo.sh
```

O reset sincroniza a password do role PostgreSQL sem a escrever nos logs e recria os utilizadores demo com as novas passwords.

## 10. Recuperação e diagnóstico

Após reinício do Ubuntu:

```bash
sudo systemctl status docker --no-pager
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/status-demo.sh
```

Se necessário:

```bash
sudo systemctl start docker
./scripts/demo/linux/start-demo.sh
./scripts/demo/linux/check-demo.sh
```

Problemas frequentes:

- **Docker sem permissão:** terminar sessão e voltar a entrar; confirmar `groups` e `docker info`.
- **Porta 8088 ocupada:** identificar com `sudo ss -lntp | grep ':8088'`; escolher outra porta livre em `FAC_DEMO_HTTP_PORT` e repetir `start-demo.sh`.
- **Serviço unhealthy:** executar `logs-demo.sh backend` ou o serviço indicado, corrigir e usar `restart-demo.sh`.
- **Base temporariamente indisponível:** o backend espera pelo healthcheck; verificar `logs-demo.sh db` e espaço em disco com `df -h`.
- **Frontend abre mas API falha:** confirmar que backend está healthy e consultar logs de frontend/backend; o browser deve usar sempre a porta pública do Nginx.
- **Configuração recusada:** corrigir `.env.demo`; nunca contornar as validações mudando os scripts.
- **Estado funcional incoerente:** executar `reset-demo.sh` e depois `check-demo.sh`.

Reiniciar apenas um container, preservando dados:

```bash
docker compose --project-directory "/home/arosario/Projetos desenvolvimento/fac" --env-file "/home/arosario/Projetos desenvolvimento/fac/.env.demo" -f "/home/arosario/Projetos desenvolvimento/fac/compose.demo.yaml" restart backend
./scripts/demo/linux/status-demo.sh
```

## 11. Confirmações antes de cada demonstração

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
grep -E '^(SPRING_PROFILES_ACTIVE|FAC_DEMO_DATABASE)=' .env.demo
./scripts/demo/linux/check-demo.sh
./scripts/demo/linux/status-demo.sh
```

O resultado deve mostrar exclusivamente `SPRING_PROFILES_ACTIVE=demo` e `FAC_DEMO_DATABASE=fac_demo`, sem imprimir segredos. Confirmar login dos perfis administrador, operador e consulta; permissões; documentos, PDF, ATCUD/QR Code, pagamentos, extratos e auditoria.

## 12. Limites

Este procedimento é apenas local/LAN. Não configura domínio, TLS público, router, túnel, VPN, cloud, backups externos ou produção. A reinicialização física do Ubuntu e o teste por outro equipamento devem ser feitos no anfitrião real.
