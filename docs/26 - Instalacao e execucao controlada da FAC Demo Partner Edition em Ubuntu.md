# MD 26 — Instalação e execução controlada da FAC Demo Partner Edition em Ubuntu

## Resultado e decisão técnica

A edição de demonstração usa Docker Compose para os três componentes: PostgreSQL, backend Spring Boot e frontend estático servido por Nginx. Esta solução reduz dependências no Ubuntu, isola a configuração demo e permite recuperação após reinício através de `restart: unless-stopped` e do arranque automático do Docker. Não é criado um serviço `systemd` adicional.

O Nginx é o único serviço publicado no anfitrião. Encaminha `/api` para o backend e serve a SPA. PostgreSQL e backend permanecem apenas na rede privada do Compose.

| Componente | Porta no container | Porta no Ubuntu | Exposição |
|---|---:|---:|---|
| Nginx/frontend | 80 | 8088 por omissão | Local e LAN |
| Backend | 8080 | nenhuma | Rede Compose |
| PostgreSQL | 5432 | nenhuma | Rede Compose |

Esta configuração é específica da demonstração; não substitui configurações de desenvolvimento, teste ou futura produção.

## Salvaguardas

Os scripts recusam a execução quando `SPRING_PROFILES_ACTIVE` não é exatamente `demo`, `FAC_DEMO_DATABASE` não é exatamente `fac_demo`, faltam segredos obrigatórios, existem marcadores do ficheiro exemplo, o endereço de escuta é ambíguo ou o Docker não está disponível. O reset contém ainda o nome literal `fac_demo` nas operações destrutivas e exige `FAC_DEMO_RESET_AUTHORIZED=true`.

`.env.demo` é local, tem permissões `600` e está ignorado pelo Git. Os segredos não são passados na linha de comandos, impressos pelos scripts ou incluídos nesta documentação. Os logs Docker são limitados a três ficheiros de 10 MB por serviço.

O volume nomeado `fac-demo-postgres-data` preserva os dados entre recriações dos containers e reinícios. `stop-demo.sh` não elimina o volume. Não executar `docker compose down -v`, salvo quando a destruição deliberada de toda a demonstração for pretendida.

## Ficheiros da solução

- `compose.demo.yaml`: stack isolado `fac-demo`.
- `deploy/demo/Dockerfile.backend`: build Maven e runtime Java 21 sem privilégios.
- `deploy/demo/Dockerfile.frontend`: build Node 20 e runtime Nginx.
- `deploy/demo/nginx.conf`: SPA, healthcheck, reverse proxy `/api` e upload ate 10 MB para importacao de dados mestres.
- `.dockerignore`: exclui segredos, artefactos e dados locais do contexto de build.
- `.env.demo.example`: contrato de configuração sem segredos.
- `scripts/demo/linux/*.sh`: operações de alto nível.
- `docs/demo/manual-operacional-ubuntu.md`: procedimento diário completo.

## Instalação e validação

O procedimento executável encontra-se no manual operacional. A sequência da primeira instalação é:

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
chmod +x scripts/demo/linux/*.sh
./scripts/demo/linux/prepare-demo.sh
nano .env.demo
./scripts/demo/linux/prepare-demo.sh
./scripts/demo/linux/reset-demo.sh
./scripts/demo/linux/check-demo.sh
./scripts/demo/linux/status-demo.sh
```

O primeiro `prepare-demo.sh` cria `.env.demo` a partir do exemplo e termina em erro intencional para obrigar ao preenchimento dos segredos. A segunda execução valida, constrói as imagens e prepara apenas o PostgreSQL demo. O reset recria exclusivamente `fac_demo`, executa Flyway e o seed, e arranca a aplicação. O check executa com Flyway desativado, `ddl-auto=validate` e verificação de leitura.

## Acesso e rede

No Ubuntu, abrir `http://127.0.0.1:8088`. Na LAN, obter o endereço com `hostname -I` e abrir `http://IP_DO_UBUNTU:8088` noutro dispositivo. Apenas TCP 8088 deve ser permitido na firewall, idealmente limitado à sub-rede local. Não abrir portas no router.

## Recuperação após reinício

O Docker deve estar ativo no arranque (`sudo systemctl enable --now docker`). Os containers já criados recuperam automaticamente devido a `restart: unless-stopped`. Confirmar com:

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/status-demo.sh
```

Se tiverem sido parados expressamente antes do reinício, usar `start-demo.sh`. Um serviço `systemd` próprio não acrescentaria robustez proporcional nesta fase e duplicaria a gestão de ciclo de vida do Compose.

## Limites e validação física

Esta entrega não publica a aplicação na Internet, não configura domínio, HTTPS público, router, túnel, VPN, cloud ou backups externos. A validação final no computador Ubuntu — incluindo IP real, UFW, reinício físico e testes a partir de outro equipamento — tem de ser executada nesse anfitrião. Não deve ser declarada concluída apenas com os testes Windows/Linux-container do repositório.

Para o MD 27 ficam a estratégia de publicação segura, TLS, domínio, backups e operação de produção, sem reutilizar automaticamente esta configuração demo como produção.

## Registo de validação desta entrega

Validações concluídas no ambiente de desenvolvimento:

- configuração Compose analisada com `docker compose ... config --quiet`;
- sintaxe dos nove scripts Bash validada com Bash do Git for Windows;
- cinco cenários de recusa executados: base `fac` no reset, perfil incorreto, reset sem passwords, base `fac` no check e arranque sem `.env.demo`; resultado `5/5`;
- teste HTTP de segurança acrescentado para operador: auditoria, configuração da empresa e gestão de utilizadores devolvem `403`;
- suite Maven: 126 testes, 0 falhas, 0 erros e 0 ignorados;
- build Maven final concluído;
- TypeScript e build Vite concluídos.

A execução das imagens ficou bloqueada neste ambiente porque o Docker não conseguiu resolver `registry-1.docker.io` e o build não conseguiu obter as imagens base. Consequentemente, reset/check em containers, logins dos três perfis, PDF e persistência após reinício não são declarados validados pelo MD 26 neste anfitrião. Devem ser executados no Ubuntu pela sequência deste manual; o documento só pode ser marcado como integralmente aceite depois desse registo físico.
