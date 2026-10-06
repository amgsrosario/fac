# TUULI AIR — Estado Operacional

Fonte canónica do estado corrente. Atualização: 2026-10-06, Europe/Lisbon. Substituir informação obsoleta quando o estado mudar; o histórico detalhado permanece no Git. Consultar também [INSTITUTIONAL_MEMORY.md](INSTITUTIONAL_MEMORY.md).

## Base produtiva confirmada

- Linha produtiva: `feature/visual-redesign`.
- HEAD remoto confirmado por fetch neste fecho pós-merge documental: `8877b9281f43212c479d5a96e5efb9a985a2d03a` (merge do PR #10, que integra `eac635e4504cdc95e3cdb4c4884a4075459b96d5`).
- Este identificador é a referência confirmada neste fecho, não o hash do commit que contém este ficheiro. Registar o próprio HEAD tornaria a atualização autorreferencial; não encadear commits apenas para acompanhar esta correção documental. Para obter o HEAD remoto corrente, atualizar as referências Git e consultar `origin/feature/visual-redesign`.

## JWT / Gestão e Invalidação de Sessões

**CONCLUÍDA E INTEGRADA. Segurança JWT: FECHADA. Capítulo global Segurança: AINDA NÃO FECHADO.**

| Entrega | Estado confirmado |
| --- | --- |
| PR principal #8 | Integrado; merge `acf74763a6b3a56000d9e7fc3c11a3a4958c0c59` |
| Implementação principal | `dc82d397ba9bdc7ec2672484c1ae92225c5d6f4a`, integrada |
| PR complementar #9 | Integrado; merge `b042b3bea660b8b5b65b87acc08ea331f75cc6f0` |
| Validação estrita e testes complementares | `ebb71bc8f91b8e60bb4a81b61436caadddf9ab32`, integrado |

A ancestralidade dos dois commits na base remota foi confirmada. Não existe trabalho funcional JWT por integrar.

### Validação final da missão

| Verificação | Resultado |
| --- | --- |
| SecurityIntegrationTests | 11 aprovados |
| UtilizadorControllerTests | 7 aprovados |
| TestDatabaseSafetyValidatorTests | 4 aprovados |
| Total dirigido | 22 aprovados, sem falhas ou erros |
| PostgreSQL de validação | 16.3 descartável |
| Flyway | 15 migrations validadas; schema V15 em duas inicializações |
| Package | `mvn -o -DskipTests package`: BUILD SUCCESS |
| Diff | `git diff --check`: aprovado |
| Revisão independente final | BLOCKER 0 / MAJOR 0 / MINOR 0; gate aprovado |
| CI do PR #9 | Verde, confirmado pelo Executivo na instrução de institucionalização |

A revisão independente foi estática, baseada no código anteriormente inspecionado e no diff complementar fornecido, sem reinspeção do commit final nem execução própria dos testes pelo revisor. Os testes e package foram executados pelo Executor. O CI do PR #9 não foi consultado diretamente nesta sessão, por ausência de cliente/conector GitHub disponível; a confirmação acima provém do Executivo.

V14 acrescenta `token_version` e V15 acrescenta `row_version`, ambas com valor inicial 0 e sem remoção de dados. A cobertura dirigida inclui login e acesso normal, reset/token antigo/password antiga e nova, desativação, reativação sem ressuscitar tokens, tokens expirados/adulterados/utilizador inexistente, claims inválidas e conflito otimista de entidade desatualizada.

### Fecho Git e evidência local

- Worktree `/home/arosario/Projetos/fac/.worktrees/jwt-session-invalidation`: removida.
- Branch `feature/jwt-session-invalidation`: removida localmente e em `origin` após confirmação da integração.
- Registos preservados: `/home/arosario/Projetos/fac/target/jwt-session-invalidation-validation` (logs de testes/package e três relatórios Surefire; ficheiros locais ignorados pelo Git, não arquivo institucional permanente).
- Checkout principal: alterações preexistentes em `docker-compose.yml` e `.worktrees/` preservadas e excluídas da institucionalização documental. A branch local principal estava atrás da referência remota; não foi atualizada nem usada como prova da base produtiva.
- Nenhuma alteração funcional adicional no fecho JWT. O estado da Fundação de Permissões encontra-se abaixo.

## Fundação de Permissões

**CONCLUÍDA E INTEGRADA. PR #12 merged; gate humano de merge concluído por António. Não existe trabalho funcional pendente desta missão.**

- Base da missão confirmada por fetch: origin/feature/visual-redesign em da5c75fbcf5744611a7de39d3e1dfa15b62b86e7. A referência documental anterior na secção de base produtiva conserva o significado histórico do respetivo fecho.
- HEAD produtivo observado e confirmado no fecho pós-merge: 967a14743ccc8a162811ac614f4be049863fb7f6 (merge do PR #12). A ancestralidade dos commits 2a319954b7d8de7c5a5c5a16b67d1a5b086491c4 e c67b0673c578d36a5b306c4cf8a56d29e7c90170 foi confirmada. Este é um registo do fecho, não um HEAD eternamente atual; consultar Git para o estado corrente.
- Branch feature/permissions-foundation removida localmente e em origin; worktree /home/arosario/.codex/worktrees/permissions-foundation/fac removida, com arquivo recuperável.
- Três perfis e mapa exato das 16 capacidades preservados. Contrato backend tipado e catálogo/helper frontend partilhados. PDF financeiro alinhado com DOCUMENTO_OBTER_PDF; acesso dos três perfis preservado.
- Mudanças efetivas de perfil incrementam token_version; tokens anteriores passam a 401. Pedidos com o mesmo perfil conservam a sessão. Novo login recebe as novas autoridades; último administrador, autoelevação e optimistic locking preservados.
- Nenhuma migration criada ou alterada. O teste de schema, anteriormente preso a V12, foi alinhado com V15 já existente e reforçado com verificação de token_version/row_version bigint não nulas, com default zero.
- Suite integral: 236 testes aprovados, zero falhas/erros/omitidos, na PostgreSQL 16.3 descartável. Inclui Segurança 23, matriz HTTP com login real 6, Utilizadores 7, Safety 4 e schema V15. A matriz usa operações e PDFs reais, sem mocks dos serviços de negócio.
- Frontend: TypeScript e build Vite aprovados; dois testes do helper aprovados com Playwright existente. Runtime Node Linux temporário utilizado para evitar limitações UNC do Node Windows, sem alterações globais ou ao CI.
- Package final: BUILD SUCCESS. Revisão independente por review_permissions_final: APROVADA, BLOCKER 0 / MAJOR 0 / MINOR 0. Revisão estática do diff completo e ficheiros novos; o revisor não repetiu os testes executados pelo Executor.
- PR #12 integrado em feature/visual-redesign, confirmado pelo histórico Git após fetch. O estado do CI não foi consultado diretamente pelo Executor; a confirmação do merge não é uma declaração autónoma de CI verde.
- Evidência local preservada em /home/arosario/Projetos/fac/target/permissions-foundation-validation (logs e revisão, ignorados pelo Git; não arquivo institucional permanente).
- Segurança global continua aberta. Security Gate está em execução na missão descrita abaixo; a Fundação de Permissões não foi reaberta.
- Alterações preexistentes do checkout principal em docker-compose.yml e .worktrees/ preservadas e excluídas desta missão.

## Security Gate

**IMPLEMENTADO E VALIDADO LOCALMENTE; AINDA NÃO INTEGRADO. Segurança global permanece aberta até merge humano e decisão formal do Executivo.**

- Base confirmada: `origin/feature/visual-redesign`, observada em `b32720a8b7060314f1d7c3b8f79181bd41df3ca3`.
- Entrega isolada: branch `feature/security-gate`, worktree `/home/arosario/Projetos/fac/.worktrees/security-gate`.
- F1: bypass operacional recusado no arranque; excepção confinada ao contexto de testes e ausente do JAR operacional.
- F2: secret operacional obrigatório/validado; chave aleatória apenas em desenvolvimento explicitamente activado/teste autorizado; duração JWT 1–1440 minutos, default 60.
- F3: limiter próprio da aplicação por identificador/origem/global, limitado em memória por instância; resposta 429, recuperação automática, logs agregados sem credenciais; BCrypt nos caminhos de credenciais inválidas.
- F4: limites precoces de CSV, admissão XLSX com expansão/estrutura limitadas incluindo parts OPC não canónicos, e formato/dimensões de imagens antes de decode. Limites funcionais e protecções de exportação preservados.
- F5: claims essenciais e tipos validados antes de consulta de identidade; issuer `fac`, expiração obrigatória, versão persistida e invalidação existentes preservadas; sem audience nova.
- F6: herança Nginx corrigida e CSP simples; headers HTTP efectivos verificados em páginas, health e erro proxy. Frontend real renderizado sob CSP em navegador, sem violações observadas no smoke de login.
- Suite integral: 259 testes em 47 classes, zero falhas/erros/omitidos, com PostgreSQL 16.3 descartável e Flyway V15. Backend package e frontend TypeScript/Vite aprovados. Após endurecer o fallback de perfil dev implícito, regressão afectada/package repetidos antes da entrega.
- Revisão independente: MAJOR sobre worksheet não canónica corrigido e testado; revisão final da implementação/documentação APROVADA, BLOCKER 0 / MAJOR 0 / MINOR 0 (revisão estática; testes executados pelo Executor). PR/CI ainda pendentes de publicação; merge não executado.
- Instalação pelo lockfile frontend reportou 7 vulnerabilidades (1 moderada, 6 altas). Não houve investigação externa de CVEs nem upgrade de dependências; avaliação de dependências permanece trabalho separado do Executivo.
- Resíduos aceites: localStorage, logout local, ausência de refresh tokens/sessões individuais, auditoria selectiva de recusas. Limiter volátil por instância: restart/múltiplas instâncias alteram orçamento; origem pode ser partilhada atrás de proxy. PDF conserva materialização final e layout.
- Produção pública exige HTTPS. TLS/redireccionamento/certificados/renovação/HSTS no edge, firewall e gestão/rotação de secrets são responsabilidades do deployment. Contrato completo em [SECURITY_GATE.md](../security/SECURITY_GATE.md).
- Checkout principal e respectivas alterações preexistentes preservados. Sem migration, nova permissão, alteração de CI ou Fecho Fiscal iniciado.

## Sequência estratégica vigente

1. JWT / Gestão e Invalidação de Sessões — **CONCLUÍDA**.
2. Fundação de Permissões — **CONCLUÍDA E INTEGRADA; GATE HUMANO CONCLUÍDO**.
3. Security Gate — **IMPLEMENTADO LOCALMENTE; INTEGRAÇÃO E FECHO EXECUTIVO PENDENTES**.
4. Fecho Fiscal Sistemático.
5. Auditoria Funcional Final.
6. Preparação e Certificação AT.
7. Piloto Controlado.

Esta sequência é a orientação vigente do Executivo. A conclusão JWT não antecipa os gates seguintes nem declara certificação ou prontidão global de segurança.

## Resíduos e limites

- Não foi criado um fluxo de alteração de password pelo próprio utilizador; o caminho implementado nesta missão é o reset administrativo existente.
- Logout permanece client-side; não existe revogação individual de JWT introduzida nesta missão.
- Fundação de Permissões integrada, sem trabalho funcional pendente. O capítulo global Segurança continua aberto; Security Gate está implementado localmente, sujeito aos gates descritos abaixo.
- A publicação documental segue o fluxo de branch/PR contra `feature/visual-redesign`; o merge é uma decisão humana de António.
